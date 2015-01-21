package barry.opencvd2;

/**
 * Created by koray on 1/9/15.
 */
import java.util.*;
import java.math.*;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
//import org.opencv.core.MatOfPoint;
import org.opencv.core.Point;
import org.opencv.core.Size;
import org.opencv.highgui.Highgui;
import org.opencv.imgproc.Imgproc;



public class FelzenswalbHuttenlocherSegmenter {

    public FelzenswalbHuttenlocherSegmenter(){}

    /*Constructor with parameters
     * sigma - amt of blur
     * k threshold
     * minSize minimum allowed component size
     */
    public FelzenswalbHuttenlocherSegmenter(float sigma, float k, int minSize){
        this.sigma = sigma;
        this.k = k;
        this.minSize = minSize;
    }

    private float diff(Mat image, Point p1, Point p2)
    {
        float sum = 0;

        double[] d1 = image.get((int)p1.y,(int) p1.x);
        double[] d2 = image.get((int)p2.y,(int) p2.x);
        // System.out.println(image.channels());
        for(int b = 0; b<image.channels();b++)
        {

            double dt = d1[b]-d2[b];
            sum += dt*dt;
        }

        return sum;
    }

    private float diff(List<Mat> image, Point p1, Point p2)
    {
        float sum = 0;

        double[] d1;
        double[] d2;
        //String tmpN; //= "img_";
        // System.out.println(image.channels());
        for(int b = 0; b<image.size();b++)
        {
            d1 = image.get(b).get((int)p1.y,(int) p1.x);
            d2 = image.get(b).get((int)p2.y,(int) p2.x);
            //tmpN = "img_"+b+".png";
            //Highgui.imwrite(tmpN,image.get(b));
            double dt = d1[0]-d2[0];
            sum += dt*dt;
        }

        return (float) Math.sqrt(sum);
    }


    public Mat segmentImage(Mat image)
    {
        int width = image.width();
        int height = image.height();
        List<Mat> smoothc = new ArrayList<Mat>();
        Core.split(image, smoothc);
        //Imgproc.cvtColor(image, smooth, Imgproc.COLOR_RGB2GRAY);//convertToGray(image);//image.clone();

        int len =((int)Math.ceil(sigma*4.0)) + 1;
        //System.out.println(len);
        //Imgproc.GaussianBlur(image, smoothr, new Size(len,len), sigma);
        Imgproc.GaussianBlur(smoothc.get(0), smoothc.get(0), new Size(len,len), sigma);
        Imgproc.GaussianBlur(smoothc.get(1), smoothc.get(1), new Size(len,len), sigma);
        Imgproc.GaussianBlur(smoothc.get(2), smoothc.get(2), new Size(len,len), sigma);

        //build graph
        System.out.println("Made before edges");
        List<SimpleWeightedEdge> edges = new ArrayList<SimpleWeightedEdge>();
        float w;
        Point p1 = new Point(0,0);
        Point p2 = new Point(0,0);
        for(int x =0; x<width; x++)
        {
            for(int y=0; y<height; y++)
            {
                if(x <width-1)
                {
                    p1.x = x;
                    p1.y = y;
                    p2.x = x+1;
                    p2.y = y;
                    w = diff(smoothc,p1,p2);
                    SimpleWeightedEdge e = new SimpleWeightedEdge(x,y,x+1,y,w,image.height());
                    edges.add(e);
                }

                if(y<height-1)
                {
                    p1.x = x;
                    p1.y = y;
                    p2.x = x;
                    p2.y = y+1;
                    w = diff(smoothc,p1,p2);
                    SimpleWeightedEdge e = new SimpleWeightedEdge(x,y,x,y+1,w,image.height());
                    edges.add(e);

                }

                if((x<width-1)&&(y<height-1))
                {
                    p1.x = x;
                    p1.y = y;
                    p2.x = x+1;
                    p2.y = y+1;
                    w = diff(smoothc,p1,p2);
                    SimpleWeightedEdge e = new SimpleWeightedEdge(x,y,x+1,y+1,w,image.height());
                    edges.add(e);

                }

                if((x<width-1)&&(y>0))
                {
                    p1.x = x;
                    p1.y = y;
                    p2.x = x+1;
                    p2.y = y-1;
                    w = diff(smoothc,p1,p2);
                    SimpleWeightedEdge e = new SimpleWeightedEdge(x,y,x+1,y-1,w,image.height());
                    edges.add(e);

                }
                //System.out.println("X value = ");
                //System.out.println(x);
                //System.out.print(" of ");
                //System.out.println(width);
            }
            //System.out.println("Y value = ");
            //System.out.println(y);
            //System.out.print(" of ");
            //System.out.println(height);
        }// end of loop to add edges to the list
        System.out.println("Made after edges");
        DisjointSetForest u = segmentGraph(width*height,edges,k);
        System.out.println("Made after Graph");
        int tmpa;
        int tmpb;
        //post process small components
        for(int i = 0; i<edges.size();i++)
        {
            tmpa = u.find(edges.get(i).a);
            tmpb = u.find(edges.get(i).b);

            if((tmpa!=tmpb) && ((u.getSize(tmpa)<minSize)||(u.getSize(tmpb)<minSize)))
                u.join(tmpa,tmpb);

        } // end of combining small components

        int[][] colors = new int[width*height][3];
        Random rg = new Random();
        for(int i = 0; i<width*height;i++)
        {
            colors[i][0] = rg.nextInt(255);
            colors[i][1] = rg.nextInt(255);
            colors[i][2] = rg.nextInt(255);

        }

        //now need to analyze DisjointSetForest Value
        Mat ccs = new Mat(height,width,CvType.CV_32FC3);
        float[] tmpc = new float[3];
        for(int y=0; y<height; y++)
        { for(int x=0; x<width; x++ )
        {
            int comp = u.find(y+x*height);
            tmpc[0] = (float)colors[comp][0];
            tmpc[1] = (float)colors[comp][1];
            tmpc[2] = (float)colors[comp][2];
            ccs.put(y,x,tmpc);
        }
        }





        return ccs;

    }

