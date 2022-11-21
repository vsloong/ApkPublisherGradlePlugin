package com.vsloong.apkpublisher.agp.entity;

/**
 * @Author: vsLoong
 * @Date: 2022/11/16 10:20
 * @Description: 发布Apk任务的实体类
 */
public class ApkPublisherTaskBean {

    private String sourceTaskName;
    private String newTaskName;
    private String groupName;

    public ApkPublisherTaskBean(String sourceTaskName, String newTaskName, String groupName) {
        this.sourceTaskName = sourceTaskName;
        this.newTaskName = newTaskName;
        this.groupName = groupName;
    }

    public String getSourceTaskName() {
        return sourceTaskName;
    }

    public void setSourceTaskName(String sourceTaskName) {
        this.sourceTaskName = sourceTaskName;
    }

    public String getNewTaskName() {
        return newTaskName;
    }

    public void setNewTaskName(String newTaskName) {
        this.newTaskName = newTaskName;
    }

    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }
}
