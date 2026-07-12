package io.dbflow.infrastructure.security;

import io.dbflow.common.security.AesGcmStringEncryptor;
import io.dbflow.common.security.StringEncryptor;

public final class CredentialSecurity {

    private CredentialSecurity() {
    }

    public static StringEncryptor stringEncryptor() {
        return Holder.STRING_ENCRYPTOR;
    }

    private static class Holder {
        private static final StringEncryptor STRING_ENCRYPTOR = new AesGcmStringEncryptor(
                new FileEncryptionKeyProvider(DbFlowSecurityPathResolver.resolveMasterKeyPath())
        );
    }
}
