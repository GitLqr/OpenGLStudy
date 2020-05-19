package com.kevin.opengles.chapter7.programs;

import android.content.Context;
import android.opengl.GLES20;

import com.lqr.openglstudy.R;

/**
 * @author LQR
 * @time 2020/4/3
 * @desc 颜色着色器程序
 */
public class ColorShaderProgram extends ShaderProgram {

    private final int uMatrixLocation;

    // Attribute locations
    private final int aPositionLocation;
    private final int aColorLocation;

    public ColorShaderProgram(Context context) {
        super(context, R.raw.simple_vertex_shader_chapter5, R.raw.simple_fragment_shader_chapter5);
        // Retrieve uniform locations for the shader program.
        uMatrixLocation = GLES20.glGetUniformLocation(mProgram, U_MATRIX);
        // Retrieve attribute locations for the shader program.
        aPositionLocation = GLES20.glGetAttribLocation(mProgram, A_POSITION);
        aColorLocation = GLES20.glGetAttribLocation(mProgram, A_COLOR);
    }

    public void setUniforms(float[] matrix) {
        // Pass the matrix into the shader program.
        GLES20.glUniformMatrix4fv(uMatrixLocation, 1, false, matrix, 0);
    }

    public int getPositionAttributeLocation() {
        return aPositionLocation;
    }

    public int getColorAttributeLocation() {
        return aColorLocation;
    }
}
