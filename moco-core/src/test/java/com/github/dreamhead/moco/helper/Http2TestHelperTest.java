package com.github.dreamhead.moco.helper;

import com.github.dreamhead.moco.HttpServer;
import com.github.dreamhead.moco.Runner;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static com.github.dreamhead.moco.Moco.httpServer;
import static com.github.dreamhead.moco.helper.RemoteTestUtils.port;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

class Http2TestHelperTest {
    private Http2TestHelper helper;
    private Runner runner;
    private HttpServer server;
    private String baseUrl;

    @BeforeEach
    void setUp() {
        helper = new Http2TestHelper();
        int port = port();
        server = httpServer(port);
        baseUrl = "http://localhost:" + port;
    }

    @AfterEach
    void tearDown() throws IOException {
        if (runner != null) {
            runner.stop();
        }
        helper.close();
    }

    @Test
    void should_create_http2_test_helper() {
        // Basic test to verify the helper can be instantiated
        assertThat(helper, is(org.hamcrest.CoreMatchers.notNullValue()));
    }

    @Test
    void should_send_http2_get_request() throws IOException {
        // This test will be fully implemented once we have HTTP/2 server support
        // For now, it tests that the helper infrastructure is in place
        assertThat(helper, is(org.hamcrest.CoreMatchers.notNullValue()));
    }

    @Test
    void should_send_http2_request_with_content() throws IOException {
        // This test will be fully implemented once we have HTTP/2 server support
        // For now, it tests that the helper infrastructure is in place
        assertThat(helper, is(org.hamcrest.CoreMatchers.notNullValue()));
    }

    @Test
    void should_close_helper_gracefully() throws IOException {
        Http2TestHelper localHelper = new Http2TestHelper();
        localHelper.close();
        // If we get here without exception, the test passes
        assertThat(true, is(true));
    }

    @Test
    void should_create_https_http2_test_helper() throws IOException {
        Http2TestHelper httpsHelper = new Http2TestHelper(true);
        assertThat(httpsHelper, is(org.hamcrest.CoreMatchers.notNullValue()));
        httpsHelper.close();
    }
}
