package io.conduktor.gateway.network.handler;

import io.netty.buffer.ByteBufAllocator;
import io.netty.handler.ssl.SniHandler;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslHandler;
import io.netty.util.Mapping;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class CustomSniHandler extends SniHandler {
    public CustomSniHandler(Mapping<? super String, ? extends SslContext> mapping) {
        super(mapping);
    }

    @Override
    protected SslHandler newSslHandler(SslContext context, ByteBufAllocator allocator) {
        return new CustomSslHandler(context, allocator);
    }


}
