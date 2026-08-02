package com.example.picture.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.io.File;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    @Value("${upload.path:/app/images/}")
    private String uploadPath;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        String location = uploadPath;
        if (!location.endsWith(File.separator)) {
            location = location + File.separator;
        }
        registry.addResourceHandler("/images/**")
                .addResourceLocations("file:" + location);
    }
}
