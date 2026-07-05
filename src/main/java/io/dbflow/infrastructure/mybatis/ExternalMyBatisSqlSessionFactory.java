package io.dbflow.infrastructure.mybatis;

import io.dbflow.common.enums.DbType;
import io.dbflow.domain.DbConfig;
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
import java.util.Properties;

public final class ExternalMyBatisSqlSessionFactory {

    private ExternalMyBatisSqlSessionFactory() {}

    public static SqlSessionFactory getSqlSessionFactory(DbConfig dbConfig) {
        return build(dbConfig);
    }

    private static SqlSessionFactory build(DbConfig dbConfig) {
        try {
            DbType dbType = DbType.valueOf(dbConfig.getDbType());

            //properties 파일 안에 있는 DB접속정보 가지고 오기
            DataSource dataSource = createDataSource(dbConfig, dbType);

            //DB접속하기 위한 설정 정보 만들기(이름, jdbc선택, 접속정보)
            Environment environment = new Environment("external", new JdbcTransactionFactory(), dataSource);

            //DB접속 정보 Configuration 객체에 저장
            Configuration configuration = new Configuration(environment);

            //MyBatis 설정 정보 입력
            configureMyBatis(configuration);

            //dao 인터페이스, xml 매퍼 등록 (Configuration 객체에 저장)
            //등록 조건은 dao인터페이스 파일명과 xml 파일명이 같아야 한다.
            registerMappers(configuration, dbType);

            //호출 시 DB와 MyBatis 설정정보 전달
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
        dataSource.setPassword(dbConfig.getDbPassword());

        return dataSource;
    }

    private static void configureMyBatis(Configuration configuration) {
        //DB컬럼명을 Java 필드명으로 자동 변환
        configuration.setMapUnderscoreToCamelCase(true);
        //MyBatis 2차 캐시 사용 여부 (Mapper namespace 단위 캐시)
        configuration.setCacheEnabled(false);
        //SQL실행 방식 (SIMPLE, REUSE, BATCH)
        configuration.setDefaultExecutorType(ExecutorType.SIMPLE);
        //Java에서 받은 데이터가 Null일 경우 어떤 null을 DB로 전달할 것인지 선택 (Oracle 시 민감)
        configuration.setJdbcTypeForNull(JdbcType.NULL);
        //DB조회 결과가 Null일 경우 setter 호출할지 여부
        configuration.setCallSettersOnNulls(true);
        //Mybatis 로그 출력 방식 (StdOutImpl, Slf4jImpl, Log4jImpl, Log4j2Impl, NoLoggingImpl)
        configuration.setLogImpl(StdOutImpl.class);
    }

    /**
     * @param configuration
     * @throws Exception
     * Dao 인터페이스 파일명과 xml 매퍼 파일명이 같아야 자동등록이 가능하다.
     */
    private static void registerMappers(Configuration configuration, DbType dbType) throws Exception {
        //dao 인터페이스 파일들 해당 경로에 있는거 수집하여 등록
        configuration.addMappers("io.dbflow.infrastructure.external.repository." + dbType.getDir() + ".mapper");

        //위에서 수집한 dao 파일 목록들을 추출
        Collection<Class<?>> mapperClasses = configuration.getMapperRegistry().getMappers();

        //추출한 파일 목록들을 .xml로 만들어 xml매퍼 등록
        for (Class<?> mapperClass : mapperClasses) {
            String resource = "mapper/external/" + dbType.getDir() + "/" + mapperClass.getSimpleName() + ".xml";
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