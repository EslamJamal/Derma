package com.derma.melanoma;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.highgui.Highgui;
import org.opencv.imgproc.*;


import com.derma.melanoma.Segmentation.SegmType;
/**
 *
 * Main class, the program starts here ...
 *
 */
public class Main
{
    static {System.loadLibrary(Core.NATIVE_LIBRARY_NAME);}
    static String fileName1 = "D:\\Derma photos\\PH2Dataset\\PH2 Dataset images\\IMD040\\IMD040_lesion\\IMD040_lesion.bmp";
    static String fileName2 = "D:\\Derma photos\\PH2Dataset\\PH2 Dataset images\\IMD040\\IMD040_Dermoscopic_Image\\IMD040.bmp";
    static Mat Assym = Highgui.imread("D:\\Derma photos\\PH2Dataset\\PH2 Dataset images\\IMD018\\IMD018_lesion\\IMD018_lesion.bmp "); 
    static Mat HRemoval = Highgui.imread("D:\\Derma photos\\PH2Dataset\\PH2 Dataset images\\IMD040\\IMD040_Dermoscopic_Image\\IMD040.bmp ");
    static Mat grayImage ;
    static Mat grayImage1 ;
    public static void main(String[] args)
    {  	
    	/*Display.displayImage(HRemoval);
    	Segmentation seg = new Segmentation(fileName2);
    	grayImage = Segmentation.convertToGrayRGB(HRemoval);
    	Display.displayImage(grayImage);
    	Imgproc.cvtColor(grayImage, grayImage, Imgproc.COLOR_RGB2GRAY);
    	Display.displayImage(grayImage);
    	Homogeneity.normHistogram(grayImage);
    	Mat segImg = seg.segmentLesion(SegmType.OTSU_THRESHOLD);
    	Display.displayImage(segImg);
    	Regularity reg = new Regularity(segImg);
    	Display.displayImage(reg.getImage());
        reg.findContour();
        Display.displayImage(reg.getContourImage());
        double regIndx1 = reg.calculateRegularity();
        System.out.println(regIndx1);
        Symmetry.Assymetry(segImg);*/
    	
    	// create an instance of the class regularity 
        /*Regularity rg1 = new Regularity(fileName1);
        Display.displayImage(rg1.getImage());
        rg1.findContour();
        Display.displayImage(rg1.getContourImage());
        double regIndx1 = rg1.calculateRegularity();
        System.out.println(regIndx1);
        */
        // another instance of the class regularity
        /*
        Regularity rg2 = new Regularity(fileName2);
        Display.displayImage(rg2.getImage());
        rg2.findContour();
        Display.displayImage(rg2.getContourImage());
        double regIndx2 = rg2.calculateRegularity();
        System.out.println(regIndx2);*/
       /* HairRemoval rg2 = new HairRemoval(HRemoval);
        Display.displayImage(rg2.removeHair());
        Homogeneity.normHistogram(rg2.removeHair());
        Segmentation rg3 = new Segmentation(fileName2);
        Mat segmentedImg=rg3.segmentLesion(SegmType.OTSU_THRESHOLD);
        Display.displayImage(segmentedImg);
        Symmetry.Assymetry(segmentedImg);*/
       
        Display.displayImage(HRemoval);
        Segmentation seg1 = new Segmentation(fileName2);
    	grayImage1 = Segmentation.convertToGrayRGB(HRemoval);
    	Display.displayImage(grayImage1);
    	Imgproc.cvtColor(grayImage1, grayImage1, Imgproc.COLOR_RGB2GRAY);
    	Display.displayImage(grayImage1);
    	Homogeneity.normHistogram(grayImage1);
    	HairRemoval hRemove = new HairRemoval(grayImage1);
    	Display.displayImage(hRemove.removeHair());
    	Mat segImg_1 = seg1.segmentLesion(SegmType.OTSU_THRESHOLD);
    	Display.displayImage(segImg_1);
    	Regularity reg_1 = new Regularity(segImg_1);
    	Display.displayImage(reg_1.getImage());
        reg_1.findContour();
        Display.displayImage(reg_1.getContourImage());
        double regIndx2 = reg_1.calculateRegularity();
        System.out.println(regIndx2);
        Symmetry.Assymetry(segImg_1);
    }
}   