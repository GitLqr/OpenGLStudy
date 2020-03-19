package com.lqr.openglstudy.es20;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;

/**
 * @author LQR
 * @time 2020/3/17
 * @desc 正方形
 */
public class Square {

    private FloatBuffer mVertexBuffer;
    private ShortBuffer mDrawListBuffer;

    // number of coordiantes per vertex in this array
    static final int COORDS_PER_VERTEX = 3;
    static float SQUARE_COORDS[] = {
            -0.5f, 0.5f, 0.0f, // top left
            -0.5f, -0.5f, 0.0f, // bottom left
            0.5f, -0.5f, 0.0f, // bottom right
            0.5f, 0.5f, 0.0f // top right
    };
    private short DRAW_ORDER[] = {0, 1, 2, 0, 2, 3}; // order to draw vertices

    public Square() {
        // 初始化ByteBuffer，长度为arr数组的长度*4，因为一个float占4个字节
        ByteBuffer bb = ByteBuffer.allocateDirect(SQUARE_COORDS.length * 4);
        bb.order(ByteOrder.nativeOrder());
        mVertexBuffer = bb.asFloatBuffer();
        mVertexBuffer.put(SQUARE_COORDS);
        mVertexBuffer.position(0);

        // 初始化ByteBuffer，长度为arr数组的长度*2，因为一个short占2个字节
        ByteBuffer d1b = ByteBuffer.allocateDirect(DRAW_ORDER.length * 2);
        d1b.order(ByteOrder.nativeOrder());
        mDrawListBuffer = d1b.asShortBuffer();
        mDrawListBuffer.put(DRAW_ORDER);
        mDrawListBuffer.position(0);
    }
}
