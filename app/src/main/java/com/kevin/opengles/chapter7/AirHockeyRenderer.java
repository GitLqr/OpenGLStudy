package com.kevin.opengles.chapter7;

import android.content.Context;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;

import com.kevin.opengles.chapter7.objects.Mallet;
import com.kevin.opengles.chapter7.objects.Table;
import com.kevin.opengles.chapter7.programs.ColorShaderProgram;
import com.kevin.opengles.chapter7.programs.TextureShaderProgram;
import com.kevin.opengles.util.MatrixHelper;
import com.kevin.opengles.util.TextureHelper;
import com.lqr.openglstudy.R;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

/**
 * @author LQR
 * @time 2020/3/24
 * @desc 空气曲棍球渲染器
 * <p>
 * 使用 纹理 丰富桌面
 */
public class AirHockeyRenderer implements GLSurfaceView.Renderer {

    private final Context mContext;
    private final float[] projectionMatrix = new float[16];
    private final float[] modelMatrix = new float[16];
    private Table mTable;
    private Mallet mMallet;
    private TextureShaderProgram mTextureProgram;
    private ColorShaderProgram mColorProgram;
    private int mTexture;

    public AirHockeyRenderer(Context context) {
        mContext = context;
    }

    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        GLES20.glClearColor(0.0f, 0.0f, 0.0f, 0.0f);
        mTable = new Table();
        mMallet = new Mallet();
        mTextureProgram = new TextureShaderProgram(mContext);
        mColorProgram = new ColorShaderProgram(mContext);
        mTexture = TextureHelper.loadTexture(mContext, R.drawable.air_hockey_surface);
    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        GLES20.glViewport(0, 0, width, height);
        MatrixHelper.perspectiveM(projectionMatrix, 45, (float) width / (float) height, 1f, 10f);
        Matrix.setIdentityM(modelMatrix, 0);
        Matrix.translateM(modelMatrix, 0, 0, 0, -2.5f);
        Matrix.rotateM(modelMatrix, 0, -60f, 1f, 0f, 0f);
        final float[] temp = new float[16];
        Matrix.multiplyMM(temp, 0, projectionMatrix, 0, modelMatrix, 0);
        System.arraycopy(temp, 0, projectionMatrix, 0, temp.length);
    }

    @Override
    public void onDrawFrame(GL10 gl) {
        // Clear the rendering surface.
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);

        // Draw the table.
        mTextureProgram.useProgram();
        mTextureProgram.setUniforms(projectionMatrix, mTexture);
        mTable.bindData(mTextureProgram);
        mTable.draw();

        // Draw the mallets.
        mColorProgram.useProgram();
        mColorProgram.setUniforms(projectionMatrix);
        mMallet.bindData(mColorProgram);
        mMallet.draw();
    }
}
