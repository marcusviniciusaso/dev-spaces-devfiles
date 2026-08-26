# QA Sample — Selenium + Cucumber + JMeter

Projeto de exemplo que valida, dentro de um workspace OpenShift Dev Spaces, os três pilares da
imagem `custom-udi-qa`: **testes de frontend com Selenium (Chrome headless)**, **cenários Gherkin
com Cucumber** e **teste de performance com Apache JMeter em modo non-GUI**.

## O que este projeto prova

1. **Chrome headless funciona no pod** — sem necessidade de `Xvfb` ou display virtual; Chrome roda com
   `--headless=new --no-sandbox --disable-dev-shm-usage`.
2. **Chromedriver vem da imagem** — não é necessário versionar binários no git; o caminho é resolvido
   via variável de ambiente `WEBDRIVER_CHROME_DRIVER`.
3. **Zero egress** — os testes Selenium carregam uma página HTML local (`file://`) e o plano JMeter
   aponta para um `jwebserver` em `localhost`. Nenhuma dependência de internet durante a execução.
4. **Compatível com 3 runners** — os testes podem ser executados via:
   - `mvn test` no terminal
   - Ícones de play do **Test Runner for Java** (Testing view)
   - Run de cenário pela extensão **Cucumber**
5. **Cinco JDKs disponíveis** — `use-java 8|11|17|21|25` no shell, `with-java <versão> <comando>`
   para execuções pontuais. O `.vscode/settings.json` registra as cinco runtimes na extensão Java.

## Estrutura

```
.vscode/
├── extensions.json                   # Extensões recomendadas (Open VSX)
└── settings.json                     # java.configuration.runtimes (5 JDKs, default 21)
jmeter/
├── README.md                         # Como rodar, relatório HTML, onde gravar
└── smoke-plan.jmx                    # Plano non-GUI apontando para localhost:8000
src/test/
├── java/com/example/
│   ├── selenium/
│   │   ├── WebDriverFactory.java     # Factory com ChromeOptions headless
│   │   └── SmokeSeleniumTest.java    # Teste JUnit 5 direto
│   └── cucumber/
│       ├── CucumberRunnerTest.java   # Suite entry-point
│       └── SearchSteps.java          # Step definitions
└── resources/
    ├── test-page.html                # Página de teste (file:// no Selenium, http:// no JMeter)
    └── com/example/cucumber/
        └── smoke.feature             # Cenários Gherkin
```

## Pré-requisitos

- Workspace usando a imagem `quay.io/marolive/custom-udi-qa:1.0.0`
- Extensões recomendadas instaladas (ver `.vscode/extensions.json`)

## Execução

```bash
# Testes de frontend (Selenium + Cucumber)
mvn test

# Smoke test do JMeter — ver jmeter/README.md
with-java 21 jwebserver -p 8000 -b 127.0.0.1 -d "$PWD/src/test/resources" &
jmeter -n -t jmeter/smoke-plan.jmx -l /tmp/jmeter-result.jtl -j /tmp/jmeter.log
kill %1
```

## Sobre a versão do Java

O `pom.xml` compila com `<maven.compiler.release>21</maven.compiler.release>`. O **Selenium 4.x exige
JVM 11+**, então os testes não rodam em Java 8. Se a aplicação sob teste for legada, compile-a com
`--release 8` mas mantenha os **testes** em JVM 11+ (ideal: 21) — ver as notas de compatibilidade no
README da imagem.
