package gc.carbon;

import com.jellymold.kiwi.Environment;
import com.jellymold.kiwi.EnvironmentObject;
import com.jellymold.utils.domain.APIUtils;
import com.jellymold.utils.domain.DatedObject;
import com.jellymold.utils.domain.UidGen;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.persistence.*;
import java.util.Calendar;
import java.util.Date;

/**
 * This file is part of AMEE.
 * <p/>
 * AMEE is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 * <p/>
 * AMEE is free software and is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * <p/>
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * <p/>
 * Created by http://www.dgen.net.
 * Website http://www.amee.cc
 */
@Entity
@Table(name = "API_VERSION")
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
public class APIVersion implements EnvironmentObject, DatedObject {

    public static final String ONE_ZERO = "1.0";
    public final static int API_VERSION_SIZE = 3;

    @Id
    @GeneratedValue
    @Column(name = "ID")
    private Long id;

    @Column(name = "UID", unique = true, nullable = false, length = UID_SIZE)
    private String uid = "";

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "ENVIRONMENT_ID")
    private Environment environment;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "CREATED")
    private Date created = null;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "MODIFIED")
    private Date modified = null;

    @Column(name = "VERSION", length = API_VERSION_SIZE, nullable = true)
    private String version;

    public APIVersion() {
        super();
    }
    
    /**
     * Default constructor (initialises version to ONE_ZERO)
     *
     * @param environment the environment
     * @see #ONE_ZERO
     */
    public APIVersion(Environment environment) {
        this(APIVersion.ONE_ZERO, environment);
    }

    public APIVersion(String version, Environment environment) {
        this.version = version;
        setUid(UidGen.getUid());
        setEnvironment(environment);
    }

    public boolean equals(Object o) {
        return this == o || o instanceof APIVersion && o.toString().equals(version);
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String toString() {
        return version;
    }

    public JSONObject getJSONObject() throws JSONException {
        return getJSONObject(true);
    }

    public JSONObject getJSONObject(boolean detailed) throws JSONException {
        JSONObject obj = new JSONObject();
        obj.put("v", version);
        return obj;
    }

    public JSONObject getIdentityJSONObject() throws JSONException {
        JSONObject obj = APIUtils.getIdentityJSONObject(this);
        obj.put("v", getVersion());
        return obj;
    }

    public Element getElement(Document document) {
        return getElement(document, true);
    }

    public Element getElement(Document document, boolean detailed) {
        return APIUtils.getElement(document, "APIVersion", getVersion());
    }

    public Element getIdentityElement(Document document) {
        Element element = APIUtils.getIdentityElement(document, this);
        element.appendChild(APIUtils.getElement(document, "APIVersion", getVersion()));
        return element;
    }

    public boolean isVersionOne() {
        return version.equals(ONE_ZERO);
    }


    @PrePersist
    public void onCreate() {
        setCreated(Calendar.getInstance().getTime());
        setModified(getCreated());
    }

    @PreUpdate
    public void onModify() {
        setModified(Calendar.getInstance().getTime());
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        if (uid != null) {
            this.uid = uid;
        }
    }

    public Environment getEnvironment() {
        return environment;
    }

    public void setEnvironment(Environment environment) {
        if (environment != null) {
            this.environment = environment;
        }
    }

    public Date getCreated() {
        return created;
    }

    public void setCreated(Date created) {
        this.created = created;
    }

    public Date getModified() {
        return modified;
    }

    public void setModified(Date modified) {
        this.modified = modified;
    }
}