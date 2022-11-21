package com.vsloong.apkpublisher.agp.entity;

import java.util.List;

/**
 * @Author: vsLoong
 * @Date: 2022/11/16 10:51
 * @Description: Apk发布相关的内容
 */
public class ApkInfoBean {

    public String applicationId;

    public int versionCode;
    public String versionName;

    public String appName;

    public List<ApkFlavorInfoBean> flavors;

    public String buildType;

    public String outputFilePath;
}
