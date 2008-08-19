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
import com.jellymold.plum.SkinSvnInitialisation;
import org.apache.commons.cli.*;
import org.jboss.seam.Component;

public class GCEngine extends Engine {

    private static String serverName;
    private static boolean initialiseSkinsFromSvn = false;
    private static int ajpPort = 8010;

    public GCEngine() {
        super();
    }

    public GCEngine(boolean initialise, int ajpPort) {
        super(initialise, ajpPort);
    }

    public static void main(String[] args) {

        Options options = new Options();

        Option serverNameOpt = OptionBuilder.withArgName("serverName")
                .hasArg()
                .withDescription("The server name")
                .create("serverName");
        serverNameOpt.setRequired(true);
        options.addOption(serverNameOpt);

        Option initialiseSkinsFromSvnOpt = new Option("initialiseSkinsFromSvn", false, "Initialise Skins from SVN");
        initialiseSkinsFromSvnOpt.setRequired(false);
        options.addOption(initialiseSkinsFromSvnOpt);

        Option ajpPortOpt = OptionBuilder.withArgName("port")
                .hasArg()
                .withDescription("The port number the jk listener will listen on")
                .create("ajpPort");
        ajpPortOpt.setRequired(false);
        options.addOption(ajpPortOpt);

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

        if (line.hasOption(initialiseSkinsFromSvnOpt.getOpt())) {
            initialiseSkinsFromSvn = true;
        }

        if (line.hasOption(ajpPortOpt.getOpt())) {
            try {
                ajpPort = Integer.parseInt(line.getOptionValue(ajpPortOpt.getOpt()));
            } catch (NumberFormatException e) {
                new HelpFormatter().printHelp("java gc.engine.GCEngine", options);
                System.exit(-1);
            }
        }

        Engine.start(new GCEngine(false, ajpPort), args);
    }

    @Override
    public void onStart() {
        // some init stuff
        if (initialiseSkinsFromSvn) {
            initialiseSkinsFromSvn();
        }
        // start scheduled tasks
        ScheduledTaskManager scheduledTaskManager = (ScheduledTaskManager) Component.getInstance("scheduledTaskManager", true);
        scheduledTaskManager.setServerName(serverName);
        scheduledTaskManager.onStart();
    }

    @Override
    public void onShutdown() {
        // shutdown scheduled tasks
        ScheduledTaskManager scheduledTaskManager = (ScheduledTaskManager) Component.getInstance("scheduledTaskManager", true);
        scheduledTaskManager.onShutdown();
    }

    private void initialiseSkinsFromSvn() {
        SkinSvnInitialisation skinSvnInitialisation =
                (SkinSvnInitialisation) Component.getInstance("skinSvnInitialisation", true);
        skinSvnInitialisation.initialise();
    }
}