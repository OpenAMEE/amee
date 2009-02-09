package com.jellymold.engine;

import com.jellymold.utils.cache.CacheHelper;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.restlet.Component;
import org.restlet.Server;
import org.restlet.ext.seam.TransactionController;
import org.restlet.service.LogService;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.tanukisoftware.wrapper.WrapperListener;
import org.tanukisoftware.wrapper.WrapperManager;

import java.io.Serializable;
import java.util.logging.*;

public class Engine implements WrapperListener, Serializable {

    private final Log log = LogFactory.getLog(getClass());

    protected ApplicationContext springContext;
    protected TransactionController transactionController;
    protected Component container;

    protected String serverName;

    public Engine() {
        super();
    }

    public Engine(String serverName) {
        this();
        this.serverName = serverName;
    }

    public static void main(String[] args) {
        start(new Engine(), args);
    }

    protected static void start(WrapperListener wrapperListener, String[] args) {
        WrapperManager.start(wrapperListener, args);
    }

    public Integer start(String[] args) {

        log.debug("Starting Engine...");

        // initialise Spring ApplicationContext
        springContext = new ClassPathXmlApplicationContext(new String[]{
                "applicationContext.xml",
                "applicationContext-container.xml",
                "applicationContext-application-*.xml",
                "applicationContext-skins.xml"});

        // initialise TransactionController (for controlling Spring)
        transactionController = (TransactionController) springContext.getBean("transactionController");

        // wrap start callback
        transactionController.begin(true);
        onStart();
        transactionController.end();

        // create the Restlet container and add Spring stuff
        // TODO: do this in Spring config
        container = ((Component) springContext.getBean("ameeContainer"));
        container.getContext().getAttributes()
                .put("transactionController", transactionController); // used in SpringServerConverter?

        // configure AJP server
        // TODO: do this in Spring config
        Server ajpServer = ((Server) springContext.getBean("ameeServer"));
        ajpServer.getContext().getAttributes()
                .put("transactionController", transactionController); // used in SpringServerConverter?

        // Configure Restlet logging to log on a single line
        LogService logService = container.getLogService();
        logService.setLogFormat("[IP:{cia}] [M:{m}] [S:{S}] [PATH:{rp}] [UA:{cig}] [REF:{fp}]");
        Logger logger = Logger.getLogger("org.restlet");
        ConsoleHandler ch = new ConsoleHandler();
        ch.setFormatter(new Formatter() {
            public String format(LogRecord record) {
                return "[org.restlet]" + record.getMessage() + "\n";
            }
        });
        logger.setUseParentHandlers(false);
        logger.addHandler(ch);

        try {
            // get things going
            container.start();
            log.debug("...Engine started.");
        } catch (Exception e) {
            log.fatal("caught Exception: " + e);
            e.printStackTrace();
            return null;
        }

        return null;
    }

    protected void onStart() {
        // start scheduled tasks
        // ScheduledTaskManager scheduledTaskManager = (ScheduledTaskManager) org.jboss.seam.Component.getInstance("scheduledTaskManager", true);
        // scheduledTaskManager.setServerName(serverName);
        // scheduledTaskManager.onStart();
    }

    protected void onShutdown() {
        // shutdown scheduled tasks
        // ScheduledTaskManager scheduledTaskManager = (ScheduledTaskManager) org.jboss.seam.Component.getInstance("scheduledTaskManager", true);
        // scheduledTaskManager.onShutdown();
    }

    public int stop(int exitCode) {
        try {
            log.debug("Stopping Engine...");
            // shutdown callback
            onShutdown();
            // shutdown Restlet container
            container.stop();
            // clean up cache
            CacheHelper.getInstance().getCacheManager().shutdown();
            log.debug("...Engine stopped.");
        } catch (Exception e) {
            log.error("caught Exception: " + e);
        }
        return exitCode;
    }

    public void controlEvent(int event) {
        log.debug("controlEvent");
        // do nothing
    }
}
