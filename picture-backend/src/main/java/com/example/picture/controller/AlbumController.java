package com.example.picture.controller;

import com.example.picture.dto.AlbumDetailView;
import com.example.picture.dto.AlbumRequest;
import com.example.picture.dto.AlbumView;
import com.example.picture.dto.ApiResponse;
import com.example.picture.dto.BatchRequest;
import com.example.picture.dto.BatchResult;
import com.example.picture.dto.OrderRequest;
import com.example.picture.dto.PictureIdRequest;
import com.example.picture.service.AlbumException;
import com.example.picture.service.AlbumService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 相册接口。统一返回 {code,message,data} 包裹结构（03 契约）。
 * Controller 只做参数绑定、调用 Service、包装响应，不直接触碰 Repository（04 规范 §2）。
 */
@RestController
@RequestMapping("/api/album")
@CrossOrigin(origins = "*")
public class AlbumController {

    @Autowired
    private AlbumService albumService;

    @PostMapping
    public ApiResponse<AlbumView> create(@RequestBody AlbumRequest request) {
        return ApiResponse.ok(albumService.create(request.getName(), request.getDescription()));
    }

    @GetMapping("/list")
    public ApiResponse<List<AlbumView>> list() {
        return ApiResponse.ok(albumService.list());
    }

    @GetMapping("/{id}")
    public ApiResponse<AlbumDetailView> detail(@PathVariable Long id) {
        return ApiResponse.ok(albumService.detail(id));
    }

    @PutMapping("/{id}")
    public ApiResponse<AlbumView> rename(@PathVariable Long id, @RequestBody AlbumRequest request) {
        return ApiResponse.ok(albumService.rename(id, request.getName(), request.getDescription()));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        albumService.delete(id);
        return ApiResponse.ok(null);
    }

    @PostMapping("/{id}/pictures")
    public ApiResponse<AlbumView> addPicture(@PathVariable Long id, @RequestBody PictureIdRequest request) {
        return ApiResponse.ok(albumService.addPicture(id, request.getPictureId()));
    }

    @DeleteMapping("/{id}/pictures/{pictureId}")
    public ApiResponse<AlbumView> removePicture(@PathVariable Long id, @PathVariable Long pictureId) {
        return ApiResponse.ok(albumService.removePicture(id, pictureId));
    }

    @PutMapping("/{id}/cover")
    public ApiResponse<AlbumView> setCover(@PathVariable Long id, @RequestBody PictureIdRequest request) {
        return ApiResponse.ok(albumService.setCover(id, request.getPictureId()));
    }

    @DeleteMapping("/{id}/cover")
    public ApiResponse<AlbumView> clearCover(@PathVariable Long id) {
        return ApiResponse.ok(albumService.clearCover(id));
    }

    @PutMapping("/{id}/order")
    public ApiResponse<AlbumView> updateOrder(@PathVariable Long id, @RequestBody OrderRequest request) {
        return ApiResponse.ok(albumService.updateOrder(id, request.getPictureIds()));
    }

    @PostMapping("/{id}/pictures/batch")
    public ApiResponse<BatchResult> batchAdd(@PathVariable Long id, @RequestBody BatchRequest request) {
        return ApiResponse.ok(albumService.batchAdd(id, request.getPictureIds()));
    }

    @DeleteMapping("/{id}/pictures/batch")
    public ApiResponse<BatchResult> batchRemove(@PathVariable Long id, @RequestBody BatchRequest request) {
        return ApiResponse.ok(albumService.batchRemove(id, request.getPictureIds()));
    }

    /** 业务校验失败统一返回 {code≠0, message}，避免裸 500 / 空响应（03 契约 §1）。 */
    @ExceptionHandler(AlbumException.class)
    public ApiResponse<Void> handleAlbumException(AlbumException e) {
        return ApiResponse.error(e.getMessage());
    }
}
