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
package net.dgen.amee.client.util;

// TODO: make these tolerant of query strings

public class UriUtils {

    public static String getLastPart(String uri) {
        int pos = uri.lastIndexOf("/");
        if (pos >= 0) {
            pos++;
            if (pos < uri.length()) {
                return uri.substring(pos);
            } else {
                uri = "";
            }
        }
        return uri;
    }

    public static String getParentUri(String uri) {
        String s = "";
        if (uri != null) {
            int i = uri.lastIndexOf("/");
            if (i >= 0) {
                return uri.substring(0, i);
            }
        }
        return s;
    }

    public static String getUriExceptFirstPart(String uri) {
        return getUriExceptXParts(uri, 1);
    }

    public static String getUriExceptFirstTwoParts(String uri) {
        return getUriExceptXParts(uri, 2);
    }

    public static String getUriExceptXParts(String uri, int pos) {
        String s = "";
        String[] parts = uri.split("/");
        for (int i = pos; i < parts.length; i++) {
            s = s + parts[i];
            if (i < parts.length - 1) {
                s = s + "/";
            }
        }
        return s;
    }

    public static String getUriFirstTwoParts(String uri) {
        String s = null;
        String[] parts = uri.split("/");
        if (parts.length >= 2) {
            s = parts[0] + "/" + parts[1];
        }
        return s;
    }
}
