# Roteiro de Teste — Selenium + Cucumber no Dev Spaces

Este documento descreve o passo a passo completo para validar a imagem `custom-udi-java21-selenium` e o devfile associado em um ambiente OpenShift Dev Spaces 3.28.0.

---

## 1. Build e push da imagem

### 1.1 Build local

```bash
cd dev-spaces-images/java21-selenium

podman-compose -f image/compose.yaml --env-file image/.env build
```

### 1.2 Validação local (Test)

```bash
podman run --rm quay.io/marolive/custom-udi-java21-selenium:1.0.0 bash -lc '
  echo "== Java =="; java -version;
  echo "== Maven =="; mvn -version | head -n 5;
  echo "== Chrome =="; chrome --version;
  echo "== Chromedriver =="; chromedriver --version;
  echo "== Library check (must be empty) ==";
  ldd /opt/chrome/current/chrome | grep "not found" || echo "OK: all libs resolved";
  echo "== Headless smoke test ==";
  chrome --headless=new --no-sandbox --disable-dev-shm-usage --disable-gpu --dump-dom about:blank | head -5;
'
```

**Critérios de sucesso:**
- `java -version` → OpenJDK 21
- `mvn -version` → Apache Maven 3.9.12
- `chrome --version` → Google Chrome for Testing 151.0.7922.170
- `chromedriver --version` → ChromeDriver 151.0.7922.170
- `ldd ... | grep "not found"` → saída vazia (todas as libs resolvidas)
- Headless smoke test → imprime `<html><head></head><body></body></html>`

### 1.3 Push para Quay.io

```bash
podman login quay.io

podman-compose -f image/compose.yaml --env-file image/.env push
```

---

## 2. Commit e push dos repositórios

```bash
# Repositório dev-spaces-images
cd dev-spaces-images
git add java21-selenium/
git commit -m "feat: add java21-selenium image (Chrome 151.0.7922.170 + Maven 3.9.12)"
git push origin main

# Repositório dev-spaces-devfiles
cd dev-spaces-devfiles
git add custom-udi-java21-selenium/
git commit -m "feat: add devfile + sample for Selenium/Cucumber testing"
git push origin main
```

---

## 3. Criação do workspace

Acesse o Dev Spaces usando a URL com apontamento para o devfile:

```
https://<devspaces-fqdn>#https://github.com/marcusviniciusaso/dev-spaces-devfiles?df=custom-udi-java21-selenium/devfile.yaml
```

Substitua `<devspaces-fqdn>` pelo FQDN da instância do Dev Spaces (ex: `devspaces.apps.cluster.example.com`).

O workspace será criado com:
- Imagem: `quay.io/marolive/custom-udi-java21-selenium:1.0.0`
- Memória: 6Gi (limit) / 3Gi (request)
- CPU: 2 cores (limit) / 1 core (request)

---

## 4. Instalação e validação das extensões

### 4.1 Instalar extensões

Ao abrir o workspace, o che-code deve sugerir as extensões de `.vscode/extensions.json`. Aceite a instalação ou instale manualmente pela view de Extensions:

- `redhat.java` — Language Support for Java
- `vscjava.vscode-java-debug` — Debugger for Java
- `vscjava.vscode-java-test` — Test Runner for Java
- `vscjava.vscode-maven` — Maven for Java
- `vscjava.vscode-java-dependency` — Project Manager for Java
- `CucumberOpen.cucumber-official` — Cucumber

> **Nota**: Se o cluster usa registro Open VSX embedded ou espelho interno, verifique que essas extensões estão publicadas lá. Consultar `spec.components.pluginRegistry.openVSXURL` no CheCluster.

### 4.2 Confirmar que o extension host NÃO crashou

1. Abra **Output** → selecionar **"Extension Host"** no dropdown
2. Verificar que NÃO há mensagens de "terminated unexpectedly"
3. No terminal ou via `oc`:

```bash
oc get events -n <namespace-do-usuario> --field-selector reason=OOMKilled
```

A saída deve estar vazia (sem eventos OOMKilled).

---

## 5. Execução do `validate-browser`

Execute a task do devfile para comprovar Chrome headless funcionando:

1. Pressione `Ctrl+Shift+P` → **Tasks: Run Task** → **devfile: Validate Chrome headless**
2. Ou pelo terminal:

```bash
chrome --version && chromedriver --version && chrome --headless=new --no-sandbox --disable-dev-shm-usage --dump-dom about:blank | head -1
```

**Resultado esperado:**
```
Google Chrome for Testing 151.0.7922.170
ChromeDriver 151.0.7922.170
<html><head></head><body></body></html>
```

---

## 6. Execução dos testes

Navegue até o diretório do sample:

```bash
cd /projects/custom-udi-java21-selenium/sample
```

### 6a. Via terminal / task `run-tests`

```bash
mvn test
```

Ou: `Ctrl+Shift+P` → **Tasks: Run Task** → **devfile: Run Selenium/Cucumber tests**

**Resultado esperado:** `BUILD SUCCESS`, todos os testes passam.

### 6b. Via Test Runner for Java (ícones de play)

1. Abra a view **Testing** (ícone de beaker na barra lateral)
2. Os testes `SmokeSeleniumTest` e `RunCucumberTest` devem aparecer
3. Clique no ícone de play (▶) para executar todos ou individualmente
4. Verifique check verde em cada teste

### 6c. Via extensão Cucumber

1. Abra o arquivo `smoke.feature`
2. Ícones de play (▶) devem aparecer ao lado de cada `Scenario`
3. Clique para executar; os steps devem ficar verdes
4. Autocomplete de steps Gherkin deve funcionar

---

## 7. Troubleshooting

### OOM do extension host

**Sintoma:** Mensagem "Remote Extension host terminated unexpectedly 3 times within the last 5 minutes" no Output.

**Correção:**
- Verificar se o devfile está com `memoryLimit: 6Gi` (não 2Gi)
- Para teste rápido, subir memória via query param na URL: `?memoryLimit=8Gi`
- Verificar eventos OOM: `oc get events -n <ns> --field-selector reason=OOMKilled`

### Crash do Chrome por /dev/shm

**Sintoma:** Chrome crash imediato ou "session not created" do Selenium.

**Correção:**
- Confirmar que `--disable-dev-shm-usage` está nas ChromeOptions
- Em pods OpenShift, `/dev/shm` é limitado a 64MB por padrão; a flag força Chrome a usar `/tmp`

### Biblioteca faltante

**Sintoma:** Chrome não inicia; erro de shared library.

**Correção:**
```bash
ldd /opt/chrome/current/chrome | grep "not found"
```
- Se houver libs faltantes, adicionar ao Containerfile e rebuildar a imagem
- Libs mais comuns: `nss`, `libdrm`, `mesa-libgbm`, `alsa-lib`, `at-spi2-atk`

### Chromedriver version mismatch

**Sintoma:** "session not created: This version of ChromeDriver only supports Chrome version X"

**Correção:**
- Confirmar que chrome e chromedriver são da mesma versão (par casado)
- Verificar: `chrome --version` e `chromedriver --version` devem retornar o mesmo número