    protected DisjointSetForest segmentGraph(int numV, List<SimpleWeightedEdge> edges, float c)
    {
        DisjointSetForest u = new DisjointSetForest(numV);
        System.out.println("Made Before Sort");
        Collections.sort(edges,SimpleWeightedEdge.Comparators.WEIGHTED_EDGE_COMPARATOR);
        System.out.println("Made After Sort");
        float[] thresholds = new float[numV];
        for(int t=0; t<numV;t++)
            thresholds[t] =  c / (float) 1.0;


		/*for(SimpleWeightedEdge edge: edges){
		 System.out.print("Weight = ");
		 System.out.println(edge.weight);
		}*/
        System.out.println("Made Before loop through edges");

        for(SimpleWeightedEdge edge: edges){
            //System.out.println(edge.a);
            //System.out.println(edge.b);
            int a = u.find(edge.a);
            int b = u.find(edge.b);


            if(a!=b)
            {
                if( edge.weight <= thresholds[a] && edge.weight <= thresholds[b] ) {
                    u.join( a, b );
                    a = u.find( a );
                    thresholds[a] = (float) edge.weight + ((float) c / (float)u.getSize( a ));
                }

            }
        }
        System.out.println("Made After loop through edges");
        return u;

    }

    public class DisjointSetForest{

        public DisjointSetForest(int numV){

            numVertices = numV;
            parents = new ArrayList<Integer>();
            rank = new ArrayList<Integer>();
            size = new ArrayList<Integer>();
            for(int i = 0; i <numVertices; i++)
            {
                parents.add(i, new Integer(i));
                rank.add(i, new Integer(0));
                size.add(i, new Integer(1));

            }

        }


        public int find(int x)
        {
            //Integer y = new Integer(x);
            int y = x;

            //while(!y.equals(parents.get(y)))
            //System.out.print(parents.get(y).intValue());
            //System.out.print(" ");
            //System.out.println(y);
            while(y != parents.get(y).intValue())//!= parents.get(y)
            {
                //System.out.print(parents.get(y).intValue());
                //System.out.print(" ");
                //System.out.println(y);
                y = parents.get(y).intValue();
            }
            parents.set(x, new Integer(y));
            return y;

        }

