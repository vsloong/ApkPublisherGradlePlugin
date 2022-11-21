package com.vsloong.apkpublisher.agp;

import com.vsloong.apkpublisher.agp.entity.ApkPublisherExtension;
import com.vsloong.apkpublisher.agp.entity.ApkPublisherTaskBean;
import com.vsloong.apkpublisher.agp.utils.LogUtil;

import org.gradle.api.Action;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.Task;

import java.util.ArrayList;
import java.util.List;

/**
 * @Author: vsLoong
 * @Date: 2022/11/14 11:13
 * @Description: Gradle插件实现类
 */
public class ApkPublisherPlugin implements Plugin<Project> {

    @Override
    public void apply(Project project) {
        project.getExtensions().create("apkPublisherInfo", ApkPublisherExtension.class);


        /**
         * 获取配置的参数值
         */
        project.afterEvaluate(new Action<Project>() {
            @Override
            public void execute(Project p) {
                List<ApkPublisherTaskBean> taskList = new ArrayList<>();

                for (Task task : p.getTasks()) {
                    String taskName = task.getName();

                    if (taskName.startsWith("assemble") && taskName.endsWith("Release")) {
                        String myTaskName = taskName.replace("assemble", "publish");

                        ApkPublisherTaskBean taskBean = new ApkPublisherTaskBean(
                                taskName,
                                myTaskName,
                                "publishApk"
                        );
                        taskList.add(taskBean);
                    }
                }

                if (!taskList.isEmpty()) {
                    LogUtil.log(taskList.size() + " matching task(s) have been found");
                }

                for (ApkPublisherTaskBean taskBean : taskList) {
                    p.getTasks().create(
                            taskBean.getNewTaskName(),
                            ApkPublisherTask.class,
                            taskBean.getSourceTaskName());
                }
                LogUtil.log("New tasks have been added");
            }
        });
    }

}
