package io.dbflow.common.console;

import io.dbflow.domain.WorkChange;
import io.dbflow.domain.WorkComponent;
import io.dbflow.domain.WorkTarget;

import java.util.List;

public final class WorkDiffPrinter {

    private WorkDiffPrinter() {
    }

    public static void print(List<WorkTarget> targets) {
        System.out.println();
        System.out.println("DBFlow Work Diff");
        System.out.println();

        if (targets.isEmpty()) {
            System.out.println("No changes.");
            return;
        }

        for (WorkTarget target : targets) {
            printTarget(target);
        }
    }

    private static void printTarget(WorkTarget target) {
        System.out.printf("[%s] %s %s%n",
                target.getChangeType(),
                target.getObjectType(),
                target.getObjectName()
        );

        for (WorkComponent component : target.getComponents()) {
            printComponent(component);
        }

        System.out.println();
    }

    private static void printComponent(WorkComponent component) {
        System.out.printf("  [%s] %s %s%n",
                component.getChangeType(),
                component.getComponentType(),
                component.getComponentName()
        );

        for (WorkChange change : component.getChanges()) {
            printChange(change);
        }
    }

    private static void printChange(WorkChange change) {
        System.out.printf("    - %-15s : %s -> %s%n",
                change.getChangeColumn(),
                change.getBeforeValue(),
                change.getAfterValue()
        );
    }
}
