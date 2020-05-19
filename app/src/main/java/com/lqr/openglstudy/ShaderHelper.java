package com.lqr.openglstudy;

import android.opengl.GLES20;
import android.util.Log;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

/**
 * @author LQR
 * @time 2020/3/20
 * @desc 着色器帮助类
 */
public class ShaderHelper {

    private static final String TAG = "ShaderHelper";
    private static final int BYTES_PER_FLOAT = 4; // Float类型占4Byte

    /**
     * 创建OpenGL程序对象
     *
     * @param vertexShader   顶点着色器代码
     * @param fragmentShader 片段着色器代码
     */
    protected void makeProgram(String vertexShader, String fragmentShader) {
        // 1. 编译顶点着色器
        int vertexShaderId = ShaderHelper.compileVertexShader(vertexShader);
        // 2. 编译片段着色器
        int fragmentShaderId = ShaderHelper.compileFragmentShader(fragmentShader);
        // 3. 将顶点着色器、片段着色器进行链接，组装成一个OpenGL程序
        int program = ShaderHelper.linkProgram(vertexShaderId, fragmentShaderId);
        ShaderHelper.validateProgram(program);
        // 4. 通知OpenGL开始使用该程序
        GLES20.glUseProgram(program);
    }

    /**
     * 编译顶点着色器
     *
     * @param shaderCode 编译代码
     * @return 着色器对象ID
     */
    public static int compileVertexShader(String shaderCode) {
        return compileShader(GLES20.GL_VERTEX_SHADER, shaderCode);
    }

    /**
     * 编译片段着色器
     *
     * @param shaderCode 编译代码
     * @return 着色器对象ID
     */
    public static int compileFragmentShader(String shaderCode) {
        return compileShader(GLES20.GL_FRAGMENT_SHADER, shaderCode);
    }

    /**
     * 编译着色器
     *
     * @param type       着色器类型
     * @param shaderCode 编译代码
     * @return 着色器对象ID
     */
    private static int compileShader(int type, String shaderCode) {
        // 1. 创建一个新的着色器对象
        final int shaderObjectId = GLES20.glCreateShader(type);
        // 2. 判断着色器创建状态
        if (shaderObjectId == 0) {
            return 0;
        }
        // 3. 将着色器代码上传到着色器对象中
        GLES20.glShaderSource(shaderObjectId, shaderCode);
        // 4. 编译着色器对象
        GLES20.glCompileShader(shaderObjectId);
        // 5. 获取编译状态：OpenGL将想要获取的值放入长度为1的数组的首位
        int[] compileStatus = new int[1];
        GLES20.glGetShaderiv(shaderObjectId, GLES20.GL_COMPILE_STATUS, compileStatus, 0);
        // 获取着色器信息日志
        // GLES20.glGetShaderInfoLog(shaderObjectId);
        // 6. 验证编译状态
        if (compileStatus[0] == 0) {
            GLES20.glDeleteShader(shaderObjectId);
            // 7. 返回着色器对象：失败，为0
            return 0;
        }
        // 7. 返回着色器对象：成功，非0
        return shaderObjectId;
    }

    public static int linkProgram(int vertexShaderId, int fragmentShaderId) {
        // 1. 创建一个OpenGL程序对象
        int programObjectId = GLES20.glCreateProgram();
        // 2.获取创建状态
        if (programObjectId == 0) {
            return 0;
        }
        // 3. 将顶点着色器依附到OpenGL程序对象
        GLES20.glAttachShader(programObjectId, vertexShaderId);
        GLES20.glAttachShader(programObjectId, fragmentShaderId);
        // 4. 将两个着色器链接到OpenGL程序对象
        GLES20.glLinkProgram(programObjectId);
        // 5. 获取链接状态：OpenGL将想要获取的值放入长度为1的数组的首位
        int[] linkStatus = new int[1];
        GLES20.glGetProgramiv(programObjectId, GLES20.GL_LINK_STATUS, linkStatus, 0);
        // 打印链接信息
        // GLES20.glGetProgramInfoLog(programObjectId);
        // 6. 验证链接状态
        if (linkStatus[0] == 0) {
            // 链接失败删除程序对象
            GLES20.glDeleteProgram(programObjectId);
            // 返回程序对象：失败，为0
            return 0;
        }
        // 7. 返回程序对象：成功，非0
        return programObjectId;
    }

    /**
     * 验证OpenGL程序对象状态
     *
     * @param programObjectId OpenGL程序ID
     * @return 是否可用
     */
    public static boolean validateProgram(int programObjectId) {
        GLES20.glValidateProgram(programObjectId);
        final int[] validateStatus = new int[1];
        GLES20.glGetProgramiv(programObjectId, GLES20.GL_VALIDATE_STATUS, validateStatus, 0);
        Log.v(TAG, "Results of validating program: " + validateStatus[0] + "\nLog:" + GLES20.glGetProgramInfoLog(programObjectId));
        return validateStatus[0] != 0;
    }

    public static FloatBuffer createFloatBuffer(float[] array) {
        FloatBuffer buffer = ByteBuffer
                .allocateDirect(array.length * BYTES_PER_FLOAT)
                .order(ByteOrder.nativeOrder())
                .asFloatBuffer();
        buffer.put(array);
        buffer.position(0);
        return buffer;
    }
}
