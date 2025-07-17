class Student {
  selected = false
  no = STUDENTS.length + 1
  #studentNo = 0
  set studentNo(studentNo) {
    this.#studentNo = parseInt(studentNo)
  }
  get studentNo() {
    return this.#studentNo
  }
  name
  college
  major
  #grade = 0
  set grade(grade) {
    this.#grade = parseInt(grade)
  }
  get grade() {
    return this.#grade
  }
  #classNo = 0
  set classNo(classNo) {
    this.#classNo = parseInt(classNo)
  }
  get classNo() {
    return this.#classNo
  }
  #age = 0
  set age(age) {
    this.#age = parseInt(age)
  }
  get age() {
    return this.#age
  }
}
// 存放学生信息
const STUDENTS = []

// 记录全选状态
const SELECTED_STATUS = []
// 统计选中数量
let checkedCount = 0

const TBODY = document.getElementById('table').children[0]

// 当前页码
let currentPage = 1

/**
 * 更新页面显示的数据
 */
function update() {
  // 学生数量
  document.getElementById('student-count-viewer').textContent = STUDENTS.length

  //清空表格
  for (let i = 1; i < TBODY.children.length; ) {
    TBODY.removeChild(TBODY.children[i])
  }

  // 学生信息有变化后检查页码
  let largestPage = parseInt((STUDENTS.length - 1) / 10) + 1
  if (currentPage > largestPage) {
    currentPage = largestPage
  }
  document.getElementById('current-page-viewer').textContent = currentPage

  // 恢复全选状态
  document.getElementById('full-page-selected-box').checked =
    SELECTED_STATUS[currentPage - 1]

  // 展示当前页的学生信息
  for (
    let i = (currentPage - 1) * 10;
    i < currentPage * 10 && i < STUDENTS.length;
    i++
  ) {
    let tr = document.createElement('tr')
    tr.id = 'student-' + i + 1
    tr.className = 'info'
    tr.setAttribute('data-no', i + 1) // 记录学生的序号
    tr.innerHTML = `<td>
            <input
                type="checkbox" ${STUDENTS[i].selected ? 'checked' : ''} />
        </td>
        <td>${STUDENTS[i].no}</td>
        <td>${STUDENTS[i].studentNo}</td>
        <td>${STUDENTS[i].name}</td>
        <td>${STUDENTS[i].college}</td>
        <td>${STUDENTS[i].major}</td>
        <td>${STUDENTS[i].grade}</td>
        <td>${STUDENTS[i].classNo}</td>
        <td>${STUDENTS[i].age}</td>
        <td>
            <a class="review" href="javascript:void(0);">查看</a>
            <a class="modify" href="javascript:void(0);">修改</a>
        </td>`
    TBODY.append(tr)
  }
}

const DIALOG_CONTAINER = document.getElementById('dialog-container')
let dialogType // -> [ 'add', 'review', 'modify' ]

// 新增学生
document.getElementById('add-btn').addEventListener('click', function () {
  dialogType = 'add'
  DIALOG_CONTAINER.style.display = 'flex'
})

// 删除学生
document.getElementById('remove-btn').addEventListener('click', function () {
  if (checkedCount === 0) {
    alert('无选中的学生信息')
    return
  }
  if (!confirm('确认删除？')) {
    return
  }
  // 删除所有被选中的学生，并且调整序号
  for (let i = 0; i < STUDENTS.length; ) {
    if (STUDENTS[i].selected) {
      STUDENTS.splice(i, 1)
    } else {
      STUDENTS[i].no = ++i
    }
  }
  // 全部页面取消全选
  for (let i = 0; i < SELECTED_STATUS.length; i++) {
    SELECTED_STATUS[i] = false
  }
  checkedCount = 0

  update()
})

