package io.dbflow.command;

import io.dbflow.common.DbFlowVersion;
import picocli.CommandLine;

public class DbFlowVersionProvider implements CommandLine.IVersionProvider {

    @Override
    public String[] getVersion() {
        return new String[]{DbFlowVersion.getAppVersion()};
    }
}
