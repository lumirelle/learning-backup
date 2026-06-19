/** 极简 GFM → HTML（escape 优先，支持标题/列表/粗体/行内代码/链接） */

function escapeHtml(s: string) {
  return s
    .replace(/&/g, '&amp;')
    .replace(/</g, '&lt;')
    .replace(/>/g, '&gt;')
    .replace(/"/g, '&quot;')
}

function inlineFormat(s: string) {
  return s
    .replace(/`([^`]+)`/g, '<code>$1</code>')
    .replace(/\*\*([^*]+)\*\*/g, '<strong>$1</strong>')
    .replace(/\[([^\]]+)\]\(([^)]+)\)/g, '<a href="$2" target="_blank" rel="noopener">$1</a>')
}

export function renderMarkdown(md: string | null | undefined): string {
  if (!md)
    return ''
  const lines = escapeHtml(md).split('\n')
  const out: string[] = []
  let para: string[] = []
  let list: string[] = []

  function flushPara() {
    if (para.length) {
      out.push(`<p>${inlineFormat(para.join(' '))}</p>`)
      para = []
    }
  }
  function flushList() {
    if (list.length) {
      out.push(`<ul>${list.map(li => `<li>${inlineFormat(li)}</li>`).join('')}</ul>`)
      list = []
    }
  }

  for (const raw of lines) {
    const line = raw.trim()
    if (!line) {
      flushPara()
      flushList()
      continue
    }
    const h = line.match(/^(#{1,4})\s+(.*)/)
    if (h) {
      flushPara()
      flushList()
      const lvl = Math.min(h[1].length + 1, 5)
      out.push(`<h${lvl}>${inlineFormat(h[2])}</h${lvl}>`)
      continue
    }
    const li = line.match(/^[-*]\s+(.*)/)
    if (li) {
      flushPara()
      list.push(li[1])
      continue
    }
    flushList()
    para.push(line)
  }
  flushPara()
  flushList()
  return out.join('')
}
