package com.ucast.myglsurfaceview.tools;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.os.Environment;

import com.ucast.myglsurfaceview.exception.CrashHandler;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Created by pj on 2019/3/8.
 */
public class BitCreateTools {
    public static int WIDTH = 250;
    public final static int FONT_SIZE = 36 ;
    private static int LINE_STRING_NUMBER = WIDTH / ( FONT_SIZE / 2) ;
    public final static int SMALL_LINE_HEIGHT = FONT_SIZE + 2 ;
    private final static int OFFSET_X = 6;
    public static String TEMPBITPATH = CrashHandler.ALBUM_PATH + "/temp";
    public final static String BASEDIR
            = Environment.getExternalStorageDirectory().toString();

    public static Bitmap getBitMapByStringReturnBitmaPath(String string,int bgColor) {
        int width = (WIDTH + 7) / 8 * 8;
        LINE_STRING_NUMBER = (width - OFFSET_X) / ( FONT_SIZE / 2)  ;
        int firstEnterIndex = string.indexOf("\n");
        if (firstEnterIndex != -1 && firstEnterIndex + 1 < width / 12 ) {  //小于一行的空格数据全部忽略
            if (string.substring(0,firstEnterIndex).replace(" ","").equals(""))
                string = string.substring(firstEnterIndex + 1, string.length());
        }
        List<String> list = getLineStringDatas(string);
        int Height = list.size() * SMALL_LINE_HEIGHT;
        Bitmap bmp = Bitmap.createBitmap(width, Height + 8 , Bitmap.Config.RGB_565);
        Canvas canvas = new Canvas(bmp);
        canvas.drawColor(bgColor);
        Paint print = new Paint();
        print.setColor(Color.WHITE);
        print.setTextSize(FONT_SIZE);
        print.setTypeface(Typeface.DEFAULT_BOLD);
        int offsetY =  4;
        for (int i = 0; i < list.size(); i++) {
            canvas.drawText(list.get(i), OFFSET_X, i * SMALL_LINE_HEIGHT + SMALL_LINE_HEIGHT - offsetY, print);
        }
//        canvas.save(0x1F);
//        canvas.restore();

//        File f = new File(TEMPBITPATH);
//        if (!f.exists())
//            f.mkdir();
//        String bmpPath = TEMPBITPATH + File.separator + "ucast_bit_and_string_" + UUID.randomUUID().toString().replace("-", "")+"_2552" + ".bmp";
//        saveBmpUse1Bit(bmp,bmpPath);

//        canvas = null;
//        if (bmp != null && !bmp.isRecycled()){
//            bmp.recycle();
//            bmp = null;
//        }

        return bmp;
    }

