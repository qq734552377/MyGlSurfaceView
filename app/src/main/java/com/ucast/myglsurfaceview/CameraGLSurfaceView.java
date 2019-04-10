package com.ucast.myglsurfaceview;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;


import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.SurfaceTexture;
import android.opengl.GLES11Ext;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.GLSurfaceView.Renderer;
import android.opengl.Matrix;
import android.os.Environment;
import android.util.AttributeSet;
import android.util.Log;

import com.ucast.myglsurfaceview.beans.BoxMsg;
import com.ucast.myglsurfaceview.cameraInterface.CameraInterface;
import com.ucast.myglsurfaceview.rect.PicGLRender;
import com.ucast.myglsurfaceview.rect.GLHelper;
import com.ucast.myglsurfaceview.tools.BitCreateTools;
import com.ucast.myglsurfaceview.tools.MyTools;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CameraGLSurfaceView extends GLSurfaceView implements Renderer, SurfaceTexture.OnFrameAvailableListener {
    private static final String TAG = "yanzi";
    Context mContext;
    SurfaceTexture mSurface;
    int mTextureID = -1;
    DirectDrawer mDirectDrawer;
    public boolean isTakePahoto = false;

    //    MyRenderer myRenderer;
    private int mTextureId_RECT = GLHelper.NO_TEXTURE;
//    Bitmap bitmap = null;
//    PicGLRender picGLRender = null;

    private List<Integer> textureIDs = new ArrayList<>();


    ArrayList<PicGLRender> picGLRenders = new ArrayList<>();
    ArrayList<float[]> matixs = new ArrayList<>();
    ArrayList<Bitmap> bmps = new ArrayList<>();



    public CameraGLSurfaceView(Context context, AttributeSet attrs) {
        super(context, attrs);
        MyTools.copyCfg(context,"port1.jpg","port2.jpg","port3.jpg");
        // TODO Auto-generated constructor stub
        mContext = context;
        setEGLContextClientVersion(2);
        setRenderer(this);
        setRenderMode(RENDERMODE_WHEN_DIRTY);
//        try {
//            bitmap = BitmapFactory.decodeStream(context.getAssets().open("models/girl.jpg"));
//        } catch (IOException e) {
//            e.printStackTrace();
//        }

        initTakePhotoMsg();
//        initMyMsg();
        initMyPicMsg();

//        myRenderer = new MyRenderer();
    }
    public void initTakePhotoMsg(){
        if(TakephotoActivity.boxs.isEmpty())
            return;

        for(Map.Entry<String, BoxMsg> item : TakephotoActivity.boxs.entrySet()){
            BoxMsg one = item.getValue();
            Point p = one.getPointInScreen();
            if (p == null)
                continue;
            if (p.x==0 && p.y==0)
                continue;
            //显示的信息
            Bitmap oneBit = BitmapFactory.decodeFile(one.getShowPic());
            bmps.add(oneBit);

            float[] roateM = new float[16];
            Matrix.setIdentityM(roateM, 0);
            float[] pFloat = MyTools.getPicPosition(p);
            Matrix.translateM(roateM, 0, pFloat[0], pFloat[1], 0);
            float[] vertexRect = MyTools.getPicVertex(oneBit.getWidth(),oneBit.getHeight());
            PicGLRender picGLRender = new PicGLRender(roateM,vertexRect);
            picGLRenders.add(picGLRender);
        }
    }

    public void initMyMsg(){
        for (int i = 0; i < 6; i++) {
            int bgColor = Color.WHITE;
            switch (i%5){
                case 0:
                    bgColor = 0xFFFF0000;
                    break;
                case 1:
                    bgColor = 0xFF00FF00;
                    break;
                case 2:
                    bgColor = 0xFF0000FF;
                    break;
                case 3:
                    bgColor = 0xFF00FFFF;
                    break;
                case 4:
                    bgColor = 0xFFFF00FF;
                    break;
                case 5:
                    bgColor = 0xFFFFFF00;
                    break;
            }
            //显示的信息
            Bitmap oneBit = BitCreateTools.getBitMapByStringReturnBitmaPath("Number:" + i + 1
                            + "\nWeight:" + (i+15) +"0 kg"
                            + "\nCountry:" + (i+35) +" s",
                    bgColor
            );
            bmps.add(oneBit);

            float[] roateM = new float[16];
            Matrix.setIdentityM(roateM, 0);
            Matrix.translateM(roateM, 0, 0.6f - i * 0.2f, -0.6f + i * 0.2f, 0);
            float[] vertexRect = MyTools.getPicVertex(oneBit.getWidth(),oneBit.getHeight());
            PicGLRender picGLRender = new PicGLRender(roateM,vertexRect);
            picGLRenders.add(picGLRender);
        }
    }

    public void initMyPicMsg(){
        if(!TakephotoActivity.boxs.isEmpty())
            return;
        Map<String,BoxMsg> boxs = initData();
        int i = 1;
        for(Map.Entry<String, BoxMsg> item : boxs.entrySet()){
            BoxMsg one = item.getValue();
            Point p = new Point(100 + i * 100,100 + i * 100);
            i++;
            if (p == null)
                continue;
            if (p.x==0 && p.y==0)
                continue;
            //显示的信息
            Bitmap oneBit = BitmapFactory.decodeFile(one.getShowPic());
            bmps.add(oneBit);

            float[] roateM = new float[16];
            Matrix.setIdentityM(roateM, 0);
            float[] pFloat = MyTools.getPicPosition(p);
            Matrix.translateM(roateM, 0, pFloat[0], pFloat[1], 0);
            float[] vertexRect = MyTools.getPicVertex(oneBit.getWidth(),oneBit.getHeight());
            PicGLRender picGLRender = new PicGLRender(roateM,vertexRect);
            picGLRenders.add(picGLRender);
        }
    }

    public Map<String,BoxMsg> initData(){
        Map<String,BoxMsg> boxs = new HashMap<>();
        BoxMsg boxMsg1 = new BoxMsg();
        boxMsg1.setLedId("BA 0A");
        boxMsg1.setShowPic(Environment.getExternalStorageDirectory().getPath() + "/Ucast/port1.jpg");
        boxMsg1.setShowPic(true);
        BoxMsg boxMsg2 = new BoxMsg();
        boxMsg2.setLedId("BB 0A");
        boxMsg2.setShowPic(Environment.getExternalStorageDirectory().getPath() + "/Ucast/port2.jpg");
        boxMsg2.setShowPic(true);
        BoxMsg boxMsg3 = new BoxMsg();
        boxMsg3.setLedId("BC 0A");
        boxMsg3.setShowPic(Environment.getExternalStorageDirectory().getPath() + "/Ucast/port3.jpg");
        boxMsg3.setShowPic(true);

        boxs.put(boxMsg1.getLedId(),boxMsg1);
        boxs.put(boxMsg2.getLedId(),boxMsg2);
        boxs.put(boxMsg3.getLedId(),boxMsg3);
        return boxs;
    }
    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        // TODO Auto-generated method stub
        Log.i(TAG, "onSurfaceCreated...");
        MyTools.writeSimpleLogWithTime("GLView onSurfaceCreated");
        mTextureID = createTextureID();
        mSurface = new SurfaceTexture(mTextureID);
        mSurface.setOnFrameAvailableListener(this);
        mDirectDrawer = new DirectDrawer(mTextureID);
        CameraInterface.getInstance().doOpenCamera();
//        myRenderer.onSurfaceCreated();
//        mTextureId_RECT = GLHelper.loadTexture(bitmap, mTextureId_RECT);
        for (int i = 0; i < picGLRenders.size(); i++) {
            PicGLRender item = picGLRenders.get(i);
            item.init();
            int mTextureId_RECT = GLHelper.NO_TEXTURE;
            Bitmap oneTexture = bmps.get(i);
            mTextureId_RECT = GLHelper.loadTexture(oneTexture, mTextureId_RECT);
            textureIDs.add(mTextureId_RECT);
            oneTexture.recycle();
        }
        bmps.clear();
    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        // TODO Auto-generated method stub
        Log.i(TAG, "onSurfaceChanged...");
        GLES20.glViewport(0, 0, width, height);
        if (!CameraInterface.getInstance().isPreviewing()) {
            CameraInterface.getInstance().doStartPreview(mSurface);
        }
        for (int i = 0; i < picGLRenders.size(); i++) {
            PicGLRender picGLRender = picGLRenders.get(i);
            picGLRender.onsurfaceChange(width, height);
        }

    }

    @Override
    public void onDrawFrame(GL10 gl) {
        // TODO Auto-generated method stub
        Log.i(TAG, "onDrawFrame...");
        GLES20.glClearColor(0.1f, 0.1f, 0.1f, 0.1f);
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);
        mSurface.updateTexImage();
        float[] mtx = new float[16];
        mSurface.getTransformMatrix(mtx);

        mDirectDrawer.draw(mtx);
