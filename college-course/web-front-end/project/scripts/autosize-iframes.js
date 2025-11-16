/**
 * iframe 自适应高度
 * @param {Element} iframe 需要自适应高度的 iframe
 */
function changeFrameHeight(iframe) {
  if (iframe == undefined) return
  iframe.height = iframe.contentDocument.body.scrollHeight + 1
}

let iframes = document.getElementsByClassName('m-container')
for (const iframe of iframes) {
  iframe.onload = function () {
    changeFrameHeight(this)
  }
}
