package io.dbflow.infrastructure.repository;

import io.dbflow.common.Exception.RepositoryException;
import io.dbflow.domain.User;
import io.dbflow.infrastructure.mybatis.MainMyBatisSqlSessionFactory;
import io.dbflow.infrastructure.repository.mapper.UserMapper;
import org.apache.ibatis.session.SqlSession;

public class UserRepository {

    //삭제 프로세스 고민 후 업데이트로 수정 필요
    public void saveOnlyOne(User user) {
        try (SqlSession session = MainMyBatisSqlSessionFactory.getSqlSessionFactory().openSession(false)) {
            try {
                UserMapper userMapper = session.getMapper(UserMapper.class);

                userMapper.deleteAll();
                userMapper.insert(user);

                session.commit();
            } catch (Exception e) {
                session.rollback();
                throw new RepositoryException("사용자 정보 저장 중 오류가 발생했습니다", e);
            }
        }
    }

    public User findActiveUser() {
        try (SqlSession session = MainMyBatisSqlSessionFactory.getSqlSessionFactory().openSession()) {
            UserMapper userMapper = session.getMapper(UserMapper.class);

            return userMapper.findActiveUser();
        } catch (Exception e) {
            throw new RepositoryException("사용자 정보 조회 중 오류가 발생했습니다", e);
        }
    }

    public void updateCurrentDbConfigId(User user) {
        try (SqlSession session = MainMyBatisSqlSessionFactory.getSqlSessionFactory().openSession(false)) {
            try {
                UserMapper userMapper = session.getMapper(UserMapper.class);
                userMapper.updateCurrentDbConfigId(user);
                session.commit();
            } catch (Exception e) {
                session.rollback();
                throw new RepositoryException("DB작업 설정 정보 업데이트 중 오류가 발생했습니다", e);
            }
        }
    }

    public void updateDelDbConfigId(User user) {
        try (SqlSession session = MainMyBatisSqlSessionFactory.getSqlSessionFactory().openSession(false)) {
            try {
                UserMapper userMapper = session.getMapper(UserMapper.class);
                userMapper.updateDelDbConfigId(user);
                session.commit();
            } catch (Exception e) {
                session.rollback();
                throw new RepositoryException("DB작업 설정 정보 업데이트 중 오류가 발생했습니다", e);
            }
        }
    }
}