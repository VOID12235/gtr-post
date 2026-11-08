package com.btr.platform.controller;

import com.btr.platform.common.ApiResponse;
import com.btr.platform.entity.UploadFile;
import com.btr.platform.service.FileStorageService;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
public class FileController {

    private final FileStorageService fileStorageService;

    public FileController(FileStorageService fileStorageService) {
        this.fileStorageService = fileStorageService;
    }

    @PostMapping("/api/v1/files")
    public ApiResponse<UploadFile> upload(@RequestPart("file") MultipartFile file) {
        return ApiResponse.ok(fileStorageService.save(file));
    }

    @GetMapping("/api/v1/files/{id}")
    public ResponseEntity<Resource> download(@PathVariable Long id) {
        UploadFile file = fileStorageService.get(id);
        Resource resource = fileStorageService.load(file);
        MediaType mediaType = MediaType.APPLICATION_OCTET_STREAM;
        if (file.contentType != null && !file.contentType.isBlank()) {
            try {
                mediaType = MediaType.parseMediaType(file.contentType);
            } catch (Exception ignored) {
                // keep octet-stream
            }
        }
        String filename = file.originalName == null ? "file" : file.originalName.replace("\"", "");
        return ResponseEntity.ok()
                .contentType(mediaType)
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + filename + "\"")
                .body(resource);
    }
}
