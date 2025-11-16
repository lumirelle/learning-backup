import { useBase } from '/$/base'
import { config } from '/@/config'

export function useStream() {
  const { user } = useBase()
  let abortController: AbortController | null = null

  // 调用
  async function invoke({
    url,
    method = 'POST',
    data,
    cb,
  }: {
    url: string
    method?: string
    data?: any
    cb?(result: any): void
  }) {
    abortController = new AbortController()

    let cacheText = ''

    return fetch(config.baseUrl + url, {
      method,
      headers: {
        'Authorization': user.token,
        'Content-Type': 'application/json',
      },
      body: JSON.stringify(data),
      signal: abortController?.signal,
    })
      .then((res) => {
        if (res.body) {
          const reader = res.body.getReader()
          const decoder = new TextDecoder('utf-8')
          const stream = new ReadableStream({
            start(controller) {
              function push() {
                reader.read().then(({ done, value }) => {
                  if (done) {
                    controller.close()
                    return
                  }

                  let text = decoder.decode(value, { stream: true })

                  if (cb) {
                    if (cacheText) {
                      text = cacheText + text
                    }

                    if (text.indexOf('data:') == 0) {
                      text = `\n\n${text}`
                    }

                    try {
                      const arr = text
                        .split(/\n\ndata:/g)
                        .filter(Boolean)
                        .map(e => JSON.parse(e))

                      arr.forEach(cb)

                      cacheText = ''
                    }
                    catch (err) {
                      console.error('[parse text]', text)
                      cacheText = text
                    }
                  }

                  controller.enqueue(text)
                  push()
                })
              }
              push()
            },
          })

          return new Response(stream)
        }

        return res
      })
      .catch((err) => {
        console.error(err)
        throw err
      })
  }

  // 取消
  function cancel() {
    if (abortController) {
      abortController.abort()
      abortController = null
    }
  }

  return {
    invoke,
    cancel,
  }
}
