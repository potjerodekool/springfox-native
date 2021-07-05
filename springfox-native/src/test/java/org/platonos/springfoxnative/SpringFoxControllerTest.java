package org.platonos.springfoxnative;

import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.http.HttpRequest;

import java.net.URI;
import java.net.URISyntaxException;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class SpringFoxControllerTest {

    @Test
    public void testGetDocumentation() throws URISyntaxException {
        final HttpRequest requestMock = mock(HttpRequest.class);
        when(requestMock.getURI())
                .thenReturn(new URI("https://mysite/v2/api-docs"));


        SpringFoxController controller = new SpringFoxController();
        controller.getDocumentation(requestMock);
    }
}