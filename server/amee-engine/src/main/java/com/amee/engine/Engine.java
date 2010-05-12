package com.amee.engine;

import com.amee.base.transaction.TransactionController;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.joda.time.DateTimeZone;
import org.restlet.Component;
import org.restlet.Server;
import org.restlet.service.LogService;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.tanukisoftware.wrapper.WrapperListener;
import org.tanukisoftware.wrapper.WrapperManager;

import java.io.Serializable;
import java.util.TimeZone;

public class Engine implements WrapperListener, Serializable {

    private final Log log = LogFactory.getLog(getClass());

    // TODO: This is only static because the Servlet code needs it. Re-do Servlet code to acquire context differently.
    protected static ClassPathXmlApplicationContext applicationContext;
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

        log.info("start() Starting Engine...");

        // Initialise Spring ApplicationContext.
        applicationContext = new ClassPathXmlApplicationContext(new String[]{
                "applicationContext.xml",
                "applicationContext-jmx.xml",
                "applicationContext-jobs.xml",
                "applicationContext-container.xml",
                "applicationContext-application-*.xml",
                "applicationContext-messaging.xml",
                "applicationContext-skins.xml",
                "applicationContext-algorithmServices.xml",
                "applicationContext-servlets.xml"});

        // Obtain the Restlet container.
        container = ((Component) applicationContext.getBean("ameeContainer"));

        // Initialise TransactionController (for controlling Spring).
        transactionController = (TransactionController) applicationContext.getBean("transactionController");

        // Configure Restlet server (ajp, http, etc).
        // TODO: try and do this in Spring XML config
        Server ajpServer = ((Server) applicationContext.getBean("ameeServer"));
        ajpServer.getContext().getAttributes()
                .put("transactionController", transactionController); // used in TransactionServerConverter

        // Configure Restlet logging to log on a single line.
        LogService logService = container.getLogService();
        logService.setLogFormat("[IP:{cia}] [M:{m}] [S:{S}] [PATH:{rp}] [UA:{cig}] [REF:{fp}]");

        try {
            // Get things going - start the Restlet server.
            container.start();

            // Optionally start the Servlet container.
            String startServletContext = System.getenv("START_SERVLET_CONTEXT");
            if (Boolean.parseBoolean(startServletContext)) {
                org.mortbay.jetty.Server server = (org.mortbay.jetty.Server) applicationContext.getBean("servletServer");
                server.start();
                server.join();
            }

            log.info("start() ...Engine started.");

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

        // Define serverName option.
        Option serverNameOpt = OptionBuilder.withArgName("serverName")
                .hasArg()
                .withDescription("The server name")
                .create("serverName");
        serverNameOpt.setRequired(true);
        options.addOption(serverNameOpt);

        // Define timeZone option.
        Option timeZoneOpt = OptionBuilder.withArgName("timeZone")
                .hasArg()
                .withDescription("The time zone")
                .create("timeZone");
        timeZoneOpt.setRequired(false);
        options.addOption(timeZoneOpt);

        // Parse the options.
        try {
            line = parser.parse(options, args);
        } catch (ParseException exp) {
            new HelpFormatter().printHelp("java com.amee.engine.Engine", options);
            System.exit(-1);
        }

        // Handle serverName.
        if (line.hasOption(serverNameOpt.getOpt())) {
            serverName = line.getOptionValue(serverNameOpt.getOpt());
        }

        // Handle timeZone.
        if (line.hasOption(timeZoneOpt.getOpt())) {
            String timeZoneStr = line.getOptionValue(timeZoneOpt.getOpt());
            if (!StringUtils.isBlank(timeZoneStr)) {
                TimeZone timeZone = TimeZone.getTimeZone(timeZoneStr);
                if (timeZone != null) {
                    TimeZone.setDefault(timeZone);
                    DateTimeZone.setDefault(DateTimeZone.forTimeZone(timeZone));
                }
            }
        }
        log.info("parseOptions() Time Zone is: " + TimeZone.getDefault().getDisplayName() + " (" + TimeZone.getDefault().getID() + ")");
    }

    public int stop(int exitCode) {
        log.info("stop() Stopping Engine...");
        try {
            // Stop Restlet container.
            container.stop();
            // Stop ApplicationContext.
            applicationContext.close();
        } catch (Exception e) {
            log.error("stop() Caught Exception: " + e.getMessage(), e);
        }
        log.info("stop() ...Engine stopped.");
        return exitCode;
    }

    public void controlEvent(int event) {
        log.info("controlEvent() " + event);
    }

    public static ApplicationContext getAppContext() {
        return applicationContext;
    }
}
