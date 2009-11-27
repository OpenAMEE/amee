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

import com.amee.domain.AMEEEntity;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * AuthorizationContext encapsulates the 'context' for an authorization request. The context
 * contains three main collections; a list of 'principals', a list of 'entities' and a set of
 * PermissionEntries. These collections are taken into consideration when deciding if a
 * request should be authorized.
 */
public class AuthorizationContext implements Serializable {

    /**
     * A list of principals which may or may not be authorized for the entities.
     */
    private List<AMEEEntity> principals = new ArrayList<AMEEEntity>();

    /**
     * A list of entities over which the principals may or may not have permissions, along with
     * the requested permission entries.
     */
    private List<AccessSpecification> accessSpecifications = new ArrayList<AccessSpecification>();

    /**
     * A set of PermissionEntries representing the consolidated & inherited access rights following
     * execution of AuthorizationService.isAuthorized(). The PermissionEntries will reflect the state-of-play at
     * the point of AuthorizationService.isAuthorized() returning ALLOW or DENY for the authorization check.
     */
    private Set<PermissionEntry> entries = new HashSet<PermissionEntry>();

    /**
     * Local cache for the result of AuthorizationService.isAuthorized().
     */
    private Boolean authorized = null;

    /**
     * Indicates if a User that is a super-user is one of the principals.
     */
    private boolean superUser = false;

    /**
     * Default constructor.
     */
    public AuthorizationContext() {
        super();
    }

    /**
     * Convienience method to add a principal to the principals collection.
     *
     * @param principal to add
     */
    public void addPrincipal(AMEEEntity principal) {
        if (principal == null) throw new IllegalArgumentException("The principal argument must not be null.");
        getPrincipals().add(principal);
    }

    /**
     * Convienience method to add a list of principals to the principals collections.
     *
     * @param principals to add
     */
    public void addPrincipals(List<AMEEEntity> principals) {
        if (principals == null) throw new IllegalArgumentException("The principals argument must not be null.");
        getPrincipals().addAll(principals);
    }

    /**
     * Convienience method to add an access specification to the accessSpecifications collection.
     *
     * @param accessSpecification to add
     */
    public void addAccessSpecification(AccessSpecification accessSpecification) {
        if (accessSpecification == null)
            throw new IllegalArgumentException("The accessSpecification argument must not be null.");
        getAccessSpecifications().add(accessSpecification);
    }

    /**
     * Convienience method to add a list of access specifications to the accessSpecifications collection.
     *
     * @param accessSpecifications to add
     */
    public void addAccessSpecifications(List<AccessSpecification> accessSpecifications) {
        if (accessSpecifications == null)
            throw new IllegalArgumentException("The accessSpecifications argument must not be null.");
        getAccessSpecifications().addAll(accessSpecifications);
    }

    /**
     * Reset the AuthorizationContext. All collections are cleared.
     */
    public void reset() {
        authorized = null;
        principals.clear();
        accessSpecifications.clear();
        entries.clear();
    }

    public JSONObject getJSONObject() throws JSONException {
        JSONObject obj = new JSONObject();
        JSONArray entries = new JSONArray();
        for (PermissionEntry entry : getEntries()) {
            entries.put(entry.getJSONObject());
        }
        obj.put("entries", entries);
        return obj;
    }

    /**
     * Returns the principals list.
     *
     * @return principals list
     */
    public List<AMEEEntity> getPrincipals() {
        return principals;
    }

    /**
     * Returns the accessSpecifications list.
     *
     * @return accessSpecifications list
     */
    public List<AccessSpecification> getAccessSpecifications() {
        return accessSpecifications;
    }

    /**
     * Returns the last AccessSpecifications from the accessSpecifications collection.
     *
     * @return the last AccessSpecifications or null
     */
    public AccessSpecification getLastAccessSpecifications() {
        if (!accessSpecifications.isEmpty()) {
            return accessSpecifications.get(accessSpecifications.size() - 1);
        } else {
            return null;
        }
    }

    /**
     * Returns the entries set;
     *
     * @return entries set
     */
    public Set<PermissionEntry> getEntries() {
        return entries;
    }

    /**
     * Returns true if this AuthorizationContext has been checked.
     *
     * @return true if this AuthorizationContext has been checked
     */
    public boolean hasBeenChecked() {
        return (authorized != null);
    }

    /**
     * Returns true if this AuthorizationContext is authorized.
     *
     * @return true if authorize result is allow, otherwise false if result is deny
     */
    public boolean isAuthorized() {
        return hasBeenChecked() && authorized;
    }

    /**
     * Set the authorized flag.
     *
     * @param authorized value to set
     */
    public void setAuthorized(boolean authorized) {
        this.authorized = authorized;
    }

    /**
     * Returns true if one of the priniples was a super-user.
     *
     * @return true if one of the priniples was a super-user
     */
    public boolean isSuperUser() {
        return superUser;
    }

    /**
     * Sets the super-user flag.
     *
     * @param superUser new value for the super-user flag
     */
    public void setSuperUser(Boolean superUser) {
        this.superUser = superUser;
    }
}