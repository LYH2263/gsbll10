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
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 相册业务：CRUD、成员关系维护、封面计算与自动回退、相册内排序。
 * 所有业务规则集中于此（04 规范 §2），Controller 不直接触碰 Repository。
 */
@Service
public class AlbumService {

    @Autowired
    private AlbumRepository albumRepository;

    @Autowired
    private AlbumMembershipRepository membershipRepository;

    @Autowired
    private PictureRepository pictureRepository;

    // ---------------------------------------------------------------- 相册 CRUD

    @Transactional
    public AlbumView create(String name, String description) {
        String cleanName = normalizeName(name);
        String cleanDesc = normalizeDescription(description);

        Album album = new Album();
        album.setName(cleanName);
        album.setDescription(cleanDesc);
        Date now = new Date();
        album.setCreateTime(now);
        album.setUpdateTime(now);
        album = albumRepository.save(album);
        return toView(album);
    }

    public List<AlbumView> list() {
        List<Album> albums = albumRepository.findAll();
        List<AlbumView> views = new ArrayList<AlbumView>();
        for (Album album : albums) {
            views.add(toView(album));
        }
        return views;
    }

    public AlbumDetailView detail(Long albumId) {
        Album album = requireAlbum(albumId);
        List<AlbumMembership> members = membershipRepository.findByAlbumIdOrderBySortOrderAsc(albumId);
        List<Picture> pictures = new ArrayList<Picture>();
        for (AlbumMembership m : members) {
            pictureRepository.findById(m.getPictureId()).ifPresent(pictures::add);
        }
        return new AlbumDetailView(toView(album), pictures);
    }

    @Transactional
    public AlbumView rename(Long albumId, String name, String description) {
        Album album = requireAlbum(albumId);
        album.setName(normalizeName(name));
        album.setDescription(normalizeDescription(description));
        album.setUpdateTime(new Date());
        album = albumRepository.save(album);
        return toView(album);
    }

    /**
     * 删除相册：仅删除相册本身及其成员关系记录，绝不删除图片文件或图片记录（HD-1 / CL-7）。
     */
    @Transactional
    public void delete(Long albumId) {
        requireAlbum(albumId);
        membershipRepository.deleteByAlbumId(albumId);
        albumRepository.deleteById(albumId);
    }

    // ------------------------------------------------------------ 成员关系维护

    /** 加入单张图片：建立成员关系；新加入者置顶。幂等：已在相册中则直接忽略。 */
    @Transactional
    public AlbumView addPicture(Long albumId, Long pictureId) {
        Album album = requireAlbum(albumId);
        if (!pictureRepository.findById(pictureId).isPresent()) {
            throw new AlbumException("图片不存在");
        }
        AlbumMembership existing = membershipRepository.findByAlbumIdAndPictureId(albumId, pictureId);
        if (existing == null) {
            AlbumMembership m = new AlbumMembership();
            m.setAlbumId(albumId);
            m.setPictureId(pictureId);
            m.setSortOrder(nextTopSortOrder(albumId));
            m.setJoinTime(new Date());
            membershipRepository.save(m);
            touch(album);
        }
        return toView(album);
    }

    /** 移出单张图片：仅解除成员关系，不删除图片（HD-1）；若移出的是封面则自动回退。 */
    @Transactional
    public AlbumView removePicture(Long albumId, Long pictureId) {
        Album album = requireAlbum(albumId);
        AlbumMembership m = membershipRepository.findByAlbumIdAndPictureId(albumId, pictureId);
        if (m != null) {
            membershipRepository.delete(m);
            touch(album);
        }
        return toView(album);
    }

    /**
     * 图库删除图片时的连带清理：移除该图片在所有相册中的成员关系。
     * 由图片删除流程调用，保证封面按最新成员自动回退（HD-3）。
     */
    @Transactional
    public void onPictureDeleted(Long pictureId) {
        membershipRepository.deleteByPictureId(pictureId);
    }

    // ------------------------------------------------------------ 封面与排序

    /**
     * 手动设置封面：pictureId 必须是该相册当前成员，否则报错（HD-3 / CL-5）。
     * 仅记录 coverPictureId，展示封面仍由 resolveCover 统一计算（含后续自动回退）。
     */
    @Transactional
    public AlbumView setCover(Long albumId, Long pictureId) {
        Album album = requireAlbum(albumId);
        if (pictureId == null) {
            throw new AlbumException("封面图片不能为空");
        }
        AlbumMembership m = membershipRepository.findByAlbumIdAndPictureId(albumId, pictureId);
        if (m == null) {
            throw new AlbumException("封面必须是相册成员");
        }
        album.setCoverPictureId(pictureId);
        touch(album);
        return toView(album);
    }

