package io.dbflow.infrastructure.repository;

import io.dbflow.common.Exception.RepositoryException;
import io.dbflow.domain.*;
import io.dbflow.infrastructure.mybatis.MainMyBatisSqlSessionFactory;
import io.dbflow.infrastructure.repository.mapper.WorkMapper;
import org.apache.ibatis.session.SqlSession;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class WorkRepository {
    public Work findCurrentWorkInfo() {
        try (SqlSession session = MainMyBatisSqlSessionFactory.getSqlSessionFactory().openSession()) {
            WorkMapper workMapper = session.getMapper(WorkMapper.class);
            return workMapper.findCurrentWorkInfo();
        } catch (Exception e) {
            throw new RepositoryException("DB작업 공간 정보 조회 중 오류가 발생했습니다", e);
        }
    }

    public void deleteWork(Long dbConfigId) {
        try (SqlSession session = MainMyBatisSqlSessionFactory.getSqlSessionFactory().openSession(false)) {
            try {
                WorkMapper workMapper = session.getMapper(WorkMapper.class);

                workMapper.deleteWorkChange(dbConfigId);
                workMapper.deleteWorkComponent(dbConfigId);
                workMapper.deleteWorkTarget(dbConfigId);

                session.commit();
            } catch (Exception e) {
                session.rollback();
                throw new RepositoryException("DB접속정보 비활성화 UPDATE 중 오류가 발생했습니다.", e);
            }
        }
    }

    public void replace(Long dbConfigId, WorkDiffResult result) {
        try (SqlSession session = MainMyBatisSqlSessionFactory.getSqlSessionFactory().openSession(false)) {
            try {
                WorkMapper workMapper = session.getMapper(WorkMapper.class);

                workMapper.deleteWorkChange(dbConfigId);
                workMapper.deleteWorkComponent(dbConfigId);
                workMapper.deleteWorkTarget(dbConfigId);

                for (WorkTarget target : result.getTargets()) {
                    target.setDbConfigId(dbConfigId);
                    workMapper.insertWorkTarget(target);
                    for (WorkComponent component : target.getComponents()) {
                        component.setWorkTargetId(target.getWorkTargetId());
                        workMapper.insertWorkComponent(component);
                        for (WorkChange change : component.getChanges()) {
                            change.setWorkComponentId(component.getWorkComponentId());
                            workMapper.insertWorkChange(change);
                        }
                    }
                }
                session.commit();
            } catch (Exception e) {
                session.rollback();
                throw new RepositoryException("작업 변경내역 저장 중 오류가 발생했습니다.", e);
            }
        }
    }

    public List<WorkTarget> findWorkDiff(Long dbConfigId) {
        try (SqlSession session = MainMyBatisSqlSessionFactory.getSqlSessionFactory().openSession()) {
            WorkMapper mapper = session.getMapper(WorkMapper.class);
            List<WorkTarget> targets = mapper.findWorkTargets(dbConfigId);
            List<WorkComponent> components = mapper.findWorkComponents(dbConfigId);
            List<WorkChange> changes = mapper.findWorkChanges(dbConfigId);
            return assembleWorkDiff(targets, components, changes);
        }
    }

    private List<WorkTarget> assembleWorkDiff(List<WorkTarget> targets, List<WorkComponent> components, List<WorkChange> changes) {
        Map<Long, WorkTarget> targetMap = new HashMap<>();
        Map<Long, WorkComponent> componentMap = new HashMap<>();
        for (WorkTarget target : targets) {
            target.getComponents().clear();
            targetMap.put(target.getWorkTargetId(), target);
        }

        for (WorkComponent component : components) {
            component.getChanges().clear();
            WorkTarget target = targetMap.get(component.getWorkTargetId());
            if (target != null) {
                target.addComponent(component);
                componentMap.put(component.getWorkComponentId(), component);
            }
        }

        for (WorkChange change : changes) {
            WorkComponent component = componentMap.get(change.getWorkComponentId());
            if (component != null) {
                component.addChange(change);
            }
        }
        return targets;
    }

    public int countWorkTarget() {
        try (SqlSession session = MainMyBatisSqlSessionFactory.getSqlSessionFactory().openSession()) {
            WorkMapper mapper = session.getMapper(WorkMapper.class);
            return mapper.countWorkTarget();
        } catch (Exception e) {
            throw new RepositoryException("작업 변경내역 조회 중 오류가 발생했습니다.", e);
        }
    }

    public List<WorkTarget> findWorkTarget(Long dbConfigId) {
        try (SqlSession session = MainMyBatisSqlSessionFactory.getSqlSessionFactory().openSession()) {
            WorkMapper mapper = session.getMapper(WorkMapper.class);
            return mapper.findWorkTarget(dbConfigId);
        } catch (Exception e) {
            throw new RepositoryException("작업 변경내역 조회 중 오류가 발생했습니다.", e);
        }
    }
}
