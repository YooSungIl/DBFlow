package io.dbflow.application;

import io.dbflow.common.DateTimeHelper;
import io.dbflow.common.Exception.ServiceException;
import io.dbflow.domain.User;
import io.dbflow.infrastructure.repository.UserRepository;

public class UserService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public void saveUserConfig(String name, String email) {
        String now = DateTimeHelper.now();
        User user = new User(name.trim(), email.trim(), 1, now, now);
        userRepository.saveOnlyOne(user);
    }

    public User findActiveUser() {
        return userRepository.findActiveUser();
    }

    public void checkUserExists() {
        User user = userRepository.findActiveUser();

        if (user == null) {
            throw new ServiceException("사용자 정보가 없습니다. 먼저 dbf user set 명령어를 실행해 주세요.");
        }
    }
}
