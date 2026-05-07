package com.github.dreamhead.moco.internal;

import com.github.dreamhead.moco.HttpsCertificate;
import io.netty.handler.ssl.SslHandler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static com.github.dreamhead.moco.Moco.pathResource;
import static com.github.dreamhead.moco.HttpsCertificate.certificate;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;

public class AlpnSslHandlerTest {
    private HttpsCertificate cert;

    @BeforeEach
    public void setUp() {
        cert = certificate(pathResource("cert.jks"), "mocohttps", "mocohttps");
    }

    @Test
    void should_support_alpn_negotiation() {
        AlpnSslHandler sslHandler = new AlpnSslHandler(cert);

        assertThat(sslHandler, instanceOf(SslHandler.class));
        assertThat(sslHandler.engine(), notNullValue());
        assertThat(sslHandler.getSslContext(), notNullValue());
    }
}
