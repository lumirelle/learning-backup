/// <reference types="vite/client" />
/// <reference types="@dcloudio/types/uni-app/index.d.ts" />
/// <reference types="../build/cool/eps.d.ts" />
/// <reference types="../uni_modules/cool-ui/types/index.d.ts" />

declare module '*.vue' {
  import type { DefineComponent } from 'vue'
  // eslint-disable-next-line ts/no-empty-object-type
  const component: DefineComponent<{}, {}, any>
  export default component
}

declare module 'virtual:ctx';
declare module 'virtual:eps';
declare module '@dcloudio/vite-plugin-uni';
