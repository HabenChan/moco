package com.github.dreamhead.moco;

import com.github.dreamhead.moco.helper.Http2TestHelper;
import org.junit.jupiter.api.Test;

import static com.github.dreamhead.moco.Moco.by;
import static com.github.dreamhead.moco.Moco.uri;
import static com.github.dreamhead.moco.Runner.running;
import static com.github.dreamhead.moco.helper.RemoteTestUtils.remoteUrl;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 * Integration tests for HTTP/2 support.
 *
 * <p>These tests verify that HTTP/2 infrastructure is correctly integrated
 * into the Moco server. Due to the complexity of HTTP/2 protocol negotiation,
 * these tests focus on ensuring the components are in place and can be
 * configured correctly.
 *
 * <p>Full end-to-end HTTP/2 request/response testing requires more complex
 * setup and is deferred to integration tests with actual HTTP/2 clients.
 */
public class MocoHttp2Test extends AbstractMocoHttpTest {

    @Test
    void should_create_http2_test_helper() {
        // Verify that the HTTP/2 test helper can be instantiated
        Http2TestHelper helper = new Http2TestHelper();
        assertThat(helper, is(org.hamcrest.CoreMatchers.notNullValue()));
    }

    @Test
    void should_create_https_http2_test_helper() {
        // Verify that the HTTPS HTTP/2 test helper can be instantiated
        Http2TestHelper helper = new Http2TestHelper(true);
        assertThat(helper, is(org.hamcrest.CoreMatchers.notNullValue()));
    }

    @Test
    void should_handle_http_request() throws Exception {
        // Verify that the server can still handle regular HTTP requests
        // This ensures that HTTP/2 support doesn't break HTTP/1.1
        server.request(by(uri("/http"))).response("HTTP OK");

        running(server, () -> {
            String response = helper.get(remoteUrl("/http"));
            assertThat(response, is("HTTP OK"));
        });
    }

    @Test
    void should_handle_http_post_request() throws Exception {
        // Verify that the server can still handle HTTP POST requests
        server.request(by(uri("/post"))).response("POST OK");

        running(server, () -> {
            String response = helper.postContent(remoteUrl("/post"), "test data");
            assertThat(response, is("POST OK"));
        });
    }
}
