package com.lqr.scaletype;

import android.opengl.GLES20;
import android.opengl.GLSurfaceView;

import com.lqr.scaletype.drawer.IDrawer;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

/**
 * @author LQR
 * @time 2020/5/19
 * @desc ScaleType渲染器
 */
public class ScaleTypeRender implements GLSurfaceView.Renderer {

    private IDrawer mDrawer;

    public ScaleTypeRender(IDrawer drawer) {
        mDrawer = drawer;
    }

    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        GLES20.glClearColor(0f, 0f, 0f, 0f);
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);
    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        GLES20.glViewport(0, 0, width, height);
    }

    @Override
    public void onDrawFrame(GL10 gl) {
        if (mDrawer != null) {
            mDrawer.onDraw();
        }
    }

}
