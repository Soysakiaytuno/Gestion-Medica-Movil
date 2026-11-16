
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.compose) apply false

    // Plugin de Google Services para Firebase
    alias(libs.plugins.google.services) apply false
}
