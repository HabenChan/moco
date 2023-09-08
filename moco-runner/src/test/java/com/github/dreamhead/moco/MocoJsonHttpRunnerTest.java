package com.github.dreamhead.moco;

import org.junit.jupiter.api.Test;

import static com.github.dreamhead.moco.Moco.file;
import static com.github.dreamhead.moco.Moco.pathResource;
import static com.github.dreamhead.moco.MocoJsonRunner.jsonHttpServer;
import static com.github.dreamhead.moco.Runner.running;
import static com.github.dreamhead.moco.helper.RemoteTestUtils.port;
import static com.github.dreamhead.moco.helper.RemoteTestUtils.root;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

public class MocoJsonHttpRunnerTest extends AbstractMocoStandaloneTest {
    @Test
    public void should_return_expected_response() throws Exception {
        final HttpServer server = jsonHttpServer(port(), file("src/test/resources/foo.json"));
        running(server, () -> assertThat(helper.get(root()), is("foo")));
    }

    @Test
    public void should_return_expected_response_from_path_resource() throws Exception {
        final HttpServer server = jsonHttpServer(port(), pathResource("foo.json"));
        running(server, () -> assertThat(helper.get(root()), is("foo")));
    }

    @Test
    public void should_return_expected_response_without_port() throws Exception {
        final HttpServer server = jsonHttpServer(file("src/test/resources/foo.json"));
        running(server, () -> assertThat(helper.get(root(server.port())), is("foo")));
    }
}
