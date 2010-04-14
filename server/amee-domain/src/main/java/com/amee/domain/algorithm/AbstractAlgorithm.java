package com.amee.domain.algorithm;

import com.amee.core.APIUtils;
import com.amee.domain.AMEEEnvironmentEntity;
import com.amee.domain.TimeZoneHolder;
import com.amee.domain.environment.Environment;
import com.amee.platform.science.StartEndDate;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.Index;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.persistence.*;

@Entity
@Inheritance
@Table(name = "ALGORITHM")
@DiscriminatorColumn(name = "TYPE", length = 3)
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
public abstract class AbstractAlgorithm extends AMEEEnvironmentEntity {

    public final static int NAME_SIZE = 255;

    @Column(name = "NAME", length = NAME_SIZE, nullable = false)
    @Index(name = "NAME_IND")
    private String name = "";

    @Lob
    @Column(name = "CONTENT", nullable = true)
    private String content = "";

    public AbstractAlgorithm() {
        super();
    }

    public AbstractAlgorithm(Environment environment) {
        super(environment);
    }

    public AbstractAlgorithm(Environment environment, String content) {
        super(environment);
        setContent(content);
    }

    public JSONObject getJSONObject() throws JSONException {
        return getJSONObject(true);
    }

    public JSONObject getJSONObject(boolean detailed) throws JSONException {
        JSONObject obj = new JSONObject();
        obj.put("uid", getUid());
        obj.put("name", getName());
        obj.put("content", getContent());
        if (detailed) {
            obj.put("created", StartEndDate.getLocalStartEndDate(getCreated(), TimeZoneHolder.getTimeZone()).toDate());
            obj.put("modified", StartEndDate.getLocalStartEndDate(getModified(), TimeZoneHolder.getTimeZone()).toDate());
            obj.put("environment", getEnvironment().getIdentityJSONObject());
        }
        return obj;
    }

    public JSONObject getIdentityJSONObject() throws JSONException {
        return APIUtils.getIdentityJSONObject(this);
    }

    public Element getElement(Document document) {
        return getElement(document, true);
    }

    public abstract String getElementName();

    public Element getElement(Document document, boolean detailed) {
        Element element = document.createElement(getElementName());
        element.setAttribute("uid", getUid());
        element.appendChild(APIUtils.getElement(document, "Name", getName()));
        element.appendChild(APIUtils.getElement(document, "Content", getContent()));
        if (detailed) {
            element.setAttribute("created",
                    StartEndDate.getLocalStartEndDate(getCreated(), TimeZoneHolder.getTimeZone()).toDate().toString());
            element.setAttribute("modified",
                    StartEndDate.getLocalStartEndDate(getModified(), TimeZoneHolder.getTimeZone()).toDate().toString());
            element.appendChild(getEnvironment().getIdentityElement(document));
        }
        return element;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        if (name == null) {
            name = "";
        }
        this.name = name;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        if (content == null) {
            content = "";
        }
        this.content = content;
    }
}