    public static void saveBitmapAsPng(Bitmap bmp,File f) {
        try {
            FileOutputStream out = new FileOutputStream(f);
            bmp.compress(Bitmap.CompressFormat.PNG, 100, out);
            out.flush();
            out.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    /**
     * 通过字符串获取分行的数据
     *
     * */
    public static List<String> getLineStringDatas(String string){
        string = string.replace("\r","");
//        String[] dataString = null;
//        dataString = string.split("\n");

        List<String> src = new ArrayList<>();
        int offset = 0;
        int index = string.indexOf("\n",offset);
        while (index != -1){
            src.add(string.substring(offset,index));
            offset = index + 1;
            index = string.indexOf("\n",offset);
        }

        if (offset < string.length()){
            src.add(string.substring(offset));
        }

        List<String> list = new ArrayList<>();
        List<String> splistlist;
        for (int i = 0; i < src.size(); i++) {
            String stringData = src.get(i);
            byte[] one =null;
            try {
                one = stringData.getBytes("GB18030");
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
            int len = one == null ? stringData.getBytes().length : one.length;
            if ( len > LINE_STRING_NUMBER) {
                splistlist = splitString(stringData);
                for (int t = 0; t < splistlist.size(); t++) {
                    list.add(splistlist.get(t));
                }
            } else {
                list.add(stringData);
            }
        }

        return list;
    }

    /**
     * 拆分字符串
     *
     * @param data
     * @return
     */
    public static List<String> splitString(String data) {
        List<String> list = new ArrayList<>();
        String string = "";
        int offert = 0;
        for (int i = 0; i < data.length(); i++) {
            String s = data.substring(i, i + 1);
            if (s.getBytes().length > 1) {
                string += s;
                offert = offert + 2;
            } else {
                string += s;
                offert++;
            }
            if (offert >= LINE_STRING_NUMBER) {
                list.add(string);
                string = "";
                offert = 0;
            }
        }
        list.add(string);
        return list;
    }

    /**
     * 保存为bmp图片 位图深度为1
     *
     * @param bitmap
     * @return 生成bmp的绝对路径
     */
    public static String saveBmpUse1Bit(Bitmap bitmap,String outPath){
        if (bitmap == null)
            return null;
        int w = bitmap.getWidth();
        int h = bitmap.getHeight();
        byte[] datas = getSaveOnebitBmpData(bitmap);

        int line_byte_num = w / 8;
        int saveBmpHeight = h;
        int saveBmpWidth = ((w + 31)/32) * 32;
        int bufferSize =  saveBmpHeight * saveBmpWidth / 8;

        byte[] header = addBMPImageHeader(62 + bufferSize );
        byte[] infos = addBMPImageInfosHeader(saveBmpWidth, saveBmpHeight,bufferSize);
        byte[] colortable = addBMPImageColorTable();

        // 像素扫描 并用0x00补位
        byte bmpData[] = new byte[bufferSize];
        for (int i = 0; i < saveBmpHeight; i++) {
            for (int j = 0; j < saveBmpWidth / 8 ; j++) {
                int srcDataIndex = i * line_byte_num + j;
                int destDataIndex = i * (saveBmpWidth / 8) + j;

                if(j < line_byte_num) {
                    bmpData[destDataIndex] = datas[srcDataIndex];
                }else{
                    bmpData[destDataIndex] = 0x00;
                }
            }
        }
        String path = "";
        try {
            File dirFile = new File(BASEDIR + "/Ucast/" +MyTools.millisToDateStringOnlyYMD(System.currentTimeMillis()));
            if (!dirFile.exists()) {
                dirFile.mkdir();
            }
            if (outPath == null) {
                path = BASEDIR + "/Ucast/" + MyTools.millisToDateStringOnlyYMD(System.currentTimeMillis()) + File.separator + MyTools.millisToDateStringNoSpace(System.currentTimeMillis()) +"_"+ UUID.randomUUID().toString().replace("-", "") + ".bmp";
            }else{
                path = outPath;
            }
            File myCaptureFile = new File(path);
            FileOutputStream fileos = new FileOutputStream(myCaptureFile);

            fileos.write(header);
            fileos.write(infos);
            fileos.write(colortable);
            fileos.write(bmpData);

            fileos.flush();
            fileos.close();

        } catch (Exception e){
            return null;
        }
//        if (bitmap != null && !bitmap.isRecycled()) {
//            bitmap.recycle();
//            bitmap = null;
//        }
        return path;
    }
    //将bitmap对象中像素数据转换成位图深度为1的bmp数据
    private static byte[] getSaveOnebitBmpData(Bitmap bitmap) {
        int w = bitmap.getWidth();
        int h = bitmap.getHeight();
        int len = w * h;
        int[] b = new int[ len ];
        bitmap.getPixels(b, 0, w, 0, 0, w, h);//取得BITMAP的所有像素点


        int bufflen = 0;
        int[] tmp = new int[3];
        int index = 0,bitindex = 1;
        //将8字节变成1个字节,不足补0
        if (len% 8 != 0){
            bufflen = len / 8 + 1;
        } else {
            bufflen = len / 8;
        }
        //BMP图像数据大小，必须是4的倍数，图像数据大小不是4的倍数时用0填充补足
        if (bufflen % 4 != 0){
            bufflen = bufflen + bufflen%4;
        }

        byte[] buffer = new byte[bufflen];

        for (int i = len - 1; i >= w; i -= w) {
            // DIB文件格式最后一行为第一行，每行按从左到右顺序
            int end = i, start = i - w + 1;
            for (int j = start; j <= end; j++) {

                tmp[0] = b[j]  & 0x000000FF;
                tmp[1] = b[j]  & 0x0000FF00;
                tmp[2] = b[j]  & 0x00FF0000;

                if (bitindex > 8) {
                    index += 1;
                    bitindex = 1;
                }

                if (tmp[0] + tmp[1] +tmp[2] != 0x00FFFFFF) {
                    buffer[index] = (byte) (buffer[index] | (0x01 << 8-bitindex));
                }
                bitindex++;
            }
        }

        return buffer;
    }

    // BMP文件头
    public static byte[] addBMPImageHeader(int size) {
        byte[] buffer = new byte[14];
        buffer[0] = 0x42;
        buffer[1] = 0x4D;
        buffer[2] = (byte) (size >> 0);
        buffer[3] = (byte) (size >> 8);
        buffer[4] = (byte) (size >> 16);
        buffer[5] = (byte) (size >> 24);
        buffer[6] = 0x00;
        buffer[7] = 0x00;
        buffer[8] = 0x00;
        buffer[9] = 0x00;
        //  buffer[10] = 0x36;
        buffer[10] = 0x3E;
        buffer[11] = 0x00;
        buffer[12] = 0x00;
        buffer[13] = 0x00;
        return buffer;
    }
    // BMP文件信息头
    public static byte[] addBMPImageInfosHeader(int w, int h, int size) {
        byte[] buffer = new byte[40];
        buffer[0] = 0x28;
        buffer[1] = 0x00;
        buffer[2] = 0x00;
        buffer[3] = 0x00;

        buffer[4] = (byte) (w >> 0);
        buffer[5] = (byte) (w >> 8);
        buffer[6] = (byte) (w >> 16);
        buffer[7] = (byte) (w >> 24);

        buffer[8] = (byte) (h >> 0);
        buffer[9] = (byte) (h >> 8);
        buffer[10] = (byte) (h >> 16);
        buffer[11] = (byte) (h >> 24);

        buffer[12] = 0x01;
        buffer[13] = 0x00;

        buffer[14] = 0x01;
        buffer[15] = 0x00;

        buffer[16] = 0x00;
        buffer[17] = 0x00;
        buffer[18] = 0x00;
        buffer[19] = 0x00;

        buffer[20] = (byte) (size >> 0);
        buffer[21] = (byte) (size >> 8);
        buffer[22] = (byte) (size >> 16);
        buffer[23] = (byte) (size >> 24);

        //  buffer[24] = (byte) 0xE0;
        //  buffer[25] = 0x01;
        buffer[24] = (byte) 0xC3;
        buffer[25] = 0x0E;
        buffer[26] = 0x00;
        buffer[27] = 0x00;

        //  buffer[28] = 0x02;
        //  buffer[29] = 0x03;
        buffer[28] = (byte) 0xC3;
        buffer[29] = 0x0E;
        buffer[30] = 0x00;
        buffer[31] = 0x00;

        buffer[32] = 0x00;
        buffer[33] = 0x00;
        buffer[34] = 0x00;
        buffer[35] = 0x00;

        buffer[36] = 0x00;
        buffer[37] = 0x00;
        buffer[38] = 0x00;
        buffer[39] = 0x00;
        return buffer;
    }
    //bmp调色板
    public static byte[] addBMPImageColorTable() {
        byte[] buffer = new byte[8];
        buffer[0] = (byte) 0xFF;
        buffer[1] = (byte) 0xFF;
        buffer[2] = (byte) 0xFF;
        buffer[3] = 0x00;

        buffer[4] = 0x00;
        buffer[5] = 0x00;
        buffer[6] = 0x00;
        buffer[7] = 0x00;
        return buffer;
    }
}
