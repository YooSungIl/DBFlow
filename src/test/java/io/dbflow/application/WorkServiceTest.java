package io.dbflow.application;

import io.dbflow.common.Exception.ServiceException;
import io.dbflow.domain.DbConfig;
import io.dbflow.domain.User;
import io.dbflow.domain.Work;
import io.dbflow.infrastructure.repository.DbConfigRepository;
import io.dbflow.infrastructure.repository.UserRepository;
import io.dbflow.infrastructure.repository.WorkRepository;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

class WorkServiceTest {

    @Test
    void 사용자가_없으면_Work를_설정할_수_없다() {
        WorkService service = service(null, dbConfig(10L), null);

        ServiceException exception = assertThrows(ServiceException.class, () -> service.setWork("local"));

        assertEquals(ServiceException.USER_NOT_FOUND, exception.getMessage());
    }

    @Test
    void DB접속정보가_없으면_Work를_설정할_수_없다() {
        WorkService service = service(user(1L), null, null);

        ServiceException exception = assertThrows(ServiceException.class, () -> service.setWork("missing"));

        assertEquals(ServiceException.DB_CONFIG_NOT_FOUND, exception.getMessage());
    }

    @Test
    void Work를_설정하면_사용자의_DB설정_ID를_변경한다() {
        User user = user(1L);
        StubUserRepository userRepository = new StubUserRepository(user);
        WorkService service = new WorkService(new StubDbConfigRepository(dbConfig(10L)), userRepository, new StubWorkRepository(null));

        service.setWork("local");

        assertEquals(10L, user.getDbConfigId());
        assertSame(user, userRepository.updatedUser);
    }

    @Test
    void 현재_Work가_없으면_조회할_수_없다() {
        WorkService service = service(user(1L), dbConfig(10L), null);

        ServiceException exception = assertThrows(ServiceException.class, service::showWork);

        assertEquals(ServiceException.WORK_NOT_FOUND, exception.getMessage());
    }

    @Test
    void Work를_해제하면_사용자의_DB설정_ID를_제거한다() {
        User user = user(1L);
        StubUserRepository userRepository = new StubUserRepository(user);
        WorkService service = new WorkService(new StubDbConfigRepository(null), userRepository, new StubWorkRepository(null));

        service.delWork();

        assertSame(user, userRepository.deletedWorkUser);
    }

    @Test
    void 사용자가_없으면_Work를_해제할_수_없다() {
        WorkService service = service(null, null, null);

        ServiceException exception = assertThrows(ServiceException.class, service::delWork);

        assertEquals(ServiceException.USER_NOT_FOUND, exception.getMessage());
    }

    private WorkService service(User user, DbConfig dbConfig, Work work) {
        return new WorkService(
                new StubDbConfigRepository(dbConfig),
                new StubUserRepository(user),
                new StubWorkRepository(work)
        );
    }

    private User user(Long id) {
        User user = new User();
        user.setUserId(id);
        return user;
    }

    private DbConfig dbConfig(Long id) {
        DbConfig dbConfig = new DbConfig();
        dbConfig.setDbConfigId(id);
        return dbConfig;
    }

    private static class StubUserRepository extends UserRepository {
        private final User user;
        private User updatedUser;
        private User deletedWorkUser;

        private StubUserRepository(User user) {
            this.user = user;
        }

        @Override
        public User findActiveUser() {
            return user;
        }

        @Override
        public void updateCurrentDbConfigId(User user) {
            updatedUser = user;
        }

        @Override
        public void updateDelDbConfigId(User user) {
            deletedWorkUser = user;
        }
    }

    private static class StubDbConfigRepository extends DbConfigRepository {
        private final DbConfig dbConfig;

        private StubDbConfigRepository(DbConfig dbConfig) {
            this.dbConfig = dbConfig;
        }

        @Override
        public DbConfig findDbConfig(String dbAlias) {
            return dbConfig;
        }
    }

    private static class StubWorkRepository extends WorkRepository {
        private final Work work;

        private StubWorkRepository(Work work) {
            this.work = work;
        }

        @Override
        public Work findCurrentWorkInfo() {
            return work;
        }
    }
}
