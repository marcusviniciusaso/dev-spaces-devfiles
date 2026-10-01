package test

import (
	"os"
	"path/filepath"
	"testing"

	"github.com/gruntwork-io/terratest/modules/terraform/v2"
	"github.com/stretchr/testify/assert"
	"github.com/stretchr/testify/require"
)

// TestHelloWorld aplica o projeto num workspace Terraform próprio ("terratest"), confere os
// outputs e o arquivo criado, e destrói tudo no fim. O state do workspace "default" (usado
// quando você roda terraform apply na mão) não é tocado.
func TestHelloWorld(t *testing.T) {
	ctx := t.Context()
	outputPath := filepath.Join(t.TempDir(), "hello.txt")

	options := terraform.WithDefaultRetryableErrors(t, &terraform.Options{
		TerraformDir: "..",
		Vars: map[string]any{
			"greeting":    "Olá, terratest!",
			"output_path": outputPath,
		},
		NoColor: true,
	})

	terraform.InitContext(t, ctx, options)
	terraform.WorkspaceSelectOrNewContext(t, ctx, options, "terratest")
	defer terraform.WorkspaceDeleteContext(t, ctx, options, "terratest")
	defer terraform.DestroyContext(t, ctx, options)

	terraform.ApplyContext(t, ctx, options)

	petName := terraform.OutputContext(t, ctx, options, "pet_name")
	assert.NotEmpty(t, petName)
	assert.Equal(t, outputPath, terraform.OutputContext(t, ctx, options, "file_path"))

	content, err := os.ReadFile(outputPath)
	require.NoError(t, err)
	assert.Equal(t, "Olá, terratest! Workspace: "+petName+"\n", string(content))
}
