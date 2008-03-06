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
/**
 * This file is part of AMEE Java Client Library.
 *
 * AMEE Java Client Library is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 *
 * AMEE Java Client Library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package net.dgen.amee.client.cache;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class AmeeObjectCacheImpl implements Serializable, AmeeObjectCache {

    private Map<String, AmeeObjectCacheEntry> cache = new HashMap<String, AmeeObjectCacheEntry>();

    public AmeeObjectCacheImpl() {
        super();
    }

    public void put(AmeeObjectCacheEntry objectCacheEntry) {
        cache.put(objectCacheEntry.getObjectReference().getUri(), objectCacheEntry);
    }

    public AmeeObjectCacheEntry get(String path) {
        return cache.get(path);
    }

    public boolean remove(String path) {
        return cache.remove(path) != null;
    }

    public void removeAll() {
        cache.clear();
    }
}
