package com.lqr.scaletype.glutils;

import android.opengl.GLES11Ext;
import android.opengl.GLES20;
import android.opengl.Matrix;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

/**
 * @author LQR
 * @time 2020/5/19
 * @desc OpenGL 2D绘制器
 */
public class GLDrawer2D {
    private static final String VERTICE_SHADER_SOURCE = "#version 100\n" +
            "uniform mat4 uMVPMatrix;\n" +
            "uniform mat4 uTexMatrix;\n" +
            "attribute highp vec4 aPosition;\n" +
            "attribute highp vec4 aTextureCoord;\n" +
            "varying highp vec2 vTextureCoord;\n" +
            "void main() {\n" +
            "    gl_Position = uMVPMatrix * aPosition;\n" +
            "    vTextureCoord = (uTexMatrix * aTextureCoord).xy;\n" +
            "}";
    private static final String FRAGMENT_SHADER_SOURCE = "#version 100\n" +
            "precision mediump float;\n" +
            "uniform sampler2D sTexture;\n" +
            "varying highp vec2 vTextureCoord;\n" +
            "void main() {\n" +
            "  gl_FragColor = texture2D(sTexture, vTextureCoord);\n" +
            "}";
    private static final String FRAGMENT_SHADER_SOURCE_OES = "#version 100\n" +
            "#extension GL_OES_EGL_image_external : require\n" +
            "precision mediump float;\n" +
            "uniform samplerExternalOES sTexture;\n" +
            "varying highp vec2 vTextureCoord;\n" +
            "void main() {\n" +
            "  gl_FragColor = texture2D(sTexture, vTextureCoord);\n" +
            "}";

    // 顶点坐标
    // OpenGL世界坐标范围是[-1, 1]。
    private static final float[] VERTICES = {-1f, -1f, 1f, -1f, -1f, 1f, 1f, 1f};
    //    private static final float[] VERTICES = {1.0F, 1.0F, -1.0F, 1.0F, 1.0F, -1.0F, -1.0F, -1.0F};
    // 纹理坐标
    // 1、纹理坐标范围是[0, 1]。
    // 2、纹理坐标系与安卓坐标系y轴方向相反。
    private static final float[] TEXCOORD = {0f, 1f, 1f, 1f, 0f, 0f, 1f, 0f};
    //    private static final float[] TEXCOORD = {1.0F, 1.0F, 0.0F, 1.0F, 1.0F, 0.0F, 0.0F, 0.0F};
    // float类型对应的字节大小
    private static final int FLOAT_SZ = 4;

    private final float[] mMvpMatrix;
    private final int mVertexNum;
    private final int mVertexSz;
    private final int mTexTarget;
    private final FloatBuffer mVertexBuffer;
    private final FloatBuffer mTexCoordBuffer;
    private int mProgram;
    private int maPositionLoc;
    private int maTextureCoordLoc;
    private int muMVPMatrixLoc;
    private int muTexMatrixLoc;

    public GLDrawer2D(boolean isOES) {
        this(VERTICES, TEXCOORD, isOES);
    }

    public GLDrawer2D(float[] vertices, float[] texcoord, boolean isOES) {
        // 初始化投影矩阵（默认为单位矩阵）
        this.mMvpMatrix = new float[16];
        Matrix.setIdentityM(this.mMvpMatrix, 0);
        // 计算顶点个数
        this.mVertexNum = Math.min(vertices != null ? vertices.length : 0, texcoord != null ? texcoord.length : 0) / 2;
        this.mVertexSz = mVertexNum * 2;
        // 纹理目标
        this.mTexTarget = isOES ? GLES11Ext.GL_TEXTURE_EXTERNAL_OES : GLES20.GL_TEXTURE_2D;
        // 顶点坐标数据
        this.mVertexBuffer = ByteBuffer.allocateDirect(this.mVertexSz * FLOAT_SZ).order(ByteOrder.nativeOrder()).asFloatBuffer();
        this.mVertexBuffer.put(vertices);
        this.mVertexBuffer.flip();
        // 纹理坐标数据
        this.mTexCoordBuffer = ByteBuffer.allocateDirect(this.mVertexSz * FLOAT_SZ).order(ByteOrder.nativeOrder()).asFloatBuffer();
        this.mTexCoordBuffer.put(texcoord);
        this.mTexCoordBuffer.flip();
        // 创建程序
        this.mProgram = GLHelper.loadShader(VERTICE_SHADER_SOURCE, isOES ? FRAGMENT_SHADER_SOURCE_OES : FRAGMENT_SHADER_SOURCE);
        this.init();
    }

