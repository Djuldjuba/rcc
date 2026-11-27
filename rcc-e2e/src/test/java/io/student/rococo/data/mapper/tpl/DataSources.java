package io.student.rococo.data.mapper.tpl;

import com.atomikos.icatch.jta.UserTransactionManager;
import com.atomikos.jdbc.AtomikosDataSourceBean;
import com.p6spy.engine.spy.P6DataSource;
import io.student.rococo.config.Config;
import org.apache.commons.lang3.StringUtils;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NameAlreadyBoundException;
import javax.naming.NamingException;
import javax.sql.DataSource;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;

public final class DataSources {

    private DataSources() {
    }

    private static final Config CFG = Config.getInstance();
    private static final ConcurrentHashMap<String, DataSource> datasources = new ConcurrentHashMap<>();

    static {
        initJndi();
    }

    private static synchronized void initJndi() {
        try {
            System.setProperty(Context.INITIAL_CONTEXT_FACTORY, "org.osjava.sj.MemoryContextFactory");
            System.setProperty("org.osjava.sj.jndi.shared", "true");

            UserTransactionManager userTransactionManager = new UserTransactionManager();
            userTransactionManager.setTransactionTimeout(300);
            userTransactionManager.init();

            Context context = new InitialContext();
            try {
                context.bind("java:comp/UserTransaction", userTransactionManager);
            } catch (NameAlreadyBoundException ignored) {
            }
            try {
                context.bind("java:comp/env/UserTransaction", userTransactionManager);
            } catch (NameAlreadyBoundException ignored) {
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to initialize JNDI", e);
        }
    }

    public static DataSource dataSource(String jdbcUrl) {
        return datasources.computeIfAbsent(jdbcUrl, key -> {
            AtomikosDataSourceBean dsBean = new AtomikosDataSourceBean();

            String uniqId = StringUtils.substringAfter(jdbcUrl, "3306/");
            uniqId = StringUtils.substringBefore(uniqId, "?");
            if (uniqId == null || uniqId.isBlank()) {
                uniqId = "ds-" + Math.abs(jdbcUrl.hashCode());
            }

            dsBean.setUniqueResourceName(uniqId);
            dsBean.setXaDataSourceClassName("com.mysql.cj.jdbc.MysqlXADataSource");

            Properties props = new Properties();
            props.put("URL", jdbcUrl);
            props.put("user", CFG.dbUserName());
            props.put("password", CFG.dbPassword());
            dsBean.setXaProperties(props);
            dsBean.setPoolSize(3);
            dsBean.setMaxPoolSize(10);
            dsBean.setBorrowConnectionTimeout(30);

            P6DataSource p6DataSource = new P6DataSource(dsBean);

            try {
                Context context = new InitialContext();
                String jndiName = "java:comp/env/jdbc/" + uniqId;
                try {
                    context.bind(jndiName, p6DataSource);
                } catch (NameAlreadyBoundException ignored) {
                }
            } catch (NamingException e) {
                throw new RuntimeException("Failed to bind JNDI for " + uniqId, e);
            }

            return p6DataSource;
        });
    }
}