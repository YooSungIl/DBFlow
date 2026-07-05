package io.dbflow.domain;

import java.util.ArrayList;
import java.util.List;

public class WorkDiffResult {

    private final List<WorkTarget> targets = new ArrayList<>();

    public List<WorkTarget> getTargets() {
        return targets;
    }

    public WorkTarget addTarget(WorkTarget target) {
        targets.add(target);
        return target;
    }

    public boolean isEmpty() {
        return targets.isEmpty();
    }
}
