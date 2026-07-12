package io.dbflow.application;

import io.dbflow.common.Exception.ServiceException;
import io.dbflow.domain.User;
import io.dbflow.infrastructure.repository.CommitRepository;
import io.dbflow.infrastructure.repository.SnapshotRepository;
import io.dbflow.infrastructure.repository.UserRepository;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class CommitServiceTest {

    @Test
    void 제목이_비어_있으면_Commit할_수_없다() {
        CommitService service = service(user(), 1);

        ServiceException exception = assertThrows(ServiceException.class, () -> service.commit(" ", "내용"));

        assertEquals(ServiceException.COMMIT_TITLE_REQUIRED, exception.getMessage());
    }

    @Test
    void 사용자가_없으면_Commit할_수_없다() {
        CommitService service = service(null, 1);

        ServiceException exception = assertThrows(ServiceException.class, () -> service.commit("제목", "내용"));

        assertEquals(ServiceException.USER_NOT_FOUND, exception.getMessage());
    }

    @Test
    void 변경내역이_없으면_Commit할_수_없다() {
        CommitService service = service(user(), 0);

        ServiceException exception = assertThrows(ServiceException.class, () -> service.commit("제목", "내용"));

        assertEquals(ServiceException.COMMIT_WORK_NOT_FOUND, exception.getMessage());
    }

    private CommitService service(User user, int workCount) {
        return new CommitService(
                new CommitRepository(),
                new SnapshotRepository(),
                new StubUserService(user),
                new StubWorkService(workCount)
        );
    }

    private User user() {
        User user = new User();
        user.setUserId(1L);
        user.setDbConfigId(100L);
        return user;
    }

    private static class StubUserService extends UserService {
        private final User user;

        private StubUserService(User user) {
            super(new UserRepository());
            this.user = user;
        }

        @Override
        public User findActiveUser() {
            return user;
        }
    }

    private static class StubWorkService extends WorkService {
        private final int workCount;

        private StubWorkService(int workCount) {
            this.workCount = workCount;
        }

        @Override
        public int countWorkTarget(Long dbConfigId) {
            return workCount;
        }
    }
}
