# Roteiro de Validação — Selenium + Cucumber no Dev Spaces

Este documento descreve o passo a passo para validar as extensões, o Chrome headless e os testes Selenium/Cucumber dentro de um workspace OpenShift Dev Spaces 3.28.0 usando a imagem `custom-udi-java21-selenium`.

---

## 1. Instalação e validação das extensões

### 1.1 Instalar extensões

Ao abrir o workspace, o che-code deve sugerir as extensões de `.vscode/extensions.json`. Aceite a instalação ou instale manualmente pela view de Extensions:

- `redhat.java` — Language Support for Java
- `vscjava.vscode-java-debug` — Debugger for Java
- `vscjava.vscode-java-test` — Test Runner for Java
- `vscjava.vscode-maven` — Maven for Java
- `vscjava.vscode-java-dependency` — Project Manager for Java
- `CucumberOpen.cucumber-official` — Cucumber

> **Nota**: Se o cluster usa registro Open VSX embedded ou espelho interno, verifique que essas extensões estão publicadas lá. Consultar `spec.components.pluginRegistry.openVSXURL` no CheCluster.

### 1.2 Confirmar que o extension host NÃO crashou

1. Abra **Output** → selecionar **"Extension Host"** no dropdown
2. Verificar que NÃO há mensagens de "terminated unexpectedly"
3. No terminal ou via `oc`:

```bash
oc get events -n <namespace-do-usuario> --field-selector reason=OOMKilled
```

A saída deve estar vazia (sem eventos OOMKilled).

---

## 2. Execução do `validate-browser`

Execute a task do devfile para comprovar Chrome headless funcionando:

1. Pressione `Ctrl+Shift+P` → **Tasks: Run Task** → **devfile: Validate Chrome headless**
2. Ou pelo terminal:

```bash
chrome --version && chromedriver --version && chrome --headless=new --no-sandbox --disable-dev-shm-usage --disable-gpu --dump-dom about:blank 2>/dev/null | head -1
```

**Resultado esperado:**
```
Google Chrome for Testing 151.0.7922.170
ChromeDriver 151.0.7922.170
<html><head></head><body></body></html>
```

---

## 3. Execução dos testes

Navegue até o diretório do sample:

```bash
cd /projects/custom-udi-java21-selenium/sample
```

### 3a. Via terminal / task `run-tests`

```bash
mvn test
```

Ou: `Ctrl+Shift+P` → **Tasks: Run Task** → **devfile: Run Selenium/Cucumber tests**

**Resultado esperado:** `BUILD SUCCESS`, todos os testes passam.

### 3b. Via Test Runner for Java (ícones de play)

1. Abra a view **Testing** (ícone de beaker na barra lateral)
2. Os testes `SmokeSeleniumTest` e `RunCucumberTest` devem aparecer
3. Clique no ícone de play (▶) para executar todos ou individualmente
4. Verifique check verde em cada teste

### 3c. Via extensão Cucumber

1. Abra o arquivo `smoke.feature`
2. Ícones de play (▶) devem aparecer ao lado de cada `Scenario`
3. Clique para executar; os steps devem ficar verdes
4. Autocomplete de steps Gherkin deve funcionar

---

## 4. Troubleshooting

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
