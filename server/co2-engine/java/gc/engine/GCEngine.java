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
import com.jellymold.kiwi.UserPasswordToMD5;
import com.jellymold.kiwi.environment.ScheduledTaskManager;
import org.apache.commons.cli.*;

public class GCEngine extends Engine {

    public GCEngine() {
        super();
    }

    public GCEngine(String serverName) {
        super(serverName);
    }

    public static void main(String[] args) {

        String serverName = "localhost";

        Options options = new Options();

        Option serverNameOpt = OptionBuilder.withArgName("serverName")
                .hasArg()
                .withDescription("The server name")
                .create("serverName");
        serverNameOpt.setRequired(true);
        options.addOption(serverNameOpt);

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

        Engine.start(new GCEngine(serverName), args);
    }

    @Override
    public void onStart() {
        ScheduledTaskManager taskMan = (ScheduledTaskManager) springContext.getBean("scheduledTaskManager");
        taskMan.setServerName(serverName);
        taskMan.onStart();

        if (System.getProperty("amee.userPasswordToMD5") != null) {
            // TODO: TEMPORARY CODE!!!! Remove once all databases have been migrated. Get rid of UserPasswordToMD5 too.
            UserPasswordToMD5 userPasswordToMD5 =
                    (UserPasswordToMD5) springContext.getBean("userPasswordToMD5");
            userPasswordToMD5.updateUserPasswordToMD5(false);
        }
    }

    @Override
    public void onShutdown() {
        ScheduledTaskManager taskMan = (ScheduledTaskManager) springContext.getBean("scheduledTaskManager");
        taskMan.onShutdown();
    }
}