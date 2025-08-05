# DogPixel Velocity v1.0 发布版

## 介绍： 
Hi,there.我是NetScn，今天给大家公开发布一款我服务器即将投入使用的核心
DogVelocity。这是一款开源的核心，增加了多数使用功能。开源并鸣谢PaperMC团队。

## 功能： 
①去除CheckStyle，以便于中国开发者编辑
##
②常见log汉化
##
③增添功能如Alert `(/alert <msg>)`
##
etc.

欢迎大家使用！
##
## 以下为PaperMC原文
[![Build Status](https://img.shields.io/github/actions/workflow/status/PaperMC/Velocity/gradle.yml)](https://papermc.io/downloads/velocity)
[![Join our Discord](https://img.shields.io/discord/289587909051416579.svg?logo=discord&label=)](https://discord.gg/papermc)

一款拥有无与伦比的服务器支持、可扩展性和灵活性的Minecraft服务器代理。

Velocity采用GPLv3许可证发布。

## 目标

* 构建易于理解的代码库，尽可能合理地遵循Java项目的最佳实践规范
* 高性能：单代理可支持数千名玩家同时在线
* 全新设计的API体系，基于灵活强大的理念从头构建，避免其他代理的设计缺陷与次优方案
* 为Paper、Sponge、Fabric和Forge提供原生支持（其他服务端实现也可运行，但我们重点保障这些平台的兼容性）
  
## 构建指南

Velocity使用 [Gradle](https://gradle.org) 构建。推荐使用封装脚本 (`./gradlew`) ，我们的CI系统也采用此方式构建。

执行 `./gradlew build` 命令即可完成完整构建流程。

## 运行说明

构建完成后，可从 `proxy/build/libs` 目录获取 `-all` 版本JAR文件。Velocity将自动生成默认配置文件供您进行配置调整。

您也可以直接通过下载页面获取代理服务端JAR文件。 [downloads](https://papermc.io/downloads/velocity)
