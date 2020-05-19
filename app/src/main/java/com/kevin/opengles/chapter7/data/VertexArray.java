package com.kevin.opengles.chapter7.data;

import android.opengl.GLES20;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

/**
 * @author LQR
 * @time 2020/4/2
 * @desc 顶点数组类
 */
public class VertexArray {

    private final FloatBuffer mFloatBuffer;

    public VertexArray(float[] vertexData) {
        mFloatBuffer = ByteBuffer
                .allocateDirect(vertexData.length * Constants.BYTES_PER_FLOAT)
                .order(ByteOrder.nativeOrder())
                .asFloatBuffer()
                .put(vertexData);
    }

    public void setVertexAttribPointer(int dateOffset, int attributeLocation, int componentCount, int stride) {
        mFloatBuffer.position(dateOffset);
        GLES20.glVertexAttribPointer(attributeLocation, componentCount, GLES20.GL_FLOAT, false, stride, mFloatBuffer);
        GLES20.glEnableVertexAttribArray(attributeLocation);
        mFloatBuffer.position(0);
    }
}
