<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import AppShell from '../components/AppShell.vue'
import { api } from '../api/http'
import type { Product, ShopOrder } from '../api/types'
import { formatDate, statusLabel } from '../utils/format'

const tab = ref<'products' | 'orders'>('products')
const products = ref<Product[]>([])
const orders = ref<ShopOrder[]>([])
const loading = ref(true)
const error = ref('')
const message = ref('')

const form = reactive({
  name: '',
  description: '',
  cover: '',
  price: 0,
  stock: 100
})

async function load() {
  loading.value = true
  error.value = ''
  try {
    const [productList, orderList] = await Promise.all([
      api.get<Product[]>('/api/v1/admin/shop/products'),
      api.get<ShopOrder[]>('/api/v1/admin/shop/orders')
    ])
    products.value = productList
    orders.value = orderList
  } catch (e) {
    error.value = e instanceof Error ? e.message : '加载失败'
  } finally {
    loading.value = false
  }
}

async function createProduct() {
  error.value = ''
  message.value = ''
  try {
    await api.post<Product>('/api/v1/admin/shop/products', {
      name: form.name.trim(),
      description: form.description.trim(),
      cover: form.cover.trim() || null,
      price: Number(form.price),
      stock: Number(form.stock)
    })
    message.value = '商品已创建'
    form.name = ''
    form.description = ''
    form.cover = ''
    form.price = 0
    form.stock = 100
    await load()
  } catch (e) {
    error.value = e instanceof Error ? e.message : '创建失败'
  }
}

async function toggleProduct(product: Product) {
  try {
    await api.put(`/api/v1/admin/shop/products/${product.id}/status`, {
      status: product.status === 'ACTIVE' ? 'INACTIVE' : 'ACTIVE'
    })
    await load()
  } catch (e) {
    error.value = e instanceof Error ? e.message : '操作失败'
  }
}

async function editProduct(product: Product) {
  const price = window.prompt('新价格', String(product.price))
  if (price === null) return
  const stock = window.prompt('新库存', String(product.stock))
  if (stock === null) return
  try {
    await api.put(`/api/v1/admin/shop/products/${product.id}`, {
      name: product.name,
      description: product.description || '',
      cover: product.cover || null,
      price: Number(price),
      stock: Number(stock)
    })
    message.value = '商品已更新'
    await load()
  } catch (e) {
    error.value = e instanceof Error ? e.message : '更新失败'
  }
}

async function markPaid(order: ShopOrder) {
  try {
    await api.post(`/api/v1/admin/shop/orders/${order.id}/paid`, {})
    message.value = '已确认支付'
    await load()
  } catch (e) {
    error.value = e instanceof Error ? e.message : '操作失败'
  }
}

async function ship(order: ShopOrder) {
  const trackingNo = window.prompt('输入快递单号', 'SF' + Date.now())
  if (!trackingNo) return
  try {
    await api.post(`/api/v1/admin/shop/orders/${order.id}/ship`, { trackingNo })
    message.value = '已发货'
    await load()
  } catch (e) {
    error.value = e instanceof Error ? e.message : '操作失败'
  }
}

async function complete(order: ShopOrder) {
  try {
    await api.post(`/api/v1/admin/shop/orders/${order.id}/complete`, {})
    message.value = '订单已完成'
    await load()
  } catch (e) {
    error.value = e instanceof Error ? e.message : '操作失败'
  }
}

async function refund(order: ShopOrder) {
  try {
    await api.post(`/api/v1/admin/shop/orders/${order.id}/refund`, {})
    message.value = '已退款，库存已恢复'
    await load()
  } catch (e) {
    error.value = e instanceof Error ? e.message : '操作失败'
  }
}

onMounted(load)
</script>

