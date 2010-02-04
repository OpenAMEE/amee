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

    // Profiles
    private long profileCreateCount;
    private ThreadLocal<Long> threadProfileCreateCount = new ThreadLocal<Long>() {
        protected Long initialValue() {
            return 0L;
        }
    };

    // Profile Items - Create
    private long profileItemCreateCount;
    private ThreadLocal<Long> threadProfileItemCreateCount = new ThreadLocal<Long>() {
        protected Long initialValue() {
            return 0L;
        }
    };

    // Profile Items - Modify
    private long profileItemModifyCount;
    private ThreadLocal<Long> threadProfileItemModifyCount = new ThreadLocal<Long>() {
        protected Long initialValue() {
            return 0L;
        }
    };

    // Profile Item Values - Create
    private long profileItemValueCreateCount;
    private ThreadLocal<Long> threadProfileItemValueCreateCount = new ThreadLocal<Long>() {
        protected Long initialValue() {
            return 0L;
        }
    };

    // Profile Item Values - Modify
    private long profileItemValueModifyCount;
    private ThreadLocal<Long> threadProfileItemValueModifyCount = new ThreadLocal<Long>() {
        protected Long initialValue() {
            return 0L;
        }
    };

    // Calculations - Seconds
    private long calculationDuration;

    // Calculations - Nanoseconds
    private ThreadLocal<Long> threadCalculationDuration = new ThreadLocal<Long>() {
        protected Long initialValue() {
            return 0L;
        }
    };

    // Transactions
    private long transactionCommitCount;
    private long transactionRollbackCount;

    // Errors
    private long errorCount;

    // Thread state

    /**
     * Reset the state of counters for this thread. Values are set to zero. This must be called at least
     * once prior calling any of the update methods, such as createProfileItem.
     */
    public void resetThread() {
        // Profiles
        threadProfileCreateCount.set(0L);
        // Profile Items
        threadProfileItemCreateCount.set(0L);
        threadProfileItemModifyCount.set(0L);
        // Profile Item Values
        threadProfileItemValueCreateCount.set(0L);
        threadProfileItemValueModifyCount.set(0L);
        // Calculations
        threadCalculationDuration.set(0L);
    }

    public void commitThread() {
        // only commit if values are present
        if (threadProfileCreateCount.get() != null) {
            // Profiles
            profileCreateCount += threadProfileCreateCount.get();
            // Profile Items
            profileItemCreateCount += threadProfileItemCreateCount.get();
            profileItemModifyCount += threadProfileItemModifyCount.get();
            // Profile Item Values
            profileItemValueCreateCount += threadProfileItemValueCreateCount.get();
            profileItemValueModifyCount += threadProfileItemValueModifyCount.get();
            // reset for subsequent requests
            resetThread();
        }
    }

    // Profiles

    public void createProfile() {
        threadProfileCreateCount.set(threadProfileCreateCount.get() + 1);
    }

    public long getProfileCreateCount() {
        return profileCreateCount;
    }

    // Profile Items

    public void createProfileItem() {
        threadProfileItemCreateCount.set(threadProfileItemCreateCount.get() + 1);
    }

    public long getProfileItemCreateCount() {
        return profileItemCreateCount;
    }

    public void updateProfileItem() {
        threadProfileItemModifyCount.set(threadProfileItemModifyCount.get() + 1);
    }

    public long getProfileItemModifyCount() {
        return profileItemModifyCount;
    }

    // Profile Item Values

    public void createProfileItemValue() {
        threadProfileItemValueCreateCount.set(threadProfileItemValueCreateCount.get() + 1);
    }

    public long getProfileItemValueCreateCount() {
        return profileItemValueCreateCount;
    }

    public void updateProfileItemValue() {
        threadProfileItemValueModifyCount.set(threadProfileItemValueModifyCount.get() + 1);
    }

    public long getProfileItemValueModifyCount() {
        return profileItemValueModifyCount;
    }

    // Errors

    public void error() {
        errorCount++;
    }

    public long getErrorCount() {
        return errorCount;
    }

    // Calculations

    public long getCalculationDuration() {
        return calculationDuration;
    }

    public void addToCalculationDuration(long duration) {
        if (duration > 0) {
            calculationDuration += (duration / 1000000000);
        }
    }

    public long getThreadCalculationDuration() {
        return threadCalculationDuration.get();
    }

    public void addToThreadCalculationDuration(long duration) {
        if (threadCalculationDuration.get() == null)
            threadCalculationDuration.set(0L);

        threadCalculationDuration.set(threadCalculationDuration.get() + duration);
        addToCalculationDuration(duration);
    }

    // Transactions

    public void transactionCommit() {
        transactionCommitCount++;
    }

    public long getTransactionCommitCount() {
        return transactionCommitCount;
    }

    public void transactionRollback() {
        transactionRollbackCount++;
    }

    public long getTransactionRollbackCount() {
        return transactionRollbackCount;
    }
}