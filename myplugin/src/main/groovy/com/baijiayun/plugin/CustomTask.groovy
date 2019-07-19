package com.baijiayun.plugin

import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction

public class CustomTask extends DefaultTask {

    @TaskAction
    void output() {
        println("~~~~~~~~~~~~~~~~~~~~~~~")
        println "param1 is ${project.pluginExt.packageName}"
        println "param2 is ${project.pluginExt.pushKey}"

    }

}