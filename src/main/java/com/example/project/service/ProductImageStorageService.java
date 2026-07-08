package com.example.project.service;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

/**
 * Uploads/removes product photos on Cloudinary. The {@code public_id} is always
 * {@code "pharmacy-products/" + product.code} — {@code Product.code} is unique and immutable once
 * assigned (see {@code ProductService.generateNextProductCode()}), so it doubles as a stable asset
 * key without needing a separate column to remember Cloudinary's own identifier.
 */
@Service
public class ProductImageStorageService {

    private static final String FOLDER = "pharmacy-products";

    private final Cloudinary cloudinary;

    public ProductImageStorageService(Cloudinary cloudinary) {
        this.cloudinary = cloudinary;
    }

    /** Uploads (or overwrites, if one already exists for this product code) and returns the public HTTPS URL. */
    public String upload(MultipartFile file, String productCode) {
        try {
            Map<?, ?> result = cloudinary.uploader().upload(file.getBytes(), ObjectUtils.asMap(
                    "public_id", publicId(productCode),
                    "overwrite", true,
                    "invalidate", true
            ));
            return (String) result.get("secure_url");
        } catch (IOException e) {
            throw new IllegalStateException("Không thể tải ảnh lên Cloudinary", e);
        }
    }

    /** Removes the asset for this product code, if any. */
    public void delete(String productCode) {
        try {
            cloudinary.uploader().destroy(publicId(productCode), ObjectUtils.emptyMap());
        } catch (IOException e) {
            throw new IllegalStateException("Không thể xoá ảnh trên Cloudinary", e);
        }
    }

    private String publicId(String productCode) {
        return FOLDER + "/" + productCode;
    }
}
