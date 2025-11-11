package com.aimovie.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.web.servlet.config.annotation.ContentNegotiationConfigurer;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void configureContentNegotiation(ContentNegotiationConfigurer configurer) {
        configurer
            .favorParameter(false)
            .favorPathExtension(false)
            .defaultContentType(MediaType.APPLICATION_JSON)
            .mediaType("mp4", MediaType.valueOf("video/mp4"))
            .mediaType("mkv", MediaType.valueOf("video/x-matroska"))
            .mediaType("avi", MediaType.valueOf("video/x-msvideo"))
            .mediaType("mov", MediaType.valueOf("video/quicktime"))
            .mediaType("wmv", MediaType.valueOf("video/x-ms-wmv"))
            .mediaType("flv", MediaType.valueOf("video/x-flv"))
            .mediaType("webm", MediaType.valueOf("video/webm"));
    }
}

