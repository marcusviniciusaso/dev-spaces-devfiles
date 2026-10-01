# Roteiro de Validação — Terraform no Dev Spaces

Este documento descreve o passo a passo para validar as ferramentas, a troca de versões do
Terraform, o lint, a documentação, o terratest e o `devspaces-setup` dentro de um workspace
OpenShift Dev Spaces 3.28 usando a imagem `custom-udi-terraform`.

---

## 1. Ferramentas

```bash
devspaces-environment
tfenv list                   # 1.11.4 1.12.2 1.13.5 * 1.14.9
terraform version            # 1.14.9
tflint --version             # 0.64.0 + ruleset.terraform (bundled)
terraform-docs --version     # 0.24.0
go version                   # >= 1.26
/usr/bin/python3 -V          # Python do sistema, intacto
podman-compose --version     # ferramenta da base, intacta
```

---

## 2. tfenv

```bash
for v in 1.11.4 1.12.2 1.13.5 1.14.9; do tfenv use "$v" >/dev/null && terraform version | head -1; done
tfenv use 1.12.2 && terraform version      # 1.12.2
cd /projects/dev-spaces-devfiles/custom-udi-terraform/sample
terraform version                          # 1.14.9 (.terraform-version do sample)
cd ~ && tfenv use 1.14.9
```

---

## 3. Lint, plan e apply

```bash
cd /projects/dev-spaces-devfiles/custom-udi-terraform/sample
terraform init               # providers vêm de /usr/share/terraform/plugins
terraform fmt -check -recursive
terraform validate
tflint
terraform plan
terraform apply -auto-approve
cat /tmp/devspaces-terraform-hello.txt
terraform destroy -auto-approve
```

---

## 4. terraform-docs

```bash
terraform-docs .             # README.md sem diff (git diff --exit-code README.md)
```

---

## 5. terratest

```bash
cd test
go test -v -count=1 ./...    # PASS; módulos vêm de /opt/terratest/goproxy
```

---

## 6. devspaces-setup

Com as URLs do devfile ainda como placeholder:

```bash
devspaces-setup              # aborta sem pedir credenciais
```

Com um proxy Go público no lugar do repositório corporativo (qualquer usuário e token servem):

```bash
ARTIFACTORY_GO_URL=https://proxy.golang.org devspaces-setup
ls -l /home/user/persistent/.terraform.d/   # .netrc e devspaces.env com modo 600
source ~/.bashrc && go env GOPROXY          # file:///opt/terratest/goproxy,https://proxy.golang.org
```

---

## 7. Extensão

Abra `sample/main.tf`: hover em `random_pet` mostra a documentação, e `Ctrl + Space` dentro de um
bloco `resource` sugere os argumentos.

---

## 8. Persistência (pause/resume)

Após `tfenv use 1.13.5`, pausar e retomar o workspace:

```bash
terraform version                                  # 1.13.5 (fora do sample)
ls /home/user/persistent/.terraform.d/plugin-cache # providers em cache
```
