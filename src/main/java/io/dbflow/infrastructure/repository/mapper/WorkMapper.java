package io.dbflow.infrastructure.repository.mapper;

import io.dbflow.domain.Work;
import io.dbflow.domain.WorkChange;
import io.dbflow.domain.WorkComponent;
import io.dbflow.domain.WorkTarget;

import java.util.List;

public interface WorkMapper {

    Work findCurrentWorkInfo();

    void deleteWorkChange(Long dbConfigId);
    void deleteWorkComponent(Long dbConfigId);
    void deleteWorkTarget(Long dbConfigId);

    void insertWorkTarget(WorkTarget workTarget);
    void insertWorkComponent(WorkComponent workComponent);
    void insertWorkChange(WorkChange workChange);

    List<WorkTarget> findWorkTargets(Long dbConfigId);
    List<WorkComponent> findWorkComponents(Long dbConfigId);
    List<WorkChange> findWorkChanges(Long dbConfigId);

    int countWorkTarget();
    List<WorkTarget> findWorkTarget(Long dbConfigId);
}
