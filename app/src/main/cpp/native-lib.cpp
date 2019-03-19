/*Sample project for boxdt*/
/*Robin Wang 2019 03 08*/

#include <jni.h>
#include <opencv2/opencv.hpp>
//#include <bits/stdc++.h>
#include <android/log.h>
#include "LedJpeg.h"
#define  LOG_TAG    "JNI_PART"
#define LOGI(...)  __android_log_print(ANDROID_LOG_INFO,LOG_TAG, __VA_ARGS__)
#define LOGD(...)  __android_log_print(ANDROID_LOG_DEBUG,LOG_TAG, __VA_ARGS__)
#define LOGW(...)  __android_log_print(ANDROID_LOG_WARN,LOG_TAG, __VA_ARGS__)
#define LOGE(...)  __android_log_print(ANDROID_LOG_ERROR,LOG_TAG, __VA_ARGS__)
#define LOGF(...)  __android_log_print(ANDROID_LOG_FATAL,LOG_TAG, __VA_ARGS__)
using namespace cv;
using namespace std;


extern "C"{
jstring Java_com_martin_ads_testopencv_MainActivity_stringFromJNI(
        JNIEnv *env,
        jobject /* this */) {
    std::string hello = "Hello from C++";
    return env->NewStringUTF(hello.c_str());
}


jintArray Java_com_ucast_myglsurfaceview_tools_LedControlTools_nativeProcessFrame(JNIEnv* env, jobject, jlong addrRgbaOff, jlong addrRgba, jint threshIn) {
    Mat &mRgbOff = *(Mat *) addrRgbaOff;
    Mat &mRgb = *(Mat *) addrRgba;

    int ret= 0;
    int num = 10;		
    Point coordinate;

    jint BoxCoordinate[2];
    JNIEnv* jniEnv;

    //LOGD("Box detector test begin!!!");
    LOGD("Box detector input image size %dx%d, thresh is %d", mRgb.cols, mRgb.rows, threshIn);
    ret = LedJpeg(mRgb, mRgbOff, num, coordinate, threshIn);
    //LOGD("Box detector test end!!!");

    if(ret < 0)
    {
        LOGD("Box detector failed!!!");
    }
    else{
        //LOGD("Deteted %d Box...",num);
        LOGD("Box Deteted coordinate.x = %d  coordinate.y = %d", coordinate.x, coordinate.y);
    }

    BoxCoordinate[0]= coordinate.x;
    BoxCoordinate[1]= coordinate.y;	

    if(jniEnv == NULL) {
        jniEnv = env;
    }
     jintArray array = jniEnv->NewIntArray(2);
    jniEnv->SetIntArrayRegion(array, 0, 2, BoxCoordinate);	

    return array;

}

}
