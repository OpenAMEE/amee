package org.restlet.ext.seam;

import org.restlet.resource.Representation;
import org.restlet.service.ConnectorService;

public class SpringConnectorService extends ConnectorService {

    private TransactionController transactionController;

    private SpringConnectorService() {
        super();
    }

    public SpringConnectorService(TransactionController transactionController) {
        this();
        this.transactionController = transactionController;
    }

    public void beforeSend(Representation entity) {
        transactionController.beforeSend();
    }

    public void afterSend(Representation entity) {
        transactionController.afterSend();
    }
}