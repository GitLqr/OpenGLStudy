package com.lqr.openglstudy.camera;

import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.os.Bundle;
import android.view.TextureView;
import android.view.View;
import android.widget.Button;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.lqr.openglstudy.R;

import java.io.IOException;

/**
 * @author LQR
 * @time 2020/3/17
 * @desc 使用TextureView预览相机图像
 */
public class TextureViewCameraActivity extends AppCompatActivity implements TextureView.SurfaceTextureListener, View.OnClickListener {

    private TextureView mTvPreview;
    private Button mBtnChange;
    private Camera mCamera;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_textureview_camera);
        mTvPreview = findViewById(R.id.tv_preview);
        mTvPreview.setSurfaceTextureListener(this);
        mBtnChange = findViewById(R.id.btn_change);
        mBtnChange.setOnClickListener(this);
    }

    @Override
    public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
        try {
            mCamera = Camera.open(0);
            mCamera.setDisplayOrientation(90);
            mCamera.setPreviewTexture(surface);
            mCamera.startPreview();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {

    }

    @Override
    public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
        if (mCamera != null) {
            mCamera.stopPreview();
            mCamera.release();
            mCamera = null;
        }
        return false;
    }

    @Override
    public void onSurfaceTextureUpdated(SurfaceTexture surface) {

    }

    @Override
    public void onClick(View v) {
        PropertyValuesHolder translationX = PropertyValuesHolder.ofFloat("translationX", 0.0f, 0.0f);
        PropertyValuesHolder scaleX = PropertyValuesHolder.ofFloat("scaleX", 1.0f, 0.3f, 1.0f);
        PropertyValuesHolder scaleY = PropertyValuesHolder.ofFloat("scaleY", 1.0f, 0.3f, 1.0f);
        PropertyValuesHolder rotationX = PropertyValuesHolder.ofFloat("rotationX", 0.0f, 2 * 360.0f, 0.0f);
        PropertyValuesHolder rotationY = PropertyValuesHolder.ofFloat("rotationY", 0.0f, 2 * 360.0f, 0.0f);
        PropertyValuesHolder alpha = PropertyValuesHolder.ofFloat("alpha", 1.0f, 0.7f, 1.0f);
        ObjectAnimator objectAnimator = ObjectAnimator.ofPropertyValuesHolder(mTvPreview, translationX, scaleX, scaleY, rotationX, rotationY, alpha);
        objectAnimator.setDuration(5000).start();
    }
}
