package barry.opencvd2;

		/*
		This code to go into CameraBridgeViewBase in
		order to scale the bitmap to the size of the screen

		The file is at:

		[your path to the sdk]\OpenCV-2.4.4-android-sdk\sdk\java\src\org\opencv\android

		   protected void deliverAndDrawFrame(CvCameraViewFrame frame) {
			   Mat modified;

			   if (mListener != null) {
				   modified = mListener.onCameraFrame(frame);
			   } else {
				   modified = frame.rgba();
			   }

			   boolean bmpValid = true;
			   if (modified != null) {
				   try {
					   Utils.matToBitmap(modified, mCacheBitmap);
				   } catch(Exception e) {
					   Log.e(TAG, "Mat type: " + modified);
					   Log.e(TAG, "Bitmap type: " + mCacheBitmap.getWidth() + "*" + mCacheBitmap.getHeight());
					   Log.e(TAG, "Utils.matToBitmap() throws an exception: " + e.getMessage());
					   bmpValid = false;
				   }
			   }

			   if (bmpValid && mCacheBitmap != null) {
				   Canvas canvas = getHolder().lockCanvas();
				   if (canvas != null) {
					   canvas.drawColor(0, android.graphics.PorterDuff.Mode.CLEAR);


					   /////////////////////////////////////////////////////
					   ////// THIS IS THE CHANGED PART /////////////////////
					   int width = mCacheBitmap.getWidth();
					   int height = mCacheBitmap.getHeight();
					   float scaleWidth = ((float) canvas.getWidth()) / width;
					   float scaleHeight = ((float) canvas.getHeight()) / height;
					   float fScale = Math.min(scaleHeight,  scaleWidth);
					   // CREATE A MATRIX FOR THE MANIPULATION
					   Matrix matrix = new Matrix();
					   // RESIZE THE BITMAP
					   matrix.postScale(fScale, fScale);

					   /////////////////////////////////////////////////////

					   // RECREATE THE NEW BITMAP
					   Bitmap resizedBitmap = Bitmap.createBitmap(mCacheBitmap, 0, 0, width, height, matrix, false);

					   canvas.drawBitmap(resizedBitmap, (canvas.getWidth() - resizedBitmap.getWidth()) / 2, (canvas.getHeight() - resizedBitmap.getHeight()) / 2, null);
					   if (mFpsMeter != null) {
						   mFpsMeter.measure();
						   mFpsMeter.draw(canvas, 20, 30);
					   }
					   getHolder().unlockCanvasAndPost(canvas);
				   }
			   }
		   }



		*
		*
		*/


import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewListener;
import org.opencv.android.JavaCameraView;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfByte;
import org.opencv.core.MatOfFloat;
import org.opencv.core.MatOfInt;
import org.opencv.core.MatOfPoint;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.MatOfRect;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.highgui.Highgui;
import org.opencv.imgproc.Imgproc;
import org.opencv.objdetect.CascadeClassifier;
import org.opencv.utils.Converters;
import org.opencv.video.Video;



import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.hardware.Camera;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.SurfaceView;
import android.view.WindowManager;
import android.widget.Toast;
import android.widget.LinearLayout.LayoutParams;



public class Opencvd2Activity extends Activity  implements CvCameraViewListener {

    public static final int     VIEW_MODE_RGBA  =                 0;
    public static int           viewMode = VIEW_MODE_RGBA;
    public static final int     VIEW_MODE_HOUGHCIRCLES =          1;
    public static final int     VIEW_MODE_HOUGHLINES =            2;
    public static final int     VIEW_MODE_CANNY =                 3;
    public static final int     VIEW_MODE_COLCONTOUR =            4;
    public static final int     VIEW_MODE_FACEDETECT =            5;
    public static final int     VIEW_MODE_YELLOW_QUAD_DETECT =    6;
    public static final int     VIEW_MODE_GFTT =                  7;
    public static final int     VIEW_MODE_OPFLOW =                8;
    public static final int     VIEW_MODE_OBJECT_DETECTION =        9;

    private static final String    TAG                 = "OCVSample::Activity";
    private static final Scalar    FACE_RECT_COLOR     = new Scalar(0, 255, 0, 255);
    private int                    mAbsoluteFaceSize   = 0;
    private float                  mRelativeFaceSize   = 0.2f;

    private CascadeClassifier mCascade;
    private BaseLoaderCallback  mLoaderCallback = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            switch (status) {
                case LoaderCallbackInterface.SUCCESS:
                {
                    mOpenCvCameraView0.enableView();

                    if (iNumberOfCameras > 1)
                        mOpenCvCameraView1.enableView();

                    try {
                        // DO FACE CASCADE SETUP

                        Context context = getApplicationContext();
                        InputStream is3 = context.getResources().openRawResource(R.raw.banana_classifier);
                        File cascadeDir = context.getDir("cascade", Context.MODE_PRIVATE);
                        File cascadeFile = new File(cascadeDir, "banana_classifier.xml");
//                        File cascadeFile = new File(cascadeDir, "chair.xml");
//                        File cascadeFile = new File(cascadeDir, "cascade.xml");
                        FileOutputStream os = new FileOutputStream(cascadeFile);

                        byte[] buffer = new byte[4096];
                        int bytesRead;

                        while ((bytesRead = is3.read(buffer)) != -1) {
                            os.write(buffer, 0, bytesRead);
                        }

                        is3.close();
                        os.close();

                        mCascade = new CascadeClassifier(cascadeFile.getAbsolutePath());

                        if (mCascade.empty()) {
                            //Log.d(TAG, "Failed to load cascade classifier");
                            mCascade = null;
                        }

                        cascadeFile.delete();
                        cascadeDir.delete();

                    }
                    catch (IOException e) {
                        e.printStackTrace();
                        // Log.d(TAG, "Failed to load cascade. Exception thrown: " + e);
                    }

                } break;
                default:
                {
                    super.onManagerConnected(status);
                } break;
            }
        }
    };
    private boolean bShootNow = false, bDisplayTitle = true, bFirstFaceSaved = false;
    private byte[] byteColourTrackCentreHue;
    private double d, dTextScaleFactor, x1, x2, y1, y2;
    private double[] vecHoughLines;
    private Point pt, pt1, pt2;
    private int  x, y, radius, iMinRadius, iMaxRadius, iCannyLowerThreshold,
            iCannyUpperThreshold, iAccumulator, iLineThickness = 3,
            iHoughLinesThreshold = 50, iHoughLinesMinLineSize = 20,
            iHoughLinesGap = 20, iMaxFaceHeight, iMaxFaceHeightIndex,
            iFileOrdinal = 0, iCamera = 0, iNumberOfCameras = 0, iGFFTMax = 40,
            iContourAreaMin = 1000;
    private JavaCameraView mOpenCvCameraView0;
    private JavaCameraView mOpenCvCameraView1;
    private List<Byte> byteStatus;
    private List<Integer> iHueMap, channels;
    private List<Float> ranges;
    private List<Point> pts, corners, cornersThis, cornersPrev;
    private List<MatOfPoint> contours;
    private long lFrameCount = 0, lMilliStart = 0, lMilliNow = 0, lMilliShotTime = 0, lTimeStart = 0, lTimeEnd = 0;
    private Mat mRgba, mGray, mIntermediateMat, mMatRed, mMatGreen, mMatBlue, mROIMat,
            mMatRedInv, mMatGreenInv, mMatBlueInv, mHSVMat, mErodeKernel, mContours,
            lines, mFaceDest, mFaceResized, matOpFlowPrev, matOpFlowThis,
            matFaceHistogramPrevious, matFaceHistogramThis, mHist, mSlidingWindow, mDepth;
    private MatOfFloat mMOFerr, MOFrange;
    private MatOfRect faces;
    private MatOfByte mMOBStatus;
    private MatOfPoint2f mMOP2f1, mMOP2f2, mMOP2fptsPrev, mMOP2fptsThis, mMOP2fptsSafe;
    private MatOfPoint2f mApproxContour;
    private MatOfPoint MOPcorners;
    private MatOfInt MOIone, histSize;
    private Rect rect, rDest;
    private Scalar colorRed, colorGreen;
    private Size sSize, sSize3, sSize5, sMatSize;
    private String string, sShotText;
    private CPUController        cpuController1;
    //    private FelzenswalbHuttenlocherSegmenter fh;
    private int counter = 0;
    private long timeCount = 0;
