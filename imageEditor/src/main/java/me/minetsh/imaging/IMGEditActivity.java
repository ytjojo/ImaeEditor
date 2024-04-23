package me.minetsh.imaging;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import me.minetsh.imaging.core.IMGMode;
import me.minetsh.imaging.core.IMGText;
import me.minetsh.imaging.core.file.IMGAssetFileDecoder;
import me.minetsh.imaging.core.file.IMGDecoder;
import me.minetsh.imaging.core.file.IMGFileDecoder;
import me.minetsh.imaging.core.util.IMGUtils;
import me.minetsh.imaging.core.util.ImageSaveNotifyAblumUtil;

/**
 * Created by felix on 2017/11/14 下午2:26.
 */

public class IMGEditActivity extends IMGEditBaseActivity {

    private static final int MAX_WIDTH = 1024 * 2;

    private static final int MAX_HEIGHT = 1024 * 15;
    private static final int maxImageSquare = 8 * 1024 * 1024;

    public static final String EXTRA_IMAGE_URI = "IMAGE_URI";

    public static final String EXTRA_IMAGE_SAVE_PATH = "IMAGE_SAVE_PATH";
    public static final String SAVE_FILE_PATH = "save_file_path";

    public static final String IMAGE_IS_EDIT = "image_is_edit";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onCreated() {
        mImgView.setDoodlePaintStrokeWidth(Math.round(dip2px(3f)));
        mImgView.setMosaicPaintStrokeWidth(Math.round(dip2px(10f)));
    }

    @Override
    public Bitmap getBitmap() {
        Intent intent = getIntent();
        if (intent == null) {
            return null;
        }

        Uri uri = intent.getParcelableExtra(EXTRA_IMAGE_URI);
        if (uri == null) {
            return null;
        }

        IMGDecoder decoder = null;

        String path = uri.getPath();
        if (!TextUtils.isEmpty(path)) {
            switch (uri.getScheme()) {
                case "asset":
                    decoder = new IMGAssetFileDecoder(this, uri);
                    break;
                case "file":
                    decoder = new IMGFileDecoder(uri);
                    break;
            }
        }

        if (decoder == null) {
            return null;
        }

        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inSampleSize = 2;
        options.inJustDecodeBounds = true;

        decoder.decode(options);

        if (options.outWidth > MAX_WIDTH) {
            options.inSampleSize = IMGUtils.inSampleSize(Math.round(1f * options.outWidth / MAX_WIDTH));
        }

        if (options.outHeight > MAX_HEIGHT) {
            options.inSampleSize = Math.max(options.inSampleSize,
                    IMGUtils.inSampleSize(Math.round(1f * options.outHeight / MAX_HEIGHT)));
        }

        options.inJustDecodeBounds = false;

        Bitmap bitmap = decoder.decode(options);
        if (bitmap == null) {
            return null;
        }

        return bitmap;
    }

    public boolean isLongImage(int width, int height) {
        int max = Math.max(width, height);
        int min = Math.min(width, height);
        return max > min * 3 && (min > 200 || width * height > maxImageSquare);
    }

    public int inSampleSize(int width, int height) {
        int max = Math.max(width, height);
        int min = Math.min(width, height);
        int inSampleSize = 1;
        if (isLongImage(width, height)) {
            if (min > MAX_WIDTH) {
                inSampleSize = IMGUtils.inSampleSize(Math.round(1f * min / MAX_WIDTH));
            }

            if (max > MAX_HEIGHT) {
                inSampleSize = Math.max(inSampleSize,
                        IMGUtils.inSampleSize(Math.round(1f * max / MAX_HEIGHT)));
            }
        } else {
            if (width * height > maxImageSquare) {

                while ((width * height) / inSampleSize * inSampleSize > maxImageSquare) {
                    inSampleSize = inSampleSize * 2;
                }
            }
        }
        return inSampleSize;

    }


    @Override
    public void onText(IMGText text) {
        mImgView.addStickerText(text);
    }

    @Override
    public void onModeClick(IMGMode mode) {
        IMGMode cm = mImgView.getMode();
        if (cm == mode) {
            mode = IMGMode.NONE;
        }
        mImgView.setMode(mode);
        updateModeUI();

        if (mode == IMGMode.CLIP) {
            setOpDisplay(OP_CLIP);
        }
    }

    @Override
    public void onUndoClick() {
        IMGMode mode = mImgView.getMode();
        if (mode == IMGMode.DOODLE) {
            mImgView.undoDoodle();
        } else if (mode == IMGMode.MOSAIC) {
            mImgView.undoMosaic();
        }
    }

    @Override
    public void onCancelClick() {
        finish();
    }

    @Override
    public void onDoneClick() {
        String path = getIntent().getStringExtra(EXTRA_IMAGE_SAVE_PATH);
        if (!TextUtils.isEmpty(path)) {
            File targetFile = new File(path);
            File parentFile = targetFile.getParentFile();
            if (!parentFile.exists()) {
                parentFile.mkdirs();
            }
            if (targetFile.exists()) {
                targetFile.deleteOnExit();
            }
            Bitmap bitmap = mImgView.saveBitmap();
            if (bitmap != null) {
                FileOutputStream fout = null;
                try {
                    fout = new FileOutputStream(path);
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 90, fout);
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } finally {
                    if (fout != null) {
                        try {
                            fout.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
                ImageSaveNotifyAblumUtil.onSaveTaskDone(this, path);
                Intent dataIntent = new Intent();
                dataIntent.putExtra(SAVE_FILE_PATH, path);
                boolean isEdited = mImgView.isEdited();
                dataIntent.putExtra(IMAGE_IS_EDIT, isEdited);
                setResult(RESULT_OK, dataIntent);
                finish();
                return;
            }
        }
        setResult(RESULT_CANCELED);
        finish();
    }

    @Override
    public void onCancelClipClick() {
        mImgView.cancelClip();
        setOpDisplay(mImgView.getMode() == IMGMode.CLIP ? OP_CLIP : OP_NORMAL);
    }

    @Override
    public void onDoneClipClick() {
        mImgView.doClip();
        setOpDisplay(mImgView.getMode() == IMGMode.CLIP ? OP_CLIP : OP_NORMAL);
    }

    @Override
    public void onResetClipClick() {
        mImgView.resetClip();
    }

    @Override
    public void onRotateClipClick() {
        mImgView.doRotate();
    }

    @Override
    public void onClipRatioClick(int w, int h) {
        // TODO
    }

    @Override
    public void onColorChanged(int checkedColor) {
        mImgView.setPenColor(checkedColor);
    }


    public float dip2px( float dpValue) {
        final float scale = this.getApplication().getResources().getDisplayMetrics().density;
        return(dpValue * scale + 0.5f);
    }
}
