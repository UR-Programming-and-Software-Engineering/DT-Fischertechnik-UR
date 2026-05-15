package com.seurteaching.digitaltwin.simulation.optimization;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/optimization")
public class OptimizationApiController {

    @Autowired
    private ParameterOptimization optimizer;

    @PostMapping("/optimize")
    public ResponseEntity<OptimizationResponseDTO> optimize(@RequestBody OptimizationRequestDTO request) {
        OptimizationResponseDTO optimizedParams = optimizer.optimizeParameters(request);
        return ResponseEntity.ok(optimizedParams);
    }
}


