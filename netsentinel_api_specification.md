x# NetSentinel — API Specification (`routes.md`)

Esta documentação descreve todos os endpoints REST disponibilizados pela aplicação **NetSentinel**, detalhando contratos de entrada e saída, parâmetros de consulta, códigos de status HTTP e exemplos funcionais de payloads.

---

## Sumário

- [1. Módulo de Gestão de Alvos (`/api/v1/targets`)](#1-módulo-de-gestão-de-alvos-apiv1targets)
  - [GET /api/v1/targets](#get-apiv1targets)
  - [POST /api/v1/targets](#post-apiv1targets)
  - [GET /api/v1/targets/{id}](#get-apiv1targetsid)
  - [PUT /api/v1/targets/{id}](#put-apiv1targetsid)
  - [DELETE /api/v1/targets/{id}](#delete-apiv1targetsid)
- [2. Módulo de Diagnóstico e Status (`/api/v1/checks`)](#2-módulo-de-diagnóstico-e-status-apiv1checks)
  - [GET /api/v1/checks/status](#get-apiv1checksstatus)
  - [GET /api/v1/checks/{targetId}/history](#get-apiv1checkstargetidhistory)
  - [POST /api/v1/checks/{targetId}/execute](#post-apiv1checkstargetidexecute)
- [3. Módulo de Observabilidade e Sistema (`/actuator` / `/metrics`)](#3-módulo-de-observabilidade-e-sistema-actuator--metrics)
  - [GET /actuator/health](#get-actuatorhealth)
  - [GET /metrics](#get-metrics)

---

## 1. Módulo de Gestão de Alvos (`/api/v1/targets`)

Responsável pelo ciclo de vida dos ativos (serviços e hosts) que o NetSentinel monitora periodicamente.

---

### `GET /api/v1/targets`

Retorna a lista completa de alvos cadastrados no sistema.

* **Método HTTP:** `GET`
* **URL:** `/api/v1/targets`
* **Parâmetros de Consulta (Query Parameters):**
  * `enabled` *(boolean, opcional)*: Filtra apenas os alvos ativos (`true`) ou inativos (`false`).
  * `type` *(string, opcional)*: Filtra pelo protocolo de checagem (`PING`, `TCP`, `HTTP`).

#### Respostas HTTP

* **`200 OK`**: Retornada com sucesso contendo a lista de alvos.

#### Exemplo de Resposta (`200 OK`):

```json
[
  {
    "id": "srv-db-01",
    "name": "PostgreSQL Primary",
    "type": "TCP",
    "host": "192.168.1.50",
    "port": 5432,
    "endpointPath": null,
    "expectedStatusCode": null,
    "intervalSeconds": 30,
    "timeoutMs": 2000,
    "enabled": true,
    "createdAt": "2026-10-10T02:00:00Z"
  },
  {
    "id": "api-gateway",
    "name": "API Gateway Produção",
    "type": "HTTP",
    "host": "api.empresa.internal",
    "port": 443,
    "endpointPath": "/health",
    "expectedStatusCode": 200,
    "intervalSeconds": 10,
    "timeoutMs": 3000,
    "enabled": true,
    "createdAt": "2026-10-10T02:05:00Z"
  }
]
```

---

### `POST /api/v1/targets`

Cadastra um novo alvo de monitoramento e o insere dinamicamente no motor de agendamento em segundo plano (`ScheduledExecutorService`).

* **Método HTTP:** `POST`
* **URL:** `/api/v1/targets`
* **Cabeçalhos Exigidos:** `Content-Type: application/json`

#### Payload de Requisição (Exemplo - Alvo TCP):

```json
{
  "name": "Redis Cache Server",
  "type": "TCP",
  "host": "10.0.0.15",
  "port": 6379,
  "intervalSeconds": 15,
  "timeoutMs": 1000
}
```

#### Payload de Requisição (Exemplo - Alvo HTTP):

```json
{
  "name": "Serviço de Autenticação",
  "type": "HTTP",
  "host": "auth.empresa.internal",
  "port": 8080,
  "endpointPath": "/actuator/health",
  "expectedStatusCode": 200,
  "intervalSeconds": 20,
  "timeoutMs": 2500
}
```

#### Respostas HTTP

* **`201 Created`**: Alvo cadastrado e agendado com sucesso.
* **`400 Bad Request`**: Estrutura JSON inválida ou campos obrigatórios ausentes.

#### Exemplo de Resposta (`201 Created`):

```json
{
  "id": "target-9f8e7d",
  "name": "Redis Cache Server",
  "type": "TCP",
  "host": "10.0.0.15",
  "port": 6379,
  "endpointPath": null,
  "expectedStatusCode": null,
  "intervalSeconds": 15,
  "timeoutMs": 1000,
  "enabled": true,
  "createdAt": "2026-10-10T02:45:00Z"
}
```

---

### `GET /api/v1/targets/{id}`

Busca as configurações detalhadas de um alvo específico através do seu ID.

* **Método HTTP:** `GET`
* **URL:** `/api/v1/targets/{id}`
* **Parâmetros de Path:**
  * `id` *(string, obrigatório)*: Identificador único do alvo (ex.: `srv-db-01`).

#### Respostas HTTP

* **`200 OK`**: Registro encontrado.
* **`404 Not Found`**: NENHUM alvo cadastrado com o ID especificado.

#### Exemplo de Resposta (`200 OK`):

```json
{
  "id": "srv-db-01",
  "name": "PostgreSQL Primary",
  "type": "TCP",
  "host": "192.168.1.50",
  "port": 5432,
  "intervalSeconds": 30,
  "timeoutMs": 2000,
  "enabled": true,
  "createdAt": "2026-10-10T02:00:00Z"
}
```

---

### `PUT /api/v1/targets/{id}`

Atualiza os parâmetros de um alvo existente. Se o atributo `enabled` for alterado para `false`, as rotinas periódicas de checagem do alvo são imediatamente pausadas.

* **Método HTTP:** `PUT`
* **URL:** `/api/v1/targets/{id}`
* **Cabeçalhos Exigidos:** `Content-Type: application/json`

#### Payload de Requisição:

```json
{
  "name": "PostgreSQL Secondary (Failover)",
  "type": "TCP",
  "host": "192.168.1.51",
  "port": 5432,
  "intervalSeconds": 10,
  "timeoutMs": 1500,
  "enabled": true
}
```

#### Respostas HTTP

* **`200 OK`**: Registro atualizado com sucesso.
* **`400 Bad Request`**: Dados da requisição inconsistentes.
* **`404 Not Found`**: Alvo não localizado para atualização.

---

### `DELETE /api/v1/targets/{id}`

Remove o alvo do cadastro e cancela todas as checagens periódicas agendadas para ele.

* **Método HTTP:** `DELETE`
* **URL:** `/api/v1/targets/{id}`

#### Respostas HTTP

* **`204 No Content`**: Removido com sucesso (sem corpo de resposta).
* **`404 Not Found`**: Alvo não encontrado.

---

## 2. Módulo de Diagnóstico e Status (`/api/v1/checks`)

Disponibiliza a visualização do estado da rede e permite acionar varreduras manuais e pontuais sob demanda.

---

### `GET /api/v1/checks/status`

Retorna a última fotografia de estado conhecida (*last known state*) de todos os alvos cadastrados no sistema.

* **Método HTTP:** `GET`
* **URL:** `/api/v1/checks/status`

#### Respostas HTTP

* **`200 OK`**: Relatório de status consolidado gerado com sucesso.

#### Exemplo de Resposta (`200 OK`):

```json
[
  {
    "targetId": "srv-db-01",
    "targetName": "PostgreSQL Primary",
    "status": "UP",
    "latencyMs": 3,
    "details": {
      "socketConnected": true,
      "resolvedIp": "192.168.1.50"
    },
    "lastCheckedAt": "2026-10-10T02:41:30Z"
  },
  {
    "targetId": "api-gateway",
    "targetName": "API Gateway Produção",
    "status": "DOWN",
    "latencyMs": 3001,
    "details": {
      "errorType": "TIMEOUT",
      "errorMessage": "Connection timed out after 3000ms while connecting to api.empresa.internal:443"
    },
    "lastCheckedAt": "2026-10-10T02:41:35Z"
  }
]
```

---

### `GET /api/v1/checks/{targetId}/history`

Consulta a série temporal de checagens realizadas para um alvo específico.

* **Método HTTP:** `GET`
* **URL:** `/api/v1/checks/{targetId}/history`
* **Parâmetros de Query:**
  * `limit` *(integer, opcional, padrão: 20)*: Quantidade máxima de registros no retorno.
  * `status` *(string, opcional)*: Filtra o histórico por `UP` ou `DOWN`.

#### Respostas HTTP

* **`200 OK`**: Histórico retornado com sucesso.
* **`404 Not Found`**: Alvo não encontrado.

#### Exemplo de Resposta (`200 OK`):

```json
{
  "targetId": "srv-db-01",
  "totalRecords": 3,
  "history": [
    {
      "timestamp": "2026-10-10T02:41:30Z",
      "status": "UP",
      "latencyMs": 3,
      "errorMessage": null
    },
    {
      "timestamp": "2026-10-10T02:41:00Z",
      "status": "UP",
      "latencyMs": 4,
      "errorMessage": null
    },
    {
      "timestamp": "2026-10-10T02:40:30Z",
      "status": "DOWN",
      "latencyMs": 2000,
      "errorMessage": "Connection refused"
    }
  ]
}
```

---

### `POST /api/v1/checks/{targetId}/execute`

Executa um teste de conectividade de rede **síncrono e imediato** no alvo informado (*ad-hoc ping*), sem afetar a agenda regular de varreduras.

* **Método HTTP:** `POST`
* **URL:** `/api/v1/checks/{targetId}/execute`

#### Respostas HTTP

* **`200 OK`**: Teste executado e resultado retornado.
* **`404 Not Found`**: Alvo especificado não cadastrado.
* **`504 Gateway Timeout`**: A tentativa de conexão excedeu o tempo limite máximo de segurança da API.

#### Exemplo de Resposta (`200 OK`):

```json
{
  "targetId": "srv-db-01",
  "executedAt": "2026-10-10T02:42:00Z",
  "status": "UP",
  "latencyMs": 2,
  "httpStatusCode": null,
  "errorMessage": null
}
```

---

## 3. Módulo de Observabilidade e Sistema (`/actuator` / `/metrics`)

Endpoints projetados para integração direta com Docker, Kubernetes e coletores de métricas como o Prometheus.

---

### `GET /actuator/health`

Exibe a integridade e saúde do processo interno do **NetSentinel** (gerenciador de threads e armazenamento).

* **Método HTTP:** `GET`
* **URL:** `/actuator/health`

#### Respostas HTTP

* **`200 OK`**: Aplicação saudável e em operação normal.
* **`503 Service Unavailable`**: A aplicação sofreu falha grave e necessita de intervenção do orquestrador.

#### Exemplo de Resposta (`200 OK`):

```json
{
  "status": "UP",
  "components": {
    "scheduler": {
      "status": "UP",
      "details": {
        "activeThreads": 4,
        "scheduledTasks": 12
      }
    },
    "diskSpace": {
      "status": "UP",
      "details": {
        "freeBytes": 10737418240
      }
    }
  }
}
```

---

### `GET /metrics`

Expõe métricas operacionais e de diagnóstico de rede formatadas segundo a especificação **OpenMetrics / Prometheus**.

* **Método HTTP:** `GET`
* **URL:** `/metrics`
* **Content-Type:** `text/plain; version=0.0.4`

#### Exemplo de Resposta (`200 OK`):

```text
# HELP netsentinel_target_up Status do alvo monitorado (1 para UP, 0 para DOWN)
# TYPE netsentinel_target_up gauge
netsentinel_target_up{target_id="srv-db-01",type="TCP"} 1.0
netsentinel_target_up{target_id="api-gateway",type="HTTP"} 0.0

# HELP netsentinel_check_latency_seconds Tempo de resposta do teste de rede em segundos
# TYPE netsentinel_check_latency_seconds summary
netsentinel_check_latency_seconds{target_id="srv-db-01",type="TCP"} 0.003
netsentinel_check_latency_seconds{target_id="api-gateway",type="HTTP"} 3.001
```