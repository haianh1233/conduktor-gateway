package io.conduktor.gateway.network.handler;

import io.netty.buffer.ByteBufAllocator;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.ssl.SniCompletionEvent;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslHandler;
import io.netty.handler.ssl.SslHandshakeCompletionEvent;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;
import lombok.extern.slf4j.Slf4j;

import javax.net.ssl.SSLSession;
import java.security.cert.X509Certificate;

@Slf4j
public class CustomSslHandler extends SslHandler {
    public CustomSslHandler(SslContext context, ByteBufAllocator allocator) {
        super(context.newEngine(allocator));
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if (evt instanceof SniCompletionEvent handshakeEvent) {
            if (handshakeEvent.isSuccess()) {
                log.info("Handshake successful");
                log.info("Get client certificates");

                // Add a listener to be notified when the SSL handshake completes
                ctx.pipeline().get(SslHandler.class).handshakeFuture().addListener(
                        (GenericFutureListener<Future<Channel>>) future -> {
                            if (future.isSuccess()) {
                                SSLSession sslSession = engine().getSession();
                                X509Certificate[] clientCertificates = (X509Certificate[]) sslSession.getPeerCertificates();

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
                                // Now you have access to the client certificates in the clientCertificates array
                                // You can process them as needed
                            } else {
                                log.info("Handshake failed: {}", future.cause().getMessage());
                            }
                        }
                );
            } else {
                log.info("SNI detection failed: {}", handshakeEvent.cause().getMessage());

                // Handle SNI detection failure
            }
        }
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
