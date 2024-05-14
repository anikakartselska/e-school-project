<template>
  <div id="notfound">
    <div class="notfound">
      <div class="notfound-404">
        <h1>404</h1>
      </div>
      <h2>Опа! Тази страница или данни не могат да бъдат намерени.</h2>
      <p>Съжаляваме, но страницата, която търсите, не съществува, е премахната, името ѝ е променено или временно не е
        достъпна</p>
      <a @click="$router.push('/');">начало</a>
      <a @click="$router.go(-1);">назад</a>
    </div>
  </div>
</template>

<script lang="ts" setup>

</script>

<style scoped>

#notfound .notfound {
  position: absolute;
  left: 50%;
  top: 50%;
  transform: translate(-50%, -50%);
}


.notfound .notfound-404 {
  position: relative;
  height: 150px;
  line-height: 150px;
  margin-bottom: 25px;
}

.notfound .notfound-404 h1 {
  font-size: 186px;
  font-weight: 900;
  margin: 0;
}

.notfound h2 {
  font-size: 26px;
  font-weight: 700;
  margin: 0;
}

.notfound p {
  font-size: 14px;
  font-weight: 500;
  margin-bottom: 0px;
}

.notfound a {
  display: inline-block;
  text-transform: uppercase;
  color: #fff;
  text-decoration: none;
  border: none;
  background: #5c91fe;
  padding: 10px 40px;
  font-size: 14px;
  font-weight: 700;
  border-radius: 1px;
  margin-top: 15px;
  margin-right: 10px;
  transition: 0.2s all;
}

.notfound a:hover {
  opacity: 0.8;
}
</style>
