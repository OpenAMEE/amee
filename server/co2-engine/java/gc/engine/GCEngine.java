/**
 * This file is part of AMEE.
 *
 * AMEE is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 *
 * AMEE is free software and is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 * Created by http://www.dgen.net.
 * Website http://www.amee.cc
 */
package gc.engine;

import com.jellymold.engine.Engine;
import com.jellymold.kiwi.environment.ScheduledTaskManager;
import org.apache.commons.cli.*;

public class GCEngine extends Engine {

    public GCEngine(boolean initialise, int ajpPort, String serverName) {
        super(initialise, ajpPort, serverName);
    }

    /**
     * Constructor
     *
     * @param initialise               true if engine should be initialised
     * @param ajpPort                  ajp setting: port
     * @param maxThreads               Jetty setting: Maximum threads waiting to service requests.
     * @param minThreads               Jetty setting: Minimum threads that will service requests.
     * @param threadMaxIdleTimeMs      Jetty setting: Time for an idle thread to wait for a request or read.
     * @param lowThreads               Jetty setting: Threshold of remaining threads at which the server is considered as running low on resources.
     * @param lowResourceMaxIdleTimeMs Jetty setting: Time in ms that connections will persist if listener is low on resources.
     * @param acceptorThreads          Jetty setting: Number of acceptor threads to set.
     * @param acceptQueueSize          Jetty setting: Size of the accept queue.
     */
    public GCEngine(boolean initialise, int ajpPort, String serverName, int maxThreads, int minThreads, int threadMaxIdleTimeMs,
                    int lowThreads, int lowResourceMaxIdleTimeMs, int acceptorThreads, int acceptQueueSize) {
        super(initialise, ajpPort, serverName, maxThreads, minThreads, threadMaxIdleTimeMs, lowThreads,
                lowResourceMaxIdleTimeMs, acceptorThreads, acceptQueueSize);
    }

