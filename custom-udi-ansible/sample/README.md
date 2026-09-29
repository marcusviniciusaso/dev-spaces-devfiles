# Sample Ansible — Dev Spaces

Ambiente para automação com Ansible: criação, validação e execução de playbooks.

Este sample oferece um workspace pré-configurado com as ferramentas Ansible e as extensões do
VS Code. A partir dele você pode:

- Criar playbooks e roles com autocompletar, documentação inline e validação no editor
- Validar o código com `ansible-lint` antes de publicar
- Executar playbooks localmente (`ansible-playbook`) ou dentro de um Execution Environment (`ansible-navigator`)
- Instalar collections do Automation Hub
- Construir Execution Environments customizados com `ansible-builder`

O ambiente é idêntico para todos os desenvolvedores, independentemente da máquina ou do sistema
operacional.

---

## Quick Start

**1.** Abra o terminal integrado com `Ctrl + J` e configure as credenciais:

```bash
devspaces-setup
```

O script pede o **token do Automation Hub** (para baixar collections) e o **usuário e senha do
registry** (para baixar Execution Environments), e verifica o acesso ao Ansible Automation
Platform. As credenciais ficam em storage persistente.

**2.** Abra um novo terminal e confira o ambiente:

```bash
devspaces-environment
```

**3.** Rode o playbook de exemplo:

```bash
cd sample     # a partir da raiz do projeto clonado
ansible-lint
ansible-playbook playbooks/site.yml
```

---

## Ferramentas instaladas

| Ferramenta | Para que serve |
| --- | --- |
| `ansible-core` | Motor do Ansible: `ansible`, `ansible-playbook`, `ansible-galaxy`, `ansible-doc`, ... |
| `ansible-lint` | Valida playbooks e roles contra boas práticas |
| `ansible-navigator` | Executa e inspeciona playbooks, inclusive dentro de Execution Environments |
| `ansible-builder` | Constrói imagens de Execution Environment |
| `molecule` | Testa roles |
| `ansible-creator` | Gera a estrutura de novos projetos, collections e playbooks |
| `podman` | Executa e constrói containers (usado pelo navigator e pelo builder) |

### Extensões do VS Code

| Extensão | Para que serve |
| --- | --- |
| Red Hat Ansible (`redhat.ansible`) | Autocompletar, documentação inline, lint e execução de playbooks |
| YAML (`redhat.vscode-yaml`) | Validação de YAML e schemas |
| Kubernetes (`ms-kubernetes-tools.vscode-kubernetes-tools`) | Navegação e gerenciamento de recursos Kubernetes |
| OpenShift Connector (`redhat.vscode-openshift-connector`) | Integração com clusters OpenShift |

As extensões são sugeridas a partir de `.vscode/extensions.json`.

---

## Comandos úteis

| Comando | Descrição |
| --- | --- |
| `devspaces-environment` | Mostra as versões das ferramentas e a conectividade com o AAP e o Automation Hub |
| `devspaces-setup` | Configura o Automation Hub, faz login no registry de EEs e verifica o AAP |
| `devspaces-linux-release` | Mostra a versão da imagem base |
| `ansible-lint` | Valida o projeto atual |
| `ansible-playbook playbooks/site.yml` | Executa o playbook localmente |
| `ansible-playbook playbooks/site.yml --check --diff` | Simula a execução sem alterar nada |
| `ansible-navigator run playbooks/site.yml --ee true` | Executa o playbook dentro do Execution Environment |
| `ansible-navigator run playbooks/site.yml --ee false` | Executa com o navigator, sem container |
| `ansible-navigator collections --ee true -m stdout` | Lista as collections do Execution Environment |
| `ansible-galaxy collection install -r requirements.yml` | Instala as collections do Automation Hub |
| `ansible-galaxy collection list` | Lista as collections instaladas |
| `ansible-doc <módulo>` | Mostra a documentação de um módulo (ex.: `ansible-doc ansible.builtin.copy`) |

---

## Estrutura do sample

```
.vscode/
├── extensions.json          # Extensões recomendadas
└── settings.json            # Interpretador do Ansible, lint e abertura deste README
.ansible-lint                # Perfil production
ansible.cfg                  # Inventário e roles_path do projeto
ansible-navigator.yml        # Saída em stdout; imagem do EE vem do devfile
execution-environment.yml    # Exemplo de EE para o ansible-builder
inventory/hosts.yml          # localhost com conexão local
playbooks/site.yml           # Playbook de exemplo
requirements.yml             # Collections a instalar do Automation Hub
roles/hello/                 # Role de exemplo (só ansible.builtin)
```

---

## Collections

Nenhuma collection vem embutida na imagem: só o `ansible.builtin`. Depois do `devspaces-setup`,
instale as que o projeto precisa:

```bash
ansible-galaxy collection install -r requirements.yml
```

Elas são gravadas em `$ANSIBLE_HOME/collections` (storage persistente).

---

## Execution Environments

A imagem padrão do `ansible-navigator` está na variável
`ANSIBLE_NAVIGATOR_EXECUTION_ENVIRONMENT_IMAGE`, definida no devfile. Para usar outra imagem numa
execução:

```bash
ansible-navigator run playbooks/site.yml --ee true --eei <registry>/<imagem>:<tag>
```

Para construir um EE com as collections de `requirements.yml`:

```bash
ansible-builder build -t meu-ee:1.0 \
  --build-arg ANSIBLE_GALAXY_SERVER_LIST \
  --build-arg ANSIBLE_GALAXY_SERVER_AUTOMATION_HUB_URL \
  --build-arg ANSIBLE_GALAXY_SERVER_AUTOMATION_HUB_TOKEN \
  --build-arg ANSIBLE_GALAXY_SERVER_AUTOMATION_HUB_AUTH_URL
```

Os `--build-arg` sem valor repassam as variáveis gravadas pelo `devspaces-setup`. Para gerar só o
contexto de build, sem construir, use `ansible-builder create`.

> Pull e push de imagens só são permitidos nos registries liberados no ambiente.

---

## Storage persistente

O volume `/home/user/persistent` é montado automaticamente e **persiste entre sessões**. Nele
ficam as collections, as credenciais do Automation Hub e do registry, e as imagens baixadas pelo
podman.

> ⚠️ Arquivos fora de `/home/user/persistent` e de `/projects` são perdidos ao reiniciar o workspace.
