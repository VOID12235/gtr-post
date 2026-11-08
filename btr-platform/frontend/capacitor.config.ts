/**
 * Capacitor 配置：把同一套 PWA 打包成 Android / iOS 原生壳。
 * 需要本机安装 Android Studio / Xcode 后再执行：
 *   npm i -D @capacitor/cli @capacitor/core
 *   npm i @capacitor/android @capacitor/ios
 *   npx cap add android
 *   npx cap add ios
 *   npm run build && npx cap sync
 */
const config = {
  appId: 'com.btr.platform',
  appName: 'BTR',
  webDir: 'dist',
  bundledWebRuntime: false,
  server: {
    androidScheme: 'https'
  }
}

export default config
