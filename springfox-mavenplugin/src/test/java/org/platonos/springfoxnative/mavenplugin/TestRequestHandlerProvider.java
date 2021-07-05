package org.platonos.springfoxnative.mavenplugin;

import edu.emory.mathcs.backport.java.util.Arrays;
import org.springframework.stereotype.Component;
import springfox.documentation.RequestHandler;
import springfox.documentation.spi.service.RequestHandlerProvider;

import java.util.List;

@Component
public class TestRequestHandlerProvider implements RequestHandlerProvider  {

    private List<RequestHandler> requestHandlers = Arrays.asList(new RequestHandler[]{new TestRequestHandler()});

    @Override
    public List<RequestHandler> requestHandlers() {
        return requestHandlers;
    }
}
