# Sample Terraform — Dev Spaces

Ambiente de desenvolvimento Terraform com várias versões gerenciadas pelo tfenv, para
provisionamento de infraestrutura como código.

Este sample oferece um workspace pré-configurado com as ferramentas Terraform e a extensão
HashiCorp Terraform do VS Code. A partir dele você pode:

- Escrever código Terraform com autocompletar, documentação inline e validação no editor
- Alternar entre as versões 1.11, 1.12, 1.13 e 1.14 do Terraform com o `tfenv`
- Validar o código com `terraform validate` e `tflint` antes de publicar
- Gerar a documentação do módulo com `terraform-docs`
- Testar a infraestrutura com `terratest` (Go)

---

## Quick Start

**1.** Abra o terminal integrado com `Ctrl + J` e confira o ambiente:

```bash
devspaces-environment
```

**2.** Rode o projeto de exemplo. Ele funciona sem configurar nada: os providers `random` e
`local` já vêm na imagem.

```bash
cd sample     # a partir da raiz do projeto clonado
terraform init
tflint
terraform plan
terraform apply
cat /tmp/devspaces-terraform-hello.txt
terraform destroy
```

**3.** Rode o teste do terratest:

```bash
cd test
go test -v ./...
```

**4.** Para usar outros providers e módulos nos seus projetos, configure o repositório
corporativo:

```bash
devspaces-setup
```

O script pede o **usuário e o token do Artifactory** e configura o mirror de providers, o token de
módulos, o proxy de módulos Go e, se houver, o mirror de releases do Terraform. As credenciais
ficam em storage persistente. Os providers `random` e `local` continuam vindo da imagem
(3.9.1 e 2.9.1).

---

## Ferramentas instaladas

| Ferramenta | Para que serve |
| --- | --- |
| `tfenv` | Instala e alterna versões do Terraform (`tfenv list`, `tfenv use`) |
| `terraform` | 1.11.4, 1.12.2, 1.13.5 e 1.14.9 (padrão) |
| `tflint` | Lint do código Terraform (ruleset `terraform` embutido) |
| `terraform-docs` | Gera a documentação de entradas e saídas do módulo |
| `terratest` | Biblioteca Go para testes automatizados de infraestrutura |
| `go` | Executa os testes do terratest |
| `podman` | Executa e constrói containers |

### Extensões do VS Code

| Extensão | Para que serve |
| --- | --- |
| HashiCorp Terraform (`hashicorp.terraform`) | Autocompletar, documentação inline, validação e formatação de código Terraform |

A extensão é sugerida a partir de `.vscode/extensions.json`.

---

## Versões do Terraform

```bash
tfenv list                 # versões instaladas (* = em uso)
tfenv use 1.12.2           # troca a versão padrão (fica salva entre sessões)
terraform version
```

Para fixar a versão de um projeto, crie um `.terraform-version` na raiz dele (este sample usa
`1.14.9`). Ele tem prioridade sobre o `tfenv use`.

---

## Comandos úteis

| Comando | Descrição |
| --- | --- |
| `devspaces-environment` | Mostra as versões das ferramentas e a conectividade com os repositórios |
| `devspaces-setup` | Configura o repositório corporativo de providers, módulos Go e releases |
| `devspaces-linux-release` | Mostra a versão da imagem base |
| `terraform init` | Inicializa o projeto e instala os providers |
| `terraform fmt -recursive` | Formata o código |
| `terraform validate` | Valida a sintaxe e a configuração |
| `terraform plan` | Mostra o que será criado, alterado ou destruído |
| `terraform apply` | Aplica as mudanças |
| `terraform destroy` | Destrói os recursos criados |
| `tflint` | Roda o lint no projeto atual |
| `terraform-docs .` | Atualiza a seção de entradas e saídas deste README |
| `go test -v ./...` | Roda os testes do terratest (no diretório `test/`) |

---

## Estrutura do sample

```
.vscode/
└── extensions.json          # Extensão recomendada
.terraform-version           # Versão do Terraform do projeto (tfenv)
.terraform.lock.hcl          # Versões e hashes dos providers
.tflint.hcl                  # Ruleset terraform, preset recommended
.terraform-docs.yml          # Injeta a documentação neste README
versions.tf                  # Versão mínima do Terraform e providers
variables.tf                 # Entradas
main.tf                      # random_pet + local_file
outputs.tf                   # Saídas
test/                        # Teste do terratest (Go)
```

O teste aplica o projeto num workspace Terraform próprio (`terratest`) e o destrói no fim, sem
mexer no state do seu `terraform apply`.

---

## Documentação do módulo

<!-- BEGIN_TF_DOCS -->
## Requirements

| Name | Version |
| ---- | ------- |
| <a name="requirement_terraform"></a> [terraform](#requirement\_terraform) | >= 1.11.0 |
| <a name="requirement_local"></a> [local](#requirement\_local) | ~> 2.9 |
| <a name="requirement_random"></a> [random](#requirement\_random) | ~> 3.9 |

## Providers

| Name | Version |
| ---- | ------- |
| <a name="provider_local"></a> [local](#provider\_local) | 2.9.1 |
| <a name="provider_random"></a> [random](#provider\_random) | 3.9.1 |

## Modules

No modules.

## Resources

| Name | Type |
| ---- | ---- |
| [local_file.hello](https://registry.terraform.io/providers/hashicorp/local/latest/docs/resources/file) | resource |
| [random_pet.workspace](https://registry.terraform.io/providers/hashicorp/random/latest/docs/resources/pet) | resource |

## Inputs

| Name | Description | Type | Default | Required |
| ---- | ----------- | ---- | ------- | :------: |
| <a name="input_greeting"></a> [greeting](#input\_greeting) | Saudação gravada no arquivo de exemplo. | `string` | `"Olá, Dev Spaces!"` | no |
| <a name="input_output_path"></a> [output\_path](#input\_output\_path) | Caminho do arquivo criado pelo exemplo. | `string` | `"/tmp/devspaces-terraform-hello.txt"` | no |

## Outputs

| Name | Description |
| ---- | ----------- |
| <a name="output_content"></a> [content](#output\_content) | Conteúdo gravado no arquivo. |
| <a name="output_file_path"></a> [file\_path](#output\_file\_path) | Arquivo criado pelo provider local. |
| <a name="output_pet_name"></a> [pet\_name](#output\_pet\_name) | Nome gerado pelo provider random. |
<!-- END_TF_DOCS -->

---

## Storage persistente

O volume `/home/user/persistent` é montado automaticamente e **persiste entre sessões**. Nele
ficam a versão escolhida no `tfenv use`, o cache de providers (`TF_PLUGIN_CACHE_DIR`), o cache de
módulos Go e as credenciais do `devspaces-setup`.

> ⚠️ Arquivos fora de `/home/user/persistent` e de `/projects` são perdidos ao reiniciar o workspace.
