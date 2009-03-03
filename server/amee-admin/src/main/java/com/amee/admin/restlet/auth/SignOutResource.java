package com.amee.admin.restlet.auth;

import com.amee.domain.APIUtils;
import com.amee.domain.auth.User;
import com.amee.restlet.BaseResource;
import com.amee.restlet.auth.AuthUtils;
import com.amee.service.auth.AuthService;
import org.json.JSONException;
import org.json.JSONObject;
import org.restlet.Context;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.io.Serializable;
import java.util.Map;

@Component
@Scope("prototype")
public class SignOutResource extends BaseResource implements Serializable {

    public final static String VIEW_SIGN_OUT = "auth/signOut.ftl";

    @Autowired
    private AuthService authService;

    private User user;

    @Override
    public void init(Context context, Request request, Response response) {
        super.init(context, request, response);
        // sign out the current User and sign in the guest auth instead
        AuthUtils.discardAuthCookie(response);
        user = authService.doGuestSignIn();
        setAvailable(isValid());
    }

    @Override
    public String getTemplatePath() {
        return VIEW_SIGN_OUT;
    }

    @Override
    public Map<String, Object> getTemplateValues() {
        Map<String, Object> values = super.getTemplateValues();
        values.put("next", AuthUtils.getNextUrl(getRequest(), getForm()));
        return values;
    }

    @Override
    public JSONObject getJSONObject() throws JSONException {
        JSONObject obj = new JSONObject();
        obj.put("next", AuthUtils.getNextUrl(getRequest(), getForm()));
        if (user != null) {
            obj.put("auth", user.getJSONObject(false));
        }
        return obj;
    }

    @Override
    public Element getElement(Document document) {
        Element element = document.createElement("SignOutResource");
        element.appendChild(APIUtils.getElement(document, "Next", AuthUtils.getNextUrl(getRequest(), getForm())));
        if (user != null) {
            element.appendChild(user.getElement(document, false));
        }
        return element;
    }
}