
import java.io.*;
import java.util.*;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.awt.image.WritableRaster;
import javax.imageio.ImageIO;

class bdp2bmp {

    final static boolean DEBUG = false;
        
    static SortedMap<Integer, String> map = new TreeMap<>();
    static Image BMPImage;
    static int width,
               height;
 
    public static void main(String[] args) {

        String fileName         =   null;
        String fileNamePrefix   =   null;
        int[] bmpArray;

        if (args.length != 0) {
            fileName = args[0];
            debug("Command Argument: " + fileName);
            fileNamePrefix = fileName.substring(0,fileName.lastIndexOf('.'));
            debug("Base File Name: " + fileNamePrefix);
        }
        if (fileName == null || !fileName.endsWith(".bdp")) {
            System.out.println("Command form: bdp2bmp FILE.BDP");
            return;
        }
        
        System.out.println(
            "BDP2BMP - \n" + 
            "Image translator. Processes Matthews Swedot .BDP file\n" + 
            "and outputs a bitmap image that can be viewed on a PC.\n" +
            "Copyright 2011-2014");
        System.out.println();
        
        parseBDP(fileName);
        bmpArray = buildImgArray();
       
        BufferedImage image = new BufferedImage(width, height, 
                BufferedImage.TYPE_BYTE_BINARY);
        WritableRaster raster = image.getRaster();
        raster.setPixels(0,0,width,height,bmpArray);
        try{
            ImageIO.write(image, "BMP", new File(fileNamePrefix + ".bmp"));
            System.out.println("Bitmap saved to " + fileNamePrefix + ".bmp");
        } catch (IOException e) {
            System.out.println("Error writing image file");
        }

    }//main()

    private static void parseBDP(String BDPFile) {
        
        BufferedReader reader;
        String[] lineElements;
        String line = null,
               hexData;
        int lineNum;

        try {
            reader = new BufferedReader(new FileReader(BDPFile));
            System.out.println("File loaded: " + BDPFile);
            System.out.print("Parsing file");
            while (true) {
                System.out.print(".");
                line = reader.readLine();
                if (line == null) {
                    break;
                }
                if (line.length() == 0) {
                    continue;
                }
               debug("Processing Line: " + line);
               lineElements = line.split("\\s+");
                //First element in each line should be line number
                try {
                    lineNum = Integer.parseInt(lineElements[0]);
                } catch (NumberFormatException e) {
                    throw new IOException();
                }
                //Second element should be the make bitmap command "bdbmp"
                if (!lineElements[1].equalsIgnoreCase("bdbmp")) {
                    throw new IOException();
                }
                //The first line must include the graphic dimensions
                if (line.contains(",")) {
                    String regex = ",";
                    try {
                        height = Integer.parseInt(
                                    lineElements[2].split(regex)[0]);
                        width = Integer.parseInt(
                                    lineElements[2].split(regex)[1]);
                        hexData = lineElements[2].split(regex)[2];
                    } catch (NumberFormatException e) {
                        throw new IOException();
                    }
                }//end if
                else {
                    hexData = lineElements[2];
                }
                map.put(lineNum, hexData);
            }//end while
        }//end try
        catch (FileNotFoundException e) {
            System.out.println("Error: File not found: " + BDPFile);
            System.exit(0);
        } 
        catch (IOException e) {
            System.out.println("FATAL ERROR PARSING FILE.");
            System.out.println("Formatting problem in line: " + line);
            System.exit(0);
        }
        
        System.out.println("Done!");
        System.out.println("Graphic Dimensions: " + 
                width + "W x " + height + "H" );        
    }//readFile()
    
    private static int[] buildImgArray() {
        
        Iterator mapIterator = map.values().iterator();
        String hexStr;
        int[] outArray = new int[width * height];
        int[] tmpArray;
        int lineLength  =   0,
            index       =   0,
            column      =   0;
        
        System.out.print("Processing image data...");
        
        while (mapIterator.hasNext()) {
            hexStr = mapIterator.next().toString();
            lineLength = hexStr.length();
            for(int i = 0; i < lineLength; i++) {
                tmpArray = hexDigit2IntArray(hexStr.charAt(i));
                for(int n = 0; n < 4; n++) {
                    outArray[index] = tmpArray[n];
                    debug("Index: " + index + " n: " + n);
                    index += width;
                    if(index >= outArray.length)
                    index = ++column;
                    debug("Index after: " + index);
                }
            }
        } //end while   
        
        System.out.println("Done!");
        System.out.print(outArray.length + " pixels buffered..."); 
        
        return outArray;

    }//buildImgArray()
    
    private static int[] hexDigit2IntArray(char digit) {
        int hex2Int;
        int[] bin = new int[4];
        String binStr;        
        hex2Int = Character.digit(digit, 16);
        binStr = Integer.toBinaryString(hex2Int);        
        while (binStr.length() < 4) {
            binStr = "0" + binStr;
        }
        for(int i = 0; i < 4; i++) {
            bin[i] = Character.digit(binStr.charAt(i),2) ^ 0b1;
        }
        return bin;        
    }//hexDigit2IntArray()

    private static void debug(String s) {
        if (DEBUG) {
            System.out.println(s);
        }
    }//debug()
}//class printBDP
