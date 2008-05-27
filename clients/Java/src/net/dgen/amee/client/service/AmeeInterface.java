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

import net.dgen.amee.client.AmeeException;
import net.dgen.amee.client.util.Choice;
import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.HttpMethodBase;
import org.apache.commons.httpclient.methods.DeleteMethod;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.IOException;
import java.io.Serializable;
import java.util.List;

public class AmeeInterface implements Serializable {
    
    private final static Log log = LogFactory.getLog(AmeeInterface.class);
    
    public final static int RETRIES = 3;
    public final static int SUCCESS_OK = 200;
    public final static int CLIENT_ERROR_BAD_REQUEST = 400;
    public final static int CLIENT_ERROR_UNAUTHORIZED = 401;
    public final static int CLIENT_ERROR_FORBIDDEN = 403;
    public final static int CLIENT_ERROR_NOT_FOUND = 404;
    public final static int CLIENT_ERROR_METHOD_NOT_ALLOWED = 405;
    public final static int SERVER_ERROR_INTERNAL = 500;
    
    // TODO: inject
    private static AmeeContext ameeContext = AmeeContext.getInstance();
    
    private static AmeeInterface instance = new AmeeInterface();
    
    public static AmeeInterface getInstance() {
        return instance;
    }
    
    private AmeeInterface() {
        super();
    }
    
    // *** Authentication ***
    
    public boolean signIn() throws AmeeException {
        ameeContext.setAuthToken(null);
        if (ameeContext.isValid()) {
            PostMethod post = ameeContext.getPostMethod("/auth/signIn");
            post.addParameter("username", ameeContext.getUsername());
            post.addParameter("password", ameeContext.getPassword());

            try {
                ameeContext.getClient().executeMethod(post);
                Header headers[] = post.getResponseHeaders("authToken");
                if (headers.length > 0) {
                    ameeContext.setAuthToken(headers[0].getValue());
                }
            } catch (IOException e) {
                throw new AmeeException("Caught IOException: " + e.getMessage());
            } finally {
                post.releaseConnection();
            }
        }
        return ameeContext.getAuthToken() != null;
    }
    
    private void checkAuthenticated() throws AmeeException {
        if ((ameeContext.getAuthToken() == null)) {
            if (!signIn()) {
                throw new AmeeException("Could not authenticate.");
            }
        }
    }
    
    // *** API Calls ***
    
    public String getAmeeResource(String url) throws AmeeException {
        GetMethod get = null;
        checkAuthenticated();
        try {
            // prepare method
            get = ameeContext.getGetMethod(url);
            // execute method and allow retries
            execute(get);
            return get.getResponseBodyAsString();
        } catch (IOException e) {
            throw new AmeeException("Caught IOException: " + e.getMessage());
        } finally {
            if (get != null) {
                get.releaseConnection();
            }
        }
    }
    
    public String getAmeeResourceByPost(String url, List<Choice> parameters) throws AmeeException {
        return getAmeeResource(url, parameters, false);
    }
    
    public String getAmeeResourceByPut(String url, List<Choice> parameters) throws AmeeException {
        return getAmeeResource(url, parameters, true);
    }
    
    public String getAmeeResource(String url, List<Choice> parameters, boolean tunnelPut) throws AmeeException {
        PostMethod post = null;
        checkAuthenticated();
        try {
            // prepare method
            if (tunnelPut) {
                if (url.indexOf("?") == -1) {
                    url = url + "?";
                } else {
                    url = url + "&";
                }
                url = url + "method=put";
            }
            post = ameeContext.getPostMethod(url);
            for (Choice parameter : parameters) {
                post.addParameter(parameter.getName(), parameter.getValue());
            }
            // execute method and allow retries
            execute(post);
            return post.getResponseBodyAsString();
        } catch (IOException e) {
            throw new AmeeException("Caught IOException: " + e.getMessage());
        } finally {
            if (post != null) {
                post.releaseConnection();
            }
        }
    }
    
    public void deleteAmeeResource(String url) throws AmeeException {
        DeleteMethod delete = null;
        checkAuthenticated();
        try {
            // prepare method
            delete = ameeContext.getDeleteMethod(url);
            // execute method and allow retries
            execute(delete);
        } catch (IOException e) {
            throw new AmeeException("Caught IOException: " + e.getMessage());
        } finally {
            if (delete != null) {
                delete.releaseConnection();
            }
        }
    }
    
    // *** Utility ***
    
    private void execute(HttpMethodBase method) throws IOException, AmeeException {
        for (int i = 1; i < RETRIES; i++) {
            ameeContext.getClient().executeMethod(method);
            switch (method.getStatusCode()) {
                case SUCCESS_OK:
                    // done, don't retry
                    return;
                case CLIENT_ERROR_BAD_REQUEST:
                    // don't retry
                    throw new AmeeException("The request could not be understood (" + method.getURI() + ").");
                case CLIENT_ERROR_NOT_FOUND:
                    // don't retry
                    throw new AmeeException("The resource could not be found (" + method.getURI() + ").");
                case CLIENT_ERROR_FORBIDDEN:
                    // don't retry
                    throw new AmeeException("Access to this resource is forbidden (" + method.getURI() + ").");
                case CLIENT_ERROR_METHOD_NOT_ALLOWED:
                    // don't retry
                    throw new AmeeException("Method is not allowed (" + method.getURI() + ").");
                case CLIENT_ERROR_UNAUTHORIZED:
                    // authentication may have expired, try authenticating again
                    if (!signIn()) {
                        throw new AmeeException("Could not authenticate (" + method.getURI() + ").");
                    }
                    ameeContext.prepareHttpMethod(method);//re-auth fix
                    // allow retries
                    break;
                default:
                    // allow retries - like with 500s or something else
                    break;
            }
        }
        throw new AmeeException("Could not execute request (" + method.getURI() + ").");
    }
}