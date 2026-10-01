variable "greeting" {
  description = "Saudação gravada no arquivo de exemplo."
  type        = string
  default     = "Olá, Dev Spaces!"
}

variable "output_path" {
  description = "Caminho do arquivo criado pelo exemplo."
  type        = string
  default     = "/tmp/devspaces-terraform-hello.txt"
}
