$(function () {
  function checkInfo(element, type, msg) {
    switch (type) {
      case 'forbid':
        $(element)
          .removeClass('pass')
          .addClass('forbid')
          .siblings()
          .remove('div.prompt')
          .parent()
          .append(
            '<div class="prompt forbid">' +
              '<img src="images/forbid.svg" width="15px" height="15px" />' +
              msg +
              '</div>'
          )
        break
      case 'pass':
        $(element)
          .removeClass('forbid')
          .addClass('pass')
          .siblings()
          .remove('div.prompt')
          .parent()
          .append(
            '<div class="prompt pass">' +
              '<img src="images/pass.svg" width="15px" height="15px" />' +
              msg +
              '</div>'
          )
    }
  }

  $('input#username').change(function () {
    if ($.trim(this.value) == '' || $.trim(this.value).length < 6) {
      checkInfo(this, 'forbid', '请输入至少6位的用户名')
    } else {
      checkInfo(this, 'pass', '输入正确')
    }
  })

  $('input#email').change(function () {
    if (
      /^[a-z0-9][\w\.\-]*@[a-z0-9\-]+(\.[a-z]{2,5}){1,2}$/i.test(
        $(this).val()
      ) == false
    ) {
      checkInfo($(this), 'forbid', '请输入正确的E-Mail地址')
    } else {
      checkInfo(this, 'pass', '输入正确')
    }
  })

  $('input#submit').click(function () {
    if ($('div.prompt.pass').length !== $('div.input-box.required').length) {
      alert('验证不通过')
      return
    } else {
      alert('注册成功，密码已发到你的邮箱，请查收。')
    }
  })

  $('input#reset').click(function () {
    $('input#username')
      .removeClass('forbid pass')
      .siblings()
      .remove('div.prompt')
    $('input#email').removeClass('forbid pass').siblings().remove('div.prompt')
  })
})
