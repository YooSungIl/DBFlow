package io.dbflow.infrastructure.repository;

import io.dbflow.common.exception.RepositoryException;
import io.dbflow.domain.DbConfig;
import io.dbflow.infrastructure.mybatis.MainMyBatisSqlSessionFactory;
import io.dbflow.infrastructure.repository.mapper.DbConfigMapper;
import org.apache.ibatis.session.SqlSession;

import java.util.List;

public class DbConfigRepository {

    public void saveDbConfig(DbConfig dbConfig) {
        try (SqlSession session = MainMyBatisSqlSessionFactory.getSqlSessionFactory().openSession(false)) {
            try {
                DbConfigMapper dbConfigMapper = session.getMapper(DbConfigMapper.class);
                dbConfigMapper.insert(dbConfig);
                session.commit();
            } catch (Exception e) {
                session.rollback();
                throw new RepositoryException(e.getMessage(), e);
            }
        }
    }

    public List<DbConfig> findDbConfigs() {
        try (SqlSession session = MainMyBatisSqlSessionFactory.getSqlSessionFactory().openSession()) {
            DbConfigMapper dbConfigMapper = session.getMapper(DbConfigMapper.class);
            return dbConfigMapper.findDbConfigs();
        } catch (Exception e) {
            throw new RepositoryException(e.getMessage(), e);
        }
    }

    public DbConfig findDbConfig(String dbAlias) {
        try (SqlSession session = MainMyBatisSqlSessionFactory.getSqlSessionFactory().openSession()) {
            DbConfigMapper dbConfigMapper = session.getMapper(DbConfigMapper.class);
            return dbConfigMapper.findDbConfig(dbAlias);
        } catch (Exception e) {
            throw new RepositoryException(e.getMessage(), e);
        }
    }

    public void deleteDbConfig(DbConfig dbConfig) {
        try (SqlSession session = MainMyBatisSqlSessionFactory.getSqlSessionFactory().openSession(false)) {
            try {
                DbConfigMapper dbConfigMapper = session.getMapper(DbConfigMapper.class);
                int result = dbConfigMapper.deleteDbConfig(dbConfig);
                if (result == 0) {
                    throw new RepositoryException("비활성화 처리할 DB접속 정보가 없습니다.");
                }
                session.commit();
            } catch (RepositoryException e) {
                session.rollback();
                throw e;
            } catch (Exception e) {
                session.rollback();
                throw new RepositoryException(e.getMessage(), e);
            }
        }
    }

    public void updateDbConfig(DbConfig dbConfig) {
        try (SqlSession session = MainMyBatisSqlSessionFactory.getSqlSessionFactory().openSession(false)) {
            try {
                DbConfigMapper dbConfigMapper = session.getMapper(DbConfigMapper.class);
                int result = dbConfigMapper.updateDbConfig(dbConfig);
                if (result == 0) {
                    throw new RepositoryException("수정할 DB접속 정보가 없습니다.");
                }
                session.commit();
            } catch (RepositoryException e) {
                session.rollback();
                throw e;
            } catch (Exception e) {
                session.rollback();
                throw new RepositoryException(e.getMessage(), e);
            }
        }
    }
}