//        myRenderer.onDrawFrame();
        for (int i = 0; i < picGLRenders.size(); i++) {
            PicGLRender picGLRender = picGLRenders.get(i);
            picGLRender.drawFrame(textureIDs.get(i));
        }
        takePhoto();

    }

    public void takePhotoInt(){
        if (!isTakePahoto)
            return;
        IntBuffer outPutBuffer = IntBuffer.allocate(MainActivity.FRAMECALLBACKWIDTH *
                MainActivity.FRAMECALLBACKHEIGHT);
        outPutBuffer.position(0);
        GLES20.glReadPixels(0, 0, MainActivity.FRAMECALLBACKWIDTH, MainActivity.FRAMECALLBACKHEIGHT,
                GLES20.GL_RGBA, GLES20.GL_UNSIGNED_BYTE, outPutBuffer);

        outPutBuffer.position(0);
        int[] b = new int[MainActivity.FRAMECALLBACKWIDTH * MainActivity.FRAMECALLBACKHEIGHT];
        outPutBuffer.get(b);
        for (int i = 0; i < MainActivity.FRAMECALLBACKHEIGHT / 2; i++) {
            for (int j = 0; j < MainActivity.FRAMECALLBACKWIDTH; j++) {
                int first = i * MainActivity.FRAMECALLBACKWIDTH + j;
                int last = (MainActivity.FRAMECALLBACKHEIGHT - 1 - i) * MainActivity.FRAMECALLBACKWIDTH + j;
                int temp = b[first];
                b[first] = b[last];
                b[last] = temp;
            }
        }
        Bitmap bitmap = Bitmap.createBitmap(b, MainActivity.FRAMECALLBACKWIDTH, MainActivity.FRAMECALLBACKHEIGHT, Bitmap.Config.ARGB_8888);
        saveBitmap(bitmap);
        bitmap.recycle();

        isTakePahoto = false;

    }

    public void takePhoto() {
        if (!isTakePahoto)
            return;
        ByteBuffer outPutBuffer = ByteBuffer.allocate(MainActivity.FRAMECALLBACKWIDTH *
               MainActivity.FRAMECALLBACKHEIGHT * 4);
        outPutBuffer.position(0);
        GLES20.glReadPixels(0, 0, MainActivity.FRAMECALLBACKWIDTH, MainActivity.FRAMECALLBACKHEIGHT,
                GLES20.GL_RGBA, GLES20.GL_UNSIGNED_BYTE, outPutBuffer);

        outPutBuffer.position(0);
        byte[] b = outPutBuffer.array();
        int width = MainActivity.FRAMECALLBACKWIDTH * 4;
        for (int i = 0; i < MainActivity.FRAMECALLBACKHEIGHT / 2; i++) {
            for (int j = 0; j < width; j++) {
                    int first = i * width + j ;
                    int last = (MainActivity.FRAMECALLBACKHEIGHT - 1 - i) * width + j ;
                    byte temp = b[first];
                    b[first] = b[last];
                    b[last] = temp;
            }
        }

        Bitmap bitmap = Bitmap.createBitmap(MainActivity.FRAMECALLBACKWIDTH, MainActivity.FRAMECALLBACKHEIGHT, Bitmap.Config.ARGB_8888);

        bitmap.copyPixelsFromBuffer(outPutBuffer);
        saveBitmap(bitmap);
        bitmap.recycle();

        isTakePahoto = false;

    }

    public int getARGB(int rgba) {
        int argb = (rgba >> 24) + (rgba >> 16 & 0XFF) << 8 + (rgba >> 8 & 0xFF) << 16 + (rgba & 0XFF) << 24;
        return argb;
    }

    //图片保存
    public void saveBitmap(Bitmap b) {
        String path = Environment.getExternalStorageDirectory().toString() + "/Ucast/photo";

        File folder = new File(path);
        if (!folder.exists()) {
            boolean isOk = folder.mkdirs();
            if (!isOk)
                return;
        }
        long dataTake = System.currentTimeMillis();
        final String jpegName = path + "/" + dataTake + ".jpg";
        try {
            FileOutputStream fout = new FileOutputStream(jpegName);
            BufferedOutputStream bos = new BufferedOutputStream(fout);
            b.compress(Bitmap.CompressFormat.JPEG, 100, bos);
            bos.flush();
            bos.close();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }


    @Override
    public void onPause() {
        super.onPause();
        CameraInterface.getInstance().doStopCamera();
    }

    private int createTextureID() {
        int[] texture = new int[1];

        GLES20.glGenTextures(1, texture, 0);
        GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, texture[0]);
        GLES20.glTexParameterf(GLES11Ext.GL_TEXTURE_EXTERNAL_OES,
                GL10.GL_TEXTURE_MIN_FILTER, GL10.GL_LINEAR);
        GLES20.glTexParameterf(GLES11Ext.GL_TEXTURE_EXTERNAL_OES,
                GL10.GL_TEXTURE_MAG_FILTER, GL10.GL_LINEAR);
        GLES20.glTexParameteri(GLES11Ext.GL_TEXTURE_EXTERNAL_OES,
                GL10.GL_TEXTURE_WRAP_S, GL10.GL_CLAMP_TO_EDGE);
        GLES20.glTexParameteri(GLES11Ext.GL_TEXTURE_EXTERNAL_OES,
                GL10.GL_TEXTURE_WRAP_T, GL10.GL_CLAMP_TO_EDGE);

        return texture[0];
    }

    public SurfaceTexture _getSurfaceTexture() {
        return mSurface;
    }

    @Override
    public void onFrameAvailable(SurfaceTexture surfaceTexture) {
        // TODO Auto-generated method stub
        Log.i(TAG, "onFrameAvailable...");
        this.requestRender();
    }

//    public void updateFilterPosition(float x, float y) {
//        if (picGLRender != null) {
//            picGLRender.updateVertexValue(x, y);
//        }
//    }

    public void setMatrix(float[] matrix) {
        for (int i = 0; i < picGLRenders.size(); i++) {
            PicGLRender picGLRender = picGLRenders.get(i);
            picGLRender.setMatrix(matrix);
        }
    }
    public void updateMatrix(float[] matrix) {
        for (int i = 0; i < picGLRenders.size(); i++) {
            PicGLRender picGLRender = picGLRenders.get(i);
            picGLRender.updateMatrix(matrix);
        }
    }

}