//    private SN1_3 SN;





    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        iNumberOfCameras = Camera.getNumberOfCameras();

        //Log.d(TAG, "called onCreate");
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        setContentView(R.layout.opencvd2);

        mOpenCvCameraView0 = (JavaCameraView) findViewById(R.id.java_surface_view0);

        if (iNumberOfCameras > 1)
            mOpenCvCameraView1 = (JavaCameraView) findViewById(R.id.java_surface_view1);

        mOpenCvCameraView0.setVisibility(SurfaceView.VISIBLE);
        mOpenCvCameraView0.setCvCameraViewListener(this);

        mOpenCvCameraView0.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));

        if (iNumberOfCameras > 1) {
            mOpenCvCameraView1.setVisibility(SurfaceView.GONE);
            mOpenCvCameraView1.setCvCameraViewListener(this);
            mOpenCvCameraView1.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
        }

        OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_2_4_4, this, mLoaderCallback);



    }


    @Override
    public void onPause()
    {
        super.onPause();
        if (mOpenCvCameraView0 != null)
            mOpenCvCameraView0.disableView();
        if (iNumberOfCameras > 1)
            if (mOpenCvCameraView1 != null)
                mOpenCvCameraView1.disableView();
    }


    public void onResume()
    {
        super.onResume();

        viewMode = VIEW_MODE_RGBA;

        OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_2_4_4, this, mLoaderCallback);
    }


    public void onDestroy() {
        super.onDestroy();
        if (mOpenCvCameraView0 != null)
            mOpenCvCameraView0.disableView();
        if (iNumberOfCameras > 1)
            if (mOpenCvCameraView1 != null)
                mOpenCvCameraView1.disableView();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.opencvd2, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_info) {
            Intent myIntent1 = new Intent(Intent.ACTION_VIEW, Uri.parse("http://www.barrythomas.co.uk/machinevision.html"));
            startActivity(myIntent1);
        } else if (item.getItemId() == R.id.action_rgbpreview) {
            viewMode = VIEW_MODE_RGBA;
            lFrameCount = 0;
            lMilliStart = 0;
        } else if (item.getItemId() == R.id.action_cannyedges) {
            viewMode = VIEW_MODE_CANNY;
            lFrameCount = 0;
            lMilliStart = 0;
        } else if (item.getItemId() == R.id.action_houghcircles) {
            viewMode = VIEW_MODE_HOUGHCIRCLES;
            lFrameCount = 0;
            lMilliStart = 0;
        } else if (item.getItemId() == R.id.action_houghlines) {
            viewMode = VIEW_MODE_HOUGHLINES;
            lFrameCount = 0;
            lMilliStart = 0;
        } else if (item.getItemId() == R.id.action_colourcontour) {
            viewMode = VIEW_MODE_COLCONTOUR;
            lFrameCount = 0;
            lMilliStart = 0;
        } else if (item.getItemId() == R.id.action_facedetect) {
            viewMode = VIEW_MODE_FACEDETECT;
            lFrameCount = 0;
            lMilliStart = 0;
            bFirstFaceSaved = false;
        } else if (item.getItemId() == R.id.action_colourquad) {
            viewMode = VIEW_MODE_YELLOW_QUAD_DETECT;
            lFrameCount = 0;
            lMilliStart = 0;
        } else if (item.getItemId() == R.id.action_gftt) {
            viewMode = VIEW_MODE_GFTT;
            lFrameCount = 0;
            lMilliStart = 0;
        } else if (item.getItemId() == R.id.action_opflow) {
            viewMode = VIEW_MODE_OPFLOW;
            lFrameCount = 0;
            lMilliStart = 0;
        } else if (item.getItemId() == R.id.action_slidingwindows) {
            viewMode = VIEW_MODE_OBJECT_DETECTION;
            lFrameCount = 0;
            lMilliStart = 0;
        } else if (item.getItemId() == R.id.action_toggletitles) {
            if (bDisplayTitle == true)
                bDisplayTitle = false;
            else
                bDisplayTitle = true;
        } else if (item.getItemId() == R.id.action_swapcamera) {
            if (iNumberOfCameras > 1) {
                if (iCamera == 0) {
                    mOpenCvCameraView0.setVisibility(SurfaceView.GONE);
                    mOpenCvCameraView1 = (JavaCameraView) findViewById(R.id.java_surface_view1);
                    mOpenCvCameraView1.setCvCameraViewListener(this);
                    mOpenCvCameraView1.setVisibility(SurfaceView.VISIBLE);

                    iCamera = 1;
                }
                else {
                    mOpenCvCameraView1.setVisibility(SurfaceView.GONE);
                    mOpenCvCameraView0 = (JavaCameraView) findViewById(R.id.java_surface_view0);
                    mOpenCvCameraView0.setCvCameraViewListener(this);
                    mOpenCvCameraView0.setVisibility(SurfaceView.VISIBLE);

                    iCamera = 0;
                }
            }
            else
                Toast.makeText(getApplicationContext(), "Sadly, your device does not have a second camera",
                        Toast.LENGTH_LONG).show();
        }

        return true;
    }

    @Override
    public void onCameraViewStarted(int width, int height) {
        // TODO Auto-generated method stub
        byteColourTrackCentreHue = new byte[3];
        // green = 60 // mid yellow  27
        byteColourTrackCentreHue[0] = 27;
        byteColourTrackCentreHue[1] = 100;
        byteColourTrackCentreHue[2] = (byte)255;
        byteStatus = new ArrayList<Byte>();

        channels = new ArrayList<Integer>();
        channels.add(0);
        colorRed = new Scalar(255, 0, 0, 255);
        colorGreen = new Scalar(0, 255, 0, 255);
        contours = new ArrayList<MatOfPoint>();
        corners = new ArrayList<Point>();
        cornersThis = new ArrayList<Point>();
        cornersPrev = new ArrayList<Point>();

        faces = new MatOfRect();

        histSize = new MatOfInt(25);

        iHueMap = new ArrayList<Integer>();
        iHueMap.add(0);
        iHueMap.add(0);
        lines = new Mat();

        mApproxContour = new MatOfPoint2f();
        mContours = new Mat();
        mHist = new Mat();
        mGray = new Mat();
        mDepth = new Mat();
        mHSVMat = new Mat();
        mIntermediateMat = new Mat();
        mMatRed = new Mat();
        mMatGreen = new Mat();
        mMatBlue = new Mat();
        mMatRedInv = new Mat();
        mMatGreenInv = new Mat();
        mMatBlueInv = new Mat();
        MOIone = new MatOfInt(0);

        MOFrange = new MatOfFloat(0f, 256f);
        mMOP2f1 = new MatOfPoint2f();
        mMOP2f2 = new MatOfPoint2f();
        mMOP2fptsPrev = new MatOfPoint2f();
        mMOP2fptsThis = new MatOfPoint2f();
        mMOP2fptsSafe = new MatOfPoint2f();
        mMOFerr = new MatOfFloat();
        mMOBStatus = new MatOfByte();
        MOPcorners = new MatOfPoint();
        mRgba = new Mat();
        mROIMat = new Mat();
        mFaceDest = new Mat();
        mFaceResized = new Mat();
        matFaceHistogramPrevious = new Mat();
        matFaceHistogramThis= new Mat();
        matOpFlowThis = new Mat();
        matOpFlowPrev= new Mat();
        mSlidingWindow = new Mat();

        pt = new Point (0, 0);
        pt1 = new Point (0, 0);
        pt2 = new Point (0, 0);

        pts = new ArrayList<Point>();

        ranges = new ArrayList<Float>();
        ranges.add(50.0f);
        ranges.add(256.0f);
        rect = new Rect();
        rDest = new Rect();

        sMatSize = new Size();
        sSize = new Size();
        sSize3 = new Size(3, 3);
        sSize5 = new Size(5, 5);

        string = "";

        DisplayMetrics dm = this.getResources().getDisplayMetrics();
        int densityDpi = dm.densityDpi;
        dTextScaleFactor = ((double)densityDpi / 240.0) * 0.9;

        mRgba = new Mat(height, width, CvType.CV_8UC4);
        mIntermediateMat = new Mat(height, width, CvType.CV_8UC4);

        //      intialize FelzenswalbHuttenlocherSegmenter
//        fh = new FelzenswalbHuttenlocherSegmenter((float)0.8,500,500);

        //        initialize cpu controller, set to 400 MHz;
        cpuController1 = new CPUController();
        cpuController1.CPU_FreqChange(0);

        System.out.println("here");
        //        Log.i(TAG, "Power save mode");

    }

    @Override
    public void onCameraViewStopped() {
        releaseMats();
    }

    public void releaseMats () {
        mRgba.release();
        mIntermediateMat.release();
        mGray.release();
        mMatRed.release();
        mMatGreen.release();
        mMatBlue.release();
        mROIMat.release();
        mMatRedInv.release();
        mMatGreenInv.release();
        mMatBlueInv.release();
        mHSVMat.release();
        mErodeKernel.release();
        mContours.release();
        lines.release();
        faces.release();
        MOPcorners.release();
        mMOP2f1.release();
        mMOP2f2.release();
        mApproxContour.release();

    }

    @Override
    public Mat onCameraFrame(Mat inputFrame) {
        iMinRadius = 20;
        iMaxRadius = 400;
        iCannyLowerThreshold = 50;
        iCannyUpperThreshold = 180;
        iAccumulator = 300;
        mErodeKernel = Imgproc.getStructuringElement(Imgproc.MORPH_CROSS, sSize3);

        // start the timing counter to put the framerate on screen
        // and make sure the start time is up to date, do
        // a reset every 10 seconds
        if (lMilliStart == 0)
            lMilliStart = System.currentTimeMillis();

        if ((lMilliNow - lMilliStart) > 10000) {
            lMilliStart = System.currentTimeMillis();
            lFrameCount = 0;
        }

        inputFrame.copyTo(mRgba);
        sMatSize.width = mRgba.width();
        sMatSize.height = mRgba.height();

        switch (Opencvd2Activity.viewMode) {

            case VIEW_MODE_RGBA:


                //                Mat newFrame = Highgui.imread("/mnt/sdcard/ImageDataset/img_0100.png");
                //                mRgba = newFrame;


                if (bDisplayTitle)
                    ShowTitle ("BGR Preview", 1, colorGreen);

                break;


            case VIEW_MODE_CANNY:

                Imgproc.cvtColor(mRgba, mGray, Imgproc.COLOR_RGBA2GRAY);

                // doing a gaussian blur prevents getting a lot of false hits
                Imgproc.GaussianBlur(mGray, mGray, sSize5, 2, 2);

                iCannyLowerThreshold = 35;
                iCannyUpperThreshold = 75;

                Imgproc.Canny(mGray, mIntermediateMat, iCannyLowerThreshold, iCannyUpperThreshold);

                Imgproc.cvtColor(mIntermediateMat, mRgba, Imgproc.COLOR_GRAY2BGRA, 4);

                if (bDisplayTitle)
                    ShowTitle ("Canny Edges", 1, colorGreen);

                break;

            case VIEW_MODE_HOUGHCIRCLES:

                Imgproc.cvtColor(mRgba, mGray, Imgproc.COLOR_RGBA2GRAY);

                // doing a gaussian blur prevents getting a lot of false hits
                Imgproc.GaussianBlur(mGray, mGray, sSize5, 2, 2);

                // the lower this figure the more spurious circles you get
                // 50 looks good in CANNY, but 100 is better when converting that into Hough circles
                iCannyUpperThreshold = 100;

                Imgproc.HoughCircles(mGray, mIntermediateMat, Imgproc.CV_HOUGH_GRADIENT, 2.0, mGray.rows() / 8,
                        iCannyUpperThreshold, iAccumulator, iMinRadius, iMaxRadius);

                if (mIntermediateMat.cols() > 0)
                    for (int x = 0; x < Math.min(mIntermediateMat.cols(), 10); x++)
                    {
                        double vCircle[] = mIntermediateMat.get(0,x);

                        if (vCircle == null)
                            break;

                        pt.x = Math.round(vCircle[0]);
                        pt.y = Math.round(vCircle[1]);
                        radius = (int)Math.round(vCircle[2]);
                        // draw the found circle
                        Core.circle(mRgba, pt, radius, colorRed, iLineThickness);

                        // draw a cross on the centre of the circle
                        DrawCross (mRgba, colorRed, pt);
                    }

                if (bDisplayTitle)
                    ShowTitle ("Hough Circles", 1, colorGreen);

                break;

            case VIEW_MODE_HOUGHLINES:

                Imgproc.cvtColor(mRgba, mGray, Imgproc.COLOR_RGBA2GRAY);

                // doing a gaussian blur prevents getting a lot of false hits
                Imgproc.GaussianBlur(mGray, mGray, sSize5, 2, 2);


                // the lower this figure the more spurious circles you get
                // 50 upper looks good in CANNY, but 75 is better when converting that into Hough circles
                iCannyLowerThreshold = 45;
                iCannyUpperThreshold = 75;

                Imgproc.Canny(mGray, mGray, iCannyLowerThreshold, iCannyUpperThreshold);

                Imgproc.HoughLinesP(mGray, lines, 1, Math.PI/180, iHoughLinesThreshold, iHoughLinesMinLineSize, iHoughLinesGap);

                for (int x = 0; x < Math.min(lines.cols(), 40); x++)
                {
                    vecHoughLines = lines.get(0, x);

                    if (vecHoughLines.length == 0)
                        break;

                    x1 = vecHoughLines[0];
                    y1 = vecHoughLines[1];
                    x2 = vecHoughLines[2];
                    y2 = vecHoughLines[3];

                    pt1.x = x1;
                    pt1.y = y1;
                    pt2.x = x2;
                    pt2.y = y2;

                    Core.line(mRgba, pt1, pt2, colorRed, 3);
                }

                if (bDisplayTitle)
                    ShowTitle ("Hough Lines", 1, colorGreen);

                break;

            case VIEW_MODE_COLCONTOUR:

                // Convert the image into an HSV image

                Imgproc.cvtColor(mRgba, mHSVMat, Imgproc.COLOR_RGB2HSV, 3);

                Core.inRange(mHSVMat, new Scalar(byteColourTrackCentreHue[0] - 10, 100, 100),
                        new Scalar(byteColourTrackCentreHue[0] + 10, 255, 255), mHSVMat);

                // Here i'm only using the external contours and by
                // eroding we make the draw a teeny bit faster and the result a lot smoother
                // on the rough edges where the colour fades out of range by losing a lot
                // of the little spiky corners.

                Imgproc.erode(mHSVMat, mHSVMat, mErodeKernel);
                contours.clear();

                Imgproc.findContours(mHSVMat, contours, mContours, Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE);

                for (x = 0; x < contours.size(); x++) {
                    d = Imgproc.contourArea (contours.get(x));

                    // get an approximation of the contour (last but one param is the min required
                    // distance between the real points and the new approximation (in pixels)

                    // contours is a List<MatOfPoint>
                    // so contours.get(x) is a single MatOfPoint
                    // but to use approxPolyDP we need to pass a MatOfPoint2f
                    // so we need to do a conversion

                    contours.get(x).convertTo(mMOP2f1, CvType.CV_32FC2);

                    if (d > iContourAreaMin) {

                        Imgproc.approxPolyDP(mMOP2f1, mMOP2f2, 2, true);

                        // convert back to MatOfPoint and put it back in the list
                        mMOP2f2.convertTo(contours.get(x), CvType.CV_32S);

                        // draw the contour itself
                        Imgproc.drawContours(mRgba, contours, x, colorRed, iLineThickness);

                    }
                }

                if (bDisplayTitle)
                    ShowTitle ("Colour Contours", 1, colorGreen);

                break;


            case VIEW_MODE_YELLOW_QUAD_DETECT:

                // Convert the image into an HSV image

                Imgproc.cvtColor(mRgba, mHSVMat, Imgproc.COLOR_RGB2HSV, 3);

                Core.inRange(mHSVMat, new Scalar(byteColourTrackCentreHue[0] - 12, 100, 100),
                        new Scalar(byteColourTrackCentreHue[0] + 12, 255, 255), mHSVMat);

                contours.clear();

                Imgproc.findContours(mHSVMat, contours, mContours, Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE);

                for (x = 0; x < contours.size(); x++) {
                    d = Imgproc.contourArea (contours.get(x));

                    if (d > iContourAreaMin) {
                        // get an approximation of the contour (last but one param is the min required
                        // distance between the real points and the new approximation (in pixels)

                        contours.get(x).convertTo(mMOP2f1, CvType.CV_32FC2);

                        Imgproc.approxPolyDP(mMOP2f1, mMOP2f2, 15, true);

                        // convert back to MatOfPoint and put it back in the list
                        mMOP2f2.convertTo(contours.get(x), CvType.CV_32S);

                        if (contours.get(x).rows() == 4) {

                            Converters.Mat_to_vector_Point2f(contours.get(x), pts);

                            Imgproc.drawContours(mRgba, contours, x, colorRed, iLineThickness);

                            Core.line(mRgba, pts.get(0), pts.get(2), colorRed, iLineThickness);
                            Core.line(mRgba, pts.get(1), pts.get(3), colorRed, iLineThickness);
                        }
                    }
                }

                if (bDisplayTitle)
                    ShowTitle ("Colour quadrilateral", 1, colorGreen);

                break;



            case VIEW_MODE_FACEDETECT:

                // Convert the image into a gray image

                Imgproc.cvtColor(mRgba, mGray, Imgproc.COLOR_RGBA2GRAY);

                rDest.x = 5;
                rDest.y = 5;
                rDest.width = 100;
                rDest.height = 100;

                mFaceDest = mRgba.submat(rDest);
                iMaxFaceHeight = 0;
                iMaxFaceHeightIndex = -1;

                if (mCascade != null) {
                    int height = mGray.rows();
                    double faceSize = (double) height * 0.25;

                    sSize.width = faceSize;
                    sSize.height = faceSize;

                    mCascade.detectMultiScale(mGray, faces, 1.1, 2, 2, sSize, new Size());

                    Rect[] facesArray = faces.toArray();

                    for (int i = 0; i < facesArray.length; i++) {

                        // draw the rectangle itself
                        Core.rectangle(mRgba, facesArray[i].tl(), facesArray[i].br(), colorRed, 3);
                        if (iMaxFaceHeight < facesArray[i].height) {
                            iMaxFaceHeight = facesArray[i].height;
                            iMaxFaceHeightIndex = i;
                        }
                    }

                    // now save the biggest face to a file
                    if (iMaxFaceHeight > 0) {
                        // we have at least one face
                        rect = facesArray[iMaxFaceHeightIndex];

                        // get the submat of the rect containing the face
                        mROIMat = mRgba.submat(rect);

                        if (bFirstFaceSaved == false) {
                            SaveImage (mROIMat);
                            bFirstFaceSaved = true;
                        }

                        // resize it to the dest rect size (100x100)
                        sSize.width = 100;
                        sSize.height = 100;
                        Imgproc.resize(mROIMat, mFaceResized, sSize);
                        // copy it to dest rect in main image

                        mFaceResized.copyTo(mFaceDest);

                        // compare the histogram of this face with the histogram
                        // of the previous face. If they are a good match (ie
                        // return val of comparison is > 0.9) then they are probably
                        // the same face. We already saved this face before, so
                        // don't save it again. If the previous version was blurry
                        // and this one is sharp, the coefficient of similarness
                        // is around 0.7 to 0.85 so the the most recent face will
                        // be saved this means if some pics of this face are blurry
                        // and some are sharp, we save both, so one result should be
                        // sharp.

                        matFaceHistogramThis.copyTo(matFaceHistogramPrevious);

                        matFaceHistogramThis = getHistogram (mFaceDest);

                        // this test makes sure we don't do a compare on the first face found
                        // because the width of the previous face will be zero
                        if (matFaceHistogramThis.width() == matFaceHistogramPrevious.width()) {

                            d = Imgproc.compareHist(matFaceHistogramThis, matFaceHistogramPrevious, Imgproc.CV_COMP_CORREL);
                            //Log.d("Baz", "compareHist d = "+d);
                            if (d < 0.95) {
                                SaveImage (mROIMat);
                                //Log.d("Baz", "New face: "+d);
                            }
                        }
                    }
                }

                if (bDisplayTitle)
                    ShowTitle ("Face Detection", 1, colorGreen);

                break;

            case VIEW_MODE_GFTT:

                Imgproc.cvtColor(mRgba, mGray, Imgproc.COLOR_RGBA2GRAY);

                // DON'T do a gaussian blur here, it makes the results poorer and
                // takes 0.5 off the fps rate

                Imgproc.goodFeaturesToTrack(mGray, MOPcorners, iGFFTMax, 0.01, 20);

                y = MOPcorners.rows();

                corners = MOPcorners.toList();

                for (int x = 0; x < y; x++)
                    Core.circle(mRgba, corners.get(x), 6, colorRed, iLineThickness - 1);
                //DrawCross (mRgba, colorRed, corners.get(x));

                if (bDisplayTitle)
                    ShowTitle ("Track Features", 1, colorGreen);

                break;


            case VIEW_MODE_OPFLOW:

                if (mMOP2fptsPrev.rows() == 0) {

                    //Log.d("Baz", "First time opflow");
                    // first time through the loop so we need prev and this mats
                    // plus prev points
                    // get this mat
                    Imgproc.cvtColor(mRgba, matOpFlowThis, Imgproc.COLOR_RGBA2GRAY);

                    // copy that to prev mat
                    matOpFlowThis.copyTo(matOpFlowPrev);

                    // get prev corners
                    Imgproc.goodFeaturesToTrack(matOpFlowPrev, MOPcorners, iGFFTMax, 0.05, 20);
                    mMOP2fptsPrev.fromArray(MOPcorners.toArray());

                    // get safe copy of this corners
                    mMOP2fptsPrev.copyTo(mMOP2fptsSafe);
                }
                else
                {
                    //Log.d("Baz", "Opflow");
                    // we've been through before so
                    // this mat is valid. Copy it to prev mat
                    matOpFlowThis.copyTo(matOpFlowPrev);

                    // get this mat
                    Imgproc.cvtColor(mRgba, matOpFlowThis, Imgproc.COLOR_RGBA2GRAY);

                    // get the corners for this mat
                    Imgproc.goodFeaturesToTrack(matOpFlowThis, MOPcorners, iGFFTMax, 0.05, 20);
                    mMOP2fptsThis.fromArray(MOPcorners.toArray());

                    // retrieve the corners from the prev mat
                    // (saves calculating them again)
                    mMOP2fptsSafe.copyTo(mMOP2fptsPrev);

                    // and save this corners for next time through

                    mMOP2fptsThis.copyTo(mMOP2fptsSafe);
                }


					/*
					Parameters:
						prevImg first 8-bit input image
						nextImg second input image
						prevPts vector of 2D points for which the flow needs to be found; point coordinates must be single-precision floating-point numbers.
						nextPts output vector of 2D points (with single-precision floating-point coordinates) containing the calculated new positions of input features in the second image; when OPTFLOW_USE_INITIAL_FLOW flag is passed, the vector must have the same size as in the input.
						status output status vector (of unsigned chars); each element of the vector is set to 1 if the flow for the corresponding features has been found, otherwise, it is set to 0.
						err output vector of errors; each element of the vector is set to an error for the corresponding feature, type of the error measure can be set in flags parameter; if the flow wasn't found then the error is not defined (use the status parameter to find such cases).
					*/
                Video.calcOpticalFlowPyrLK(matOpFlowPrev, matOpFlowThis, mMOP2fptsPrev, mMOP2fptsThis, mMOBStatus, mMOFerr);

                cornersPrev = mMOP2fptsPrev.toList();
                cornersThis = mMOP2fptsThis.toList();
                byteStatus = mMOBStatus.toList();

                y = byteStatus.size() - 1;

                for (x = 0; x < y; x++) {
                    if (byteStatus.get(x) == 1) {
                        pt = cornersThis.get(x);
                        pt2 = cornersPrev.get(x);

                        Core.circle(mRgba, pt, 5, colorRed, iLineThickness - 1);

                        Core.line(mRgba, pt, pt2, colorRed, iLineThickness);
                    }
                }

                //Log.d("Baz", "Opflow feature count: "+x);
                if (bDisplayTitle)
                    ShowTitle ("Optical Flow", 1, colorGreen);

                break;


            case VIEW_MODE_OBJECT_DETECTION:

//                boolean FullScan = true;
//                boolean DepthOnly = true;
//                boolean Adaptive = true;
//
//                if (FullScan){
//
//            }
//                cpuController1.CPU_FreqChange(4);

////                Demo version
//                boolean detection = false;
//                Imgproc.cvtColor(mRgba, mGray, Imgproc.COLOR_RGBA2GRAY); // Convert to grayscale
//
//                MatOfRect faces = new MatOfRect();
//
//                if (mAbsoluteFaceSize == 0) {
//                    int height = mGray.rows();
//
//                    if (Math.round(height * mRelativeFaceSize) > 0) {
//                        mAbsoluteFaceSize = Math.round(height * mRelativeFaceSize);
//                    }
//                }
//                if (mCascade != null)
//                    mCascade.detectMultiScale(mGray, faces, 1.1, 6, 0, // TODO: objdetect.CV_HAAR_SCALE_IMAGE
//                            new Size(mAbsoluteFaceSize, mAbsoluteFaceSize), new Size());
//
//                // Each rectangle in the faces array is a face
//                // Draw a rectangle around each face
//                Rect[] facesArray = faces.toArray();
//                for (int i = 0; i < facesArray.length; i++) {
//                    detection = true;
//                    Core.rectangle(mRgba, facesArray[i].tl(), facesArray[i].br(), FACE_RECT_COLOR, 3);
//                }
//                if (detection) {
//                    cpuController1.CPU_FreqChange(4);
//                }

//                Highgui.imwrite("/mnt/sdcard/results/" + files.get(j).getName(), newFrame);
//                if (counter > 82){
//                    cpuController1.CPU_FreqChange(4);// Set the frequency to 1134000 KHz
//                    Log.d("detected", "detected:");
//
//                }
//

                if (counter > 100) {
                    Log.d("over", "over:");
                }















//                Imgproc.cvtColor(mRgba, mGray, Imgproc.COLOR_RGBA2GRAY);

//                Size sizeRgba = mRgba.size();
//                int step = 200;
//
//                rDest.x = 5;
//                rDest.y = 5;
//                rDest.width = 400;
//                rDest.height = 400;
//
//                for (rDest.x = 5; rDest.x < sizeRgba.width; rDest.x = rDest.x + step ) {
//                    for (rDest.y = 5; rDest.y < sizeRgba.height; rDest.y = rDest.y + step) {
//                        mSlidingWindow = mRgba.submat(rDest);
//                        Core.rectangle(mRgba, rDest.tl(), rDest.br(), colorRed, 3);
//
//
//                    }
//                }

//                Mat rgbaInnerWindow, rgbaInnerWindow1, rgbaInnerWindow2;
//                Mat cellWindow;
//                MatOfByte status = new MatOfByte();
//                MatOfFloat err = new MatOfFloat();
//                MatOfPoint2f nextPts =new MatOfPoint2f();
//                MatOfPoint2f prevPts =new MatOfPoint2f();
//                MatOfPoint initial = new MatOfPoint();

//                int rows = (int) sizeRgba.height;
//                int cols = (int) sizeRgba.width;


//                int left = 2*cols / 8;
//                int top = 2*rows / 8;
//
//                int width = cols * 1 / 4;
//                int height = rows * 1 / 4;

//                Core.rectangle(mRgba, facesArray[i].tl(), facesArray[i].br(), colorRed, 3);
//                int cols = mRgba.cols();
//                int rows = mRgba.rows();
//                int xOffset = (mOpenCvCameraView.getWidth() - cols) / 2;
//                int yOffset = (mOpenCvCameraView.getHeight() - rows) / 2;
//                int x = (int)event.getX() - xOffset;
//                int y = (int)event.getY() - yOffset;


//                // doing a gaussian blur prevents getting a lot of false hits
//                Imgproc.GaussianBlur(mGray, mGray, sSize5, 2, 2);
//
//                iCannyLowerThreshold = 35;
//                iCannyUpperThreshold = 75;
//
//                Imgproc.Canny(mGray, mIntermediateMat, iCannyLowerThreshold, iCannyUpperThreshold);
//
//                Imgproc.cvtColor(mIntermediateMat, mRgba, Imgproc.COLOR_GRAY2BGRA, 4);
//

























                cpuController1.CPU_FreqChange(4);

//                Test code for ground removal
//                Mat newFrame = Highgui.imread("/mnt/sdcard/ImageDataset/ground/G1.png");
////                newFrame = newFrame.submat(1,100,1,100);
////                Highgui.imwrite("/mnt/sdcard/ImageDataset/test/submatK111.png",newFrame);
//
//                Mat depthFrame = new Mat();
//                //depthFrame=newFrame.clone();
//                Imgproc.cvtColor(newFrame, depthFrame, Imgproc.COLOR_RGBA2GRAY); // Convert to grayscale
//
//                lTimeStart = System.currentTimeMillis();
//
//                SN1_2 SN;
//                SN = new SN1_2(depthFrame);
//
////                test SN1_1
////                SN1_1 SN;
////                SN = new SN1_1(depthFrame);
//
//
//                lTimeEnd = System.currentTimeMillis();
//                Log.d("Time1", "used :"+ (lTimeEnd-lTimeStart));




    /*                Test code for image segmentation
//                Image Segmentation
//                =========start=========
//                Mat tRgba = mRgba.submat(0,100,0,100);
//                lTimeStart = System.currentTimeMillis();
//                mRgba = fh.segmentImage(tRgba);
//                Highgui.imwrite("/mnt/sdcard/ImageDataset/test/result1.png", mRgba);
//                lTimeEnd = System.currentTimeMillis();
//                Log.d("Time", "used :"+ (lTimeEnd-lTimeStart));
//                =========end=========
*/

//                Test code for adaptive DVFS
//                =========start=========
//                Start with lowest freq., increase when fps less than 10, decrease when fps more than 20

        // load image folder
//
//                List<File> files = getListFiles(new File("/mnt/sdcard/ImageDataset/banana_chair/"));
//                List<File> files1 = getListFiles(new File("/mnt/sdcard/ImageDataset/depth new banana/"));
//
//
//                for (int j = 0; j < files.size(); j++) {

//                    Log.d("Files", "FileName:" + files.get(j).getName());
//                    Log.d("detected", "detected:" + counter);
//                    Mat depthFrame = Highgui.imread("/mnt/sdcard/ImageDataset/depth_large/" + files1.get(j).getName());
//                    Mat newFrame = Highgui.imread("/mnt/sdcard/ImageDataset/banana_chair/" + files.get(j).getName());

//                    Mat depthFrame = Highgui.imread("/mnt/sdcard/ImageDataset/depth new banana/" + files1.get(j).getName());
//                    Mat newFrame = Highgui.imread("/mnt/sdcard/ImageDataset/new banana/" + files.get(j).getName());


//                cpuController1.CPU_FreqChange(1);
//                if (counter <= 72){
//                    cpuController1.CPU_FreqChange(0); // >72 for banana, >82 for chair
//                }
//                if (counter == 73){
//                    cpuController1.CPU_FreqChange(1); // >72 for banana, >82 for chair
//                }
//                if (counter == 74){
//                    cpuController1.CPU_FreqChange(2); // >72 for banana, >82 for chair
//                }
//                if (counter >= 75){
//                    cpuController1.CPU_FreqChange(3); // >72 for banana, >82 for chair
//                }

// cd sys/devices/system/cpu/cpu0/cpufreq
// cat cpuinfo_cur_freq


//                cpuController1.CPU_FreqChange(4);
                    lTimeStart = System.currentTimeMillis(); // start time

                    counter = counter + 1;
                    Log.d("Now", "count :" + counter);
//                Log.d("Start time","at:"+ counter);

//                load single image, color and depth
//                Mat newFrame = Highgui.imread("/mnt/sdcard/ImageDataset/new_chair3/KinectScreenshot-Color-07-32-22.png"); // chair video 1
//                Mat newFrame = Highgui.imread("/mnt/sdcard/ImageDataset/new_chair3/KinectScreenshot-Color-07-37-40.png"); // chair video 2
//                Mat newFrame = Highgui.imread("/mnt/sdcard/ImageDataset/new_chair3/KinectScreenshot-Color-08-36-23.png"); // chair video 3
//                Mat newFrame = Highgui.imread("/mnt/sdcard/ImageDataset/bag_chair/KinectScreenshot-Color-10-52-38.png"); // chair bag video 4
                Mat newFrame = Highgui.imread("/mnt/sdcard/ImageDataset/banana_chair/KinectScreenshot-Color-10-56-27.png"); // chair banana video 5


                Mat depthFrame = Highgui.imread("/mnt/sdcard/ImageDataset/new_chair3/depth/KinectScreenshot-Depth-11-32-22.png");

                    // rgbd threoshold
//                newFrame = newFrame.submat(40,424,10,512);//for KinectScreenshot-Color-07-32-22.png
//                newFrame = newFrame.submat(121,424,6,512);//for KinectScreenshot-Color-07-37-40.png
//                newFrame = newFrame.submat(109,424,1,512);//for KinectScreenshot-Color-08-36-23.png
//                newFrame = newFrame.submat(113,424,1,512);//for bag chair
//                newFrame = newFrame.submat(78,424,1,512);//for banana chair


//                // rgbd threshold + ground removal
//                Mat depthFrame = Highgui.imread("/mnt/sdcard/ImageDataset/new_chair3/KinectScreenshot-Color-08-36-23.png", Highgui.CV_LOAD_IMAGE_ANYDEPTH);
//		        Mat H1 = Highgui.imread("/mnt/sdcard/ImageDataset/new_chair3/H1.png");
//		        Imgproc.cvtColor(H1, H1, Imgproc.COLOR_RGB2GRAY);
//		        Mat H2 = Highgui.imread("/mnt/sdcard/ImageDataset/new_chair3/H2.png");
//		        Imgproc.cvtColor(H2, H2, Imgproc.COLOR_RGB2GRAY);
//
//                SN1_3 SN;
//		        SN = new SN1_3(depthFrame, H1 ,H2);

                    // rgbd threshold + ground removal
//                newFrame = newFrame.submat(146,347,133,258);//for KinectScreenshot-Color-07-32-22.png
//                newFrame = newFrame.submat(124,273,231,347);//for KinectScreenshot-Color-07-37-40.png
//                newFrame = newFrame.submat(137,422,241,415);//for KinectScreenshot-Color-08-36-23.png
//                newFrame = newFrame.submat(104,321,129,479);//for chair bag
//                newFrame = newFrame.submat(78,418,96,371);//for chair banana



////                newFrame = newFrame.submat(209,424,69,303); // for video 2 @48
////                depthFrame = depthFrame.submat(0,100,0,100); // for video 2 @48
//
////                Mat newFrame = depthFrame.submat(180,424,238,341); // for video 1 @47
////                 newFrame = newFrame.submat(149,419,199,270); // for video 2 @48


//                // for binary dvfs test
//                        if (counter >= 69){ // 69 for video 4
//                            cpuController1.CPU_FreqChange(4);
//                            Log.d("FPS", "detected:");
//                        }
//
//                // for adaptive dvfs test
//                if (counter == 82){
//                cpuController1.CPU_FreqChange(1);
//                    Log.d("FPS", "detected:");
//                }
//                if (counter == 83){
//                    cpuController1.CPU_FreqChange(2);
//                    Log.d("FPS", "detected:");
//                }
//                if (counter >83){
//                    cpuController1.CPU_FreqChange(3);
//                    Log.d("FPS", "detected:");
//                }


                    Imgproc.cvtColor(newFrame, mGray, Imgproc.COLOR_RGBA2GRAY); // Convert to grayscale
//
                    MatOfRect faces = new MatOfRect();
                    if (mAbsoluteFaceSize == 0) {
                        int height = mGray.rows();
                        if (Math.round(height * mRelativeFaceSize) > 0) {
                            mAbsoluteFaceSize = Math.round(height * mRelativeFaceSize);
                        }
                    }
                    if (mCascade != null)
                        mCascade.detectMultiScale(mGray, faces, 1.1, 2, 2, // TODO: objdetect.CV_HAAR_SCALE_IMAGE

                                new Size(mAbsoluteFaceSize, mAbsoluteFaceSize), new Size());
//                Each rectangle in the faces array is a face
//                Draw a rectangle around each face
                    Rect[] facesArray = faces.toArray();
                    for (int i = 0; i < facesArray.length; i++)
                        Core.rectangle(newFrame, facesArray[i].tl(), facesArray[i].br(), FACE_RECT_COLOR, 3);
//                Save image to result folder
//                    Highgui.imwrite("/mnt/sdcard/results/" + files.get(j).getName(), newFrame);
//                Highgui.imwrite("/mnt/sdcard/results/results.png", newFrame);

//                Calculate process time and FPS
                    lTimeEnd = System.currentTimeMillis();
                    Log.d("Time", "used :" + (lTimeEnd - lTimeStart));
                    Log.d("FPS", " :" + (float) (1000.0 / (lTimeEnd - lTimeStart)));

                timeCount = timeCount+(lTimeEnd-lTimeStart);
//
//
////}
                if (counter > 100) {
                    Log.d("Average Time", "used :"+ timeCount/100.0);
                    Log.d("Count", "over:");
                }

        // set target FPS range 10 ~ 20
        // if FPS < 10, then setCPUcontroller up
        // if FPS > 20, then setCPUcontroller down
        //

        // load image folder
//                for (int j = 0; j < files.size(); j++) {
//
//                    Log.d("Files", "FileName:" + files.get(j).getName());
//                    Log.d("detected", "detected:"+ counter);
//                    Mat depthFrame = Highgui.imread("/mnt/sdcard/ImageDataset/depth_large/" + files1.get(j).getName());
//                    Mat newFrame = Highgui.imread("/mnt/sdcard/ImageDataset/resized_color_large/" + files.get(j).getName());
//
//
//                    List<File> files = getListFiles(new File("/mnt/sdcard/ImageDataset/test/"));
//                    List<File> files1 = getListFiles(new File("/mnt/sdcard/ImageDataset/depth_large/"));
//

//


//            Mat rgbIm = Highgui.imread("/Users/scalzom/Desktop/Weak_OD/objectness-release-v2.2/VOCB3DO/KinectColor/img_0836.png");//"/Users/scalzom/Desktop/Weak_OD/objectness-release-v2.2/VOCB3DO/KinectColor/img_0836.png");
//                 FelzenswalbHuttenlocherSegmenter fh = new FelzenswalbHuttenlocherSegmenter(sigma,k,mins);
//        long starttime = System.nanoTime();
//        long elapseTime = System.nanoTime()-starttime;
//        System.out.println("Elapsed time = "+elapseTime/Math.pow(10.0, 9.0));

//                Mat newFrame = depthFrame.submat(180,424,238,341); // for video 1 @47
//                 newFrame = newFrame.submat(149,419,199,270); // for video 2 @48

//                Imgproc.cvtColor(mRgba, mGray, Imgproc.COLOR_RGBA2GRAY); // Convert to grayscale
//                MatOfRect faces = new MatOfRect();
//                if (mAbsoluteFaceSize == 0) {
//                    int height = mGray.rows();
//                    if (Math.round(height * mRelativeFaceSize) > 0) {
//                        mAbsoluteFaceSize = Math.round(height * mRelativeFaceSize);
//                    }
//                }
//                if (mCascade != null)
//                    mCascade.detectMultiScale(mGray, faces, 1.1, 2, 2, // TODO: objdetect.CV_HAAR_SCALE_IMAGE
//                            new Size(mAbsoluteFaceSize, mAbsoluteFaceSize), new Size());
//
////              Each rectangle in the faces array is a face
////              Draw a rectangle around each face
//                Rect[] facesArray = faces.toArray();
//                for (int i = 0; i < facesArray.length; i++)
//                    Core.rectangle(mRgba, facesArray[i].tl(), facesArray[i].br(), FACE_RECT_COLOR, 3);
////                Highgui.imwrite("/mnt/sdcard/results/" + files.get(j).getName(), newFrame);
////                Highgui.imwrite("/mnt/sdcard/results/ KinectScreenshot-Color-07-36-09.png", newFrame);
//
//                if (counter > 82){
//                    cpuController1.CPU_FreqChange(2);// Set the frequency to 1134000 KHz
//                    Log.d("detected", "detected:");
//                }
////}
//                if (counter > 100) {
//                    Log.d("over", "over:");
//                }
//
//                // set target FPS range 10 ~ 20
//                // if FPS < 10, then setCPUcontroller up
//                // if FPS > 20, then setCPUcontroller down



//                  Simple object detection
//                Imgproc.cvtColor(mRgba, mGray, Imgproc.COLOR_RGBA2GRAY); // Convert to grayscale
//
//                MatOfRect faces = new MatOfRect();
//
//                if (mAbsoluteFaceSize == 0) {
//                    int height = mGray.rows();
//
//                    if (Math.round(height * mRelativeFaceSize) > 0) {
//                        mAbsoluteFaceSize = Math.round(height * mRelativeFaceSize);
//                    }
//                }
//                if (mCascade != null)
//                    mCascade.detectMultiScale(mGray, faces, 1.1, 2, 2, // TODO: objdetect.CV_HAAR_SCALE_IMAGE
//                            new Size(mAbsoluteFaceSize, mAbsoluteFaceSize), new Size());
//
//                // Each rectangle in the faces array is a face
//                // Draw a rectangle around each face
//                Rect[] facesArray = faces.toArray();
//                for (int i = 0; i < facesArray.length; i++)
//                    Core.rectangle(mRgba, facesArray[i].tl(), facesArray[i].br(), FACE_RECT_COLOR, 3);

//                Highgui.imwrite("/mnt/sdcard/results/test5.png", newFrame);

//                lTimeEnd = System.currentTimeMillis();
//                Log.d("Time", "used :"+ (lTimeEnd-lTimeStart));














//                                Imgproc.cvtColor(mRgba, mGray, Imgproc.COLOR_RGBA2GRAY);
//
//                                Size sizeRgba = mRgba.size();
//                                int step = 200;
//
//                                rDest.x = 5;
//                                rDest.y = 5;
//                                rDest.width = 400;
//                                rDest.height = 400;
//
//                                for (rDest.x = 5; rDest.x < sizeRgba.width; rDest.x = rDest.x + step ) {
//                                    for (rDest.y = 5; rDest.y < sizeRgba.height; rDest.y = rDest.y + step) {
//                                        mSlidingWindow = mRgba.submat(rDest);
//                                        Core.rectangle(mRgba, rDest.tl(), rDest.br(), colorRed, 3);
//
//
//                                    }
//                                }
//
//                                Mat rgbaInnerWindow, rgbaInnerWindow1, rgbaInnerWindow2;
//                                Mat cellWindow;
//                                MatOfByte status = new MatOfByte();
//                                MatOfFloat err = new MatOfFloat();
//                                MatOfPoint2f nextPts =new MatOfPoint2f();
//                                MatOfPoint2f prevPts =new MatOfPoint2f();
//                                MatOfPoint initial = new MatOfPoint();
//
//                                int rows = (int) sizeRgba.height;
//                                int cols = (int) sizeRgba.width;
//
//
//                                int left = 2*cols / 8;
//                                int top = 2*rows / 8;
//
//                                int width = cols * 1 / 4;
//                                int height = rows * 1 / 4;
//
//                                Core.rectangle(mRgba, facesArray[i].tl(), facesArray[i].br(), colorRed, 3);
//                                int cols = mRgba.cols();
//                                int rows = mRgba.rows();
//                                int xOffset = (mOpenCvCameraView.getWidth() - cols) / 2;
//                                int yOffset = (mOpenCvCameraView.getHeight() - rows) / 2;
//                                int x = (int)event.getX() - xOffset;
//                                int y = (int)event.getY() - yOffset;
//
//
//                                // doing a gaussian blur prevents getting a lot of false hits
//                                Imgproc.GaussianBlur(mGray, mGray, sSize5, 2, 2);
//
//                                iCannyLowerThreshold = 35;
//                                iCannyUpperThreshold = 75;
//
//                                Imgproc.Canny(mGray, mIntermediateMat, iCannyLowerThreshold, iCannyUpperThreshold);
//
//                                Imgproc.cvtColor(mIntermediateMat, mRgba, Imgproc.COLOR_GRAY2BGRA, 4);

        if (bDisplayTitle)
            ShowTitle ("Object Detection", 1, colorGreen);
        break;
    }


    // get the time now in every frame
    lMilliNow = System.currentTimeMillis();

    // update the frame counter
    lFrameCount++;

    if (bDisplayTitle) {
        string = String.format("FPS: %2.1f", (float)(lFrameCount * 1000) / (float)(lMilliNow - lMilliStart));

        ShowTitle (string, 2, colorGreen);
    }

    if (bShootNow) {
        // get the time of the attempt to save a screenshot
        lMilliShotTime = System.currentTimeMillis();
        bShootNow = false;

        // try it, and set the screen text accordingly.
        // this text is shown at the end of each frame until
        // 1.5 seconds has elapsed
        if (SaveImage (mRgba)) {
            sShotText = "SCREENSHOT SAVED";
        }
        else {
            sShotText = "SCREENSHOT FAILED";
        }

    }

    if (System.currentTimeMillis() - lMilliShotTime < 1500)
    ShowTitle (sShotText, 3, colorRed);

    return mRgba;
}

    public boolean onTouchEvent(final MotionEvent event) {

        bShootNow = true;
        return false; // don't need more than one touch event

    }


