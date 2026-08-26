# Roteiro de Validação — QA (Selenium + Cucumber + JMeter) no Dev Spaces

Este documento descreve o passo a passo para validar as extensões, os cinco JDKs, o Chrome headless,
os testes Selenium/Cucumber e o Apache JMeter dentro de um workspace OpenShift Dev Spaces 3.28.0
usando a imagem `custom-udi-qa`.

> Build e push da imagem não fazem parte deste roteiro — estão no `README.md` da imagem, em
> `dev-spaces-images/qa/`.

---

## 1. Abrir o workspace

```
https://<devspaces-fqdn>#https://github.com/marcusviniciusaso/dev-spaces-devfiles?df=custom-udi-qa/devfile.yaml
```

O repositório é clonado em `/projects/dev-spaces-devfiles`; o sample fica em
`/projects/dev-spaces-devfiles/custom-udi-qa/sample` (é para onde `${PROJECT_SOURCE}/custom-udi-qa/sample`
aponta nas tasks do devfile).

---

## 2. Instalação e validação das extensões

### 2.1 Instalar extensões

Ao abrir o workspace, o che-code deve sugerir as extensões de `sample/.vscode/extensions.json`.
Aceite a instalação ou instale manualmente pela view de Extensions:

- `redhat.java` — Language Support for Java
- `vscjava.vscode-java-debug` — Debugger for Java
- `vscjava.vscode-java-test` — Test Runner for Java
- `vscjava.vscode-maven` — Maven for Java
- `vscjava.vscode-java-dependency` — Project Manager for Java
- `CucumberOpen.cucumber-official` — Cucumber
- `redhat.vscode-xml` — XML (edição dos planos `.jmx`)

> **Nota**: Se o cluster usa registro Open VSX embedded ou espelho interno, verifique que essas
> extensões estão publicadas lá. Consultar `spec.components.pluginRegistry.openVSXURL` no CheCluster.
> Não usar o Marketplace da Microsoft nem `.vsix` de lá.

### 2.2 Confirmar que o extension host NÃO crashou

1. Abra **Output** → selecionar **"Extension Host"** no dropdown
2. Verificar que NÃO há mensagens de "terminated unexpectedly"
3. No terminal ou via `oc`:

```bash
oc get events -n <namespace-do-usuario> --field-selector reason=OOMKilled
```

A saída deve estar vazia (sem eventos OOMKilled).

---

## 3. Validação dos JDKs

Execute a task do devfile: `Ctrl+Shift+P` → **Tasks: Run Task** → **devfile: Validate JDKs (8/11/17/21/25)**.

Ou pelo terminal:

```bash
for v in 8 11 17 21 25; do echo "-- Java $v --"; use-java $v; done
```

**Resultado esperado:**
```
-- Java 8 --   openjdk version "1.8.0_492"
-- Java 11 --  openjdk version "11.0.25" 2024-10-15 LTS
-- Java 17 --  openjdk version "17.0.19" 2026-04-21 LTS
-- Java 21 --  openjdk version "21.0.11" 2026-04-21 LTS
-- Java 25 --  openjdk version "25.0.4.1" 2026-08-18 LTS
```

Um terminal novo sempre volta ao **default Java 21**:

```bash
java -version   # 21.0.11
```

### 3.1 Executar um comando pontual em outro JDK

`use-java` só altera o shell corrente. Para um comando isolado, sem trocar o shell:

```bash
with-java 8  mvn -version
with-java 11 mvn test
```

> **Atenção**: o **Selenium 4.x exige JVM 11+**. Uma aplicação legada pode ser compilada para 8
> (`--release 8`), mas os testes Selenium precisam rodar em 11+ (ideal: 21).

### 3.2 Runtimes na extensão Java

Abra qualquer `.java` do sample e confira em **Java: Configure Java Runtime** (Command Palette) que
as cinco runtimes aparecem, com **JavaSE-21 como default**. Elas vêm de `sample/.vscode/settings.json`.

---

## 4. Teste de frontend — Selenium + Cucumber

### 4.1 Execução do `validate-browser`

Execute a task do devfile para comprovar Chrome headless funcionando:

1. Pressione `Ctrl+Shift+P` → **Tasks: Run Task** → **devfile: Validate Chrome headless**
2. Ou pelo terminal:

```bash
chrome --version && chromedriver --version && chrome --headless=new --no-sandbox --disable-dev-shm-usage --disable-gpu --dump-dom about:blank 2>/dev/null | head -1
```

**Resultado esperado:**
```
Google Chrome for Testing 152.0.7977.64
ChromeDriver 152.0.7977.64
<html><head></head><body></body></html>
```

### 4.2 Execução dos testes

Navegue até o diretório do sample:

```bash
cd ${PROJECT_SOURCE}/custom-udi-qa/sample
```

#### 4.2a Via terminal / task `run-tests`

```bash
mvn test
```

Ou: `Ctrl+Shift+P` → **Tasks: Run Task** → **devfile: Run Selenium/Cucumber tests**

**Resultado esperado:** `BUILD SUCCESS`, todos os testes passam.

#### 4.2b Via Test Runner for Java (ícones de play)

