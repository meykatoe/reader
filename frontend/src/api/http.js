export class HttpError extends Error {
  constructor(status, message) {
    super(message)
    this.status = status
  }
}

let unauthorizedHandler = () => {}

export function onUnauthorized(handler) {
  unauthorizedHandler = handler
}

function readCookie(name) {
  const match = document.cookie.match(new RegExp(`(?:^|; )${name}=([^;]*)`))
  return match ? decodeURIComponent(match[1]) : ''
}

export async function request(path, { method = 'GET', body, keepalive = false } = {}) {
  const headers = {}
  let payload = body
  if (body && !(body instanceof FormData)) {
    headers['Content-Type'] = 'application/json'
    payload = JSON.stringify(body)
  }
  if (method !== 'GET') {
    // 防跨站請求
    headers['X-XSRF-TOKEN'] = readCookie('XSRF-TOKEN')
  }

  const response = await fetch(`/api${path}`, { method, headers, body: payload, keepalive })
  const text = await response.text()
  const data = text && response.headers.get('content-type')?.includes('json') ? JSON.parse(text) : null

  if (!response.ok) {
    if (response.status === 401 && !path.startsWith('/auth/')) {
      unauthorizedHandler()
    }
    throw new HttpError(response.status, data?.message || `請求失敗（${response.status}）`)
  }
  return data
}
