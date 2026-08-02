package com.example.picture.controller;

import com.example.picture.dto.AlbumDetailView;
import com.example.picture.dto.AlbumRequest;
import com.example.picture.dto.AlbumView;
import com.example.picture.dto.ApiResponse;
import com.example.picture.dto.BatchRequest;
import com.example.picture.dto.BatchResult;
import com.example.picture.dto.OrderRequest;
import com.example.picture.dto.PictureIdRequest;
import com.example.picture.entity.Album;
import com.example.picture.service.AlbumException;
import com.example.picture.service.AlbumService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/album")
@CrossOrigin(origins = "*")
public class AlbumController {

    @Autowired
    private AlbumService albumService;

    @PostMapping
    public ResponseEntity<ApiResponse<AlbumView>> createAlbum(@RequestBody AlbumRequest request) {
        try {
            AlbumView view = toView(albumService.createAlbum(request.getName(), request.getDescription()));
            return ResponseEntity.ok(ApiResponse.success("创建成功", view));
        } catch (AlbumException e) {
            return ResponseEntity.ok(ApiResponse.error(e.getCode(), e.getMessage()));
        }
    }

    @GetMapping("/list")
    public ResponseEntity<ApiResponse<List<AlbumView>>> listAlbums() {
        List<AlbumView> list = albumService.listAlbums();
        return ResponseEntity.ok(ApiResponse.success(list));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<AlbumDetailView>> getAlbum(@PathVariable Long id) {
        try {
            AlbumDetailView detail = albumService.getAlbumDetail(id);
            return ResponseEntity.ok(ApiResponse.success(detail));
        } catch (AlbumException e) {
            return ResponseEntity.ok(ApiResponse.error(e.getCode(), e.getMessage()));
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<AlbumView>> updateAlbum(@PathVariable Long id, @RequestBody AlbumRequest request) {
        try {
            AlbumView view = toView(albumService.updateAlbum(id, request.getName(), request.getDescription()));
            return ResponseEntity.ok(ApiResponse.success("更新成功", view));
        } catch (AlbumException e) {
            return ResponseEntity.ok(ApiResponse.error(e.getCode(), e.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteAlbum(@PathVariable Long id) {
        try {
            albumService.deleteAlbum(id);
            return ResponseEntity.ok(ApiResponse.success("删除成功", null));
        } catch (AlbumException e) {
            return ResponseEntity.ok(ApiResponse.error(e.getCode(), e.getMessage()));
        }
    }

    @PostMapping("/{id}/pictures")
    public ResponseEntity<ApiResponse<Void>> addPicture(@PathVariable Long id, @RequestBody PictureIdRequest request) {
        try {
            albumService.addPicture(id, request.getPictureId());
            return ResponseEntity.ok(ApiResponse.success("添加成功", null));
        } catch (AlbumException e) {
            return ResponseEntity.ok(ApiResponse.error(e.getCode(), e.getMessage()));
        }
    }

    @DeleteMapping("/{id}/pictures/{pictureId}")
    public ResponseEntity<ApiResponse<Void>> removePicture(@PathVariable Long id, @PathVariable Long pictureId) {
        try {
            albumService.removePicture(id, pictureId);
            return ResponseEntity.ok(ApiResponse.success("移出成功", null));
        } catch (AlbumException e) {
            return ResponseEntity.ok(ApiResponse.error(e.getCode(), e.getMessage()));
        }
    }

    @PutMapping("/{id}/cover")
    public ResponseEntity<ApiResponse<Void>> setCover(@PathVariable Long id, @RequestBody PictureIdRequest request) {
        try {
            albumService.setCover(id, request.getPictureId());
            return ResponseEntity.ok(ApiResponse.success("封面设置成功", null));
        } catch (AlbumException e) {
            return ResponseEntity.ok(ApiResponse.error(e.getCode(), e.getMessage()));
        }
    }

    @PutMapping("/{id}/order")
    public ResponseEntity<ApiResponse<Void>> updateOrder(@PathVariable Long id, @RequestBody OrderRequest request) {
        try {
            albumService.updateOrder(id, request.getPictureIds());
            return ResponseEntity.ok(ApiResponse.success("排序更新成功", null));
        } catch (AlbumException e) {
            return ResponseEntity.ok(ApiResponse.error(e.getCode(), e.getMessage()));
        }
    }

    @PostMapping("/{id}/pictures/batch")
    public ResponseEntity<ApiResponse<BatchResult>> batchAdd(@PathVariable Long id, @RequestBody BatchRequest request) {
        try {
            BatchResult result = albumService.batchAddPictures(id, request.getPictureIds());
            return ResponseEntity.ok(ApiResponse.success(result));
        } catch (AlbumException e) {
            return ResponseEntity.ok(ApiResponse.error(e.getCode(), e.getMessage()));
        }
    }

    @DeleteMapping("/{id}/pictures/batch")
    public ResponseEntity<ApiResponse<BatchResult>> batchRemove(@PathVariable Long id, @RequestBody BatchRequest request) {
        try {
            BatchResult result = albumService.batchRemovePictures(id, request.getPictureIds());
            return ResponseEntity.ok(ApiResponse.success(result));
        } catch (AlbumException e) {
            return ResponseEntity.ok(ApiResponse.error(e.getCode(), e.getMessage()));
        }
    }

    private AlbumView toView(Album album) {
        AlbumView view = new AlbumView();
        view.setId(album.getId());
        view.setName(album.getName());
        view.setDescription(album.getDescription());
        view.setCoverPictureId(album.getCoverPictureId());
        view.setCreateTime(album.getCreateTime());
        view.setUpdateTime(album.getUpdateTime());
        return view;
    }
}
