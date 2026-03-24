plugins {
    id("java")
    id("org.jetbrains.intellij") version "1.17.2"
}

group = "com.dailyreport"
version = "1.0.3"

repositories {
    mavenCentral()
}

dependencies {
    // JGit for Git log parsing
    implementation("org.eclipse.jgit:org.eclipse.jgit:6.7.0.202309050840-r")
    
    // Apache Commons Lang3
    implementation("org.apache.commons:commons-lang3:3.13.0")
    
    // OkHttp for API calls
    implementation("com.squareup.okhttp3:okhttp:4.12.0")
    
    // Gson for JSON parsing
    implementation("com.google.code.gson:gson:2.10.1")
}

intellij {
    version.set("2022.3")
    type.set("IC") // IC = IDEA Community, IU = IDEA Ultimate
    plugins.set(listOf("Git4Idea"))
    downloadSources.set(true)
    updateSinceUntilBuild.set(false)
}

tasks {
    withType<JavaCompile> {
        sourceCompatibility = "11"
        targetCompatibility = "11"
        options.encoding = "UTF-8"
    }
    
    patchPluginXml {
        sinceBuild.set("223")
        untilBuild.set("")
    }
    
    buildSearchableOptions {
        enabled = false
    }
}