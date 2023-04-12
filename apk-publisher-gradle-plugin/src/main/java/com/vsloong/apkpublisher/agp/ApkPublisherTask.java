package com.vsloong.apkpublisher.agp;

import static com.vsloong.apkpublisher.agp.entity.ConstantsKt.ROOT_DIR;

import com.android.build.gradle.AppExtension;
import com.android.build.gradle.api.ApplicationVariant;
import com.android.build.gradle.api.BaseVariantOutput;
import com.android.builder.model.ProductFlavor;
import com.vsloong.apkpublisher.agp.entity.ApkFlavorInfoBean;
import com.vsloong.apkpublisher.agp.entity.ApkInfoBean;
import com.vsloong.apkpublisher.agp.entity.ApkPublisherExtension;
import com.vsloong.apkpublisher.agp.usecase.DingTalkUseCase;
import com.vsloong.apkpublisher.agp.usecase.FtpUseCase;
import com.vsloong.apkpublisher.agp.usecase.QRCodeUseCase;
import com.vsloong.apkpublisher.agp.utils.LogUtil;

import org.gradle.api.DefaultTask;
import org.gradle.api.DomainObjectSet;
import org.gradle.api.tasks.TaskAction;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import javax.inject.Inject;

/**
 * @Author: vsLoong
 * @Date: 2022/11/14 19:47
 * @Description: 打包任务
 */
public class ApkPublisherTask extends DefaultTask {

    private final String dependsOnTaskName;

    @Inject
    public ApkPublisherTask(String dependsOnTaskName) {
        setGroup("apkPublisher");
        this.dependsOnTaskName = dependsOnTaskName;
        dependsOn(dependsOnTaskName);
    }

    @TaskAction
    void doAction() {
        ApkPublisherExtension extension = (ApkPublisherExtension) getProject().getExtensions().getByName("apkPublisherInfo");
        if (null == extension.dingTalkServerUrl ||
                null == extension.dingTalkSecret ||
                null == extension.ftpHost ||
                null == extension.ftpBasePath ||
                null == extension.ftpUsername ||
                null == extension.ftpPassword) {
            LogUtil.log("Please check whether the \"apkpublisher\" parameters are correctly configured");
            return;
        }

        ApkInfoBean apkInfoBean = null;
        List<ApkFlavorInfoBean> flavorInfoList = new ArrayList<>();

        AppExtension appExtension = getProject().getExtensions().getByType(AppExtension.class);

        DomainObjectSet<ApplicationVariant> applicationVariants = appExtension.getApplicationVariants();
        for (ApplicationVariant applicationVariant : applicationVariants) {
            //判断当前task名是否对应的该variant
            String appVariantName = applicationVariant.getName();
            if (!("assemble" + appVariantName.toLowerCase()).equals(dependsOnTaskName.toLowerCase())) {
                continue;
            }
            apkInfoBean = new ApkInfoBean();
            apkInfoBean.applicationId = applicationVariant.getApplicationId();
            apkInfoBean.buildType = applicationVariant.getBuildType().getName();
            apkInfoBean.versionCode = applicationVariant.getVersionCode();
            apkInfoBean.versionName = applicationVariant.getVersionName();
            apkInfoBean.appName = applicationVariant.getName();

            for (ProductFlavor productFlavor : applicationVariant.getProductFlavors()) {
                ApkFlavorInfoBean apkFlavorInfoBean = new ApkFlavorInfoBean();
                apkFlavorInfoBean.dimension = productFlavor.getDimension();
                apkFlavorInfoBean.name = productFlavor.getName();
                flavorInfoList.add(apkFlavorInfoBean);
            }
            apkInfoBean.flavors = flavorInfoList;

            //输出的文件信息
            for (BaseVariantOutput output : applicationVariant.getOutputs()) {
                apkInfoBean.outputFilePath = output.getOutputFile().getAbsolutePath();
            }
        }

        //如果apkInfoBean为null，则可能是多flavor的情况下，直接点击了assembleRelease的情况，我们不允许该任务
        if (apkInfoBean == null) {
            LogUtil.log("This task is not supported, please choose another task");
        } else {
            uploadAndSendMessage(apkInfoBean, extension);
        }

    }

