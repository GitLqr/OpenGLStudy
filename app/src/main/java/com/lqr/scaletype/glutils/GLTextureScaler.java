package com.lqr.scaletype.glutils;

import android.opengl.Matrix;

/**
 * @author LQR
 * @time 2020/5/19
 * @desc 渲染器 纹理规模转换器
 * <p>
 * ScaleType算法参考：
 * https://www.jianshu.com/p/fe5d2e3feed3
 */
public class GLTextureScaler {

    /**
     * 规模类型
     */
    public static class ScaleType {
        public static final int MATRIX = 0x001;
        public static final int FIT_XY = 0x002;
        public static final int FIT_START = 0x003;
        public static final int FIT_CENTER = 0x004;
        public static final int FIT_END = 0x005;
        public static final int CENTER = 0x006;
        public static final int CENTER_INSIDE = 0x007;
        public static final int CENTER_CROP = 0x008;
        public static final int CENTER_CROP_HORIZONTAL = 0x009;
        public static final int CENTER_CROP_VERTICAL = 0x010;
    }

    public int mScaleType; // 规模类型
    public float mSrcWidth; // 源宽度（即：相机预览宽度）
    public float mSrcHeight; // 源高度（即：相机预览高度）
    public float mDstWidth; // 目标宽度（即：预览窗口宽度）
    public float mDstHeight; // 目标高度（即：预览窗口高度）
    public float[] mMatrix; // 用于ScaleType.MATRIX模式下的矩阵转换

    public GLTextureScaler(int scaleType, float srcWidth, float srcHeight, float dstWidth, float dstHeight, float[] matrix) {
        mScaleType = scaleType;
        mSrcWidth = srcWidth;
        mSrcHeight = srcHeight;
        mDstWidth = dstWidth;
        mDstHeight = dstHeight;
        mMatrix = matrix;
    }

    public float[] invoke(float[] tex_matrix) {
        switch (mScaleType) {
            case ScaleType.MATRIX:
                return matrix(tex_matrix, mMatrix);
            case ScaleType.FIT_XY:
                return fitXY(tex_matrix, mSrcWidth, mSrcHeight, mDstWidth, mDstHeight);
            case ScaleType.FIT_START:
                return fitStart(tex_matrix, mSrcWidth, mSrcHeight, mDstWidth, mDstHeight);
            case ScaleType.FIT_CENTER:
                return fitCenter(tex_matrix, mSrcWidth, mSrcHeight, mDstWidth, mDstHeight);
            case ScaleType.FIT_END:
                return fitEnd(tex_matrix, mSrcWidth, mSrcHeight, mDstWidth, mDstHeight);
            case ScaleType.CENTER:
                return center(tex_matrix, mSrcWidth, mSrcHeight, mDstWidth, mDstHeight);
            case ScaleType.CENTER_INSIDE:
                return centerInside(tex_matrix, mSrcWidth, mSrcHeight, mDstWidth, mDstHeight);
            case ScaleType.CENTER_CROP:
                return centerCrop(tex_matrix, mSrcWidth, mSrcHeight, mDstWidth, mDstHeight);
            case ScaleType.CENTER_CROP_HORIZONTAL:
                return centerCropHorizontal(tex_matrix, mSrcWidth, mSrcHeight, mDstWidth, mDstHeight);
            case ScaleType.CENTER_CROP_VERTICAL:
                return centerCropVertical(tex_matrix, mSrcWidth, mSrcHeight, mDstWidth, mDstHeight);
            default:
                return tex_matrix;
        }
    }

    /**
     * 由外部指定 转换矩阵
     */
    private float[] matrix(float[] tex_matrix, float[] matrix) {
        if (matrix != null) {
            Matrix.multiplyMV(tex_matrix, 0, tex_matrix, 0, matrix, 0);
        }
        return tex_matrix;
    }

    /**
     * 不按比例缩放图片，目标是把图片塞满整个View。
     */
    private float[] fitXY(float[] tex_matrix, float srcWidth, float srcHeight, float dstWidth, float dstHeight) {
        // 不处理，默认就是占满宽高
        return tex_matrix;
    }

    /**
     * 把图片按比例扩大/缩小到View的宽度，置于顶部
     */
    private float[] fitStart(float[] tex_matrix, float srcWidth, float srcHeight, float dstWidth, float dstHeight) {
        return opera4FitScaleType(ScaleType.FIT_START, tex_matrix, srcWidth, srcHeight, dstWidth, dstHeight);
    }

    /**
     * 把图片按比例扩大/缩小到View的宽度，居中显示
     */
    private float[] fitCenter(float[] tex_matrix, float srcWidth, float srcHeight, float dstWidth, float dstHeight) {
        return opera4FitScaleType(ScaleType.FIT_CENTER, tex_matrix, srcWidth, srcHeight, dstWidth, dstHeight);
    }

    /**
     * 把图片按比例扩大/缩小到View的宽度，置于底部
     */
    private float[] fitEnd(float[] tex_matrix, float srcWidth, float srcHeight, float dstWidth, float dstHeight) {
        return opera4FitScaleType(ScaleType.FIT_END, tex_matrix, srcWidth, srcHeight, dstWidth, dstHeight);
    }

    /**
     * 按图片的原来size居中显示，当图片长/宽超过View的长/宽，则截取图片的居中部分显示
     */
    private float[] center(float[] tex_matrix, float srcWidth, float srcHeight, float dstWidth, float dstHeight) {
        Matrix.scaleM(tex_matrix, 0, dstWidth / srcWidth, dstHeight / srcHeight, 1);
        Matrix.translateM(tex_matrix, 0, (srcWidth - dstWidth) * 0.5f / srcWidth, (srcHeight - dstHeight) * 0.5f / srcHeight, 0);
        return tex_matrix;
    }

