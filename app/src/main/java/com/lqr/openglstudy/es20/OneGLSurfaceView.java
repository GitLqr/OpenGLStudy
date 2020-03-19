package com.lqr.openglstudy.es20;

import android.content.Context;
import android.opengl.GLSurfaceView;

public class OneGLSurfaceView extends GLSurfaceView {

    private final OneGLRenderer mRenderer;

    public OneGLSurfaceView(Context context) {
        super(context);
        // Create an OpenGL ES 2.0 context
        setEGLContextClientVersion(2);
        mRenderer = new OneGLRenderer();
        // Set the Renderer for drawing on the GLSurfaceView
        setRenderer(mRenderer);
    }
}