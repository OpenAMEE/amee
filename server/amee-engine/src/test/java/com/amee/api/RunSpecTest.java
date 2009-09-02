/*
 * This file is part of AMEE.
 *
 * Copyright (c) 2007, 2008, 2009 AMEE UK LIMITED (help@amee.com).
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
package com.amee.api;

import static junit.framework.Assert.assertTrue;
import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.DefaultExecutor;
import org.apache.commons.exec.ExecuteWatchdog;

//TODO - Need to complete this class and plug-in to the maven lifecycle. 
//TODO - The intention is to have this run the spec tests and so allow the generation of cobertura resports
public class RunSpecTest {

    //@Test
    public void run() {

        try {
            CommandLine commandLine = CommandLine.parse("spec");
            commandLine.addArguments("/Development/AMEE/internal/tests/api");
            ExecuteWatchdog watchdog = new ExecuteWatchdog(300000);
            DefaultExecutor executor = new DefaultExecutor();
            executor.setWatchdog(watchdog);
            executor.setExitValue(1);
            int exitValue = executor.execute(commandLine);
            System.out.println("Process exitValue: " + exitValue);
            assertTrue(true);
        } catch (Throwable t) {
            t.printStackTrace();
            assertTrue(false);
	    }
    }
}
