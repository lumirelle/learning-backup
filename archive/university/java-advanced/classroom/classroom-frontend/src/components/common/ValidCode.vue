<template>
  <div @click="$emit('validCode')" style="width:156px; height:50px">
    <span v-for="(item, index) in validCode1" :key="index" :style="getStyle(item)">{{ item.code }}</span>
  </div>
</template>

<script>
export default {
  name: 'validCode',
  props: ['info'],
  data() {
    return {
      code: '',
      validCode1: []
    }
  },
  watch: {
    info: function (info) {
      if (this.code !== info.code) {
        this.code = info.code
        this.createdCode()
      }
    }
  },
  methods: {
    createdCode() {
      this.validCode1 = []
      for (let i = 0; i < this.code.length; i++) {
        this.validCode1.push({
          code: this.code.charAt(i),
          color: `rgb(${[Math.round(Math.random() * 220), Math.round(Math.random() * 240), Math.round(Math.random() * 200)]})`,
          fontSize: `${[Math.floor(Math.random() * 10) + 16]}px`,
          padding: `${[Math.floor(Math.random() * 10)]}px`,
          transform: `rotate(${Math.floor(Math.random() * 90) - Math.floor(Math.random() * 90)}deg)`
        })
      }
    },
    getStyle(data) {
      return `color: ${data.color}; font-size: ${data.fontSize}; padding: ${data.padding}; transform: ${data.transform}`
    }
  }
}
</script>

<style scoped>
div {
  display: flex;
  justify-content: center;
  align-items: center;
  cursor: pointer;
}

div span {
  display: inline-block;
}
</style>