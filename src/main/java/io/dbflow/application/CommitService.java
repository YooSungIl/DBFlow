package io.dbflow.application;

import io.dbflow.common.Exception.ServiceException;
import io.dbflow.common.DateTimeHelper;
import io.dbflow.domain.CommitLog;
import io.dbflow.domain.User;
import io.dbflow.dto.CommitLogView;
import io.dbflow.infrastructure.mybatis.MainMyBatisSqlSessionFactory;
import io.dbflow.infrastructure.repository.CommitRepository;
import io.dbflow.infrastructure.repository.SnapshotRepository;
import io.dbflow.infrastructure.repository.UserRepository;
import org.apache.ibatis.session.SqlSession;

import java.util.List;

public class CommitService {

    private final CommitRepository commitRepository;
    private final SnapshotRepository snapshotRepository;
    private final UserService userService;
    private final WorkService workService;

    public CommitService() {
        this(
                new CommitRepository(),
                new SnapshotRepository(),
                new UserService(new UserRepository()),
                new WorkService()
        );
    }

    public CommitService(
            CommitRepository commitRepository,
            SnapshotRepository snapshotRepository,
            UserService userService,
            WorkService workService
    ) {
        this.commitRepository = commitRepository;
        this.snapshotRepository = snapshotRepository;
        this.userService = userService;
        this.workService = workService;
    }

    public void commit(String title, String content) {
        if (title == null || title.isBlank()) {
            throw new ServiceException(ServiceException.COMMIT_TITLE_REQUIRED);
        }

        User user = userService.findActiveUser();

        if (user == null) {
            throw new ServiceException(ServiceException.USER_NOT_FOUND);
        }

        int workCount = workService.countWorkTarget(user.getDbConfigId());

        if (workCount == 0) {
            throw new ServiceException(ServiceException.COMMIT_WORK_NOT_FOUND);
        }

        try (SqlSession session = MainMyBatisSqlSessionFactory.getSqlSessionFactory().openSession(false)) {
            try {
                CommitLog commitLog = new CommitLog();
                commitLog.setDbConfigId(user.getDbConfigId());
                commitLog.setCommitTitle(title);
                commitLog.setCommitContent(content);
                commitLog.setUserId(user.getUserId());
                commitLog.setCommitCreatedAt(DateTimeHelper.now());

                commitRepository.commitWork(commitLog, session);
                snapshotRepository.insertCommitHistorySnapshot(commitLog, session);
                snapshotRepository.insertCommitCurrentSnapshot(commitLog, session);

                session.commit();
            } catch (Exception e) {
                session.rollback();
                throw new ServiceException(e.getMessage(), e);
            }
        }
    }

    public List<CommitLogView> commitLogList(int limit) {
        User user = userService.findActiveUser();

        return commitRepository.selectCommitLogList(user, limit);
    }

    public CommitLogView commitTargetList(Long commitLogId) {
        User user = userService.findActiveUser();

        return commitRepository.selectCommitTargetList(user, commitLogId);
    }

    public CommitLogView commitObjectDetail(Long commitLogId, String objectName) {
        User user = userService.findActiveUser();

        return commitRepository.selectCommitObjectDetail(user, commitLogId, objectName);
    }

    public CommitLogView commitComponentDetail(Long commitLogId, String objectName, String componentName) {
        User user = userService.findActiveUser();

        return commitRepository.selectCommitComponentDetail(user, commitLogId, objectName, componentName);
    }
}
