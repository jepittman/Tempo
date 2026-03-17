package com.jepittman.tempo

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform