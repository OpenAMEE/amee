package com.jellymold.engine;

import com.jellymold.kiwi.*;
import com.jellymold.kiwi.auth.AuthFilter;
import com.jellymold.kiwi.auth.GuestFilter;
import com.jellymold.kiwi.auth.BasicAuthFilter;
import com.jellymold.utils.ThreadBeanHolderFilter;
import com.jellymold.utils.cache.CacheHelper;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.restlet.*;
import org.restlet.ext.seam.TransactionController;
import org.restlet.ext.seam.SpringFilter;
import org.restlet.service.LogService;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.tanukisoftware.wrapper.WrapperListener;
import org.tanukisoftware.wrapper.WrapperManager;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;
import java.util.logging.ConsoleHandler;
import java.util.logging.Formatter;
import java.util.logging.LogRecord;

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
        container = ((Component) springContext.getBean("ameeContainer"));
        container.getContext().getAttributes().put("springContext", springContext);
        container.getContext().getAttributes().put("transactionController", transactionController);
        container.setStatusService(new EngineStatusService(true));

        // configure AJP server
        Server ajpServer = ((Server) springContext.getBean("ameeServer"));
        ajpServer.getContext().getAttributes().put("springContext", springContext);
        ajpServer.getContext().getAttributes().put("transactionController", transactionController);

        // wrap VirtualHost creation
        transactionController.begin(true);

        // Spring wrapper
        // ConnectorService connectorService = new TransactionConnectorService(transactionController);

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

        // create a VirtualHost per Site
//        SiteService siteService = (SiteService) springContext.getBean("siteService");
//        List<Site> sites = siteService.getSites();
//        for (Site site : sites) {
//            // create VirtualHost based on Site and SiteAliases
//            VirtualHost virtualHost = new VirtualHost(container.getContext());
//            virtualHost.setName(site.getName().equals("") ? "Site" : site.getName());
//            virtualHost.setHostScheme(site.getServerScheme().equals("") ? ".*" : site.getServerScheme());
//            virtualHost.setHostPort(site.getServerPort().equals("") ? ".*" : site.getServerPort());
//            virtualHost.setServerAddress(site.getServerAddress().equals("") ? ".*" : site.getServerPort());
//            virtualHost.setServerPort(site.getServerPort().equals("") ? ".*" : site.getServerPort());
//            String hostDomain = site.getServerName();
//            for (SiteAlias siteAlias : site.getSiteAliases()) {
//                hostDomain = hostDomain + "|" + siteAlias.getServerAlias();
//            }
//            virtualHost.setHostDomain(hostDomain.equals("") ? ".*" : hostDomain);
//            container.getHosts().add(virtualHost);
//            // add Apps to host based on Apps attached to Site
//            for (SiteApp siteApp : site.getSiteApps()) {
//                if (siteApp.isEnabled()) {
//                    App app = siteApp.getApp();
//                    // use the SiteApp UID as the EngineApplication name so that we can later retrieve the SiteApp
//                    EngineApplication engineApplication = new EngineApplication(container.getContext(), siteApp.getUid());
//                    engineApplication.setConnectorService(connectorService);
//                    engineApplication.setFilterNames(app.getFilterNames());
//                    if (!siteApp.isDefaultApp()) {
//                        virtualHost.attach(siteApp.getUriPattern(), addFilters(engineApplication, app.getAuthenticationRequired()));
//                    } else {
//                        virtualHost.attachDefault(addFilters(engineApplication, app.getAuthenticationRequired()));
//                    }
//                }
//            }
//        }

        // wrap VirtualHost creation
        transactionController.end();

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

    protected Restlet addFilters(EngineApplication engineApplication, boolean addAuthFilter) {
        // create sequential list of Filters
        List<Filter> filters = new ArrayList<Filter>();
        // add standard Filters
        filters.add(new ThreadBeanHolderFilter());
        filters.add(new SpringFilter(engineApplication, transactionController, springContext));
        filters.add(new SiteFilter(engineApplication, engineApplication.getName()));
        //filters.add(new FreeMarkerConfigurationFilter(engineApplication));
        // only add AuthFilter if required
        if (addAuthFilter) {
            filters.add(new BasicAuthFilter(engineApplication));
            filters.add(new AuthFilter(engineApplication));
        } else {
            filters.add(new GuestFilter(engineApplication));
        }
        // add custom Filter if available
        Filter customFilter = getCustomFilter(engineApplication);
        if (customFilter != null) {
            filters.add(customFilter);
        }
        // NOTE: the indexing below will only work there are enough Filters in the list
        // set next Restlet/Filter for all Filters in sequence, except the last one
        for (int i = 0; i < (filters.size() - 1); i++) {
            filters.get(i).setNext(filters.get(i + 1));
        }
        // set next Restlet for last Filter in sequence
        filters.get(filters.size() - 1).setNext(engineApplication);
        // return the first Filter in sequence
        return filters.get(0);
    }

    protected Filter getCustomFilter(EngineApplication engineApplication) {
        Filter customFilter = null;
        if (engineApplication.getFilterNames().length() > 0) {
            try {
                customFilter = (Filter) Class.forName(engineApplication.getFilterNames()).newInstance();
                customFilter.setContext(engineApplication.getContext());
            } catch (InstantiationException e) {
                // swallow
            } catch (IllegalAccessException e) {
                // swallow
            } catch (ClassNotFoundException e) {
                // swallow
            }
        }
        return customFilter;
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
