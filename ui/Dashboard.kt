package ui

import javafx.application.Application
import javafx.scene.Scene
import javafx.scene.control.Label
import javafx.scene.layout.VBox
import javafx.stage.Stage

class Dashboard : Application() {
    override fun start(primaryStage: Stage) {
        val vbox = VBox()
        vbox.children.addAll(
            Label("Dashboard"),
            Label("Analytics: "),
            Label("AI Models: "),
            Label("Cloud Management: ")
        )

        primaryStage.title = "Dashboard"
        primaryStage.scene = Scene(vbox, 800, 600)
        primaryStage.show()
    }
}

fun main(args: Array<String>) {
    Application.launch(Dashboard::class.java, *args)
}
