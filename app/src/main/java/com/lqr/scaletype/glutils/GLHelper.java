package com.lqr.scaletype.glutils;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.opengl.GLES20;
import android.opengl.GLUtils;
import android.os.Build;
import android.util.Log;

/**
 * @author LQR
 * @time 2020/5/19
 * @desc OpenGL帮助类
 */
public class GLHelper {

    private static final String TAG = "GLHelper";

    private GLHelper() {
    }

    /**
     * 检查OpenGL错误
     *
     * @param op
     */
    public static void checkGLError(String op) {
        int error = GLES20.glGetError();
        if (error != 0) {
            String msg = op + ": glError 0x" + Integer.toHexString(error);
            Log.e(TAG, msg);
            new Throwable(msg).printStackTrace();
        }
    }

    /**
     * 初始化纹理并返回纹理id
     *
     * @param texTarget   纹理目标
     * @param filterParam 纹理过滤模式
     * @return
     */
    public static int initTex(int texTarget, int filterParam) {
        return initTex(texTarget, GLES20.GL_TEXTURE0, filterParam, filterParam, GLES20.GL_CLAMP_TO_EDGE);
    }

    /**
     * 初始化纹理并返回纹理id
     *
     * @param texTarget 纹理目标
     * @param texUnit   纹理单元
     * @param minFilter 纹理过滤模式
     * @param magFilter 纹理过滤模式
     * @param wrap      纹理环绕方式
     * @return
     */
    public static int initTex(int texTarget, int texUnit, int minFilter, int magFilter, int wrap) {
        int[] tex = new int[1];
        GLES20.glActiveTexture(texUnit);
        GLES20.glGenTextures(1, tex, 0);
        GLES20.glBindTexture(texTarget, tex[0]);
        GLES20.glTexParameteri(texTarget, GLES20.GL_TEXTURE_WRAP_S, wrap);
        GLES20.glTexParameteri(texTarget, GLES20.GL_TEXTURE_WRAP_T, wrap);
        GLES20.glTexParameteri(texTarget, GLES20.GL_TEXTURE_MIN_FILTER, minFilter);
        GLES20.glTexParameteri(texTarget, GLES20.GL_TEXTURE_MAG_FILTER, magFilter);
        return tex[0];
    }

    /**
     * 删除纹理
     *
     * @param texId 纹理id
     */
    public static void deleteTex(int texId) {
        int[] tex = new int[]{texId};
        GLES20.glDeleteTextures(1, tex, 0);
    }

    /**
     * 删除纹理
     *
     * @param texIds 纹理id数组
     */
    public static void deleteTex(int[] texIds) {
        GLES20.glDeleteTextures(texIds.length, texIds, 0);
    }

    /**
     * 从资源中加载纹理
     *
     * @param context
     * @param resId
     * @return
     */
    public static int loadTextureFromResource(Context context, int resId) {
        return loadTextureFromResource(context, resId, null);
    }

    /**
     * 从资源中加载纹理
     *
     * @param context
     * @param resId
     * @param theme
     * @return
     */
    public static int loadTextureFromResource(Context context, int resId, Resources.Theme theme) {
        Bitmap bitmap = Bitmap.createBitmap(256, 256, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        canvas.drawARGB(0, 0, 255, 0);
        Drawable background;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            background = context.getResources().getDrawable(resId, theme);
        } else {
            background = context.getResources().getDrawable(resId);
        }
        background.setBounds(0, 0, 256, 256);
        background.draw(canvas);
        int[] textures = new int[1];
        GLES20.glGenTextures(1, textures, 0);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textures[0]);
        GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_NEAREST);
        GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);
        GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_REPEAT);
        GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_REPEAT);
        GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, bitmap, 0);
        bitmap.recycle();
        return textures[0];
    }

    public static int createTextureWithTextContext(String text) {
        Bitmap bitmap = Bitmap.createBitmap(256, 256, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        canvas.drawARGB(0, 0, 255, 0);
        Paint paint = new Paint();
        paint.setTextSize(32.0f);
        paint.setAntiAlias(true);
        paint.setARGB(255, 255, 255, 255);
        canvas.drawText(text, 16.0f, 112.0f, paint);
        int texture = initTex(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE0, GLES20.GL_NEAREST, GLES20.GL_LINEAR, GLES20.GL_REPEAT);
        GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, bitmap, 0);
        bitmap.recycle();
        return texture;
    }

    /**
     * 创建、编译Shader
     *
     * @param shaderType shader类型
     * @param source     shader源码
     * @return
     */
    public static int loadShader(int shaderType, String source) {
        int shader = GLES20.glCreateShader(shaderType);
        checkGLError("glCreateShader type=" + shaderType);
        GLES20.glShaderSource(shader, source);
        GLES20.glCompileShader(shader);
        int[] compiled = new int[1];
        GLES20.glGetShaderiv(shader, GLES20.GL_COMPILE_STATUS, compiled, 0);
        if (compiled[0] == 0) {
            Log.e(TAG, "Could not compile shader " + shaderType + ":");
            Log.e(TAG, " " + GLES20.glGetShaderInfoLog(shader));
            GLES20.glDeleteShader(shader);
            shader = 0;
        }
        return shader;
    }

    public static int loadShader(String vss, String fss) {
        int[] compiled = new int[1];
        int vs = loadShader(GLES20.GL_VERTEX_SHADER, vss);
        if (vs == 0) {
            return 0;
        } else {
            int fs = loadShader(GLES20.GL_FRAGMENT_SHADER, fss);
            if (fs == 0) {
                return 0;
            } else {
                int program = GLES20.glCreateProgram();
                checkGLError("glCreateProgram");
                if (program == 0) {
                    Log.e(TAG, "Could not create program");
                }
                GLES20.glAttachShader(program, vs);
                checkGLError("glAttachShader");
                GLES20.glAttachShader(program, fs);
                checkGLError("glAttachShader");
                GLES20.glLinkProgram(program);
                int[] linkStatus = new int[1];
                GLES20.glGetProgramiv(program, GLES20.GL_LINK_STATUS, linkStatus, 0);
                if (linkStatus[0] != 1) {
                    Log.e(TAG, "Could not link program: ");
                    Log.e(TAG, GLES20.glGetProgramInfoLog(program));
                    return 0;
                } else {
                    return program;
                }
            }
        }
    }
}