    /**
     * 创建文件夹的时候格式化时间
     * 相同VersionCode情况下，需要根据时间来区分
     */
    private String getCurrentFormatTime() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd-hh-mm", Locale.CHINA);
        return dateFormat.format(new Date());
    }

    /**
     * 获取要保存的路径
     * 例如：ROOT_DIR(根目录)/packageName或flavors[0]/versionCode/flavors[1]/flavor[n]/time/
     */
    private String getTargetDirPath(ApkInfoBean apkInfoBean) {

        StringBuilder path = new StringBuilder();
        path.append("/").append(ROOT_DIR);

        if (apkInfoBean.flavors.isEmpty()) {
            path.append("/").append(apkInfoBean.applicationId);
            path.append("/").append(apkInfoBean.versionCode);
        } else {
            int size = apkInfoBean.flavors.size();
            for (int i = 0; i < size; i++) {
                String flavor = apkInfoBean.flavors.get(i).name.toLowerCase();
                path.append("/").append(flavor);
                if (i == 0) {
                    path.append("/").append(apkInfoBean.versionCode);
                }
            }
        }
        path.append("/").append(getCurrentFormatTime());
        return path.toString();
    }

    /**
     * 上传到FTP文件夹，并发送钉钉通知
     */
    private void uploadAndSendMessage(
            ApkInfoBean apkInfoBean,
            ApkPublisherExtension extension
    ) {

        File apkFile = new File(apkInfoBean.outputFilePath);

        String targetDirPath = getTargetDirPath(apkInfoBean);

        //组装二维码的下载连接
        String baseUrl = extension.ftpHost + ":" + extension.ftpPort + targetDirPath + "/";
        String apkDownloadUrl = baseUrl + apkFile.getName();

        QRCodeUseCase useCase = new QRCodeUseCase();
        String qrImagePath = useCase.createQRCodeImage(
                apkDownloadUrl,
                apkFile.getParent());

        if (qrImagePath == null) {
            LogUtil.log("Failed to generate QR code");
            return;
        }

        File qrCodeImageFile = new File(qrImagePath);

        FtpUseCase ftpUseCase = new FtpUseCase();
        boolean isUploadSuccess = ftpUseCase.upload(
                extension.ftpHost,
                extension.ftpPort,
                extension.ftpUsername,
                extension.ftpPassword,
                extension.ftpBasePath,
                targetDirPath,
                qrCodeImageFile, apkFile
        );

        if (!isUploadSuccess) {
            LogUtil.log("Failed to upload file to server");
            return;
        }

        //=====发送钉钉消息
        DingTalkUseCase dingTalkUseCase = new DingTalkUseCase();

        List<String> atUsers = new ArrayList<>();
        if (null != extension.dingTalkAtUsers && !extension.dingTalkAtUsers.isBlank()) {
            String[] users = extension.dingTalkAtUsers.split(",");
            atUsers.addAll(Arrays.asList(users));
        }

        StringBuilder appName = new StringBuilder();
        if (apkInfoBean.flavors.isEmpty()) {
            appName.append(apkInfoBean.appName);
            appName.append("_");
        } else {
            for (ApkFlavorInfoBean flavor : apkInfoBean.flavors) {
                appName.append(flavor.name);
                appName.append("_");
            }
        }

        String qrCodeImageUrl = baseUrl + qrCodeImageFile.getName();
        StringBuilder markdown = new StringBuilder(
                "### " + appName + apkInfoBean.versionCode + "_" + apkInfoBean.versionName +
                        "\n-----" +
                        "\n注意：仅支持内网环境" +
                        "\n- [历史APK目录](" + extension.ftpHost + ":" + extension.ftpPort + "/" + ROOT_DIR + ")" +
                        "\n- [点击下载APK](" + apkDownloadUrl + ")" +
                        "\n- [点击显示二维码](" + qrCodeImageUrl + ")"
        );

        if (!atUsers.isEmpty()) {
            markdown.append("\n-----");
            markdown.append("\n");
            for (String atUser : atUsers) {
                markdown.append("@").append(atUser).append(" ");
            }
        }

        boolean isSendSuccess = dingTalkUseCase.sendMarkdownMessage(
                extension.dingTalkServerUrl,
                extension.dingTalkSecret,
                markdown.toString(),
                atUsers
        );

        if (isSendSuccess) {
            LogUtil.log("The apk publishing task has been successfully completed");
        }
    }
}
