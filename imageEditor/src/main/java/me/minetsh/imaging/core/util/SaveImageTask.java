package me.minetsh.imaging.core.util;

import android.app.Activity;
import android.app.Dialog;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.text.TextUtils;
import android.widget.Toast;

import me.minetsh.imaging.R;

public abstract class SaveImageTask extends AsyncTask<Bitmap, Void, Boolean> {
    private Dialog dialog;
    private String saveFilePath;
    private Activity context;

    public SaveImageTask(String saveFilePath, Activity context){
        super();
        this.saveFilePath = saveFilePath;
        this.context = context;

    }
    @Override
    protected Boolean doInBackground(Bitmap... params) {
        if (TextUtils.isEmpty(saveFilePath))
            return false;

        if (params[0]==null) {
            Toast.makeText(context, R.string.image_save_error, Toast.LENGTH_SHORT).show();
            return false;
        }

        return ImageSaveNotifyAblumUtil.saveBitmap(params[0], saveFilePath);
    }

    @Override
    protected void onCancelled() {
        super.onCancelled();
        if (context != null && (!context.isFinishing() ||!context.isDestroyed())) {
            dialog.dismiss();
        }
    }

    @Override
    protected void onCancelled(Boolean result) {
        super.onCancelled(result);
        if (context != null && (!context.isFinishing() ||!context.isDestroyed())) {
            dialog.dismiss();
        }
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        dialog = ImageSaveNotifyAblumUtil.getLoadingDialog(context, R.string.image_saving_hint, false);
        dialog.show();
    }

    @Override
    protected void onPostExecute(Boolean result) {
        super.onPostExecute(result);
        if (context != null && (!context.isFinishing() ||!context.isDestroyed())) {
            dialog.dismiss();
        }
        if (result) {
            onFinish(result);
        } else {
            Toast.makeText(context, R.string.image_save_error, Toast.LENGTH_SHORT).show();
        }
    }

    public abstract void onFinish(boolean success);
}