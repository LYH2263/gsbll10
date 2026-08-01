package com.example.picture.service;

import com.example.picture.entity.Album;
import com.example.picture.entity.Picture;
import com.example.picture.repository.AlbumPictureRepository;
import com.example.picture.repository.AlbumRepository;
import com.example.picture.repository.PictureRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
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

    @Autowired
    private AlbumPictureRepository albumPictureRepository;

    @Autowired
    private AlbumRepository albumRepository;

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

    @Transactional
    public void delete(Long id) {
        pictureRepository.findById(id).ifPresent(picture -> {
            String fileName = picture.getUrl().replace("/images/", "");
            File file = new File(uploadPath + fileName);
            if (file.exists()) {
                file.delete();
            }
            pictureRepository.delete(picture);
            // 图库删除图片时，连带清理各相册中的成员关系与手动封面引用，
            // 相册展示封面会自动回退到"最近加入的成员"
            for (Album album : albumRepository.findAll()) {
                if (id.equals(album.getCoverPictureId())) {
                    album.setCoverPictureId(null);
                    albumRepository.save(album);
                }
            }
            albumPictureRepository.deleteByPictureId(id);
        });
    }
}
