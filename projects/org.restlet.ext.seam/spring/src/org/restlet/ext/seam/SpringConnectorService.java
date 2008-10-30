package org.restlet.ext.seam;

import org.restlet.resource.Representation;
import org.restlet.service.ConnectorService;

public class SpringConnectorService extends ConnectorService {

    private SpringController springController;

    private SpringConnectorService() {
        super();
    }

    public SpringConnectorService(SpringController springController) {
        this();
        this.springController = springController;
    }

    public void beforeSend(Representation entity) {
        springController.beforeSend();
    }

    public void afterSend(Representation entity) {
        springController.afterSend();
    }
}