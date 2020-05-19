package com.kevin.opengles.util;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.opengl.GLES20;
import android.opengl.GLUtils;
import android.util.Log;

/**
 * @author LQR
 * @time 2020/4/2
 * @desc 纹理帮助类
 * <p>
 * OpenGL纹理过滤模式：
 * GL_NEAREST                   最近邻过滤
 * GL_NEAREST_MIPMAP_NEAREST    使用MIP贴图的最近邻过滤
 * GL_NEAREST_MIPMAP_LINEAR     使用MIP贴图级别之间插值的最近邻过滤
 * GL_LINEAR                    双线性过滤
 * GL_LINEAR_MIPMAP_NEAREST     使用MIP贴图的双线性过滤
 * GL_LINEAR_MIPMAP_LIENAR      三线性过滤（使用MIP贴图级别之间插值的双线性过滤）
 * <p>
 * 每种情况下允许的纹理过滤模式
 * 缩小：
 * GL_NEAREST
 * GL_NEAREST_MIPMAP_NEAREST
 * GL_NEAREST_MIPMAP_LINEAR
 * GL_LINEAR
 * GL_LINEAR_MIPMAP_NEAREST
 * GL_LINEAR_MIPMAP_LIENAR
 * 放大：
 * GL_NEAREST
 * GL_LINEAR
 */
public class TextureHelper {

    private static final String TAG = "TextureHelper";

    public static int loadTexture(Context context, int resourceId) {
        final int[] textureObjectIds = new int[1];
        // 创建一个纹理
        GLES20.glGenTextures(1, textureObjectIds, 0);
        if (textureObjectIds[0] == 0) {
            if (LoggerConfig.ON) {
                Log.w(TAG, "Could not generate a new OpenGL texture object.");
            }
            return 0;
        }

        // 创建位图
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inScaled = false; // 告诉Android我们想要原始的图像数据，而不是这个图像的缩放版本
        final Bitmap bitmap = BitmapFactory.decodeResource(context.getResources(), resourceId, options);
        if (bitmap == null) {
            if (LoggerConfig.ON) {
                Log.w(TAG, "Resource ID " + resourceId + " could not be decoded.");
            }
            GLES20.glDeleteTextures(1, textureObjectIds, 0);
            return 0;
        }

        // 绑定纹理
        // 参数1：GL_TEXTURE_2D告诉OpenGL这应该被作为一个二维纹理对待。
        // 参数2：告诉OpenGL要绑定到哪个纹理对象的ID。
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureObjectIds[0]);

        // 纹理缩小：使用三线性过滤
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR_MIPMAP_LINEAR);
        // 纹理放大：使用双线性过滤
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);

        // 加载纹理到OpenGL
        GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, bitmap, 0);
        // 纹理加载进OpenGL之后，不需要持有Android的位图了
        bitmap.recycle();

        // 生成MIP贴图
        GLES20.glGenerateMipmap(GLES20.GL_TEXTURE_2D);

        // 解除与纹理的绑定
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D,0);

        return textureObjectIds[0];
    }
}
