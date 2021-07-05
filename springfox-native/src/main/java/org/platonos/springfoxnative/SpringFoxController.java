package org.platonos.springfoxnative;

import org.springframework.http.HttpRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

// /v2/api-docs
@RestController
public class SpringFoxController {

    public ResponseEntity<String> getDocumentation(final HttpRequest request) {
        final String hostName = request.getURI().getHost();

        final InputStream inputStream = getClass().getClassLoader().getResourceAsStream("api-docs.json");

        if (inputStream == null) {
            return ResponseEntity.notFound().build();
        }

        try {
            String documentation = new String(inputStream.readAllBytes());
            documentation = documentation.replace("$host", hostName);
            return ResponseEntity.ok(documentation);
        } catch (IOException e) {
            return ResponseEntity.notFound().build();
        }


    }
}