// 全选
document
  .getElementById('full-page-selected-box')
  .addEventListener('click', function () {
    // 判断选择状态
    SELECTED_STATUS[currentPage - 1] = !SELECTED_STATUS[currentPage - 1]
    // 对当前页的学生进行选中或取消选中
    for (
      let i = (currentPage - 1) * 10;
      i < currentPage * 10 && i < STUDENTS.length;
      i++
    ) {
      // 如果状态更新了，才操作赋值
      if (STUDENTS[i].selected !== SELECTED_STATUS[currentPage - 1]) {
        STUDENTS[i].selected = SELECTED_STATUS[currentPage - 1]
        // 状态更新了，选中的数量也要更新
        if (SELECTED_STATUS[currentPage - 1]) {
          checkedCount++
        } else {
          checkedCount--
        }
      }
    }

    update()
  })

const DIALOG_CONTENT = document.getElementById('dialog-content')
// 当前操作（查看/修改）的学生（数组元素）
let currentOperatedStudent

// 事件委托，包括单条学生信息的选中、查看和修改
TBODY.addEventListener('click', function (e) {
  if (
    (e.target.tagName !== 'INPUT' ||
      e.target.id === 'full-page-selected-box') &&
    e.target.tagName !== 'A'
  ) {
    return
  }

  // 选中的学生（数组元素）
  currentOperatedStudent =
    STUDENTS[e.target.parentNode.parentNode.getAttribute('data-no') - 1]

  if (e.target.tagName === 'INPUT') {
    // 选中
    let selected = !currentOperatedStudent.selected
    currentOperatedStudent.selected = selected

    if (selected) {
      checkedCount++
    } else {
      checkedCount--
    }

    update()
  } else if (e.target.tagName === 'A') {
    // 查看、修改
    DIALOG_CONTAINER.style.display = 'flex'
    DIALOG_CONTENT.children[0].children[1].value =
      currentOperatedStudent.studentNo
    DIALOG_CONTENT.children[1].children[1].value = currentOperatedStudent.name
    DIALOG_CONTENT.children[2].children[1].value =
      currentOperatedStudent.college
    DIALOG_CONTENT.children[3].children[1].value = currentOperatedStudent.major
    DIALOG_CONTENT.children[4].children[1].value = currentOperatedStudent.grade
    DIALOG_CONTENT.children[5].children[1].value =
      currentOperatedStudent.classNo
    DIALOG_CONTENT.children[6].children[1].value = currentOperatedStudent.age
    if (e.target.className === 'review') {
      // 查看
      dialogType = 'review'
      Array.from(DIALOG_CONTENT.children).forEach((element) => {
        element.children[1].disabled = true
      })
    } else if (e.target.className === 'modify') {
      // 修改
      dialogType = 'modify'
    }
  }
})

// 展示上一页信息
document.getElementById('last-page-btn').addEventListener('click', function () {
  if (currentPage === 1) {
    alert('已经是第一页！')
  } else {
    currentPage--
    update()
  }
})

// 展示下一页信息
document.getElementById('next-page-btn').addEventListener('click', function () {
  if (currentPage === parseInt((STUDENTS.length - 1) / 10) + 1) {
    alert('已经是最后一页！')
  } else {
    currentPage++
    update()
  }
})

