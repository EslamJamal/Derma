package com.derma.melanoma;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.highgui.*;
import org.opencv.imgproc.Imgproc;
import java.util.*;

import org.opencv.utils.Converters;

/**
 * Class to find the contour in the image and calculate its regularity.
 * @author Ahmad 
 *
 */
public class Regularity
{
    private Mat _srcImg = null;
    private Mat _contourImg = null;
    private ArrayList<Point> _contourPoints = null;
    
    /**
     * Constructor.
     * @param fileName: name of the image file.
     */
    public Regularity(Mat img)
    {
        _srcImg = img;

        // _srcImg = Highgui.imread(fileName);
        // if (_srcImg.empty() == true)
        // {
        // throw new
        // IllegalArgumentException("No image found at the given location!");
        // }
    }
    
    /**
     * Return the original image.
     */
    public Mat getImage()
    {
        return _srcImg;
    }

    /**
     * Find contour coordinates in the loaded image.
     * @return: ArrayList of the contour coordinates.
     */
    public ArrayList<Point> findContour()
    {
        if (null == _srcImg)
        {
            throw new IllegalArgumentException ("Image is null, cannot find contours.");
        }
        
        List<MatOfPoint> contours = new Vector<MatOfPoint>();
        Mat hierarchy = new Mat();
        //Imgproc.cvtColor(_srcImg, _srcImg, Imgproc.COLOR_RGB2GRAY);
        Imgproc.findContours(_srcImg, contours, hierarchy, Imgproc.RETR_EXTERNAL,
                Imgproc.CHAIN_APPROX_NONE);
        _contourPoints = new ArrayList<>();
        
        double largestArea = 0;
        int contourIndx = 0;
        // find the largest contour
        for( int i = 0; i< contours.size(); i++ )
        {
            double area = Imgproc.contourArea( contours.get(i),false); 
            if(area > largestArea)
            {
                largestArea = area;
                contourIndx = i;               
            }
        }
        
        Converters.Mat_to_vector_Point(contours.get(contourIndx), _contourPoints);
        
        _contourImg = _srcImg.clone();
        Scalar color = new Scalar(255, 255, 255);
        Imgproc.drawContours(_contourImg, contours, contourIndx, color);
        
        return _contourPoints;
    }
    
    /**
     * Return ArrayList of the contour points. 
     */
    public ArrayList<Point> getContourPoints()
    {
        return _contourPoints;
    }
    
    /**
     * Return contour points as an image.
     */
    public Mat getContourImage()
    {
        return _contourImg;
    }
    
    /**
     * Calculate regularity index of the contour  
     * @return: Regularity index proportional to the regularity of the contour.
     * The function returns zero if the contour is a perfect circle and the returned 
     * value increases proportional to the irregularity of the contour.
     */
    public double calculateRegularity()
    {
        double sigma = 0;
        int count = -1;
        int N = _contourPoints.size();
        double  h = 1.0/ (double)(N);
        
        ArrayList<Point> convContour = null;
        
        while (count != 0)
        {
            sigma = sigma + .01;
            count = 0;
            
            // Define the gaussian function
            double[] gauss = new double[N];
            int indx = 0;
            for (double t = 0; t < 1 - h; t += h)
            {
                double gaussValue = Math.exp((-Math.pow((t - 0.5), 2))
                        / (2 * Math.pow(sigma, 2)));
                gauss[indx] = (gaussValue);
                indx++;
            }

            // convolve with the Gaussian function
            convContour = cirConvovle(_contourPoints, gauss);
            
            // calculate curvature
            double[] K = curvature(convContour);

            // detect inflection points
            for (int i = 0; i < N - 2; i++)
            {
                if (Math.signum(K[i]) != Math.signum(K[i + 1]))
                {
                    count++; // inflection point detected -> increase the counter
                }
            }

        } // while loop end

        // rotate to cancel the effect of the shift in the Gaussian function
        Collections.rotate(convContour, (0 - (int) Math.floor(N / 2)));

        // calculate RMSD between the original and smoothed contours
        double RMSD = calculateRMSD(_contourPoints, convContour);
        
        //System.out.println(RMSD);
        
        // calculate the regularity index similar to the definition in the paper
        double regIndex = RMSD * sigma / contourLength(_contourPoints);
        
        return regIndex;
       
    }
    
    /**
     * Private function to perform circular convolution.
     * @param origArray original array 
     * @param convFilter filter array 
     * @return convolved array
     */
    private ArrayList<Point> cirConvovle(final ArrayList<Point> origArray, final double[] convFilter)
    {
        int length = origArray.size();
        int subLength = convFilter.length;

        if (subLength > length)
        {
            throw new IllegalArgumentException("The length of the filter array is larger than the original array, but it must be smaller");
        }
        
        // extend the original array by the length of the filter array
        ArrayList<Point> origExtention = new ArrayList<Point>();
        for (int i=0; i < subLength; i++)
        {
            origExtention.add(i, origArray.get(i));
        }
        ArrayList<Point> origExtended = new ArrayList<>(length + subLength);
        origExtended.addAll(origArray);
        origExtended.addAll(length, origExtention);

        // calculate the summation of the filter array elements to cancel the scaling effect later
        double filterSum = sum(convFilter);

        // perform the convolution
        ArrayList<Point> convolutionResult = new ArrayList<>(length);
        for (int i = 0; i < length; i++)
        {
            double valueX = 0;
            double valueY = 0;
            for (int j = 0; j < subLength; j++)
            {
                valueX += convFilter[j] * origExtended.get(i + j).x;
                valueY += convFilter[j] * origExtended.get(i + j).y;

            }
            
            // correct the scale
            valueX /= filterSum;
            valueY /= filterSum;
            
            convolutionResult.add(i, new Point(valueX,valueY));
        }
        
        return convolutionResult;

    }
    
