# Developer notes

## Monaco Editor

### Install dependencies

```shell
yarn add monaco-editor
# additional langs
yarn add monaco-graphql # <- 'graphql-language-server' will be installed transitively
yarn add graphql
```

### Configure project

```shell
yarn add -D vite-plugin-monaco-editor
```

For Quasar in `quasar.config.js` add to `vitePlugins`:

```js
[
  'vite-plugin-monaco-editor',
  {
    languageWorkers: ['json', 'editorWorkerService'],
    customWorkers: [
      {
        label: 'graphql',
        entry: 'monaco-graphql/dist/graphql.worker',
      },
    ],
    customDistPath: (root, buildOutDir) => {
      return path.join(buildOutDir, 'monacoeditorwork');
    },
  },
],
```

For Vite in `vite.config.ts` add to `plugins`:

```ts
// @ts-ignore
monacoEditorPlugin.default({
  languageWorkers: ['json', 'editorWorkerService'],
  customWorkers: [
    {
      label: 'graphql',
      entry: 'monaco-graphql/dist/graphql.worker',
    },
  ],
}),
```

^^^ See [issue](https://github.com/vdesjs/vite-plugin-monaco-editor/issues/21#issuecomment-1002102971).

### Use in Vue

Install [Monaco Editor for Vue3](https://github.com/bazingaedward/monaco-editor-vue3):

```
yarn add monaco-editor-vue3
```

### Other libs and tools

[GraphiQL Toolkit](https://github.com/graphql/graphiql/tree/main/packages/graphiql-toolkit)

```
yarn add @graphiql/toolkit
```

## Loading progress

See [this page](https://quasar.dev/quasar-plugins/loading) for more.

```
plugins: [
  'Loading'
],
config: {
  loading: { /* look at QuasarConfOptions from the API card */ }
}
```
