// src/main/java/com/mysite/sbb/CloudinaryService.java
package com.mysite.sbb;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

@RequiredArgsConstructor
@Service
public class CloudinaryService {
    
    private final Cloudinary cloudinary;
    
    public String uploadImage(MultipartFile file) throws IOException {
        Map uploadResult = cloudinary.uploader().upload(file.getBytes(), 
            ObjectUtils.asMap(
                "folder", "sbb",  // Cloudinary 폴더명
                "resource_type", "auto"
            ));
        
        return (String) uploadResult.get("url");  // 이미지 URL 반환
    }
    
    public void deleteImage(String imageUrl) throws IOException {
        // URL에서 public_id 추출하여 삭제
        String publicId = extractPublicId(imageUrl);
        cloudinary.uploader().destroy(publicId, ObjectUtils.emptyMap());
    }
    
    private String extractPublicId(String imageUrl) {
        // URL에서 public_id 추출 로직
        String[] parts = imageUrl.split("/");
        String filename = parts[parts.length - 1];
        return "sbb/" + filename.split("\\.")[0];
    }
}