package io.conduktor.gateway.network;

import io.conduktor.gateway.authorization.SecurityHandler;
import io.conduktor.gateway.model.User;
import io.conduktor.gateway.network.handler.CustomSslHandler;
import io.netty.buffer.ByteBuf;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.ssl.SslHandler;
import lombok.extern.slf4j.Slf4j;

import javax.net.ssl.SSLPeerUnverifiedException;
import javax.net.ssl.SSLSession;
import java.io.IOException;
import java.security.cert.X509Certificate;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

@Slf4j
public class SslSecurityHandler implements SecurityHandler {
    private final SocketChannel gatewaySocketChannel;
    public SslSecurityHandler(SocketChannel gatewaySocketChannel) {
        this.gatewaySocketChannel = gatewaySocketChannel;
    }

    @Override
    public void authenticate(ByteBuf byteBuf) throws Exception {


    }

    @Override
    public boolean complete() {
        SslHandler sslHandler = gatewaySocketChannel.pipeline().get(CustomSslHandler.class);

        if (sslHandler == null) {
            log.debug("No SSL handler found in the pipeline");
            return true;
        }

        SSLSession sslSession = sslHandler.engine().getSession();

        X509Certificate[] clientCertificates;
        try {
            clientCertificates = (X509Certificate[]) sslSession.getPeerCertificates();
        } catch (SSLPeerUnverifiedException e) {
            throw new RuntimeException(e);
        }

        for (X509Certificate clientCertificate : clientCertificates) {
            if (isCACertificate(clientCertificate)) {
                log.info("-------------------------------------------");
                log.info("Client certificate is a CA certificate");
                log.info("Client certificate: {}", clientCertificate);

            } else {
                log.info("-------------------------------------------");
                log.info("Client certificate is not a CA certificate");
                log.info("Client certificate: {}", clientCertificate);
                log.info("Client certificate subject: {}", clientCertificate.getSubjectDN());
            }
        }
        return true;
    }

    @Override
    public Optional<User> getUser() {
        return Optional.empty();
    }

    @Override
    public void setGatewayChannel(GatewayChannel channel) {
        
    }

    @Override
    public CompletableFuture<Void> handleAuthenticationFailure() {
        return CompletableFuture.completedFuture(null);
    }

    @Override
    public void close() throws IOException {

    }

    private boolean isCACertificate(X509Certificate certificate) {
        // Check if the certificate has the BasicConstraints extension and it's marked as a CA certificate
        if (certificate.getExtensionValue("2.5.29.19") != null) {
            return certificate.getBasicConstraints() >= 0;
        }
        // Check if the certificate has the KeyUsage extension and it's marked as a keyCertSign
        if (certificate.getExtensionValue("2.5.29.15") != null) {
            boolean[] keyUsage = certificate.getKeyUsage();
            return keyUsage != null && keyUsage[5]; // 5 corresponds to keyCertSign
        }
        return false;
    }
}
