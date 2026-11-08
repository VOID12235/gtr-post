package com.btr.platform.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.btr.platform.common.BizException;
import com.btr.platform.common.ErrorCode;
import com.btr.platform.entity.UploadFile;
import com.btr.platform.mapper.UploadFileMapper;
import com.btr.platform.tenant.TenantContext;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class FileStorageService {

    private final UploadFileMapper fileMapper;
    private final ConfigService configService;
    private final String uploadDir;

    public FileStorageService(UploadFileMapper fileMapper,
                              ConfigService configService,
                              @Value("${btr.upload.dir:./uploads}") String uploadDir) {
        this.fileMapper = fileMapper;
        this.configService = configService;
        this.uploadDir = uploadDir;
    }

    public UploadFile save(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BizException(ErrorCode.BAD_REQUEST, "文件为空");
        }
        Long tenantId = TenantContext.require();
        Map<String, String> upload = configService.getRawGroup("upload");
        long maxBytes = Long.parseLong(upload.getOrDefault("max_size_mb", "50")) * 1024L * 1024L;
        if (file.getSize() > maxBytes) {
            throw new BizException(ErrorCode.BAD_REQUEST,
                    "文件超过上限：" + upload.getOrDefault("max_size_mb", "50") + "MB");
        }

        String original = file.getOriginalFilename() == null ? "file" : file.getOriginalFilename();
        String ext = extension(original);
        Set<String> allowed = Arrays.stream(upload.getOrDefault("allowed_types", "").split(","))
                .map(String::trim)
                .map(s -> s.toLowerCase(Locale.ROOT))
                .filter(s -> !s.isBlank())
                .collect(Collectors.toSet());
        if (!allowed.isEmpty() && !allowed.contains(ext)) {
            throw new BizException(ErrorCode.BAD_REQUEST, "不支持的文件类型：" + ext);
        }

        String storedName = tenantId + "/" + LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMM"))
                + "/" + UUID.randomUUID().toString().replace("-", "") + (ext.isBlank() ? "" : "." + ext);
        Path target = Paths.get(uploadDir).resolve(storedName).normalize();
        try {
            Files.createDirectories(target.getParent());
            file.transferTo(target);
        } catch (IOException ex) {
            throw new BizException(ErrorCode.INTERNAL, "文件保存失败");
        }

        UploadFile record = new UploadFile();
        record.tenantId = tenantId;
        record.originalName = original;
        record.storedName = storedName;
        record.contentType = file.getContentType();
        record.size = file.getSize();
        record.createdAt = LocalDateTime.now();
        record.url = "";
        fileMapper.insert(record);
        record.url = "/api/v1/files/" + record.id;
        fileMapper.updateById(record);
        return record;
    }

    public UploadFile get(Long id) {
        UploadFile file = fileMapper.selectByIdIgnoreTenant(id);
        if (file == null) {
            throw new BizException(ErrorCode.NOT_FOUND, "文件不存在");
        }
        return file;
    }

    public Resource load(UploadFile file) {
        Path path = Paths.get(uploadDir).resolve(file.storedName).normalize();
        if (!Files.exists(path)) {
            throw new BizException(ErrorCode.NOT_FOUND, "文件已丢失");
        }
        return new FileSystemResource(path);
    }

    public long count() {
        Long count = fileMapper.selectCount(new QueryWrapper<UploadFile>());
        return count == null ? 0 : count;
    }

    private String extension(String name) {
        int index = name.lastIndexOf('.');
        return index < 0 ? "" : name.substring(index + 1).toLowerCase(Locale.ROOT);
    }
}
