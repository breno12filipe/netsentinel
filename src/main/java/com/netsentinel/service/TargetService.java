package com.netsentinel.service;

import com.netsentinel.dto.TargetRequestDTO;
import com.netsentinel.model.CheckResult;
import com.netsentinel.model.Target;
import com.netsentinel.model.TargetType;
import com.netsentinel.repository.TargetRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import jakarta.annotation.PreDestroy;
import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;

@Service
public class TargetService {
    private static final Logger log = LoggerFactory.getLogger(TargetService.class);
    private final TargetRepository targetRepository;

    // Pool de Threads para execução paralela de checagens de rede
    private final ScheduledExecutorService executorService;

    // Mapeia o ID do Target com a sua tarefa agendada ativa para permitir cancelamentos/reagendamentos dinâmicos
    private final Map<String, ScheduledFuture<?>> scheduledTasks = new ConcurrentHashMap<>();

    // Cliente HTTP reutilizável e thread-safe
    private final HttpClient httpClient;

    public TargetService(TargetRepository targetRepository) {
        this.targetRepository = targetRepository;
        // Pool de 10 threads para checagens assíncronas em background
        this.executorService = Executors.newScheduledThreadPool(10);
        this.httpClient = HttpClient.newBuilder()
                .followRedirects(HttpClient.Redirect.NORMAL)
                .connectTimeout(Duration.ofSeconds(5))
                .build();
    }

    // --- OPERAÇÕES CRUD & AGENDAMENTO DINÂMICO
    public List<Target> findAll(Boolean enabled, TargetType type) {
        return targetRepository.findByEnabledAndType(enabled, type);
    }

    public Target findById(String id) {
        return targetRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Alvo não encontrado com o ID: " + id));
    }

    public Target create(TargetRequestDTO dto) {
        Target target = new Target(
                null,
                dto.name(),
                dto.type(),
                dto.host(),
                dto.port(),
                dto.endpointPath(),
                dto.expectedStatusCode(),
                dto.intervalSeconds(),
                dto.timeoutMs()
        );

        Target savedTarget = targetRepository.save(target);

        if (savedTarget.isEnabled()) {
            scheduleTask(savedTarget);
        }

        return savedTarget;
    }

    public Target update(String id, TargetRequestDTO dto) {
        Target existing = findById(id);

        existing.setName(dto.name());
        existing.setType(dto.type());
        existing.setHost(dto.host());
        existing.setPort(dto.port());
        existing.setEndpointPath(dto.endpointPath());
        existing.setExpectedStatusCode(dto.expectedStatusCode());
        existing.setIntervalSeconds(dto.intervalSeconds());
        existing.setTimeoutMs(dto.timeoutMs());

        Target updated = targetRepository.save(existing);

        // Reagenda a tarefa com os novos parâmetros ou cancela se foi desativada
        cancelScheduledTask(id);
        if (updated.isEnabled()) {
            scheduleTask(updated);
        }

        return updated;
    }

    public void delete(String id) {
        if (!targetRepository.existsById(id)) {
            throw new IllegalArgumentException("Alvo não encontrado com o ID: " + id);
        }
        cancelScheduledTask(id);
        targetRepository.deleteById(id);
    }

    // --- GERENCIAMENTO DE THREADS (SCHEDULEDEXECUTORSERVICE) ---

    private void scheduleTask(Target target) {
        log.info("Agendando checagem periódica para '{}' [{}] a cada {}s",
                target.getName(), target.getType(), target.getIntervalSeconds());

        // Programa a tarefa para rodar repetidamente com um delay fixo (fixedDelay garante o intervalo entre o FIM de uma checagem e o INÍCIO da próxima)
        ScheduledFuture<?> future = executorService.scheduleWithFixedDelay(
                () -> performCheck(target),
                0, // Primeira execução imediata
                target.getIntervalSeconds(),
                TimeUnit.SECONDS
        );

        scheduledTasks.put(target.getId(), future);
    }

    private void cancelScheduledTask(String targetId) {
        ScheduledFuture<?> future = scheduledTasks.remove(targetId);
        if (future != null) {
            log.info("Cancelando agendamento prévio do alvo ID: {}", targetId);
            future.cancel(true);
        }
    }

    // --- MOTOR DE CHECAGEM DE REDES (L3, L4, L7) ---
    public CheckResult performCheck(Target target) {
        long startTime = System.currentTimeMillis();
        boolean isUp = false;
        String errorMessage = null;
        Integer statusCode = null;

        try {
            switch(target.getType()) {
                case PING -> isUp = checkPing(target.getHost(), target.getTimeoutMs());
                case TCP -> isUp = checkTcpSocket(target.getHost(), target.getPort(), target.getTimeoutMs());
                case HTTP -> {
                    HttpResponse<Void> response = checkHttp(target);
                    statusCode = response.statusCode();
                    Integer expected = target.getExpectedStatusCode() != null ? target.getExpectedStatusCode() : 200;
                    isUp = (statusCode.equals(expected));
                    if (!isUp) {
                        errorMessage = "Status HTTP retornado (" + statusCode + ") difere do esperado (" + expected + ")";
                    }
                }
            }
        } catch (Exception e) {
            isUp = false;
            errorMessage = e.getMessage();
        }

        long latency = System.currentTimeMillis() - startTime;

        CheckResult result = new CheckResult(
                target.getId(),
                target.getName(),
                isUp ? "UP" : "DOWN",
                latency,
                statusCode,
                errorMessage,
                LocalDateTime.now()
        );

        log.debug("Result da varredura em '{}': Status={}, Latência={}ms",
            target.getName(), result.status(), latency);

        return result;
    }

    // Sub-motor L3: PING / ICMP
    private boolean checkPing(String host, int timeoutMs) throws IOException {
        InetAddress address = InetAddress.getByName(host);
        return address.isReachable(timeoutMs);
    }

    // Sub-motor L4: Socket TCP (3-Way Handshake)
    private boolean checkTcpSocket(String host, int port, int timeoutMs) throws IOException {
        try (Socket socket = new Socket()) {
            socket.connect(new java.net.InetSocketAddress(host, port), timeoutMs);
            return socket.isConnected();
        }
    }

    // Sub-motor L7: HTTP/HTTPS Client Request
    private HttpResponse<Void> checkHttp(Target target) throws IOException, InterruptedException {
        String protocol = target.getPort() != null && target.getPort() == 443 ? "https" : "http";
        String portSuffix = (target.getPort() != null && target.getPort() != 80 && target.getPort() != 443)
                ? ":" + target.getPort() : "";

        String url = String.format("%s://%s%s%s", protocol, target.getHost(), portSuffix, target.getEndpointPath());

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .timeout(Duration.ofMillis(target.getTimeoutMs()))
                .method("HEAD", HttpRequest.BodyPublishers.noBody()) // Usa HEAD para economizar banda
                .build();

        return httpClient.send(request, HttpResponse.BodyHandlers.discarding());
    }

    @PreDestroy
    public void shutdown() {
        log.info("Encerrando pool de threads do NetSentinel...");
        executorService.shutdown();
        try {
            if (!executorService.awaitTermination(5, TimeUnit.SECONDS)) {
                executorService.shutdownNow();
            }
        } catch (InterruptedException e) {
            executorService.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }
}
