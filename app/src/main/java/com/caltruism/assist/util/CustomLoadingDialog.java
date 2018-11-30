package com.caltruism.assist.util;

import android.app.Dialog;
import android.content.Context;
import android.view.Window;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.DrawableImageViewTarget;
import com.caltruism.assist.R;

public class CustomLoadingDialog {

    private Context context;
    private Dialog dialog;

    public CustomLoadingDialog(Context context) {
        this.context = context;
    }

    public void showDialog() {

        dialog = new Dialog(context);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setCancelable(false);
        dialog.setContentView(R.layout.loading_layout);

        ImageView imageView = dialog.findViewById(R.id.customLoadingView);
        DrawableImageViewTarget imageViewTarget = new DrawableImageViewTarget(imageView);
        Glide.with(context).setDefaultRequestOptions(new RequestOptions().centerCrop())
                .load(R.drawable.loading).into(imageViewTarget);

        dialog.show();
    }

    public void hideDialog(){
        if (dialog != null)
            dialog.dismiss();
    }
}
