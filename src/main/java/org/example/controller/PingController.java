package org.example.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class PingController {

    record PingResponse(String status, String java) {}

    @GetMapping("/api/ping")
    public PingResponse ping() {
        String javaMajor = String.valueOf(Runtime.version().feature());
        return new PingResponse("ok", javaMajor);
    }
}