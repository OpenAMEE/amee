package com.jellymold.kiwi.account;
//
//import com.jellymold.kiwi.User;
//import com.jellymold.kiwi.auth.Authorized;
//import com.jellymold.utils.BaseResource;
//import org.jboss.seam.ScopeType;
//////import org.jboss.seam.annotations.Scope;
//import org.json.JSONException;
//import org.json.JSONObject;
//import org.restlet.Context;
//import org.restlet.data.Request;
//import org.restlet.data.Response;
//
//import java.util.Map;
//
//@Name("accountDetailsResource")
//@Scope("prototype")
//@Authorized(actions = "account.details.view")
//public class AccountDetailsResource extends BaseResource {
//
//    public final static String VIEW_DETAILS = "account/details.ftl";
//
//    @Autowired
//    User user;
//
//    public AccountDetailsResource() {
//        super();
//    }
//
//    public AccountDetailsResource(Context context, Request request, Response response) {
//        super(context, request, response);
//    }
//
//    public String getTemplatePath() {
//        return VIEW_DETAILS;
//    }
//
//    public Map<String, Object> getTemplateValues() {
//        Map<String, Object> values = super.getTemplateValues();
//        values.put("user", user);
//        return values;
//    }
//
//    public JSONObject getJSONObject() throws JSONException {
//        JSONObject obj = new JSONObject();
//        return obj;
//    }
//}