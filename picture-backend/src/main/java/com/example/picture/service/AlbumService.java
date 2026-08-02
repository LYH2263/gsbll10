package com.example.picture.service;

import com.example.picture.dto.AlbumDetailView;
import com.example.picture.dto.AlbumView;
import com.example.picture.dto.BatchResult;
import com.example.picture.entity.Album;
import com.example.picture.entity.AlbumMembership;
import com.example.picture.entity.Picture;
import com.example.picture.repository.AlbumMembershipRepository;
import com.example.picture.repository.AlbumRepository;
import com.example.picture.repository.PictureRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class AlbumService {

    @Autowired
    private AlbumRepository albumRepository;

    @Autowired
    private AlbumMembershipRepository membershipRepository;

    @Autowired
    private PictureRepository pictureRepository;

    @Transactional
    public Album createAlbum(String name, String description) {
        String trimmedName = name == null ? "" : name.trim();
        if (trimmedName.isEmpty()) {
            throw new AlbumException(400, "相册名不能为空");
        }
        if (trimmedName.length() > 50) {
            throw new AlbumException(400, "相册名不能超过50个字符");
        }
        if (description != null && description.length() > 200) {
            throw new AlbumException(400, "相册简介不能超过200个字符");
        }

        Album album = new Album();
        album.setName(trimmedName);
        album.setDescription(description);
        album.setCoverPictureId(null);
        Date now = new Date();
        album.setCreateTime(now);
        album.setUpdateTime(now);
        return albumRepository.save(album);
    }

    @Transactional
    public Album updateAlbum(Long id, String name, String description) {
        Album album = getAlbumOrThrow(id);

        if (name != null) {
            String trimmedName = name.trim();
            if (trimmedName.isEmpty()) {
                throw new AlbumException(400, "相册名不能为空");
            }
            if (trimmedName.length() > 50) {
                throw new AlbumException(400, "相册名不能超过50个字符");
            }
            album.setName(trimmedName);
        }
        if (description != null) {
            if (description.length() > 200) {
                throw new AlbumException(400, "相册简介不能超过200个字符");
            }
            album.setDescription(description);
        }
        album.setUpdateTime(new Date());
        return albumRepository.save(album);
    }

    @Transactional
    public void deleteAlbum(Long id) {
        Album album = getAlbumOrThrow(id);
        membershipRepository.deleteAllByAlbumId(id);
        albumRepository.delete(album);
    }

    public List<AlbumView> listAlbums() {
        List<Album> albums = albumRepository.findAllByOrderByUpdateTimeDesc();
        List<AlbumView> views = new ArrayList<AlbumView>();
        for (Album album : albums) {
            views.add(toAlbumView(album));
        }
        return views;
    }

    public AlbumDetailView getAlbumDetail(Long id) {
        Album album = getAlbumOrThrow(id);
        AlbumDetailView view = new AlbumDetailView();
        view.setId(album.getId());
        view.setName(album.getName());
        view.setDescription(album.getDescription());
        view.setCoverPictureId(album.getCoverPictureId());
        view.setCreateTime(album.getCreateTime());
        view.setUpdateTime(album.getUpdateTime());

        List<AlbumMembership> memberships = membershipRepository.findByAlbumIdOrderBySortOrderAsc(id);
        List<Long> pictureIds = new ArrayList<Long>();
        for (AlbumMembership m : memberships) {
            pictureIds.add(m.getPictureId());
        }

        List<Picture> pictures;
        if (pictureIds.isEmpty()) {
            pictures = Collections.emptyList();
        } else {
            pictures = pictureRepository.findAllById(pictureIds);
            final Map<Long, Integer> sortMap = new HashMap<Long, Integer>();
            for (AlbumMembership m : memberships) {
                sortMap.put(m.getPictureId(), m.getSortOrder());
            }
            pictures.sort(new Comparator<Picture>() {
                @Override
                public int compare(Picture p1, Picture p2) {
                    Integer s1 = sortMap.get(p1.getId());
                    Integer s2 = sortMap.get(p2.getId());
                    if (s1 == null) s1 = Integer.MAX_VALUE;
                    if (s2 == null) s2 = Integer.MAX_VALUE;
                    return s1.compareTo(s2);
                }
            });
        }

        view.setPictures(pictures);
        view.setPictureCount(pictures.size());
        view.setCover(resolveCover(album, pictureIds));
        return view;
    }

    @Transactional
    public void addPicture(Long albumId, Long pictureId) {
        Album album = getAlbumOrThrow(albumId);
        Picture picture = pictureRepository.findById(pictureId)
                .orElseThrow(() -> new AlbumException(404, "图片不存在"));

        Optional<AlbumMembership> existing = membershipRepository.findByAlbumIdAndPictureId(albumId, pictureId);
        if (existing.isPresent()) {
            return;
        }

        Integer minSort = membershipRepository.findMinSortOrderByAlbumId(albumId);
        int newSort = (minSort == null ? 0 : minSort) - 1;

        AlbumMembership membership = new AlbumMembership();
        membership.setAlbumId(albumId);
        membership.setPictureId(pictureId);
        membership.setSortOrder(newSort);
        membership.setJoinTime(new Date());
        membershipRepository.save(membership);

        album.setUpdateTime(new Date());
        albumRepository.save(album);
    }

    @Transactional
    public void removePicture(Long albumId, Long pictureId) {
        Album album = getAlbumOrThrow(albumId);
        membershipRepository.findByAlbumIdAndPictureId(albumId, pictureId).ifPresent(m -> {
            membershipRepository.delete(m);
        });

        if (album.getCoverPictureId() != null && album.getCoverPictureId().equals(pictureId)) {
            album.setCoverPictureId(null);
        }
        album.setUpdateTime(new Date());
        albumRepository.save(album);
    }

    @Transactional
    public void setCover(Long albumId, Long pictureId) {
        Album album = getAlbumOrThrow(albumId);
        if (pictureId == null) {
            album.setCoverPictureId(null);
            album.setUpdateTime(new Date());
            albumRepository.save(album);
            return;
        }
        boolean isMember = membershipRepository.findByAlbumIdAndPictureId(albumId, pictureId).isPresent();
        if (!isMember) {
            throw new AlbumException(400, "封面必须是相册成员");
        }
        album.setCoverPictureId(pictureId);
        album.setUpdateTime(new Date());
        albumRepository.save(album);
    }

    @Transactional
    public void updateOrder(Long albumId, List<Long> pictureIds) {
        Album album = getAlbumOrThrow(albumId);
        if (pictureIds == null || pictureIds.isEmpty()) {
            return;
        }
        for (int i = 0; i < pictureIds.size(); i++) {
            Long pid = pictureIds.get(i);
            Optional<AlbumMembership> m = membershipRepository.findByAlbumIdAndPictureId(albumId, pid);
            if (m.isPresent()) {
                m.get().setSortOrder(i);
                membershipRepository.save(m.get());
            }
        }
        album.setUpdateTime(new Date());
        albumRepository.save(album);
    }

    @Transactional
    public BatchResult batchAddPictures(Long albumId, List<Long> pictureIds) {
        Album album = getAlbumOrThrow(albumId);
        BatchResult result = new BatchResult();

        if (pictureIds == null || pictureIds.isEmpty()) {
            result.setRequested(0);
            return result;
        }

        result.setRequested(pictureIds.size());
        Date now = new Date();

        Integer minSort = membershipRepository.findMinSortOrderByAlbumId(albumId);
        int sortCounter = (minSort == null ? 0 : minSort) - pictureIds.size();
        boolean changed = false;

        for (Long pid : pictureIds) {
            if (pid == null) {
                result.getFailed().add(new BatchResult.FailedItem(null, "图片ID为空"));
                continue;
            }
            Optional<Picture> picOpt = pictureRepository.findById(pid);
            if (!picOpt.isPresent()) {
                result.getFailed().add(new BatchResult.FailedItem(pid, "图片不存在"));
                continue;
            }
            Optional<AlbumMembership> existing = membershipRepository.findByAlbumIdAndPictureId(albumId, pid);
            if (existing.isPresent()) {
                result.getIgnored().add(pid);
                continue;
            }

            AlbumMembership membership = new AlbumMembership();
            membership.setAlbumId(albumId);
            membership.setPictureId(pid);
            membership.setSortOrder(sortCounter++);
            membership.setJoinTime(now);
            membershipRepository.save(membership);
            result.getSucceeded().add(pid);
            changed = true;
        }

        if (changed) {
            album.setUpdateTime(now);
            albumRepository.save(album);
        }

        return result;
    }

    @Transactional
    public BatchResult batchRemovePictures(Long albumId, List<Long> pictureIds) {
        Album album = getAlbumOrThrow(albumId);
        BatchResult result = new BatchResult();

        if (pictureIds == null || pictureIds.isEmpty()) {
            result.setRequested(0);
            return result;
        }

        result.setRequested(pictureIds.size());
        Date now = new Date();
        boolean coverChanged = false;
        boolean changed = false;

        for (Long pid : pictureIds) {
            if (pid == null) {
                result.getFailed().add(new BatchResult.FailedItem(null, "图片ID为空"));
                continue;
            }
            Optional<AlbumMembership> existing = membershipRepository.findByAlbumIdAndPictureId(albumId, pid);
            if (!existing.isPresent()) {
                result.getIgnored().add(pid);
                continue;
            }
            membershipRepository.delete(existing.get());
            result.getSucceeded().add(pid);
            changed = true;

            if (album.getCoverPictureId() != null && album.getCoverPictureId().equals(pid)) {
                album.setCoverPictureId(null);
                coverChanged = true;
            }
        }

        if (changed) {
            album.setUpdateTime(now);
            albumRepository.save(album);
        }

        return result;
    }

    @Transactional
    public void onPictureDeleted(Long pictureId) {
        List<Album> coverAlbums = albumRepository.findByCoverPictureId(pictureId);
        Date now = new Date();
        for (Album a : coverAlbums) {
            a.setCoverPictureId(null);
            a.setUpdateTime(now);
            albumRepository.save(a);
        }
        membershipRepository.deleteAllByPictureId(pictureId);
    }

    private Album getAlbumOrThrow(Long id) {
        return albumRepository.findById(id)
                .orElseThrow(() -> new AlbumException(404, "相册不存在"));
    }

    private AlbumView toAlbumView(Album album) {
        AlbumView view = new AlbumView();
        view.setId(album.getId());
        view.setName(album.getName());
        view.setDescription(album.getDescription());
        view.setCoverPictureId(album.getCoverPictureId());
        view.setCreateTime(album.getCreateTime());
        view.setUpdateTime(album.getUpdateTime());

        long count = membershipRepository.countByAlbumId(album.getId());
        view.setPictureCount((int) count);

        List<Long> pictureIds = membershipRepository.findPictureIdsByAlbumId(album.getId());
        view.setCover(resolveCover(album, pictureIds));

        return view;
    }

    private AlbumView.CoverInfo resolveCover(Album album, List<Long> memberPictureIds) {
        if (memberPictureIds == null || memberPictureIds.isEmpty()) {
            return null;
        }

        Long coverId = null;

        if (album.getCoverPictureId() != null && memberPictureIds.contains(album.getCoverPictureId())) {
            coverId = album.getCoverPictureId();
        } else {
            List<AlbumMembership> recent = membershipRepository.findByAlbumIdOrderByJoinTimeDesc(album.getId());
            if (!recent.isEmpty()) {
                coverId = recent.get(0).getPictureId();
            }
        }

        if (coverId == null) {
            return null;
        }

        Optional<Picture> pic = pictureRepository.findById(coverId);
        if (!pic.isPresent()) {
            return null;
        }

        AlbumView.CoverInfo cover = new AlbumView.CoverInfo();
        cover.setId(pic.get().getId());
        cover.setName(pic.get().getName());
        cover.setUrl(pic.get().getUrl());
        return cover;
    }
}
