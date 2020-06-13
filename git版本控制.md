# Git版本管理
## 本地库
>常用命令

|命令名称|命令作用|
|---|---|
|git init|初始化本地库|
|git config --global user.name 用户名|设置用户签名的用户名部分|
|git config --global user.email 邮箱|设置用户签名的邮箱部分|
|git status|查看本地库状态|
|git add 文件名|添加到暂存区|
|git commit -m "日志信息" 文件名|提交到本地库|
|git reflog|查看历史记录|
|git reset --hard 版本号|版本穿梭|
+ 案例
1. 到文件下初始化
```shell
git init
```
2. 添加文件hive复习.md
```shell
git add hive复习.md
```
3. 合并添加和提交，以及注释
```shell
git commit -am hive的笔记
```
4. 查看日志
```shell
git log
#一行显示
git log --pretty=oneline
#查看历史操作
git reflog
```
5. 版本穿梭
```shell
#回退到上一个版本
git reset --hard HEAD^
#回退n次操作
git  reset  --hard HEAD~n
#版本跳转
git reset --hard 版本号
```
6. 撤销操作
```shell
#未add，未commit
git checkout -- 文件名
#已add，未commit（恢复到add之前）
git add 文件名
git reset
```


>分支操作
|命令名称|作用|
|---|---|
|git branch 分支名|创建分支|
|git branch -v|查看分支|
|git checkout 分支名|切换分支|
|git merge 分支名|把指定的分支合并到当前分支上|

+ 示例
1. 创建，切换分支
```shell
#创建分支 bra1
git branch bra1
#查看分支
git branch -v
#切换分支
git checkout bra1
```
2. 合并分支(把“目标分支”合并到“当前分支”)
```shell
#把bra1合并到master分支 -- 当前分支是master
git merge hotfx
#把master合并到bra1分支 -- 当前分支是bra1
git merge master
```
+ 合并冲突 -- 合并分支时，两个分支在同一个文件的同一个位置有两套完全不同的修改
>解决合并冲突
1. 编辑有冲突的文件，删除特殊符号，（选择使用内容）
2. 添加到暂存区（git add）
3. 执行提交（注意：使用git commit命令时不能带文件名）
>避免冲突
+ 容易冲突的操作方式
1. 多个人同时操作了同一个文件
2. 一个人一直写不提交
3. 修改之前不更新最新代码
4. 提交之前不更新最新代码
5. 擅自修改同事代码
+ 减少冲突的操作方式
1. 养成良好的操作习惯，先`pull`在修改,修改完立即`commit`和`push`
2. 一定要确保自己正在修改的文件是最新版本的
3. 各自开发各自的模块
4. 如果要修改公共文件，一定要先确认有没有人正在修改
5. 下班前一定要提交代码,上班第一件事拉取最新代码
6. 一定不要擅自修改同事的代码
## GitHub远程库
github是一个git项目托管网站，主要提供基于git的版本托管服务
### 本地项目推送到远程库
+ 创建本地项目
```shell
#创建项目文件夹
mkdir demo
cd demo
#初始化项目
git init
#填写用户名
git config user.name "用户名
#填写用户email
git config user.email "用户email"
# 提交temp到本地库
git commit -am "提交temp" temp.md
```
+ 登录github创建远端仓库
![7.png](https://i.loli.net/2020/06/12/2A3It7SNJygKHWT.png)
+ 本地项目推送到远程库
1. 增加远程地址 -- git remote add <远端代号> <远端地址>
```shell
#<远端代号>是指远程连接的代号,一般用origin(可自定义)<远端地址>默认远程连接的url
git remote add origin git remote add origin https://github.com/tinypumpkin/test1.git
```
2. 推送至远端 --git push <远端代号> <本地分支名称>
```shell
git push -u origin master
```
3. 远程库不为空 -- 远程库与本地同步
```shell
#本地同步
git pull --rebase origin master
#推送
git push -u origin master
```
+ 删除库 -- 打开项目目录-->settings-->拉到底-->Delete this repository
![8.png](https://i.loli.net/2020/06/12/QX5j971Pv2mDVol.png)
+ GitHub上克隆一个项目
```shell
git clone 项目地址
```
## IDEA 
### IDEA集成Git
#####  idea中创建git工程 
+ 打开idea-->VCS-->Import into Version Control-->Create Git Repository
+ 提交到本地库 ==>工程文件下右键Git-->Commit Directory  
+ 创建分支==>Git-->Repository-->Branches
+ 切换分支==>左下角Git-->Checkout ‘分支’
+ 合并分支==>左下角Git-->切换当前分支，合并目标分支到当前-->Merge into Current
+ 合并冲突==>选择Accept Yours或者“Accept Theirs来解决冲突。
+ 详细修改冲突内容则
1. 点击Close-->此时IDEA会提示Resolve,点击Resolve-->Merge手动修改
2. 根据需要调整完成后点击“Save changes and finish merging”-->提交本地库

### IDEA连接GitHub
## 工作流
Gitflow工作流通过为功能开发、发布准备和维护设立了独立的分支，让发布迭代过程更流畅。严格的分支模型也为大型项目提供了一些非常必要的结构。
![6.png](https://i.loli.net/2020/06/12/k75x4rReABGMp98.png)
### 分支种类
#### 主干分支 master
主要负责管理正在运行的生产环境代码。永远保持与正在运行的生产环境完全一致。
#### 开发分支   develop
主要负责管理正在开发过程中的代码。一般情况下应该是最新的代码。 
#### bug修理分支  hotfix
要负责管理生产环境下出现的紧急修复的代码。 从主干分支分出，修理完毕并测试上线后，并回主干分支。并回后，视情况可以删除该分支。
#### 发布版本分支  release
较大的版本上线前，会从开发分支中分出发布版本分支，进行最后阶段的集成测试。该版本上线后，会合并到主干分支。生产环境运行一段阶段较稳定后可以视情况删除。
#### 功能分支    feature
为了不影响较短周期的开发工作，一般把中长期开发模块，会从开发分支中独立出来。 开发完成后会合并到开发分支。
