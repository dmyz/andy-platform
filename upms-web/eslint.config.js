// eslint.config.mjs
import antfu from '@antfu/eslint-config'

export default antfu(
  {
    vue: true,
    typescript: true,
    formatters: true,
    gitignore: true,
    ignores: [
      '**/dist',
      '**/node_modules',
      '**/public',
      '**/coverage',
      '**/*.min.js',
      'package.json',
      'tsconfig*',
    ],
    rules: {
      'no-console': 'off',
    },
  },

)
