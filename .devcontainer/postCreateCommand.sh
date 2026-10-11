#!/usr/bin/env bash
set -e

echo "Installing AI CLI tools globally with npm..."

# Codex CLI (OpenAI Codex client)
npm install -g @openai/codex@latest

# Claude Code CLI (Anthropic client)
npm install -g @anthropic-ai/claude-code
claude mcp add --transport http frc-mentoring https://ca-frc-mentoring-mcp.politeglacier-8eb53737.westus2.azurecontainerapps.io

# Gemini CLI (Google Gemini client)
npm install -g @google/gemini-cli
gemini mcp add --transport http frc-mentoring https://ca-frc-mentoring-mcp.politeglacier-8eb53737.westus2.azurecontainerapps.io


# Configure CoPilot MCP, Check if .vscode/mcp.json exists, if not create it
if [ ! -f .vscode/mcp.json ]; then
    cat > .vscode/mcp.json << 'EOF'
{
	"servers": {
		"frc-mentoring": {
			"url": "https://ca-frc-mentoring-mcp.politeglacier-8eb53737.westus2.azurecontainerapps.io",
			"type": "http"
		}
	},
	"inputs": []
}
EOF
    echo "Created .vscode/mcp.json"
fi

echo "✅ All tools installed successfully!"
echo "Run with: codex, claude, gemini (depending on tool)."
