package com.netsentinel.dto;

import com.netsentinel.model.TargetType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record TargetRequestDTO(
        @NotBlank(message = "O nome é obrigatório")
        String name,

        @NotNull(message = "O tipo do alvo (PING, TCP, HTTP) é obrigatório")
        TargetType type,

        @NotBlank(message = "O host ou IP é obrigatório")
        String host,

        Integer port,

        String endpointPath,

        Integer expectedStatusCode,

        @NotNull(message = "O intervalo é obrigatório")
        @Positive(message = "O intervalo em segundos deve ser maior que zero")
        Integer intervalSeconds,

        @NotNull(message = "O timeout é obrigatório")
        @Positive(message = "O timeout em milissegundos deve ser maior que zero")
        Integer timeoutMs
) {
    public static final int MAX_PORT_NUMBER = 65535;

    // Validação das dependências de cada camada de rede
    public void validate() {
        if (type.requiresPort()) {
            if (port == null || port <= 0 || port > MAX_PORT_NUMBER) {
                throw new IllegalArgumentException(
                        "A porta é obrigatória e deve estar entre 1 e 65535 para o tipo " + type
                );
            }
        }

        if (type.requiresHttpDetails()) {
            if (endpointPath == null || endpointPath.isBlank()) {
                throw new IllegalArgumentException(
                        "O endpointPath (ex: '/health') é obrigatório para o tipo HTTP"
                );
            }
            if (expectedStatusCode == null) {
                throw new IllegalArgumentException(
                        "O expectedStatusCode (ex: 200) é obrigatório para o tipo HTTP"
                );
            }
        }

    }
}
