package com.derma.melanoma;

import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.highgui.Highgui;
import org.opencv.imgproc.Imgproc;

/**
 * Class to segment the lesion in the image
 * @author Eslam
 *
 */
public class Segmentation
{

    Mat _originalImg = null;
    Mat _segmentedImg = null;
    
    public enum SegmType 
    {
        OTSU_THRESHOLD, REGION_GROWING
    }

    public Segmentation(String fileName)
    {
        Mat loadedImg = Highgui.imread(fileName);
        if (loadedImg.empty() == true)
        {
            throw new IllegalArgumentException("No image found at the given location!");
        }
        
        _originalImg = convertToGrayRGB(loadedImg);
            
    }
    

    public static Mat convertToGrayRGB(Mat brgLoadedImage)
    {
        Mat resultImage = new Mat(brgLoadedImage.size(), CvType.CV_8U);
        Mat tempImg = brgLoadedImage.clone();

        Imgproc.cvtColor(brgLoadedImage, tempImg, Imgproc.COLOR_BGR2RGB);

        tempImg.convertTo(resultImage, CvType.CV_8U);

        brgLoadedImage.release();
        brgLoadedImage = null;
        tempImg.release();
        tempImg = null;
        
        return resultImage;
    }

    /**
     * Segments the lesion in the image using sigmoid filter and Otsu threshold.
     * 
     * @return binary image with segmented lesion
     */
    public Mat segmentLesion(SegmType segmType)
    {
        switch (segmType)
        {
        case OTSU_THRESHOLD:
            _segmentedImg = segmentOtsu();
            break;
        case REGION_GROWING:
            _segmentedImg = segmentRG();
            break;
        default:
            segmentOtsu();
        }
        
        return _segmentedImg;
    }
    
    private Mat segmentRG()
    {
        return _segmentedImg;
    }
    
    private Mat segmentOtsu()
    {
        if (null != _originalImg)
        {
            // image variables
            Mat grayImage = new Mat(_originalImg.size(), _originalImg.type());
            Mat blurredImage = new Mat(_originalImg.size(), _originalImg.type());

            // convert image from colored to grayscale
            Imgproc.cvtColor(_originalImg, grayImage, Imgproc.COLOR_RGB2GRAY);

            Rect roi = findRegionOfInterest(grayImage);
            Mat croppedImage = grayImage.submat(roi);
                    
            // size must be odd !!!
            Imgproc.medianBlur(croppedImage, blurredImage, 13);
           
            // apply sigmoid filter to enhance contrast between lesion and
            // background
//            final double alpha = -10.0;
            Scalar meanValue = Core.mean(blurredImage);
            final double meanGrayValue = meanValue.val[0];

            // Otsu threshold
            _segmentedImg = blurredImage.clone();
            Imgproc.threshold(blurredImage, _segmentedImg, meanGrayValue, 255,
                    Imgproc.THRESH_OTSU + Imgproc.THRESH_BINARY_INV);

            // delete all created images except the segmentation result
            grayImage.release();
            grayImage = null;
            blurredImage.release();
            blurredImage = null;

        } else
        {
            throw new IllegalArgumentException(
                    "segmentLesion() failed, image is null");
        }
        
        return _segmentedImg;

    }
        
    private Rect findRegionOfInterest(Mat img)
    {
        final Size imgSize = img.size();
        final Scalar meanSclar = Core.mean(img);
        final double meanValue = meanSclar.val[0];
        
        Point cornerUpLeft = new Point(0,0);
        Point cornerUpRight = new Point(imgSize.width, 0);
        Point cornerDownLeft = new Point(0, imgSize.height);
        Point cornerDownRight = new Point(imgSize.width, imgSize.height);
        
        final int skip = 3; // there is a very thin white line at the most right border, workaround: skip few pixels
        final int step = 2; // increase the ROI by this step every iteration, the bigger the faster
        final int maxDist = 3; // don't go more than 1/maxDist from each corner
        int x = (int)cornerUpLeft.x + skip;
        int y = (int)cornerUpLeft.y + skip;
        while (img.get(y, x)[0] < meanValue && 
                x < img.width()/maxDist && 
                y < img.height()/maxDist )
        {
            cornerUpLeft.set(new double[]{x+step, y+step});
            x = (int)cornerUpLeft.x;
            y = (int)cornerUpLeft.y;
        }
        
        x = (int)cornerUpRight.x-1 - skip;
        y = (int)cornerUpRight.y + skip;
        while (img.get(y, x)[0] < meanValue && 
                x > (img.width() - img.width()/maxDist) && 
                y < img.height()/maxDist )
        {
            cornerUpRight.set(new double[]{x-step, y+step});
            x = (int)cornerUpRight.x;
            y = (int)cornerUpRight.y;
        }
        
        x = (int)cornerDownLeft.x + skip;
        y = (int)cornerDownLeft.y-1 - skip;
        while (img.get(y, x)[0] < meanValue && 
                x < img.width()/maxDist && 
                y > (img.height() - img.height()/maxDist) )
        {
            cornerDownLeft.set(new double[]{x+step, y-step});
            x = (int)cornerDownLeft.x;
            y = (int)cornerDownLeft.y;
        }
        
        x = (int)cornerDownRight.x-1 - skip;
        y = (int)cornerDownRight.y-1 - skip;
        while (img.get(y, x)[0] < meanValue && 
                x > (img.width() - img.width()/maxDist) && 
                y > (img.height() - img.height()/maxDist) )
        {
            cornerDownRight.set(new double[]{x-step, y-step});
            x = (int)cornerDownRight.x;
            y = (int)cornerDownRight.y;
        }
        
        int rectXStart = (int) Math.max(cornerUpLeft.x, cornerDownLeft.x);
        int rectXEnd   = (int) Math.min(cornerUpRight.x, cornerDownRight.x);
        int rectYStart = (int) Math.max(cornerUpLeft.y, cornerUpRight.y);
        int rectYEnd   = (int) Math.min(cornerDownLeft.y, cornerDownRight.y);

        Rect rect = new Rect(new Point(rectXStart,rectYStart), new Point(rectXEnd,rectYEnd));
        
        return rect;
    }
      
//    /**
//     * This function enhances the image contrast using a sigmoid function
//     */
//    private Mat applySigmoidFilter(Mat img, double alpha, double beta)
//    {
//        // image must be gray scale 255
//        if (img.type() != CvType.CV_8UC1)
//        {
//            throw new IllegalArgumentException(
//                    "Image type for the sigmoid filter must be CV_8UC1: gray scale, 1 channel.");
//        }
//
//        int size = (int) img.total() * img.channels();
//        byte[] data = new byte[size];
//        img.get(0, 0, data);
//
//        for (int i = 0; i < size; i++)
//        {
//            final double x = ((int) (data[i] & 0xFF) - beta) / alpha;
//            final double e = 1.0 / (1.0 + Math.exp(-x));
//            final double v = 255 * e;
//            data[i] = (byte) v;
//        }
//
//        Mat returnImg = img.clone();
//        returnImg.put(0, 0, data);
//
//        return returnImg;
//    }
}
