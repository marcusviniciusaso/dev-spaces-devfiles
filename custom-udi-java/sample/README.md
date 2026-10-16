# Hello World — Spring Boot 4.0 · Java 17+ · Swagger

[![Java](https://img.shields.io/badge/Java-17%20|%2021%20|%2025-black?style=flat&logo=openjdk&logoColor=%23FFFFFF)](https://openjdk.org/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-4.0.3-black?style=flat&logo=spring&logoColor=%236DB33F)](https://spring.io/projects/spring-boot)
[![Maven](https://img.shields.io/badge/Maven-3.9-black?style=flat&logo=apachemaven&logoColor=%23C71A36)](https://maven.apache.org/)
[![Swagger](https://img.shields.io/badge/Swagger-OpenAPI%203-black?style=flat&logo=swagger&logoColor=%2385EA2D)](https://swagger.io/)

---

Projeto de exemplo que demonstra uma API REST construída com **Spring Boot 4.0**, executada inteiramente no **Dev Spaces** com a imagem `custom-udi-java`. A aplicação expõe um endpoint `GET /api/hello` e documentação interativa via **Swagger UI**.

O projeto é compilado para **Java 17**, o mínimo do Spring Boot 4, e roda em **Java 17, 21 e 25**. Com o `devspaces-use-java` você executa a mesma aplicação em cada uma dessas versões.

Ao seguir este guia, você irá executar a aplicação no workspace, expor a porta para acesso externo e testar os endpoints pelo browser e via `curl`.

---
## Índice
- [Hello World — Spring Boot 4.0 · Java 17+ · Swagger](#hello-world--spring-boot-40--java-17--swagger)
  - [Índice](#índice)
  - [Estrutura do projeto](#estrutura-do-projeto)
  - [Quick Start](#quick-start)
    - [Pré-requisitos](#pré-requisitos)
    - [1 - Navegue até o projeto](#1---navegue-até-o-projeto)
    - [2 - Execute a aplicação](#2---execute-a-aplicação)
    - [3 - Acesse os endpoints](#3---acesse-os-endpoints)
      - [Expor a porta pela notificação do VS Code](#expor-a-porta-pela-notificação-do-vs-code)
      - [Opcional — Gerar as URLs pelo terminal](#opcional--gerar-as-urls-pelo-terminal)
    - [4 - Teste os endpoints com curl](#4---teste-os-endpoints-com-curl)
  - [Outras versões do Java](#outras-versões-do-java)
  - [Configurações avançadas (opcional)](#configurações-avançadas-opcional)
    - [Executando como JAR](#executando-como-jar)
    - [Configuração de proxy / ingress](#configuração-de-proxy--ingress)

---

## Estrutura do projeto

```
sample/
├── pom.xml
└── src/
    └── main/
        ├── java/com/example/helloworld/
        │   ├── Application.java         # entry point
        │   ├── HelloController.java     # GET /api/hello
        │   ├── HelloResponse.java       # DTO de resposta
        │   └── OpenApiConfig.java       # configuração Swagger
        └── resources/
            └── application.properties   # porta e paths
```

---

## Quick Start

### Pré-requisitos

- Workspace **OpenShift Dev Spaces** criado a partir do devfile `custom-udi-java`, com Java 17, 21 e 25, Spring Boot CLI 4.0 e Maven 3.9 pré-instalados.
- Acesso do workspace ao Maven Central, de onde as dependências são baixadas.

### 1 - Navegue até o projeto

Abra o terminal integrado (`Ctrl + J`) e execute:

```bash
cd /projects/dev-spaces-devfiles/custom-udi-java/sample
```

### 2 - Execute a aplicação

```bash
mvn spring-boot:run
```

Na primeira execução o Maven baixa as dependências do Maven Central. A aplicação roda na versão ativa do Java (25, a padrão do workspace) e estará disponível via URL gerada pelo Dev Spaces (não `localhost`).

### 3 - Acesse os endpoints

No Dev Spaces, cada porta exposta recebe uma URL única por workspace.

#### Expor a porta pela notificação do VS Code

- O VS Code exibe uma notificação no canto inferior direito (🔔): *"A new process is now listening on port 8080 but this port is not a current endpoint. Would you want to add a redirect for this port so it becomes available?"*. Clique em **"yes"**.
- Uma segunda notificação confirma que o redirect foi habilitado e exibe a URL pública do seu workspace: *"Redirect is now enabled on port 8080. External URL is https://..."*. Clique em **"Open In New Tab"**.
- O VS Code solicita confirmação para abrir o site externo. Clique em **"Open"**.

A URL criada segue o padrão `https://<workspace-id>-code-redirect-3.<domínio>`. Adicione o caminho desejado:

| Recurso | Caminho |
|---|---|
| Swagger UI | `/swagger-ui/index.html` |
| Hello World | `/api/hello` |
| OpenAPI JSON | `/v3/api-docs` |

#### Opcional — Gerar as URLs pelo terminal

> ⚠️ **Pré-requisito:** exponha a porta pela notificação do VS Code antes de usar os comandos abaixo.

Gere as URLs de acesso executando no terminal do workspace:

  ```bash
  DEVSPACES_DOMAIN=$(echo $CHE_DASHBOARD_URL | sed 's|https://||')
  echo "Swagger UI  : https://${DEVWORKSPACE_ID}-code-redirect-3.${DEVSPACES_DOMAIN}/swagger-ui/index.html"
  echo "Hello World : https://${DEVWORKSPACE_ID}-code-redirect-3.${DEVSPACES_DOMAIN}/api/hello"
  echo "OpenAPI JSON: https://${DEVWORKSPACE_ID}-code-redirect-3.${DEVSPACES_DOMAIN}/v3/api-docs"
  ```

### 4 - Teste os endpoints com curl

No terminal do workspace, teste os endpoints diretamente via `localhost:8080`:

```bash
# Hello World — retorna a mensagem JSON
curl http://localhost:8080/api/hello

# OpenAPI JSON — especificação completa da API
curl http://localhost:8080/v3/api-docs

# Swagger UI — página HTML de documentação interativa
curl http://localhost:8080/swagger-ui/index.html
```

Resposta esperada do endpoint `/api/hello`:

```json
{
  "message": "Hello, World!",
  "application": "hello-world",
  "version": "v1",
  "timestamp": "2026-10-15T21:08:40"
}
```

> ℹ️ `localhost:8080` funciona apenas **dentro** do terminal do workspace. Para acesso pelo **browser**, use a URL pública `https://<workspace-id>-code-redirect-3.<domínio>` exposta pelo Dev Spaces nos passos anteriores.

---

## Outras versões do Java

Pare a aplicação (`Ctrl + C`) e execute-a em outra versão:

```bash
# Troca a versão ativa do terminal (fica salva para os próximos terminais)
devspaces-use-java 17
mvn spring-boot:run

# Ou roda só este comando em outra versão, sem mudar o terminal
devspaces-with-java 21 mvn spring-boot:run
```

A linha `Starting Application ... using Java <versão>` do log mostra a versão em uso.

> O Spring Boot 4 exige Java 17 ou superior: em **Java 8 e 11** o build falha. No Java 11 o erro é `release version 17 not supported`; no Java 8, a compilação para em erros como `class, interface, or enum expected`. Projetos em Java 8 ou 11 precisam do Spring Boot 2.7.

---

## Configurações avançadas (opcional)

### Executando como JAR

Para gerar e executar o artefato empacotado:

  ```bash
  # Compilar e empacotar
  mvn clean package

  # Executar o JAR
  java -jar target/hello-spring-0.0.1-SNAPSHOT.jar
  ```

---

### Configuração de proxy / ingress

Se o ambiente estiver atrás de um proxy reverso ou ingress com path base, adicione ao `application.properties`:

  ```properties
  server.forward-headers-strategy=framework
  springdoc.swagger-ui.path=/swagger-ui
  ```
