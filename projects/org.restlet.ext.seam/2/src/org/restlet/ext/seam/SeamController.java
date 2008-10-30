package org.restlet.ext.seam;

import org.apache.log4j.Logger;
import org.jboss.seam.contexts.Lifecycle;
import org.jboss.seam.contexts.ServletLifecycle;
import org.jboss.seam.init.Initialization;
import org.jboss.seam.mock.MockServletContext;
import org.jboss.seam.transaction.Transaction;

import javax.servlet.ServletContext;
import java.io.Serializable;

public class SeamController implements Serializable {

    private final static Logger log = Logger.getLogger(SeamController.class);

    private static SeamController instance = new SeamController();

    private boolean manageTransactions;
    private ServletContext servletContext;

    private SeamController() {
        super();
        manageTransactions = true;
        servletContext = new MockServletContext();
    }

    public static SeamController getInstance() {
        return instance;
    }

    public void startSeam() {
        ServletLifecycle.beginApplication(servletContext);
        new Initialization(servletContext).create().init();
    }

    public void stopSeam() {
        ServletLifecycle.endApplication();
    }

    public void beginSeamCall(boolean withTransaction) {
        Lifecycle.beginCall();
        log.info(">>> START JBOSS SEAM CALL BEFORE HANDLE");
        if (withTransaction && manageTransactions) {
            beginTransaction();
        }
    }

    public void endSeamCall() {
        // TODO: find out how to remove dependency on Seam conversation
        // TODO: as entityManager is closed before stateful EJBs
        commitOrRollbackTransaction();
        Lifecycle.endCall();
        log.info("<<< END JBOSS SEAM CALL AFTER SEND");
    }

    // based on org.jboss.seam.jsf.TransactionalSeamPhaseListener.begin
    public void beginTransaction() {
        if (manageTransactions) {
            try {
                if (!Transaction.instance().isActiveOrMarkedRollback()) {
                    log.info(">>> BEGIN TRANSACTION");
                    Transaction.instance().begin();
                }
            } catch (Exception e) {
                // TODO: what should we *really* do here??
                log.error("Caught Exception: " + e.getMessage());
                throw new IllegalStateException("Could not start transaction", e);
            }
        }
    }

    // based on org.jboss.seam.jsf.TransactionalSeamPhaseListener.commitOrRollback
    public void commitOrRollbackTransaction() {
        if (manageTransactions) {
            try {
                if (Transaction.instance().isActive()) {
                    log.info("<<< COMMIT TRANSACTION");
                    Transaction.instance().commit();
                } else if (Transaction.instance().isMarkedRollback()) {
                    log.info("<<< ROLLBACK TRANSACTION");
                    Transaction.instance().rollback();
                }
            } catch (Exception e) {
                // TODO: what should we *really* do here??
                log.error("Caught Exception: " + e.getMessage());
                throw new IllegalStateException("Could not commit transaction", e);
            }
        }
    }

    /**
     * Called before HttpConverter.toRequest
     *
     * @param withTransaction specify whether a transaction should be used
     */
    public void beforeToRequest(boolean withTransaction) {
        log.info(">>> BEFORE TO REQUEST");
        beginSeamCall(withTransaction);
    }

    /**
     * Called before Filter.doHandle
     */
    public void beforeHandle() {
        log.info(">>> BEFORE HANDLE");
    }

    /**
     * Called after Filter.doHandle
     */
    public void afterHandle() {
        log.info("<<< AFTER HANDLE");
        commitOrRollbackTransaction();
    }

    /**
     * Called after ConnectorService.beforeSend
     */
    public void beforeSend() {
        log.info(">>> BEFORE SEND");
    }

    /**
     * Called after ConnectorService.afterSend
     */
    public void afterSend() {
        log.info("<<< AFTER SEND");
    }

    /**
     * Called after HttpConverter.commit
     */
    public void afterCommit() {
        log.info("<<< AFTER COMMIT");
        endSeamCall();
    }

    public boolean isManageTransactions() {
        return manageTransactions;
    }

    public ServletContext getServletContext() {
        return servletContext;
    }
}