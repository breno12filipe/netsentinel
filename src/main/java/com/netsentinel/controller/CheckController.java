package com.netsentinel.controller;

import com.netsentinel.model.CheckResult;
import com.netsentinel.model.Target;
import com.netsentinel.service.TargetService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/checks")
public class CheckController {
    private final TargetService targetService;

    public CheckController(TargetService targetService) {
        this.targetService = targetService;
    }

    // GET /api/v1/checks/status
    // Executa a varredura instantânea de todos os alvos cadastrados (dashboard)
    @GetMapping("/status")
    public ResponseEntity<List<CheckResult>> getAllStatus() {
        List<Target> targets = targetService.findAll(null, null);
        List<CheckResult> results = targets.stream()
                .map(targetService::performCheck)
                .toList();
        return ResponseEntity.ok(results);
    }

    // POST /api/v1/checks/{targetId}/execute
    // Dispara um teste de rede imediato e síncrono para um alvo específico
    @PostMapping("/{targetId}/execute")
    public ResponseEntity<CheckResult> executeCheckNow(@PathVariable String targetId) {
        Target target = targetService.findById(targetId);
        CheckResult result = targetService.performCheck(target);
        return ResponseEntity.ok(result);
    }
}
