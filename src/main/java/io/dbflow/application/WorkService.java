package io.dbflow.application;

import io.dbflow.common.Exception.ServiceException;
import io.dbflow.domain.*;
import io.dbflow.infrastructure.repository.DbConfigRepository;
import io.dbflow.infrastructure.repository.UserRepository;
import io.dbflow.infrastructure.repository.WorkRepository;

import java.util.List;

public class WorkService {
    private final DbConfigRepository dbConfigRepository;
    private final UserRepository userRepository;
    private final WorkRepository workRepository;

    public WorkService() {
        this(new DbConfigRepository(), new UserRepository(), new WorkRepository());
    }

    public WorkService(
            DbConfigRepository dbConfigRepository,
            UserRepository userRepository,
            WorkRepository workRepository
    ) {
        this.dbConfigRepository = dbConfigRepository;
        this.userRepository = userRepository;
        this.workRepository = workRepository;
    }

    public void setWork(String alias) {

        User user = userRepository.findActiveUser();

        if (user == null) {
            throw new ServiceException(ServiceException.USER_NOT_FOUND);
        }

        DbConfig dbConfig = dbConfigRepository.findDbConfig(alias);

        if (dbConfig == null) {
            throw new ServiceException(ServiceException.DB_CONFIG_NOT_FOUND);
        }

        user.setDbConfigId(dbConfig.getDbConfigId());

        userRepository.updateCurrentDbConfigId(user);
    }

    public Work showWork() {
        Work workInfo = workRepository.findCurrentWorkInfo();

        if (workInfo == null) {
            throw new ServiceException(ServiceException.WORK_NOT_FOUND);
        }

        return workInfo;
    }

    public void delWork() {
        User user = userRepository.findActiveUser();

        if (user == null) {
            throw new ServiceException(ServiceException.USER_NOT_FOUND);
        }

        userRepository.updateDelDbConfigId(user);
    }

    public void diffResult(Long dbConfigId, WorkDiffResult result) {
        workRepository.replace(dbConfigId, result);
    }

    public List<WorkTarget> findWorkDiff(Long dbConfigId) {
        return workRepository.findWorkDiff(dbConfigId);
    }

    public int countWorkTarget(Long dbConfigId) {
        return workRepository.countWorkTarget(dbConfigId);
    }

    public List<WorkTarget> findWorkTarget(Long dbConfigId) {
        return workRepository.findWorkTarget(dbConfigId);
    }

}