    /**
     * Private function to calculate the curvature at every point on a given contour
     * @param Contour for which the curvature will be calculated 
     * @return: double array holding the curvature at every contour point
     */
    private double[] curvature(ArrayList<Point> convContour)
    {
        int arrayLength = convContour.size();
        // 1st & 2nd Derivative and Curvature
        double[] X1 = new double[arrayLength];
        double[] X2 = new double[arrayLength];
        double[] Y1 = new double[arrayLength];
        double[] Y2 = new double[arrayLength];
        // first and last elements
        X1[0] = (convContour.get(1).x - convContour.get(arrayLength - 1).x) / 2.0;
        Y1[0] = (convContour.get(1).y - convContour.get(arrayLength - 1).y) / 2.0;
        X2[0] = (convContour.get(1).x - 2 * convContour.get(0).x + convContour.get(arrayLength - 1).x);
        Y2[0] = (convContour.get(1).y - 2 * convContour.get(0).y + convContour.get(arrayLength - 1).y);
        
        X1[arrayLength - 1] = (convContour.get(0).x - convContour.get(arrayLength - 2).x) / 2.0;
        Y1[arrayLength - 1] = (convContour.get(0).y - convContour.get(arrayLength - 2).y) / 2.0;
        X2[arrayLength - 1] = (convContour.get(0).x - 2 * convContour.get(arrayLength - 1).x + convContour.get(arrayLength - 2).x);
        Y2[arrayLength - 1] = (convContour.get(0).y - 2 * convContour.get(arrayLength - 1).y + convContour.get(arrayLength - 2).y);
        
        // rest of the array
        for (int i = 1; i <= arrayLength - 2; i++)
        {
            X1[i] = (convContour.get(i + 1).x - convContour.get(i - 1).x) / 2.0;
            Y1[i] = (convContour.get(i + 1).y - convContour.get(i - 1).y) / 2.0;
            X2[i] = (convContour.get(i + 1).x - 2 * convContour.get(i).x + convContour.get(i - 1).x);
            Y2[i] =  (convContour.get(i + 1).y - 2 * convContour.get(i).y + convContour.get(i - 1).y);
        }

        // calculate curvature
        double[] nom = new double[arrayLength];
        double[] denom = new double[arrayLength];
        double[] K = new double[arrayLength];
        for (int i = 0; i < arrayLength; i++)
        {
            nom[i] = (X1[i] * Y2[i]) - (X2[i] * Y1[i]);
            denom[i] = Math.pow((Math.pow(X1[i], 2) + Math.pow(Y1[i], 2)), 1.5);
            K[i] = (nom[i] / denom[i]);
        }
        
        return K;
        
    }

    /**
     * private function to compute sum of the array elements
     * @param double array
     * @return: sum of array elements
     */
    private double sum(double[] list)
    {
        double total = 0;
        for (int i=0; i<list.length; i++)
        {
            total += list[i];
        }
        return total;
    }
    
    /**
     * private function to compute contour length
     * @param contour points
     * @return length of the array 
     */
    private double contourLength(ArrayList<Point> contour)
    {
        int arrayLength = contour.size();
        double countourLength = 0;
        for (int i=0; i<arrayLength-1; i++)
        {
            double diffX2 = Math.pow(contour.get(i+1).x-contour.get(i).x,2);
            double diffY2 = Math.pow(contour.get(i+1).y-contour.get(i).y,2);
            countourLength += Math.sqrt(diffX2 + diffY2); 
        }
        
        return countourLength;  
    }
    
    /**
     * calculate root mean square distance (RMSD) between corresponding pair points of two contours
     * @param first contour
     * @param second contour
     * @return RMSD between points
     */
    private double calculateRMSD( ArrayList<Point> originalContour, ArrayList<Point> convContour)
    {
        int arrayLength = convContour.size();
        double[] xDifSqr = new double[arrayLength];
        double[] yDifSqr = new double[arrayLength];

        for (int i = 0; i < arrayLength - 1; i++)
        {
            xDifSqr[i] = Math.pow((convContour.get(i).x - originalContour.get(i).x), 2);
            yDifSqr[i] = Math.pow((convContour.get(i).x - originalContour.get(i).y), 2);
        }

        double RMSD = Math.sqrt((sum(xDifSqr) + sum(yDifSqr)) / 2.0);
        
        return RMSD;
    }
    
}