    /** 取消手动封面：清空 coverPictureId，展示封面回退为"最近加入"（CL-5）。 */
    @Transactional
    public AlbumView clearCover(Long albumId) {
        Album album = requireAlbum(albumId);
        album.setCoverPictureId(null);
        touch(album);
        return toView(album);
    }

    /**
     * 更新相册内顺序：按传入 pictureIds 顺序重排 sortOrder（越靠前越小）。
     * 只处理仍是成员的 id；未在请求中出现的现有成员追加到末尾，保持其相对顺序。
     * 顺序持久化到成员关系，刷新后一致（CL-5 排序）。
     */
    @Transactional
    public AlbumView updateOrder(Long albumId, List<Long> pictureIds) {
        Album album = requireAlbum(albumId);
        List<AlbumMembership> members = membershipRepository.findByAlbumIdOrderBySortOrderAsc(albumId);

        Map<Long, AlbumMembership> byPictureId = new LinkedHashMap<Long, AlbumMembership>();
        for (AlbumMembership m : members) {
            byPictureId.put(m.getPictureId(), m);
        }

        int order = 0;
        java.util.Set<Long> handled = new java.util.HashSet<Long>();
        if (pictureIds != null) {
            for (Long pid : pictureIds) {
                AlbumMembership m = byPictureId.get(pid);
                if (m != null && !handled.contains(pid)) {
                    m.setSortOrder(order++);
                    membershipRepository.save(m);
                    handled.add(pid);
                }
            }
        }
        // 请求未覆盖的现有成员，按原顺序追加到末尾，避免顺序丢失
        for (AlbumMembership m : members) {
            if (!handled.contains(m.getPictureId())) {
                m.setSortOrder(order++);
                membershipRepository.save(m);
            }
        }
        touch(album);
        return toView(album);
    }

    // ------------------------------------------------------------ 批量操作

    /**
     * 批量加入图片（CL-6 / 03 契约 §5）：部分成功语义。
     * - 相册不存在 → 整单前置失败（由 requireAlbum 抛出）。
     * - 已在相册中 → 幂等忽略（ignored），不算失败。
     * - 图片不存在 → failed 逐项说明，不影响其它有效项。
     * - 成功项整体置顶，组内按请求给定顺序排列（2.10 / 2.11）。
     */
    @Transactional
    public BatchResult batchAdd(Long albumId, List<Long> pictureIds) {
        Album album = requireAlbum(albumId);
        BatchResult result = new BatchResult();
        if (pictureIds == null || pictureIds.isEmpty()) {
            result.setRequested(0);
            return result;
        }
        result.setRequested(pictureIds.size());

        // 先收集本次真正需要新建成员关系的有效 id（去重、保序），用于整体置顶时的组内顺序
        List<Long> toInsert = new ArrayList<Long>();
        java.util.Set<Long> seen = new java.util.HashSet<Long>();
        for (Long pid : pictureIds) {
            if (pid == null) {
                result.getFailed().add(new BatchResult.FailedItem(null, "图片id为空"));
                continue;
            }
            if (seen.contains(pid)) {
                // 同一请求内重复出现：首个已处理，后续视为已在相册中安全忽略
                result.getIgnored().add(pid);
                continue;
            }
            seen.add(pid);
            if (!pictureRepository.findById(pid).isPresent()) {
                result.getFailed().add(new BatchResult.FailedItem(pid, "图片不存在"));
                continue;
            }
            if (membershipRepository.findByAlbumIdAndPictureId(albumId, pid) != null) {
                result.getIgnored().add(pid);
                continue;
            }
            toInsert.add(pid);
        }

        if (!toInsert.isEmpty()) {
            // 整体置顶：本批成员的 sortOrder 排在所有现有成员之前，组内按请求顺序
            int base = nextTopSortOrder(albumId); // 当前最靠前位置
            int start = base - (toInsert.size() - 1); // 保证整批都比现有成员靠前，且组内保序
            Date now = new Date();
            int offset = 0;
            for (Long pid : toInsert) {
                AlbumMembership m = new AlbumMembership();
                m.setAlbumId(albumId);
                m.setPictureId(pid);
                m.setSortOrder(start + offset);
                m.setJoinTime(now);
                membershipRepository.save(m);
                result.getSucceeded().add(pid);
                offset++;
            }
            touch(album);
        }
        return result;
    }

