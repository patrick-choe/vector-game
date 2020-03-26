# Vector Game

[![Build Status](https://travis-ci.org/patrick-mc/vector-game.svg?branch=master)](https://travis-ci.com/patrick-mc/vector-game)
[![Maintainability](https://api.codeclimate.com/v1/badges/b17b137a6f59132279ba/maintainability)](https://codeclimate.com/github/patrick-mc/vector-game/maintainability)
[![Codacy Badge](https://api.codacy.com/project/badge/Grade/99aee4fe51d64407a4f7eefe869d4250)](https://app.codacy.com/manual/patrickchoe/vector-game)
[![JitPack - Version](https://jitpack.io/v/patrick-mc/vector-game.svg)](https://jitpack.io/#patrick-mc/vector-game)
[![Twitch Status](https://img.shields.io/twitch/status/patrickkr)](https://twitch.tv/patrickkr)

[![GitHub License](https://img.shields.io/github/license/patrick-mc/vector-game)](https://github.com/patrick-mc/vector-game/blob/master/LICENSE)
[![GitHub Repository Size](https://img.shields.io/github/repo-size/patrick-mc/vector-game)](https://github.com/patrick-mc/vector-game)
[![GitHub Last Commit](https://img.shields.io/github/last-commit/patrick-mc/vector-game)](https://github.com/patrick-mc/vector-game/commits)
[![GitHub Release Date](https://img.shields.io/github/release-date/patrick-mc/vector-game)](https://github.com/patrick-mc/vector-game/releases)
[![GitHub Latest Release](https://img.shields.io/github/v/release/patrick-mc/vector-game)](https://github.com/patrick-mc/vector-game/releases)

> A vector game for education

> #### Features
> - Help players about the vector and the velocity in Minecraft
> - Now Supporting Bukkit 1.10 ~ 1.15.2, Noonmaru's [Tap](https://github.com/noonmaru/tap/releases/1.0.1/) plugin required when using versions under 1.12.2.
> - Must compile it with Kotlin, or use external Kotlin library. 
> - You can use [kotlin-1.3.70-lib.jar](https://github.com/noonmaru/kotlin-plugin/releases/download/1.3.70/kotlin-1.3.70-lib.jar) built by Noonmaru for external Kotlin library.

> #### How to use
> - Use '/vector' to toggle vector functions
> - Command alias: '/vc', '/vec', '/vctr', '/벡터'
> - Use '/vector config <config-name> <config-value> to change config.yml'
> - Use '/vector config reset' to reset config.yml

> #### Gradle (Groovy)
>```groovy
>allprojects {
>    repositories {
>        ...
>        mavenCentral()
>    }
>}
>
>
>...
>dependencies {
>    implementation 'com.github.patrick-mc:vector-game:1.1'
>}
>```

> #### Gradle (Kotlin DSL)
>```groovy
>allprojects {
>    repositories {
>        ...
>        mavenCentral()
>    }
>}
>
>...
>dependencies {
>    implementation("com.github.patrick-mc:vector-game:1.1")
>}
>```