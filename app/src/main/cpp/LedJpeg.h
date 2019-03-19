/*
 * LedJpeg.h
 *
 * Copyright: AI System 2018
 * Author: Robin Wang 2018/7/29
 */

#ifndef _CAPTURE_H_
#define _CAPTURE_H_

#include <stdio.h>
#include <iostream>
#include <stdio.h>
#include <opencv2/opencv.hpp>
#include <opencv/highgui.h>
#include <opencv/cxcore.h>
#include <opencv/cv.h>

#ifndef LINUX
//#include <windows.h>
#endif

#include "opencv2/core/core.hpp"
#include "opencv2/highgui/highgui.hpp"
#include "opencv2/imgproc/imgproc.hpp"
#include <map>
#include <iostream>
#include <string.h>
#include <string> 
#define NUMBEROFBOX 16
//#define ALGRIGHM_DEBUG

using   namespace   std;

typedef struct{
float x;
float y;
}IRpoint;

//#define RESEMBLANCE
using namespace cv;

int LedJpeg(Mat &LedOnImage, Mat &LedOffImage, int &numPint, Point &coordinate, int thresh);
//int BoxDt();
#endif /* _CAPTURE_H_ */
