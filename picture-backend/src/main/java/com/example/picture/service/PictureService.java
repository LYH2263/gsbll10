package com.example.picture.service;

import com.example.picture.entity.Picture;
import com.example.picture.repository.PictureRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import javax.annotation.PostConstruct;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Date;
import java.util.List;
import java.util.UUID;

@Service
public class PictureService {

    @Autowired
    private PictureRepository pictureRepository;

    @Value("${upload.path:/app/images/}")
    private String uploadPath;

    @PostConstruct
    public void init() {
        File dir = new File(uploadPath);
        if (!dir.exists()) {
            dir.mkdirs();
        }
    }

    public Picture upload(MultipartFile file) throws IOException {
        String originalName = file.getOriginalFilename();
        String suffix = "";
        if (originalName != null && originalName.contains(".")) {
            suffix = originalName.substring(originalName.lastIndexOf("."));
        }
        String fileName = UUID.randomUUID().toString() + suffix;
        
        // Save file
        Path path = Paths.get(uploadPath + fileName);
        Files.write(path, file.getBytes());

        // Save entity
        Picture picture = new Picture();
        picture.setName(originalName);
        picture.setUrl("/images/" + fileName);
        picture.setSize(file.getSize());
        picture.setCreateTime(new Date());
        return pictureRepository.save(picture);
    }

    public List<Picture> list() {
        return pictureRepository.findAll();
    }

    public void delete(Long id) {
        pictureRepository.findById(id).ifPresent(picture -> {
            String fileName = picture.getUrl().replace("/images/", "");
            File file = new File(uploadPath + fileName);
            if (file.exists()) {
                file.delete();
            }
            pictureRepository.delete(picture);
        });
    }
}
