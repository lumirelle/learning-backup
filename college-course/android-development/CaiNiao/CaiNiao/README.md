# 菜鸟驿站 App

## 关键功能

- 使用用户名称做登录，首页将展示使用用户名寄件或收件的所有包裹
- 用户可以使用该用户名寄件
- 用户可以使用该用户名生成身份码
- 用户可以点击查看首页的包裹详情

## 分工

- UI
    - T：首页、我的界面，Java 代码写在 main 包下
    - W：包裹的详情界面，Java 代码写在 parceldetails 包下
    - L：寄包裹界面、身份码界面，Java 代码写在 parceldispatch、identitycode 包下
- 其它
    - 共同开发

## 项目结构和规范

### Java 代码

所有的 Java 代码在 org.seagulls.cainiao 包下按以下规则分类：

- common：存放三个人的界面中通用的 Java 代码

    - bean：存放数据类以及相关的表示类型的枚举类
    - config：存放配置类
    - listener.impl：存放包含界面跳转逻辑的监听器实现
    - server：存放服务器访问相关的类
    - util：存放工具类

- identitycode：存放身份码界面相关的 Java 代码
- main：存放首页、学生专享、我的界面相关的 Java 代码
- parceldispatch：存放寄包裹界面相关的 Java 代码
- parceldetails：存放包裹详情界面相关的 Java diamond

### 图片文件

所有的图片文件、背景文件等在 res/drawable 文件夹下按以下规则命名，防止重复：

<图片类型>\_<图片名称>\_<图片描述>.<后缀名>

如：

- 一张 xml 格式的表示首页的图标，它的颜色是黑色，大小
  24dp，应该命名为：图标_首页_黑色_24dp.xml，即 ic_home_black_24dp.xml
- 一张 xml 格式的 search view 控件的背景图，应该命名为：背景_search_view.xml，即
  bg_search_view.xml

图片类型英文对照表：

| 图片类型 | 英文 |
|------|----|
| 图标   | ic |
| 背景   | bg |

图片描述是在图片名称不足以区分图片的时候做补充的，或是给图片做备注时才需要的。

### 布局文件

所有的布局文件在 res/layout 文件夹下按以下规则命名，防止重复：

<布局作用的类型>_<所属的界面名称>_<布局描述>.xml

如：

- 一个作用于 MainActivity 的布局应该命名为：activity_main.xml
- 一个作用于 MineFragment 的布局应该命名为：fragment_mine.xml
- 一个作用于 AbcActivity 中的 ListView 或 RecycleView，用来展示物流进度
  LogisticsProgress 的 Item 的布局应该命名为：item_logistics_progress_activity_abc.xml

布局描述意义同图片描述。

### 其它文件

举一反三！！

## 注意事项

每个人开发一个分支，每写完一部分功能（或一个模块）记得提交代码并推送到远程仓库，合并到主分支。

为了最后好合并和课设的质量高，每个人可以 **修改的** Java
代码范围规定是“分工”里面分配的包 + 存放监听器实现的 common.interfaces.listener
包，Java 代码也要写在分配的包里。

至于图片、布局文件一样只能修改、删除自己创建的，当然，res 目录下资源文件是可以共用的，看着来咯。

## 开发进度

完成项：

- [x] 包裹数据类 Parcel
  以及相关联的数据类、枚举类：`.common.bean`，`.common.bean.type`
- [x] 保存配置的配置类：`.common.config`
- [x] 包含界面跳转逻辑的监听器实现：`.common.listenter.impl`
- [x] 
  服务器请求器以及访问服务器时使用的回调方法接口：`.common.server`，`.common.server.callback`
- [x] 工具类 `.common.util`
- [x] Main Activity 部分 `.main.MainActivity`
- [x] 首页 Home Fragment 部分 `.main.home`
- [x] 包裹详情 Activity 部分 `.parceldetails`
- [x] 寄件 Activity 部分 `.parceldispatch`
- [x] 身份码 Activity 部分 `.identitycode`
- [x] 
  浅色/深色主题适配 `/res/values/themes.xml`，`/res/values-night/themes.xml`，`/res/values/attr.xml`
  和 `/res/values/colors.xml`

目标完成项（按从上到下的顺序完成）：

- [ ] 登录注册支持
- [ ] 我的 Mine Fragment 部分 `.main.mine`

## 测试进度

NULL
