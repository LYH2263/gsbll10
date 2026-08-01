package com.example.picture.controller;

import com.example.picture.entity.Picture;
import com.example.picture.service.PictureService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*")
public class PictureController {

    @Autowired
    private PictureService pictureService;

    @PostMapping("/upload")
    public ResponseEntity<Picture> upload(@RequestParam("file") MultipartFile file) throws IOException {
        return ResponseEntity.ok(pictureService.upload(file));
    }

    @GetMapping("/list")
    public ResponseEntity<List<Picture>> list() {
        return ResponseEntity.ok(pictureService.list());
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        pictureService.delete(id);
        return ResponseEntity.ok().build();
    }
}
