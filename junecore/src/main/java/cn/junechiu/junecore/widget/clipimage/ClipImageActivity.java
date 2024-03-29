
package cn.junechiu.junecore.widget.clipimage;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.*;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.media.ExifInterface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import cn.junechiu.junecore.R;

import java.io.FileOutputStream;
import java.io.IOException;

/**
 * 裁剪图片
 */
public class ClipImageActivity extends Activity implements View.OnClickListener {

    public static final int REQUEST_CROP = 6709;

    private ClipImageView mClipImageView;

    private TextView mCancel;

    private TextView mClip;

    private String mOutput;

    private String mInput;

    private int mMaxWidth;

    // 图片被旋转的角度
    private int mDegree;

    // 大图被设置之前的缩放比例
    private int mSampleSize;

    private int mSourceWidth;

    private int mSourceHeight;

    private ProgressDialog mDialog;

    private boolean canScale;

    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (msg.what == 0) {
                if (mDialog != null) {
                    mDialog.dismiss();
                }
            } else {
                Toast.makeText(ClipImageActivity.this, R.string.msg_could_not_save_photo,
                        Toast.LENGTH_SHORT).show();
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setContentView(R.layout.activity__clip__image);
        mClipImageView = findViewById(R.id.clip_image_view);
        mCancel = findViewById(R.id.cancel);
        mClip = findViewById(R.id.clip);

        mCancel.setOnClickListener(this);
        mClip.setOnClickListener(this);

        ClipOptions clipOptions = ClipOptions.createFromBundle(getIntent());
        mOutput = clipOptions.getOutputPath();
        mInput = clipOptions.getInputPath();
        mMaxWidth = clipOptions.getMaxWidth();
        canScale = clipOptions.getCanScale();
        mClipImageView.setAspect(clipOptions.getAspectX(), clipOptions.getAspectY());
        mClipImageView.setTip(clipOptions.getTip());
        mClipImageView.setMaxOutputWidth(mMaxWidth);
        mClipImageView.setCanScale(canScale);

        setImageAndClipParams(); // 大图裁剪
        // mClipImageView.setImageURI(Uri.fromFile(new File(mInput)));
        mDialog = new ProgressDialog(this);
        mDialog.setMessage(getString(R.string.msg_clipping_image));
    }

    // 设置要剪裁的图片
    private void setImageAndClipParams() {
        mClipImageView.post(() -> {
            mClipImageView.setMaxOutputWidth(mMaxWidth);
            mDegree = readPictureDegree(mInput);
            final boolean isRotate = (mDegree == 90 || mDegree == 270);
            final BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;
            BitmapFactory.decodeFile(mInput, options);

            mSourceWidth = options.outWidth;
            mSourceHeight = options.outHeight;
            // 如果图片被旋转，则宽高度置换
            int w = isRotate ? options.outHeight : options.outWidth;
            // 裁剪是宽高比例3:2，只考虑宽度情况，这里按border宽度的两倍来计算缩放。
            mSampleSize = findBestSample(w, mClipImageView.getClipBorder().width());

            options.inJustDecodeBounds = false;
            options.inSampleSize = mSampleSize;
            options.inPreferredConfig = Bitmap.Config.RGB_565;
            final Bitmap source = BitmapFactory.decodeFile(mInput, options);

            // 解决图片被旋转的问题
            Bitmap target;
            if (mDegree == 0) {
                target = source;
            } else {
                final Matrix matrix = new Matrix();
                matrix.postRotate(mDegree);
                target = Bitmap.createBitmap(source, 0, 0, source.getWidth(),
                        source.getHeight(), matrix, false);
                if (target != source && !source.isRecycled()) {
                    source.recycle();
                }
            }
            mClipImageView.setImageBitmap(target);
        });
    }

    /**
     * 计算最好的采样大小。
     *
     * @param origin 当前宽度
     * @param target 限定宽度
     * @return sampleSize
     */
    private static int findBestSample(int origin, int target) {
        int sample = 1;
        for (int out = origin / 2; out > target; out /= 2) {
            sample *= 2;
        }
        return sample;
    }

