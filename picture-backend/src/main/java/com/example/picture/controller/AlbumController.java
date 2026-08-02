package com.example.picture.controller;

import com.example.picture.dto.AddPictureRequest;
import com.example.picture.dto.AlbumDTO;
import com.example.picture.dto.AlbumDetailDTO;
import com.example.picture.dto.AlbumRequest;
import com.example.picture.dto.ApiResponse;
import com.example.picture.dto.BatchOperationResult;
import com.example.picture.dto.CoverRequest;
import com.example.picture.dto.OrderRequest;
import com.example.picture.dto.PictureIdsRequest;
import com.example.picture.service.AlbumService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/album")
@CrossOrigin(origins = "*")
public class AlbumController {

    @Autowired
    private AlbumService albumService;

    @PostMapping
    public ResponseEntity<ApiResponse<AlbumDTO>> create(@RequestBody AlbumRequest request) {
        return ResponseEntity.ok(ApiResponse.ok("创建成功", albumService.create(request)));
    }

    @GetMapping("/list")
    public ResponseEntity<ApiResponse<List<AlbumDTO>>> list() {
        return ResponseEntity.ok(ApiResponse.ok(albumService.list()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<AlbumDetailDTO>> detail(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok(albumService.detail(id)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<AlbumDTO>> rename(@PathVariable Long id,
                                                        @RequestBody AlbumRequest request) {
        return ResponseEntity.ok(ApiResponse.ok("更新成功", albumService.rename(id, request)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Object>> delete(@PathVariable Long id) {
        albumService.delete(id);
        return ResponseEntity.ok(ApiResponse.ok("删除成功", null));
    }

    @PostMapping("/{id}/pictures")
    public ResponseEntity<ApiResponse<AlbumDTO>> addPicture(@PathVariable Long id,
                                                            @RequestBody AddPictureRequest request) {
        Long pictureId = request == null ? null : request.getPictureId();
        return ResponseEntity.ok(ApiResponse.ok("加入成功", albumService.addPicture(id, pictureId)));
    }

    @DeleteMapping("/{id}/pictures/{pictureId}")
    public ResponseEntity<ApiResponse<AlbumDTO>> removePicture(@PathVariable Long id,
                                                               @PathVariable Long pictureId) {
        return ResponseEntity.ok(ApiResponse.ok("移出成功", albumService.removePicture(id, pictureId)));
    }

    @PostMapping("/{id}/pictures/batch")
    public ResponseEntity<ApiResponse<BatchOperationResult>> batchAddPictures(@PathVariable Long id,
                                                                              @RequestBody PictureIdsRequest request) {
        return ResponseEntity.ok(ApiResponse.ok("批量加入完成",
                albumService.batchAdd(id, request == null ? null : request.getPictureIds())));
    }

    @DeleteMapping("/{id}/pictures/batch")
    public ResponseEntity<ApiResponse<BatchOperationResult>> batchRemovePictures(@PathVariable Long id,
                                                                                 @RequestBody PictureIdsRequest request) {
        return ResponseEntity.ok(ApiResponse.ok("批量移出完成",
                albumService.batchRemove(id, request == null ? null : request.getPictureIds())));
    }

    @PutMapping("/{id}/cover")
    public ResponseEntity<ApiResponse<AlbumDTO>> setCover(@PathVariable Long id,
                                                          @RequestBody CoverRequest request) {
        Long pictureId = request == null ? null : request.getPictureId();
        if (pictureId == null) {
            return ResponseEntity.ok(ApiResponse.ok("已移除封面", albumService.clearCover(id)));
        }
        return ResponseEntity.ok(ApiResponse.ok("封面已设置", albumService.setCover(id, pictureId)));
    }

    @PutMapping("/{id}/order")
    public ResponseEntity<ApiResponse<AlbumDTO>> updateOrder(@PathVariable Long id,
                                                             @RequestBody OrderRequest request) {
        return ResponseEntity.ok(ApiResponse.ok("顺序已保存",
                albumService.updateOrder(id, request == null ? null : request.getPictureIds())));
    }
}
