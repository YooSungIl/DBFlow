package io.dbflow.application;

import io.dbflow.common.Exception.ServiceException;
import io.dbflow.domain.CommitLog;
import io.dbflow.domain.User;
import io.dbflow.dto.CommitLogView;
import io.dbflow.infrastructure.mybatis.MainMyBatisSqlSessionFactory;
import io.dbflow.infrastructure.repository.CommitRepository;
import io.dbflow.infrastructure.repository.SnapshotRepository;
import io.dbflow.infrastructure.repository.UserRepository;
import org.apache.ibatis.session.SqlSession;

import java.time.LocalDateTime;
import java.util.List;

public class CommitService {

    private final CommitRepository commitRepository = new CommitRepository();
    private final SnapshotRepository snapshotRepository = new SnapshotRepository();

    public void commit(String title, String content) {
        if (title == null || title.isBlank()) {
            throw new ServiceException("커밋 제목을 입력해주세요.");
        }

        WorkService workService = new WorkService();
        int workCount = workService.countWorkTarget();

        if (workCount == 0) {
            throw new ServiceException("커밋할 변경내역이 없습니다. 먼저 dbf diff 명령어를 실행해주세요.");
        }

        UserService userService = new UserService(new UserRepository());
        User user = userService.findActiveUser();

        try (SqlSession session = MainMyBatisSqlSessionFactory.getSqlSessionFactory().openSession(false)) {
            try {
                CommitLog commitLog = new CommitLog();
                commitLog.setDbConfigId(user.getDbConfigId());
                commitLog.setCommitTitle(title);
                commitLog.setCommitContent(content);
                commitLog.setUserId(user.getUserId());
                commitLog.setCommitCreatedAt(LocalDateTime.now().toString());

                commitRepository.commitWork(commitLog, session);
                snapshotRepository.insertCommitHistorySnapshot(commitLog, session);
                snapshotRepository.insertCommitCurrentSnapshot(commitLog, session);

                session.commit();

                System.out.println();
                System.out.println("커밋이 완료되었습니다.");
                System.out.println("Commit title : " + title);
            } catch (Exception e) {
                session.rollback();
                throw new RuntimeException(e.getMessage());
            }
        }
    }

    public List<CommitLogView> commitLogList(int limit) {
        UserService userService = new UserService(new UserRepository());
        User user = userService.findActiveUser();

        return commitRepository.selectCommitLogList(user, limit);
    }

    public CommitLogView commitTargetList(Long commitLogId) {
        UserService userService = new UserService(new UserRepository());
        User user = userService.findActiveUser();

        return commitRepository.selectCommitTargetList(user, commitLogId);
    }

    public CommitLogView commitObjectDetail(Long commitLogId, String objectName) {
        UserService userService = new UserService(new UserRepository());
        User user = userService.findActiveUser();

        return commitRepository.selectCommitObjectDetail(user, commitLogId, objectName);
    }

    public CommitLogView commitComponentDetail(Long commitLogId, String objectName, String componentName) {
        UserService userService = new UserService(new UserRepository());
        User user = userService.findActiveUser();

        return commitRepository.selectCommitComponentDetail(user, commitLogId, objectName, componentName);
    }
}