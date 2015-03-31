package barry.opencvd2;

import org.opencv.core.*;
import org.opencv.highgui.Highgui;
import org.opencv.imgproc.Imgproc;

public class SN1_2 {
    public SN1_2(Mat src) {
        this.H = src.rows();
        this.W = src.cols();
        runSurfaceNormal(src);
    }


    int H, W;

    private void runSurfaceNormal(Mat A) {

        // Calculate Integral Image

        Mat S_plus=new Mat(H+1,W+1, CvType.CV_64FC1);
        Mat S=new Mat(H,W, CvType.CV_64FC1);
        Imgproc.integral(A, S_plus);
        S=S_plus.submat(1, H+1, 1, W+1);  //get rid of the 0th row and 0th col
        //submat(i1,i2,j1,j2) start at the original Mat (0,H-1,0,W-1) line i1 and end at the line before i2(not include i2)
        S_plus.release();

        // Calculate y-direction surface normal

//		Mat mat = Mat.eye( 3, 3, CvType.CV_8UC1 );
//		Mat mat1=mat.submat(1,2,0,3);
//		Scalar D=new Scalar(18);
//		Core.multiply(mat, D, mat);
//		System.out.println( "mat1 = " + mat.dump() );

        Mat R0=S.clone();
        Mat R1=R0.submat(4,H-1,5,W);
        Mat R2=R0.submat(1,H-4,5,W);
        Mat R3=R0.submat(4,H-1,2,W-3);
        Mat R4=R0.submat(1,H-4,2,W-3);
        Mat R_sum=new Mat();
        Core.subtract(R1, R2, R_sum);
        Core.subtract(R_sum, R3, R_sum);
        Core.add(R_sum, R4, R_sum);
        R0.release();
        R1.release();
        R2.release();
        R3.release();
        R4.release();

        Mat L0=S.clone();
        Mat L1=L0.submat(4,H-1,3,W-2);
        Mat L2=L0.submat(1,H-4,3,W-2);
        Mat L3=L0.submat(4,H-1,0,W-5);
        Mat L4=L0.submat(1,H-4,0,W-5);
        Mat L_sum=new Mat();
        Core.subtract(L1, L2, L_sum);
        Core.subtract(L_sum, L3, L_sum);
        Core.add(L_sum, L4, L_sum);
        L0.release();
        L1.release();
        L2.release();
        L3.release();
        L4.release();

        Mat Row_sum=new Mat();
        Core.subtract(R_sum, L_sum, Row_sum);
        R_sum.release();
        L_sum.release();


        Mat U0=S.clone();
        Mat U1=U0.submat(3,H-2,4,W-1);
        Mat U2=U0.submat(0,H-5,4,W-1);
        Mat U3=U0.submat(3,H-2,1,W-4);
        Mat U4=U0.submat(0,H-5,1,W-4);
        Mat U_sum=new Mat();
        Core.subtract(U1, U2, U_sum);
        Core.subtract(U_sum, U3, U_sum);
        Core.add(U_sum, U4, U_sum);
        U0.release();
        U1.release();
        U2.release();
        U3.release();
        U4.release();

        Mat D0=S.clone();
        Mat D1=D0.submat(5,H,4,W-1);
        Mat D2=D0.submat(2,H-3,4,W-1);
        Mat D3=D0.submat(5,H,1,W-4);
        Mat D4=D0.submat(2,H-3,1,W-4);
        Mat D_sum=new Mat();
        Core.subtract(D1, D2, D_sum);
        Core.subtract(D_sum, D3, D_sum);
        Core.add(D_sum, D4, D_sum);
        D0.release();
        D1.release();
        D2.release();
        D3.release();
        D4.release();
        S.release();

        Mat Col_sum=new Mat(H-3,W-3,CvType.CV_64FC1);
        Core.subtract(U_sum, D_sum, Col_sum);
        U_sum.release();
        D_sum.release();


//		Mat add_row=Mat.zeros(3,507,CvType.CV_32SC1);	
//		Mat add_col=Mat.zeros(422,3,CvType.CV_32SC1);



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
//        int erosion_row_size=1;
//        int erosion_col_size=1;
//        Mat element1 = Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(2*erosion_row_size + 1, 2*erosion_col_size+1));
//        Imgproc.erode(Col_ero, SNorm_ero, element1);
//
//        int dilation_row_size=2;
//        int dilation_col_size=2;
//        Mat element = Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(2*dilation_row_size + 1, 2*dilation_col_size+1));
//        Imgproc.dilate(SNorm_ero, SNorm_erodil, element);




//        Mat Show=A.clone();     //for Unit8
//        double[] G=new double[3];
//        G[0]=255;
//        G[1]=255;
//        G[2]=255;
//
//        for (int i=1;i<=H-5;i++){
//            for (int j=1;j<=W-5;j++){
//                if (SNorm_erodil.get(i-1,j-1)[0]>3.6 && Col_sum.get(i-1,j-1)[0]<90){
//
//
//                    Show.put(i+2, j+2, G);
//                }
//            }
//        }
//        Highgui.imwrite("/mnt/sdcard/ImageDataset/test/result1_2Ero.png", Show);





//        Mat Show=A.clone();     //for Unit16
//        double[] G=new double[3];
//        G[0]=255;
//        G[1]=255;
//        G[2]=255;
//
//        for (int i=1;i<=H-5;i++){
//            for (int j=1;j<=W-5;j++){
//                if (Col_sum.get(i-1,j-1)[0]>0 && Col_sum.get(i-1,j-1)[0]<90){
//
//
//                    Show.put(i+2, j+2, G);
//                }
//            }
//        }
//        Highgui.imwrite("/mnt/sdcard/ImageDataset/test/G1.png", Show);

    }

//    public static void main(String[] args) {
//        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
//        Mat src = Highgui.imread("./a.png");
//        Mat src_gray = new Mat(src.rows(), src.cols(), CvType.CV_8UC1);
//        // Highgui.imwrite("./Img_src.png",src);
//        Imgproc.cvtColor(src, src_gray, Imgproc.COLOR_RGB2GRAY);
//        long starttime = System.nanoTime();
//        SN1_2 SN;
//        SN = new SN1_2(src_gray);
//        long elapseTime = System.nanoTime() - starttime;
//        System.out.println(elapseTime / 1000000);
//    }
}