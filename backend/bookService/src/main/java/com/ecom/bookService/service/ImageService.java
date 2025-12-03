package com.ecom.bookService.service;

import com.ecom.bookService.exception.ImageUploadFailedException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.util.Base64;
import java.util.Map;

@Service
@Slf4j
@RequiredArgsConstructor
public class ImageService {

    @Value("${imgbb.api-key}")
    private String apiKey;

    private static final String IMGBB_API_URL = "https://api.imgbb.com/1/upload";
    private final RestTemplate restTemplate = new RestTemplate();

    /**
     * Upload une image vers ImgBB
     * @param file Le fichier à uploader
     * @return L'URL de l'image uploadée
     */
    public String uploadImage(MultipartFile file) throws ImageUploadFailedException {
        if (file == null || file.isEmpty()) {
            throw new ImageUploadFailedException("File is empty");
        }

        if (file.getContentType() == null || !file.getContentType().startsWith("image/")) {
            throw new ImageUploadFailedException("File is not an image");
        }

        try {
            String base64Image = Base64.getEncoder().encodeToString(file.getBytes());

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.MULTIPART_FORM_DATA);

            MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
            body.add("key", apiKey);
            body.add("image", base64Image);

            HttpEntity<MultiValueMap<String, String>> requestEntity = new HttpEntity<>(body, headers);

            ResponseEntity<Map> response = restTemplate.exchange(
                    IMGBB_API_URL,
                    HttpMethod.POST,
                    requestEntity,
                    Map.class
            );

            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                Map<String, Object> responseBody = response.getBody();
                Map<String, Object> data = (Map<String, Object>) responseBody.get("data");
                String imageUrl = (String) data.get("url");

                log.info("Image uploaded successfully to ImgBB: {}", imageUrl);
                return imageUrl;
            } else {
                throw new ImageUploadFailedException("Failed to upload image to ImgBB");
            }

        } catch (Exception e) {
            log.error("Error uploading image to ImgBB", e);
            throw new ImageUploadFailedException("Failed to upload image: " + e.getMessage());
        }
    }

    /**
     * Note: ImgBB ne permet pas la suppression d'images via l'API gratuite
     * Les images sont automatiquement supprimées après 6 mois d'inactivité
     */
    public void deleteImage(String imageUrl) {
        log.warn("ImgBB free API does not support image deletion. Image: {}", imageUrl);
    }

}
