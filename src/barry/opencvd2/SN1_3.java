package barry.opencvd2;

import org.opencv.core.*;
import org.opencv.highgui.Highgui;
import org.opencv.imgproc.Imgproc;

public class SN1_3 {
    public SN1_3(Mat src, Mat h1, Mat h2) {
        this.H = src.rows();
        this.W = src.cols();
        runSurfaceNormal(src, h1, h2);
    }


    int H, W;

    private void runSurfaceNormal(Mat A, Mat h1, Mat h2) {
        A.convertTo(A, CvType.CV_32FC1);
        h1.convertTo(h1, CvType.CV_32FC1);
        h2.convertTo(h2, CvType.CV_32FC1);
        // Calculate y-direction pixel slope

//      Mat mat = Mat.eye( 3, 3, CvType.CV_8UC1 );
//      Mat mat1=mat.submat(1,2,0,3);
//      Scalar D=new Scalar(18);
//      Core.multiply(mat, D, mat);
//      System.out.println( "mat1 = " + mat.dump() );

//        System.out.println("H:"+H+" W:"+W);

        Mat Up_pixel1=A.submat(0,H-4,0,W-2);
        Mat Up_pixel2=A.submat(0,H-4,1,W-1);
        Mat Up_pixel3=A.submat(0,H-4,2,W);
        Mat Up_pixel4=A.submat(1,H-3,0,W-2);
        Mat Up_pixel5=A.submat(1,H-3,1,W-1);
        Mat Up_pixel6=A.submat(1,H-3,2,W);
        Mat Dn_pixel1=A.submat(3,H-1,0,W-2);
        Mat Dn_pixel2=A.submat(3,H-1,1,W-1);
        Mat Dn_pixel3=A.submat(3,H-1,2,W);
        Mat Dn_pixel4=A.submat(4,H,0,W-2);
        Mat Dn_pixel5=A.submat(4,H,1,W-1);
        Mat Dn_pixel6=A.submat(4,H,2,W);
        Mat col_dif_sub=new Mat();
        Core.add(Up_pixel1, Up_pixel2, col_dif_sub);
        Core.add(col_dif_sub, Up_pixel3, col_dif_sub);
        Core.add(col_dif_sub, Up_pixel4, col_dif_sub);
        Core.add(col_dif_sub, Up_pixel5, col_dif_sub);
        Core.add(col_dif_sub, Up_pixel6, col_dif_sub);
        Core.subtract(col_dif_sub, Dn_pixel1, col_dif_sub);
        Core.subtract(col_dif_sub, Dn_pixel2, col_dif_sub);
        Core.subtract(col_dif_sub, Dn_pixel3, col_dif_sub);
        Core.subtract(col_dif_sub, Dn_pixel4, col_dif_sub);
        Core.subtract(col_dif_sub, Dn_pixel5, col_dif_sub);
        Core.subtract(col_dif_sub, Dn_pixel6, col_dif_sub);

        Mat Ad_row=Mat.zeros(2,W-2,CvType.CV_32FC1);
        col_dif_sub.push_back(Ad_row);
        Ad_row.push_back(col_dif_sub);
        Mat Ad_row_trans=new Mat();
        Core.transpose(Ad_row, Ad_row_trans);
        Mat Ad_col=Mat.zeros(1,H,CvType.CV_32FC1);
        Ad_row_trans.push_back(Ad_col);
        Ad_col.push_back(Ad_row_trans);
        Mat col_dif=new Mat();
        Core.transpose(Ad_col, col_dif);


        Mat Show1=Mat.zeros(H,W,CvType.CV_32FC1);

////10-800
        double th1=800;
        double m1=800;
        int type1=4;
        double th2=10;
        double m2=1;
        int type2=1;
        double xx;
        xx=Imgproc.threshold(A, Show1, th1, m1, type1);
        xx=Imgproc.threshold(Show1, Show1, th2, m2, type2);

////        for (int i=1;i<=col_dif.rows();i++){
////            for (int j=1;j<=col_dif.cols();j++){
//////                System.out.print(" ,"+col_dif.get(i-1,j-1)[0]);
////                if (col_dif.get(i-1,j-1)[0]>10 && col_dif.get(i-1,j-1)[0]<800){
////                    Show1.put(i-1, j-1, G1);
////                }
////            }
//////            System.out.println(" ");
////        }
////        Highgui.imwrite("./result1_2Ori.png", Show1);


//        Mat Result1=Mat.zeros(W-40,W,CvType.CV_32FC1);
//        Mat ZeroMat1=Mat.zeros(W-40,W,CvType.CV_32FC1);
//        Core.gemm(h1, Show1, 1, ZeroMat1, 0, Result1, 0);
//        Mat Ad_rows=Mat.zeros(20,W, CvType.CV_32FC1);
//        Ad_rows.push_back(Result1);
//        Result1=Ad_rows.clone();

//        Mat Result2=Mat.zeros(20,W,CvType.CV_32FC1);
//        Mat ZeroMat2=Mat.zeros(20,W,CvType.CV_32FC1);
//        Mat Show1_sub=Show1.submat(H-20,H,0,W);
//        Core.gemm(Show1_sub,h2, 1, ZeroMat2, 0, Result2, 0);
//        Result1.push_back(Result2);
//
//
//
////        Mat Show3=Mat.zeros(Result1.rows(),Result1.cols(),CvType.CV_8UC1);
////        double[] G3=new double[3];
////        G3[0]=255;
////        G3[1]=255;
////        G3[2]=255;
////        for (int i=1;i<=Result1.rows();i++){
////            for (int j=1;j<=Result1.cols();j++){
////                if (Result1.get(i-1,j-1)[0]>28){
////                    Show3.put(i-1, j-1, G3);
////                }
////                else{
////                    Show3.put(i-1, j-1, G1);
////                }
////
////                // System.out.print(Show3.get(i-1,j-1)[0]+", ");
////            }
////            //System.out.println();
////        }
////        Highgui.imwrite("./result.png", Show3);




        // make erosion and expansion
//        Mat Col_dil=Col_sum.clone();
//        Col_dil.convertTo(Col_dil, A.type());
//        Mat SNorm_dil=new Mat(Col_dil.rows(),Col_dil.cols(),Col_dil.type());
//
//        int dilation_row_size=1;
//        int dilation_col_size=1;
//        Mat element = Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(2*dilation_row_size + 1, 2*dilation_col_size+1));
//        Imgproc.dilate(Col_dil, SNorm_dil, element);
//
//
//        Mat SNorm_erodil=SNorm_dil.clone();
//        int erosion_row_size=1;
//        int erosion_col_size=1;
//        Mat element1 = Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(2*erosion_row_size + 1, 2*erosion_col_size+1));
//
//        Imgproc.erode(SNorm_erodil, SNorm_dil, element1);




//        Mat Col_ero=Col_sum.clone();
//        Col_ero.convertTo(Col_ero, A.type());
//        Mat SNorm_ero=new Mat(Col_ero.rows(),Col_ero.cols(),Col_ero.type());
//        Mat SNorm_erodil=SNorm_ero.clone();
//
//        int erosion_row_size=0;
//        int erosion_col_size=0;
//        Mat element1 = Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(2*erosion_row_size + 1, 2*erosion_col_size+1));
//        Imgproc.erode(Col_ero, SNorm_ero, element1);
//
//        int dilation_row_size=1;
//        int dilation_col_size=1;
//        Mat element = Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(2*dilation_row_size + 1, 2*dilation_col_size+1));
//        Imgproc.dilate(SNorm_ero, SNorm_erodil, element);

    }



//    public static void main(String[] args) {
//        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
//        Mat src = Highgui.imread("./KinectScreenshot-Depth-12-36-11.png", Highgui.CV_LOAD_IMAGE_ANYDEPTH);
//        Mat H1 = Highgui.imread("./H1.png");
//        Imgproc.cvtColor(H1, H1, Imgproc.COLOR_RGB2GRAY);
//        Mat H2 = Highgui.imread("./H2.png");
//        Imgproc.cvtColor(H2, H2, Imgproc.COLOR_RGB2GRAY);
//        //Highgui.imwrite("./input.png", H1);
//        long starttime = System.nanoTime();
//        SN1_3 SN;
//        SN = new SN1_3(src, H1 ,H2);
//        long elapseTime = System.nanoTime() - starttime;
//        System.out.println(elapseTime / 1000000);
//    }
};