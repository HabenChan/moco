package com.github.dreamhead.moco.extractor;

import com.github.dreamhead.moco.HttpProtocolVersion;
import com.github.dreamhead.moco.HttpRequest;
import io.netty.handler.codec.http.HttpVersion;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class VersionExtractorTest {
	private VersionExtractor extractor;
	private HttpRequest request;

	@BeforeEach
	public void setUp() {
		extractor = new VersionExtractor();
        request = mock(HttpRequest.class);
    }

	@Test
	public void should_extract_version() {
        when(request.getVersion()).thenReturn(HttpProtocolVersion.VERSION_1_0);
		assertThat(extractor.extract(request).get(),
				is(HttpVersion.HTTP_1_0.toString()));
	}
}
