package com.example.project.service;

import java.util.List;

/**
 * Thrown by {@link ProductService#createProduct} when the submitted product fails business
 * validation. Carries the user-facing error messages so the controller can re-render the form.
 */
public class ProductValidationException extends RuntimeException {

    private final List<String> errors;

    public ProductValidationException(List<String> errors) {
        super(String.join("; ", errors));
        this.errors = errors;
    }

    public List<String> getErrors() {
        return errors;
    }
}
