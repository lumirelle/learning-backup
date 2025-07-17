<template>
  <div>
    <Title/>
    <router-view></router-view>
  </div>
</template>

<script>
import Title from "../components/common/Title.vue";

export default {
  name: "MajorHome",
  components: {Title}
};
</script>
