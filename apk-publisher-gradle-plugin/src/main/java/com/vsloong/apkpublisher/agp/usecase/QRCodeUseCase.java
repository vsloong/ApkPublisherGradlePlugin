package com.vsloong.apkpublisher.agp.usecase;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;
import com.vsloong.apkpublisher.agp.interfaces.IQRCodeProcessor;

import java.awt.image.BufferedImage;
import java.io.File;
import java.util.HashMap;
import java.util.Map;

import javax.imageio.ImageIO;

/**
 * @Author: vsLoong
 * @Date: 2022/11/11 19:49
 * @Description:
 */
public class QRCodeUseCase implements IQRCodeProcessor {

    @Override
    public String createQRCodeImage(String content, String imageDir) {
        try {
            File file = new File(imageDir, "qr_code.png");
            if (!file.exists()) {
                file.createNewFile();
            }
            ImageIO.write(
                    createBufferedImage(content, 400, 400),
                    "png",
                    file);
            return file.getAbsolutePath();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private BufferedImage createBufferedImage(
            String content,
            int width,
            int height
    ) {
        Map<EncodeHintType, Object> map = new HashMap<>();
        map.put(EncodeHintType.CHARACTER_SET, "UTF-8");
        map.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.M);
        map.put(EncodeHintType.MARGIN, 1);

        MultiFormatWriter writer = new MultiFormatWriter();
        BufferedImage bufferedImage = null;
        try {
            BitMatrix matrix = writer.encode(content, BarcodeFormat.QR_CODE, width, height, map);
            bufferedImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_BGR);
            for (int i = 0; i < width; i++) {
                for (int j = 0; j < height; j++) {
                    int color;
                    if (matrix.get(i, j)) {
                        color = 0x000000;
                    } else {
                        color = 0xFFFFFF;
                    }

                    bufferedImage.setRGB(i, j, color);
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return bufferedImage;
    }

}
