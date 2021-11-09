package de.mmenze.corona.web.schedule;

import java.io.PrintWriter;
import java.io.StringWriter;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.task.TaskSchedulerCustomizer;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;

import de.mmenze.corona.web.service.SendMailService;
import lombok.extern.slf4j.Slf4j;

/**
 * Common error handler for all scheduled tasks in the system
 */
@Slf4j
@Configuration
public class SchedulerErrorHandler implements TaskSchedulerCustomizer {

    @Autowired
    private SendMailService sendMailService;


    @Override
    public void customize(ThreadPoolTaskScheduler taskScheduler) {
        taskScheduler.setErrorHandler(this::handleError);
        log.info("Configured error handler");
    }

    public void handleError(Throwable t) {
        log.info("Calling error handler due to: {}", t.getMessage());
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        t.printStackTrace(pw);
        String subject = "Error in application: " + t.getMessage();
        sendMailService.sendMail(subject, sw.toString());
    }

}
