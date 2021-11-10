package Mrld;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageConfig;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;

public class GenerateQRCode {
    public BufferedImage image;

    //static function that creates QR Code
    public void generateQRCode(String data, String charset, int h, int w) throws WriterException, IOException {
        //the BitMatrix class represents the 2D matrix of bits
        //MultiFormatWriter is a factory class that finds the appropriate Writer subclass for the BarcodeFormat requested and encodes the barcode with the supplied contents.
        BitMatrix matrix = new MultiFormatWriter().encode(new String(data.getBytes(charset), charset), BarcodeFormat.QR_CODE, w, h);
        image = MatrixToImageWriter.toBufferedImage(matrix, new MatrixToImageConfig(Color.BLACK.getRGB(), 0));
    }

    //main() method
    public GenerateQRCode(String str) throws WriterException, IOException {

        //path where we want to get QR Code
//        path = (new File(System.getProperty("java.io.tmpdir"))).toPath().resolve(str.replaceAll("/", "|") + ".png");
        //Encoding charset to be used
        String charset = "UTF-8";
        //invoking the user-defined method that creates the QR code
        generateQRCode(str, charset, 100, 100);//increase or decrease height and width accodingly
        //prints if the QR code is generated
        System.out.println("QR Code created successfully.");
    }
}


