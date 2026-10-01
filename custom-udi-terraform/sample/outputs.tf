output "pet_name" {
  description = "Nome gerado pelo provider random."
  value       = random_pet.workspace.id
}

output "file_path" {
  description = "Arquivo criado pelo provider local."
  value       = local_file.hello.filename
}

output "content" {
  description = "Conteúdo gravado no arquivo."
  value       = local_file.hello.content
}
