package com.amee.domain.auth;

import com.amee.domain.*;
import com.amee.domain.environment.Environment;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.persistence.*;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * Permission represents the permissions (rights) that a 'principal' has over an 'entity'.
 * The entity can be any persistent entity within AMEE, such as a Profile or DataCategory. A
 * principal can be a User, Group or other entity that needs to own or access an entity.
 * <p/>
 * The principal and entity are represented by AMEEEntityReference instances, the
 * principalReference and entityReference properties, respectively.
 * <p/>
 * Permissions are made up of permission entries. These typically represent the 'view',
 * 'create', 'modify', 'delete' verbs (CRUD). Each permission entry is accompanied
 * by an allow or deny flag. Other types of entries are also possible, such as
 * 'own' or 'create.profile'.
 * <p/>
 * The permission entries are stored in the entries property. Internally this is
 * held and persisted as a JSON String. The entries are exposed externally as a
 * Set of PermissionEntry instances. The entries String is automatically managed
 * by Permission.
 *
 * @author Diggory Briercliffe
 */
@Entity
@Table(name = "PERMISSION")
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
public class Permission extends AMEEEnvironmentEntity implements Comparable {

    public final static int ENTRIES_MAX_SIZE = 1000;

    /**
     * The entity that this permission is governing access to.
     */
    @Embedded
    private AMEEEntityReference entityReference = new AMEEEntityReference();