    /**
     * 批量移出图片（CL-6 / 03 契约 §5）：部分成功语义，仅解关系不删图片（HD-1）。
     * - 相册不存在 → 整单前置失败。
     * - 本就不在相册中 → 幂等忽略（ignored）。
     * - 若移出的是封面成员，触发 resolveCover 自动回退。
     */
    @Transactional
    public BatchResult batchRemove(Long albumId, List<Long> pictureIds) {
        Album album = requireAlbum(albumId);
        BatchResult result = new BatchResult();
        if (pictureIds == null || pictureIds.isEmpty()) {
            result.setRequested(0);
            return result;
        }
        result.setRequested(pictureIds.size());

        boolean changed = false;
        java.util.Set<Long> seen = new java.util.HashSet<Long>();
        for (Long pid : pictureIds) {
            if (pid == null) {
                result.getFailed().add(new BatchResult.FailedItem(null, "图片id为空"));
                continue;
            }
            if (seen.contains(pid)) {
                result.getIgnored().add(pid);
                continue;
            }
            seen.add(pid);
            AlbumMembership m = membershipRepository.findByAlbumIdAndPictureId(albumId, pid);
            if (m == null) {
                // 本就不在相册中：安全忽略（不算失败）
                result.getIgnored().add(pid);
                continue;
            }
            membershipRepository.delete(m);
            result.getSucceeded().add(pid);
            changed = true;
        }
        if (changed) {
            touch(album);
        }
        return result;
    }

    // -------------------------------------------------------------------- 内部

    private Album requireAlbum(Long albumId) {
        return albumRepository.findById(albumId)
                .orElseThrow(() -> new AlbumException("相册不存在"));
    }

    private String normalizeName(String name) {
        String cleanName = name == null ? "" : name.trim();
        if (cleanName.isEmpty()) {
            throw new AlbumException("相册名不能为空");
        }
        if (cleanName.length() > 50) {
            throw new AlbumException("相册名长度不能超过50个字符");
        }
        return cleanName;
    }

    private String normalizeDescription(String description) {
        if (description == null) {
            return null;
        }
        String cleanDesc = description.trim();
        if (cleanDesc.isEmpty()) {
            return null;
        }
        if (cleanDesc.length() > 200) {
            throw new AlbumException("简介长度不能超过200个字符");
        }
        return cleanDesc;
    }

    private void touch(Album album) {
        album.setUpdateTime(new Date());
        albumRepository.save(album);
    }

    /** 新加入者应获得最靠前位置：取当前最小 sortOrder 再 -1，空相册从 0 开始。 */
    private int nextTopSortOrder(Long albumId) {
        List<AlbumMembership> members = membershipRepository.findByAlbumId(albumId);
        if (members.isEmpty()) {
            return 0;
        }
        int min = Integer.MAX_VALUE;
        for (AlbumMembership m : members) {
            if (m.getSortOrder() != null && m.getSortOrder() < min) {
                min = m.getSortOrder();
            }
        }
        if (min == Integer.MAX_VALUE) {
            return 0;
        }
        return min - 1;
    }

    private AlbumView toView(Album album) {
        AlbumView view = new AlbumView();
        view.setId(album.getId());
        view.setName(album.getName());
        view.setDescription(album.getDescription());
        view.setCoverPictureId(album.getCoverPictureId());
        view.setCreateTime(album.getCreateTime());
        view.setUpdateTime(album.getUpdateTime());

        List<AlbumMembership> members = membershipRepository.findByAlbumId(album.getId());
        view.setPictureCount(members.size());
        view.setCover(resolveCover(album, members));
        return view;
    }

    /**
     * 计算展示封面（03 契约 §4 / CL-5）：
     * 1) 手动封面非空且仍是成员 → 用它；
     * 2) 否则用相册中最近加入（joinTime 最新，同刻以 id 较大者）的成员；
     * 3) 相册为空 → null。
     */
    private Picture resolveCover(Album album, List<AlbumMembership> members) {
        if (members == null || members.isEmpty()) {
            return null;
        }
        // 建立成员 pictureId 集合，快速判断手动封面是否仍是成员
        Map<Long, AlbumMembership> byPictureId = new LinkedHashMap<Long, AlbumMembership>();
        for (AlbumMembership m : members) {
            byPictureId.put(m.getPictureId(), m);
        }

        Long manual = album.getCoverPictureId();
        if (manual != null && byPictureId.containsKey(manual)) {
            Picture p = pictureRepository.findById(manual).orElse(null);
            if (p != null) {
                return p;
            }
        }

        // 回退：最近加入的成员
        AlbumMembership latest = null;
        for (AlbumMembership m : members) {
            if (isMoreRecent(m, latest)) {
                latest = m;
            }
        }
        if (latest == null) {
            return null;
        }
        return pictureRepository.findById(latest.getPictureId()).orElse(null);
    }

    /** joinTime 毫秒值比较，避免 java.sql.Timestamp 与 java.util.Date 比较不可靠；同刻以 id 较大者为更近。 */
    private boolean isMoreRecent(AlbumMembership candidate, AlbumMembership current) {
        if (current == null) {
            return true;
        }
        long ct = candidate.getJoinTime() == null ? 0L : candidate.getJoinTime().getTime();
        long curt = current.getJoinTime() == null ? 0L : current.getJoinTime().getTime();
        if (ct != curt) {
            return ct > curt;
        }
        return candidate.getId() != null && current.getId() != null && candidate.getId() > current.getId();
    }
}
