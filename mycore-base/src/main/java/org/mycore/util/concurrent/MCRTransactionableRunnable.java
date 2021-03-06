package org.mycore.util.concurrent;

import java.util.Objects;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.mycore.common.MCRSession;
import org.mycore.common.MCRSessionMgr;

/**
 * Decorates a {@link Runnable} with a mycore session and a database transaction.
 * 
 * @author Matthias Eichner
 */
public class MCRTransactionableRunnable implements Runnable {

    protected final static Logger LOGGER = LogManager.getLogger();

    private Runnable decorator;

    private MCRSession session;

    /**
     * Creates a new {@link Runnable} decorating the {@link #run()} method with a new
     * {@link MCRSession} and a database transaction. Afterwards the transaction will
     * be committed and the session will be released and closed.
     * 
     * <p>If you want to execute your runnable in the context of an already existing
     * session use the {@link MCRTransactionableRunnable#MCRTransactionableRunnable(Runnable, MCRSession)}
     * constructor instead.
     * 
     * @param decorator the runnable to decorate
     */
    public MCRTransactionableRunnable(Runnable decorator) {
        this.decorator = decorator;
    }

    /**
     * Creates a new {@link Runnable} decorating the {@link #run()} method with a new
     * a database transaction. The transaction will be created in the context of the
     * given session. Afterwards the transaction will be committed and the session
     * will be released (but not closed!).
     * 
     * @param decorator the runnable to decorate
     * @param session the session to use
     */
    public MCRTransactionableRunnable(Runnable decorator, MCRSession session) {
        this.decorator = Objects.requireNonNull(decorator, "decorator must not be null");
        this.session = Objects.requireNonNull(session, "session must not be null");
    }

    @Override
    public void run() {
        boolean newSession = this.session == null;
        boolean closeSession = newSession && !MCRSessionMgr.hasCurrentSession();
        if (newSession) {
            this.session = MCRSessionMgr.getCurrentSession();
        }
        MCRSessionMgr.setCurrentSession(this.session);
        session.beginTransaction();
        try {
            this.decorator.run();
        } finally {
            try {
                session.commitTransaction();
            } catch (Exception commitExc) {
                LOGGER.error("Error while commiting transaction.", commitExc);
                try {
                    session.rollbackTransaction();
                } catch (Exception rollbackExc) {
                    LOGGER.error("Error while rollbacking transaction.", commitExc);
                }
            } finally {
                MCRSessionMgr.releaseCurrentSession();
                if (closeSession && session != null) {
                    session.close();
                }
            }
        }
    }

}