    public void release() {
        if (this.mProgram >= 0) {
            GLES20.glDeleteProgram(mProgram);
        }
        this.mProgram = -1;
    }

    public boolean isOES() {
        return mTexTarget == GLES11Ext.GL_TEXTURE_EXTERNAL_OES;
    }

    public float[] getMvpMatrix() {
        return mMvpMatrix;
    }

    public GLDrawer2D setMvpMatrix(float[] matrix, int offset) {
        System.arraycopy(matrix, offset, this.mMvpMatrix, 0, 16);
        return this;
    }

    public void getMvpMatrix(float[] matrix, int offset) {
        System.arraycopy(this.mMvpMatrix, 0, matrix, offset, 16);
    }

    public int getTexTarget() {
        return mTexTarget;
    }

    public synchronized void draw(int texId, float[] tex_matrix, int offset) {
        if (this.mProgram >= 0) {
            GLES20.glUseProgram(this.mProgram);
            if (tex_matrix != null) {
                GLES20.glUniformMatrix4fv(this.muTexMatrixLoc, 1, false, tex_matrix, offset);
            }

            GLES20.glUniformMatrix4fv(this.muMVPMatrixLoc, 1, false, this.mMvpMatrix, 0);
            GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
            GLES20.glBindTexture(this.mTexTarget, texId);
            GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, this.mVertexNum);
            GLES20.glBindTexture(this.mTexTarget, 0);
            GLES20.glUseProgram(0);
        }
    }

    public int initTex() {
        return GLHelper.initTex(this.mTexTarget, GLES20.GL_NEAREST);
    }

    public void deleteTex(int texId) {
        GLHelper.deleteTex(texId);
    }

    public synchronized void updateShader(String vs, String fs) {
        this.release();
        this.mProgram = GLHelper.loadShader(vs, fs);
        this.init();
    }

    public void updateShader(String fs) {
        this.updateShader(VERTICE_SHADER_SOURCE, fs);
    }

    public void resetShader() {
        this.release();
        this.mProgram = GLHelper.loadShader(VERTICE_SHADER_SOURCE, isOES() ? FRAGMENT_SHADER_SOURCE_OES : FRAGMENT_SHADER_SOURCE);
        this.init();
    }

    private void init() {
        GLES20.glUseProgram(mProgram);
        // 获取shader中的属性
        this.maPositionLoc = GLES20.glGetAttribLocation(this.mProgram, "aPosition");
        this.maTextureCoordLoc = GLES20.glGetAttribLocation(this.mProgram, "aTextureCoord");
        this.muMVPMatrixLoc = GLES20.glGetUniformLocation(this.mProgram, "uMVPMatrix");
        this.muTexMatrixLoc = GLES20.glGetUniformLocation(this.mProgram, "uTexMatrix");
        // 初始化shader中的属性值
        float[] identityMatrix = new float[16];
        Matrix.setIdentityM(identityMatrix, 0);
        GLES20.glUniformMatrix4fv(this.muMVPMatrixLoc, 1, false, identityMatrix, 0);
        GLES20.glUniformMatrix4fv(this.muTexMatrixLoc, 1, false, identityMatrix, 0);
        GLES20.glVertexAttribPointer(this.maPositionLoc, 2, GLES20.GL_FLOAT, false, this.mVertexSz, this.mVertexBuffer);
        GLES20.glVertexAttribPointer(this.maTextureCoordLoc, 2, GLES20.GL_FLOAT, false, this.mVertexSz, this.mTexCoordBuffer);
        GLES20.glEnableVertexAttribArray(this.maPositionLoc);
        GLES20.glEnableVertexAttribArray(this.maTextureCoordLoc);
    }
}
