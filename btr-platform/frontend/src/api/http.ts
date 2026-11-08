type ApiEnvelope<T> = {
  code: number
  message: string
  data: T
}

const STORAGE_KEY = 'btr.auth'

function token(): string {
  const raw = localStorage.getItem(STORAGE_KEY)
  if (!raw) return ''
  try {
    return (JSON.parse(raw) as { token?: string }).token ?? ''
  } catch {
    return ''
  }
}

async function request<T>(path: string, options: RequestInit = {}): Promise<T> {
  const headers = new Headers(options.headers)
  headers.set('Content-Type', 'application/json')
  const current = token()
  if (current) {
    headers.set('Authorization', `Bearer ${current}`)
  }

  const response = await fetch(path, { ...options, headers })
  const body = (await response.json().catch(() => null)) as ApiEnvelope<T> | null

  if (!response.ok || !body || body.code !== 0) {
    if (response.status === 401) {
      localStorage.removeItem(STORAGE_KEY)
    }
    throw new Error(body?.message || `请求失败（HTTP ${response.status}）`)
  }

  return body.data
}

export const api = {
  get: <T>(path: string) => request<T>(path),
  post: <T>(path: string, body: unknown) => request<T>(path, {
    method: 'POST',
    body: JSON.stringify(body)
  }),
  put: <T>(path: string, body: unknown) => request<T>(path, {
    method: 'PUT',
    body: JSON.stringify(body)
  }),
  del: <T>(path: string) => request<T>(path, { method: 'DELETE' })
}

export async function uploadFile(file: File): Promise<import('./types').UploadFile> {
  const headers = new Headers()
  const current = token()
  if (current) {
    headers.set('Authorization', `Bearer ${current}`)
  }
  const form = new FormData()
  form.append('file', file)
  const response = await fetch('/api/v1/files', { method: 'POST', headers, body: form })
  const body = (await response.json().catch(() => null)) as ApiEnvelope<import('./types').UploadFile> | null
  if (!response.ok || !body || body.code !== 0) {
    throw new Error(body?.message || `上传失败（HTTP ${response.status}）`)
  }
  return body.data
}
