# Usa o ruleset "terraform" que já vem embutido no tflint: não precisa de "tflint --init".
plugin "terraform" {
  enabled = true
  preset  = "recommended"
}
