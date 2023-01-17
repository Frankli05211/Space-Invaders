import javafx.geometry.Insets
import javafx.geometry.Pos
import javafx.scene.layout.*
import javafx.scene.paint.Color
import javafx.scene.text.*

class EndBoard(isWin:Boolean, gameScore: Int): BorderPane() {
    init {
        // Generate the logo and add it to the vbox
        var gameResult:Text = Text()
        gameResult = if (isWin) {
            Text("YOU WIN!")
        } else {
            Text("YOU LOSE!")
        }
        gameResult.font = Font.font("Verdana", FontWeight.BOLD, 70.0)
        gameResult.fill = Color.WHITE

        // Generate Text for displaying instructions
        val innerVbox1 = VBox()
        val text1 = Text("YOUR SCORE: $gameScore")
        text1.font = Font.font("Verdana", FontWeight.BOLD, 50.0)
        text1.fill = Color.WHITE
        text1.textAlignment = TextAlignment.CENTER
        innerVbox1.isFillWidth = true
        innerVbox1.spacing = 100.0
        innerVbox1.alignment = Pos.CENTER

        // Generate Vbox for displaying details of instructions
        val textFlowPane = TextFlow()
        val font = Font.font("Verdana", 30.0)
        val text2 = Text("R - Restart Game\n")
        text2.font = font
        text2.fill = Color.WHITE
        val text3 = Text("A or \uD83E\uDC90, D or \uD83E\uDC92 - Move ship left or right\n")
        text3.font = font
        text3.fill = Color.WHITE
        val text4 = Text("SPACE - Fire!\n")
        text4.font = font
        text4.fill = Color.WHITE
        val text5 = Text("Q - Quit Game\n")
        text5.font = font
        text5.fill = Color.WHITE
        val text6 = Text("1 or 2 or 3 - Restart Game at a specific level")
        text6.font = font
        text6.fill = Color.WHITE
        textFlowPane.children.addAll(text2, text3, text4, text5, text6)
        textFlowPane.textAlignment = TextAlignment.CENTER
        textFlowPane.lineSpacing = 5.0

        innerVbox1.children.addAll(text1, textFlowPane)
        this.top = gameResult
        this.center = innerVbox1
        setAlignment(gameResult, Pos.CENTER)
        setMargin(gameResult, Insets(100.0, 0.0, 0.0, -375.0))
        setMargin(innerVbox1, Insets(-275.0, 0.0, 0.0, -375.0))

        background = Background(
            BackgroundFill(Color.BLACK, CornerRadii.EMPTY, Insets.EMPTY)
        )
    }
}