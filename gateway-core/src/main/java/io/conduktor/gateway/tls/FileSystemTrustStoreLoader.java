package io.conduktor.gateway.tls;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;

public class FileSystemTrustStoreLoader implements ReloadingKeyStoreProvider.KeyStoreLoader {
    private final Filesystem filesystem;
    private final TrustStoreConfig config;

    public FileSystemTrustStoreLoader(
            final Filesystem filesystem,
            final TrustStoreConfig config
    ) {
        this.filesystem = filesystem;
        this.config = config;
    }

    @Override
    public KeyStoreWrapper load()
            throws KeyStoreException, IOException, CertificateException, NoSuchAlgorithmException {
        final char[] keyStorePassword = config.getTrustStorePassword().toCharArray();
        final char[] keyPassword = config.getKeyPassword().toCharArray();
        final KeyStore keyStore = KeyStore.getInstance(config.getTrustStoreType());
        try (final InputStream is = new ByteArrayInputStream(filesystem.readFile(config.getTrustStorePath()))) {
            keyStore.load(is, keyStorePassword);
        }
        return new KeyStoreWrapper(keyStore, keyPassword);
    }
}
