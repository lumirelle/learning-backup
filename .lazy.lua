-- ESLint as formatter
local eslint_support_filetypes = {
  -- JS & TS
  "javascript",
  "javascriptreact",
  "typescript",
  "typescriptreact",
  -- Markdown
  "markdown",
  -- JSON
  "json",
  "jsonc",
  -- YAML
  "yaml",
  -- TOML
  "toml",
  -- Vue
  "vue",
}
local eslint_support_file_patterns = {
  -- JS & TS
  "*.js",
  "*.mjs",
  "*.cjs",
  "*.jsx",
  "*.ts",
  "*.mts",
  "*.cts",
  "*.tsx",
  -- Markdown
  "*.md",
  "*.mdc",
  "*.mdx",
  -- JSON
  "*.json",
  "*.jsonc",
  -- YAML
  "*.yaml",
  "*.yml",
  -- TOML
  "*.toml",
  -- Vue
  "*.vue",
}
-- Disable other formatter conflict with ESLint
require("snacks").util.lsp.on(function(_, client)
  if vim.tbl_contains(eslint_support_filetypes, vim.bo.filetype) then
    client.server_capabilities.documentFormattingProvider = false
    client.server_capabilities.documentRangeFormattingProvider = false
  end
end)
-- yamlls cannot be disabled via previous method.
-- This is quite wired...
require("lspconfig").yamlls.setup({
  settings = {
    yaml = {
      format = { enable = false },
    },
  },
})
return {
  {
    "neovim/nvim-lspconfig",
    opts = {
      servers = {
        eslint = {
          filetypes = eslint_support_filetypes,
        },
      },
      setup = {
        eslint = function()
          -- Auto-fix on save for specific filetypes
          vim.api.nvim_create_autocmd("BufWritePre", {
            pattern = eslint_support_file_patterns,
            command = "LspEslintFixAll",
          })
        end,
      },
    },
  },
}
