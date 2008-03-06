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
package net.dgen.amee.client.cache;

import java.io.Serializable;

public class AmeeObjectCacheDummyImpl implements Serializable, AmeeObjectCache {

    public AmeeObjectCacheDummyImpl() {
        super();
    }

    public void put(AmeeObjectCacheEntry objectCacheEntry) {
        // do nothing
    }

    public AmeeObjectCacheEntry get(String path) {
        return null;
    }

    public boolean remove(String path) {
        return false;
    }

    public void removeAll() {
        // do nothing
    }
}