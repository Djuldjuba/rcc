package io.student.rococo.data.mapper.tpl;

import com.atomikos.icatch.jta.UserTransactionManager;
import com.atomikos.jdbc.AtomikosDataSourceBean;
import org.apache.commons.lang3.StringUtils;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NameAlreadyBoundException;
import javax.naming.NamingException;
import javax.sql.DataSource;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;

public class DataSources {

    private static final ConcurrentHashMap<String, DataSource> datasources = new ConcurrentHashMap<>();

    static {
        initJndi();
    }

    private static synchronized void initJndi() {
        try {
            System.setProperty(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.rmi.registry.RegistryContextFactory");
            System.setProperty(Context.PROVIDER_URL, "rmi://localhost:1099");

            try {
                java.rmi.registry.LocateRegistry.createRegistry(1099);
            } catch (java.rmi.RemoteException e) {

            }

            UserTransactionManager userTransactionManager = new UserTransactionManager();
            userTransactionManager.setTransactionTimeout(300);
            userTransactionManager.init();

            Context context = new InitialContext();

            try {
                context.bind("UserTransaction", userTransactionManager);
            } catch (NameAlreadyBoundException e) {

            }

            try {
                context.bind("java:comp/env/UserTransaction", userTransactionManager);
            } catch (NameAlreadyBoundException e) {

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
            dsBean.setUniqueResourceName(uniqId);
            dsBean.setXaDataSourceClassName("com.mysql.cj.jdbc.MysqlXADataSource");

            Properties props = new Properties();
            props.put("URL", jdbcUrl);
            props.put("user", "root");
            props.put("password", "secret");
            dsBean.setXaProperties(props);
            dsBean.setPoolSize(3);
            dsBean.setMaxPoolSize(10);
            dsBean.setBorrowConnectionTimeout(30);

            try {
                Context context = new InitialContext();
                String jndiName = "java:comp/env/jdbc/" + uniqId;
                try {
                    context.bind(jndiName, dsBean);
                } catch (NameAlreadyBoundException e) {

                }
            } catch (NamingException e) {
                throw new RuntimeException("Failed to bind JNDI for " + uniqId, e);
            }

            return dsBean;
        });
    }
}