    /**
     * The principal that this permission is defining access permissions for.
     */
    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "entityId", column = @Column(name = "PRINCIPAL_ID")),
            @AttributeOverride(name = "entityUid", column = @Column(name = "PRINCIPAL_UID")),
            @AttributeOverride(name = "entityType", column = @Column(name = "PRINCIPAL_TYPE"))})
    private AMEEEntityReference principalReference = new AMEEEntityReference();

    /**
     * A String containing permission entries structured as JSON. These entries
     * are private but exposed via a Set of PermissionEntty instances.
     */
    @Column(name = "ENTRIES", length = ENTRIES_MAX_SIZE, nullable = false)
    @Lob
    private String entries = "";

    /**
     * Set of PermissionEntry instances. This represents the materialised view of
     * the entries JSON String. This property is managed internally and not exposed in
     * a form that can be manipulated.
     */
    @Transient
    private Set<PermissionEntry> entrySet;

    public Permission() {
        super();
    }

    public Permission(Environment environment) {
        this();
        setEnvironment(environment);
    }

    public Permission(Environment environment, IAMEEEntityReference principal, IAMEEEntityReference entity) {
        this(environment);
        setPrincipalReference(new AMEEEntityReference(principal));
        setEntityReference(new AMEEEntityReference(entity));
    }

    public Permission(Environment environment, IAMEEEntityReference principal, IAMEEEntityReference entity, PermissionEntry entry) {
        this(environment, principal, entity);
        addEntry(entry);
    }

    public Permission(Environment environment, IAMEEEntityReference principal, IAMEEEntityReference entity, Collection<PermissionEntry> entries) {
        this(environment, principal, entity);
        addEntries(entries);
    }

    @Override
    public String toString() {
        return "Permission_" + getUid();
    }

    public int compareTo(Object o) {
        if (this == o) return 0;
        if (equals(o)) return 0;
        Permission permission = (Permission) o;
        return getUid().compareTo(permission.getUid());
    }

    public JSONObject getJSONObject() throws JSONException {
        return getJSONObject(false);
    }

    public JSONObject getJSONObject(boolean detailed) throws JSONException {
        JSONObject obj = new JSONObject();
        obj.put("uid", getUid());
        JSONArray entriesArr = new JSONArray();
        for (PermissionEntry pe : this.getEntries()) {
            entriesArr.put(pe.getJSONObject());
        }
        obj.put("entries", entriesArr);
        obj.put("entity", getEntityReference().getJSONObject());
        obj.put("principal", getPrincipalReference().getJSONObject());
        if (detailed) {
            obj.put("created", getCreated());
            obj.put("modified", getModified());
            obj.put("environmentUid", getEnvironment().getUid());
        }
        return obj;
    }

    public Element getElement(Document document) {
        return getElement(document, false);
    }

    public Element getElement(Document document, boolean detailed) {
        Element element = document.createElement("Permission");
        element.setAttribute("uid", getUid());
        Element entriesElement = document.createElement("Entries");
        for (PermissionEntry pe : this.getEntries()) {
            entriesElement.appendChild(pe.getElement(document));
        }
        element.appendChild(entriesElement);
        element.appendChild(getEntityReference().getElement(document, "Entity"));
        element.appendChild(getPrincipalReference().getElement(document, "Principal"));
        if (detailed) {
            element.setAttribute("created", getCreated().toString());
            element.setAttribute("modified", getModified().toString());
            element.appendChild(getEnvironment().getIdentityElement(document));
        }
        return element;
    }

    /**
     * Returns the entityReference instance that associates this Permission
     * with the entity that the principal has permissions over.
     *
     * @return the entityReference instance
     */
    public AMEEEntityReference getEntityReference() {
        return entityReference;
    }

    /**
     * Update this Permission with the supplied entityReference representing the
     * entity for this Permission.
     *
     * @param entityReference instance to set
     */
    public void setEntityReference(AMEEEntityReference entityReference) {
        if (entityReference != null) {
            this.entityReference = entityReference;
        }
    }

    /**
     * Returns the principalReference instance that associates this Permission
     * with the principal that has permissions over the entity.
     *
     * @return the entityReference instance
     */
    public AMEEEntityReference getPrincipalReference() {
        return principalReference;
    }

    /**
     * Update this Permission with the supplied principalReference representing
     * the principal for this Permission.
     *
     * @param principalReference instance to set
     */
    public void setPrincipalReference(AMEEEntityReference principalReference) {
        if (principalReference != null) {
            this.principalReference = principalReference;
        }
    }

    /**
     * Returns an immutable Set of PermissionEntry instances. As PermissionEntry
     * instances are also immutable the returned set represents a read-only
     * view of the permission entries for this Permission instance. Use the addEntry
     * and removeEntry methods to modify the internal representation of entries.
     *
     * @return set of PermissionEntries
     */
    public Set<PermissionEntry> getEntries() {
        if (entrySet == null) {
            entrySet = new HashSet<PermissionEntry>();
            JSONObject json = getEntriesJSONObject();
            try {
                if (json.has("e")) {
                    JSONArray arr = json.getJSONArray("e");
                    for (int i = 0; i < arr.length(); i++) {
                        JSONObject obj = arr.getJSONObject(i);
                        String value = obj.has("v") ? obj.getString("v") : "";
                        String allow = obj.has("a") ? obj.getString("a") : "1";
                        AMEEStatus status = obj.has("s") ? AMEEStatus.values()[Integer.valueOf(obj.getString("s"))] : AMEEStatus.ACTIVE;
                        entrySet.add(new PermissionEntry(value, allow.equals("1"), status));
                    }
                }
            } catch (NumberFormatException e) {
                throw new RuntimeException("Caught NumberFormatException: " + e.getMessage(), e);
            } catch (JSONException e) {
                throw new RuntimeException("Caught JSONException: " + e.getMessage(), e);
            }
        }
        return Collections.unmodifiableSet(entrySet);
    }

    /**
     * Add a PermissionEntry to the entries. Will internally make sure the entries
     * set and string are updated.
     *
     * @param entry to add
     */
    public void addEntry(PermissionEntry entry) {
        // make sure entrySet is exists
        getEntries();
        // add the entry to entrySet
        entrySet.add(entry);
        // update the entries string
        updateEntriesJSONObject();
    }

    /**
     * Add PermissionEntries to the entries. Will internally make sure the entries
     * set and string are updated.
     *
     * @param entries to add
     */
    public void addEntries(Collection<PermissionEntry> entries) {
        // make sure entrySet is exists
        getEntries();
        // add the entries to entrySet
        entrySet.addAll(entries);
        // update the entries string
        updateEntriesJSONObject();
    }

    /**
     * Remove a PermissionEntry, matching the supplied PermissionEntry, from the
     * entries. Matching is based on the identity of the PermissionEntry as defined
     * by the equals method. Will internally make sure the entries set and
     * string are updated.
     *
     * @param entry to remove
     */
    public void removeEntry(PermissionEntry entry) {
        // TODO: Implement this.
        throw new UnsupportedOperationException();
    }

    private void updateEntriesJSONObject() {
        try {
            JSONArray arr = new JSONArray();
            for (PermissionEntry entry : getEntries()) {
                JSONObject obj = new JSONObject();
                obj.put("v", entry.getValue());
                if (!entry.isAllow()) {
                    obj.put("a", "0");
                }
                if (!entry.getStatus().equals(AMEEStatus.ACTIVE)) {
                    obj.put("s", entry.getStatus().ordinal());
                }
                arr.put(obj);
            }
            JSONObject json = new JSONObject();
            json.put("e", arr);
            entries = json.toString();
        } catch (JSONException e) {
            throw new RuntimeException("Caught JSONException: " + e.getMessage(), e);
        }
    }

    private JSONObject getEntriesJSONObject() {
        try {
            return new JSONObject(entries);
        } catch (JSONException e) {
            return new JSONObject();
        }
    }

    public ObjectType getObjectType() {
        return ObjectType.PRM;
    }
}
