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
package com.amee.domain.auth;

import org.apache.commons.lang.StringUtils;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

public class PermissionEntry implements Serializable {

    /**
     * Constants for the various permission entry values.
     */
    public final static PermissionEntry OWN = new PermissionEntry("own");
    public final static PermissionEntry VIEW = new PermissionEntry("view");
    public final static PermissionEntry VIEW_DEPRECATED = new PermissionEntry("view-deprecated");
    public final static PermissionEntry CREATE = new PermissionEntry("create");
    public final static PermissionEntry MODIFY = new PermissionEntry("modify");
    public final static PermissionEntry DELETE = new PermissionEntry("delete");

    /**
     * Helpful PermissionEntry Sets.
     */
    public final static Set<PermissionEntry> OWN_VIEW = new HashSet<PermissionEntry>();
    public final static Set<PermissionEntry> OWN_CREATE = new HashSet<PermissionEntry>();
    public final static Set<PermissionEntry> OWN_MODIFY = new HashSet<PermissionEntry>();
    public final static Set<PermissionEntry> OWN_DELETE = new HashSet<PermissionEntry>();

    /**
     * Populate PermissionEntry Sets.
     */
    {
        OWN_VIEW.add(OWN);
        OWN_VIEW.add(VIEW);
        OWN_CREATE.add(OWN);
        OWN_CREATE.add(CREATE);
        OWN_MODIFY.add(OWN);
        OWN_MODIFY.add(MODIFY);
        OWN_DELETE.add(OWN);
        OWN_DELETE.add(DELETE);
    }

    private String value = "";
    private boolean allow = true;

    private PermissionEntry() {
        super();
    }

    public PermissionEntry(String value) {
        this();
        setValue(value);
    }

    public PermissionEntry(String value, boolean allow) {
        this(value);
        setAllow(allow);
    }

    public PermissionEntry(String value, String allow) {
        this(value, Boolean.valueOf(allow));
    }

    public String toString() {
        return "PermissionEntry_" + getValue() + "_" + (isAllow() ? "allow" : "deny");
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!PermissionEntry.class.isAssignableFrom(o.getClass())) return false;
        PermissionEntry entry = (PermissionEntry) o;
        return getValue().equals(entry.getValue());
    }

    @Override
    public int hashCode() {
        return getValue().hashCode();
    }

    public String getValue() {
        return value;
    }

    private void setValue(String value) {
        if (StringUtils.isBlank(value)) throw new IllegalArgumentException();
        this.value = value.trim().toLowerCase();
    }

    public boolean isAllow() {
        return allow;
    }

    private void setAllow(boolean allow) {
        this.allow = allow;
    }
}