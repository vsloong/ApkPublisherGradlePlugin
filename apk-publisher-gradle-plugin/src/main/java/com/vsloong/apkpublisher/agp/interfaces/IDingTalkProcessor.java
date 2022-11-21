package com.vsloong.apkpublisher.agp.interfaces;

import java.util.List;

/**
 * @Author: vsLoong
 * @Date: 2022/11/14 15:14
 * @Description: 发送钉钉消息接口
 */
public interface IDingTalkProcessor {

    boolean sendTextMessage(
            String serverUrl,
            String secret,
            String content,
            List<String> atUsers
    );

    boolean sendMarkdownMessage(
            String serverUrl,
            String secret,
            String content,
            List<String> atUsers
    );
}
