// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    // Объявляем плагин для Android-приложения.
    // `apply false` означает "сделать доступным, но не применять к самому проекту"
    id("com.android.application") version "8.4.1" apply false

    // Объявляем плагин для Kotlin в Android
    id("org.jetbrains.kotlin.android") version "1.9.23" apply false
}