    /**
     * 读取图片属性：旋转的角度
     *
     * @param path 图片绝对路径
     * @return degree旋转的角度
     */
    public static int readPictureDegree(String path) {
        int degree = 0;
        try {
            ExifInterface exifInterface = new ExifInterface(path);
            int orientation = exifInterface.getAttributeInt(ExifInterface.TAG_ORIENTATION,
                    ExifInterface.ORIENTATION_NORMAL);
            switch (orientation) {
                case ExifInterface.ORIENTATION_ROTATE_90:
                    degree = 90;
                    break;
                case ExifInterface.ORIENTATION_ROTATE_180:
                    degree = 180;
                    break;
                case ExifInterface.ORIENTATION_ROTATE_270:
                    degree = 270;
                    break;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return degree;
    }

    @Override
    public void onClick(View v) {
        final int id = v.getId();
        if (id == R.id.cancel) {
            onBackPressed();
        }
        if (id == R.id.clip) {
            clipImage();
        }
    }

    // 对图片进行剪裁
    private void clipImage() {
        if (mOutput != null) {
            mDialog.show();
            new Thread(() -> {
                FileOutputStream fos = null;
                try {
                    fos = new FileOutputStream(mOutput);
                    Bitmap bitmap = createClippedBitmap();
                    bitmap.compress(Bitmap.CompressFormat.PNG, 80, fos);
                    if (!bitmap.isRecycled()) {
                        bitmap.recycle();
                    }
                    handler.sendEmptyMessage(0);
                    setResult(Activity.RESULT_OK, getIntent());
                    finish();
                } catch (Exception e) {
                    e.printStackTrace();
                    handler.sendEmptyMessage(1);
                } finally {
                    IOUtils.close(fos);
                }
            }).start();
        } else {
            finish();
        }
    }

    // 图片剪裁
    private Bitmap createClippedBitmap() {
        if (mSampleSize <= 1) {
            return mClipImageView.clip();
        }

        // 获取缩放位移后的矩阵值
        final float[] matrixValues = mClipImageView.getClipMatrixValues();
        final float scale = matrixValues[Matrix.MSCALE_X];
        final float transX = matrixValues[Matrix.MTRANS_X];
        final float transY = matrixValues[Matrix.MTRANS_Y];

        // 获取在显示的图片中裁剪的位置
        final Rect border = mClipImageView.getClipBorder();
        final float cropX = ((-transX + border.left) / scale) * mSampleSize;
        final float cropY = ((-transY + border.top) / scale) * mSampleSize;
        final float cropWidth = (border.width() / scale) * mSampleSize;
        final float cropHeight = (border.height() / scale) * mSampleSize;

        if (canScale) {
            Drawable drawable = mClipImageView.getDrawable();
            Bitmap originalBitmap = ((BitmapDrawable)drawable).getBitmap();
            Matrix matrix = new Matrix();
            matrix.postScale(scale, scale); // 长和宽放大缩小的比例
            Bitmap resizeBmp = Bitmap.createBitmap(originalBitmap, 0, 0, originalBitmap.getWidth(),
                    originalBitmap.getHeight(), matrix, true);
            Bitmap bgBitmap = Bitmap.createBitmap(border.width(), border.height(),
                    Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(bgBitmap);
            Paint paint = new Paint();
            paint.setAlpha(255);
            int centerX = (bgBitmap.getWidth() - resizeBmp.getWidth()) / 2;
            int centerY = (bgBitmap.getHeight() - resizeBmp.getHeight()) / 2;
            canvas.drawBitmap(resizeBmp, centerX, centerY, paint);
            return bgBitmap;
        }

        // 获取在旋转之前的裁剪位置
        final RectF srcRect = new RectF(cropX, cropY, cropX + cropWidth, cropY + cropHeight);
        final Rect clipRect = getRealRect(srcRect);

        final BitmapFactory.Options ops = new BitmapFactory.Options();
        final Matrix outputMatrix = new Matrix();

        outputMatrix.setRotate(mDegree);
        // 如果裁剪之后的图片宽高仍然太大,则进行缩小
        if (mMaxWidth > 0 && cropWidth > mMaxWidth) {
            ops.inSampleSize = findBestSample((int)cropWidth, mMaxWidth);
            final float outputScale = mMaxWidth / (cropWidth / ops.inSampleSize);
            outputMatrix.postScale(outputScale, outputScale);
        }

        // 裁剪
        BitmapRegionDecoder decoder = null;
        try {
            decoder = BitmapRegionDecoder.newInstance(mInput, false);
            final Bitmap source = decoder.decodeRegion(clipRect, ops);
            recycleImageViewBitmap();
            return Bitmap.createBitmap(source, 0, 0, source.getWidth(), source.getHeight(),
                    outputMatrix, false);
        } catch (Exception e) {
            return mClipImageView.clip();
        } finally {
            if (decoder != null && !decoder.isRecycled()) {
                decoder.recycle();
            }
        }
    }

    private Rect getRealRect(RectF srcRect) {
        switch (mDegree) {
            case 90:
                return new Rect((int)srcRect.top, (int)(mSourceHeight - srcRect.right),
                        (int)srcRect.bottom, (int)(mSourceHeight - srcRect.left));
            case 180:
                return new Rect((int)(mSourceWidth - srcRect.right),
                        (int)(mSourceHeight - srcRect.bottom), (int)(mSourceWidth - srcRect.left),
                        (int)(mSourceHeight - srcRect.top));
            case 270:
                return new Rect((int)(mSourceWidth - srcRect.bottom), (int)srcRect.left,
                        (int)(mSourceWidth - srcRect.top), (int)srcRect.right);
            default:
                return new Rect((int)srcRect.left, (int)srcRect.top, (int)srcRect.right,
                        (int)srcRect.bottom);
        }
    }

    private void recycleImageViewBitmap() {
        mClipImageView.post(new Runnable() {
            @Override
            public void run() {
                mClipImageView.setImageBitmap(null);
            }
        });
    }

    @Override
    public void onBackPressed() {
        setResult(Activity.RESULT_CANCELED, getIntent());
        super.onBackPressed();
    }

    public static ClipOptions prepare() {
        return new ClipOptions();
    }

    public static class ClipOptions {
        private int aspectX;

        private int aspectY;

        private int maxWidth;

        private String tip;

        private String inputPath;

        private String outputPath;

        private boolean canScale;

        private ClipOptions() {
        }

        public ClipOptions aspectX(int aspectX) {
            this.aspectX = aspectX;
            return this;
        }

        public ClipOptions aspectY(int aspectY) {
            this.aspectY = aspectY;
            return this;
        }

        public ClipOptions maxWidth(int maxWidth) {
            this.maxWidth = maxWidth;
            return this;
        }

        public ClipOptions tip(String tip) {
            this.tip = tip;
            return this;
        }

        public ClipOptions inputPath(String path) {
            this.inputPath = path;
            return this;
        }

        public ClipOptions outputPath(String path) {
            this.outputPath = path;
            return this;
        }

        public ClipOptions canScale(boolean canScale) {
            this.canScale = canScale;
            return this;
        }

        public int getAspectX() {
            return aspectX;
        }

        public int getAspectY() {
            return aspectY;
        }

        public int getMaxWidth() {
            return maxWidth;
        }

        public boolean getCanScale() {
            return canScale;
        }

        public String getTip() {
            return tip;
        }

        public String getInputPath() {
            return inputPath;
        }

        public String getOutputPath() {
            return outputPath;
        }

        public void startForResult(Activity activity, int requestCode) {
            checkValues();
            Intent intent = new Intent(activity, ClipImageActivity.class);
            intent.putExtra("aspectX", aspectX);
            intent.putExtra("aspectY", aspectY);
            intent.putExtra("maxWidth", maxWidth);
            intent.putExtra("tip", tip);
            intent.putExtra("inputPath", inputPath);
            intent.putExtra("outputPath", outputPath);
            intent.putExtra("canScale", canScale);
            activity.startActivityForResult(intent, requestCode);
        }

        private void checkValues() {
            if (TextUtils.isEmpty(inputPath)) {
                throw new IllegalArgumentException("The input path could not be empty");
            }
            if (TextUtils.isEmpty(outputPath)) {
                throw new IllegalArgumentException("The output path could not be empty");
            }
        }

        public static ClipOptions createFromBundle(Intent intent) {
            return new ClipOptions().aspectX(intent.getIntExtra("aspectX", 1))
                    .aspectY(intent.getIntExtra("aspectY", 1))
                    .maxWidth(intent.getIntExtra("maxWidth", 0)).tip(intent.getStringExtra("tip"))
                    .inputPath(intent.getStringExtra("inputPath"))
                    .outputPath(intent.getStringExtra("outputPath"))
                    .canScale(intent.getBooleanExtra("canScale", false));
        }
    }
}
