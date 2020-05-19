package com.kevin.opengles.util;

/**
 * @author LQR
 * @time 2020/4/1
 * @desc 矩阵帮助类
 * <p>
 * frustumM()有缺陷，会影响某些类型的投影。
 * perspectiveM()没有缺陷，但从Android ICS版本开始引入。
 */
public class MatrixHelper {

    public static void perspectiveM(float[] m, float yFovInDegrees, float aspect, float n, float f) {
        // 视野：度 转 弧度
        final float angleInRadians = (float) (yFovInDegrees * Math.PI / 180.0);
        // 计算焦距
        final float a = (float) (1.0 / Math.tan(angleInRadians / 2.0));
        // OpenGL把矩阵数据按照以列为主的顺序存储，所以我们需要一列一列写数据
        // 第一列
        m[0] = a / aspect;
        m[1] = 0f;
        m[2] = 0f;
        m[3] = 0f;
        // 第二列
        m[4] = 0f;
        m[5] = a;
        m[6] = 0f;
        m[7] = 0f;
        // 第三列
        m[8] = 0f;
        m[9] = 0f;
        m[10] = -((f + n) / (f - n));
        m[11] = -1f;
        // 第四列
        m[12] = 0f;
        m[13] = 0f;
        m[14] = -((2f * f * n) / (f - n));
        m[15] = 0f;
    }
}
