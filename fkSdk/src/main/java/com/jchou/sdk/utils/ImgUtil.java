package com.jchou.sdk.utils;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.drawable.Drawable;
import android.media.MediaMetadataRetriever;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.renderscript.Allocation;
import android.renderscript.Element;
import android.renderscript.RenderScript;
import android.renderscript.ScriptIntrinsicBlur;
import android.support.v4.content.FileProvider;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;
import android.widget.Toast;



import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.HashMap;
import java.util.Hashtable;

public class ImgUtil {


    //图片高斯模糊
    public static Bitmap blurBitmap(Bitmap bitmap, Context context) {

        // 用需要创建高斯模糊bitmap创建一个空的bitmap
        Bitmap outBitmap = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getHeight(), Bitmap.Config.ARGB_8888);

        // 初始化Renderscript，这个类提供了RenderScript context，在创建其他RS类之前必须要先创建这个类，他控制RenderScript的初始化，资源管理，释放
        RenderScript rs = RenderScript.create(context);

        // 创建高斯模糊对象
        ScriptIntrinsicBlur blurScript = ScriptIntrinsicBlur.create(rs, Element.U8_4(rs));

        // 创建Allocations，此类是将数据传递给RenderScript内核的主要方法，并制定一个后备类型存储给定类型
        Allocation allIn = Allocation.createFromBitmap(rs, bitmap);
        Allocation allOut = Allocation.createFromBitmap(rs, outBitmap);

        // 设定模糊度
        blurScript.setRadius(25f);

        // Perform the Renderscript
        blurScript.setInput(allIn);
        blurScript.forEach(allOut);

        // Copy the final bitmap created by the out Allocation to the outBitmap
        allOut.copyTo(outBitmap);

        // recycle the original bitmap
        bitmap.recycle();

        // After finishing everything, we destroy the Renderscript.
        rs.destroy();