1. Abra a view **Testing** (ícone de beaker na barra lateral)
2. Os testes `SmokeSeleniumTest` e `CucumberRunnerTest` devem aparecer
3. Clique no ícone de play (▶) para executar todos ou individualmente
4. Verifique check verde em cada teste

#### 4.2c Via extensão Cucumber

1. Abra o arquivo `smoke.feature`
2. Ícones de play (▶) devem aparecer ao lado de cada `Scenario`
3. Clique para executar; os steps devem ficar verdes
4. Autocomplete de steps Gherkin deve funcionar

---

## 5. Teste de performance — JMeter

### 5.1 Execução do `validate-jmeter`

`Ctrl+Shift+P` → **Tasks: Run Task** → **devfile: Validate JMeter (non-GUI, sem egress)**

A task sobe um `jwebserver` local servindo `sample/src/test/resources`, roda `jmeter/smoke-plan.jmx`
e derruba o servidor.

**Resultado esperado:**
```
[jmeter] JAVA_HOME=/usr/lib/jvm/java-21-openjdk
summary =     10 in 00:00:02 =    4.9/s Avg:    89 Min:     6 Max:   411 Err:     0 (0.00%)
```
E um `.jtl` com `success=true` (coluna 8) em todas as 10 linhas.

Equivalente pelo terminal:

```bash
cd ${PROJECT_SOURCE}/custom-udi-qa/sample
with-java 21 jwebserver -p 8000 -b 127.0.0.1 -d "$PWD/src/test/resources" &
rm -f /tmp/jmeter-result.jtl
jmeter -n -t jmeter/smoke-plan.jmx -l /tmp/jmeter-result.jtl -j /tmp/jmeter.log
kill %1
```

### 5.2 Confirmar que o JMeter roda no JDK 21

O Groovy embarcado no JMeter 5.6.3 quebra em JDK 22+ e em Java 25. Por isso `/usr/local/bin/jmeter`
é um **wrapper que fixa o JDK 21**, independentemente do `use-java` ativo. Para comprovar:

```bash
use-java 25                       # shell passa a rodar Java 25
java -version                     # openjdk version "25.0.4.1"
JMETER_SHOW_JVM=true jmeter -v    # [jmeter] JAVA_HOME=/usr/lib/jvm/java-21-openjdk
```

### 5.3 Relatório HTML

```bash
rm -rf /tmp/jmeter-result.jtl /tmp/jmeter-report
jmeter -n -t jmeter/smoke-plan.jmx -l /tmp/jmeter-result.jtl -e -o /tmp/jmeter-report -j /tmp/jmeter.log
```

O relatório fica em `/tmp/jmeter-report/index.html`. `-l` e `-o` falham se o arquivo/diretório já
existir — sempre limpe antes.

### 5.4 Edição de planos

A **GUI do JMeter não roda no workspace** (container headless, sem X11). Edite o XML do `.jmx`
direto no che-code (a extensão `redhat.vscode-xml` dá validação e formatação), ou autore o plano na
GUI da máquina local do QA e versione o `.jmx` aqui.

> **Não gere carga real de dentro do workspace.** Com `cpuLimit: 2` e `memoryLimit: 6Gi`, os números
> saem distorcidos pelos limites do pod. O workspace serve para desenvolver, versionar e rodar smoke
> tests dos planos.

Detalhes adicionais em `sample/jmeter/README.md`.

---

## 6. Troubleshooting

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

### Selenium tentando baixar driver da internet

**Sintoma:** Timeout ou erro do Selenium Manager tentando alcançar `googlechromelabs.github.io`.

**Correção:**
- O Selenium Manager (4.6+) só baixa driver quando não encontra um configurado
- Garantir que `webdriver.chrome.driver` está setado (o `WebDriverFactory` do sample lê a env
  `WEBDRIVER_CHROME_DRIVER`, definida no devfile e na imagem)

### JMeter rodando no Java errado

**Sintoma:** `NoClassDefFoundError` / erros de Groovy ao executar plano com JSR223, especialmente
depois de um `use-java 25`.

**Correção:**
- Chamar sempre `jmeter` (o wrapper em `/usr/local/bin`), nunca `/opt/jmeter/current/bin/jmeter`
- Confirmar a JVM: `JMETER_SHOW_JVM=true jmeter -v` deve mostrar `java-21-openjdk`
- Para forçar outra JVM conscientemente: `JMETER_JAVA_HOME=/usr/lib/jvm/java-17-openjdk jmeter ...`

### `OutOfMemoryError` no JMeter

**Sintoma:** o plano aborta com heap exhausted.

**Correção:**
- O heap default do wrapper é `-Xms256m -Xmx1g`. Aumentar: `HEAP="-Xms512m -Xmx2g" jmeter -n -t ...`
- Não passar do `memoryLimit` do pod (6Gi), lembrando que o Java LS e o Chrome também consomem

### Relatórios JMeter grandes

**Sintoma:** `/projects` poluído, `git status` com centenas de arquivos, ou disco cheio.

**Correção:**
- Gravar `.jtl` e relatórios em `/tmp` (descartável) ou `/home/user/persistent` (volume de 1Gi)
- Nunca dentro de `/projects`

### `jmeter -l` falha dizendo que o arquivo existe

**Correção:** `-l` e `-e -o` não sobrescrevem. Rodar `rm -rf` no `.jtl` e no diretório de relatório
antes de cada execução.
