[maven-central]: https://img.shields.io/maven-central/v/dev.xirado/jdui-core?color=blue
[jitpack]: https://img.shields.io/badge/Snapshots-JitPack-blue

[![maven-central][]](https://img.shields.io/maven-central/v/dev.xirado/jdui-core)
[![jitpack][]](https://jitpack.io/#dev.xirado/jdui-core)

# JDUI
A Kotlin library for effortlessly creating stateful and interactive messages in Discord using JDA with 0 boilerplate

```kt
fun counterView() = view {
    var counter: Int by state(0)

    compose {
        +text("## Counter: $counter")
        +row {
            +button(ButtonStyle.SECONDARY, "Increment") {
                counter += 1
                println("Button was pressed")
            }
        }
    }
}
```

> [!WARNING]  
> This library is a work in progress. Features are missing and API breakage is likely!

## Features
* Easily create isolated message "views" (as we will call them from now on) and make them interactive with 0 boilerplate
* Store a Base65536 encoded and encrypted representation of the state of your views inside the `custom_id` field of components, or optionally in a database. The library does it for you
* Makes use of Discords "Components V2", an improved way to creating structured messages with even more options for bot developers
* Dependency injection using global and per-view context

## Installation
[![maven-central][]](https://img.shields.io/maven-central/v/dev.xirado/jdui-core)
[![jitpack][]](https://jitpack.io/#dev.xirado/jdui-core)

### Gradle

```gradle
repositories {
    mavenCentral()
}

dependencies {
    implementation("dev.xirado:jdui-core:0.2.0")
    implementation("net.dv8tion:JDA:6.0.0-rc.1")
}
```

### Maven

```xml
<dependency>
    <groupId>dev.xirado</groupId>
    <artifactId>jdui-core</artifactId>
    <version>0.1.1</version>
</dependency>
<dependency>
    <groupId>net.dv8tion</groupId>
    <artifactId>JDA</artifactId>
    <version>6.0.0-rc.1</version>
</dependency>
```
