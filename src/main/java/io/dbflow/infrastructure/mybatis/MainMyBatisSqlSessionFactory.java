package io.dbflow.infrastructure.mybatis;

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

public final class MainMyBatisSqlSessionFactory {

    private static final SqlSessionFactory SQL_SESSION_FACTORY = build();

    private MainMyBatisSqlSessionFactory() {}

    public static SqlSessionFactory getSqlSessionFactory() {
        return SQL_SESSION_FACTORY;
    }

    private static SqlSessionFactory build() {
        try {
            //properties 파일 읽기
            Properties properties = loadProperties();

            //properties 파일 안에 있는 DB접속정보 가지고 오기
            DataSource dataSource = createDataSource(properties);

            //DB접속하기 위한 설정 정보 만들기(이름, jdbc선택, 접속정보)
            Environment environment = new Environment("local", new JdbcTransactionFactory(), dataSource);

            //DB접속 정보 Configuration 객체에 저장
            Configuration configuration = new Configuration(environment);

            //MyBatis 설정 정보 입력
            configureMyBatis(configuration);

            //dao 인터페이스, xml 매퍼 등록 (Configuration 객체에 저장)
            //등록 조건은 dao인터페이스 파일명과 xml 파일명이 같아야 한다.
            registerMappers(configuration);

            //호출 시 DB와 MyBatis 설정정보 전달
            return new SqlSessionFactoryBuilder().build(configuration);
        } catch (Exception e) {
            throw new IllegalStateException("MyBatis SqlSessionFactory 생성에 실패했습니다.", e);
        }
    }

    private static Properties loadProperties() throws Exception {
        Properties properties = new Properties();

        try (InputStream inputStream = Resources.getResourceAsStream("dbflow.properties")) {
            properties.load(inputStream);
        }

        return properties;
    }

    private static DataSource createDataSource(Properties properties) {
        PooledDataSource dataSource = new PooledDataSource();

        dataSource.setDriver(properties.getProperty("db.driver"));
        dataSource.setUrl(properties.getProperty("db.url"));
        dataSource.setUsername(properties.getProperty("db.username", ""));
        dataSource.setPassword(properties.getProperty("db.password", ""));

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
    private static void registerMappers(Configuration configuration) throws Exception {
        //dao 인터페이스 파일들 해당 경로에 있는거 수집하여 등록
        configuration.addMappers("io.dbflow.infrastructure.repository.mapper");

        //위에서 수집한 dao 파일 목록들을 추출
        Collection<Class<?>> mapperClasses = configuration.getMapperRegistry().getMappers();

        //추출한 파일 목록들을 .xml로 만들어 xml매퍼 등록
        for (Class<?> mapperClass : mapperClasses) {
            String resource = "mapper/main/" + mapperClass.getSimpleName() + ".xml";
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