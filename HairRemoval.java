package com.derma.melanoma;

import org.opencv.core.Mat;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;
import org.opencv.photo.Photo;

/**
 * Class to remove the hair from the image
 * @author Ahmad
 *
 */
public class HairRemoval
{
    Mat _originalImg;
    Mat _hairRemovedImg;
    
    public  HairRemoval(Mat img)
    {
        _originalImg = img;
    }
    /**
     * Detects and removes the hair from the image.
     * 
     * @return image with the hair removed
     */
    public Mat removeHair()
    {
        if (null != _originalImg)
        {
            // image variables
            Mat grayImage = new Mat(_originalImg.size(), _originalImg.type());
            Mat blurredImage = new Mat(_originalImg.size(), _originalImg.type());
            Mat edges = new Mat(_originalImg.size(), _originalImg.type());
            Mat dilatedEdges = new Mat(_originalImg.size(), _originalImg.type());
            _hairRemovedImg = new Mat(_originalImg.size(), _originalImg.type());

            // convert image from colored to grayscale
            //Imgproc.cvtColor(_originalImg, grayImage, Imgproc.COLOR_RGB2GRAY);

            Imgproc.GaussianBlur(grayImage, blurredImage, new Size(3, 3), 0, 0);

            // apply canny edge detector to detect hair
            Imgproc.Canny(blurredImage, edges, 40, 100);

            // structuring element for dilation
            int dilation_size = 5;
            Mat element = Imgproc.getStructuringElement(Imgproc.MORPH_CROSS,
                    new Size(dilation_size, dilation_size));

            // apply dilation on the image
            Imgproc.dilate(edges, dilatedEdges, element);

            // TODO: inpainting is too slow, perhaps find an alternative?
            // apply inpainting to restore the original image without hair
            Photo.inpaint(grayImage, dilatedEdges, _hairRemovedImg, 1,
                    Photo.INPAINT_TELEA);

            grayImage.release();
            grayImage = null;
            blurredImage.release();
            blurredImage = null;
            edges.release();
            edges = null;
            dilatedEdges.release();
            dilatedEdges = null;

        } else
        {
            throw new IllegalArgumentException("removeHair() failed, image is null");
        }
       
        return _hairRemovedImg;
    }
}
