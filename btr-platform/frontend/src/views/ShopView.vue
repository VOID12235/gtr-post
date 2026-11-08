<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import PublicShell from '../components/PublicShell.vue'
import { api } from '../api/http'
import { auth } from '../auth'
import type { Product } from '../api/types'

const router = useRouter()
const products = ref<Product[]>([])
const selected = ref<Product | null>(null)
const loading = ref(true)
const message = ref('')
const error = ref('')
const submitting = ref(false)

const form = reactive({
  quantity: 1,
  contactName: '',
  contactPhone: '',
  address: '',
  note: ''
})

async function load() {
  loading.value = true
  try {
    products.value = await api.get<Product[]>('/api/v1/public/shop/products?tenantCode=demo')
  } catch (e) {
    error.value = e instanceof Error ? e.message : '加载失败'
  } finally {
    loading.value = false
  }
}

function buy(product: Product) {
  if (!auth.token) {
    router.push({ path: '/login', query: { redirect: '/shop' } })
    return
  }
  selected.value = product
  form.quantity = 1
  form.contactName = auth.user?.nickname || ''
  form.contactPhone = ''
  form.address = ''
  form.note = ''
  message.value = ''
  error.value = ''
}

async function submitOrder() {
  if (!selected.value || submitting.value) return
  submitting.value = true
  error.value = ''
  message.value = ''
  try {
    await api.post('/api/v1/shop/orders', {
      productId: selected.value.id,
      quantity: Number(form.quantity),
      contactName: form.contactName,
      contactPhone: form.contactPhone,
      address: form.address,
      note: form.note
    })
    message.value = '下单成功。请联系主办方完成线下支付，管理员确认后发货。'
    selected.value = null
    await load()
  } catch (e) {
    error.value = e instanceof Error ? e.message : '下单失败'
  } finally {
    submitting.value = false
  }
}

onMounted(load)
</script>

<template>
  <PublicShell>
    <div class="page-head public-page-head">
      <div>
        <p class="eyebrow">SHOP</p>
        <h1>赛事商城</h1>
        <p class="muted">赛事周边、参赛包、数字商品。线下支付，管理员确认后发货。</p>
      </div>
      <RouterLink class="secondary-button" to="/me">我的订单</RouterLink>
    </div>

    <p v-if="error" class="alert">{{ error }}</p>
    <p v-if="message" class="alert success">{{ message }}</p>

    <div v-if="loading" class="empty">加载中…</div>
    <div v-else-if="products.length === 0" class="empty">商城正在筹备中。</div>
    <div v-else class="shop-grid">
      <article v-for="product in products" :key="product.id" class="shop-card">
        <div class="shop-cover">
          <img v-if="product.cover" :src="product.cover" :alt="product.name" />
          <span v-else>BTR</span>
        </div>
        <div class="shop-body">
          <h3>{{ product.name }}</h3>
          <p class="muted">{{ product.description || '赛事官方周边' }}</p>
          <div class="shop-meta">
            <strong>¥{{ product.price }}</strong>
            <span class="muted">库存 {{ product.stock }}</span>
          </div>
          <button class="primary-button" type="button" :disabled="product.stock <= 0" @click="buy(product)">
            {{ product.stock > 0 ? '立即购买' : '已售罄' }}
          </button>
        </div>
      </article>
    </div>

    <section v-if="selected" class="panel" style="margin-top: 24px">
      <div class="panel-head">
        <h2>购买：{{ selected.name }}</h2>
        <button class="text-link" type="button" @click="selected = null">关闭</button>
      </div>
      <form class="form" @submit.prevent="submitOrder">
        <div class="field-row">
          <label class="field"><span>数量</span><input v-model.number="form.quantity" type="number" min="1" :max="selected.stock" required /></label>
          <label class="field"><span>金额</span><input :value="'¥' + (selected.price * form.quantity).toFixed(2)" disabled /></label>
        </div>
        <div class="field-row">
          <label class="field"><span>联系人</span><input v-model="form.contactName" required /></label>
          <label class="field"><span>手机号</span><input v-model="form.contactPhone" required /></label>
        </div>
        <label class="field"><span>收货地址</span><input v-model="form.address" required /></label>
        <label class="field"><span>备注</span><input v-model="form.note" /></label>
        <p class="hint">提交后请联系主办方线下支付；管理员确认支付后安排发货。</p>
        <button class="primary-button" type="submit" :disabled="submitting">{{ submitting ? '提交中…' : '提交订单' }}</button>
      </form>
    </section>
  </PublicShell>
</template>
