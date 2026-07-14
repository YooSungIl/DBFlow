package io.dbflow.infrastructure.mybatis;

import io.dbflow.infrastructure.path.DbFlowPathResolver;
import org.apache.ibatis.builder.xml.XMLMapperBuilder;
import org.apache.ibatis.datasource.pooled.PooledDataSource;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.mapping.Environment;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.session.ExecutorType;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.apache.ibatis.transaction.jdbc.JdbcTransactionFactory;
import org.apache.ibatis.type.JdbcType;

import javax.sql.DataSource;
import java.io.InputStream;
import java.util.Collection;
import java.util.Properties;

public final class MainMyBatisSqlSessionFactory {

    private static final String ENVIRONMENT_ID = "main";
    private static final String PROPERTIES_RESOURCE = "dbflow.properties";
    private static final String MAPPER_PACKAGE = "io.dbflow.infrastructure.repository.mapper";
    private static final String MAPPER_XML_DIR = "mapper/main/";
    private static final String SQLITE_URL_PREFIX = "jdbc:sqlite:";

    private static final SqlSessionFactory SQL_SESSION_FACTORY = build();

    private MainMyBatisSqlSessionFactory() {
    }

    public static SqlSessionFactory getSqlSessionFactory() {
        return SQL_SESSION_FACTORY;
    }

    private static SqlSessionFactory build() {
        try {
            Properties properties = loadProperties();
            DataSource dataSource = createDataSource(properties);
            Configuration configuration = createConfiguration(dataSource);
            registerMappers(configuration);

            return new SqlSessionFactoryBuilder().build(configuration);
        } catch (Exception e) {
            throw new IllegalStateException("MyBatis SqlSessionFactory 생성에 실패했습니다.", e);
        }
    }

    private static DataSource createDataSource(Properties properties) {
        PooledDataSource dataSource = new PooledDataSource();

        dataSource.setDriver(properties.getProperty("db.driver"));
        dataSource.setUrl(SQLITE_URL_PREFIX + DbFlowPathResolver.resolveDatabasePath());
        dataSource.setUsername(properties.getProperty("db.username", ""));
        dataSource.setPassword(properties.getProperty("db.password", ""));

        return dataSource;
    }

    private static Configuration createConfiguration(DataSource dataSource) {
        Environment environment = new Environment(ENVIRONMENT_ID, new JdbcTransactionFactory(), dataSource);
        Configuration configuration = new Configuration(environment);
        configureMyBatis(configuration);
        return configuration;
    }

    private static void configureMyBatis(Configuration configuration) {
        configuration.setMapUnderscoreToCamelCase(true);
        configuration.setCacheEnabled(false);
        configuration.setDefaultExecutorType(ExecutorType.SIMPLE);
        configuration.setJdbcTypeForNull(JdbcType.NULL);
        configuration.setCallSettersOnNulls(true);
        MyBatisLogConfiguration.configure(configuration);
    }

    private static void registerMappers(Configuration configuration) throws Exception {
        configuration.addMappers(MAPPER_PACKAGE);
        Collection<Class<?>> mapperClasses = configuration.getMapperRegistry().getMappers();

        for (Class<?> mapperClass : mapperClasses) {
            String resource = MAPPER_XML_DIR + mapperClass.getSimpleName() + ".xml";
            parseMapperXml(configuration, resource);
        }
    }

    private static void parseMapperXml(Configuration configuration, String resource) throws Exception {
        try (InputStream inputStream = Resources.getResourceAsStream(resource)) {
            if (inputStream == null) {
                throw new IllegalStateException("Mapper XML 파일을 찾을 수 없습니다: " + resource);
            }
            XMLMapperBuilder xmlMapperBuilder = new XMLMapperBuilder(inputStream, configuration, resource, configuration.getSqlFragments());
            xmlMapperBuilder.parse();
        }
    }

    private static Properties loadProperties() throws Exception {
        Properties properties = new Properties();

        try (InputStream inputStream = Resources.getResourceAsStream(PROPERTIES_RESOURCE)) {
            properties.load(inputStream);
        }

        return properties;
    }
}
