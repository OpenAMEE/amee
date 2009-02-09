package gc.engine;

import org.restlet.Context;
import org.restlet.ext.jaxrs.JaxRsApplication;

// TODO: This needs to be configured as a Spring bean somewhere
public class JaxRsEngineApplication extends JaxRsApplication {

    private String filterNames = "";

    public JaxRsEngineApplication() {
        super();
    }

    public JaxRsEngineApplication(Context parentContext) {
        super(parentContext.createChildContext());
    }

    public String getFilterNames() {
        return filterNames;
    }

    public void setFilterNames(String filterNames) {
        if (filterNames == null) {
            filterNames = "";
        }
        this.filterNames = filterNames;
    }
}