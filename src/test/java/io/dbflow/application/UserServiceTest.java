package io.dbflow.application;

import io.dbflow.common.Exception.ServiceException;
import io.dbflow.domain.User;
import io.dbflow.infrastructure.repository.UserRepository;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

class UserServiceTest {

    @Test
    void 사용자_이름과_이메일의_앞뒤_공백을_제거해_저장한다() {
        StubUserRepository repository = new StubUserRepository(null);
        UserService service = new UserService(repository);

        service.saveUserConfig("  tester  ", "  tester@example.com  ");

        assertEquals("tester", repository.savedUser.getUserName());
        assertEquals("tester@example.com", repository.savedUser.getUserEmail());
        assertEquals(1, repository.savedUser.getUseYn());
    }

    @Test
    void 활성_사용자를_그대로_반환한다() {
        User user = new User();
        UserService service = new UserService(new StubUserRepository(user));

        assertSame(user, service.findActiveUser());
    }

    @Test
    void 사용자가_없으면_존재_검증에_실패한다() {
        UserService service = new UserService(new StubUserRepository(null));

        assertThrows(ServiceException.class, service::checkUserExists);
    }

    private static class StubUserRepository extends UserRepository {
        private final User activeUser;
        private User savedUser;

        private StubUserRepository(User activeUser) {
            this.activeUser = activeUser;
        }

        @Override
        public void saveOnlyOne(User user) {
            savedUser = user;
        }

        @Override
        public User findActiveUser() {
            return activeUser;
        }
    }
}
