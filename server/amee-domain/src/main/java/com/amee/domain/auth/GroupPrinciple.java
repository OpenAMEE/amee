package com.amee.domain.auth;

import com.amee.core.APIUtils;
import com.amee.domain.AMEEEntity;
import com.amee.domain.AMEEEntityReference;
import com.amee.domain.AMEEEnvironmentEntity;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.persistence.*;

/**
 * A GroupPrinciple joins a Group to a principle via an EntityReference.
 *
 * @author Diggory Briercliffe
 */
@Entity
@Table(name = "GROUP_PRINCIPLE")
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
public class GroupPrinciple extends AMEEEnvironmentEntity implements Comparable {

    /**
     * The Group that the principle is a member of.
     */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "GROUP_ID")
    private Group group;

    /**
     * The principle that is a member of the Group.
     */
    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "entityId", column = @Column(name = "PRINCIPLE_ID")),
            @AttributeOverride(name = "entityUid", column = @Column(name = "PRINCIPLE_UID")),
            @AttributeOverride(name = "entityType", column = @Column(name = "PRINCIPLE_TYPE"))})
    private AMEEEntityReference principleReference = new AMEEEntityReference();

    public GroupPrinciple() {
        super();
    }

    public GroupPrinciple(Group group, AMEEEntity principle) {
        super(group.getEnvironment());
        setGroup(group);
        setPrincipleReference(new AMEEEntityReference(principle));
    }

    public String toString() {
        return "GroupPrinciple_" + getUid();
    }

    public int compareTo(Object o) throws ClassCastException {
        if (this == o) return 0;
        if (equals(o)) return 0;
        GroupPrinciple groupPrinciple = (GroupPrinciple) o;
        return getUid().compareTo(groupPrinciple.getUid());
    }

    public JSONObject getJSONObject() throws JSONException {
        return getJSONObject(true);
    }

    public JSONObject getJSONObject(boolean detailed) throws JSONException {
        JSONObject obj = new JSONObject();
        obj.put("uid", getUid());
        obj.put("group", getGroup().getIdentityJSONObject());
        obj.put("principle", getPrincipleReference().getJSONObject());
        if (detailed) {
            obj.put("environment", getEnvironment().getIdentityJSONObject());
            obj.put("created", getCreated());
            obj.put("modified", getModified());
        }
        return obj;
    }

    public JSONObject getIdentityJSONObject() throws JSONException {
        return APIUtils.getIdentityJSONObject(this);
    }

    public Element getElement(Document document) {
        return getElement(document, true);
    }

    public Element getElement(Document document, boolean detailed) {
        Element element = document.createElement("GroupPrinciple");
        element.setAttribute("uid", getUid());
        element.appendChild(getGroup().getIdentityElement(document));
        element.appendChild(getPrincipleReference().getElement(document, "Principle"));
        if (detailed) {
            element.appendChild(getEnvironment().getIdentityElement(document));
            element.setAttribute("created", getCreated().toString());
            element.setAttribute("modified", getModified().toString());
        }
        return element;
    }

    public Element getIdentityElement(Document document) {
        return APIUtils.getIdentityElement(document, this);
    }

    public Group getGroup() {
        return group;
    }

    public void setGroup(Group group) {
        if (group != null) {
            this.group = group;
        }
    }

    public AMEEEntityReference getPrincipleReference() {
        return principleReference;
    }

    public void setPrincipleReference(AMEEEntityReference principleReference) {
        if (principleReference != null) {
            this.principleReference = principleReference;
        }
    }
}