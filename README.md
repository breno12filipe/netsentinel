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

| Método | Endpoint | Descrição |
| :--- | :--- | :--- |
| `GET` | `/` | Retorna o status de execução da API NetSentinel |
| `GET` | `/api/hello` | Endpoint de teste inicial da aplicação |

---

## 🛠️ Próximos Passos & Roadmap

- [ ] Estruturação de pacotes por camadas (`controller`, `service`, `dto`, `model`)
- [ ] Conexão com banco de dados via Spring Data JPA
- [ ] Endpoint de verificação de conectividade (Ping / Healthcheck)

---
*Desenvolvido como projeto de aprendizado em arquitetura de APIs REST com Spring Boot.*