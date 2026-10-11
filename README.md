# NetSentinel 🛡️

O **NetSentinel** é um serviço backend desenvolvido em Java com **Spring Boot** focado em monitoramento, coleta de métricas e verificação de saúde de aplicações e redes.

---

## 🚀 Tecnologias Utilizadas

- **Java 17** (LTS)
- **Spring Boot 3.3.4**
    - Spring Web (REST API)
- **Maven** (Gerenciador de dependências e build)

---

## 📋 Pré-requisitos

Antes de iniciar, certifique-se de ter instalado em sua máquina:
- [Java Development Kit (JDK) 17](https://www.oracle.com/java/technologies/downloads/#java17)
- [Git](https://git-scm.com/)

---

## 🔧 Como Executar o Projeto

1. **Clonar o repositório:**
   ```bash
   git clone https://github.com/SEU_USUARIO/netsentinel.git
   cd netsentinel
   ```

2. **Executar a aplicação usando o Maven Wrapper:**

    - **Windows (PowerShell / CMD):**
      ```cmd
      .\mvnw.cmd spring-boot:run
      ```
    - **Linux / macOS:**
      ```bash
      ./mvnw spring-boot:run
      ```

3. **Acessar o serviço:**

   A API estará ativa em: `http://localhost:8080`

---

## 🛣️ Endpoints da API
* `Gestão de Alvos (/api/v1/targets)` - Endpoints para cadastrar, listar, atualizar e remover os serviços/hosts que o NetSentinel deve vigiar.
* `Diagnóstico e Status (/api/v1/checks)`-  Endpoints para consultar o estado atual da rede e o histórico recente de execuções.
* `Observabilidade e Sistema (/actuator / /health)`-  Endpoints essenciais para integração com ecossistemas de infraestrutura (Docker, Kubernetes e Prometheus).

| Método   | Endpoint                            | Descrição                                                                                                                                                                                  | Contexto                  |
|:---------|:------------------------------------|:-------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|:--------------------------|
| `GET`    | `/`                                 | Retorna o status de execução da API NetSentinel                                                                                                                                            |                           |
| `GET`    | `/api/v1/targets`                   | Retorna a lista de todos os alvos configurados para monitoramento.                                                                                                                         | Gestão de Alvos           |
| `POST`   | `/api/v1/targets`                   | Cadastra um novo alvo de monitoramento em tempo de execução sem precisar reiniciar a aplicação.                                                                                            | Gestão de Alvos           |
| `GET`    | `/api/v1/targets/{id}`              | Retorna os detalhes de configuração de um alvo específico.                                                                                                                                 | Gestão de Alvos           |
| `PUT`    | `/api/v1/targets/{id}`              | Atualiza os parâmetros de um alvo existente (ex.: alterar o intervalo de checagem ou desativar temporariamente com `"enabled": false`).                                                    | Gestão de Alvos           |
| `DELETE` | `/api/v1/targets/{id}`              | Remove o alvo do ciclo de monitoramento do `ScheduledExecutorService`.                                                                                                                     | Gestão de Alvos           |
| `GET`    | `/api/v1/checks/status`             | Retorna o último estado conhecido de todos os serviços monitorados (visão geral do dashboard).                                                                                             | Diagnóstico e Status      |
| `GET`    | `/api/v1/checks/{targetId}/history` | Retorna o histórico de varreduras de um alvo específico para análise de oscilação ou latência média (aceita parâmetros de consulta como `?limit=50` ou `?since=1h`).                       | Diagnóstico e Status      |
| `POST`   | `/api/v1/checks/{targetId}/execute` | Dispara uma checagem imediata/síncrona sob demanda (ad-hoc ping) para aquele alvo, ignorando o timer agendado. Útil para testar se um serviço voltou a funcionar logo após uma manutenção. | Diagnóstico e Status      |
| `GET`    | `/health/liveness`                  | Indica se o processo Java do NetSentinel está rodando e saudável. Usado pelo Docker/Kubernetes para saber se o container precisa ser reiniciado.                                           | Observabilidade e Sistema |
| `GET`    | `/health/readiness`                 | Confirma se a aplicação Java carregou todas as configurações e está pronta para executar as varreduras de rede.                                                                            | Observabilidade e Sistema |
| `GET`    | `/metrics`                          | Expõe métricas de rede e do sistema no formato padrão do Prometheus.                                                                                                                       | Observabilidade e Sistema |

---

## ⚙️ Exemplo de uso

Subir a aplicação localmente com:
`docker compose up -d`

Cadastrar os alvos usando o DNS interno do Docker (`test-nginx`, `test-postgres`, `test-redis`) na API do NetSentinel:

1. Cadastrar Alvo HTTP (Nginx)
```Bash
   curl -X POST http://localhost:8080/api/v1/targets \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Nginx Webserver Test",
    "type": "HTTP",
    "host": "test-nginx",
    "port": 80,
    "endpointPath": "/",
    "expectedStatusCode": 200,
    "intervalSeconds": 10,
    "timeoutMs": 2000
  }'
```

2. Cadastrar Alvo TCP (PostgreSQL)
```Bash
curl -X POST http://localhost:8080/api/v1/targets \
  -H "Content-Type: application/json" \
  -d '{
    "name": "PostgreSQL DB Test",
    "type": "TCP",
    "host": "test-postgres",
    "port": 5432,
    "intervalSeconds": 15,
    "timeoutMs": 1000
  }'
```

3. Cadastrar Alvo TCP (Redis)
```Bash
curl -X POST http://localhost:8080/api/v1/targets \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Redis Cache Test",
    "type": "TCP",
    "host": "test-redis",
    "port": 6379,
    "intervalSeconds": 15,
    "timeoutMs": 1000
  }'
```
---

## 📑 Documentação do Projeto

- 🚀 **Apresentação do Projeto (Slides):** [Clique para visualizar a Apresentação Interativa](https://breno12filipe.github.io/netsentinel/docs/netsentinel_presentation.html)
- 📖 **Especificação da API REST:** [`docs/api_specification.md`](docs/routes.md)
- 📐 **Diagrama de Arquitetura:** [`docs/architecture.puml`](docs/architecture.puml)

---

## 🛠️ Próximos Passos & Roadmap

- [x] Estruturação de pacotes por camadas (`controller`, `service`, `dto`, `model`)
- [X] Configurar Swagger/OpenAPI no pacote config para documentar a API
- [ ] Conexão com banco de dados via Spring Data JPA
- [ ] Endpoint de verificação de conectividade (Ping / Healthcheck)

---
*Desenvolvido como projeto de aprendizado em arquitetura de APIs REST com Spring Boot.*