package com.lqr.openglstudy.camera;

import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.media.ThumbnailUtils;
import android.opengl.GLES11;
import android.opengl.GLES11Ext;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.GLUtils;
import android.opengl.Matrix;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.lqr.openglstudy.R;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

/**
 * @author LQR
 * @time 2020/3/17
 * @desc 使用GLSurfaceView预览相机图像
 */
public class GLSurfaceViewCameraActivity extends AppCompatActivity implements View.OnClickListener, SurfaceTexture.OnFrameAvailableListener {

    private Button mBtnAnimator;
    private Button mBtnSwitch;
    private GLSurfaceView mSvPreview;
    private MyRender mRender;
    private SurfaceTexture mSurfaceTexture;

    private Camera mCamera;
    private int mCameraStatus = Camera.CameraInfo.CAMERA_FACING_FRONT;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_glsurfaceview_camera);
        mBtnAnimator = findViewById(R.id.btn_animator);
        mBtnSwitch = findViewById(R.id.btn_switch);
        mBtnAnimator.setOnClickListener(this);
        mBtnSwitch.setOnClickListener(this);

        mSvPreview = findViewById(R.id.sv_preview);
        // 在setRenderer()方法前调用此方法
        mSvPreview.setEGLContextClientVersion(2);
        mRender = new MyRender();
        mSvPreview.setRenderer(mRender);
        mSvPreview.setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);
    }

    @Override
    public void onFrameAvailable(SurfaceTexture surfaceTexture) {
        mSvPreview.requestRender();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_animator:
                PropertyValuesHolder scaleX = PropertyValuesHolder.ofFloat("scaleX", 1.0f, 0.5f, 1.0f);
                PropertyValuesHolder scaleY = PropertyValuesHolder.ofFloat("scaleY", 1.0f, 0.5f, 1.0f);
                PropertyValuesHolder rotationY = PropertyValuesHolder.ofFloat("rotationY", 0.0f, 360.0f, 0.0F);
                ObjectAnimator objectAnimator = ObjectAnimator.ofPropertyValuesHolder(mSvPreview, scaleX, scaleY, rotationY);
                objectAnimator.setDuration(3000).start();
                break;
            case R.id.btn_switch:
                mCameraStatus ^= 1;
                if (mCamera != null) {
                    mCamera.stopPreview();
                    mCamera.release();
                }
                mRender.mBoolean = true;
                mCamera = Camera.open(mCameraStatus);
                try {
                    mCamera.setPreviewTexture(mSurfaceTexture);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                mCamera.startPreview();
                break;
        }
    }

    public class MyRender implements GLSurfaceView.Renderer {
        private final String vertexShaderCode = "uniform mat4 textureTransform;\n" +
                "attribute vec2 inputTextureCoordinate;\n" +
                "attribute vec4 position;            \n" +//NDK坐标点
                "varying   vec2 textureCoordinate; \n" +//纹理坐标点变换后输出
                "\n" +
                " void main() {\n" +
                "     gl_Position = position;\n" +
                "     textureCoordinate = inputTextureCoordinate;\n" +
                " }";
        private final String fragmentShaderCode = "#extension GL_OES_EGL_image_external : require\n" +
                "precision mediump float;\n" +
                "uniform samplerExternalOES videoTex;\n" +
                "varying vec2 textureCoordinate;\n" +
                "\n" +
                "void main() {\n" +
                "    vec4 tc = texture2D(videoTex, textureCoordinate);\n" +
                "    float color = tc.r * 0.3 + tc.g * 0.59 + tc.b  * 0.11;\n" +  //所有视图修改成黑白
                "    gl_FragColor = vec4(color,color,color,1.0);\n" +
//                "    gl_FragColor = vec4(tc.r,tc.g,tc.b,1.0);\n" +
                "}\n";
        private FloatBuffer mPosBuffer;
        private FloatBuffer mTexBuffer;
        private float[] mPosCoordinate = {-1, -1, -1, 1, 1, -1, 1, 1};
        private float[] mTexCoordinateBackRight = {1, 1, 0, 1, 1, 0, 0, 0}; // 顺时针转90并沿Y轴翻转后，摄像头正确，前摄像头上下颠倒
        private float[] mTexCoordinateFrontRight = {0, 1, 1, 1, 0, 0, 1, 0}; // 顺时针旋转90后，摄像头上下颠倒了，前摄像头正确
        public int mProgram;
        public boolean mBoolean = false;

        private int uPosHandle;
        private int aTexHandle;
        private int mMVPMatrixHandle;
        private float[] mProjectMatrix = new float[16];
        private float[] mCameraMatrix = new float[16];
        private float[] mMVPMatrix = new float[16];
        private float[] mTempMatrix = new float[16];

        public MyRender() {
            Matrix.setIdentityM(mProjectMatrix, 0);
            Matrix.setIdentityM(mCameraMatrix, 0);
            Matrix.setIdentityM(mMVPMatrix, 0);
            Matrix.setIdentityM(mTempMatrix, 0);
        }

        private int loadShader(int type, String shaderCode) {
            int shader = GLES20.glCreateShader(type);
            // 添加上面编写的着色器代码并编译它
            GLES20.glShaderSource(shader, shaderCode);
            GLES20.glCompileShader(shader);
            return shader;
        }

        private void createProgram() {
            // 通常做法
            // String vertexSource = AssetsUtils.read(CameraGlSurfaceShowActivity.this, "vertex_texture.glsl");
            // int vertexShader = loadShader(GLES20.GL_VERTEX_SHADER, vertexSource);
            // String fragmentSource = AssetsUtils.read(CameraGlSurfaceShowActivity.this, "fragment_texture.glsl");
            // int fragmentShader = loadShader(GLES20.GL_FRAGMENT_SHADER, fragmentSource);
            int vertexShader = loadShader(GLES20.GL_VERTEX_SHADER, vertexShaderCode);
            int fragmentShader = loadShader(GLES20.GL_FRAGMENT_SHADER, fragmentShaderCode);
            // 创建空的OpenGL ES程序
            mProgram = GLES20.glCreateProgram();
            // 添加顶点着色器到程序中
            GLES20.glAttachShader(mProgram, vertexShader);
            // 添加片段着色器到程序中
            GLES20.glAttachShader(mProgram, fragmentShader);
            // 创建OpenGL ES程序可执行文件
            GLES20.glLinkProgram(mProgram);
            // 释放shader资源
            GLES20.glDeleteShader(vertexShader);
            GLES20.glDeleteShader(fragmentShader);
        }

        private FloatBuffer convertToFloatBuffer(float[] buffer) {
            FloatBuffer fb = ByteBuffer.allocateDirect(buffer.length * 4)
                    .order(ByteOrder.nativeOrder())
                    .asFloatBuffer();
            fb.put(buffer);
            fb.position(0);
            return fb;
        }

        /**
         * 添加程序到ES环境中
         */
        private void activeProgram() {
            // 将程序添加到OpenGL ES环境
            GLES20.glUseProgram(mProgram);

            mSurfaceTexture.setOnFrameAvailableListener(GLSurfaceViewCameraActivity.this);
            // 获取顶点着色器的位置的句柄
            uPosHandle = GLES20.glGetAttribLocation(mProgram, "position");
            aTexHandle = GLES20.glGetAttribLocation(mProgram, "inputTextureCoordinate");
            mMVPMatrixHandle = GLES20.glGetUniformLocation(mProgram, "textureTransform");

            mPosBuffer = convertToFloatBuffer(mPosCoordinate);

            if (mCameraStatus == Camera.CameraInfo.CAMERA_FACING_BACK) {
                mTexBuffer = convertToFloatBuffer(mTexCoordinateBackRight);
            } else {
                mTexBuffer = convertToFloatBuffer(mTexCoordinateFrontRight);
            }

            GLES20.glVertexAttribPointer(uPosHandle, 2, GLES20.GL_FLOAT, false, 0, mPosBuffer);
            GLES20.glVertexAttribPointer(aTexHandle, 2, GLES20.GL_FLOAT, false, 0, mTexBuffer);

            // 启用顶点位置的句柄
            GLES20.glEnableVertexAttribArray(uPosHandle);
            GLES20.glEnableVertexAttribArray(aTexHandle);
        }

        @Override
        public void onSurfaceCreated(GL10 gl, EGLConfig config) {
            GLES20.glClearColor(0.0f, 0.0f, 0.0f, 0.0f);
            mSurfaceTexture = new SurfaceTexture(createOESTextureObject());
            createProgram();
            mCamera = Camera.open(mCameraStatus);
            try {
                mCamera.setPreviewTexture(mSurfaceTexture);
                mCamera.startPreview();
            } catch (IOException e) {
                e.printStackTrace();
            }
            activeProgram();
        }

        @Override
        public void onSurfaceChanged(GL10 gl, int width, int height) {
            GLES20.glViewport(0, 0, width, height);
            Matrix.scaleM(mMVPMatrix, 0, 1, -1, 1);
            float ratio = (float) width / height;
            Matrix.orthoM(mProjectMatrix, 0, -1, 1, -ratio, ratio, 1, 7); // 3和7代表远近视点与眼睛的距离，非坐标点
            Matrix.setLookAtM(mCameraMatrix, 0, 0, 0, 3, 0f, 0f, 0f, 0f, 1.0f, 0.0f); // 3代表眼睛的坐标点
            Matrix.multiplyMM(mMVPMatrix, 0, mProjectMatrix, 0, mCameraMatrix, 0);
        }

        @Override
        public void onDrawFrame(GL10 gl) {
            if (mBoolean) {
                activeProgram();
                mBoolean = false;
            }
            if (mSurfaceTexture != null) {
                GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);
                mSurfaceTexture.updateTexImage();
                GLES20.glUniformMatrix4fv(mMVPMatrixHandle, 1, false, mMVPMatrix, 0);
                GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, mPosCoordinate.length / 2);
            }
        }

    }

    /**
     * 如果是相机数据处理，我们使用 GLES11Ext.GLTEXTUREEXTERNALOES，
     * 如果是处理贴纸图片，我们使用 GLES20.GLTEXTURE2D。
     * 因为相机输出的数据类型是 YUV420P 格式的，
     * 使用 GLES11Ext.GLTEXTUREEXTERNALOES 扩展纹理可以实现自动将 YUV420P 转 RGB，
     * 我们就不需要在存储成 MP4 的时候再进行数据转换了。
     */
    public static int createOESTextureObject() {
        // 生成一个纹理
        int[] tex = new int[1];
        // 第一个参数表示创建几个纹理对象，并将创建好的纹理对象旋转到第二个参数中去，第二个参数里面存放的是纹理ID（纹理索引），第三个从我偏移值，通常填0即可。
        GLES20.glGenTextures(1, tex, 0);
        // 纹理绑定：将此纹理绑定到外部纹理上
        GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, tex[0]);
        // 设置纹理过滤参数
        // 设置缩小过滤方式为GL_LINEAR（双线性过滤，目前最主要的过滤方式），当然还有GL_NEAREST（容易出现锯齿效果）和MIP贴图（占用更多内存）
        GLES20.glTexParameterf(GLES11Ext.GL_TEXTURE_EXTERNAL_OES,
                GL10.GL_TEXTURE_MIN_FILTER, GL10.GL_NEAREST);
        // 设置放大过滤为GL_LINEAR，同上
        GLES20.glTexParameterf(GLES11Ext.GL_TEXTURE_EXTERNAL_OES,
                GL10.GL_TEXTURE_MAG_FILTER, GL10.GL_LINEAR);
        // 设置纹理的S方向范围，控制纹理贴纸的范围在（0,1）之内，大于1的设置为1，小于0的设置为0.
        GLES20.glTexParameterf(GLES11Ext.GL_TEXTURE_EXTERNAL_OES,
                GL10.GL_TEXTURE_WRAP_S, GL10.GL_CLAMP_TO_EDGE);
        // 设置纹理的T方向范围，同上
        GLES20.glTexParameterf(GLES11Ext.GL_TEXTURE_EXTERNAL_OES,
                GL10.GL_TEXTURE_WRAP_T, GL10.GL_CLAMP_TO_EDGE);
        // 解除纹理绑定
        GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, 0);
        return tex[0];
    }
}
