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

async function csrfToken() {
  if (!readCookie('XSRF-TOKEN')) {
    // 登出後補發
    await fetch('/api/auth/me')
  }
  return readCookie('XSRF-TOKEN')
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
    headers['X-XSRF-TOKEN'] = await csrfToken()
  }

  const response = await fetch(`/api${path}`, { method, headers, body: payload, keepalive })
  const text = await response.text()
  const data = text && response.headers.get('content-type')?.includes('json') ? JSON.parse(text) : null

  if (!response.ok) {
    // 登入除外
    if (response.status === 401 && !/^(\/admin)?\/auth\//.test(path)) {
      unauthorizedHandler(path)
    }
    throw new HttpError(response.status, data?.message || `請求失敗（${response.status}）`)
  }
  return data
}
