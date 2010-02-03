package com.amee.engine;

import com.amee.domain.cache.CacheHelper;
import com.amee.service.transaction.TransactionController;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.restlet.Component;
import org.restlet.Server;
import org.restlet.service.LogService;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.tanukisoftware.wrapper.WrapperListener;
import org.tanukisoftware.wrapper.WrapperManager;

import java.io.Serializable;

public class Engine implements WrapperListener, Serializable {

    private final Log log = LogFactory.getLog(getClass());

    protected static ApplicationContext springContext;
    protected TransactionController transactionController;
    protected Component container;
    protected String serverName = "localhost";

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

        parseOptions(args);

        log.debug("start() Starting Engine...");

        // initialise Spring ApplicationContext
        springContext = new ClassPathXmlApplicationContext(new String[]{
                "applicationContext.xml",
                "applicationContext-jmx.xml",
                "applicationContext-jobs.xml",
                "applicationContext-container.xml",
                "applicationContext-application-*.xml",
                "applicationContext-skins.xml",
                "applicationContext-algorithmServices.xml",
                "applicationContext-servlets.xml"});

        // obtain the Restlet container
        container = ((Component) springContext.getBean("ameeContainer"));

        // initialise TransactionController (for controlling Spring)
        transactionController = (TransactionController) springContext.getBean("transactionController");

        // wrap start callback
        transactionController.begin(true);
        onStart();
        transactionController.end();

        // configure Restlet server (ajp, http, etc)
        // TODO: try and do this in Spring XML config
        Server ajpServer = ((Server) springContext.getBean("ameeServer"));
        ajpServer.getContext().getAttributes()
                .put("transactionController", transactionController); // used in TransactionServerConverter

        // configure Restlet logging to log on a single line
        LogService logService = container.getLogService();
        logService.setLogFormat("[IP:{cia}] [M:{m}] [S:{S}] [PATH:{rp}] [UA:{cig}] [REF:{fp}]");

        try {
            // Get things going - start the Restlet server.
            container.start();

            // Optionally start the Servlet container.
            String startServletContext = System.getenv("START_SERVLET_CONTEXT");
            if (Boolean.parseBoolean(startServletContext)) {
                org.mortbay.jetty.Server server = (org.mortbay.jetty.Server) springContext.getBean("servletServer");
                server.start();
                server.join();
            }

            log.debug("start() ...Engine started.");

        } catch (Exception e) {

            // Ouch. This really should not happen.
            log.fatal("start() Caught Exception: " + e.getMessage(), e);
            e.printStackTrace();

            // A non-null error code to indicate that Wrapper should exit.
            return 1;
        }

        // A null indicates success and that the Wrapper should stay alive.
        return null;
    }

    protected void parseOptions(String[] args) {

        CommandLine line = null;
        CommandLineParser parser = new GnuParser();
        Options options = new Options();

        // define serverName option
        Option serverNameOpt = OptionBuilder.withArgName("serverName")
                .hasArg()
                .withDescription("The server name")
                .create("serverName");
        serverNameOpt.setRequired(true);
        options.addOption(serverNameOpt);

        // parse the options
        try {
            line = parser.parse(options, args);
        } catch (ParseException exp) {
            new HelpFormatter().printHelp("java com.amee.engine.Engine", options);
            System.exit(-1);
        }

        // serverName
        if (line.hasOption(serverNameOpt.getOpt())) {
            serverName = line.getOptionValue(serverNameOpt.getOpt());
        }

    }

    protected void onStart() {
        // do nothing
    }

    protected void onShutdown() {
        // do nothing
    }

    public int stop(int exitCode) {
        try {
            log.debug("stop() Stopping Engine...");
            // shutdown callback
            onShutdown();
            // shutdown Restlet container
            container.stop();
            // clean up cache
            CacheHelper.getInstance().getCacheManager().shutdown();
            log.debug("stop() ...Engine stopped.");
        } catch (Exception e) {
            log.error("stop() Caught Exception: " + e.getMessage(), e);
        }
        return exitCode;
    }

    public void controlEvent(int event) {
        log.debug("controlEvent");
        // do nothing
    }


    public static ApplicationContext getAppContext() {
        return springContext;
    }
}
