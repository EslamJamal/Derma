package com.derma.melanoma;
import java.awt.FlowLayout;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;

import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;

import org.opencv.core.Mat;

/**
 * Helper class to display open cv "mat" images.
 *
 */
public class Display
{    
    // create a windoe and show the image
    public static void displayImage(Mat matImg)
    {   
        Image img = Mat2BufferedImage(matImg);
        ImageIcon icon=new ImageIcon(img);
        JFrame frame=new JFrame();
        frame.setLayout(new FlowLayout());        
        frame.setSize(img.getWidth(null)+50, img.getHeight(null)+50);     
        JLabel lbl=new JLabel();
        lbl.setIcon(icon);
        frame.add(lbl);
        frame.setVisible(true);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    }
    
    // private helper function 
    private static BufferedImage Mat2BufferedImage(Mat src)
    {

        // The output can be assigned either to a BufferedImage or to an Image

        int type = BufferedImage.TYPE_BYTE_GRAY;
        if ( src.channels() > 1 ) {
            type = BufferedImage.TYPE_3BYTE_BGR;
        }
        int bufferSize = src.channels()*src.cols()*src.rows();
        byte [] b = new byte[bufferSize];
        src.get(0,0,b); // get all the pixels
        BufferedImage image = new BufferedImage(src.cols(),src.rows(), type);
        final byte[] targetPixels = ((DataBufferByte) image.getRaster().getDataBuffer()).getData();
        System.arraycopy(b, 0, targetPixels, 0, b.length);  
        return image;
    }
}
