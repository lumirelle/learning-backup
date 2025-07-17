/**
 * 轮播图
 */
const IMAGES_BOX = document.getElementById('imgs-box')
const IMAGES_SIZE = IMAGES_BOX.childElementCount
const IMAGE_WIDTH = 1200

const PLAY = document.getElementById('play')
const PAUSE = document.getElementById('pause')

const MIN_INDEX = 0
const MAX_INDEX = IMAGES_SIZE - 1

let index = MIN_INDEX

/**
 * 在移动之前，预先准备图片
 * @param {String} orientation 图片移动方向
 */
function prepareImages(orientation = 'r2l') {
  switch (orientation) {
    case 'r2l':
      // 到达最后一个图片时
      if (index === MAX_INDEX) {
        IMAGES_BOX.append(IMAGES_BOX.children[0])
        index--
        IMAGES_BOX.style.left = -index * IMAGE_WIDTH + 'px'
      }
      break
    case 'l2r':
      // 到达第一个图片时
      if (index === MIN_INDEX) {
        IMAGES_BOX.prepend(IMAGES_BOX.children[IMAGES_SIZE - 1])
        index++
        IMAGES_BOX.style.left = -index * IMAGE_WIDTH + 'px'
      }
  }
}

/**
 * 移动图片
 * @param {String} orientation 图片移动方向
 */
function move(orientation = 'r2l') {
  switch (orientation) {
    // 图片从右向左移
    case 'r2l':
      if (IMAGES_BOX.onMoving === undefined) {
        // 准备图片
        prepareImages(orientation)

        // 开始移动
        IMAGES_BOX.onMoving = window.setInterval(function () {
          // 已经到达指定位置
          if (IMAGES_BOX.offsetLeft <= -(index + 1) * IMAGE_WIDTH) {
            window.clearInterval(IMAGES_BOX.onMoving)
            index++
            IMAGES_BOX.onMoving = undefined
          }

          // 还未到达指定位置
          else {
            IMAGES_BOX.style.left = IMAGES_BOX.offsetLeft - 5 + 'px'
          }
        }, 1)
      }
      break

    // 图片从左向右移
    case 'l2r':
      if (IMAGES_BOX.onMoving === undefined) {
        // 准备图片
        prepareImages(orientation)

        //开始移动
        IMAGES_BOX.onMoving = window.setInterval(function () {
          // 已经到达指定位置
          if (IMAGES_BOX.offsetLeft >= -(index - 1) * IMAGE_WIDTH) {
            window.clearInterval(IMAGES_BOX.onMoving)
            index--
            IMAGES_BOX.onMoving = undefined
          }

          // 还未到达指定位置
          else {
            IMAGES_BOX.style.left = IMAGES_BOX.offsetLeft + 5 + 'px'
          }
        }, 1)
      }
  }
}

/**
 * 自动播放轮播图
 */
const play = (PLAY.onclick = function () {
  if (IMAGES_BOX.onPlaying === undefined) {
    IMAGES_BOX.onPlaying = window.setInterval(move, 4000)
    PLAY.classList.remove('display')
    PAUSE.classList.add('display')
  }
})
play()

PAUSE.onclick = function () {
  if (IMAGES_BOX.onPlaying !== undefined) {
    window.clearInterval(IMAGES_BOX.onPlaying)
    IMAGES_BOX.onPlaying = undefined
    PAUSE.classList.remove('display')
    PLAY.classList.add('display')
  }
}

/**
 * 播放上一张图片
 */
document.getElementById('previous').addEventListener('click', function () {
  move('l2r')
})

/**
 * 播放下一张图片
 */
document.getElementById('next').addEventListener('click', function () {
  move('r2l')
})
