package org.restlet.ext.seam;

import org.restlet.resource.Representation;
import org.restlet.service.ConnectorService;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;

@Service
public class TransactionConnectorService extends ConnectorService {

    @Autowired
    private TransactionController transactionController;

    private TransactionConnectorService() {
        super();
    }

    public void beforeSend(Representation entity) {
        transactionController.beforeSend();
    }

    public void afterSend(Representation entity) {
        transactionController.afterSend();
    }
}