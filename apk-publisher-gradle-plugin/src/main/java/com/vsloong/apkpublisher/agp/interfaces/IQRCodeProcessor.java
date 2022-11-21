package com.vsloong.apkpublisher.agp.interfaces;

/**
 * @Author: vsLoong
 * @Date: 2022/11/11 20:16
 * @Description: 生成二维码接口
 */
public interface IQRCodeProcessor {

    String createQRCodeImage(String content, String imageDir);
}
