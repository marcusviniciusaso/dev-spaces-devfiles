# Planos JMeter

Plano de exemplo para validar o **Apache JMeter 5.6.3** dentro do workspace, em modo **non-GUI** e
**sem egress**.

## `smoke-plan.jmx`

| Elemento | Configuração |
| --- | --- |
| Thread Group | 2 threads × 5 iterações (10 samples) |
| HTTP Request | `GET http://localhost:8000/test-page.html` |
| Assertions | `responseCode == 200` e resposta contendo `Hello from Dev Spaces!` |
| Timer | Constant Timer de 200 ms |
| Listener | Summary Report |

O alvo é a mesma página usada pelos testes Selenium (`../src/test/resources/test-page.html`),
servida por um `jwebserver` local — o servidor HTTP que acompanha o próprio JDK 18+.

## Como executar

```bash
cd /projects/dev-spaces-devfiles/custom-udi-qa/sample

# 1. Sobe o servidor local (JDK 21) servindo as páginas de teste
with-java 21 jwebserver -p 8000 -b 127.0.0.1 -d "$PWD/src/test/resources" &

# 2. Roda o plano em modo non-GUI
jmeter -n -t jmeter/smoke-plan.jmx -l /tmp/jmeter-result.jtl -j /tmp/jmeter.log

# 3. Derruba o servidor
kill %1
```

Ou, pelo devfile: `Ctrl+Shift+P` → **Tasks: Run Task** → **devfile: Validate JMeter (non-GUI, sem egress)**.

Saída esperada: `summary = 10 in 00:00:0X ... Err: 0 (0.00%)` e um `.jtl` com `success=true` em
todas as linhas.

## Relatório HTML

```bash
rm -rf /tmp/jmeter-result.jtl /tmp/jmeter-report
jmeter -n -t jmeter/smoke-plan.jmx -l /tmp/jmeter-result.jtl -e -o /tmp/jmeter-report -j /tmp/jmeter.log
```

O relatório fica em `/tmp/jmeter-report/index.html` (abra pelo Explorer do che-code, ou copie para
`/home/user/persistent` se quiser mantê-lo entre reinícios do workspace).

> `-l` e `-o` **falham se o arquivo/diretório já existir**. Sempre limpe antes (`rm -rf`) ou use
> um nome novo a cada execução.

## Onde gravar os resultados

- **`/tmp`** — descartável, some quando o pod reinicia. Ideal para smoke tests.
- **`/home/user/persistent`** — volume persistente de 1Gi montado pelo devfile. Use para resultados
  que precisam sobreviver ao restart.
- **Nunca em `/projects`** — o diretório do repositório clonado; `.jtl` e relatórios HTML são grandes
  e acabariam no `git status`.

## Autoria de planos

A **GUI do JMeter não roda no workspace** (container headless, sem X11). As opções são:

1. Editar o XML do `.jmx` direto no che-code — a extensão `redhat.vscode-xml` dá validação e
   formatação. É o fluxo recomendado para ajustes pontuais e para revisar diffs no PR.
2. Autorar o plano na GUI do JMeter na **máquina local do QA** e versionar o `.jmx` aqui.
3. Último recurso: VNC/noVNC (fora do escopo desta imagem).

## Aviso: não gere carga real daqui

O pod tem `cpuLimit: 2` e `memoryLimit: 6Gi`. Qualquer teste de carga executado de dentro do
workspace tem os números distorcidos pelos limites do container e pela rede do cluster. **O workspace
serve para desenvolver, versionar e rodar smoke tests dos planos** — a execução de carga real deve
sair de uma injetora dedicada.

O heap default do JMeter aqui é `-Xms256m -Xmx1g` (definido no wrapper `/usr/local/bin/jmeter`).
Para planos maiores: `HEAP="-Xms512m -Xmx2g" jmeter -n -t ...`.
