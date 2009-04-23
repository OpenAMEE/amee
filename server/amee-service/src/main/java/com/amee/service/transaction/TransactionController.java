package com.amee.service.transaction;

import com.amee.domain.AMEEStatistics;
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
public class TransactionController extends EntityManagerFactoryAccessor {

    @Autowired
    private PlatformTransactionManager transactionManager;

    @Autowired
    private AMEEStatistics ameeStatistics;

    private boolean manageTransactions;
    private TransactionAttribute transactionAttribute = new DefaultTransactionAttribute();
    private ThreadLocal<TransactionStatus> transactionStatus = new ThreadLocal<TransactionStatus>();
    private ThreadLocal<Boolean> transactionRollback = new ThreadLocal<Boolean>();

    public TransactionController() {
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

    /**
     * 1) Called by TransactionFilter before Filter.doHandle
     *
     * @param withTransaction specify whether a transaction should be used
     */
    public void beforeHandle(boolean withTransaction) {
        logger.debug("beforeHandle() - >>> BEFORE HANDLE {withTransaction=" + withTransaction + "}");
        // Reset thread bound statistics.
        ameeStatistics.resetThread();
        // Ensure any EntityManager associated with this thread is closed before handling this new request.
        ensureEntityManagerIsClosed();
        // Begin transaction if required.
        begin(withTransaction);
    }

    /**
     * 2) Called by TransactionFilter after Filter.doHandle
     *
     * @param success true or false
     */
    public void afterHandle(boolean success) {
        logger.debug("afterHandle() - <<< AFTER HANDLE");
        if (!success) {
            transactionRollback.set(true);
        }
        commitOrRollbackTransaction();
    }

    /**
     * 3) Called by TransactionServerConverter after HttpServerConverter.commit
     */
    public void afterCommit() {
        logger.debug("afterCommit() - <<< AFTER COMMIT");
        end();
    }

    public void begin(boolean withTransaction) {
        logger.debug("begin() - >>> BEGIN");
        openEntityManager();
        if (withTransaction && manageTransactions) {
            beginTransaction();
        }
    }

    public void end() {
        commitOrRollbackTransaction();
        ensureEntityManagerIsClosed();
        logger.debug("end() - <<< END");
    }

    private void beginTransaction() {
        if (manageTransactions && (transactionStatus.get() == null)) {
            transactionStatus.set(transactionManager.getTransaction(transactionAttribute));
            if (!transactionStatus.get().isNewTransaction()) {
                logger.error("beginTransaction() - Transaction already open.");
                throw new RuntimeException("Transaction already open.");
            }
            logger.debug("beginTransaction() - >>> TRANSACTION OPENED");
        }
    }

    private void commitOrRollbackTransaction() {
        if (manageTransactions && (transactionStatus.get() != null)) {
            if (!transactionStatus.get().isCompleted()) {
                if (transactionRollback.get() != null) {
                    // Roll back transaction.
                    transactionManager.rollback(transactionStatus.get());
                    // Update statistics.
                    ameeStatistics.resetThread();
                    ameeStatistics.transactionRollback();
                    logger.warn("commitOrRollbackTransaction() - <<< TRANSACTION ROLLED BACK");
                } else {
                    // Commit the transaction.
                    transactionManager.commit(transactionStatus.get());
                    // Update statistics.
                    ameeStatistics.commitThread();
                    ameeStatistics.transactionCommit();
                    logger.debug("commitOrRollbackTransaction() - <<< TRANSACTION COMMITTED");
                }
            } else {
                logger.warn("commitOrRollbackTransaction() - <<< TRANSACTION ALREADY COMPLETED");
            }
            transactionStatus.set(null);
        }
        transactionRollback.set(null);
    }

    public void openEntityManager() {
        // Return if there is an EntityManager already associated with this request.
        if (TransactionSynchronizationManager.hasResource(getEntityManagerFactory()))
            return;

        logger.debug("openEntityManager() - Opening JPA EntityManager in TransactionController");
        try {
            EntityManager em = createEntityManager();
            TransactionSynchronizationManager.bindResource(getEntityManagerFactory(), new EntityManagerHolder(em));
        } catch (PersistenceException ex) {
            throw new DataAccessResourceFailureException("Could not create JPA EntityManager", ex);
        }
    }

    public void ensureEntityManagerIsClosed() {
        // Return if there is no EntityManager already associated with this request.
        if (!TransactionSynchronizationManager.hasResource(getEntityManagerFactory()))
            return;

        logger.debug("ensureEntityManagerIsClosed() - Closing JPA EntityManager in TransactionController");
        EntityManagerHolder emHolder =
                (EntityManagerHolder) TransactionSynchronizationManager.unbindResourceIfPossible(getEntityManagerFactory());
        if (emHolder != null) {
            EntityManagerFactoryUtils.closeEntityManager(emHolder.getEntityManager());
        }
    }

    public boolean isManageTransactions() {
        return manageTransactions;
    }
}