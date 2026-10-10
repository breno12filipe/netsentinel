package com.netsentinel.model;

import java.time.LocalDateTime;

public record CheckResult(
   String targetId,
   String targetName,
   String status,          // "UP" ou "DOWN"
   long latencyMs,         // "Tempo de resposta/RTT em milissegundos
   Integer httpStatusCode, // Código retornado se for requisição HTTP (null se for PING/TCP)
   String errorMessage,    // Detalhes de falhas (ex: "Connection timed out, "Connection refused"
   LocalDateTime checkedAt // Carimbo de data/hora da checagem
) {}
