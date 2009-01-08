package gc.engine;

import com.jellymold.engine.EngineStatusService;
import org.restlet.*;
import org.restlet.ext.seam.SpringController;
import org.restlet.ext.jaxrs.JaxRsApplication;
import org.springframework.context.ApplicationContext;

public class JaxRsEngineApplication extends JaxRsApplication {

    private ApplicationContext springContext;
    private SpringController springController;
    private String filterNames = "";

    public JaxRsEngineApplication() {
        super();
    }

    public JaxRsEngineApplication(Context parentContext) {
        super(parentContext.createChildContext());
        this.springContext = (ApplicationContext) parentContext.getAttributes().get("springContext");
        this.springController = (SpringController) parentContext.getAttributes().get("springController");
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

    public ApplicationContext getSpringContext() {
        return springContext;
    }

    public SpringController getSpringController() {
        return springController;
    }
}