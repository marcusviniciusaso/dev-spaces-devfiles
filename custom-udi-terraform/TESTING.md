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

Fora do sample: dentro dele, o `.terraform-version` (1.14.9) tem prioridade sobre o `tfenv use`.

```bash
cd ~
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
terraform validate           # Success! The configuration is valid.
tflint
terraform plan
terraform apply -auto-approve
cat /tmp/devspaces-terraform-hello.txt
terraform destroy -auto-approve
```

---

## 4. terraform-docs

O `terraform-docs` lê variáveis, outputs, providers e resources dos `.tf` e reescreve a tabela
entre `<!-- BEGIN_TF_DOCS -->` e `<!-- END_TF_DOCS -->` do `README.md`.

```bash
cd /projects/dev-spaces-devfiles/custom-udi-terraform/sample
terraform-docs . && git diff --exit-code README.md   # sem diff: a documentação já está em dia

cat > docs_demo.tf <<'TF'
output "demo" {
  description = "Output temporário para testar o terraform-docs."
  value       = "ok"
}
TF
terraform-docs .
git diff README.md            # nova linha "demo" na tabela de Outputs

rm docs_demo.tf && git checkout README.md
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
ls -lah /home/user/persistent/.terraform.d/   # .netrc e devspaces.env com modo 600
source ~/.bashrc && go env GOPROXY          # file:///opt/terratest/goproxy,https://proxy.golang.org
```

---

## 7. Persistência (pause/resume)

Fora do sample (dentro dele vale o `.terraform-version`):

```bash
cd ~ && tfenv use 1.13.5 && terraform version      # 1.13.5
```

Pausar e retomar o workspace e, num novo terminal:

```bash
cd ~ && terraform version                          # 1.13.5
cat /home/user/persistent/.tfenv/version           # 1.13.5
ls /home/user/persistent/.terraform.d/plugin-cache # providers em cache
```
