package gc.engine;

import org.restlet.*;
import org.restlet.ext.seam.TransactionController;
import org.restlet.ext.jaxrs.JaxRsApplication;
import org.springframework.context.ApplicationContext;

public class JaxRsEngineApplication extends JaxRsApplication {

    private ApplicationContext springContext;
    private TransactionController transactionController;
    private String filterNames = "";

    public JaxRsEngineApplication() {
        super();
    }

    public JaxRsEngineApplication(Context parentContext) {
        super(parentContext.createChildContext());
        this.springContext = (ApplicationContext) parentContext.getAttributes().get("springContext");
        this.transactionController = (TransactionController) parentContext.getAttributes().get("transactionController");
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

    public TransactionController getTransactionController() {
        return transactionController;
    }
}