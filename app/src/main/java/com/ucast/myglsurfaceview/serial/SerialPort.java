/*
 * Copyright 2009 Cedric Priscal
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License. 
 */

package com.ucast.myglsurfaceview.serial;


import java.io.File;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class SerialPort {
	private static final String TAG = "SerialPort";
	private FileDescriptor mFd;
	private FileInputStream mFileInputStream;
	private FileOutputStream mFileOutputStream;
	public static final int PRINTER_TYPE = 1;
	public static final int USB_TYPE = 2;
	public static final int KEYBOARD_TYPE = 3;
	public static final int TEST_TYPE = 4;


	public SerialPort(File device, int baudrate, int flags) throws SecurityException, IOException {

		/* Check access permission */
//		if (!device.canRead() || !device.canWrite()) {
//            MyTools.writeSimpleLog("没有读写权限");
//			try {
//				/* Missing read/write permission, trying to chmod the file */
//				Process su;
//				su = Runtime.getRuntime().exec("/system/bin/su");
//				String cmd = "chmod 666 " + device.getAbsolutePath() + "\n"
//						+ "exit\n";
//				su.getOutputStream().write(cmd.getBytes());
//				if ((su.waitFor() != 0) || !device.canRead()
//						|| !device.canWrite()) {
//					throw new SecurityException("等待返回不为0");
//				}
//			} catch (Exception e) {
//				throw new SecurityException("权限拒绝");
//			}
//		}

		mFd = open(device.getAbsolutePath(), baudrate, flags);

		if (mFd == null) {
			throw new IOException(device.getAbsolutePath() + "打开设备串口错误，错误原因大概没找到");
		}
		mFileInputStream = new FileInputStream(mFd);
		mFileOutputStream = new FileOutputStream(mFd);
	}

	 public void closeSerialPort(){
		 close();
	 }

	// Getters and setters
	public InputStream getInputStream() throws IOException {
		return mFileInputStream;
	}

	public OutputStream getOutputStream() throws IOException {
		return mFileOutputStream;
	}

	// JNI
	private native static FileDescriptor open(String path, int baudrate, int flags);
	public native void close();
	static {
		System.loadLibrary("native-lib");
	}
}
