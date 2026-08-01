package com.example.picture.controller;

import com.example.picture.common.ApiResponse;
import com.example.picture.entity.Album;
import com.example.picture.service.AlbumService;
import org.springframework.beans.factory.annotation.Autowired;
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
import java.util.Map;

@RestController
@RequestMapping("/api/album")
@CrossOrigin(origins = "*")
public class AlbumController {

    @Autowired
    private AlbumService albumService;

    @PostMapping
    public ApiResponse<Album> create(@RequestBody Map<String, String> body) {
        return ApiResponse.ok(albumService.create(body.get("name"), body.get("description")));
    }

    @GetMapping("/list")
    public ApiResponse<List<Map<String, Object>>> list() {
        return ApiResponse.ok(albumService.list());
    }

    @GetMapping("/{id}")
    public ApiResponse<Map<String, Object>> detail(@PathVariable Long id) {
        return ApiResponse.ok(albumService.detail(id));
    }

    @PutMapping("/{id}")
    public ApiResponse<Album> update(@PathVariable Long id, @RequestBody Map<String, String> body) {
        return ApiResponse.ok(albumService.update(id, body.get("name"), body.get("description")));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        albumService.delete(id);
        return ApiResponse.ok(null);
    }

    @PostMapping("/{id}/pictures")
    public ApiResponse<Void> addPicture(@PathVariable Long id, @RequestBody Map<String, Long> body) {
        albumService.addPicture(id, body.get("pictureId"));
        return ApiResponse.ok(null);
    }

    @DeleteMapping("/{id}/pictures/{pictureId}")
    public ApiResponse<Void> removePicture(@PathVariable Long id, @PathVariable Long pictureId) {
        albumService.removePicture(id, pictureId);
        return ApiResponse.ok(null);
    }

    @PutMapping("/{id}/cover")
    public ApiResponse<Void> setCover(@PathVariable Long id, @RequestBody Map<String, Long> body) {
        albumService.setCover(id, body.get("pictureId"));
        return ApiResponse.ok(null);
    }

    @PutMapping("/{id}/order")
    public ApiResponse<Void> updateOrder(@PathVariable Long id, @RequestBody Map<String, List<Long>> body) {
        albumService.updateOrder(id, body.get("pictureIds"));
        return ApiResponse.ok(null);
    }

    @PostMapping("/{id}/pictures/batch")
    public ApiResponse<Map<String, Object>> addPicturesBatch(@PathVariable Long id,
                                                             @RequestBody Map<String, List<Long>> body) {
        return ApiResponse.ok(albumService.addPicturesBatch(id, body.get("pictureIds")));
    }

    @DeleteMapping("/{id}/pictures/batch")
    public ApiResponse<Map<String, Object>> removePicturesBatch(@PathVariable Long id,
                                                                @RequestBody Map<String, List<Long>> body) {
        return ApiResponse.ok(albumService.removePicturesBatch(id, body.get("pictureIds")));
    }
}
