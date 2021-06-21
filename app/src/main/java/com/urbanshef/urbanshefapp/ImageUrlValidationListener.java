package com.urbanshef.urbanshefapp;

public interface ImageUrlValidationListener {
    void imageUrlValidationSuccess(String imageUrl);

    void imageUrlValidationFailure(String imageUrl);
}
