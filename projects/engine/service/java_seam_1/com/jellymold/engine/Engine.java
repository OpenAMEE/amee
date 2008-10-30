package com.jellymold.engine;

import com.jellymold.kiwi.*;
import com.jellymold.kiwi.auth.AuthFilter;
import com.jellymold.kiwi.auth.GuestFilter;
import com.jellymold.kiwi.environment.SiteService;
import com.jellymold.plum.admin.FreeMarkerConfigurationFilter;
import com.jellymold.utils.ApplicationFilter;
import com.jellymold.utils.cache.CacheHelper;
import org.restlet.Component;
import org.restlet.Filter;
import org.restlet.Restlet;
import org.restlet.VirtualHost;
import org.restlet.data.Protocol;
import org.restlet.ext.seam.SeamConnectorService;
import org.restlet.ext.seam.SeamController;
import org.restlet.ext.seam.SeamFilter;
import org.restlet.service.ConnectorService;
import org.tanukisoftware.wrapper.WrapperListener;
import org.tanukisoftware.wrapper.WrapperManager;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Engine implements WrapperListener, Serializable {

    private final Log log = LogFactory.getLog(getClass());

    private Component container;
    private boolean initialise = false;
    private int ajpPort = 8010;
    private int maxThreads = 700;
    private int minThreads = 200;
    private int threadMaxIdleTimeMs = 30000;

    public Engine() {
        super();
    }

    public Engine(boolean initialise, int ajpPort) {
        this();
        this.initialise = initialise;
        this.ajpPort = ajpPort;
    }

    public Engine(boolean initialise) {
        this();
        this.initialise = initialise;
    }

    public Engine(boolean initialise, int ajpPort, int maxThreads, int minThreads, int threadMaxIdleTimeMs) {
        this();
        this.initialise = initialise;
        this.ajpPort = ajpPort;
        this.maxThreads = maxThreads;
        this.minThreads = minThreads;
        this.threadMaxIdleTimeMs = threadMaxIdleTimeMs;
    }

    public static void main(String[] args) {
        boolean initialise = ((args.length > 0) && (args[0].equalsIgnoreCase("initialise")));
        Engine.start(new Engine(initialise), args);
    }

    protected static void start(WrapperListener wrapperListener, String[] args) {
        WrapperManager.start(wrapperListener, args);
    }

    public Integer start(String[] args) {

        log.debug("start");

        // control JBoss Seam
        SeamController seamController = SeamController.getInstance();

        // start Seam call
        seamController.startSeam();

        // initialise if needed
        if (initialise) {
            log.debug("initialising...");
            seamController.beginSeamCall(true);
            initialise();
            seamController.endSeamCall();
            log.debug("...initialised");
        }

        seamController.beginSeamCall(true);

        onStart();

        seamController.endSeamCall();

        // create the container
        container = new Component();

        // add an HTTP server connector to the Restlet container
        // TODO: some constants to extract here
        container.getServers().add(Protocol.AJP, ajpPort);
        container.getClients().add(Protocol.FILE);
        container.getContext().getParameters().add("converter", "org.restlet.ext.seam.SeamServerConverter");
        container.getContext().getParameters().add("minThreads", "" + minThreads); // default is 1
        container.getContext().getParameters().add("maxThreads", "" + maxThreads); // default is 255
        container.getContext().getParameters().add("threadMaxIdleTimeMs", "" + threadMaxIdleTimeMs); // default is 60000
        // more params here: htthttp://jetty.mortbay.org/jetty5/doc/optimization.htmlp://www.restlet.org/documentation/1.0/ext/com/noelios/restlet/ext/jetty/JettyServerHelper
        // advice here:  (what about Jetty 6?)

        // wrap VirtualHost creation in a Seam call
        seamController.beginSeamCall(true);

        // JBoss Seam wrapper
        ConnectorService connectorService = new SeamConnectorService();

        // create a VirtualHost per Site
        SiteService siteService = KiwiBeans.getSiteService();
        List<Site> sites = siteService.getSites();
        for (Site site : sites) {
            // create VirtualHost based on Site and SiteAliases
            VirtualHost virtualHost = new VirtualHost(container.getContext());
            virtualHost.setName(site.getName().equals("") ? "Site" : site.getName());
            virtualHost.setHostScheme(site.getServerScheme().equals("") ? "http" : site.getServerScheme());
            virtualHost.setHostPort(site.getServerPort().equals("") ? "80" : site.getServerPort());
            // TODO: virtualHost.setServerAddress(); ???
            // TODO: virtualHost.setServerPort(); ???
            String hostDomain = site.getServerName();
            for (SiteAlias siteAlias : site.getSiteAliases()) {
                hostDomain = hostDomain + "|" + siteAlias.getServerAlias();
            }
            virtualHost.setHostDomain(hostDomain.equals("") ? "localhost" : hostDomain);
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
        seamController.endSeamCall();

        // get things going
        try {
            container.start();
        } catch (Exception e) {
            log.fatal("caught Exception: " + e);
        }

        return null;
    }

    protected void initialise() {
        // do nothing
    }

    protected void onStart() {
        //do nothing
    }

    protected void onShutdown() {
        //do nothing
    }

    protected Restlet addFilters(EngineApplication engineApplication, boolean addAuthFilter) {
        // create sequential list of Filters
        List<Filter> filters = new ArrayList<Filter>();
        // add standard Filters
        filters.add(new SeamFilter(engineApplication));
        filters.add(new SiteFilter(engineApplication));
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
        // note: the indexing below will only work there are enough Filters in the list
        // set next Restlet/Filter for all Filters in sequence, except the last one
        for (int i = 0; i < filters.size() - 1; i++) {
            filters.get(i).setNext(filters.get(i + 1));
        }
        // set next Restlet for last Filter in sequence
        filters.get(filters.size() - 1).setNext(engineApplication);
        // return the first Filter in sequence
        return filters.get(0);
    }

    protected Filter getCustomFilter(EngineApplication engineApplication) {
        ApplicationFilter customFilter = null;
        if (engineApplication.getFilterNames().length() > 0) {
            try {
                customFilter = (ApplicationFilter) Class.forName(engineApplication.getFilterNames()).newInstance();
                customFilter.setContext(engineApplication.getContext());
                customFilter.setApplication(engineApplication);
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
        log.debug("stopping...");
        SeamController.getInstance().beginSeamCall(true);
        onShutdown();
        SeamController.getInstance().endSeamCall();
        SeamController.getInstance().stopSeam();
        CacheHelper.getInstance().getCacheManager().shutdown();
        try {
            container.stop();
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