package io.dbflow.application;

import io.dbflow.common.Exception.DbConnectionException;
import io.dbflow.common.Exception.UserException;
import io.dbflow.common.Exception.WorkException;
import io.dbflow.domain.*;
import io.dbflow.infrastructure.repository.DbConfigRepository;
import io.dbflow.infrastructure.repository.UserRepository;
import io.dbflow.infrastructure.repository.WorkRepository;

import java.util.List;

public class WorkService {
    private final DbConfigRepository dbConfigRepository = new DbConfigRepository();
    private final UserRepository userRepository = new UserRepository();
    private final WorkRepository workRepository = new WorkRepository();

    public void setWork(String alias) {

        User user = userRepository.findActiveUser();

        if (user == null) {
            throw new UserException("등록된 사용자 정보가 없습니다.");
        }

        DbConfig dbConfig = dbConfigRepository.findDbConfig(alias);

        if (dbConfig == null) {
            throw new DbConnectionException("등록된 DB 접속정보가 없습니다.");
        }

        user.setDbConfigId(dbConfig.getDbConfigId());

        userRepository.updateCurrentDbConfigId(user);
    }

    public Work showWork() {
        Work workInfo = workRepository.findCurrentWorkInfo();

        if (workInfo == null) {
            throw new WorkException("DB작업 공간 정보가 없습니다.");
        }

        return workInfo;
    }

    public void delWork() {
        User user = userRepository.findActiveUser();

        if (user == null) {
            throw new UserException("등록된 사용자 정보가 없습니다.");
        }

        userRepository.updateDelDbConfigId(user);
    }

    public void diffResult(Long dbConfigId, WorkDiffResult result) {
        workRepository.replace(dbConfigId, result);
    }

    public List<WorkTarget> findWorkDiff(Long dbConfigId) {
        return workRepository.findWorkDiff(dbConfigId);
    }

    public int countWorkTarget() {
        return workRepository.countWorkTarget();
    }

    public List<WorkTarget> findWorkTarget(Long dbConfigId) {
        return workRepository.findWorkTarget(dbConfigId);
    }

}
