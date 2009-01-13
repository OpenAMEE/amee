package com.jellymold.engine;

import com.jellymold.kiwi.*;
import com.jellymold.kiwi.auth.AuthFilter;
import com.jellymold.kiwi.auth.GuestFilter;
import com.jellymold.kiwi.environment.SiteService;
import com.jellymold.utils.ThreadBeanHolderFilter;
import com.jellymold.utils.skin.FreeMarkerConfigurationFilter;
import com.jellymold.utils.cache.CacheHelper;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.restlet.*;
import org.restlet.data.Protocol;
import org.restlet.ext.seam.SpringConnectorService;
import org.restlet.ext.seam.SpringController;
import org.restlet.ext.seam.SpringFilter;
import org.restlet.service.ConnectorService;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.tanukisoftware.wrapper.WrapperListener;
import org.tanukisoftware.wrapper.WrapperManager;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Engine implements WrapperListener, Serializable {

    private final Log log = LogFactory.getLog(getClass());

    protected ApplicationContext springContext;
    protected SpringController springController;
    protected Component container;

    protected static int AJP_PORT = 8010;
    protected static int MAX_THREADS = 700;
    protected static int MIN_THREADS = 200;
    protected static int THREAD_MAX_IDLE_TIME_MS = 30000;
    protected static int LOW_THREADS = 25;
    protected static int LOW_RESOURCE_MAX_IDLE_TIME_MS = 2500;
    protected static int ACCEPTOR_THREADS = 1;
    protected static int ACCEPT_QUEUE_SIZE = 0;


    private boolean initialise = false;
    private int ajpPort = AJP_PORT;
    protected String serverName;
    private int maxThreads = MAX_THREADS;
    private int minThreads = MIN_THREADS;
    private int threadMaxIdleTimeMs = THREAD_MAX_IDLE_TIME_MS;
    private int lowThreads = LOW_THREADS;
    private int lowResourceMaxIdleTimeMs = LOW_RESOURCE_MAX_IDLE_TIME_MS;
    private int acceptorThreads = ACCEPTOR_THREADS;
    private int acceptQueueSize = ACCEPT_QUEUE_SIZE;

    public Engine() {
        super();
    }

    public Engine(boolean initialise) {
        this();
        this.initialise = initialise;
    }

    public Engine(boolean initialise, int ajpPort) {
        this();
        this.initialise = initialise;
        this.ajpPort = ajpPort;
    }

    public Engine(boolean initialise, int ajpPort, String serverName, int maxThreads, int minThreads, int threadMaxIdleTimeMs) {
        this(initialise,  ajpPort, serverName);
        this.maxThreads = maxThreads;
        this.minThreads = minThreads;
        this.threadMaxIdleTimeMs = threadMaxIdleTimeMs;
    }

    public Engine(boolean initialise, int ajpPort, String serverName, int maxThreads, int minThreads,
                  int threadMaxIdleTimeMs, int lowThreads, int lowResourceMaxIdleTimeMs, int acceptorThreads,
                  int acceptQueueSize) {
        this(initialise,  ajpPort, serverName, maxThreads, minThreads, threadMaxIdleTimeMs);
        if (threadMaxIdleTimeMs > 0) {
            this.threadMaxIdleTimeMs = threadMaxIdleTimeMs;
        }
        if (lowThreads > 0) {
            this.lowThreads = lowThreads;
        }
        if (lowResourceMaxIdleTimeMs > 0) {
            this.lowResourceMaxIdleTimeMs = lowResourceMaxIdleTimeMs;
        }
        if (acceptorThreads > -1) {
            this.acceptorThreads = acceptorThreads;
        }
        if (acceptQueueSize > -1) {
            this.acceptQueueSize = acceptQueueSize;
        }
    }

    public Engine(boolean initialise, int ajpPort, String serverName) {
        this();
        this.initialise = initialise;
        this.ajpPort = ajpPort;
        this.serverName = serverName;
    }

    public static void main(String[] args) {
        boolean initialise = ((args.length > 0) && (args[0].equalsIgnoreCase("initialise")));
        start(new Engine(initialise), args);
    }

    protected static void start(WrapperListener wrapperListener, String[] args) {
        WrapperManager.start(wrapperListener, args);
    }

    public Integer start(String[] args) {

        log.debug("Starting Engine...");

        // initialise Spring ApplicationContext
        springContext = new ClassPathXmlApplicationContext(new String[]{
                "META-INF/applicationContext.xml",
                "META-INF/applicationContext-skins.xml"});
        // initialise SpringController (for controlling Spring)
        springController = (SpringController) springContext.getBean("springController");
        // startup Spring
        springController.startup();

        // initialise if needed
        if (initialise) {
            log.debug("initialising...");
            springController.begin(true);
            initialise();
            springController.end();
            log.debug("...initialised");
        }

        // wrap start callback in Seam call
        springController.begin(true);
        onStart();
        springController.end();

        // create the Restlet container and add Spring stuff
        container = new Component();
        container.getContext().getAttributes().put("springContext", springContext);
        container.getContext().getAttributes().put("springController", springController);

        // configure AJP server
        Server ajpServer = container.getServers().add(Protocol.AJP, ajpPort);
        ajpServer.getContext().getAttributes().put("springContext", springContext);
        ajpServer.getContext().getAttributes().put("springController", springController);
        ajpServer.getContext().getParameters().add("converter", "org.restlet.ext.seam.SpringServerConverter");
        ajpServer.getContext().getParameters().add("minThreads", "" + minThreads); // default is 1
        ajpServer.getContext().getParameters().add("maxThreads", "" + maxThreads); // default is 255
        ajpServer.getContext().getParameters().add("threadMaxIdleTimeMs", "" + threadMaxIdleTimeMs); // default is 60000
        ajpServer.getContext().getParameters().add("lowThreads", "" + lowThreads); // default is 25
        ajpServer.getContext().getParameters().add("lowResourceMaxIdleTimeMs", "" + lowResourceMaxIdleTimeMs); // default is 2500
        ajpServer.getContext().getParameters().add("acceptorThreads", "" + acceptorThreads); // default is 1
        ajpServer.getContext().getParameters().add("acceptQueueSize", "" + acceptQueueSize); // default is 0
        // more params here: http://www.restlet.org/documentation/1.1/ext/com/noelios/restlet/ext/jetty/JettyServerHelper.html
        // advice here: http://jetty.mortbay.org/jetty5/doc/optimization.html (what about Jetty 6?)

        // configure file client
        container.getClients().add(Protocol.FILE);

        // wrap VirtualHost creation in a Seam call
        springController.begin(true);

        // JBoss Seam wrapper
        ConnectorService connectorService = new SpringConnectorService(springController);

        // create a VirtualHost per Site
        SiteService siteService = (SiteService) springContext.getBean("siteService");
        List<Site> sites = siteService.getSites();
        for (Site site : sites) {
            // create VirtualHost based on Site and SiteAliases
            VirtualHost virtualHost = new VirtualHost(container.getContext());
            virtualHost.setName(site.getName().equals("") ? "Site" : site.getName());
            virtualHost.setHostScheme(site.getServerScheme().equals("") ? ".*" : site.getServerScheme());
            virtualHost.setHostPort(site.getServerPort().equals("") ? ".*" : site.getServerPort());
            virtualHost.setServerAddress(site.getServerAddress().equals("") ? ".*" : site.getServerPort());
            virtualHost.setServerPort(site.getServerPort().equals("") ? ".*" : site.getServerPort());
            String hostDomain = site.getServerName();
            for (SiteAlias siteAlias : site.getSiteAliases()) {
                hostDomain = hostDomain + "|" + siteAlias.getServerAlias();
            }
            virtualHost.setHostDomain(hostDomain.equals("") ? ".*" : hostDomain);
            container.getHosts().add(virtualHost);
            // add Apps to host based on Apps attached to Site
            for (SiteApp siteApp : site.getSiteApps()) {
                if (siteApp.isEnabled()) {
                    App app = siteApp.getApp();
                    // use the SiteApp UID as the EngineApplication name so that we can later retrieve the SiteApp
                    EngineApplication engineApplication = new EngineApplication(container.getContext(), siteApp.getUid());
                    engineApplication.setConnectorService(connectorService);
                    engineApplication.setFilterNames(app.getFilterNames());
                    if (!siteApp.isDefaultApp()) {
                        virtualHost.attach(siteApp.getUriPattern(), addFilters(engineApplication, app.getAuthenticationRequired()));
                    } else {
                        virtualHost.attachDefault(addFilters(engineApplication, app.getAuthenticationRequired()));
                    }
                }
            }
        }


        // wrap VirtualHost creation in a Seam call
        springController.end();

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

    protected void initialise() {
        // do nothing
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
        filters.add(new SpringFilter(engineApplication, springController, springContext));
        filters.add(new SiteFilter(engineApplication, engineApplication.getName()));
        filters.add(new FreeMarkerConfigurationFilter(engineApplication));
        // only add AuthFilter if required
        if (addAuthFilter) {
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
            // shutdown Spring
            springController.shutdown();
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