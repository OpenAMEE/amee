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
package com.amee.engine;

import org.apache.commons.dbcp.BasicDataSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service("dataSourceStatistics")
public class DataSourceStatistics {

    @Autowired
    private BasicDataSource dataSource;

    public int getInitialSize() {
        return dataSource.getInitialSize();
    }

    public int getMaxIdle() {
        return dataSource.getMaxIdle();
    }

    public int getMinIdle() {
        return dataSource.getMinIdle();
    }

    public int getNumIdle() {
        return dataSource.getNumIdle();
    }

    public int getMaxActive() {
        return dataSource.getMaxActive();
    }

    public int getNumActive() {
        return dataSource.getNumActive();
    }
}