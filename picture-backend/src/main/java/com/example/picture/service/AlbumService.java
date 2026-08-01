package com.example.picture.service;

import com.example.picture.common.BizException;
import com.example.picture.entity.Album;
import com.example.picture.entity.AlbumPicture;
import com.example.picture.entity.Picture;
import com.example.picture.repository.AlbumPictureRepository;
import com.example.picture.repository.AlbumRepository;
import com.example.picture.repository.PictureRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
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
    private AlbumPictureRepository albumPictureRepository;

    @Autowired
    private PictureRepository pictureRepository;

    // ---------- 相册 CRUD ----------

    public Album create(String name, String description) {
        Album album = new Album();
        album.setName(validateName(name));
        album.setDescription(validateDescription(description));
        album.setCoverPictureId(null);
        Date now = new Date();
        album.setCreateTime(now);
        album.setUpdateTime(now);
        return albumRepository.save(album);
    }

    public Album update(Long id, String name, String description) {
        Album album = requireAlbum(id);
        album.setName(validateName(name));
        album.setDescription(validateDescription(description));
        album.setUpdateTime(new Date());
        return albumRepository.save(album);
    }

    /**
     * 删除相册：只删除相册本身与其成员关系记录，绝不删除任何图片文件或 Picture 记录（HD-1）。
     */
    @Transactional
    public void delete(Long id) {
        requireAlbum(id);
        albumPictureRepository.deleteByAlbumId(id);
        albumRepository.deleteById(id);
    }

    public List<Map<String, Object>> list() {
        List<Album> albums = albumRepository.findAll();
        List<Map<String, Object>> result = new ArrayList<Map<String, Object>>();
        for (Album album : albums) {
            result.add(toAlbumVO(album));
        }
        return result;
    }

    public Map<String, Object> detail(Long id) {
        Album album = requireAlbum(id);
        Map<String, Object> vo = toAlbumVO(album);
        List<Picture> pictures = listMemberPictures(id);
        vo.put("pictures", pictures);
        return vo;
    }

    // ---------- 成员关系 ----------

    /**
     * 把已存在的图片加入相册。重复加入安全忽略，不报错。
     * 新加入的图片排在相册最前面（sortOrder 最小）。
     */
    @Transactional
    public void addPicture(Long albumId, Long pictureId) {
        Album album = requireAlbum(albumId);
        if (!pictureRepository.existsById(pictureId)) {
            throw new BizException("图片不存在");
        }
        Optional<AlbumPicture> existing = albumPictureRepository.findByAlbumIdAndPictureId(albumId, pictureId);
        if (existing.isPresent()) {
            // 已在相册中，幂等忽略
            return;
        }
        AlbumPicture membership = new AlbumPicture();
        membership.setAlbumId(albumId);
        membership.setPictureId(pictureId);
        membership.setSortOrder(nextTopSortOrder(albumId));
        membership.setJoinTime(new Date());
        albumPictureRepository.save(membership);
        touch(album);
    }

    /**
     * 把图片移出相册：仅解除成员关系，不删除图片本身（HD-1）。
     * 若移出的是手动封面，封面 id 置空，展示封面自动回退为"最近加入的成员"。
     */
    @Transactional
    public void removePicture(Long albumId, Long pictureId) {
        Album album = requireAlbum(albumId);
        Optional<AlbumPicture> existing = albumPictureRepository.findByAlbumIdAndPictureId(albumId, pictureId);
        if (!existing.isPresent()) {
            throw new BizException("图片不在该相册中");
        }
        albumPictureRepository.delete(existing.get());
        if (pictureId.equals(album.getCoverPictureId())) {
            album.setCoverPictureId(null);
        }
        touch(album);
    }

    /**
     * 手动指定封面：图片必须是该相册当前成员，否则报错。
     * pictureId 为 null 时取消手动封面，展示封面回退为"最近加入的成员"。
     */
    @Transactional
    public void setCover(Long albumId, Long pictureId) {
        Album album = requireAlbum(albumId);
        if (pictureId == null) {
            album.setCoverPictureId(null);
            touch(album);
            return;
        }
        if (!pictureRepository.existsById(pictureId)) {
            throw new BizException("图片不存在");
        }
        if (!albumPictureRepository.findByAlbumIdAndPictureId(albumId, pictureId).isPresent()) {
            throw new BizException("封面必须是相册成员");
        }
        album.setCoverPictureId(pictureId);
        touch(album);
    }

    /**
     * 更新相册内顺序并持久化。按 pictureIds 给定顺序依次重排 sortOrder；
     * 未出现在列表中的成员保持相对顺序排在其后，非成员 id 安全忽略。
     */
    @Transactional
    public void updateOrder(Long albumId, List<Long> pictureIds) {
        Album album = requireAlbum(albumId);
        List<AlbumPicture> members = albumPictureRepository.findByAlbumIdOrderBySortOrderAsc(albumId);
        Map<Long, AlbumPicture> memberMap = new HashMap<Long, AlbumPicture>();
        for (AlbumPicture member : members) {
            memberMap.put(member.getPictureId(), member);
        }
        int order = 0;
        if (pictureIds != null) {
            for (Long pictureId : pictureIds) {
                AlbumPicture member = memberMap.remove(pictureId);
                if (member != null) {
                    member.setSortOrder(order++);
                    albumPictureRepository.save(member);
                }
            }
        }
        for (AlbumPicture member : members) {
            if (memberMap.containsKey(member.getPictureId())) {
                member.setSortOrder(order++);
                albumPictureRepository.save(member);
            }
        }
        touch(album);
    }

    /**
     * 批量加入图片：部分成功语义（CL-6）。
     * 已在相册中的图片安全忽略（ignored）；不存在的图片逐项记入 failed；
     * 不因个别无效项回滚或丢弃有效项。成功的图片整体置顶，组内按请求顺序排列。
     */
    @Transactional
    public Map<String, Object> addPicturesBatch(Long albumId, List<Long> pictureIds) {
        Album album = requireAlbum(albumId);
        List<Long> ids = pictureIds == null ? new ArrayList<Long>() : pictureIds;
        List<Long> succeeded = new ArrayList<Long>();
        List<Long> ignored = new ArrayList<Long>();
        List<Map<String, Object>> failed = new ArrayList<Map<String, Object>>();
        for (Long pictureId : ids) {
            if (pictureId == null || !pictureRepository.existsById(pictureId)) {
                failed.add(failItem(pictureId, "图片不存在"));
                continue;
            }
            if (albumPictureRepository.findByAlbumIdAndPictureId(albumId, pictureId).isPresent()) {
                ignored.add(pictureId);
                continue;
            }
            AlbumPicture membership = new AlbumPicture();
            membership.setAlbumId(albumId);
            membership.setPictureId(pictureId);
            membership.setSortOrder(nextTopSortOrder(albumId));
            membership.setJoinTime(new Date());
            albumPictureRepository.save(membership);
            succeeded.add(pictureId);
        }
        if (!succeeded.isEmpty()) {
            touch(album);
        }
        return batchResult(ids.size(), succeeded, ignored, failed);
    }

    /**
     * 批量移出图片：部分成功语义（CL-6）。仅解除成员关系，不删除图片本身（HD-1）。
     * 本就不在相册中的图片安全忽略（ignored）；不存在的图片逐项记入 failed。
     * 若移出项包含当前手动封面，封面自动回退。
     */
    @Transactional
    public Map<String, Object> removePicturesBatch(Long albumId, List<Long> pictureIds) {
        Album album = requireAlbum(albumId);
        List<Long> ids = pictureIds == null ? new ArrayList<Long>() : pictureIds;
        List<Long> succeeded = new ArrayList<Long>();
        List<Long> ignored = new ArrayList<Long>();
        List<Map<String, Object>> failed = new ArrayList<Map<String, Object>>();
        for (Long pictureId : ids) {
            if (pictureId == null || !pictureRepository.existsById(pictureId)) {
                failed.add(failItem(pictureId, "图片不存在"));
                continue;
            }
            Optional<AlbumPicture> membership = albumPictureRepository.findByAlbumIdAndPictureId(albumId, pictureId);
            if (!membership.isPresent()) {
                ignored.add(pictureId);
                continue;
            }
            albumPictureRepository.delete(membership.get());
            if (pictureId.equals(album.getCoverPictureId())) {
                album.setCoverPictureId(null);
            }
            succeeded.add(pictureId);
        }
        if (!succeeded.isEmpty()) {
            touch(album);
        }
        return batchResult(ids.size(), succeeded, ignored, failed);
    }

    private Map<String, Object> failItem(Long pictureId, String reason) {
        Map<String, Object> item = new HashMap<String, Object>();
        item.put("pictureId", pictureId);
        item.put("reason", reason);
        return item;
    }

    private Map<String, Object> batchResult(int requested, List<Long> succeeded, List<Long> ignored,
                                            List<Map<String, Object>> failed) {
        Map<String, Object> result = new HashMap<String, Object>();
        result.put("requested", requested);
        result.put("succeeded", succeeded);
        result.put("ignored", ignored);
        result.put("failed", failed);
        return result;
    }

    // ---------- 内部方法 ----------

    private Album requireAlbum(Long id) {
        Optional<Album> album = albumRepository.findById(id);
        if (!album.isPresent()) {
            throw new BizException("相册不存在");
        }
        return album.get();
    }

    private String validateName(String name) {
        String trimmed = name == null ? "" : name.trim();
        if (trimmed.isEmpty()) {
            throw new BizException("相册名不能为空");
        }
        if (trimmed.length() > 50) {
            throw new BizException("相册名长度不能超过 50 个字符");
        }
        return trimmed;
    }

    private String validateDescription(String description) {
        if (description != null && description.length() > 200) {
            throw new BizException("相册简介不能超过 200 个字符");
        }
        return description;
    }

    private void touch(Album album) {
        album.setUpdateTime(new Date());
        albumRepository.save(album);
    }

    private int nextTopSortOrder(Long albumId) {
        List<AlbumPicture> members = albumPictureRepository.findByAlbumIdOrderBySortOrderAsc(albumId);
        if (members.isEmpty() || members.get(0).getSortOrder() == null) {
            return 0;
        }
        return members.get(0).getSortOrder() - 1;
    }

    private List<Picture> listMemberPictures(Long albumId) {
        List<AlbumPicture> members = albumPictureRepository.findByAlbumIdOrderBySortOrderAsc(albumId);
        List<Picture> pictures = new ArrayList<Picture>();
        for (AlbumPicture member : members) {
            Optional<Picture> picture = pictureRepository.findById(member.getPictureId());
            if (picture.isPresent()) {
                pictures.add(picture.get());
            }
        }
        return pictures;
    }

    /**
     * 计算相册的展示封面：
     * 1. coverPictureId 非空且该图片仍是相册成员且图片存在 → 用它；
     * 2. 否则 → 相册中最近加入（joinTime 最新）的成员；
     * 3. 相册为空 → null。
     */
    private Picture resolveCover(Album album, List<AlbumPicture> members) {
        if (album.getCoverPictureId() != null) {
            for (AlbumPicture member : members) {
                if (album.getCoverPictureId().equals(member.getPictureId())) {
                    Optional<Picture> picture = pictureRepository.findById(member.getPictureId());
                    if (picture.isPresent()) {
                        return picture.get();
                    }
                }
            }
        }
        // joinTime 最新者优先。注意：不能混用 Date.after/equals 比较 Timestamp（JDK 下结果不可靠），
        // 统一用 getTime() 毫秒值比较；毫秒相同（同秒入库）时以自增 id 作为次序依据
        AlbumPicture latest = null;
        for (AlbumPicture member : members) {
            if (latest == null) {
                latest = member;
                continue;
            }
            long memberTime = member.getJoinTime() == null ? Long.MIN_VALUE : member.getJoinTime().getTime();
            long latestTime = latest.getJoinTime() == null ? Long.MIN_VALUE : latest.getJoinTime().getTime();
            if (memberTime > latestTime || (memberTime == latestTime && member.getId() > latest.getId())) {
                latest = member;
            }
        }
        if (latest == null) {
            return null;
        }
        return pictureRepository.findById(latest.getPictureId()).orElse(null);
    }

    private Map<String, Object> toAlbumVO(Album album) {
        List<AlbumPicture> members = albumPictureRepository.findByAlbumId(album.getId());
        int count = 0;
        for (AlbumPicture member : members) {
            if (pictureRepository.existsById(member.getPictureId())) {
                count++;
            }
        }
        Picture cover = resolveCover(album, members);

        Map<String, Object> vo = new HashMap<String, Object>();
        vo.put("id", album.getId());
        vo.put("name", album.getName());
        vo.put("description", album.getDescription());
        vo.put("coverPictureId", album.getCoverPictureId());
        vo.put("pictureCount", count);
        vo.put("createTime", album.getCreateTime());
        vo.put("updateTime", album.getUpdateTime());
        if (cover != null) {
            Map<String, Object> coverMap = new HashMap<String, Object>();
            coverMap.put("id", cover.getId());
            coverMap.put("name", cover.getName());
            coverMap.put("url", cover.getUrl());
            vo.put("cover", coverMap);
        } else {
            vo.put("cover", null);
        }
        return vo;
    }
}
