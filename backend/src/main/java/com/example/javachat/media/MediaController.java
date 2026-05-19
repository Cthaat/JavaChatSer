package com.example.javachat.media;

import com.example.javachat.common.ApiResponse;
import com.example.javachat.media.dto.UploadResponse;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/uploads")
public class MediaController {

    private final MediaStorageService mediaStorageService;

    public MediaController(MediaStorageService mediaStorageService) {
        this.mediaStorageService = mediaStorageService;
    }

    @PostMapping("/images")
    public ApiResponse<UploadResponse> uploadImage(@RequestPart("file") MultipartFile file) {
        return ApiResponse.success(mediaStorageService.storeImage(file, "images"));
    }
}