        public int getSize(int a)
        {
            return size.get(a);

        }

        void join(int x, int y)
        {
            if(rank.get(x) >rank.get(y))
            {
                parents.set(y, new Integer(x));
                size.set(x, new Integer(size.get(x)+size.get(y)));

            }
            else{
                parents.set(x, new Integer(y));
                size.set(y, new Integer(size.get(y)+size.get(x)));

                if(rank.get(x)==rank.get(y))
                    rank.set(y, new Integer(rank.get(y)+1));

            }
            numVertices = numVertices -1;
        }
        private
        int numVertices;
        List<Integer> parents;
        List<Integer> rank;
        List<Integer> size;


    }

    public static class SimpleWeightedEdge implements Comparable<SimpleWeightedEdge>{

        public static class Comparators{
            public static final Comparator<SimpleWeightedEdge> WEIGHTED_EDGE_COMPARATOR = new Comparator<SimpleWeightedEdge>() {
                @Override
                public int compare(SimpleWeightedEdge o1, SimpleWeightedEdge o2) {
                    //int rv;
                    if (o1.weight - o2.weight < 0)
                        return -1;
                    else if (o2.weight -o1.weight >0)
                        return  1;
                    else
                        return 0;

                    //return rv;  // This will work because age is positive integer
                }


                //Read more: http://javarevisited.blogspot.com/2014/01/java-comparator-example-for-custom.html#ixzz3NLVpEIp9

            };
        }
        @Override
        public int compareTo(SimpleWeightedEdge o)
        {
            return Comparators.WEIGHTED_EDGE_COMPARATOR.compare(this, o);
        }
        public SimpleWeightedEdge(){}
        public SimpleWeightedEdge(int x, int y, int x1, int y1,float w, int width)
        {


            fromx = x;
            fromy = y;//new Point(x,y);
            tox = x1;
            toy = y1;
            //to = new Point(x1,y1);

            weight = w;

            a = y+x*width;
            b = y1+x1*width;


        }



        public
        int fromx;
        int fromy;
        int tox;
        int toy;
        int a;
        int b;
        float weight;

    }

    private
    float sigma;
    float k;
    int minSize;

//    public static void main(String[] args)
//    {
//        System.out.println(System.getProperty("java.library.path"));
//        System.out.println("/Users/scalzom/Desktop/WACV2015/SmartphoneOD/opencv-2.4.9/opencv-2.4.9/lib");
//        //System.loadLibrary("/Users/scalzom/Desktop/WACV2015/SmartphoneOD/opencv-2.4.9/opencv-2.4.9/lib");
//        //System.load("/Users/scalzom/Desktop/WACV2015/Smartphone OD/opencv-2.4.9/opencv-2.4.9/lib/libopencv_highgui.2.4.9.dylib");
//        System.loadLibrary( Core.NATIVE_LIBRARY_NAME );
//
//        float sigma = (float)0.8;
//        float k = 500;
//        int mins = 500;
//        Mat rgbIm = Highgui.imread("/Users/scalzom/Desktop/Weak_OD/objectness-release-v2.2/VOCB3DO/KinectColor/img_0836.png");//"/Users/scalzom/Desktop/Weak_OD/objectness-release-v2.2/VOCB3DO/KinectColor/img_0836.png");
//        Mat segIm = new Mat(rgbIm.height(),rgbIm.width(),CvType.CV_32FC3);
//        FelzenswalbHuttenlocherSegmenter fh = new FelzenswalbHuttenlocherSegmenter(sigma,k,mins);
//        long starttime = System.nanoTime();
//        segIm = fh.segmentImage(rgbIm);
//        long elapseTime = System.nanoTime()-starttime;
//        System.out.println("Elapsed time = "+elapseTime/Math.pow(10.0, 9.0));
//
//        Highgui.imwrite("./img_seg.png",segIm);
//    }

}