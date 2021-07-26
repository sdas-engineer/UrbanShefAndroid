package com.urbanshef.urbanshefapp.utils;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

import com.urbanshef.urbanshefapp.BuildConfig;
import com.urbanshef.urbanshefapp.ImageUrlValidationListener;
import com.urbanshef.urbanshefapp.R;

import java.net.URLEncoder;

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

    public static ProductFlavor getProductFlavor() {
        return BuildConfig.FLAVOR.equalsIgnoreCase("UAT") ? ProductFlavor.UAT : ProductFlavor.RELEASE;
    }

    public static String getTargetStripeKey(Context mContext) {
        return getProductFlavor() == ProductFlavor.UAT ? mContext.getString(R.string.Test_Stripe_Key) : mContext.getString(R.string.Live_Stripe_Key);
    }

    public static AlertDialog showAlertDialog(Context mContext, String title, String message, DialogInterface.OnClickListener okayClickListener) {
        return new AlertDialog.Builder(mContext)
                .setTitle(title)
                .setMessage(message)
                .setPositiveButton(mContext.getString(android.R.string.ok), okayClickListener)
                .show();
    }

    public static void invokeWhatsAppChatIntent(Context mContext, String phone, String message) {
        PackageManager packageManager = mContext.getPackageManager();
        Intent i = new Intent(Intent.ACTION_VIEW);
        try {
            String url = "https://api.whatsapp.com/send?phone=" + phone + "&text=" + URLEncoder.encode(message, "UTF-8");
            i.setPackage("com.whatsapp");
            i.setData(Uri.parse(url));
            if (i.resolveActivity(packageManager) != null) {
                mContext.startActivity(i);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
