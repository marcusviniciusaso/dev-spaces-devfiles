resource "random_pet" "workspace" {
  length = 2
}

resource "local_file" "hello" {
  filename        = var.output_path
  content         = "${var.greeting} Workspace: ${random_pet.workspace.id}\n"
  file_permission = "0644"
}
