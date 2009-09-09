package com.amee.domain.auth;

import com.amee.domain.AMEEEntityReference;
import com.amee.domain.AMEEEnvironmentEntity;
import com.amee.domain.IAMEEEntityReference;
import com.amee.domain.ObjectType;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.persistence.*;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * Permission represents the permissions that a 'principle' has over an 'entity'. The
 * entity can be any persistent entity within AMEE, such as a Profile or DataCategory. A
 * principle can be a User, Group or other entity that needs to own or access an entity.
 * <p/>
 * The principle and entity are represented by AMEEEntityReference instances, the
 * principleReference and entityReference properties, respectively.
 * <p/>
 * Permissions are made up of permission entries. These typically represent the 'view',
 * 'create', 'modify', 'delete' verbs (CRUD). Each permission entry is accompanied
 * by an allow or deny flag. Other types of entries are also possible, such as
 * 'own' or 'view-deprecated'.
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

    public final static int OBJECT_CLASS_MAX_SIZE = 255;
    public final static int ENTRIES_MAX_SIZE = 1000;

    /**
     * Constants for the various commonly used permission entry values.
     */
    public final static PermissionEntry OWN = new PermissionEntry("own");
    public final static PermissionEntry VIEW = new PermissionEntry("view");
    public final static PermissionEntry VIEW_DEPRECATED = new PermissionEntry("view.deprecated");
    public final static PermissionEntry CREATE = new PermissionEntry("create");
    public final static PermissionEntry MODIFY = new PermissionEntry("modify");
    public final static PermissionEntry DELETE = new PermissionEntry("delete");

    /**
     * Helpful PermissionEntry Sets which represent combinations of PermissionEntries.
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

    /**
     * The entity that this permission is governing access to.
     */
    @Embedded
    private AMEEEntityReference entityReference = new AMEEEntityReference();

    /**
     * The principle that this permission is defining access permissions for.
     */
    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "entityId", column = @Column(name = "PRINCIPLE_ID")),
            @AttributeOverride(name = "entityUid", column = @Column(name = "PRINCIPLE_UID")),
            @AttributeOverride(name = "entityType", column = @Column(name = "PRINCIPLE_TYPE"))})
    private AMEEEntityReference principleReference = new AMEEEntityReference();

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

    public Permission(IAMEEEntityReference principle, IAMEEEntityReference entity, PermissionEntry entry) {
        this();
        setPrincipleReference(new AMEEEntityReference(principle));
        setEntityReference(new AMEEEntityReference(entity));
        addEntry(entry);
    }

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
        JSONObject obj = new JSONObject();
        obj.put("uid", getUid());
        obj.put("created", getCreated());
        obj.put("modified", getModified());
        obj.put("environmentUid", getEnvironment().getUid());
        return obj;
    }

    public Element getElement(Document document) {
        Element element = document.createElement("Permission");
        element.setAttribute("uid", getUid());
        element.setAttribute("created", getCreated().toString());
        element.setAttribute("modified", getModified().toString());
        element.appendChild(getEnvironment().getIdentityElement(document));
        return element;
    }

    /**
     * Returns the entityReference instance that associates this Permission
     * with the entity that the principle has permissions over.
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
     * Returns the principleReference instance that associates this Permission
     * with the principle that has permissions over the entity.
     *
     * @return the entityReference instance
     */
    public AMEEEntityReference getPrincipleReference() {
        return principleReference;
    }

    /**
     * Update this Permission with the supplied principleReference representing
     * the principle for this Permission.
     *
     * @param principleReference instance to set
     */
    public void setPrincipleReference(AMEEEntityReference principleReference) {
        if (principleReference != null) {
            this.principleReference = principleReference;
        }
    }

    /**
     * Returns an immutable Set of PermissionEntry instances. As PermissionEntry
     * instances are also immutable the returned set represents a read-only
     * view of the permission entries for this Permission instance. Use the addEntry
     * and removeEntry methods to modify the internal representation of entries.
     *
     * @return
     */
    public Set<PermissionEntry> getEntries() {
        if (entrySet == null) {
            entrySet = new HashSet<PermissionEntry>();
            JSONObject json = getEntriesJSONObject();
            try {
                if (json.has("entries")) {
                    JSONArray arr = json.getJSONArray("entries");
                    for (int i = 0; i < arr.length(); i++) {
                        JSONObject obj = arr.getJSONObject(i);
                        String value = obj.has("value") ? obj.getString("value") : "";
                        String allow = obj.has("allow") ? obj.getString("allow") : "true";
                        entrySet.add(new PermissionEntry(value, allow));
                    }
                }
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
                obj.put("value", entry.getValue());
                if (!entry.isAllow()) {
                    obj.put("allow", "false");
                }
                arr.put(obj);
            }
            JSONObject json = new JSONObject();
            json.put("entries", arr);
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

    @Override
    public ObjectType getObjectType() {
        return ObjectType.PRM;
    }
}
