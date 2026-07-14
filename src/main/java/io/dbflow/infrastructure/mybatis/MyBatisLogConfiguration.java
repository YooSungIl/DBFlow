package io.dbflow.infrastructure.mybatis;

import org.apache.ibatis.logging.nologging.NoLoggingImpl;
import org.apache.ibatis.logging.stdout.StdOutImpl;
import org.apache.ibatis.session.Configuration;

public final class MyBatisLogConfiguration {

    public static final String SQL_LOG_PROPERTY = "dbflow.sqlLog";

    private MyBatisLogConfiguration() {
    }

    public static void configure(Configuration configuration) {
        if (Boolean.getBoolean(SQL_LOG_PROPERTY)) {
            configuration.setLogImpl(StdOutImpl.class);
            return;
        }

        configuration.setLogImpl(NoLoggingImpl.class);
    }
}
