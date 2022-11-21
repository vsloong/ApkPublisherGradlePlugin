package com.vsloong.apkpublisher.agp.usecase;

import com.dingtalk.api.DefaultDingTalkClient;
import com.dingtalk.api.request.OapiRobotSendRequest;
import com.dingtalk.api.response.OapiRobotSendResponse;
import com.taobao.api.ApiException;
import com.vsloong.apkpublisher.agp.interfaces.IDingTalkProcessor;
import com.vsloong.apkpublisher.agp.utils.LogUtil;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.List;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

/**
 * @Author: vsLoong
 * @Date: 2022/11/14 15:18
 * @Description: 发送钉钉消息的用例
 */
public class DingTalkUseCase implements IDingTalkProcessor {

    @Override
    public boolean sendTextMessage(
            String serverUrl,
            String secret,
            String content,
            List<String> atUsers
    ) {

        String signUrl = createSignUrl(serverUrl, secret);

        if (signUrl == null) {
            LogUtil.log("Failed to create DingTalk signature");
            return false;
        }

        DefaultDingTalkClient dingTalkClient = new DefaultDingTalkClient(signUrl);

        OapiRobotSendRequest request = new OapiRobotSendRequest();
        request.setMsgtype("text");

        OapiRobotSendRequest.Text text = new OapiRobotSendRequest.Text();
        text.setContent(content);
        request.setText(text);

        if (null != atUsers && !atUsers.isEmpty()) {
            OapiRobotSendRequest.At at = new OapiRobotSendRequest.At();
            at.setAtMobiles(atUsers);
            request.setAt(at);
        }

        try {
            OapiRobotSendResponse response = dingTalkClient.execute(request);
            if (response.isSuccess()) {
                return true;
            } else {
                LogUtil.log("Failed to send DingTalk message, errCode=" + response.getErrcode() + ", errMessage=" + response.getErrmsg());
                return false;
            }

        } catch (ApiException e) {
            e.printStackTrace();
            LogUtil.log("Failed to send DingTalk message: " + e.getMessage());
            return false;
        }
    }

    @Override
    public boolean sendMarkdownMessage(String serverUrl, String secret, String content, List<String> atUsers) {
        String signUrl = createSignUrl(serverUrl, secret);

        if (signUrl == null) {
            LogUtil.log("Failed to create DingTalk signature");
            return false;
        }

        DefaultDingTalkClient dingTalkClient = new DefaultDingTalkClient(signUrl);

        OapiRobotSendRequest request = new OapiRobotSendRequest();
        request.setMsgtype("markdown");

        OapiRobotSendRequest.Markdown markdown = new OapiRobotSendRequest.Markdown();
        markdown.setText(content);
        markdown.setTitle("打包结果出来喽~");
        request.setMarkdown(markdown);

        if (null != atUsers && !atUsers.isEmpty()) {
            OapiRobotSendRequest.At at = new OapiRobotSendRequest.At();
            at.setAtMobiles(atUsers);
            request.setAt(at);
        }

        try {
            OapiRobotSendResponse response = dingTalkClient.execute(request);
            if (response.isSuccess()) {
                return true;
            } else {
                LogUtil.log("Failed to send DingTalk message, errCode = " + response.getErrcode() + ", errMessage = " + response.getErrmsg());
                return false;
            }

        } catch (ApiException e) {
            e.printStackTrace();
            LogUtil.log("Failed to send DingTalk message: " + e.getMessage());
            return false;
        }
    }

    /**
     * 创建添加签名的URL
     */
    private String createSignUrl(String serverUrl, String secret) {
        long timestamp = System.currentTimeMillis();
        String sourceStr = timestamp + "\n" + secret;
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
            byte[] signData = mac.doFinal(sourceStr.getBytes(StandardCharsets.UTF_8));
            String signStr = URLEncoder.encode(new String(Base64.getEncoder().encode(signData)), "UTF-8");
            return serverUrl + "&timestamp=" + timestamp + "&sign=" + signStr;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
