package io.dbflow.infrastructure.external.repository.postgres;

import io.dbflow.common.Exception.RepositoryException;
import io.dbflow.domain.CollectTableSnapshot;
import io.dbflow.domain.ColumnMetadata;
import io.dbflow.domain.DbConfig;
import io.dbflow.domain.TableMetadata;
import io.dbflow.infrastructure.external.repository.MetadataCollector;
import io.dbflow.infrastructure.external.repository.postgres.mapper.PostgresMetadataCollectorMapper;
import io.dbflow.infrastructure.mybatis.ExternalMyBatisSqlSessionFactory;
import org.apache.ibatis.session.SqlSession;

import java.util.List;

public class PostgresMetadataCollector implements MetadataCollector {

    @Override
    public List<TableMetadata> collectTableSnapshotList(DbConfig dbConfig) {
        try (SqlSession session = ExternalMyBatisSqlSessionFactory.getSqlSessionFactory(dbConfig).openSession()) {
            PostgresMetadataCollectorMapper mapper = session.getMapper(PostgresMetadataCollectorMapper.class);
            return mapper.collectTableSnapshotList(dbConfig);
        } catch (Exception e) {
            throw new RepositoryException(e.getMessage());
        }
    }

    @Override
    public List<ColumnMetadata> collectColumnSnapshotList(DbConfig dbConfig, List<CollectTableSnapshot> tableSnapshot) {
        try (SqlSession session = ExternalMyBatisSqlSessionFactory.getSqlSessionFactory(dbConfig).openSession()) {
            PostgresMetadataCollectorMapper mapper = session.getMapper(PostgresMetadataCollectorMapper.class);
            return mapper.collectColumnSnapshotList(dbConfig, tableSnapshot);
        } catch (Exception e) {
            throw new RepositoryException(e.getMessage());
        }
    }
}
