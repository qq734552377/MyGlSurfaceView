package com.ucast.myglsurfaceview.rect;

import android.opengl.GLES20;
import android.opengl.Matrix;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

/**
 * Created by huangwei on 2015/6/8.
 */
public class PicGLRender {

    protected static final String VERTEX_SHADER = "" +
            "attribute vec4 position;\n" +
            "uniform mat4 uRotateMatrix;\n" +
            "attribute vec4 inputTextureCoordinate;\n" +
            " \n" +
            "varying vec2 textureCoordinate;\n" +
            " \n" +
            "void main()\n" +
            "{\n" +
            "    gl_Position = uRotateMatrix*position;\n" +
            "    textureCoordinate = inputTextureCoordinate.xy;\n" +
            "}";
    protected static final String FRAGMENT_SHADER = "" +
            "varying highp vec2 textureCoordinate;\n" +
            " \n" +
            "uniform sampler2D inputImageTexture;\n" +
            " \n" +
            "void main()\n" +
            "{\n" +
            "     gl_FragColor = texture2D(inputImageTexture, textureCoordinate);\n" +
            "}";

    static float COORD1[] = {
            -0.1f, -0.1f,
            0.1f, -0.1f,
            -0.1f, 0.1f,
            0.1f, 0.1f,
    };

    static final float TEXTURE_COORD1[] = {
            0.0f, 1.0f,
            1.0f, 1.0f,
            0.0f, 0.0f,
            1.0f, 0.0f,
    };

    static final float COORD2[] = {
            -1.0f, 1.0f,
            -1.0f, -1.0f,
            1.0f, 1.0f,
            1.0f, -1.0f,
    };

    static final float TEXTURE_COORD2[] = {
            0.0f, 0.0f,
            0.0f, 1.0f,
            1.0f, 0.0f,
            1.0f, 1.0f,
    };

    static final float COORD3[] = {
            1.0f, -1.0f,
            1.0f, 1.0f,
            -1.0f, -1.0f,
            -1.0f, 1.0f,
    };

    static final float TEXTURE_COORD3[] = {
            1.0f, 1.0f,
            1.0f, 0.0f,
            0.0f, 1.0f,
            0.0f, 0.0f,
    };


    static final float COORD4[] = {
            1.0f, -1.0f,
            1.0f, 1.0f,
            -1.0f, -1.0f,
            -1.0f, 1.0f,
    };

    static final float TEXTURE_COORD4[] = {
            1.0f, 1.0f,
            1.0f, 0.0f,
            0.0f, 1.0f,
            0.0f, 0.0f,
    };

    static final float COORD_REVERSE[] = {
            1.0f, -1.0f,
            1.0f, 1.0f,
            -1.0f, -1.0f,
            -1.0f, 1.0f,
    };

    static final float TEXTURE_COORD_REVERSE[] = {
            1.0f, 0.0f,
            1.0f, 1.0f,
            0.0f, 0.0f,
            0.0f, 1.0f,
    };

    static final float COORD_FLIP[] = {
            1.0f, -1.0f,
            1.0f, 1.0f,
            -1.0f, -1.0f,
            -1.0f, 1.0f,
    };

    static final float TEXTURE_COORD_FLIP[] = {
            0.0f, 1.0f,
            0.0f, 0.0f,
            1.0f, 1.0f,
            1.0f, 0.0f,
    };

    private String mVertexShader;
    private String mFragmentShader;

    private FloatBuffer mCubeBuffer;
    private FloatBuffer mTextureCubeBuffer;

    protected int mProgId;
    protected int mAttribPosition;
    protected int mAttribTexCoord;
    protected int mUniformTexture;
    private int mHRotateMatrix;
    private float[] mRotateMatrix=new float[16];
    private float[] mViewMatrix=new float[16];
    private float[] mProjectMatrix=new float[16];
    private float[] tempViewAndProMult=new float[16];
    private float[] tempAll=new float[16];
    private float[] rectSize = COORD1;

    public PicGLRender() {
        this(VERTEX_SHADER, FRAGMENT_SHADER);
    }


    public PicGLRender(float[] matrix) {
        this(VERTEX_SHADER, FRAGMENT_SHADER);
        System.arraycopy(matrix,0,this.mRotateMatrix,0,16);
    }

    public PicGLRender(float[] matrix,float[] rectSize) {
        this(VERTEX_SHADER, FRAGMENT_SHADER);
        System.arraycopy(matrix,0,this.mRotateMatrix,0,matrix.length);
        System.arraycopy(rectSize,0,this.rectSize,0,rectSize.length);
    }