//    public Mat ThresholdImage (Mat rgbImage, Mat depthImage, double threshold){
//
//        int i,j;
//        for (i = 0; i < depthImage.width(); i++){
//            for (j = 0; j < depthImage.height(); j++){
//                if (depthImage.get(i,j)[0] < threshold)
//                    depthImage.get(i,j)[0] = 255;
//                Log.d("length of depth image is",":"+ depthImage.get(i,j).length);
//
//
//
//            }
//        }
//        return rgbImage;
//    }



    public void DrawCross (Mat mat, Scalar color, Point pt) {
        int iCentreCrossWidth = 24;

        pt1.x = pt.x - (iCentreCrossWidth >> 1);
        pt1.y = pt.y;
        pt2.x = pt.x + (iCentreCrossWidth >> 1);
        pt2.y = pt.y;

        Core.line(mat, pt1, pt2, color, iLineThickness - 1);

        pt1.x = pt.x;
        pt1.y = pt.y + (iCentreCrossWidth >> 1);
        pt2.x = pt.x;
        pt2.y = pt.y  - (iCentreCrossWidth >> 1);

        Core.line(mat, pt1, pt2, color, iLineThickness - 1);

    }




    public Mat getHistogram (Mat mat) {
        Imgproc.calcHist(Arrays.asList(mat), MOIone, new Mat(), mHist, histSize, MOFrange);

        Core.normalize(mHist, mHist);

        return mHist;
    }

    @SuppressLint("SimpleDateFormat")
    public boolean SaveImage (Mat mat) {

        Imgproc.cvtColor(mat, mIntermediateMat, Imgproc.COLOR_RGBA2BGR, 3);

        File path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);

        String filename = "OpenCV_";
        SimpleDateFormat fmt = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss");
        Date date = new Date(System.currentTimeMillis());
        String dateString = fmt.format(date);
        filename += dateString + "-" + iFileOrdinal;
        filename += ".png";

        File file = new File(path, filename);

        Boolean bool = null;
        filename = file.toString();
        bool = Highgui.imwrite(filename, mIntermediateMat);

        //if (bool == false)
        //Log.d("Baz", "Fail writing image to external storage");

        return bool;

    }



    private void ShowTitle (String s, int iLineNum, Scalar color) {
        Core.putText(mRgba, s, new Point(10, (int)(dTextScaleFactor * 60 * iLineNum)),
                Core.FONT_HERSHEY_SIMPLEX, dTextScaleFactor, color, 2);
    }

    private List<File> getListFiles(File parentDir) {
        ArrayList<File> inFiles = new ArrayList<File>();
        File[] files = parentDir.listFiles();
        for (File file : files) {
            if (file.isDirectory()) {
                inFiles.addAll(getListFiles(file));
            } else {
                if(file.getName().endsWith(".png")){
                    inFiles.add(file);
                }
            }
        }
        return inFiles;
    }
}
