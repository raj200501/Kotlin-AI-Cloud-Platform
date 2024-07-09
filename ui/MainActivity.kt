package ui

import javafx.application.Application
import javafx.scene.Scene
import javafx.scene.control.Label
import javafx.scene.layout.StackPane
import javafx.stage.Stage

class MainActivity : Application() {
    override fun start(primaryStage: Stage) {
        val root = StackPane()
        root.children.add(Label("Welcome to Kotlin-AI-Cloud-Platform"))

        primaryStage.title = "Main Activity"
        primaryStage.scene = Scene(root, 800, 600)
        primaryStage.show()
    }
}

fun main(args: Array<String>) {
    Application.launch(MainActivity::class.java, *args)
}
