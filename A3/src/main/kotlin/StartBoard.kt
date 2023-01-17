import javafx.geometry.Insets
import javafx.geometry.Pos
import javafx.scene.image.Image
import javafx.scene.image.ImageView
import javafx.scene.layout.*
import javafx.scene.text.Font
import javafx.scene.text.FontWeight
import javafx.scene.text.Text

class StartBoard : BorderPane() {
    init {
        // Generate the logo and add it to the vbox
        val logoImage = ImageView(Image("images/logo.png"))
        logoImage.fitHeight = 300.0
        logoImage.fitWidth = 700.0

        // Generate Text for displaying instructions
        val innerVbox1 = VBox()
        val text1 = Text("Instructions")
        text1.font = Font.font("Verdana", FontWeight.BOLD, 50.0)
        innerVbox1.alignment = Pos.CENTER
        innerVbox1.spacing = 40.0

        // Generate Vbox for displaying details of instructions
        val innerVBox2 = VBox()
        val font = Font.font("Verdana", 30.0)
        val text2 = Text("ENTER - Start Game")
        text2.font = font
        val text3 = Text("A or \uD83E\uDC90, D or \uD83E\uDC92 - Move ship left or right")
        text3.font = font
        val text4 = Text("SPACE - Fire!")
        text4.font = font
        val text5 = Text("Q - Quit Game")
        text5.font = font
        val text6 = Text("1 or 2 or 3 - Start Game at a specific level")
        text6.font = font
        innerVBox2.alignment = Pos.CENTER
        innerVBox2.spacing = 10.0
        innerVBox2.children.addAll(text2, text3, text4, text5, text6)

        val text7 = Text("Implemented by Boyang Li for CS 349, University of Waterloo, S22")
        text7.font = Font.font("Verdana", 18.0)

        innerVbox1.children.addAll(text1, innerVBox2)
        this.top = logoImage
//        setMargin(logoImage, Insets(12.0, 12.0, 12.0, 12.0))
        setAlignment(logoImage, Pos.CENTER)
        this.center = innerVbox1
        this.bottom = text7
        setAlignment(text7, Pos.CENTER)

        background = Background(
            BackgroundFill(javafx.scene.paint.Color.WHITE, CornerRadii.EMPTY, Insets.EMPTY))
    }
}