package org.restlet.ext.seam;

import org.restlet.resource.Representation;
import org.restlet.service.ConnectorService;

public class SeamConnectorService extends ConnectorService {

    public SeamConnectorService() {
        super();
    }

    public void beforeSend(Representation entity) {
        SeamController.getInstance().beforeSend();
    }

    public void afterSend(Representation entity) {
        SeamController.getInstance().afterSend();
    }
}
