package com.amee.service.environment;

import com.amee.domain.ScheduledTask;
import com.amee.domain.environment.Environment;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.quartz.*;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.scheduling.quartz.SchedulerFactoryBean;
import org.springframework.stereotype.Service;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.io.Serializable;
import java.text.ParseException;
import java.util.List;

// TODO: disabled for now
// @Service
public class ScheduledTaskManager implements Serializable, ApplicationContextAware {

    private final Log log = LogFactory.getLog(getClass());

    @PersistenceContext
    private EntityManager entityManager;

    @Autowired
    private EnvironmentService environmentService;

    private String serverName = "";

    @Autowired
    private SchedulerFactoryBean quartzScheduler;

    private ApplicationContext applicationContext;

    public ScheduledTaskManager() {
        super();
    }

    public void onStart() {
        List<Environment> environments = environmentService.getEnvironments();
        for (Environment environment : environments) {
            List<ScheduledTask> scheduledTasks = environmentService.getScheduledTasks(environment);
            for (ScheduledTask scheduledTask : scheduledTasks) {
                start(scheduledTask);
            }
        }

        try {
            quartzScheduler.getScheduler().start();
        } catch (SchedulerException ex) {
            ex.printStackTrace();
        }
    }

    private void start(ScheduledTask scheduledTask) {

        Object component;

        if (scheduledTask.isEnabled() && scheduledTask.isServerEnabled(serverName)) {

            component = applicationContext.getBean(scheduledTask.getComponent()).getClass();

            if (component != null) {

                try {

                    Scheduler scheduler = quartzScheduler.getScheduler();
                    JobDetail jd = new JobDetail(scheduledTask.getName(), scheduledTask.getEnvironment().getUid(), applicationContext.getBean(scheduledTask.getComponent()).getClass());
                    CronTrigger ct = new CronTrigger(scheduledTask.getName(), "DEFAULT", scheduledTask.getCron());
                    scheduler.scheduleJob(jd, ct);

                } catch (SchedulerException ex) {
                    ex.printStackTrace();
                } catch (ParseException pr) {
                    pr.printStackTrace();
                }
            }
        }
    }

    public void onShutdown() {
        onShutdown(true);
    }

    public void onShutdown(boolean runOnShutdown) {
        Scheduler scheduler = quartzScheduler.getScheduler();

        List<Environment> environments = environmentService.getEnvironments();
        for (Environment environment : environments) {
            List<ScheduledTask> scheduledTasks = environmentService.getScheduledTasks(environment);
            for (ScheduledTask scheduledTask : scheduledTasks) {
                shutdown(scheduledTask, runOnShutdown);
            }
        }
        try {
            scheduler.shutdown();
        } catch (SchedulerException ex) {
            log.error(ex);
        }
    }


    private void shutdown(ScheduledTask scheduledTask, boolean runOnShutdown) {

        Object component;

        Scheduler scheduler = quartzScheduler.getScheduler();

        if (runOnShutdown && scheduledTask.getRunOnShutdown() && scheduledTask.getEnabled() && scheduledTask.isServerEnabled(serverName)) {

            component = applicationContext.getBean(scheduledTask.getComponent()).getClass();

            if (component != null) {

                try {
                    JobDetail jd = new JobDetail(scheduledTask.getName()+"_shutdown", scheduledTask.getEnvironment().getUid(), applicationContext.getBean(scheduledTask.getComponent()).getClass());
                    SimpleTrigger st = new SimpleTrigger(
                            scheduledTask.getName()+"_shutdown", "DEFAULT", 0, 1);
                    scheduler.scheduleJob(jd, st);
                    if (!scheduler.isStarted()) {
                        scheduler.start();
                    }
                } catch (SchedulerException ex) {
                    ex.printStackTrace();
                }
            }
        }


    }

    public void run(ScheduledTask scheduledTask) {
        Object component;

        Scheduler scheduler = quartzScheduler.getScheduler();

        component = applicationContext.getBean(scheduledTask.getComponent()).getClass();

        if (component != null) {

            try {
                JobDetail jd = new JobDetail(scheduledTask.getName()+"_temp", scheduledTask.getEnvironment().getUid(), applicationContext.getBean(scheduledTask.getComponent()).getClass());
                SimpleTrigger st = new SimpleTrigger(
                        scheduledTask.getName()+"_temp", "DEFAULT", 0, 1);
                scheduler.scheduleJob(jd, st);
                if (!scheduler.isStarted()) {
                    scheduler.start();
                }
            } catch (SchedulerException ex) {
                ex.printStackTrace();
            }
        }

    }

    public String getServerName() {
        return serverName;
    }

    public void setServerName(String serverName) {
        if (serverName == null) {
            serverName = "";
        }
        this.serverName = serverName;
    }

    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }
}