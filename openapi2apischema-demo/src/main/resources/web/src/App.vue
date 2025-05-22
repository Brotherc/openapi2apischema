<template>
  <div class="app-container">
    <router-view></router-view>
  </div>
</template>

<style>
* {
  margin: 0;
  padding: 0;
  box-sizing: border-box;
}

html, body {
  margin: 0;
  padding: 0;
  width: 100%;
  height: 100%;
  overflow-x: hidden;
}

#app {
  margin: 0 0 0 16px !important;
  padding: 0 !important;
  width: 100%;
  height: 100%;
}

.app-container {
  width: 100%;
  height: 100vh;
  margin: 0;
  padding: 0;
}
</style>
