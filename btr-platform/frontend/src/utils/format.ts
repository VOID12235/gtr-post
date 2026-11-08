export function formatDate(value?: string): string {
  if (!value) return '—'
  return value.replace('T', ' ').slice(0, 16)
}

export function formatDay(value?: string): string {
  if (!value) return '—'
  return value.slice(0, 10)
}

export function statusLabel(status?: string): string {
  switch (status) {
    case 'DRAFT':
      return '筹备中'
    case 'SIGNUP':
      return '报名中'
    case 'SUBMISSION':
      return '作品提交中'
    case 'JUDGING':
      return '评审中'
    case 'PUBLISHED':
      return '已公示'
    case 'FINISHED':
      return '已结束'
    case 'ACTIVE':
      return '正常'
    case 'APPROVED':
      return '已通过'
    case 'SUBMITTED':
      return '已提交'
    case 'ISSUED':
      return '已签发'
    case 'PENDING_PAYMENT':
      return '待支付'
    case 'PENDING':
      return '待处理'
    case 'PAID':
      return '已支付'
    case 'REFUNDED':
      return '已退款'
    case 'CANCELED':
      return '已取消'
    case 'UNFUNDED':
      return '未托管'
    case 'FUNDED':
      return '已托管'
    case 'RELEASED':
      return '已发放'
    case 'EXPIRED':
      return '已过期'
    case 'INACTIVE':
      return '已下架'
    case 'SHIPPED':
      return '已发货'
    case 'COMPLETED':
      return '已完成'
    default:
      return status || '—'
  }
}

export function statusClass(status?: string): string {
  if (status === 'SIGNUP' || status === 'SUBMISSION' || status === 'ACTIVE' || status === 'APPROVED') return 'ok'
  if (status === 'JUDGING') return 'info'
  if (status === 'PUBLISHED') return 'warn'
  if (status === 'FINISHED') return 'muted'
  return ''
}
