# Roteiro de Validação — Ansible no Dev Spaces

Este documento descreve o passo a passo para validar as ferramentas, as extensões, o Automation
Hub, os Execution Environments e o acesso ao Ansible Automation Platform dentro de um workspace
OpenShift Dev Spaces 3.28 usando a imagem `custom-udi-ansible`.

> Build e push da imagem não fazem parte deste roteiro — estão no `README.md` da imagem, em
> `dev-spaces-images/ansible/`.

---

## 0. Ajustar as URLs no devfile (antes de abrir)

O `devfile.yaml` traz placeholders. Para testar, ajuste numa branch ou num fork:

| Variável | Exemplo para teste |
| --- | --- |
| `AUTOMATION_HUB_URL` | URL do Private Automation Hub, ou `https://console.redhat.com/api/automation-hub/content/published/` |
| `AUTOMATION_HUB_AUTH_URL` | Só para console.redhat.com: `https://sso.redhat.com/auth/realms/redhat-external/protocol/openid-connect/token` |
| `AUTOMATION_HUB_REGISTRY` | Host do Private Automation Hub, ou `registry.redhat.io` |
| `AAP_CONTROLLER_URL` | URL do AAP |

Sem esse ajuste, o workspace abre normalmente: o `devspaces-setup` recusa a URL placeholder, e o
`devspaces-environment` mostra "não configurado".

---

## 1. Abrir o workspace

```
https://<devspaces-fqdn>#https://github.com/marcusviniciusaso/dev-spaces-devfiles?df=custom-udi-ansible/devfile.yaml
```

O repositório é clonado em `/projects/dev-spaces-devfiles`, e o sample fica em:

```bash
cd /projects/dev-spaces-devfiles/custom-udi-ansible/sample
```

Validar no Dashboard que o workspace aparece como **Ansible**.

---

## 2. Extensões

Abrir a pasta `custom-udi-ansible/sample` (**File → Open Folder**), para que o che-code leia o
`sample/.vscode/extensions.json`, e aceitar a instalação ou instalar pela view de Extensions:

- `redhat.ansible` — Ansible
- `redhat.vscode-yaml` — YAML
- `ms-kubernetes-tools.vscode-kubernetes-tools` — Kubernetes
- `redhat.vscode-openshift-connector` — OpenShift Connector

Critérios:
- o `README.md` do sample abre em preview ao abrir a pasta;
- em `playbooks/site.yml`, o modo de linguagem é **Ansible** e há autocompletar para `ansible.builtin.*`;
- em **Output → Ansible Server**, não aparecem erros de interpretador; ele usa `/opt/ansible/bin/python`.

> Se o cluster usa Open VSX embedded ou um espelho interno, confirme que as quatro extensões
> estão publicadas lá (`spec.components.pluginRegistry.openVSXURL` no CheCluster).

---

## 3. Ferramentas

```bash
devspaces-environment
ansible --version            # core 2.21.4, python 3.12 (/opt/ansible/bin/python)
ansible-lint --version
ansible-navigator --version
ansible-builder --version
molecule --version
ansible-creator --version
/usr/bin/python3 -V          # Python do sistema, intacto
podman-compose --version     # ferramenta da base, intacta
```

---

## 4. Lint e execução local

```bash
cd /projects/dev-spaces-devfiles/custom-udi-ansible/sample
ansible-lint                                  # Passed ... Profile 'production'
ansible-playbook playbooks/site.yml           # ok=4 changed=1 failed=0
cat /tmp/devspaces-ansible-hello.txt
ansible-navigator run playbooks/site.yml --ee false
```

---

## 5. devspaces-setup (Automation Hub, registry e AAP)

```bash
devspaces-setup
```

Informar o token do Hub e o usuário e a senha do registry. Critérios:
- `✔ Servidor Galaxy configurado`;
- `✔ Login no registry ... salvo em /home/user/persistent/.config/containers/auth.json`;
- `✔ AAP acessível` (ou a mensagem de "não configurada", se a URL for placeholder).

Em um **novo terminal**:

```bash
env | grep ANSIBLE_GALAXY_SERVER_AUTOMATION_HUB_URL
ansible-galaxy collection install -r requirements.yml
ansible-galaxy collection list | grep -E 'ansible\.(posix|utils)'
ls /home/user/persistent/.ansible/collections/ansible_collections/ansible
```

---

## 6. Execution Environment

```bash
podman pull "$ANSIBLE_NAVIGATOR_EXECUTION_ENVIRONMENT_IMAGE"
ansible-navigator run playbooks/site.yml --ee true
ansible-navigator collections --ee true -m stdout | head
```

> Ponto de atenção: `--ee true` depende do podman aninhado (vfs) no cluster. Se falhar, registrar
> o erro. O modo `--ee false` (seção 4) continua como alternativa.

---

## 7. ansible-builder

```bash
ansible-builder create
ls context/                         # Containerfile + _build/
ansible-builder build -t ee-teste:1.0 \
  --build-arg ANSIBLE_GALAXY_SERVER_LIST \
  --build-arg ANSIBLE_GALAXY_SERVER_AUTOMATION_HUB_URL \
  --build-arg ANSIBLE_GALAXY_SERVER_AUTOMATION_HUB_TOKEN \
  --build-arg ANSIBLE_GALAXY_SERVER_AUTOMATION_HUB_AUTH_URL
rm -rf context
```

---

## 8. Persistência (pause/resume)

Pausar e retomar o workspace (ou reiniciar pelo Dashboard) e conferir:

```bash
ansible-galaxy collection list | grep -E 'ansible\.(posix|utils)'   # ainda instaladas
env | grep ANSIBLE_GALAXY_SERVER_AUTOMATION_HUB_URL                 # token ainda carregado
podman images                                                       # EE ainda presente
```
