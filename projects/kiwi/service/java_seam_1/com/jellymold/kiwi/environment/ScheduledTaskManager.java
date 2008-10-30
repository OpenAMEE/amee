package com.jellymold.kiwi.environment;

import com.jellymold.kiwi.Environment;
import com.jellymold.kiwi.ScheduledTask;
import com.sun.org.apache.commons.beanutils.MethodUtils;
import org.apache.log4j.Logger;
import org.jboss.seam.Component;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.core.QuartzDispatcher;
import org.quartz.SchedulerException;

import javax.persistence.EntityManager;
import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Name("scheduledTaskManager")
@Scope(ScopeType.APPLICATION)
public class ScheduledTaskManager implements Serializable {

    private final static Logger log = Logger.getLogger(ScheduledTaskManager.class);

    @In(create = true)
    private transient EntityManager entityManager;

    @In(create = true)
    private transient EnvironmentService environmentService;

    private String serverName = "";

    private Map<String, QuartzDispatcher.QuartzTriggerHandle> timers =
            Collections.synchronizedMap(new HashMap<String, QuartzDispatcher.QuartzTriggerHandle>());

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
    }

    private void start(ScheduledTask scheduledTask) {
        List<Object> args;
        Object component;
        QuartzDispatcher.QuartzTriggerHandle timer;
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.MILLISECOND, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.add(Calendar.MINUTE, 1); // start at next round minute
        if (scheduledTask.isEnabled() && scheduledTask.isServerEnabled(serverName)) {
            component = Component.getInstance(scheduledTask.getComponent(), true);
            if (component != null) {
                args = new ArrayList<Object>();
                args.add(calendar.getTime());
                args.add(scheduledTask.getCron());
                if (scheduledTask.getDuration() > 0) {
                    args.add(scheduledTask.getDuration());
                }
                args.add(scheduledTask.getEnvironment().getUid());
                try {
                    timer = (QuartzDispatcher.QuartzTriggerHandle) MethodUtils.invokeMethod(component, scheduledTask.getMethod(), args.toArray());
                    timer = timers.put(scheduledTask.getUid(), timer);
                    if (timer != null) {
                        // shutdown previous ScheduledTask
                        try {
                            timer.cancel();
                        } catch (SchedulerException e) {
                            // swallow
                        }
                    }
                } catch (NoSuchMethodException e) {
                    // swallow
                } catch (IllegalAccessException e) {
                    // swallow
                } catch (InvocationTargetException e) {
                    // swallow
                }
            }
        }
    }

    public void onShutdown() {
        onShutdown(true);
    }

    public void onShutdown(boolean runOnShutdown) {
        List<Environment> environments = environmentService.getEnvironments();
        for (Environment environment : environments) {
            List<ScheduledTask> scheduledTasks = environmentService.getScheduledTasks(environment);
            for (ScheduledTask scheduledTask : scheduledTasks) {
                shutdown(scheduledTask, runOnShutdown);
            }
        }
    }

    private void shutdown(ScheduledTask scheduledTask, boolean runOnShutdown) {
        QuartzDispatcher.QuartzTriggerHandle timer;
        List<Object> args;
        Object component;
        timer = timers.remove(scheduledTask.getUid());
        // only work with ScheduledTasks that were actually scheduled
        if (timer != null) {
            try {
                // unschedule the task
                timer.cancel();
            } catch (SchedulerException e) {
                // swallow
            }
            // if needed, run task one last time
            if (runOnShutdown && scheduledTask.getRunOnShutdown() && scheduledTask.getEnabled()) {
                component = Component.getInstance(scheduledTask.getComponent(), true);
                if (component != null) {
                    args = new ArrayList<Object>();
                    if (scheduledTask.getDuration() > 0) {
                        args.add(scheduledTask.getDuration());
                    }
                    args.add(scheduledTask.getEnvironment().getUid());
                    try {
                        MethodUtils.invokeMethod(component, scheduledTask.getMethod(), args);
                    } catch (NoSuchMethodException e) {
                        // swallow
                    } catch (IllegalAccessException e) {
                        // swallow
                    } catch (InvocationTargetException e) {
                        // swallow
                    }
                }
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
}