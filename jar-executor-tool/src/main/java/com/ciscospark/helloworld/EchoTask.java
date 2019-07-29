package com.ciscospark.helloworld;

import com.ciscospark.jarexecutor.ApplicationTask;
import com.ciscospark.jarexecutor.config.ApplicationTaskConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import picocli.CommandLine;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;

@CommandLine.Command(name = "java -jar <jarname>.jar echo_task", footerHeading = "%nSimple echo task\n\n")
public class EchoTask implements ApplicationTask {

    Logger log = LoggerFactory.getLogger(EchoTask.class);

    public static final String TASK_NAME = "echo_task";

    @Autowired
    ApplicationTaskConfig config;

    @Autowired
    EchoTask echoTask;

    @CommandLine.Option(names = "-echoInput", description = "Any string to be echoed back")
    private String echoInput;

    private static final String logFileName =   "/task/logs/hello_output.txt";

    @Override
    public void preExecute(String... args) throws Exception {
        log.info("----- Starting hello world echo task ------");
        writeOutputToFile("----- Starting hello world echo task ------\n");
        CommandLine cmdLine = new CommandLine(this);
        cmdLine.parse(args);
    }

    @Override
    public void execute(String... args) throws Exception {
        log.info("The task argument was: " + echoInput);
        writeOutputToFile("The task argument was: " + echoInput + "\n");
    }

    @Override
    public void postExecute(String... args) throws Exception {}

    @Override
    public void printHelpMsg(String... args) {
        log.info("Echoes argument.");
    }

    private void writeOutputToFile(String input) {
        try (BufferedWriter out = new BufferedWriter(new FileWriter(logFileName, true))) {
            // Open given file in append mode.
            out.write(input);
        } catch (IOException e) {
            log.error("Exception writing to log file: " + e.getMessage(), e);
        }
    }

}