package Mrld;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.NotFoundException;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;
public class GenerateQRCode
{
    //static function that creates QR Code
    public void generateQRCode(String data, String charset, int h, int w) throws WriterException, IOException {
        //the BitMatrix class represents the 2D matrix of bits
        //MultiFormatWriter is a factory class that finds the appropriate Writer subclass for the BarcodeFormat requested and encodes the barcode with the supplied contents.
        BitMatrix matrix = new MultiFormatWriter().encode(new String(data.getBytes(charset), charset), BarcodeFormat.QR_CODE, w, h);
        MatrixToImageWriter.writeToFile(matrix, path.substring(path.lastIndexOf('.') + 1), new File(path));
    }
    //main() method
    public GenerateQRCode(String str) throws WriterException, IOException {

        //path where we want to get QR Code
//        path = (new File(System.getProperty("java.io.tmpdir"))).toPath().resolve(str.replaceAll("/", "|") + ".png");
        //Encoding charset to be used
        String charset = "UTF-8";
        Map<EncodeHintType, ErrorCorrectionLevel> hashMap = new HashMap<EncodeHintType, ErrorCorrectionLevel>();
        //generates QR code with Low level(L) error correction capability
        hashMap.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.L);
        //invoking the user-defined method that creates the QR code
        generateQRCode(str, path, charset, hashMap, 200, 200);//increase or decrease height and width accodingly
        //prints if the QR code is generated
        System.out.println("QR Code created successfully.");
    }
}


