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

@Service
public class SpringController extends EntityManagerFactoryAccessor {

    @Autowired
    private PlatformTransactionManager transactionManager;

    private boolean manageTransactions;
    private TransactionAttribute transactionAttribute = new DefaultTransactionAttribute();
    private ThreadLocal<TransactionStatus> transactionStatus = new ThreadLocal<TransactionStatus>();
    private ThreadLocal<Boolean> transactionRollback = new ThreadLocal<Boolean>();

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
        logger.debug("startup() - >>> STARTUP");
    }

    public void shutdown() {
        logger.debug("shutdown() - <<< SHUTDOWN");
    }

    public void begin(boolean withTransaction) {
        logger.debug("begin() - >>> BEGIN");
        if (!TransactionSynchronizationManager.hasResource(getEntityManagerFactory())) {
            logger.debug("begin() - Opening JPA EntityManager in SpringController");
            try {
                EntityManager em = createEntityManager();
                TransactionSynchronizationManager.bindResource(getEntityManagerFactory(), new EntityManagerHolder(em));
            } catch (PersistenceException ex) {
                throw new DataAccessResourceFailureException("Could not create JPA EntityManager", ex);
            }
        } else {
            logger.debug("begin() - JPA EntityManager already open");
        }
        if (withTransaction && manageTransactions) {
            beginTransaction();
        }
    }

    public void end() {
        commitOrRollbackTransaction();
        if (TransactionSynchronizationManager.hasResource(getEntityManagerFactory())) {
            logger.debug("end() - Closing JPA EntityManager in SpringController");
            EntityManagerHolder emHolder =
                    (EntityManagerHolder) TransactionSynchronizationManager.unbindResourceIfPossible(getEntityManagerFactory());
            if (emHolder != null) {
                EntityManagerFactoryUtils.closeEntityManager(emHolder.getEntityManager());
            }
        }
        logger.debug("end() - <<< END");
    }

    public void beginTransaction() {
        if (manageTransactions && (transactionStatus.get() == null)) {
            transactionStatus.set(transactionManager.getTransaction(transactionAttribute));
            if (!transactionStatus.get().isNewTransaction()) {
                logger.error("beginTransaction() - Transaction already open.");
                throw new RuntimeException("Transaction already open.");
            }
            logger.debug("beginTransaction() - >>> TRANSACTION OPENED");
        }
    }

    public void commitOrRollbackTransaction() {
        if (manageTransactions && (transactionStatus.get() != null)) {
            if (!transactionStatus.get().isCompleted()) {
                if (transactionRollback.get() != null) {
                    transactionManager.rollback(transactionStatus.get());
                    logger.warn("commitOrRollbackTransaction() - <<< TRANSACTION ROLLED BACK");
                } else {
                    transactionManager.commit(transactionStatus.get());
                    logger.debug("commitOrRollbackTransaction() - <<< TRANSACTION COMMITTED");
                }
            } else {
                logger.warn("commitOrRollbackTransaction() - <<< TRANSACTION ALREADY COMPLETED");
            }
            transactionStatus.set(null);
        }
        transactionRollback.set(null);
    }

    /**
     * Called before HttpConverter.toRequest
     *
     * @param withTransaction specify whether a transaction should be used
     */
    public void beforeToRequest(boolean withTransaction) {
        logger.debug("beforeToRequest() - >>> BEFORE TO REQUEST");
        begin(withTransaction);
    }

    /**
     * Called before Filter.doHandle
     */
    public void beforeHandle() {
        logger.debug("beforeHandle() - >>> BEFORE HANDLE");
    }

    /**
     * Called after Filter.doHandle
     */
    public void afterHandle(boolean success) {
        logger.debug("afterHandle() - <<< AFTER HANDLE");
        if (!success) {
            transactionRollback.set(true);
        }
        commitOrRollbackTransaction();
    }

    /**
     * Called after ConnectorService.beforeSend
     */
    public void beforeSend() {
        logger.debug("beforeSend() - >>> BEFORE SEND");
    }

    /**
     * Called after ConnectorService.afterSend
     */
    public void afterSend() {
        logger.debug("afterSend() - <<< AFTER SEND");
    }

    /**
     * Called after HttpConverter.commit
     */
    public void afterCommit() {
        logger.debug("afterCommit() - <<< AFTER COMMIT");
        end();
    }

    public boolean isManageTransactions() {
        return manageTransactions;
    }
}