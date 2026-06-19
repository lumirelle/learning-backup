<script setup lang="ts">
import type { HealthVO } from '@/api/health'
import { NButton, NCard, NDescriptions, NDescriptionsItem, NSpin, NTag } from 'naive-ui'
import { onMounted, ref } from 'vue'
import { getHealth } from '@/api/health'
import { BizError } from '@/api/request'
import ErrorBlock from '@/components/ErrorBlock.vue'

const loading = ref(false)
const data = ref<HealthVO | null>(null)
const err = ref<string | null>(null)
/**
 * 是否第一次加载 —— 第一次成功时不弹 toast（避免每次进入页面噪音），
 * 用户主动点"重新检查"才弹反馈。
 */
const firstLoad = ref(true)

async function fetch(showToast = false) {
  loading.value = true
  err.value = null
  try {
    data.value = await getHealth()
    if (showToast && !firstLoad.value) {
      // toast 仅在主动重试时弹（避免初次进入打扰）
    }
  }
  catch (e) {
    err.value = e instanceof BizError ? `${e.code}: ${e.message}` : (e as Error).message
  }
  finally {
    firstLoad.value = false
    loading.value = false
  }
}

function manualRefetch() {
  fetch(true)
}

onMounted(() => fetch())
</script>

<template>
  <main
    max-w-760px
    mx-auto
    p="x-6 b-12 t-92px"
  >
    <header
      flex="~ items-end justify-between wrap"
      gap-4
      mb-6
    >
      <div>
        <p kicker m="0 b-1">
          System Health · 服务健康
        </p>
        <h1 m-0 text-3xl font-bold tracking-tight>
          Health Check
        </h1>
        <p m="t-2 b-0" text-sm text-secondary>
          前端 → Vite proxy → Spring Boot → PostgreSQL / Redis
        </p>
      </div>
      <NButton type="primary" :loading="loading" @click="manualRefetch">
        重新检查
      </NButton>
    </header>

    <NSpin :show="loading">
      <NCard size="medium" :bordered="true" embedded>
        <template v-if="data && !err">
          <NDescriptions label-placement="left" :column="2" bordered size="small">
            <NDescriptionsItem label="服务">
              {{ data.app }}
            </NDescriptionsItem>
            <NDescriptionsItem label="时间">
              <span class="num">{{ data.time }}</span>
            </NDescriptionsItem>
            <NDescriptionsItem label="PostgreSQL">
              <NTag size="small" :type="data.db === 'UP' ? 'success' : 'error'" round :bordered="false">
                {{ data.db }}
              </NTag>
            </NDescriptionsItem>
            <NDescriptionsItem label="Redis">
              <NTag size="small" :type="data.redis === 'UP' ? 'success' : 'error'" round :bordered="false">
                {{ data.redis }}
              </NTag>
            </NDescriptionsItem>
          </NDescriptions>
        </template>
        <template v-else-if="err">
          <ErrorBlock
            title="无法连接到后端"
            :description="`${err}。请确认后端已启动（bun run be:dev）。`"
            :retrying="loading"
            @retry="manualRefetch"
          />
        </template>
      </NCard>
    </NSpin>

    <p
      mt-6
      text-center text-sm text-tertiary
    >
      上面 db / redis 都显示
      <strong text-success-700>UP</strong>
      即整条链路畅通
    </p>
  </main>
</template>