<template>
  <AppShell>
    <div class="page-head">
      <div>
        <p class="eyebrow">SHOP ADMIN</p>
        <h1>商品管理</h1>
        <p class="muted">商品、库存、订单、发货、退款。</p>
      </div>
      <div class="page-actions">
        <button class="chip" :class="{ active: tab === 'products' }" type="button" @click="tab = 'products'">商品</button>
        <button class="chip" :class="{ active: tab === 'orders' }" type="button" @click="tab = 'orders'">订单</button>
      </div>
    </div>

    <p v-if="error" class="alert">{{ error }}</p>
    <p v-if="message" class="alert success">{{ message }}</p>

    <template v-if="tab === 'products'">
      <section class="panel">
        <div class="panel-head"><h2>商品列表</h2><span class="muted">{{ products.length }}</span></div>
        <div v-if="loading" class="empty">加载中…</div>
        <div v-else-if="products.length === 0" class="empty">还没有商品。</div>
        <div v-else class="table-wrap">
          <table class="table">
            <thead><tr><th>商品</th><th>价格</th><th>库存</th><th>状态</th><th>操作</th></tr></thead>
            <tbody>
              <tr v-for="product in products" :key="product.id">
                <td><strong>{{ product.name }}</strong><span class="muted block">{{ product.description }}</span></td>
                <td>¥{{ product.price }}</td>
                <td>{{ product.stock }}</td>
                <td><span class="badge">{{ statusLabel(product.status) }}</span></td>
                <td><button class="text-link" type="button" @click="toggleProduct(product)">{{ product.status === 'ACTIVE' ? '下架' : '上架' }}</button>
                  <button class="text-link" type="button" @click="editProduct(product)">编辑</button></td>
              </tr>
            </tbody>
          </table>
        </div>
      </section>

      <section class="panel" style="margin-top: 16px">
        <div class="panel-head"><h2>新建商品</h2></div>
        <form class="form" @submit.prevent="createProduct">
          <div class="field-row">
            <label class="field"><span>名称</span><input v-model="form.name" required /></label>
            <label class="field"><span>价格</span><input v-model.number="form.price" type="number" min="0" step="0.01" required /></label>
          </div>
          <label class="field"><span>描述</span><textarea v-model="form.description" rows="2"></textarea></label>
          <div class="field-row">
            <label class="field"><span>封面 URL</span><input v-model="form.cover" placeholder="/uploads/..." /></label>
            <label class="field"><span>库存</span><input v-model.number="form.stock" type="number" min="0" required /></label>
          </div>
          <button class="primary-button" type="submit">创建商品</button>
        </form>
      </section>
    </template>

    <section v-if="tab === 'orders'" class="panel">
      <div class="panel-head"><h2>订单列表</h2><span class="muted">{{ orders.length }}</span></div>
      <div v-if="loading" class="empty">加载中…</div>
      <div v-else-if="orders.length === 0" class="empty">还没有订单。</div>
      <div v-else class="table-wrap">
        <table class="table">
          <thead><tr><th>订单</th><th>商品</th><th>金额</th><th>状态</th><th>收货</th><th>操作</th></tr></thead>
          <tbody>
            <tr v-for="order in orders" :key="order.id">
              <td>#{{ order.id }}<span class="muted block">{{ formatDate(order.createdAt) }}</span></td>
              <td>#{{ order.productId }} × {{ order.quantity }}</td>
              <td>¥{{ order.amount }}</td>
              <td><span class="badge">{{ statusLabel(order.status) }}</span><span v-if="order.trackingNo" class="muted block">{{ order.trackingNo }}</span></td>
              <td class="muted">{{ order.contactName }}<br />{{ order.address }}</td>
              <td>
                <div class="row-actions">
                  <button v-if="order.status === 'PENDING'" class="text-link" type="button" @click="markPaid(order)">确认支付</button>
                  <button v-if="order.status === 'PAID'" class="text-link" type="button" @click="ship(order)">发货</button>
                  <button v-if="order.status === 'SHIPPED'" class="text-link" type="button" @click="complete(order)">完成</button>
                  <button v-if="order.status === 'PAID' || order.status === 'SHIPPED'" class="text-link" type="button" @click="refund(order)">退款</button>
                </div>
              </td>
            </tr>
          </tbody>
        </table>
      </div>
    </section>
  </AppShell>
</template>
