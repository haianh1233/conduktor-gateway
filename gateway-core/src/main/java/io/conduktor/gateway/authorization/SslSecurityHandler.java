package io.conduktor.gateway.authorization;

import io.conduktor.gateway.model.User;
import io.conduktor.gateway.network.GatewayChannel;
import io.conduktor.gateway.network.handler.CustomSslHandler;
import io.netty.buffer.ByteBuf;
import io.netty.channel.socket.SocketChannel;
import jakarta.validation.constraints.NotNull;
import org.apache.commons.lang3.StringUtils;
import org.apache.kafka.common.errors.AuthenticationException;
import io.netty.handler.ssl.SslHandler;
import lombok.extern.slf4j.Slf4j;

import javax.net.ssl.SSLPeerUnverifiedException;
import javax.net.ssl.SSLSession;
import java.io.IOException;
import java.security.Principal;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

@Slf4j
public class SslSecurityHandler implements SecurityHandler {
    private final SocketChannel gatewaySocketChannel;
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
    public boolean complete() throws SSLPeerUnverifiedException {
        SslHandler sslHandler = gatewaySocketChannel.pipeline().get(SslHandler.class);

        if (sslHandler == null) {
            log.debug("No SSL handler found in the pipeline");
            return false;
        }

        SSLSession sslSession = sslHandler.engine().getSession();

        this.clientTrustedCN = sslSession.getPeerPrincipal().getName();
        return true;
    }


    @Override
    public Optional<User> getUser() {
        if (StringUtils.isBlank(clientTrustedCN)) {
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
}
