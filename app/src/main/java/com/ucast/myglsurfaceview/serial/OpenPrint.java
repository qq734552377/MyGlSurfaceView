package com.ucast.myglsurfaceview.serial;

import com.ucast.myglsurfaceview.exception.ExceptionApplication;
import com.ucast.myglsurfaceview.tools.Config;
import com.ucast.myglsurfaceview.tools.MyTools;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;


/**
 * Created by pj on 2016/1/22.
 */
public class OpenPrint {
    private SerialPort ser;
    private InputStream intput;
    private OutputStream output;
    private String Path = Config.PORTPATHE;
    private boolean mDispose;
    private byte[] Buffer;
    private ArrayQueue<byte[]> _mQueues = new ArrayQueue<byte[]>(0x400);

    private final static String HEAD = new String(new byte[]{0x02});
    private final static String END = new String(new byte[]{0x03});

    //用于存放打印返回信息
    private byte[] fanhuiBuffer ;
    //用于监控fanBuffer的初始偏移量
    private int offSet = 0;
    //用于反应当前应截取的位置
    private int cutPosition = 0;
    //设置存放消息数组的设定长度
    private int fanhuiBufferLen = 1024 ;

    public OpenPrint(String path) {
        Path = path;
        Buffer = new byte[1024];
        fanhuiBuffer = new byte[fanhuiBufferLen];
    }

    public boolean Open() throws Exception{
//        try {
            //实例串口
            ser = new SerialPort(new File(Path), Config.PORTBAUDRATE ,0);
            //获取写入流
            intput = ser.getInputStream();
            //获取输出流
            output = ser.getOutputStream();
            Thread receiveThread = new Thread(new Runnable() {
                @Override
                public void run() {
                    Receive();
                }
            });

//            receiveThread.setPriority(2);
            receiveThread.start();
            new Thread(new Runnable() {
                @Override
                public void run() {
                    WRun();
                }
            }).start();
            ExceptionApplication.gLogger.info("Printer serial "+Path+" open normally !");
            return true;
//        } catch (IOException e) {
//            ExceptionApplication.gLogger.info("Printer serial "+ Path +" open failed !");
//            return false;
//        }
    }

    /**
     * 监听串口程序
     */
    private void Receive() {
        while (!mDispose) {
            try {
                int tatal = intput.available();
                if (tatal <= 0) {
                    Thread.sleep(1);
                    continue;
                }
                int len = intput.read(Buffer);
                if (len > 0) {
                    //解析数据
                    byte[] buffer = new byte[len];
                    System.arraycopy(Buffer, 0, buffer, 0, len);
                    AnalyticalProtocol(buffer);
                    continue;
                }

                if (len < 0) {
                    Dispose();
                }
            } catch (Exception e) {
                Dispose();
            }
        }
    }

    public void Send(byte[] buffer) {
        try {
            AddHandle(buffer);
        } catch (Exception e) {
        }
    }

    public void Send(String str) {
        try {
            AddHandle(str.getBytes());
        } catch (Exception e) {
        }
    }

    private void OnRun() {
        byte[] item = GetItem();
        try {
            if (item != null) {
                SendMessage(item);
            } else {
                Thread.sleep(1);
            }
        } catch (Exception e) {

        }
    }

    private void WRun() {
        while (!mDispose) {
            OnRun();
        }
    }

    public void AddHandle(byte[] buffer) {
        synchronized (OpenPrint.class) {
            _mQueues.enqueue(buffer);
        }
    }

    private byte[] GetItem() {
        synchronized (OpenPrint.class) {
            if (_mQueues.size() > 0) {
                return _mQueues.dequeue();
            }
            return null;
        }
    }

    private void SendMessage(byte[] buffer) {
        try {
            if (mDispose)
                return;
            output.write(buffer);
            output.flush();
//            MyTools.writeToFile(EpsonPicture.TEMPBITPATH + File.separator + "templog.txt",System.currentTimeMillis() + " 发送包完成");
        } catch (IOException e) {
            Dispose();
        }
    }



