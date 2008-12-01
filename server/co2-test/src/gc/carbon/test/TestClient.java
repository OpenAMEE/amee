package gc.carbon.test;

import org.restlet.data.*;
import org.restlet.util.Series;
import org.restlet.Client;
import org.restlet.resource.Representation;
import org.w3c.dom.Document;

import java.io.IOException;

public class TestClient {

    private static final String LOCAL_HOST_NAME = "local.stage.co2.dgen.net";
    private Reference reference = new Reference(Protocol.HTTP, LOCAL_HOST_NAME);
    private String category;

    private Series<CookieSetting> cookieSettings;
    private MediaType mediaType;

    public TestClient(String category) throws IOException {
        authenticate();
        setMediaType(MediaType.APPLICATION_XML);
        setPath(category);
    }

    private void authenticate() throws IOException {
        Client client = new Client(Protocol.HTTP);
        Reference uri = new Reference(Protocol.HTTP, LOCAL_HOST_NAME + "/auth/signIn?method=put");
        Form form = new Form();
        form.add("next", "auth");
        form.add("username", "admin");
        form.add("password", "r41n80w");
        Representation rep = form.getWebRepresentation();
        Response response = client.post(uri, rep);
        if (response.getStatus().isRedirection()) {
            cookieSettings = response.getCookieSettings();
        }
    }

    public void setQuery(String query) {
        reference.setQuery(query);
    }

    public void setMediaType(MediaType mediaType) {
        this.mediaType = mediaType;
    }

    public void setPath(String category) {
        this.category = category;
        this.reference.setPath(category);
    }

    private void addHeaders(Request request) {
        Form requestHeaders = new Form();
        requestHeaders.add("authToken", cookieSettings.getFirstValue("authToken"));
        request.getAttributes().put("org.restlet.http.headers", requestHeaders);
        request.getClientInfo().getAcceptedMediaTypes().add(new Preference<MediaType>(mediaType));
    }

    public Response get() {
        Client client = new Client(Protocol.HTTP);
        Request request = new Request(Method.GET, reference);
        addHeaders(request);
        return client.handle(request);
    }

    public Response post(Form form) {
        Client client = new Client(Protocol.HTTP);
        Request request = new Request(Method.POST, reference, form.getWebRepresentation());
        addHeaders(request);
        return client.handle(request);
    }

    public Response put(Form form) {
        Client client = new Client(Protocol.HTTP);
        Request request = new Request(Method.PUT, reference, form.getWebRepresentation());
        addHeaders(request);
        System.out.println(request.getResourceRef().getQuery());
        return client.handle(request);
    }

    public Response createProfileItemAndUpdate(Form putData) throws Exception {
        Form postData = new Form();
        copyIfPresent(putData,postData,"v");
        copyIfPresent(putData,postData,"dataItemUid");
        copyIfPresent(putData,postData,"name");
        String putPath = category + createProfileItem(postData);
        setPath(putPath);
        return put(putData);
    }

    private void copyIfPresent(Form source, Form target, String param) {
        if (source.getNames().contains(param))
            target.add(param,source.getFirstValue(param));
    }

    public String createProfileItem(Form data) throws Exception {
        return createItem(data, "ProfileItem");
    }

    public String createDateItem(Form data) throws Exception {
        return createItem(data, "DataItem");
    }

    private String createItem(Form data, String tagName) throws Exception {
        Document doc = post(data).getEntityAsDom().getDocument();
        return doc.getElementsByTagName(tagName).item(0).getAttributes().getNamedItem("uid").getNodeValue();
    }
}