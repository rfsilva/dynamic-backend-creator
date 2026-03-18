package com.rfsilva.jcodemodel.controller;

import com.rfsilva.jcodemodel.dto.EntityDefinition;
import com.rfsilva.jcodemodel.dto.GenerationResult;
import com.rfsilva.jcodemodel.service.CodeGeneratorService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/generate")
@RequiredArgsConstructor
public class CodeGeneratorController {

    private final CodeGeneratorService codeGeneratorService;

    /**
     * Generates a complete Spring Boot CRUD application from an entity definition.
     *
     * @param definition The entity definition containing name, package, and fields.
     * @return 201 Created with generation details on success, or appropriate error code.
     */
    @PostMapping
    public ResponseEntity<GenerationResult> generate(@Valid @RequestBody EntityDefinition definition) {
        GenerationResult result = codeGeneratorService.generate(definition);
        return ResponseEntity.status(HttpStatus.CREATED).body(result);
    }
}
