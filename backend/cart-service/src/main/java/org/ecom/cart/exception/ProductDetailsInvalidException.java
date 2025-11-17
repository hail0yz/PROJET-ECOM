package org.ecom.cart.exception;

import org.ecom.cart.product.BulkBookValidationResponse;

public class ProductDetailsInvalidException extends RuntimeException {

    private BulkBookValidationResponse response;

    public ProductDetailsInvalidException(BulkBookValidationResponse response) {
        super();
        this.response = response;
    }

    public ProductDetailsInvalidException(BulkBookValidationResponse response, String message) {
        super(message);
        this.response = response;
    }

}
