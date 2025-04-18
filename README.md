# JDUI
A Kotlin library for effortlessly creating stateful and interactive messages in Discord with 0 boilerplate

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
> This library is a work in progress. API breakage may (and probably will) happen!

## Features
* Easily create isolated message "views" (as we will call them from now on) and make them interactive with 0 boilerplate
* Store a Base65536 encoded and encrypted representation of the state of your views inside the `custom_id` field of components, or optionally in a database. The library does it for you
* Makes use of Discords "Components V2", an improved way to creating structured messages with even more options for bot developers
* Dependency injection using global and per-view context

## Installation
WIP