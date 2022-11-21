package com.vsloong.apkpublisher.agp.usecase;


import com.vsloong.apkpublisher.agp.interfaces.IFtpProcessor;
import com.vsloong.apkpublisher.agp.utils.LogUtil;

import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;

import java.io.File;
import java.io.FileInputStream;

/**
 * @Author: vsLoong
 * @Date: 2022/11/11 20:23
 * @Description: Ftp上传处理类
 */
public class FtpUseCase implements IFtpProcessor {

    @Override
    public boolean upload(String host,
                          int port,
                          String username,
                          String password,
                          String basePath,
                          String targetPath,
                          File... files
    ) {
        try {


            //将输入的地址格式化为仅IP地址
            String realHost = host.replace("http://", "")
                    .replace("https://", "")
                    .replace("/", "");

            FTPClient ftpClient = new FTPClient();

            //这里不需要端口号
            ftpClient.connect(realHost);

            boolean isLogin = ftpClient.login(username, password);
            if (!isLogin) {
                LogUtil.log("Ftp login failed, please check the account and password");
                return false;
            }

            //设置上传文件类型为二进制
            ftpClient.setFileType(FTP.BINARY_FILE_TYPE);


            //切换到Ftp目录，顺便检查目录是否正确
            boolean isRightBasePath = ftpClient.changeWorkingDirectory(basePath);
            if (!isRightBasePath) {
                LogUtil.log("Ftp directory is incorrect");
                return false;
            }

            //如果有新目录的需求就检查或者创建
            if (targetPath != null) {
                String[] paths = targetPath.split("/");

                StringBuilder pathBuilder = new StringBuilder(basePath);
                for (String path : paths) {
                    if (null != path && !path.isBlank()) {
                        pathBuilder.append("/");
                        pathBuilder.append(path);
                        boolean isRightNewPath = ftpClient.changeWorkingDirectory(pathBuilder.toString());
                        if (!isRightNewPath) {
                            ftpClient.makeDirectory(pathBuilder.toString());
                        }
                    }
                }

                ftpClient.changeWorkingDirectory(pathBuilder.toString());
            }

            //开始上传
            for (File file : files) {
                FileInputStream inputStream = new FileInputStream(file);
                ftpClient.storeFile(file.getName(), inputStream);
                inputStream.close();
            }

            //结束后退出登录并关闭连接
            ftpClient.logout();
            ftpClient.disconnect();
        } catch (Throwable t) {
            t.printStackTrace();
            LogUtil.log("" + t.getMessage());
            return false;
        }

        return true;
    }

}
