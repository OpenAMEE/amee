package org.restlet.ext.seam;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.orm.jpa.EntityManagerFactoryAccessor;
import org.springframework.orm.jpa.EntityManagerFactoryUtils;
import org.springframework.orm.jpa.EntityManagerHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.interceptor.DefaultTransactionAttribute;
import org.springframework.transaction.interceptor.TransactionAttribute;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.PersistenceException;
import java.util.Map;
import java.util.Properties;

/**
 * The count allows for begin to be called more than once but only one EntityManager and transaction
 * will ever be active in the same thread. GET requests may not require a transaction at first but
 * could 'declaratively' start a transaction later ('begin(true)').
 */
@Service
public class SpringController extends EntityManagerFactoryAccessor {

    @Autowired
    private PlatformTransactionManager transactionManager;

    private ThreadLocal<Integer> count = new ThreadLocal<Integer>();
    private ThreadLocal<TransactionStatus> transactionStatus = new ThreadLocal<TransactionStatus>();
    private boolean manageTransactions;
    private TransactionAttribute transactionAttribute = new DefaultTransactionAttribute();

    public SpringController() {
        super();
        manageTransactions = true;
    }

    @Autowired
    public void setEntityManagerFactory(EntityManagerFactory entityManagerFactory) {
        super.setEntityManagerFactory(entityManagerFactory);
    }

    @Autowired(required = false)
    public void setJpaProperties(Properties jpaProperties) {
        super.setJpaProperties(jpaProperties);
    }

    @Autowired(required = false)
    public void setJpaPropertyMap(Map jpaProperties) {
        super.setJpaPropertyMap(jpaProperties);
    }

    public void startup() {
        logger.info(">>> STARTUP");
    }

    public void shutdown() {
        logger.info("<<< SHUTDOWN");
    }

    public void begin(boolean withTransaction) {
        logger.info(">>> BEGIN");
        if (TransactionSynchronizationManager.hasResource(getEntityManagerFactory())) {
            // do not modify the EntityManager: just mark the request accordingly
            Integer count = getCount();
            setCount(count + 1);
        } else {
            logger.debug("Opening JPA EntityManager in SpringController");
            try {
                EntityManager em = createEntityManager();
                TransactionSynchronizationManager.bindResource(getEntityManagerFactory(), new EntityManagerHolder(em));
            } catch (PersistenceException ex) {
                throw new DataAccessResourceFailureException("Could not create JPA EntityManager", ex);
            }
        }
        if (withTransaction && manageTransactions) {
            beginTransaction();
        }
    }

    public void end() {
        commitOrRollbackTransaction();
        Integer count = getCount();
        if (count > 0) {
            // Do not modify the EntityManager: just clear the marker.
            setCount(count - 1);
        } else {
            EntityManagerHolder emHolder =
                    (EntityManagerHolder) TransactionSynchronizationManager.unbindResourceIfPossible(getEntityManagerFactory());
            logger.debug("Closing JPA EntityManager in SpringController");
            EntityManagerFactoryUtils.closeEntityManager(emHolder.getEntityManager());
        }
        logger.info("<<< END");
    }

    protected Integer getCount() {
        Integer count = this.count.get();
        if (count == null) {
            count = 0;
        }
        logger.debug("count: " + count);
        return count;
    }

    protected void setCount(Integer count) {
        this.count.set(count);
        logger.debug("count: " + count);
    }

    public void beginTransaction() {
        if (manageTransactions && (transactionStatus.get() == null)) {
            logger.info(">>> OPEN TRANSACTION");
            transactionStatus.set(transactionManager.getTransaction(transactionAttribute));
        }
    }

    public void commitOrRollbackTransaction() {
        if (manageTransactions && (transactionStatus.get() != null)) {
            if (transactionStatus.get().isRollbackOnly()) {
                logger.info("<<< ROLLBACK TRANSACTION");
            } else {
                logger.info("<<< COMMIT TRANSACTION");
            }
            transactionManager.commit(transactionStatus.get());
            transactionStatus.set(null);
        }
    }

    /**
     * Called before HttpConverter.toRequest
     *
     * @param withTransaction specify whether a transaction should be used
     */
    public void beforeToRequest(boolean withTransaction) {
        logger.info(">>> BEFORE TO REQUEST");
        begin(withTransaction);
    }

    /**
     * Called before Filter.doHandle
     */
    public void beforeHandle() {
        logger.info(">>> BEFORE HANDLE");
    }

    /**
     * Called after Filter.doHandle
     */
    public void afterHandle(boolean success) {
        logger.info("<<< AFTER HANDLE");
        if (!success && (transactionStatus.get() != null)) {
            transactionStatus.get().setRollbackOnly();
        }
        commitOrRollbackTransaction();
    }

    /**
     * Called after ConnectorService.beforeSend
     */
    public void beforeSend() {
        logger.info(">>> BEFORE SEND");
    }

    /**
     * Called after ConnectorService.afterSend
     */
    public void afterSend() {
        logger.info("<<< AFTER SEND");
    }

    /**
     * Called after HttpConverter.commit
     */
    public void afterCommit() {
        logger.info("<<< AFTER COMMIT");
        end();
    }

    public boolean isManageTransactions() {
        return manageTransactions;
    }
}