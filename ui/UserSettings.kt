package ui

import javafx.application.Application
import javafx.scene.Scene
import javafx.scene.control.Label
import javafx.scene.control.TextField
import javafx.scene.layout.VBox
import javafx.stage.Stage

class UserSettings : Application() {
    override fun start(primaryStage: Stage) {
        val vbox = VBox()
        vbox.children.addAll(
            Label("User Settings"),
            Label("Username: "),
            TextField(),
            Label("Email: "),
            TextField()
        )

        primaryStage.title = "User Settings"
        primaryStage.scene = Scene(vbox, 800, 600)
        primaryStage.show()
    }
}

fun main(args: Array<String>) {
    Application.launch(UserSettings::class.java, *args)
}