    private void AnalyticalProtocol(byte[] buffer) {
        //添加串口数据
        jointBuffer(buffer);
        MyTools.writeSimpleLog("加入缓存的数据： " + MyTools.printHexString(buffer));
//        ExceptionApplication.gLogger.info("所有的数据-->"+EpsonParseDemo.printHexString(buffer));
        while (offSet > 0) {
            int startIndex = getIndexByByte((byte) 0x68);
            if (startIndex <= -1)
                break;
            if (startIndex >= fanhuiBuffer.length -1)
                break;
            int datalen = fanhuiBuffer[startIndex + 1];
            int endIndex = startIndex + datalen + 3 ;
            if (endIndex > fanhuiBuffer.length - 1)
                break;
            if (!sumJiaoYan(startIndex,endIndex))
                break;
            byte[] printBuffer = getPrintbyte(startIndex , endIndex);
            MyTools.writeSimpleLog("解析出来的一条数据： " + MyTools.printHexString(printBuffer));
            serial(printBuffer);
            cutBuffer();
        }
    }

    private int getIndexByByte( byte b) {
        for (int i = 0; i < offSet; i++) {
            if (fanhuiBuffer[i] == b) {
                return i;
            }
        }
        return -1;
    }

    private void jointBuffer(byte[] buffer) {
        if (offSet + buffer.length  > fanhuiBuffer.length) {
            // 扩容 为原来的两倍
            byte[] temp = new byte[fanhuiBuffer.length];
            System.arraycopy(fanhuiBuffer,0,temp,0,fanhuiBuffer.length);
            fanhuiBuffer = new byte[fanhuiBuffer.length * 2];
            System.arraycopy(temp,0,fanhuiBuffer,0,temp.length);
        }
       System.arraycopy(buffer,0,fanhuiBuffer,offSet,buffer.length);
        offSet = offSet + buffer.length;
    }



    //返回一个byte对象 用于发送消息 该数组不会包含 头和尾 即0x02和0x03
    private byte[] getPrintbyte(int start, int end) {
        byte[] printByte = new byte[end - start - 1];
        int position = start + 1;
        System.arraycopy(fanhuiBuffer,position,printByte,0,printByte.length);
        cutPosition = end + 1;
        return printByte;
    }

    //用于重新截取fanhuiBuffer的数据
    private void cutBuffer() {
        System.arraycopy(fanhuiBuffer,cutPosition,fanhuiBuffer,0,offSet - cutPosition);
        offSet = offSet - cutPosition;
        if(fanhuiBuffer.length > fanhuiBufferLen && offSet < fanhuiBufferLen/2){
            byte[] temp = new byte[offSet];
            System.arraycopy(fanhuiBuffer,0,temp,0,offSet);
            fanhuiBuffer = new byte[fanhuiBufferLen];
            System.arraycopy(temp,0,fanhuiBuffer,0,offSet);
        }
    }

    public boolean sumJiaoYan(int start ,int end){
        int sum = 0;
        for (int i = start; i < end; i++) {
            sum += fanhuiBuffer[i];
        }
        return (sum & 0xFF) == fanhuiBuffer[end];
    }



    //关闭
    public void Dispose() {
        synchronized (OpenPrint.class) {
            if (!mDispose) {
                mDispose = true;
                ExceptionApplication.gLogger.error(Path + "Printer serial error close!");
                MyDispose();
//                PrinterSerialRestart.Check();
            }
        }
    }

    private void MyDispose() {
        try {
            if (intput != null) {
                intput.close();
            }
            if (output != null) {
                output.close();
            }
            if (ser != null )
                ser.closeSerialPort();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {

        }
    }

    public void serial(byte[] buffer) {
        int dataLen = buffer[0];
        int mode = buffer[1];
        if ((dataLen + 2) == buffer.length){
            switch (mode){
                case 0x28://读卡的回复

                    break;
                case 0x29://写卡的回复

                    break;


                default:

                    break;
            }
        }
    }


}
