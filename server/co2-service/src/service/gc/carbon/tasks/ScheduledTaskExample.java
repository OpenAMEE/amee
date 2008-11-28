package gc.carbon.tasks;

import org.springframework.scheduling.quartz.QuartzJobBean;
import org.springframework.stereotype.Service;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

@Service
public class ScheduledTaskExample extends QuartzJobBean {

    private final Log log = LogFactory.getLog(getClass());

    public void executeInternal(JobExecutionContext ctx) throws JobExecutionException {
        // place your Job code here
        log.info("EXECUTING EXAMPLE QUARTZ JOB");
    }

}
