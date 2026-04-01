package br.edu.central.centrallj.controller;

import java.time.Instant;
import java.util.Map;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class HelloController {

  @GetMapping("/hello")
  public Map<String, Object> hello() {
    return Map.of(
        "message", "Central-LJ online — pronta para receber missões.",
        "timestamp", Instant.now().toString());
  }
}

