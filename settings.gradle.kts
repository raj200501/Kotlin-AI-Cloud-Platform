rootProject.name = "Kotlin-AI-Cloud-Platform"

/*
pluginManagement {
    repositories {
        gradlePluginPortal()
        mavenCentral()
    }
        maven {
            url = uri("http://repo1.maven.org/maven2")
            isAllowInsecureProtocol = true
        }
}
/*
pluginManagement {
    repositories {
        maven {
            url = uri("http://repo1.maven.org/maven2")
            isAllowInsecureProtocol = true
        }
    }
}
*/
pluginManagement.repositories {
    maven {
        url = uri("http://repo1.maven.org/maven2")
        isAllowInsecureProtocol = true
    }
}
*/
/*
pluginManagement {
    repositories {
        maven {
            url = uri("http://repo1.maven.org/maven2")
            isAllowInsecureProtocol = true
        }
    }
}
*/
pluginManagement {
    repositories {
        maven {
            url = uri("http://maven.aliyun.com/repository/public")
            isAllowInsecureProtocol = true
        }
    }
}
