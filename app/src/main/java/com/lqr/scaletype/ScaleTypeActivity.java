package com.lqr.scaletype;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.lqr.openglstudy.R;
import com.lqr.scaletype.drawer.BitmapDrawer;
import com.lqr.scaletype.drawer.IDrawer;
import com.lqr.scaletype.glutils.GLTextureScaler;

/**
 * @author LQR
 * @time 2020/5/19
 * @desc OpenGL实现ImageView#ScaleType显示功能
 */
public class ScaleTypeActivity extends AppCompatActivity {

    private GLSurfaceView mGlsvScaleType;
    private IDrawer mDrawer;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scale_type);
        mGlsvScaleType = findViewById(R.id.glsv_scale_type);
        mGlsvScaleType.setEGLContextClientVersion(2);

        Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.mipmap.naruto);
        mDrawer = new BitmapDrawer(bitmap, GLTextureScaler.ScaleType.CENTER_INSIDE);
//        mDrawer = new BitmapDrawer(bitmap);
        mGlsvScaleType.setRenderer(new ScaleTypeRender(mDrawer));
//        bitmap.recycle();
//        bitmap = null;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mDrawer != null) {
            mDrawer.release();
        }
    }
}
