package com.netsentinel.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

/*
 * Tipo PING (Camada 3 (Rede) - ICMP)
 * - A máquina local envia um pacote ICMP Echo Request para o IP do alvo.
 *   A pilha de redes da máquina destino intercepta esse pacote no nível do Kernel do S.O
 *   e responde com um ICMP Echo Reply.
 * - O ICMP não possui portas. Por isso o atributo `port` é null.
 * - O PING está medindo o RTT (Round Trip Time), ou seja, o tempo em milissegundos
 *   que o pacote leva para ir e voltar através dos roteadores.
 *
 * Tipo TCP (Camada 4 (Transport) - TCP)
 * - Para saber se um serviço que está em uma porta está sendo executado, a aplicação
 *   solicita ao S.O a abertura de uma conexão. A placa de rede dispara o processo de 3 Way HandShake:
 *      SYN: O servidor envia um pacote com a flag `SYN` ativada para a porta de destino
 *      SYN-ACK: Se o serviço estiver rodando e a porta estiver aberta, o destino responde com `SYN_ACK`
 *      ACK/RST: A aplicação confirma a conexão ou fecha o socket enviando um RST (reset) para não ocupar recursos.
 * - Na camada 4, a porta identifica o serviço dentro da máquina
 * - Se o destino responder com `RST`, significa que a máquina está online, mas o serviço está desligado (porta fechada)
 * - Se der timeout sem nenhuma resposta, significa que um `Firewall/ Security Group` no caminho descartou o pacote (DROP/REJECT)
 *
 * Tipo HTTP (Camada 7 (Aplicação) = HTTP/TCP
 * - Primeiro ocorre o 3-Way Handshake TCP (Camada 4) na porta especificada (80 ou 443).
 * - Segundo a aplicação envia um cabeçalho de texto codificado conforme a especificação HTTP (Camada 7), como `GET /actuator/health HTTP/1.1`
 * - Na camada 7 precisamos de `endpointPath` e `expectedStatusCode`, pois o fato da porta TCP estar aberta não nos
 *   garante que a aplicação esteja funcionando. O servidor web pode responder `500 Internal Server Error` ou `503 Service Unavailable`
 * - Validamos que não apenas a rede e a porta estão ativas, mas que a lógica da aplicação web está saudável e devolvendo o status esperado (ex: 200 OK).
 *
 * O `timeoutMs` na entidade `Target` serve como uma janela de tolerância de conectividade:
 *    - Se um socket tentar se conectar sem um timeout, o S.O pode esperar de 45 a 120 segundos tentando retransmitir pacotes SYN
 *      antes de desistir. Não podemos travar uma thread por 2 minutos, então estabelecemos um limite estrito para interromper
 *      a tentativa e marcar o host como DOWN.
 */
public enum TargetType {
    PING("PING", false, false),
    TCP("TCP", true, false),
    HTTP("HTTP", true, true);


    private final String value;
    private final boolean requiresPort;
    private final boolean requiresHttpDetails;

    TargetType(String value, boolean requiresPort, boolean requiresHttpDetails) {
        this.value = value;
        this.requiresPort = requiresPort;
        this.requiresHttpDetails = requiresHttpDetails;
    }

    @JsonValue
    public String getValue() {
        return value;
    }

    public boolean requiresPort() {
        return requiresPort;
    }

    public boolean requiresHttpDetails() {
        return requiresHttpDetails;
    }

    @JsonCreator
    public static TargetType fromValue(String value) {
        if (value == null) {
            return null;
        }
        for (TargetType type: TargetType.values()) {
            if (type.value.equalsIgnoreCase(value.trim())) {
                return type;
            }
        }
        throw new IllegalArgumentException("Tipo de alvo inválido: " + value + ". Tipos permitidos: PING, TCP, HTTP");
    }
}
