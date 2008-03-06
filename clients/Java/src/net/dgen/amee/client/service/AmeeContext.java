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
package net.dgen.amee.client.service;

import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.cookie.CookiePolicy;
import org.apache.commons.httpclient.methods.DeleteMethod;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.PutMethod;

import java.io.Serializable;

public class AmeeContext implements Serializable {

    public static String BASE_URL = "http://stage.co2.dgen.net";
    public static Header ACCEPT_JSON = new Header("Accept", "application/json");

    private String baseUrl = BASE_URL;
    private HttpClient client = null;
    private String username = null;
    private String password = null;
    private String authToken = null;

    private static AmeeContext instance = new AmeeContext("load", "l04d");

    public static AmeeContext getInstance() {
        return instance;
    }

    private AmeeContext() {
        super();
        setClient(new HttpClient());
    }

    private AmeeContext(String username, String password) {
        this();
        setUsername(username);
        setPassword(password);
    }

    public GetMethod getGetMethod(String path) {
        return (GetMethod) prepareHttpMethod(new GetMethod(getBaseUrl() + preparePath(path)));
    }

    public PostMethod getPostMethod(String path) {
        return (PostMethod) prepareHttpMethod(new PostMethod(getBaseUrl() + preparePath(path)));
    }

    public PutMethod getPutMethod(String path) {
        return (PutMethod) prepareHttpMethod(new PutMethod(getBaseUrl() + preparePath(path)));
    }

    public DeleteMethod getDeleteMethod(String path) {
        return (DeleteMethod) prepareHttpMethod(new DeleteMethod(getBaseUrl() + preparePath(path)));
    }

    public HttpMethod prepareHttpMethod(HttpMethod method) {
        method.addRequestHeader(ACCEPT_JSON);
        method.setFollowRedirects(false);
        method.getParams().setCookiePolicy(CookiePolicy.IGNORE_COOKIES);
        if (getAuthToken() != null) {
            method.addRequestHeader("authToken", getAuthToken());
        }
        return method;
    }

    public static String preparePath(String path) {
        if (path == null) {
            path = "/";
        }
        if (!path.startsWith("/")) {
            path = "/" + path;
        }
        return path;
    }

    public boolean isValid() {
        return (getUsername() != null) && (getPassword() != null);
    }

    public String getBaseUrl() {
        return baseUrl;
    }

    public void setBaseUrl(String baseUrl) {
        if (baseUrl == null) {
            baseUrl = BASE_URL;
        }
        this.baseUrl = baseUrl;
    }

    public HttpClient getClient() {
        return client;
    }

    public void setClient(HttpClient client) {
        if (client == null) {
            client = new HttpClient();
        }
        this.client = client;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getAuthToken() {
        return authToken;
    }

    public void setAuthToken(String authToken) {
        this.authToken = authToken;
    }
}