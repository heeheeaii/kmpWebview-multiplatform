import org.jetbrains.compose.desktop.application.dsl.TargetFormat

plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.compose.multiplatorm)
}

kotlin {
    jvm{
        jvmToolchain(17)
    }
    sourceSets {
        val jvmMain by getting
        jvmMain.dependencies {
            implementation("androidx.collection:collection:1.5.0-alpha05") // 或 1.4.0 稳定版
            implementation(compose.desktop.currentOs)
            implementation(project(":sample:shared"))
            implementation("org.jogamp.gluegen:gluegen-rt:2.5.0")
            implementation("org.jogamp.jogl:jogl-all:2.5.0")

            // java.lang.NoClassDefFoundError: androidx/lifecycle/ViewModelStoreOwner
            implementation("org.jetbrains.androidx.lifecycle:lifecycle-viewmodel-compose-desktop:2.8.0")
            implementation("androidx.lifecycle:lifecycle-runtime:2.8.0")


        }
    }
}

compose.desktop {
    application {
        mainClass = "MainKt"

        nativeDistributions {
            // dmg mac, msi win, deb debian
            targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb)
            packageName = "KotlinMultiplatformComposeDesktopApplication"
            packageVersion = "1.0.0"
            includeAllModules = true
        }
        buildTypes.release.proguard {
            configurationFiles.from("compose-desktop.pro")
        }
    }
}

afterEvaluate {
    tasks.withType<JavaExec> {
        jvmArgs("--add-opens", "java.desktop/sun.awt=ALL-UNNAMED")
        jvmArgs("--add-opens", "java.desktop/java.awt.peer=ALL-UNNAMED")

        if (System.getProperty("os.name").contains("Mac")) {
            jvmArgs("--add-opens", "java.desktop/sun.awt=ALL-UNNAMED")
            jvmArgs("--add-opens", "java.desktop/sun.lwawt=ALL-UNNAMED")
            jvmArgs("--add-opens", "java.desktop/sun.lwawt.macosx=ALL-UNNAMED")
        }
    }
}
