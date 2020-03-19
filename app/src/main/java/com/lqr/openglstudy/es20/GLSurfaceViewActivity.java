package com.lqr.openglstudy.es20;

import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;


/**
 * @author LQR
 * @time 2020/3/17
 * @desc GLSurfaceView Demo
 * <p>
 * Java的缓冲区数据存储结构为大端字节序(BigEdian)，而OpenGl的数据为小端字节序（LittleEdian）
 */
public class GLSurfaceViewActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        OneGLSurfaceView glSurfaceView = new OneGLSurfaceView(this);
        setContentView(glSurfaceView);
    }


}
