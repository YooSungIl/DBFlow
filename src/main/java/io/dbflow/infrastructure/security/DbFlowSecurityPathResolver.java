package io.dbflow.infrastructure.security;

import io.dbflow.common.exception.CryptoException;
import io.dbflow.infrastructure.path.DbFlowPathResolver;

import java.nio.file.Path;

public final class DbFlowSecurityPathResolver {

    private DbFlowSecurityPathResolver() {
    }

    public static Path resolveMasterKeyPath() {
        try {
            return DbFlowPathResolver.resolveMasterKeyPath();
        } catch (Exception e) {
            throw new CryptoException(CryptoException.KEY_LOAD_FAILED, e);
        }
    }
}
