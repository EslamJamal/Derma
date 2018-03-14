	Display.displayImage(HRemoval); 	// Displaying Original Image
    	Segmentation seg = new Segmentation(fileName2); 
    	grayImage = Segmentation.convertToGrayRGB(HRemoval); // Converting image from bgr to rgb
    	Display.displayImage(grayImage); // Displaying rgb image
    	Imgproc.cvtColor(grayImage, grayImage, Imgproc.COLOR_RGB2GRAY); //Converting rgb to gray
    	Display.displayImage(grayImage); //Displaying gray image
    	Homogeneity.normHistogram(grayImage); //Normalizing color histogram for this gray image
    	Mat segImg = seg.segmentLesion(SegmType.OTSU_THRESHOLD); //Segmentation of grey image
    	Display.displayImage(segImg); //Displaying segmented Image
    	Regularity reg = new Regularity(segImg); //Regularity
    	Display.displayImage(reg.getImage());
        reg.findContour();
        Display.displayImage(reg.getContourImage());
        double regIndx1 = reg.calculateRegularity();
        System.out.println(regIndx1); //Regularity output
        Symmetry.Assymetry(segImg); // Assymetry output