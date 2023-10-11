package io.conduktor.gateway.authorization;

import io.conduktor.gateway.model.User;
import io.conduktor.gateway.network.GatewayChannel;
import io.conduktor.gateway.network.handler.CustomSslHandler;
import io.netty.buffer.ByteBuf;
import io.netty.channel.socket.SocketChannel;
import jakarta.validation.constraints.NotNull;
import org.apache.kafka.common.errors.AuthenticationException;
import io.netty.handler.ssl.SslHandler;
import lombok.extern.slf4j.Slf4j;

import javax.net.ssl.SSLPeerUnverifiedException;
import javax.net.ssl.SSLSession;
import java.io.IOException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

@Slf4j
public class SslSecurityHandler implements SecurityHandler {
    private final SocketChannel gatewaySocketChannel;
    private X509Certificate[] clientCertificates;
    private String clientTrustedCN;
    private final List<String> trustedCNs;

    public SslSecurityHandler(SocketChannel gatewaySocketChannel, @NotNull List<String> trustedCNs) {
        this.gatewaySocketChannel = gatewaySocketChannel;
        this.trustedCNs = trustedCNs;
    }

    @Override
    public void authenticate(ByteBuf byteBuf) throws Exception {

    }

    @Override
    public boolean complete() {
        SslHandler sslHandler = gatewaySocketChannel.pipeline().get(SslHandler.class);

        if (sslHandler == null) {
            log.debug("No SSL handler found in the pipeline");
            return false;
        }

        if (clientCertificates != null) {
            log.debug("Client certificates already set");
            return true;
        }

        SSLSession sslSession = sslHandler.engine().getSession();

        X509Certificate[] clientCertificates;
        try {
            clientCertificates = (X509Certificate[]) sslSession.getPeerCertificates();
        } catch (SSLPeerUnverifiedException e) {
            throw new AuthenticationException(e);
        }

        List<String> clientCNs = new ArrayList<>();

        Arrays.stream(clientCertificates).toList().forEach(certificate -> {
            String subjectDN = certificate.getSubjectDN().getName();
            String cn = null;

            cn = getClientCN(subjectDN, cn);
            if (cn != null) {
                log.debug("Client certificate CN: {}", cn);
                clientCNs.add(cn);
            }
        });

        if (clientCNs.isEmpty()) {
            log.debug("No client certificate CN found");
            throw new AuthenticationException("No client certificate CN found");
        }

        String clientTrustedCN = getClientTrustedCN(trustedCNs, clientCNs);
        if (clientTrustedCN == null) {
            log.debug("Client certificate CN not trusted");
            throw new AuthenticationException("Client certificate CN not trusted");
        }


        this.clientCertificates = clientCertificates;
        this.clientTrustedCN = clientTrustedCN;
        log.debug("Client CN: {}", clientTrustedCN);
        return true;
    }

    public static String getClientTrustedCN(List<String> trustedCNs, List<String> cnList) {
        for (String cn : cnList) {
            if (trustedCNs.contains(cn)) {
                return cn;
            }
        }
        return null;
    }

    private static String getClientCN(String subjectDN, String cn) {
        for (String part : subjectDN.split(",")) {
            if (part.trim().startsWith("CN=")) {
                cn = part.trim().substring(3);
                break;
            }
        }

        return cn;
    }

    @Override
    public Optional<User> getUser() {
        if (clientCertificates == null || clientCertificates.length == 0) {
            return Optional.empty();
        }

        return Optional.of(new User(clientTrustedCN));
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
