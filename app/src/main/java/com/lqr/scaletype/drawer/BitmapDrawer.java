package com.lqr.scaletype.drawer;

import android.graphics.Bitmap;
import android.opengl.GLES20;
import android.opengl.GLUtils;
import android.opengl.Matrix;
import android.util.Log;

import com.lqr.scaletype.glutils.GLDrawer2D;
import com.lqr.scaletype.glutils.GLTextureScaler;

import java.util.Arrays;

/**
 * @author LQR
 * @time 2020/5/19
 * @desc 图片绘制器
 */
public class BitmapDrawer implements IDrawer {

    private GLDrawer2D mGlDrawer2D;
    private float[] mTexMatrix = new float[16];
    private int mTexId;
    private Bitmap mBitmap;
    private float mBitmapWidth;
    private float mBitmapHeight;
    private int mScaleType = -1;
    private GLTextureScaler mGLTextureScaler;

    public BitmapDrawer(Bitmap bitmap) {
        this(bitmap, -1);
    }

    public BitmapDrawer(Bitmap bitmap, int scaleType) {
        mBitmap = bitmap;
        mBitmapWidth = mBitmap.getWidth();
        mBitmapHeight = mBitmap.getHeight();
        mScaleType = scaleType;
    }

    @Override
    public void onDraw() {
        if (mGlDrawer2D == null) {
            mGlDrawer2D = new GLDrawer2D(false);
            mTexId = mGlDrawer2D.initTex();
            GLUtils.texImage2D(mGlDrawer2D.getTexTarget(), 0, mBitmap, 0);
        }
        if (mGlDrawer2D != null) {
            Matrix.setIdentityM(mTexMatrix, 0);
            if (getGLTextureScaler() != null) {
                mGLTextureScaler.invoke(mTexMatrix);
                // Log.e("lqr", Arrays.toString(mTexMatrix));
            }
            mGlDrawer2D.draw(mTexId, mTexMatrix, 0);
        }
    }

    @Override
    public void release() {
        if (mGlDrawer2D != null) {
            mGlDrawer2D.release();
        }
        if (mBitmap != null) {
            if (!mBitmap.isRecycled()) {
                mBitmap.recycle();
            }
            mBitmap = null;
        }
        mGLTextureScaler = null;
    }

    private GLTextureScaler getGLTextureScaler() {
        if (mScaleType != -1 && mGLTextureScaler == null) {
            int[] viewport = new int[4];
            GLES20.glGetIntegerv(GLES20.GL_VIEWPORT, viewport, 0);
            // Log.e("lqr", Arrays.toString(viewport));
            float srcWidth = mBitmapWidth;
            float srcHeight = mBitmapHeight;
            float dstWidth = viewport[2];
            float dstHeight = viewport[3];
            Log.e("lqr", "srcWidth=" + srcWidth + ", srcHeight=" + srcHeight + ", dstWidth=" + dstWidth + ", dstHeight=" + dstHeight);
            mGLTextureScaler = new GLTextureScaler(mScaleType, srcWidth, srcHeight, dstWidth, dstHeight, null);
        }
        return mGLTextureScaler;
    }
}
