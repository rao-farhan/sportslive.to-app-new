package to.sports.live

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform