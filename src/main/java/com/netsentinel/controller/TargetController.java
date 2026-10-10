package com.netsentinel.controller;

import com.netsentinel.dto.TargetRequestDTO;
import com.netsentinel.model.Target;
import com.netsentinel.model.TargetType;
import com.netsentinel.repository.TargetRepository;
import com.netsentinel.service.TargetService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/targets")
public class TargetController {

    private final TargetService targetService;

    public TargetController(TargetService targetService) {
        this.targetService = targetService;
    }

    // GET /api/v1/targets?enabled=true&type=TCP
    // Listagem com suporte a filtros opcionais por status (enabled) e tipo (type)
    @GetMapping
    public ResponseEntity<List<Target>> getAllTargets(
            @RequestParam(required = false) Boolean enabled,
            @RequestParam(required = false) TargetType type) {
        List<Target> targets = targetService.findAll(enabled, type);
        return ResponseEntity.ok(targets);
    }

    // POST /api/v1/targets
    // Cadastra um novo alvo e inicia o agendamento no ScheduledExecutorService
    @PostMapping
    public ResponseEntity<Target> createTarget(@Valid @RequestBody TargetRequestDTO requestDTO) {
        // Valida regras específicas de cada camada de rede (ex: exige porta para TCP)
        requestDTO.validate();

        Target createdTarget = targetService.create(requestDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdTarget);
    }

    // GET /api/v1/targets/{id}
    // Busca as configurações de um alvo específico pelo seu ID
    @GetMapping("/{id}")
    public ResponseEntity<Target> getTargetById(@PathVariable String id) {
        Target target = targetService.findById(id);
        return ResponseEntity.ok(target);
    }

    // PUT /api/v1/targets/{id}
    // Atualiza o alvo e recalcula a tarefa agendada dinamicamente
    @PutMapping("/{id}")
    public ResponseEntity<Target> updateTarget(
            @PathVariable String id,
            @Valid @RequestBody TargetRequestDTO requestDTO) {
        requestDTO.validate();

        Target updatedTarget = targetService.update(id, requestDTO);
        return ResponseEntity.ok(updatedTarget);
    }

    // DELETE /api/v1/targets/{id}
    // Cancela o timer no agendador e remove o alvo do repositório
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTarget(@PathVariable String id) {
        targetService.delete(id);
        return ResponseEntity.noContent().build();
    }

}
