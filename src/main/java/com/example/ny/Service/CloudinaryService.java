package com.example.ny.Service;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

@Service
public class CloudinaryService {

    private final Cloudinary cloudinary;

    public CloudinaryService(@Value("${cloudinary.url:}") String cloudinaryUrl) {
        if (cloudinaryUrl != null && !cloudinaryUrl.isBlank()) {
            this.cloudinary = new Cloudinary(cloudinaryUrl);
        } else {
            this.cloudinary = new Cloudinary();
        }
    }

    /**
     * Upload ảnh hoặc file âm thanh lên Cloudinary.
     * Lưu ý: Cloudinary dùng resource_type = "video" cho cả file audio (.mp3, .m4a, .wav).
     */
    public String uploadFile(MultipartFile file, boolean isAudio) throws IOException {
        Map<?, ?> options = ObjectUtils.asMap(
                "folder", "mygirlfriend",
                "resource_type", isAudio ? "video" : "image"
        );
        Map<?, ?> uploadResult = cloudinary.uploader().upload(file.getBytes(), options);
        return uploadResult.get("secure_url").toString();
    }
}