// 对话框确认
document.getElementById('confirm-btn').addEventListener('click', function () {
  if (dialogType === 'add') {
    // 添加类型的对话框，即对话框用于获取添加的学生信息
    let newStudent = new Student()

    let studentNo = DIALOG_CONTENT.children[0].children[1].value
    if (studentNo.length !== 11) {
      alert('学号必须为 11 位')
      return
    }
    newStudent.studentNo = studentNo

    let name = DIALOG_CONTENT.children[1].children[1].value
    if (name.length === 0) {
      alert('姓名不可为空')
      return
    }
    newStudent.name = name

    let college = DIALOG_CONTENT.children[2].children[1].value
    if (college.length === 0) {
      alert('学院不可为空')
      return
    }
    newStudent.college = college

    let major = DIALOG_CONTENT.children[3].children[1].value
    if (major.length === 0) {
      alert('专业不可为空')
      return
    }
    newStudent.major = major

    let grade = DIALOG_CONTENT.children[4].children[1].value
    if (grade.length === 0) {
      alert('年级不可为空')
      return
    }
    newStudent.grade = grade

    let classNo = DIALOG_CONTENT.children[5].children[1].value
    if (classNo.length === 0) {
      alert('班级不可为空')
      return
    }
    newStudent.classNo = classNo

    let age = DIALOG_CONTENT.children[6].children[1].value
    if (age.length === 0) {
      alert('年龄不可为空')
      return
    }
    newStudent.age = age

    STUDENTS.push(newStudent)

    update()
  } else if (dialogType === 'review') {
    // 查看类型的对话框，不可修改值
    Array.from(DIALOG_CONTENT.children).forEach((element) => {
      element.children[1].disabled = false
    })
  } else if (dialogType === 'modify') {
    // 修改类型的对话框
    let studentNo = DIALOG_CONTENT.children[0].children[1].value
    if (studentNo.length !== 11) {
      alert('学号必须为 11 位')
      return
    }
    currentOperatedStudent.studentNo = studentNo

    let name = DIALOG_CONTENT.children[1].children[1].value
    if (name.length === 0) {
      alert('姓名不可为空')
      return
    }
    currentOperatedStudent.name = name

    let college = DIALOG_CONTENT.children[2].children[1].value
    if (college.length === 0) {
      alert('学院不可为空')
      return
    }
    currentOperatedStudent.college = college

    let major = DIALOG_CONTENT.children[3].children[1].value
    if (major.length === 0) {
      alert('专业不可为空')
      return
    }
    currentOperatedStudent.major = major

    let grade = DIALOG_CONTENT.children[4].children[1].value
    if (grade.length === 0) {
      alert('年级不可为空')
      return
    }
    currentOperatedStudent.grade = grade

    let classNo = DIALOG_CONTENT.children[5].children[1].value
    if (classNo.length === 0) {
      alert('班级不可为空')
      return
    }
    currentOperatedStudent.classNo = classNo

    let age = DIALOG_CONTENT.children[6].children[1].value
    if (age.length === 0) {
      alert('年龄不可为空')
      return
    }
    currentOperatedStudent.age = age

    update()
  }

  // 操作完成后重置对话框，清空值
  for (const content of DIALOG_CONTENT.children) {
    content.children[1].value = ''
  }
  DIALOG_CONTAINER.style.display = 'none'
})

// 取消对话框
document.getElementById('cancel-btn').addEventListener('click', function () {
  // 操作完成后重置对话框
  // 查看类型的对话框，重置禁用状态
  if (dialogType === 'review') {
    Array.from(DIALOG_CONTENT.children).forEach((element) => {
      element.children[1].disabled = false
    })
  }
  // 清空值
  for (const content of DIALOG_CONTENT.children) {
    content.children[1].value = ''
  }
  DIALOG_CONTAINER.style.display = 'none'
})

/**
 * * 测试方法
 */
function test() {
  STUDENTS[0] = new Student()
  STUDENTS[0].studentNo = 12123020400
  STUDENTS[0].name = 'Waoap'
  STUDENTS[0].college = '人工智能学院'
  STUDENTS[0].major = '软件工程'
  STUDENTS[0].grade = 2021
  STUDENTS[0].classNo = 4
  STUDENTS[0].age = 20
  for (let i = 1; i <= 25; i++) {
    let newStudent = new Student()
    newStudent.studentNo = STUDENTS[0].studentNo + i
    newStudent.name = Math.random().toString(36).slice(-8)
    newStudent.college = STUDENTS[0].college
    newStudent.major = STUDENTS[0].major
    newStudent.grade = STUDENTS[0].grade
    newStudent.classNo = STUDENTS[0].classNo - parseInt(Math.random() * 4)
    newStudent.age = STUDENTS[0].age + parseInt(Math.random() * 4)
    STUDENTS.push(newStudent)
  }
  update()
}
test()