        return outBitmap;
    }

    public static File getTempPhoto(Context context) {
        File mCache = new File(Environment.getExternalStorageDirectory().getAbsolutePath() +
                "/Android/data/" + context.getPackageName() + "/cache");

        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            if (!mCache.exists()) {
                mCache.mkdirs();
            }
            File file = new File(mCache, System.currentTimeMillis() + ".jpg");
            try {
                file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return file;
        }
        return null;
    }

    public static Uri getUriForFile(Context context, File file) {
        if (context == null || file == null) {
            throw new NullPointerException();
        }
        Uri uri;
        if (Build.VERSION.SDK_INT >= 24) {
            uri = FileProvider.getUriForFile(context.getApplicationContext(), context.getApplicationContext().getPackageName() + ".fileProvider", file);
        } else {
            uri = Uri.fromFile(file);
        }
        return uri;
    }

    //计算图片的缩放值
    public static int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;
        if (height > reqHeight || width > reqWidth) {
            final int heightRatio = Math.round((float) height / (float) reqHeight);
            final int widthRatio = Math.round((float) width / (float) reqWidth);
            inSampleSize = heightRatio < widthRatio ? heightRatio : widthRatio;
        }
        return inSampleSize;
    }

    // 根据路径获得图片并压缩，返回bitmap用于显示
    public static Bitmap getSmallBitmap(String filePath) {
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(filePath, options);
        //Calculate inSampleSize
        options.inSampleSize = calculateInSampleSize(options, 480, 800);
        //Decode bitmap with inSampleSize set
        options.inJustDecodeBounds = false;
        return BitmapFactory.decodeFile(filePath, options);
    }

    // 将Bitmap转换成字符串
    public static String fileToString(File file) {
        String string = null;
        //将图片文件转化为字节数组字符串，并对其进行Base64编码处理
        InputStream in = null;
        byte[] data = null;
        //读取图片字节数组
        try {
            in = new FileInputStream(file);
            data = new byte[in.available()];
            in.read(data);
            in.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        string = Base64.encodeToString(data, Base64.DEFAULT);
        return string;
    }

    // 将字符串转换成Bitmap类型
    public static Bitmap stringToBitmap(String string) {
        Bitmap bitmap = null;
        try {
            byte[] bitmapArray;
            bitmapArray = Base64.decode(string, Base64.DEFAULT);
            bitmap = BitmapFactory.decodeByteArray(bitmapArray, 0, bitmapArray.length);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return bitmap;
    }

    static long time;

    //Bitmap对象保存为图片文件
    public static void saveBitmapFile(Bitmap bitmap,Activity ctx) {
        File tmpDir = new File(Environment.getExternalStorageDirectory(), "喵象");
        if (!tmpDir.exists()) {
            tmpDir.mkdir();
        }
//        if (time == 0) {
//            time = System.currentTimeMillis();
//        }
        String imgName = System.currentTimeMillis() + ".png";
        File img = new File(tmpDir, imgName);
        try {
            //创建
            img.createNewFile();
        } catch (IOException e) {

        }
        FileOutputStream fOut = null;
        try {
            fOut = new FileOutputStream(img);
            //原封不动的保存在内存卡上
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, fOut);
//            ToastUtils.showShortToast("保存成功");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } finally {
            if (fOut != null) {
                try {
                    fOut.flush();
                    fOut.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }


//        // 其次把文件插入到系统图库
//        try {
//            MediaStore.Images.Media.insertImage(BaseApplication.getContext().getContentResolver(),
//                    img.getAbsolutePath(), imgName, null);
//        } catch (FileNotFoundException e) {
//            e.printStackTrace();
//        }
        // 最后通知图库更新
        ctx.getApplication().sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE,
                Uri.parse("file://" + img.getPath())));
    }

    /**
     * @param urlString
     * @return Bitmap
     * 根据图片url获取图片对象
     */
    public static Bitmap getBitMBitmap(String urlString) {
        Bitmap bitmap;
        InputStream in = null;
        try {
            URL url = new URL(urlString);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(5000);
            connection.setReadTimeout(5000);
            in = new BufferedInputStream(connection.getInputStream());
            bitmap = BitmapFactory.decodeStream(in);
            connection.disconnect();
            in.close();
            return bitmap;
        } catch (Exception e) {
            Log.e("ImgUtil",e.toString());
            e.printStackTrace();
        }
        return null;
    }

    /**
     * @param urlpath
     * @return Bitmap
     * 根据url获取布局背景的对象
     */
    public static Drawable getDrawable(String urlpath) {
        Drawable d = null;
        try {
            URL url = new URL(urlpath);
            URLConnection conn = url.openConnection();
            conn.connect();
            InputStream in;
            in = conn.getInputStream();
            d = Drawable.createFromStream(in, "background.jpg");
            // TODO Auto-generated catch block
        } catch (IOException e) {
            e.printStackTrace();
        }
        return d;
    }

    public static Bitmap readBitMap(int resId,Activity ctx) {
        BitmapFactory.Options opt = new BitmapFactory.Options();
        opt.inPreferredConfig = Bitmap.Config.RGB_565;
        opt.inPurgeable = true;
        opt.inInputShareable = true;
        //  获取资源图片
        InputStream is = ctx.getApplication().getResources().openRawResource(resId);
        return BitmapFactory.decodeStream(is, null, opt);
    }

    public static String getRealPathFromUri(Context context, Uri contentUri) {
        Cursor cursor = null;
        try {
            String[] proj = {MediaStore.Images.Media.DATA};
            cursor = context.getContentResolver().query(contentUri, proj, null, null, null);
            int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            cursor.moveToFirst();
            return cursor.getString(column_index);
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }


    /**
     * 截取全屏
     *
     * @return
     */
    public static Bitmap captureScreenWindow(Activity context) {
        context.getWindow().getDecorView().setDrawingCacheEnabled(true);
        Bitmap bmp = context.getWindow().getDecorView().getDrawingCache();
        return bmp;
    }

    /**
     * 保存到内存卡
     *
     * @param bitName
     * @param mBitmap
     */
    public static void saveBitmapForSdCard(String bitName, Bitmap mBitmap,Activity ctx) {
        File tmpDir = new File(Environment.getExternalStorageDirectory(), ctx.getApplication().getPackageName());
        if (!tmpDir.exists()) {
            tmpDir.mkdir();
        }
        //创建file对象
//        String fileName = tmpDir.getAbsolutePath() + "/" + bitName + ".png";
        String imgName = bitName + ".png";
        File img = new File(tmpDir, imgName);
        try {
            //创建
            img.createNewFile();
        } catch (IOException e) {

        }
        FileOutputStream fOut = null;
        try {
            fOut = new FileOutputStream(img);
            //原封不动的保存在内存卡上
            mBitmap.compress(Bitmap.CompressFormat.PNG, 100, fOut);
            Toast.makeText(ctx.getApplication(),"保存成功",Toast.LENGTH_SHORT).show();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } finally {
            if (fOut != null) {
                try {
                    fOut.flush();
                    fOut.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }


        // 其次把文件插入到系统图库
        try {
            MediaStore.Images.Media.insertImage(ctx.getApplication().getContentResolver(),
                    img.getAbsolutePath(), imgName, null);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        // 最后通知图库更新
        ctx.getApplication().sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE,
                Uri.parse("file://" + img.getPath())));
    }


    /**
     * 对bitmap进行压缩处理
     *
     * @param source ：需要被处理的Bitmap
     * @param width  需要压缩成的宽度 必须为浮点型
     * @param height 需要压缩成的高度 必须为浮点型
     * @return 返回压缩后的Bitmap
     * 注意！必须提供参数2，3为浮点型。
     */
    public static Bitmap zoom(Bitmap source, float width, float height) {
        Matrix matrix = new Matrix();
        float scaleX = width / source.getWidth();
        float scaleY = height / source.getHeight();
        matrix.postScale(scaleX, scaleY);

        Bitmap bitmap = Bitmap.createBitmap(source, 0, 0, source.getWidth(), source.getHeight(), matrix, true);
        return bitmap;
    }


    public static Bitmap getVideoThumbnail(String url) {
        Bitmap bitmap = null;
        //MediaMetadataRetriever 是android中定义好的一个类，提供了统一
        //的接口，用于从输入的媒体文件中取得帧和元数据；
        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        try {
            //（）根据文件路径获取缩略图
            //retriever.setDataSource(filePath);
            HashMap<String, String> headers = null;
            if (headers == null) {
                headers = new HashMap<String, String>();
                headers.put("User-Agent", "Mozilla/5.0 (Linux; U; Android 4.4.2; zh-CN; MW-KW-001 Build/JRO03C) AppleWebKit/533.1 (KHTML, like Gecko) Version/4.0 UCBrowser/1.0.0.001 U4/0.8.0 Mobile Safari/533.1");
            }
            retriever.setDataSource(url, headers);
            //获得第一帧图片
            bitmap = retriever.getFrameAtTime();
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        } catch (RuntimeException e) {
            e.printStackTrace();
        } finally {
            try {
                retriever.release();
            } catch (RuntimeException e) {
                e.printStackTrace();
            }
        }
        return bitmap;
    }

    public static Bitmap getVideoFileThumbnail(String path) {

        MediaMetadataRetriever mmr = new MediaMetadataRetriever();//实例化MediaMetadataRetriever对象

        File file = new File(path);//实例化File对象，文件路径为/storage/sdcard/Movies/music1.mp4
        Bitmap bitmap = null;
        if (file.exists()) {
            mmr.setDataSource(file.getAbsolutePath());//设置数据源为该文件对象指定的绝对路径
            bitmap = mmr.getFrameAtTime();//获得视频第一帧的Bitmap对象
        }
        return bitmap;
    }


    public static byte[] bmpToByteArray(final Bitmap bmp, final boolean needRecycle) {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        bmp.compress(Bitmap.CompressFormat.PNG, 100, output);
        if (needRecycle) {
            bmp.recycle();
        }

        byte[] result = output.toByteArray();
        try {
            output.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    public static Bitmap createVideoThumbnail(String filePath, int kind) {
        Bitmap bitmap = null;
        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        try {
            if (filePath.startsWith("http://")
                    || filePath.startsWith("https://")
                    || filePath.startsWith("widevine://")) {
                retriever.setDataSource(filePath, new Hashtable<String, String>());
            } else {
                retriever.setDataSource(filePath);
            }
            bitmap = retriever.getFrameAtTime(0, MediaMetadataRetriever.OPTION_CLOSEST_SYNC); //retriever.getFrameAtTime(-1);
        } catch (IllegalArgumentException ex) {
            // Assume this is a corrupt video file
            ex.printStackTrace();
        } catch (RuntimeException ex) {
            // Assume this is a corrupt video file.
            ex.printStackTrace();
        } finally {
            try {
                retriever.release();
            } catch (RuntimeException ex) {
                // Ignore failures while cleaning up.
                ex.printStackTrace();
            }
        }

        if (bitmap == null) {
            return null;
        }

        if (kind == MediaStore.Images.Thumbnails.MINI_KIND) {//压缩图片 开始处
            // Scale down the bitmap if it's too large.
            int width = bitmap.getWidth();
            int height = bitmap.getHeight();
            int max = Math.max(width, height);
            if (max > 512) {
                float scale = 512f / max;
                int w = Math.round(scale * width);
                int h = Math.round(scale * height);
                bitmap = Bitmap.createScaledBitmap(bitmap, w, h, true);
            }//压缩图片 结束处
        } else if (kind == MediaStore.Images.Thumbnails.MICRO_KIND) {
            bitmap = ThumbnailUtils.extractThumbnail(bitmap,
                    96,
                    96,
                    ThumbnailUtils.OPTIONS_RECYCLE_INPUT);
        }
        return bitmap;
    }


}