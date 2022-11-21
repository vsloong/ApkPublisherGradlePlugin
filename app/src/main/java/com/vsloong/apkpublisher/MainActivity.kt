package com.vsloong.apkpublisher

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle


/**
 * 一个帮助上传APK文件到FTP服务器，并生成带下载链接的二维码图片的Gradle插件。
 */
class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }
}