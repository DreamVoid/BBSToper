<div align="center">
    <h1> BBSToper </h1>
</div>

BBSToper 是由 R-Josef 开发、DreamVoid 重制的插件，能够奖励为服务器宣传帖顶帖的玩家。

## 使用方法

1. 下载插件并将其放置在 `plugins` 文件夹；
2. 启动一次服务器，然后关闭；
3. 复制 MCBBS 帖子的 ID，并将其填入配置文件中；
4. 再次启动服务器，即可享受插件的功能。

## 命令&权限
### 命令

| 命令 | 描述 | 权限 |
| ----- | ----- | ----- |
| `/bbstoper` `/poster` `/bt` `/toper` | 插件主命令 / 显示 GUI | `bbstoper.user` |
| `/bbstoper help` | 显示帮助信息 | `bbstoper.user` |
| `/bbstoper binding <MCBBS论坛ID>` | 绑定论坛账号, 注意这里是ID不是uid | `bbstoper.command.binding` |
| `/bbstoper reward` | 领取奖励 | `bbstoper.command.reward` |
| `/bbstoper testreward [模式]` | 测试奖励, 模式: `normal` `incentive` `offday` | `bbstoper.command.testreward` |
| `/bbstoper list <页数>` | 列出所有顶帖者 | `bbstoper.command.list` |
| `/bbstoper top <页数>` | 按照顶贴次数列排名出所有已绑定玩家 | `bbstoper.command.top` |
| `/bbstoper check bbsid <论坛ID>` | 查看一个论坛id的绑定者 | `bbstoper.command.check` |
| `/bbstoper check player <玩家ID>` | 查看一个玩家绑定的论坛id | `bbstoper.command.check` |
| `/bbstoper delete player <玩家ID>` | 删除一个玩家的数据 | `bbstoper.command.delete` |
| `/bbstoper reload` | 重载插件 | `bbstoper.command.reload` |

### 权限

| 权限 | 描述 | 默认 |
| ----- | ----- | ----- |
| `bbstoper.user` | 玩家默认权限 | TRUE |
| `bbstoper.command.binding` | 允许使用 /bbstoper binding | OP/`bbstoper.user` |
| `bbstoper.command.reward` | 允许使用 /bbstoper reward | OP/`bbstoper.user` |
| `bbstoper.admin` | 管理员默认权限 | OP |
| `bbstoper.command.testreward` | 允许使用 /bbstoper testreward | OP/`bbstoper.admin` |
| `bbstoper.command.list` | 允许使用 /bbstoper list | OP/`bbstoper.admin`|
| `bbstoper.command.top` | 允许使用 /bbstoper top | OP/`bbstoper.admin` |
| `bbstoper.command.check` | 允许使用 /bbstoper check | OP/`bbstoper.admin` |
| `bbstoper.command.delete` | 允许使用 /bbstoper delete | OP/`bbstoper.admin` |
| `bbstoper.command.reload` | 允许使用 /bbstoper reload | OP/`bbstoper.admin` |
| `bbstoper.bypassquerycooldown` | 绕过查询冷却 | FALSE |

## PlaceholderAPI 占位符

本插件提供了一些基于PlaceHolderAPI的占位符(Placeholders), 要想使用这些占位符就必须在服务端上同时运行了[PlaceholderAPI](https://github.com/PlaceholderAPI/PlaceholderAPI)插件.

| 占位符                | 描述                                               |
| --------------------- | -------------------------------------------------- |
| %bbstoper_bbsid%      | 当前玩家的MCBBS用户名                              |
| %bbstoper_posttimes%  | 当前玩家的顶贴次数                                 |
| %bbstoper_pageid%     | 宣传贴的id                                         |
| %bbstoper_pageurl%    | 宣传贴的链接                                       |
| %bbstoper_lastpost%   | 上一次被顶贴的时间                                 |
| %bbstoper_top_<序号>% | 顶贴排行第"序号"个的顶贴信息, 例: %bbstoper_top_1% |

## 许可

[LICENSE](./LICENSE)

## 使用的依赖库

1. [Jsoup](https://jsoup.org/)
2. [bStats](https://bstats.org/)
3. [PlaceHolderAPI](https://github.com/PlaceholderAPI/PlaceholderAPI)
