# Roteiro de Validação — Ansible no Dev Spaces

Este documento descreve o passo a passo para validar as ferramentas, as extensões, o Automation
Hub, os Execution Environments e o acesso ao Ansible Automation Platform dentro de um workspace
OpenShift Dev Spaces 3.28 usando a imagem `custom-udi-ansible`.

---

## 1. Ferramentas

```bash
devspaces-environment
ansible --version            # core 2.21.4, python 3.12
ansible-lint --version
ansible-navigator --version
ansible-builder --version
molecule --version
ansible-creator --version
/usr/bin/python3 -V          # Python do sistema, intacto
podman-compose --version     # ferramenta da base, intacta
```

---

## 2. Lint e execução local

```bash
cd /projects/dev-spaces-devfiles/custom-udi-ansible/sample
ansible-lint                                  # Passed ... Profile 'production'
ansible-playbook playbooks/site.yml           # ok=4 changed=1 failed=0
cat /tmp/devspaces-ansible-hello.txt
ansible-navigator run playbooks/site.yml --ee false
```

---

## 3. devspaces-setup (Automation Hub, registry e AAP)

```bash
devspaces-setup
```

Esperado: `✔ Servidor Galaxy configurado`, `✔ Login no registry` e `✔ AAP acessível`.

Em um **novo terminal**:

```bash
ansible-galaxy collection install -r requirements.yml
ansible-galaxy collection list | grep -E 'ansible\.(posix|utils)'
```

---

## 4. Execution Environment

```bash
podman pull "$ANSIBLE_NAVIGATOR_EXECUTION_ENVIRONMENT_IMAGE"
ansible-navigator run playbooks/site.yml --ee true
ansible-navigator collections --ee true -m stdout | head
```

---

## 5. ansible-builder

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

## 6. Persistência (pause/resume)

Após pausar e retomar o workspace:

```bash
ansible-galaxy collection list | grep -E 'ansible\.(posix|utils)'   # ainda instaladas
env | grep ANSIBLE_GALAXY_SERVER_AUTOMATION_HUB_URL                 # token ainda carregado
podman images                                                       # EE ainda presente
```
