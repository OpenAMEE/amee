package com.jellymold.kiwi.auth;

import org.restlet.Context;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.io.Serializable;

/**
 * This extends SignInResource for backwards compatibilty. This means /auth has the same behaviour as /auth/signIn.
 */
@Component
@Scope("prototype")
public class AuthResource extends SignInResource implements Serializable {

    public final static String VIEW_AUTH = "auth/home.ftl";

    @Override
    public void init(Context context, Request request, Response response) {
        super.init(context, request, response);
        setAvailable(isValid());
    }

    @Override
    public String getTemplatePath() {
        return VIEW_AUTH;
    }
}