/*
//import org.jetbrains.kotlin.gradle.tasks.KotlinCompile


/*
buildscript {
    repositories {
        maven {
            url = uri("http://repo.maven.apache.org/maven2")
            isAllowInsecureProtocol = true
        }
        maven {
            url = uri("http://maven.aliyun.com/repository/public")
            isAllowInsecureProtocol = true
        }
    }
    dependencies {
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:1.8.22")
    }
*/
}
//
apply(plugin = "org.jetbrains.kotlin.jvm")
apply(plugin = "java")
apply(plugin = "application")
/*
plugins {
    kotlin("jvm") version "1.8.22"
    application
*/
//}

//repositories {
/*
    mavenCentral()
}
*/

/*
kotlin {
    jvmToolchain(17)
*/
}

application {
    mainClass.set("app.StandaloneServices")
/*
}

sourceSets {
    main {
        kotlin.srcDirs("src/main/kotlin", "microservices")
    }
    test {
*/
/*
        kotlin.srcDirs("src/test/kotlin")
    }
}

dependencies {
    val ktorVersion = "1.6.8"
    val exposedVersion = "0.47.0"

    implementation(kotlin("stdlib"))
    implementation("io.ktor:ktor-server-netty:$ktorVersion")
    implementation("io.ktor:ktor-gson:$ktorVersion")
    implementation("ch.qos.logback:logback-classic:1.2.11")

    implementation("org.jetbrains.exposed:exposed-core:$exposedVersion")
    implementation("org.jetbrains.exposed:exposed-dao:$exposedVersion")
    implementation("org.jetbrains.exposed:exposed-jdbc:$exposedVersion")
    implementation("com.zaxxer:HikariCP:5.1.0")
*/
//    implementation("org.postgresql:postgresql:42.7.4")
//
//    testImplementation("io.ktor:ktor-server-tests:$ktorVersion")
//    testImplementation(kotlin("test"))
//}

tasks.withType<Test> {
    useJUnitPlatform()
}

//tasks.withType<KotlinCompile> {
//    kotlinOptions.jvmTarget = "17"
//}

repositories {
    maven {
        url = uri("http://repo1.maven.org/maven2")
        isAllowInsecureProtocol = true
    }
}
repositories {
    maven {
        url = uri("http://maven.aliyun.com/repository/public")
        isAllowInsecureProtocol = true
    }
}

sourceSets {
    main {
        java.srcDirs("src/main/java")
    }
    test {
        java.srcDirs("src/test/java")
    }
}

dependencies {
    // Using only JDK-provided libraries for the runnable demo
}

repositories {
    mavenLocal()
}
*/

plugins {
    java
    application
}

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

application {
    mainClass.set("app.StandaloneServices")
}

sourceSets {
    main {
        java.srcDirs("src/main/java")
    }
    test {
        java.srcDirs("src/test/java")
    }
}

dependencies {
    // No external dependencies required for the runnable demo
}

repositories {
    mavenLocal()
}

tasks.withType<Test> {
    useJUnitPlatform()
}
