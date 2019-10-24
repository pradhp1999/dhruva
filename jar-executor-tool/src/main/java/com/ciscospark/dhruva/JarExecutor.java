package com.ciscospark.dhruva;

import com.ciscospark.jarexecutor.ApplicationTask;
import com.ciscospark.jarexecutor.ApplicationTaskLauncher;

import com.google.common.collect.ImmutableMap;
import com.ciscospark.dhruva.EchoTask;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;

import java.util.Map;

@EnableAutoConfiguration
public class JarExecutor extends ApplicationTaskLauncher {


    public static void main(String[] args) {
        setupAndRun(JarExecutor.class, args);
    }


    @Override
    public Map<String, Class<? extends ApplicationTask>> getAppTaskMap() {
        Map<String, Class<? extends ApplicationTask>> m = ImmutableMap.of(
                EchoTask.TASK_NAME, EchoTask.class);
        return m;

    };
}