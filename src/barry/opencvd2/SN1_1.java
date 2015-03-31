package barry.opencvd2;

/**
 * Created by koray on 2/11/15.
 */

import org.opencv.highgui.Highgui;



        import org.opencv.core.Core;
        import org.opencv.core.CvType;
        import org.opencv.core.Mat;
        import org.opencv.highgui.Highgui;
        import org.opencv.imgproc.Imgproc;


public class SN1_1 {
    public SN1_1(Mat src) {
        this.Src = src;
        this.H = Src.rows();
        this.W = Src.cols();
        this.Ny = new double[H][W];
        runSurfaceNormal(Src);
    }

    Mat Src;
    int H, W;
    double[][] Ny;

    private void runSurfaceNormal(Mat A) {

        // Calculate Integral Image
        double[][] S = new double[H][W];
        S[0][0] = A.get(0, 0)[0];
        for (int j = 2; j <= W; j++) {
            S[0][j - 1] = S[0][j - 2] + A.get(0, j - 1)[0];

        }

        for (int i = 2; i <= H; i++) {
            S[i - 1][0] = S[i - 2][0] + A.get(i - 1, 0)[0];
        }
        for (int i = 2; i <= H; i++) {
            for (int j = 2; j <= W; j++) {
                S[i - 1][j - 1] = S[i - 2][j - 1] + S[i - 1][j - 2]
                        - S[i - 2][j - 2] + A.get(i - 1, j - 1)[0];
            }
        }

        //Calculate y-direction surface normal
        double[][] Out = new double[H][W];
        for (int i = 1; i <= H; i++) {
            for (int j = 1; j <= W; j++) {
                Out[i - 1][j - 1] = A.get(i - 1, j - 1)[0];
                if (i < 4 || i > H - 2) {
                    Ny[i - 1][j - 1] = 0;
                } else if (j < 4 || j > W - 2) {
                    Ny[i - 1][j - 1] = 0;
                } else {
                    double[][] V = new double[2][3]; // x & y dimensional
                    // vectors
                    double Right_sum = S[i][j + 1] - S[i - 3][j + 1]
                            - S[i][j - 2] + S[i - 3][j - 2];
                    double Left_sum = S[i][j - 1] - S[i - 3][j - 1]
                            - S[i][j - 4] + S[i - 3][j - 4];
                    double V_row_x = 1;
                    double V_row_y = 0;
                    double V_row_z = ((Right_sum - Left_sum) / 9) / 2;
                    double Up_sum = S[i - 1][j] - S[i - 4][j] - S[i - 1][j - 3]
                            + S[i - 4][j - 3];
                    double Down_sum = S[i + 1][j] - S[i - 2][j]
                            - S[i + 1][j - 3] + S[i - 2][j - 3];
                    double V_col_x = 0;
                    double V_col_y = 1;
                    double V_col_z = ((Up_sum - Down_sum) / 9) / 2;
                    V[0][0] = V_row_x;
                    V[0][1] = V_row_y;
                    V[0][2] = V_row_z;
                    V[1][0] = V_col_x;
                    V[1][1] = V_col_y;
                    V[1][2] = V_col_z;
                    Ny[i - 1][j - 1] = V[0][2] * V[1][0] - V[0][0] * V[1][2];
                    if (Ny[i - 1][j - 1] > -5 && Ny[i - 1][j - 1] < -0.2) {
                        Out[i - 1][j - 1] = 255;
                    }
                }
            }
        }

        // make erosion and expansion
        double[][] Ero = new double[H][W]; // erosion
        for (int i = 3; i <= H - 2; i++) {
            for (int j = 3; j <= W - 2; j++) {
                if (Out[i - 1][j - 1] == 255) {
                    Ero[i - 2][j - 2] = 255;
                    Ero[i - 2][j - 1] = 255;
                    Ero[i - 2][j] = 255;
                    Ero[i - 1][j - 2] = 255;
                    Ero[i - 1][j] = 255;
                    Ero[i][j - 2] = 255;
                    Ero[i][j - 1] = 255;
                    Ero[i][j] = 255; // one-range pixels
/*
						Ero[i - 3][j - 3] = 255;
						Ero[i - 3][j - 2] = 255;
						Ero[i - 3][j - 1] = 255;
						Ero[i - 3][j] = 255;
						Ero[i - 3][j + 1] = 255;
						Ero[i - 2][j - 3] = 255;
						Ero[i - 2][j + 1] = 255;
						Ero[i - 1][j - 3] = 255;
						Ero[i - 1][j + 1] = 255;
						Ero[i][j - 3] = 255;
						Ero[i][j + 1] = 255;
						Ero[i + 1][j - 3] = 255;
						Ero[i + 1][j - 2] = 255;
						Ero[i + 1][j - 1] = 255;
						Ero[i + 1][j] = 255;
						Ero[i + 1][j + 1] = 255; // two_range pixels
*/
                }
            }
        }
        double[][] Exp = new double[H][W]; // expansion
        for (int i = 3; i <= H - 2; i++) {
            for (int j = 3; j <= W - 2; j++) {
                Exp[i - 1][j - 1] = 255;
                if (Ero[i - 1][j - 1] != 255) {
                    Exp[i - 2][j - 2] = Out[i - 2][j - 2];
                    Exp[i - 2][j - 1] = Out[i - 2][j - 1];
                    Exp[i - 2][j] = Out[i - 2][j];
                    Exp[i - 1][j - 2] = Out[i - 1][j - 2];
                    Exp[i - 1][j] = Out[i - 1][j];
                    Exp[i][j - 2] = Out[i][j - 2];
                    Exp[i][j - 1] = Out[i][j - 1];
                    Exp[i][j] = Out[i][j]; // one-range pixels

                    Exp[i - 3][j - 3] = Out[i - 3][j - 3];
                    Exp[i - 3][j - 2] = Out[i - 3][j - 2];
                    Exp[i - 3][j - 1] = Out[i - 3][j - 1];
                    Exp[i - 3][j] = Out[i - 3][j];
                    Exp[i - 3][j + 1] = Out[i - 3][j + 1];
                    Exp[i - 2][j - 3] = Out[i - 2][j - 3];
                    Exp[i - 2][j + 1] = Out[i - 2][j + 1];
                    Exp[i - 1][j - 3] = Out[i - 1][j - 3];
                    Exp[i - 1][j + 1] = Out[i - 1][j + 1];
                    Exp[i][j - 3] = Out[i][j - 3];
                    Exp[i][j + 1] = Out[i][j + 1];
                    Exp[i + 1][j - 3] = Out[i + 1][j - 3];
                    Exp[i + 1][j - 2] = Out[i + 1][j - 2];
                    Exp[i + 1][j - 1] = Out[i + 1][j - 1];
                    Exp[i + 1][j] = Out[i + 1][j];
                    Exp[i + 1][j + 1] = Out[i + 1][j + 1]; // two_range pixels
                }
                // System.out.println(Exp[i-1][j-1] + "  ");
            }
        }

//
//
//
        Mat Show=A.clone();
        double[] G=new double[1];
        for (int i=1;i<=H;i++){
            for (int j=1;j<=W;j++){
                G[0]=Exp[i-1][j-1];
                Show.put(i-1, j-1, G);
            }
        }
        Highgui.imwrite("/mnt/sdcard/ImageDataset/test/result000.png", Show);


    }
}
