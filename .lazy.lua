-- Quality
local eslint_support_filetypes = {
  -- Code
  "javascript",
  "javascriptreact",
  "typescript",
  "typescriptreact",
  "vue",
  -- Configurations
  "json",
  "jsonc",
  "yaml",
  "toml",
  -- Documentations
  "markdown",
}
return {
  {
    "AstroNvim/astrolsp",
    ---@type AstroLSPOpts
    opts = {
      config = {
        eslint = {
          filetypes = eslint_support_filetypes,
        },
      },
      formatting = {
        format_on_save = false,
        filter = function(_)
          if vim.tbl_contains(eslint_support_filetypes, vim.bo.filetype) then
            return false
          end
          return true
        end,
      },
      autocmds = {
        eslint_fix_all = {
          cond = function(client, _)
            return client.name == "eslint"
          end,
          {
            event = "BufWritePre",
            desc = "Fix all ESLint problems on save",
            command = "LspEslintFixAll",
          },
        },
        oxlint_fix_all = {
          cond = function(client, _)
            return client.name == "oxlint"
          end,
          {
            event = "BufWritePre",
            desc = "Fix all Oxlint problems on save",
            command = "LspOxlintFixAll",
          },
        },
      },
    },
  },
}
