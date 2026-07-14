package io.dbflow.infrastructure.mybatis;

import org.apache.ibatis.logging.nologging.NoLoggingImpl;
import org.apache.ibatis.logging.stdout.StdOutImpl;
import org.apache.ibatis.session.Configuration;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class MyBatisLogConfigurationTest {

    @AfterEach
    void clearProperty() {
        System.clearProperty(MyBatisLogConfiguration.SQL_LOG_PROPERTY);
    }

    @Test
    void 기본적으로_SQL_로그를_출력하지_않는다() {
        Configuration configuration = new Configuration();

        MyBatisLogConfiguration.configure(configuration);

        assertEquals(NoLoggingImpl.class, configuration.getLogImpl());
    }

    @Test
    void 개발용_시스템_속성이_활성화되면_SQL_로그를_출력한다() {
        System.setProperty(MyBatisLogConfiguration.SQL_LOG_PROPERTY, "true");
        Configuration configuration = new Configuration();

        MyBatisLogConfiguration.configure(configuration);

        assertEquals(StdOutImpl.class, configuration.getLogImpl());
    }
}
