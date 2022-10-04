package me.ag2s.lyraandroidtest;

import android.content.Context;
import android.content.res.AssetManager;
import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.channels.ReadableByteChannel;

public class IoUtils {


    public static void copyFile(String src, String dest) {
        try {
            copy(new FileInputStream(src), new FileOutputStream(dest));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    public static void copyFile(File src, File dest) {
        try {
            copy(new FileInputStream(src), new FileOutputStream(dest));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }


    private static void copy(FileInputStream fis, FileOutputStream fos) {
        FileChannel srcChannel = null;
        FileChannel dstChannel = null;
        try {
            srcChannel = fis.getChannel();
            dstChannel = fos.getChannel();
            srcChannel.transferTo(0, srcChannel.size(), dstChannel);

        } catch (Exception e) {
            e.printStackTrace();

        } finally {
            try {
                if (srcChannel != null) {
                    srcChannel.close();
                }
                if (dstChannel != null) {
                    dstChannel.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }


    }


    public static void copyAsset(Context context, String src, String dist) {
        AssetManager assets = context.getAssets();
        try {
            InputStream fis = assets.open(src);
            FileOutputStream fos = new FileOutputStream(dist);
            copy2(fis, fos);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }


    public static void copyAssets(Context context, String basedir, String distDir) {
        AssetManager assets = context.getAssets();
        try {
            String[] list = assets.list(basedir);
            File distDf = new File(distDir);
            if (!distDf.exists()) {
                distDf.mkdirs();
            }

            for (String s : list) {
                Log.e("SSS", s);
                if (!isDir(assets, basedir + "/" + s)) {
                    InputStream fis = assets.open(basedir + "/" + s);
                    FileOutputStream fos = new FileOutputStream(new File(distDir, s));
                    copy2(fis, fos);
                } else {
                    copyAssets(context, basedir + "/" + s, distDir + "/" + s);
                }

            }


        } catch (Exception e) {
            e.printStackTrace();
            Log.e("SSSS", Log.getStackTraceString(e));
        }
    }

    private static boolean isDir(AssetManager assetManager, String name) {
        try {
            return assetManager.list(name).length > 0;
        } catch (IOException e) {
            return false;
        }
    }


    private static boolean copy2(InputStream fin, FileOutputStream fos) {
        boolean result = false;

        ReadableByteChannel srcChannel = null;
        FileChannel dstChannel = null;
        int BufferFize = 1024 * 8;
        try {

            srcChannel = Channels.newChannel(fin);
            dstChannel = fos.getChannel();
            dstChannel.transferFrom(srcChannel, 0, Long.MAX_VALUE);
            result = true;
        } catch (Exception e) {
            e.printStackTrace();

        } finally {
            try {
                if (srcChannel != null) {
                    srcChannel.close();
                }
                if (dstChannel != null) {
                    dstChannel.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return result;
    }
}
