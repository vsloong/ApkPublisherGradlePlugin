package com.vsloong.apkpublisher.agp.interfaces;

import java.io.File;

/**
 * @Author: vsLoong
 * @Date: 2022/11/11 20:21
 * @Description: Ftp接口，上传文件
 */
public interface IFtpProcessor {

    boolean upload(
            String host,
            int port,
            String username,
            String password,
            String basePath,
            String targetPath,
            File... files
    );
}
