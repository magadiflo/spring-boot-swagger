package dev.magadiflo.swagger.app.web.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(path = "/api/v1/persons")
public class PersonRestController {

    @GetMapping
    public ResponseEntity<String> getName() {
        return ResponseEntity.ok("Martín");
    }
}
