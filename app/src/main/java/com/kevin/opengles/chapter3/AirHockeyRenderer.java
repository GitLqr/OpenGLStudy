package com.kevin.opengles.chapter3;

import android.content.Context;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;

import com.kevin.opengles.util.LoggerConfig;
import com.kevin.opengles.util.ShaderHelper;
import com.kevin.opengles.util.TextResourceReader;
import com.lqr.openglstudy.R;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

/**
 * @author LQR
 * @time 2020/3/24
 * @desc 空气曲棍球渲染器
 * <p>
 * 绘制点、直线、三角形（纯色）
 */
public class AirHockeyRenderer implements GLSurfaceView.Renderer {

    private static final int POSITION_COMPONENT_COUNT = 2;
    private static final int BYTES_PER_FLOAT = 4;
    private final FloatBuffer mVertexData;
    private final Context mContext;
    private int mProgram;
    private static final String U_COLOR = "u_Color";
    private int uColorLocation;
    private static final String A_POAITION = "a_Position";
    private int aPositionLocation;

    public AirHockeyRenderer(Context context) {
        mContext = context;
        float[] tableVertices = {
                0f, 0f,
                0f, 14f,
                9f, 14f,
                9f, 0f
        };
        float[] tableVerticesWithTriangles = {
                // Triangle 1
                -0.5f, -0.5f,// <---- 0f, 0f,
                0.5f, 0.5f,// <---- 9f, 14f,
                -0.5f, 0.5f,// <---- 0f, 14f,
                // Triangle 2
                -0.5f, -0.5f,// <---- 0f, 0f,
                0.5f, -0.5f,// <---- 9f, 0f,
                0.5f, 0.5f,// <---- 9f, 14f,
                // Line 1
                -0.5f, 0f,// <---- 0f, 7f,
                0.5f, 0f,// <---- 9f, 7f,
                // Mallets
                0f, -0.25f,// <---- 4.5f, 2f,
                0f, 0.25f// <---- 4.5f, 12f
        };
        mVertexData = ByteBuffer
                .allocateDirect(tableVerticesWithTriangles.length * BYTES_PER_FLOAT)
                .order(ByteOrder.nativeOrder())
                .asFloatBuffer();
        mVertexData.put(tableVerticesWithTriangles);
    }

    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        // 清屏
        GLES20.glClearColor(0.0f, 0.0f, 0.0f, 0.0f);
        // 创建着色器
        String vertexShaderSource = TextResourceReader.readTextFileFromResource(mContext, R.raw.simple_vertex_shader_chapter3);
        String fragmentShaderSource = TextResourceReader.readTextFileFromResource(mContext, R.raw.simple_fragment_shader_chapter3);
        int vertexShader = ShaderHelper.compileVertexShader(vertexShaderSource);
        int fragmentShader = ShaderHelper.compileFragmentShader(fragmentShaderSource);
        // 创建并链接程序
        mProgram = ShaderHelper.linkProgram(vertexShader, fragmentShader);
        if (LoggerConfig.ON) {
            ShaderHelper.validateProgram(mProgram);
        }
        // 使用OpenGL程序
        GLES20.glUseProgram(mProgram);
        // 获取Uniform位置
        uColorLocation = GLES20.glGetUniformLocation(mProgram, U_COLOR);
        // 获取属性位置
        aPositionLocation = GLES20.glGetAttribLocation(mProgram, A_POAITION);
        // 关联属性与顶点数据的数组
        mVertexData.position(0);
        GLES20.glVertexAttribPointer(aPositionLocation, POSITION_COMPONENT_COUNT, GLES20.GL_FLOAT, false, 0, mVertexData);
        // 使能顶点数组
        GLES20.glEnableVertexAttribArray(aPositionLocation);
    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {

    }

    @Override
    public void onDrawFrame(GL10 gl) {
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);
        // 绘制桌面（面）
        GLES20.glUniform4f(uColorLocation, 1.0f, 1.0f, 1.0f, 1.0f);
        GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, 6);
        // 绘制分隔线（线）
        GLES20.glUniform4f(uColorLocation, 1.0f, 0.0f, 0.0f, 1.0f);
        GLES20.glDrawArrays(GLES20.GL_LINES, 6, 2);
        // 绘制木槌（点）
        GLES20.glUniform4f(uColorLocation, 0.0f, 0.0f, 1.0f, 1.0f);
        GLES20.glDrawArrays(GLES20.GL_POINTS, 8, 1);
        GLES20.glUniform4f(uColorLocation, 10f, 0.0f, 0.0f, 1.0f);
        GLES20.glDrawArrays(GLES20.GL_POINTS, 9, 1);
    }
}
