package io.dbflow.infrastructure.mybatis;

import io.dbflow.common.enums.DbType;
import io.dbflow.domain.DbConfig;
import io.dbflow.infrastructure.security.CredentialSecurity;
import org.apache.ibatis.builder.xml.XMLMapperBuilder;
import org.apache.ibatis.datasource.pooled.PooledDataSource;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.logging.stdout.StdOutImpl;
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

public final class ExternalMyBatisSqlSessionFactory {

    private static final String ENVIRONMENT_ID = "external";
    private static final String MAPPER_BASE_PACKAGE = "io.dbflow.infrastructure.external.repository.";
    private static final String MAPPER_XML_BASE_DIR = "mapper/external/";

    private ExternalMyBatisSqlSessionFactory() {
    }

    public static SqlSessionFactory getSqlSessionFactory(DbConfig dbConfig) {
        return build(dbConfig);
    }

    private static SqlSessionFactory build(DbConfig dbConfig) {
        try {
            DbType dbType = DbType.valueOf(dbConfig.getDbType());
            DataSource dataSource = createDataSource(dbConfig, dbType);
            Configuration configuration = createConfiguration(dataSource);
            registerMappers(configuration, dbType);

            return new SqlSessionFactoryBuilder().build(configuration);
        } catch (Exception e) {
            throw new IllegalStateException("MyBatis SqlSessionFactory 생성에 실패했습니다.", e);
        }
    }

    private static DataSource createDataSource(DbConfig dbConfig, DbType dbType) throws Exception {
        PooledDataSource dataSource = new PooledDataSource();

        dataSource.setDriver(dbType.getDriver());
        dataSource.setUrl(dbType.createUrl(dbConfig.getDbHost(), dbConfig.getDbPort(), dbConfig.getDbName()));
        dataSource.setUsername(dbConfig.getDbUser());
        String password = CredentialSecurity.stringEncryptor().decrypt(dbConfig.getDbPassword());
        dataSource.setPassword(password);

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
        configuration.setLogImpl(StdOutImpl.class);
    }

    private static void registerMappers(Configuration configuration, DbType dbType) throws Exception {
        configuration.addMappers(MAPPER_BASE_PACKAGE + dbType.getDir() + ".mapper");
        Collection<Class<?>> mapperClasses = configuration.getMapperRegistry().getMappers();

        for (Class<?> mapperClass : mapperClasses) {
            String resource = MAPPER_XML_BASE_DIR + dbType.getDir() + "/" + mapperClass.getSimpleName() + ".xml";
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
}