    public static void main(String[] args) {

        int ajpPort = AJP_PORT;
        String serverName = "localhost";
        int maxThreads = MAX_THREADS;
        int minThreads = MIN_THREADS;
        int threadMaxIdleTimeMs = THREAD_MAX_IDLE_TIME_MS;
        int lowThreads = LOW_THREADS;
        int lowResourceMaxIdleTimeMs = LOW_RESOURCE_MAX_IDLE_TIME_MS;
        int acceptorThreads = ACCEPTOR_THREADS;
        int acceptQueueSize = ACCEPT_QUEUE_SIZE;

        Options options = new Options();

        Option serverNameOpt = OptionBuilder.withArgName("serverName")
                .hasArg()
                .withDescription("The server name")
                .create("serverName");
        serverNameOpt.setRequired(true);
        options.addOption(serverNameOpt);

        Option ajpPortOpt = OptionBuilder.withArgName("port")
                .hasArg()
                .withDescription("The port number the jk listener will listen on")
                .create("ajpPort");
        ajpPortOpt.setRequired(false);
        options.addOption(ajpPortOpt);

        // jetty options
        Option maxThreadsOpt = OptionBuilder.withArgName("maxThreads")
                .hasArg()
                .withDescription("Jetty setting: Maximum threads waiting to service requests.")
                .create("maxThreads");
        maxThreadsOpt.setRequired(false);
        options.addOption(maxThreadsOpt);

        Option minThreadsOpt = OptionBuilder.withArgName("minThreads")
                .hasArg()
                .withDescription("Jetty setting: Minimum threads waiting to service requests.")
                .create("minThreads");
        minThreadsOpt.setRequired(false);
        options.addOption(minThreadsOpt);

        Option threadMaxIdleTimeMsOpt = OptionBuilder.withArgName("threadMaxIdleTimeMs")
                .hasArg()
                .withDescription("Jetty setting: Time for an idle thread to wait for a request or read.")
                .create("threadMaxIdleTimeMs");
        threadMaxIdleTimeMsOpt.setRequired(false);
        options.addOption(threadMaxIdleTimeMsOpt);

        Option lowThreadsOpt = OptionBuilder.withArgName("lowThreads")
                .hasArg()
                .withDescription("Jetty setting: Threshold of remaining threads at which the server is considered as running low on resources.")
                .create("lowThreads");
        lowThreadsOpt.setRequired(false);
        options.addOption(lowThreadsOpt);

        Option lowResourceMaxIdleTimeMsOpt = OptionBuilder.withArgName("lowResourceMaxIdleTimeMs")
                .hasArg()
                .withDescription("Jetty setting: Time in ms that connections will persist if listener is low on resources.")
                .create("lowResourceMaxIdleTimeMs");
        lowResourceMaxIdleTimeMsOpt.setRequired(false);
        options.addOption(lowResourceMaxIdleTimeMsOpt);

        Option acceptorThreadsOpt = OptionBuilder.withArgName("acceptorThreads")
                .hasArg()
                .withDescription("Jetty setting: Number of acceptor threads to set.")
                .create("acceptorThreads");
        acceptorThreadsOpt.setRequired(false);
        options.addOption(acceptorThreadsOpt);

        Option acceptQueueSizeOpt = OptionBuilder.withArgName("acceptQueueSize")
                .hasArg()
                .withDescription("Jetty setting: Size of the accept queue.")
                .create("acceptQueueSize");
        acceptQueueSizeOpt.setRequired(false);
        options.addOption(acceptQueueSizeOpt);

        CommandLine line = null;
        CommandLineParser parser = new GnuParser();

        try {
            line = parser.parse(options, args);
        } catch (ParseException exp) {
            new HelpFormatter().printHelp("java gc.engine.GCEngine", options);
            System.exit(-1);
        }

        if (line.hasOption(serverNameOpt.getOpt())) {
            serverName = line.getOptionValue(serverNameOpt.getOpt());
        }

        if (line.hasOption(ajpPortOpt.getOpt())) {
            try {
                ajpPort = Integer.parseInt(line.getOptionValue(ajpPortOpt.getOpt()));
            } catch (NumberFormatException e) {
                new HelpFormatter().printHelp("java gc.engine.GCEngine", options);
                System.exit(-1);
            }
        }

        // get jetty options
        if (line.hasOption(maxThreadsOpt.getOpt())) {
            try {
                maxThreads = Integer.parseInt(line.getOptionValue(maxThreadsOpt.getOpt()));
            } catch (NumberFormatException e) {
                new HelpFormatter().printHelp("java gc.engine.GCEngine", options);
                System.exit(-1);
            }
        }

        if (line.hasOption(minThreadsOpt.getOpt())) {
            try {
                minThreads = Integer.parseInt(line.getOptionValue(minThreadsOpt.getOpt()));
            } catch (NumberFormatException e) {
                new HelpFormatter().printHelp("java gc.engine.GCEngine", options);
                System.exit(-1);
            }
        }        

        if (line.hasOption(threadMaxIdleTimeMsOpt.getOpt())) {
            try {
                threadMaxIdleTimeMs = Integer.parseInt(line.getOptionValue(threadMaxIdleTimeMsOpt.getOpt()));
            } catch (NumberFormatException e) {
                new HelpFormatter().printHelp("java gc.engine.GCEngine", options);
                System.exit(-1);
            }
        }

        if (line.hasOption(lowThreadsOpt.getOpt())) {
            try {
                lowThreads = Integer.parseInt(line.getOptionValue(lowThreadsOpt.getOpt()));
            } catch (NumberFormatException e) {
                new HelpFormatter().printHelp("java gc.engine.GCEngine", options);
                System.exit(-1);
            }
        }

        if (line.hasOption(lowResourceMaxIdleTimeMsOpt.getOpt())) {
            try {
                lowResourceMaxIdleTimeMs = Integer.parseInt(line.getOptionValue(lowResourceMaxIdleTimeMsOpt.getOpt()));
            } catch (NumberFormatException e) {
                new HelpFormatter().printHelp("java gc.engine.GCEngine", options);
                System.exit(-1);
            }
        }

        if (line.hasOption(acceptorThreadsOpt.getOpt())) {
            try {
                acceptorThreads = Integer.parseInt(line.getOptionValue(acceptorThreadsOpt.getOpt()));
            } catch (NumberFormatException e) {
                new HelpFormatter().printHelp("java gc.engine.GCEngine", options);
                System.exit(-1);
            }
        }

        if (line.hasOption(acceptQueueSizeOpt.getOpt())) {
            try {
                acceptQueueSize = Integer.parseInt(line.getOptionValue(acceptQueueSizeOpt.getOpt()));
            } catch (NumberFormatException e) {
                new HelpFormatter().printHelp("java gc.engine.GCEngine", options);
                System.exit(-1);
            }
        }

        Engine.start(
                new GCEngine(false,
                        ajpPort,
                        serverName,
                        maxThreads,
                        minThreads,
                        threadMaxIdleTimeMs,
                        lowThreads,
                        lowResourceMaxIdleTimeMs,
                        acceptorThreads,
                        acceptQueueSize),
                args);
    }

    @Override
    public void onStart() {
        ScheduledTaskManager taskMan = (ScheduledTaskManager) springContext.getBean("scheduledTaskManager");
        taskMan.setServerName(serverName);
        taskMan.onStart();
    }

    @Override
    public void onShutdown() {
        ScheduledTaskManager taskMan = (ScheduledTaskManager) springContext.getBean("scheduledTaskManager");
        taskMan.onShutdown();
    }
}