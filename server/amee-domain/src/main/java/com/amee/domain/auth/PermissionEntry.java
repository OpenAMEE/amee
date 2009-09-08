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

/**
 * PermissionEntry represents individual permission entries of a Permission instance. Each
 * PermissionEntry instance is immutable. There are no setters and the default constructor
 * is private.
 * <p/>
 * PermissionEntry instances are considered equal if the value properties are identical. The
 * allow property is not taken into account. This allows PermissionEntries to be placed in
 * a Set where the entries with the same value cannot co-exist.
 * <p/>
 * PermissionEntries are intended to precisely specify something that a principle can do with
 * an entity, such as modify it or not delete it.
 */
public class PermissionEntry implements Serializable {

    /**
     * The 'value' of a PermissionEntry. Examples are 'view' or 'delete.
     */
    private String value = "";

    /**
     * Flag to declare if a PermissionEntry should allow or deny the permission
     * associated with the value property. For example, allow or deny a principle to
     * 'view' an entity.
     */
    private boolean allow = true;

    /**
     * Private default constructor, enforcing immutability for PermissionEntry instances.
     */
    private PermissionEntry() {
        super();
    }

    /**
     * Constructor to create a new PermissionEntry with the supplied value.
     *
     * @param value for new PermissionEntry
     */
    public PermissionEntry(String value) {
        this();
        setValue(value);
    }

    /**
     * Constructor to create a new PermissionEntry with the supplied value and allow state.
     *
     * @param value for new PermissionEntry
     * @param allow state to set, true or false
     */
    public PermissionEntry(String value, boolean allow) {
        this(value);
        setAllow(allow);
    }

    /**
     * Constructor to create a new PermissionEntry with the supplied value and allow state.
     *
     * @param value for new PermissionEntry
     * @param allow state to set, true or false
     */
    public PermissionEntry(String value, String allow) {
        this(value, Boolean.valueOf(allow));
    }

    public String toString() {
        return "PermissionEntry_" + getValue() + "_" + (isAllow() ? "allow" : "deny");
    }

    /**
     * Compare a PermissionEntry with the supplied object. Asides from
     * standard object equality, PermissionEntries are considered equal if they
     * have the same value.
     *
     * @param o to compare with
     * @return true if supplied object is equal
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!PermissionEntry.class.isAssignableFrom(o.getClass())) return false;
        PermissionEntry entry = (PermissionEntry) o;
        return getValue().equals(entry.getValue());
    }

    /**
     * Returns a hash code for a PermissionEntry. Internally uses the hash code of the value
     * property.
     *
     * @return the hash code
     */
    @Override
    public int hashCode() {
        return getValue().hashCode();
    }

    /**
     * Get the value of a PermissionEntry.
     *
     * @return
     */
    public String getValue() {
        return value;
    }

    private void setValue(String value) {
        if (StringUtils.isBlank(value)) throw new IllegalArgumentException();
        this.value = value.trim().toLowerCase();
    }

    /**
     * Returns true if the allow state of a PermissionEntry is true.
     *
     * @return
     */
    public boolean isAllow() {
        return allow;
    }

    private void setAllow(boolean allow) {
        this.allow = allow;
    }
}