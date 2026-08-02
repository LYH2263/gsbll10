package com.example.picture.service;

import com.example.picture.dto.AlbumDTO;
import com.example.picture.dto.AlbumDetailDTO;
import com.example.picture.dto.AlbumRequest;
import com.example.picture.dto.BatchFailedItem;
import com.example.picture.dto.BatchOperationResult;
import com.example.picture.dto.BatchResultDTO;
import com.example.picture.dto.CoverDTO;
import com.example.picture.entity.Album;
import com.example.picture.entity.AlbumPicture;
import com.example.picture.entity.Picture;
import com.example.picture.exception.BusinessException;
import com.example.picture.repository.AlbumPictureRepository;
import com.example.picture.repository.AlbumRepository;
import com.example.picture.repository.PictureRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class AlbumService {

    @Autowired
    private AlbumRepository albumRepository;

    @Autowired
    private AlbumPictureRepository albumPictureRepository;

    @Autowired
    private PictureRepository pictureRepository;

    @Transactional
    public AlbumDTO create(AlbumRequest request) {
        validateName(request.getName());
        validateDescription(request.getDescription());

        Date now = new Date();
        Album album = new Album();
        album.setName(request.getName().trim());
        album.setDescription(trimToNull(request.getDescription()));
        album.setCoverPictureId(null);
        album.setCreateTime(now);
        album.setUpdateTime(now);
        album = albumRepository.save(album);

        return AlbumDTO.from(album, null, 0L);
    }

    @Transactional
    public AlbumDTO rename(Long id, AlbumRequest request) {
        Album album = getAlbumOrThrow(id);
        validateName(request.getName());
        validateDescription(request.getDescription());

        album.setName(request.getName().trim());
        album.setDescription(trimToNull(request.getDescription()));
        album.setUpdateTime(new Date());
        album = albumRepository.save(album);

        return toDTO(album);
    }

    @Transactional
    public void delete(Long id) {
        Album album = getAlbumOrThrow(id);
        albumPictureRepository.deleteByAlbumId(id);
        albumRepository.delete(album);
    }

    @Transactional(readOnly = true)
    public List<AlbumDTO> list() {
        List<Album> albums = albumRepository.findAllByOrderByUpdateTimeDesc();
        if (albums.isEmpty()) {
            return new ArrayList<AlbumDTO>();
        }

        List<Long> albumIds = new ArrayList<Long>();
        for (Album a : albums) {
            albumIds.add(a.getId());
        }

        List<AlbumPicture> memberships = albumPictureRepository.findByAlbumIdIn(albumIds);
        Map<Long, List<AlbumPicture>> byAlbum = groupByAlbum(memberships);

        List<Long> pictureIds = collectPictureIds(memberships);
        Map<Long, Picture> pictureMap = loadPictures(pictureIds);

        List<AlbumDTO> result = new ArrayList<AlbumDTO>();
        for (Album album : albums) {
            List<AlbumPicture> members = byAlbum.get(album.getId());
            if (members == null) {
                members = Collections.emptyList();
            }
            CoverDTO cover = resolveCover(album, members, pictureMap);
            result.add(AlbumDTO.from(album, cover, members.size()));
        }
        return result;
    }

    @Transactional(readOnly = true)
    public AlbumDetailDTO detail(Long id) {
        Album album = getAlbumOrThrow(id);
        return buildDetail(album);
    }

    private AlbumDetailDTO buildDetail(Album album) {
        List<AlbumPicture> members = albumPictureRepository
                .findByAlbumIdOrderBySortOrderAsc(album.getId());

        List<Long> pictureIds = new ArrayList<Long>();
        for (AlbumPicture ap : members) {
            pictureIds.add(ap.getPictureId());
        }
        Map<Long, Picture> pictureMap = loadPictures(pictureIds);

        List<Picture> pictures = new ArrayList<Picture>();
        for (AlbumPicture ap : members) {
            Picture p = pictureMap.get(ap.getPictureId());
            if (p != null) {
                pictures.add(p);
            }
        }

        CoverDTO cover = resolveCover(album, members, pictureMap);
        AlbumDTO dto = AlbumDTO.from(album, cover, members.size());
        return AlbumDetailDTO.from(dto, pictures);
    }

    @Transactional
    public AlbumDTO addPicture(Long albumId, Long pictureId) {
        Album album = getAlbumOrThrow(albumId);
        if (pictureId == null) {
            throw new BusinessException("图片不存在");
        }
        Picture picture = pictureRepository.findById(pictureId).orElse(null);
        if (picture == null) {
            throw new BusinessException(1002, "图片不存在");
        }

        AlbumPicture existing = albumPictureRepository
                .findByAlbumIdAndPictureId(albumId, pictureId).orElse(null);
        if (existing == null) {
            Integer minSort = albumPictureRepository.findMinSortOrder(albumId);
            int newSort = (minSort == null ? 0 : minSort) - 1;

            AlbumPicture ap = new AlbumPicture();
            ap.setAlbumId(albumId);
            ap.setPictureId(pictureId);
            ap.setSortOrder(newSort);
            ap.setJoinTime(new Date());
            albumPictureRepository.save(ap);
        }

        album.setUpdateTime(new Date());
        album = albumRepository.save(album);
        return toDTO(album);
    }

    @Transactional
    public AlbumDTO removePicture(Long albumId, Long pictureId) {
        Album album = getAlbumOrThrow(albumId);
        AlbumPicture existing = albumPictureRepository
                .findByAlbumIdAndPictureId(albumId, pictureId).orElse(null);
        if (existing != null) {
            albumPictureRepository.delete(existing);
        }

        if (album.getCoverPictureId() != null
                && album.getCoverPictureId().equals(pictureId)) {
            album.setCoverPictureId(null);
        }
        album.setUpdateTime(new Date());
        album = albumRepository.save(album);
        return toDTO(album);
    }

    @Transactional
    public BatchOperationResult batchAdd(Long albumId, List<Long> pictureIds) {
        Album album = getAlbumOrThrow(albumId);
        BatchResultDTO result = new BatchResultDTO();
        if (pictureIds == null) {
            pictureIds = new ArrayList<Long>();
        }

        List<Long> uniqueIds = new ArrayList<Long>();
        for (Long id : pictureIds) {
            if (id != null && !uniqueIds.contains(id)) {
                uniqueIds.add(id);
            }
        }
        result.setRequested(uniqueIds.size());
        if (uniqueIds.isEmpty()) {
            AlbumDetailDTO detail = buildDetail(album);
            return new BatchOperationResult(result, toAlbumDTO(album, detail), detail);
        }

        Integer minSort = albumPictureRepository.findMinSortOrder(albumId);
        int baseSort = (minSort == null ? 0 : minSort) - uniqueIds.size();
        Date now = new Date();

        int offset = 0;
        for (Long pictureId : uniqueIds) {
            AlbumPicture existing = albumPictureRepository
                    .findByAlbumIdAndPictureId(albumId, pictureId).orElse(null);
            if (existing != null) {
                result.getIgnored().add(pictureId);
                continue;
            }
            Picture picture = pictureRepository.findById(pictureId).orElse(null);
            if (picture == null) {
                result.getFailed().add(new BatchFailedItem(pictureId, "图片不存在"));
                continue;
            }
            AlbumPicture ap = new AlbumPicture();
            ap.setAlbumId(albumId);
            ap.setPictureId(pictureId);
            ap.setSortOrder(baseSort + offset);
            ap.setJoinTime(now);
            albumPictureRepository.save(ap);
            result.getSucceeded().add(pictureId);
            offset++;
        }

        album.setUpdateTime(now);
        album = albumRepository.save(album);
        AlbumDetailDTO detail = buildDetail(album);
        return new BatchOperationResult(result, toAlbumDTO(album, detail), detail);
    }

    @Transactional
    public BatchOperationResult batchRemove(Long albumId, List<Long> pictureIds) {
        Album album = getAlbumOrThrow(albumId);
        BatchResultDTO result = new BatchResultDTO();
        if (pictureIds == null) {
            pictureIds = new ArrayList<Long>();
        }

        List<Long> uniqueIds = new ArrayList<Long>();
        for (Long id : pictureIds) {
            if (id != null && !uniqueIds.contains(id)) {
                uniqueIds.add(id);
            }
        }
        result.setRequested(uniqueIds.size());
        if (uniqueIds.isEmpty()) {
            AlbumDetailDTO detail = buildDetail(album);
            return new BatchOperationResult(result, toAlbumDTO(album, detail), detail);
        }

        boolean coverCleared = false;
        for (Long pictureId : uniqueIds) {
            AlbumPicture existing = albumPictureRepository
                    .findByAlbumIdAndPictureId(albumId, pictureId).orElse(null);
            if (existing == null) {
                result.getIgnored().add(pictureId);
                continue;
            }
            albumPictureRepository.delete(existing);
            result.getSucceeded().add(pictureId);
            if (album.getCoverPictureId() != null
                    && album.getCoverPictureId().equals(pictureId)) {
                album.setCoverPictureId(null);
                coverCleared = true;
            }
        }

        if (!result.getSucceeded().isEmpty() || coverCleared) {
            album.setUpdateTime(new Date());
            album = albumRepository.save(album);
        }
        AlbumDetailDTO detail = buildDetail(album);
        return new BatchOperationResult(result, toAlbumDTO(album, detail), detail);
    }

    @Transactional
    public AlbumDTO setCover(Long albumId, Long pictureId) {
        Album album = getAlbumOrThrow(albumId);
        if (pictureId == null) {
            throw new BusinessException("图片不存在");
        }
        AlbumPicture membership = albumPictureRepository
                .findByAlbumIdAndPictureId(albumId, pictureId).orElse(null);
        if (membership == null) {
            throw new BusinessException("封面必须是相册成员");
        }
        Picture picture = pictureRepository.findById(pictureId).orElse(null);
        if (picture == null) {
            throw new BusinessException(1002, "图片不存在");
        }
        album.setCoverPictureId(pictureId);
        album.setUpdateTime(new Date());
        album = albumRepository.save(album);
        return toDTO(album);
    }

    @Transactional
    public AlbumDTO clearCover(Long albumId) {
        Album album = getAlbumOrThrow(albumId);
        if (album.getCoverPictureId() != null) {
            album.setCoverPictureId(null);
            album.setUpdateTime(new Date());
            album = albumRepository.save(album);
        }
        return toDTO(album);
    }

    @Transactional
    public AlbumDTO updateOrder(Long albumId, List<Long> pictureIds) {
        Album album = getAlbumOrThrow(albumId);
        if (pictureIds == null) {
            throw new BusinessException("排序参数不能为空");
        }

        List<AlbumPicture> members = albumPictureRepository.findByAlbumIdOrderBySortOrderAsc(albumId);
        Map<Long, AlbumPicture> memberMap = new HashMap<Long, AlbumPicture>();
        for (AlbumPicture ap : members) {
            memberMap.put(ap.getPictureId(), ap);
        }

        int order = 0;
        for (Long pictureId : pictureIds) {
            AlbumPicture ap = memberMap.get(pictureId);
            if (ap != null) {
                ap.setSortOrder(order);
                albumPictureRepository.save(ap);
                memberMap.remove(pictureId);
                order++;
            }
        }
        for (AlbumPicture remaining : memberMap.values()) {
            remaining.setSortOrder(order);
            albumPictureRepository.save(remaining);
            order++;
        }

        album.setUpdateTime(new Date());
        album = albumRepository.save(album);
        return toDTO(album);
    }

    @Transactional
    public void onPictureDeletedFromLibrary(Long pictureId) {
        List<Long> albumIds = albumPictureRepository.findAlbumIdsByPictureId(pictureId);
        if (albumIds != null && !albumIds.isEmpty()) {
            for (Long albumId : albumIds) {
                Album album = albumRepository.findById(albumId).orElse(null);
                if (album != null && pictureId.equals(album.getCoverPictureId())) {
                    album.setCoverPictureId(null);
                    album.setUpdateTime(new Date());
                    albumRepository.save(album);
                }
            }
        }
        albumPictureRepository.deleteByPictureId(pictureId);
    }

    private Album getAlbumOrThrow(Long id) {
        Album album = albumRepository.findById(id).orElse(null);
        if (album == null) {
            throw new BusinessException(1001, "相册不存在");
        }
        return album;
    }

    private AlbumDTO toDTO(Album album) {
        List<AlbumPicture> members = albumPictureRepository.findByAlbumIdOrderBySortOrderAsc(album.getId());
        Map<Long, Picture> pictureMap = loadPictures(collectPictureIds(members));
        CoverDTO cover = resolveCover(album, members, pictureMap);
        return AlbumDTO.from(album, cover, members.size());
    }

    private AlbumDTO toAlbumDTO(Album album, AlbumDetailDTO detail) {
        return AlbumDTO.from(album, detail.getCover(), detail.getPictureCount());
    }

    private CoverDTO resolveCover(Album album, List<AlbumPicture> members, Map<Long, Picture> pictureMap) {
        Long coverId = album.getCoverPictureId();
        if (coverId != null) {
            Picture p = pictureMap.get(coverId);
            if (p != null) {
                return toCover(p);
            }
        }
        AlbumPicture latest = null;
        for (AlbumPicture ap : members) {
            if (pictureMap.get(ap.getPictureId()) == null) {
                continue;
            }
            if (latest == null || ap.getJoinTime().after(latest.getJoinTime())) {
                latest = ap;
            }
        }
        if (latest == null) {
            return null;
        }
        return toCover(pictureMap.get(latest.getPictureId()));
    }

    private CoverDTO toCover(Picture p) {
        return new CoverDTO(p.getId(), p.getUrl(), p.getName());
    }

    private Map<Long, Picture> loadPictures(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return new HashMap<Long, Picture>();
        }
        List<Picture> pictures = pictureRepository.findAllById(ids);
        Map<Long, Picture> map = new HashMap<Long, Picture>();
        for (Picture p : pictures) {
            map.put(p.getId(), p);
        }
        return map;
    }

    private List<Long> collectPictureIds(List<AlbumPicture> memberships) {
        List<Long> ids = new ArrayList<Long>();
        for (AlbumPicture ap : memberships) {
            if (!ids.contains(ap.getPictureId())) {
                ids.add(ap.getPictureId());
            }
        }
        return ids;
    }

    private Map<Long, List<AlbumPicture>> groupByAlbum(List<AlbumPicture> memberships) {
        Map<Long, List<AlbumPicture>> map = new HashMap<Long, List<AlbumPicture>>();
        for (AlbumPicture ap : memberships) {
            List<AlbumPicture> list = map.get(ap.getAlbumId());
            if (list == null) {
                list = new ArrayList<AlbumPicture>();
                map.put(ap.getAlbumId(), list);
            }
            list.add(ap);
        }
        return map;
    }

    private void validateName(String name) {
        if (name == null || name.trim().isEmpty()) {
            throw new BusinessException("相册名不能为空");
        }
        if (name.trim().length() > 50) {
            throw new BusinessException("相册名长度不能超过50个字符");
        }
    }

    private void validateDescription(String description) {
        if (description != null && description.length() > 200) {
            throw new BusinessException("简介长度不能超过200个字符");
        }
    }

    private String trimToNull(String s) {
        if (s == null) {
            return null;
        }
        String t = s.trim();
        return t.isEmpty() ? null : t;
    }
}
