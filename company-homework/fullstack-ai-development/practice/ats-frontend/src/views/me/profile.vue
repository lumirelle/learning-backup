<script setup lang="ts">
import { NButton, NTag, useMessage } from 'naive-ui'
import { computed, onMounted, ref } from 'vue'
import { authApi } from '@/api/auth'
import { BizError } from '@/api/request'
import { useAuthStore } from '@/stores/auth'

const auth = useAuthStore()
const msg = useMessage()

const INTEREST_OPTIONS = [
  { id: 'fe', label: '前端工程师' },
  { id: 'pm', label: '产品经理' },
  { id: 'data', label: '数据分析' },
  { id: 'design', label: 'UI 设计' },
]

const selected = ref<Set<string>>(new Set())
const saving = ref(false)

const isCandidate = computed(() => auth.isCandidate)

onMounted(async () => {
  try {
    const me = await authApi.me()
    selected.value = new Set(me.interests ?? [])
  }
  catch (e) {
    console.warn('load profile failed', e)
  }
})

function toggle(id: string) {
  if (selected.value.has(id))
    selected.value.delete(id)
  else selected.value.add(id)
  selected.value = new Set(selected.value)
}

async function save() {
  saving.value = true
  try {
    const me = await authApi.updateProfile({ interests: [...selected.value] })
    if (auth.user) {
      auth.user = { ...auth.user, interests: me.interests }
    }
    msg.success('兴趣方向已保存')
  }
  catch (e) {
    if (e instanceof BizError)
      msg.error(e.message)
  }
  finally {
    saving.value = false
  }
}
</script>

<template>
  <div max-w-640px mx-auto p="x-6 b-16" class="pt-[calc(60px+40px)]">
    <header mb-8>
      <p kicker mb-2>账户</p>
      <h1 m-0 text-2xl font-bold>个人资料</h1>
      <p mt-2 text-sm text-secondary>
        {{ auth.user?.email }} · {{ auth.user?.fullName }}
      </p>
    </header>

    <section v-if="isCandidate" p-6 rounded-xl bg-elevated border="~ subtle">
      <h2 mt-0 mb-3 text-lg font-semibold>感兴趣的方向</h2>
      <div flex flex-wrap gap-2 mb-4>
        <NTag
          v-for="t in INTEREST_OPTIONS"
          :key="t.id"
          :type="selected.has(t.id) ? 'success' : 'default'"
          :bordered="false"
          checkable
          :checked="selected.has(t.id)"
          @click="toggle(t.id)"
        >
          {{ t.label }}
        </NTag>
      </div>
      <NButton type="primary" :loading="saving" @click="save">
        保存
      </NButton>
    </section>

    <p v-else text-secondary text-sm>
      HR / 管理员账户暂无额外资料字段；可在「修改密码」中更新登录凭证。
    </p>
  </div>
</template>
