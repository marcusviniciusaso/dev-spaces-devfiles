# Selenium + Cucumber Sample

Projeto Maven de exemplo que valida a execução de testes automatizados com **Selenium (Chrome headless)** e **Cucumber** dentro de um workspace OpenShift Dev Spaces.

## O que este projeto prova

1. **Chrome headless funciona no pod** — sem necessidade de `Xvfb` ou display virtual; Chrome roda com `--headless=new --no-sandbox --disable-dev-shm-usage`.
2. **Chromedriver vem da imagem** — não é necessário versionar binários no git; o caminho é resolvido via variável de ambiente `WEBDRIVER_CHROME_DRIVER`.
3. **Zero egress** — os testes carregam uma página HTML local (`file://`), sem dependência de acesso à internet durante a execução.
4. **Compatível com 3 runners** — os testes podem ser executados via:
   - `mvn test` no terminal
   - Ícones de play do **Test Runner for Java** (Testing view)
   - Run de cenário pela extensão **Cucumber**

## Estrutura

```
src/test/
├── java/com/example/
│   ├── selenium/
│   │   ├── WebDriverFactory.java    # Factory com ChromeOptions headless
│   │   └── SmokeSeleniumTest.java   # Teste JUnit 5 direto
│   └── cucumber/
│       ├── RunCucumberTest.java     # Suite entry-point
│       └── SearchSteps.java         # Step definitions
└── resources/
    ├── test-page.html               # Página HTML de teste (file://)
    └── com/example/cucumber/
        └── smoke.feature            # Cenários Gherkin
```

## Pré-requisitos

- Workspace usando a imagem `quay.io/marolive/custom-udi-java21-selenium:1.0.0`
- Extensões recomendadas instaladas (ver `.vscode/extensions.json`)

## Execução

```bash
mvn test
```