    public PicGLRender(String vertexShader, String fragmentShader) {
        mVertexShader = vertexShader;
        mFragmentShader = fragmentShader;
    }

    public void init() {
        //设置相机位置
        Matrix.setLookAtM(mViewMatrix, 0, 0, 0, -3, 0f, 0f, 0f, 0f, 0.1f, 0.0f);
        //初始化模型矩阵
//        Matrix.setIdentityM(mRotateMatrix,0);
        loadVertex();
        initShader();
        GLES20.glBlendFunc(GLES20.GL_ONE, GLES20.GL_ONE_MINUS_SRC_ALPHA);
    }

    public void onsurfaceChange(int width,int height){
        //计算宽高比
        float ratio = (float) width / height;
        //透视投影矩阵/视锥
        MatrixHelper.perspectiveM(mProjectMatrix,0,45,ratio,3f,7);
//        Matrix.frustumM(mProjectMatrix, 0, -ratio, ratio, -1, 1, 3, 7);
    }

    public void loadVertex() {

        float[] texture_coord = TEXTURE_COORD1;

        mCubeBuffer = ByteBuffer.allocateDirect(this.rectSize.length * 4)
                .order(ByteOrder.nativeOrder())
                .asFloatBuffer();
        mCubeBuffer.put(this.rectSize).position(0);

        mTextureCubeBuffer = ByteBuffer.allocateDirect(texture_coord.length * 4)
                .order(ByteOrder.nativeOrder())
                .asFloatBuffer();
        mTextureCubeBuffer.put(texture_coord).position(0);
    }

    public void initShader() {
        mProgId = GLHelper.loadProgram(mVertexShader, mFragmentShader);
        mAttribPosition = GLES20.glGetAttribLocation(mProgId, "position");
        mUniformTexture = GLES20.glGetUniformLocation(mProgId, "inputImageTexture");
        mAttribTexCoord = GLES20.glGetAttribLocation(mProgId, "inputTextureCoordinate");
        mHRotateMatrix=GLES20.glGetUniformLocation(mProgId,"uRotateMatrix");
    }

    public void drawFrame(int glTextureId) {
        if (!GLES20.glIsProgram(mProgId)) {
            initShader();
        }
        GLES20.glUseProgram(mProgId);
        if (mCubeBuffer == null)
            return;
        mCubeBuffer.position(0);
        GLES20.glVertexAttribPointer(mAttribPosition, 2, GLES20.GL_FLOAT, false, 0, mCubeBuffer);
        GLES20.glEnableVertexAttribArray(mAttribPosition);

        mTextureCubeBuffer.position(0);
        GLES20.glVertexAttribPointer(mAttribTexCoord, 2, GLES20.GL_FLOAT, false, 0,
                mTextureCubeBuffer);
        GLES20.glEnableVertexAttribArray(mAttribTexCoord);

        Matrix.multiplyMM(tempViewAndProMult, 0, mProjectMatrix, 0, mViewMatrix, 0);
        Matrix.multiplyMM(tempAll, 0, tempViewAndProMult, 0, mRotateMatrix, 0);
        GLES20.glUniformMatrix4fv(mHRotateMatrix,1,false,mRotateMatrix,0);

        if (glTextureId != GLHelper.NO_TEXTURE) {
            GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, glTextureId);
            GLES20.glUniform1i(mUniformTexture, 0);
        }
        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4);

        GLES20.glDisableVertexAttribArray(mAttribPosition);
        GLES20.glDisableVertexAttribArray(mAttribTexCoord);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0);


        GLES20.glDisable(GLES20.GL_BLEND);

    }

    public void updateVertexValue(float x,float y){
        mCubeBuffer = null;
        float[] coord = new float[COORD1.length];
        for (int i = 0; i < COORD1.length / 2; i++) {
            coord[i * 2] = COORD1[i * 2] + x;
            coord[i * 2 + 1] = COORD1[i * 2 + 1] + y;
        }
        mCubeBuffer = ByteBuffer.allocateDirect(coord.length * 4)
                .order(ByteOrder.nativeOrder())
                .asFloatBuffer();
        mCubeBuffer.put(coord).position(0);
    }

    public void setMatrix(float[] matrix){
        System.arraycopy(matrix,0,mRotateMatrix,0,16);
    }

    public void updateMatrix(float[] matrix){
        float[] temp = new float[16];
        Matrix.multiplyMM(temp, 0, matrix, 0, mRotateMatrix, 0);
        System.arraycopy(temp,0,mRotateMatrix,0,16);
    }

}
