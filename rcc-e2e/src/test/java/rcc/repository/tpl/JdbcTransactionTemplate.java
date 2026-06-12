package rcc.repository.tpl;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Supplier;

import static java.sql.Connection.TRANSACTION_READ_COMMITTED;

public class JdbcTransactionTemplate {

    private final JdbcConnectionHolder holder;
    private final AtomicBoolean closeAfterAction = new AtomicBoolean(true);

    public JdbcTransactionTemplate(String jdbcUrl) {
        this.holder = Connections.holder(jdbcUrl);
    }

    public JdbcTransactionTemplate holdConnectionAfterAction() {
        this.closeAfterAction.set(false);
        return this;
    }

    public <T> T execute(Supplier<T> action, int isolationLvl) {
        Connection connection = null;
        boolean previousAutoCommit = true;

        try {
            connection = holder.connection();
            previousAutoCommit = connection.getAutoCommit();

            connection.setTransactionIsolation(isolationLvl);
            connection.setAutoCommit(false);

            T result = action.get();

            connection.commit();
            return result;

        } catch (Exception e) {
            if (connection != null) {
                try {
                    connection.rollback();
                } catch (SQLException ex) {
                    e.addSuppressed(ex);
                }
            }
            throw new RuntimeException(e);

        } finally {
            if (connection != null) {
                try {
                    connection.setAutoCommit(previousAutoCommit);
                } catch (SQLException e) {

                }
            }

            if (closeAfterAction.get()) {
                holder.close();
            }
        }
    }

    public <T> T execute(Supplier<T> action) {
        return execute(action, TRANSACTION_READ_COMMITTED);
    }
}