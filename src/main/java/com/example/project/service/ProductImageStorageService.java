package com.example.project.service;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.text.Normalizer;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * Uploads/removes product photos on Cloudinary. The {@code public_id} is always
 * {@code "pharmacy-products/" + product.code} — {@code Product.code} is unique and immutable once
 * assigned (see {@code ProductService.generateNextProductCode()}), so it doubles as a stable asset
 * key without needing a separate column to remember Cloudinary's own identifier.
 *
 * <p>Folder/public_id are deliberately flat (not per-{@code Type}) because {@code Type.name} and a
 * Product's {@code typeId} can both change after the image already exists, which would otherwise
 * orphan/duplicate assets. Instead, every asset also gets a {@code "type-{slug}"} Cloudinary tag so
 * images can still be filtered/managed by category — see {@link #typeTag(String)} and
 * {@link #retag(String, String, String)}.</p>
 */
@Service
public class ProductImageStorageService {

    private static final String FOLDER = "pharmacy-products";
    private static final String TYPE_TAG_FALLBACK = "type-chua-phan-loai";

    private static final Pattern DIACRITICS = Pattern.compile("\\p{Mn}+");
    private static final Pattern NON_ALNUM = Pattern.compile("[^a-z0-9]+");
    private static final Pattern EDGE_HYPHENS = Pattern.compile("^-+|-+$");

    private final Cloudinary cloudinary;

    public ProductImageStorageService(Cloudinary cloudinary) {
        this.cloudinary = cloudinary;
    }

    /**
     * Uploads (or overwrites, if one already exists for this product code) and returns the public
     * HTTPS URL. {@code public_id} is passed as the bare product code (not pre-joined with
     * {@link #FOLDER}) — Cloudinary prepends the {@code folder} option to it itself; passing an
     * already-prefixed public_id together with {@code folder} would double up the path (e.g.
     * {@code pharmacy-products/pharmacy-products/SP000002}). The resulting effective public_id is
     * still {@code "pharmacy-products/" + productCode}, matching {@link #publicId(String)} used by
     * {@link #retag} / {@link #delete}.
     */
    public String upload(MultipartFile file, String productCode, String typeName) {
        try {
            Map<?, ?> result = cloudinary.uploader().upload(file.getBytes(), ObjectUtils.asMap(
                    "public_id", productCode,
                    "folder", FOLDER,
                    "overwrite", true,
                    "invalidate", true,
                    "tags", List.of(typeTag(typeName))
            ));
            return (String) result.get("secure_url");
        } catch (IOException e) {
            throw new IllegalStateException("Không thể tải ảnh lên Cloudinary", e);
        }
    }

    /**
     * Swaps the category tag on an already-uploaded asset without re-uploading the file — used when
     * a product's Type changes but its photo doesn't. Only the two tags involved are touched; any
     * other tags the asset may carry are left alone.
     */
    public void retag(String productCode, String oldTag, String newTag) {
        try {
            String[] publicIds = {publicId(productCode)};
            if (oldTag != null) {
                cloudinary.uploader().removeTag(oldTag, publicIds, ObjectUtils.emptyMap());
            }
            cloudinary.uploader().addTag(newTag, publicIds, ObjectUtils.emptyMap());
        } catch (IOException e) {
            throw new IllegalStateException("Không thể cập nhật tag ảnh trên Cloudinary", e);
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

    /** {@code "type-{slug}"} for the given Type name, or the fallback tag when null/blank/unslug-able. */
    public String typeTag(String typeName) {
        String slug = slugify(typeName);
        return slug.isEmpty() ? TYPE_TAG_FALLBACK : "type-" + slug;
    }

    private String slugify(String input) {
        if (input == null) {
            return "";
        }
        String trimmed = input.trim();
        if (trimmed.isEmpty()) {
            return "";
        }
        String dReplaced = trimmed.replace('đ', 'd').replace('Đ', 'D');
        String decomposed = Normalizer.normalize(dReplaced, Normalizer.Form.NFD);
        String withoutDiacritics = DIACRITICS.matcher(decomposed).replaceAll("");
        String lower = withoutDiacritics.toLowerCase(Locale.ROOT);
        String hyphenated = NON_ALNUM.matcher(lower).replaceAll("-");
        return EDGE_HYPHENS.matcher(hyphenated).replaceAll("");
    }

    private String publicId(String productCode) {
        return FOLDER + "/" + productCode;
    }
}
