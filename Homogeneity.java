package com.derma.melanoma;

import java.util.ArrayList;
import java.util.List;

import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfFloat;
import org.opencv.core.MatOfInt;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;

/**
 * Class to extract gray scale homogeneity feature 
 * @author Mazen
 *
 */
public class Homogeneity
{
    public static void normHistogram(Mat histnorm)
    {

        List<Mat> rgb_planes = new ArrayList<Mat>();
        Mat grayImage = new Mat();
        int imageDepth1 = histnorm.depth();
        System.out.println("depth 0 = " + imageDepth1);
       // Imgproc.cvtColor(histnorm, grayImage, Imgproc.COLOR_RGB2GRAY);
       // Display.displayImage(histnorm);
        Core.split(histnorm, rgb_planes);
        rgb_planes.add(histnorm);

        int height_image = histnorm.height();
        int width_image = histnorm.width();
        int imageSize = height_image * width_image;
        int imageDepth = histnorm.depth();

        System.out.println("depth = " + imageDepth);
        int nbins = 256;
        MatOfInt histsize = new MatOfInt(nbins);
        int[] hist = new int[256];
        MatOfInt test1 = new MatOfInt(1);
        MatOfFloat ranges = new MatOfFloat(0, 256);
        Mat r_hist = new Mat();
        Imgproc.calcHist(rgb_planes, test1, new Mat(), r_hist, histsize, ranges);

        long bin_w = Math.round((double) width_image / 256);
        Mat histImage = new Mat(height_image, width_image, CvType.CV_8UC3);
        Core.normalize(r_hist, r_hist, 0, histImage.rows(), Core.NORM_MINMAX,
                -1, new Mat());

        for (int i = 1; i < 256; i++)
        {

            Point p1 = new Point(bin_w * (i - 1), height_image
                    - Math.round(r_hist.get(i - 1, 0)[0]));
            Point p2 = new Point(bin_w * (i), height_image
                    - Math.round(r_hist.get(i, 0)[0]));
            Core.line(histImage, p1, p2, new Scalar(255, 255, 0), 2, 8, 0);

        }

        double sumOfNormHist = 0;

        for (int c = 0; c < nbins; c++)
        {
            double binval = r_hist.get(c, 0)[0];
            sumOfNormHist += binval;
        }

        System.out.println("sumOfNormHist = " + sumOfNormHist);

        Display.displayImage(histImage);
    }
}
