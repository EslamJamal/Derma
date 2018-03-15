package com.derma.melanoma;

import java.util.ArrayList;
import java.util.List;

import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;
import org.opencv.imgproc.Moments;

/**
 * Class to extract the symmetry feature from the image
 * @author Mazen
 *
 */
public class Symmetry
{
    public static void Assymetry(Mat Assymetry)
    {
        Scalar color = new Scalar(255,255,255);
        
        Scalar color2 = new Scalar ( 0 ,255,0);
        
        // ********************************Find Center of Mass / Contours
        // present********************************
        int largest_contour_index = 0;
        List<MatOfPoint> contours = new ArrayList<MatOfPoint>();
        Mat hierarchy = new Mat();
       // Imgproc.cvtColor(Assymetry, Assymetry, Imgproc.COLOR_RGB2GRAY);
        Imgproc.findContours(Assymetry, contours, hierarchy,
                Imgproc.RETR_CCOMP, Imgproc.CHAIN_APPROX_NONE);
     // find the largest contour
        double largestArea = 0;
        int contourIndx = 0;
        for( int i = 0; i< contours.size(); i++ )
        {
            double area = Imgproc.contourArea( contours.get(i),false); 
            if(area > largestArea)
            {
                largestArea = area;
                contourIndx = i;               
            }
        }
        MatOfPoint cnt = contours.get(contourIndx);
        Moments M = Imgproc.moments(cnt);
        double Cx = (int) (M.get_m10() / M.get_m00());
        double Cy = (int) (M.get_m01() / M.get_m00());
        System.out.println("Cx = " + Cx);
        System.out.println("Cy = " + Cy);
        Point p = new Point(0, 0);
        Point cen = new Point(Cx, Cy);
        Rect bounding_rect = Imgproc.boundingRect(contours.get(contourIndx));
        Imgproc.drawContours(Assymetry, contours, contourIndx, color,
                -1, 8, hierarchy, 5, p);
        Core.rectangle(Assymetry, bounding_rect.tl(), bounding_rect.br(),
                color, 2, 8, 0);

        // ********************************Find Center of Mass / Contours
        // present********************************

        // ********************************Approximating Radius of
        // contour********************************
        int width = bounding_rect.width;
        int height = bounding_rect.height;
        int length = 0;

        if (width > height)
        {
            length = width;
        } else
        {
            length = height;
        }

        System.out.println("height = " + height);
        System.out.println("width = " + width);
        // ********************************Approximating Radius of
        // contour********************************

        // ********************************Approximating total length of
        // contour*****************************

        Point farthest_perimeter_point = new Point();
        MatOfPoint2f contour2f = new MatOfPoint2f(contours.get(contourIndx).toArray());
        MatOfPoint2f approx = new MatOfPoint2f();

        double approxDistance = Imgproc.arcLength(contour2f, true);
        double epsilon = 0.05 * approxDistance;
        Imgproc.approxPolyDP(contour2f, approx, epsilon, true);
        double approxDistance1 = Imgproc.arcLength(approx, true) - 50;
        int approxDistance2 = (int) Math.round(approxDistance1);

        System.out.println("Contour length = " + approxDistance1);
        Point[] contourpoints = contours.get(contourIndx).toArray();
        System.out.println("Contour points x = " + contourpoints[contourIndx].x);
        System.out.println("Contour points y = " + contourpoints[contourIndx].y);
        Point check = new Point(contourpoints[0].x, contourpoints[contourIndx].y);

        // ********************************Approximating total length of
        // contour*****************************

        // ********************************Finding Maximum Diameter of the
        // lesion***************************************
        double[] temp;
        double slope = 0.0;
        double MaxDistance = 0;
        int maxJ = 0;
        System.out.println("aprox = " + approxDistance2);

        for (int j = 0; j < (approxDistance1); j++)
        {
            Point second_intersection_point = new Point();
            double x_difference = contourpoints[j].x - cen.x;
            double y_difference = contourpoints[j].y - cen.y;
            double sqrt_value = (x_difference * x_difference)
                    + (y_difference * y_difference);
            double distaneBetweenPoints = Math.sqrt(sqrt_value);
            second_intersection_point.x = cen.x + (cen.x - contourpoints[j].x)
                    / distaneBetweenPoints * (length / 8);
            second_intersection_point.y = cen.y + (cen.y - contourpoints[j].y)
                    / distaneBetweenPoints * (length / 8);
            int secondy = (int) Math.round(second_intersection_point.y);
            int secondx = (int) Math.round(second_intersection_point.x);
            temp = Assymetry.get(secondy, secondx);

            while (temp[0] == 255)
            {
                double newx_difference = cen.x - second_intersection_point.x;
                double newy_difference = cen.y - second_intersection_point.y;
                double newsqrt_value = (newx_difference * newx_difference)
                        + (newy_difference * newy_difference);
                double newdistaneBetweenPoints = Math.sqrt(newsqrt_value);

                second_intersection_point.x = (second_intersection_point.x + (second_intersection_point.x - cen.x)
                        / newdistaneBetweenPoints * (1));
                second_intersection_point.y = (second_intersection_point.y + (second_intersection_point.y - cen.y)
                        / newdistaneBetweenPoints * (1));
                int secondyy = (int) Math.round(second_intersection_point.y);
                int secondxx = (int) Math.round(second_intersection_point.x);
                temp = Assymetry.get(secondyy, secondxx);
            }
            double xFinal_difference = contourpoints[j].x
                    - second_intersection_point.x;
            double yFinal_difference = contourpoints[j].y
                    - second_intersection_point.y;
            double FinalSqrt_value = (xFinal_difference * xFinal_difference)
                    + (yFinal_difference * yFinal_difference);
            double finalDistaneBetweenPoints = Math.sqrt(FinalSqrt_value);

            if (finalDistaneBetweenPoints > MaxDistance)
            {
                MaxDistance = finalDistaneBetweenPoints;
                maxJ = j;
                farthest_perimeter_point = second_intersection_point;
                slope = y_difference / x_difference;
                System.out.println("farthest intersection y = "
                        + farthest_perimeter_point.y);
                System.out.println("farthest intersection x = "
                        + farthest_perimeter_point.x);
                System.out.println("Max dist = " + MaxDistance);
                System.out.println("slope = " + slope);
                System.out.println("j = " + j);
            }

        }
        double slope_2 = (double) Math.round(slope * 10) / 10;
        double perpendicular_slope = -(1 / slope_2);
        double rounded_perpendicular_slope = (double) Math
                .round(perpendicular_slope * 10) / 10;
        System.out.println("rounded slope = " + slope_2);
        System.out.println("rounded slope 2  = " + rounded_perpendicular_slope);

        // ********************************Finding Maximum Diameter of the
        // lesion***************************************

        // ********************************Finding Maximum Perpendicular
        // Diameter of the lesion*****************************
        int maxZ = 0;

        forloop: for (int z = 0; z < (approxDistance1); z++)
        {
            double perpendicular_x_difference = contourpoints[z].x - cen.x;
            double perpendicular_y_difference = contourpoints[z].y - cen.y;
            double perpendicular_slope_test = perpendicular_y_difference
                    / perpendicular_x_difference;
            double rounded_perpendicular_test = (double) Math
                    .round(perpendicular_slope_test * 10) / 10;

            if (rounded_perpendicular_slope == rounded_perpendicular_test)
            {
                maxZ = z;
                System.out.println("z= " + maxZ);
                break forloop;
            }

        }

        double temp_2[];

        int cenY = (int) Math.round(cen.y);
        int cenX = (int) Math.round(cen.x);
        temp_2 = Assymetry.get(cenY, cenX);
        Point perpendicular_cen = cen;
        while (temp_2[0] == 255)
        {
            double perpendicular_x_difference = contourpoints[maxZ].x
                    - perpendicular_cen.x;
            double perpendicular_y_difference = contourpoints[maxZ].y
                    - perpendicular_cen.y;
            double newsqrt_value = (perpendicular_x_difference * perpendicular_x_difference)
                    + (perpendicular_y_difference * perpendicular_y_difference);
            double newdistaneBetweenPoints = Math.sqrt(newsqrt_value);

            perpendicular_cen.x = (perpendicular_cen.x + (perpendicular_cen.x - contourpoints[maxZ].x)
                    / newdistaneBetweenPoints * (1));
            perpendicular_cen.y = (perpendicular_cen.y + (perpendicular_cen.y - contourpoints[maxZ].y)
                    / newdistaneBetweenPoints * (1));
            int secondyy = (int) Math.round(perpendicular_cen.y);
            int secondxx = (int) Math.round(perpendicular_cen.x);
            temp_2 = Assymetry.get(secondyy, secondxx);

        }

        double perpendicular_x_Final_difference = contourpoints[maxZ].x
                - perpendicular_cen.x;
        double perpendicular_y_Final_difference = contourpoints[maxZ].y
                - perpendicular_cen.y;
        double FinalSqrt_value = (perpendicular_x_Final_difference * perpendicular_x_Final_difference)
                + (perpendicular_y_Final_difference * perpendicular_y_Final_difference);
        double perpendicularDistaneBetweenPoints = Math.sqrt(FinalSqrt_value);
        System.out.println("Max perpendicular dist = "
                + perpendicularDistaneBetweenPoints);

        // ********************************Finding Maximum Perpendicular
        // Diameter of the lesion*****************************

        Core.line(Assymetry, contourpoints[maxZ], perpendicular_cen, color2);
        Core.line(Assymetry, contourpoints[maxJ], farthest_perimeter_point,
                color2);
        Core.circle(Assymetry, cen, 1, color2); // center of mass point
        Core.circle(Assymetry, check, 1, color2);
        System.out.println("farthest intersection y = "
                + farthest_perimeter_point.y);
        System.out.println("farthest intersection x = "
                + farthest_perimeter_point.x);
        System.out.println("perpendicular_cen.x = " + perpendicular_cen.x);
        System.out.println("perpendicular_cen.y = " + perpendicular_cen.y);
        System.out.println("cen.x = " + cen.x);
        System.out.println("cen.y = " + cen.y);
        System.out.println("maxj = " + maxJ);
        Core.circle(Assymetry, farthest_perimeter_point, 1, color);

        Display.displayImage(Assymetry);

    }

}
