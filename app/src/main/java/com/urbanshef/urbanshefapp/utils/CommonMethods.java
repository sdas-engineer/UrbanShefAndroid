package com.urbanshef.urbanshefapp.utils;

import android.app.Activity;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

import com.urbanshef.urbanshefapp.ImageUrlValidationListener;

public class CommonMethods {

    public static void hideKeyboard(Activity activity) {
        if (activity != null) {
            InputMethodManager imm = (InputMethodManager) activity.getSystemService(Activity.INPUT_METHOD_SERVICE);
            //Find the currently focused view, so we can grab the correct window token from it.
            View view = activity.getCurrentFocus();
            //If no view currently has focus, create a new one, just so we can grab a window token from it
            if (view == null) {
                view = new View(activity);
            }
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    public static void loadImageFromPath(String imageUrl, ImageUrlValidationListener urlValidationListener) {
        if (imageUrl != null && !imageUrl.isEmpty()) {
            urlValidationListener.imageUrlValidationSuccess(imageUrl);
        } else {
            urlValidationListener.imageUrlValidationFailure(imageUrl);
        }
    }
}
