package io.conduktor.gateway.network.handler;

import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.ssl.SslHandler;
import io.netty.util.concurrent.FutureListener;
import lombok.extern.slf4j.Slf4j;

import java.security.cert.Certificate;
import java.security.cert.X509Certificate;

@Slf4j
public class PostHandshakeHandler extends SimpleChannelInboundHandler<Object> {
    private final SslHandler sslHandler;

    public PostHandshakeHandler(SslHandler sslHandler) {
        this.sslHandler = sslHandler;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Object msg) throws Exception {
        sslHandler.handshakeFuture().addListener(future -> {
            if (future.isSuccess()) {
                Certificate[] clientCerts = sslHandler.engine().getSession().getPeerCertificates();

                // Handle client certificates here
                for (Certificate cert : clientCerts) {
                    // Do something with the certificate
                    if (cert instanceof X509Certificate x509Cert) {
                        // Access certificate details
                        log.info("Client certificate details: {}", x509Cert.getSubjectDN());
                    }
                }
            } else {
                // Handle handshake failure
            }
        });

        ctx.fireChannelActive();
    }


    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        super.channelActive(ctx);
    }
}
