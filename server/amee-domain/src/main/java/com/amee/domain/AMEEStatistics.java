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
package com.amee.domain;

import org.springframework.stereotype.Service;

@Service("ameeStatistics")
public class AMEEStatistics {

    // Profile Items
    private long profileItemCreateCount;
    private ThreadLocal<Long> threadProfileItemCreateCount = new ThreadLocal<Long>();
    private long profileItemUpdateCount;
    private ThreadLocal<Long> threadProfileItemUpdateCount = new ThreadLocal<Long>();

    // Errors
    private long errorCount;

    // Thread state

    /**
     * Reset the state of counters for this thread. Values are set to zero. This must be called at least
     * once prior calling any of the update methods, such as createProfileItem.
     */
    public void resetThread() {
        threadProfileItemCreateCount.set(0L);
        threadProfileItemUpdateCount.set(0L);
    }

    public void commitThread() {
        // only commit if values are present
        if (threadProfileItemCreateCount.get() != null) {
            // Profile Items
            profileItemCreateCount += threadProfileItemCreateCount.get();
            profileItemUpdateCount += threadProfileItemUpdateCount.get();
            // reset for subsequent requests
            resetThread();
        }
    }

    // Profile Items

    public void createProfileItem() {
        threadProfileItemCreateCount.set(threadProfileItemCreateCount.get() + 1);
    }

    public long getProfileItemCreateCount() {
        return profileItemCreateCount;
    }

    public void updateProfileItem() {
        threadProfileItemUpdateCount.set(threadProfileItemUpdateCount.get() + 1);
    }

    public long getProfileItemUpdateCount() {
        return profileItemUpdateCount;
    }

    // Errors

    public void error() {
        errorCount++;
    }

    public long getErrorCount() {
        return errorCount;
    }
}