    /**
     * 将图片的内容完整居中显示，通过按比例缩小或原来的size使得图片长/宽等于或小于View的长/宽
     */
    private float[] centerInside(float[] tex_matrix, float srcWidth, float srcHeight, float dstWidth, float dstHeight) {
        if (srcWidth < dstWidth && srcHeight < dstHeight) {
            commonOpera(tex_matrix, srcWidth, srcHeight, dstWidth, dstHeight,
                    dstWidth / srcWidth, dstHeight / srcHeight,
                    0.5f, 0.5f);
        } else if (dstHeight / srcHeight < dstWidth / srcWidth) {
            // 以高度为准
            // float scale = srcHeight / dstHeight;
            // commonOpera(tex_matrix, srcWidth, srcHeight, dstWidth, dstHeight,
            //         scale, scale, 0.5f, 0.5f);
            centerCropHorizontal(tex_matrix, srcWidth, srcHeight, dstWidth, dstHeight);
        } else {
            // 以宽度为准
            // float scale = srcWidth / dstWidth;
            // commonOpera(tex_matrix, srcWidth, srcHeight, dstWidth, dstHeight,
            //         scale, scale, 0.5f, 0.5f);
            centerCropVertical(tex_matrix, srcWidth, srcHeight, dstWidth, dstHeight);
        }
        return tex_matrix;
    }

    /**
     * 按比例扩大图片的size居中显示，使得图片长(宽)等于或大于View的长(宽)
     */
    private float[] centerCrop(float[] tex_matrix, float srcWidth, float srcHeight, float dstWidth, float dstHeight) {
        if (dstHeight / srcHeight < dstWidth / srcWidth) {
            // 以dst宽度为准
            float scale = srcWidth / dstWidth;
            commonOpera(tex_matrix, srcWidth, srcHeight, dstWidth, dstHeight,
                    scale, scale, 0.5f, 0.5f);
        } else {
            // 以dst高度为准
            float scale = srcHeight / dstHeight;
            commonOpera(tex_matrix, srcWidth, srcHeight, dstWidth, dstHeight,
                    scale, scale, 0.5f, 0.5f);
        }
        return tex_matrix;
    }

    /**
     * 裁剪显示图像水平中间区域，竖直不变
     */
    private float[] centerCropHorizontal(float[] tex_matrix, float srcWidth, float srcHeight, float dstWidth, float dstHeight) {
        float scale = srcHeight / dstHeight;
        commonOpera(tex_matrix, srcWidth, srcHeight, dstWidth, dstHeight,
                scale, scale, 0.5f, 0.5f);
        return tex_matrix;
    }

    /**
     * 裁剪显示图像竖直中间区域，水平不变
     */
    private float[] centerCropVertical(float[] tex_matrix, float srcWidth, float srcHeight, float dstWidth, float dstHeight) {
        float scale = srcWidth / dstWidth;
        commonOpera(tex_matrix, srcWidth, srcHeight, dstWidth, dstHeight,
                scale, scale, 0.5f, 0.5f);
        return tex_matrix;
    }

    private float[] opera4FitScaleType(int fitScaleType, float[] tex_matrix, float srcWidth, float srcHeight, float dstWidth, float dstHeight) {
        if (dstHeight > dstWidth) {
            // 宽度为准
            float scale = srcWidth / dstWidth;
            switch (fitScaleType) {
                case ScaleType.FIT_START:
                    commonOpera(tex_matrix, srcWidth, srcHeight, dstWidth, dstHeight,
                            scale, scale, 0, 1);
                    break;
                case ScaleType.FIT_CENTER:
                    commonOpera(tex_matrix, srcWidth, srcHeight, dstWidth, dstHeight,
                            scale, scale, 0, 0.5f);
                    break;
                case ScaleType.FIT_END:
                    commonOpera(tex_matrix, srcWidth, srcHeight, dstWidth, dstHeight,
                            scale, scale, 0, 0);
                    break;
            }
        } else {
            // 以高度为准
            float scale = srcHeight / dstHeight;
            switch (fitScaleType) {
                case ScaleType.FIT_START:
                    commonOpera(tex_matrix, srcWidth, srcHeight, dstWidth, dstHeight,
                            scale, scale, 0, 0);
                    break;
                case ScaleType.FIT_CENTER:
                    commonOpera(tex_matrix, srcWidth, srcHeight, dstWidth, dstHeight,
                            scale, scale, 0, 0.5f);
                    break;
                case ScaleType.FIT_END:
                    commonOpera(tex_matrix, srcWidth, srcHeight, dstWidth, dstHeight,
                            scale, scale, 0, 1);
                    break;
            }
        }
        return tex_matrix;
    }

    private void commonOpera(float[] tex_matrix, float srcWidth, float srcHeight, float dstWidth, float dstHeight,
                             float dstWidthScale, float dstHeightScale, float translateFactorX, float translateFactorY) {
        float dstWidthIdeally = dstWidth * dstWidthScale;
        float dstHeightIdeally = dstHeight * dstHeightScale;
        float scaleX = dstWidthIdeally / srcWidth;
        float scaleY = dstHeightIdeally / srcHeight;
        Matrix.scaleM(tex_matrix, 0, scaleX, scaleY, 1);
        Matrix.translateM(tex_matrix, 0,
                -(dstWidthIdeally - srcWidth) * translateFactorX / dstWidthIdeally,
                -(dstHeightIdeally - srcHeight) * translateFactorY / dstHeightIdeally,
                0);
    }
}
