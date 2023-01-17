import javafx.application.Application
import javafx.application.Platform
import javafx.event.EventHandler
import javafx.scene.Scene
import javafx.scene.input.KeyCode
import javafx.stage.Stage
import javafx.scene.input.KeyEvent
import javafx.scene.layout.BorderPane

class SpaceInvaders: Application() {
    // Fix the board size
    private val boardWidth = 1500.0
    private val boardHeight = 1100.0

    // Retrieve from SwitchScenes sample code
    // Set up different scene to switch
    private var startScene: Scene? = null
    private var sceneLevel1: Scene? = null
    private var sceneLevel2: Scene? = null
    private var sceneLevel3: Scene? = null
    private var endScene: Scene? = null

    var currStage: Stage? = null

    enum class SCENES {
        SCENE1, SCENE2, SCENE3, SCENE4, SCENE5
    }

    override fun start(stage: Stage?) {
        currStage = stage
        // Name the stage by using the current class name
        stage!!.title = this.javaClass.name

        // Start Menu of the game
        val startBoard = StartBoard()
        startScene = Scene(startBoard, boardWidth, boardHeight)

        // Pressing "ENTER" to switch to the play board
        startScene!!.onKeyPressed = EventHandler { event: KeyEvent ->
            when (event.code) {
                KeyCode.ENTER, KeyCode.DIGIT1, KeyCode.NUMPAD1 -> {
                    setScene(SCENES.SCENE2, 0, false)
                }
                KeyCode.DIGIT2, KeyCode.NUMPAD2 -> {
                    setScene(SCENES.SCENE3, 0, false)
                }
                KeyCode.DIGIT3, KeyCode.NUMPAD3 -> {
                    setScene(SCENES.SCENE4, 0, false)
                }
                KeyCode.Q -> {
                    Platform.exit()
                }
            }
        }

        stage.width = boardWidth
        stage.height = boardHeight
        stage.isResizable = false
        stage.scene = startScene
        stage.show()
    }

    // Function will set the current scene to the target scene, this function is
    //   retrieved from SwitchScenes Sample code
    private fun setScene(scene: SCENES?, score:Int, isWin: Boolean) {
        when (scene) {
            SCENES.SCENE1 -> {
                currStage?.scene = startScene
            }
            SCENES.SCENE2 -> {
                // Generate level 1 game board and set the current scene to that board
                val gameBoard = GameBoard(this, 1, score)
                sceneLevel1 = generateGameBoard(gameBoard)
                currStage?.scene = sceneLevel1
            }
            SCENES.SCENE3 -> {
                // Generate level 2 game board and set the current scene to that board
                val gameBoard = GameBoard(this, 2, score)
                sceneLevel2 = generateGameBoard(gameBoard)
                currStage?.scene = sceneLevel2
            }
            SCENES.SCENE4 -> {
                // Generate level 3 game board and set the current scene to that board
                val gameBoard = GameBoard(this, 3, score)
                sceneLevel3 = generateGameBoard(gameBoard)
                currStage?.scene = sceneLevel3
            }
            SCENES.SCENE5 -> {
                val endBoard = EndBoard(isWin, score)
                endScene = Scene(endBoard, boardWidth, boardHeight)

                endScene!!.onKeyPressed = EventHandler { event: KeyEvent ->
                    when (event.code) {
                        KeyCode.R, KeyCode.DIGIT1, KeyCode.NUMPAD1 -> {
                            setScene(SCENES.SCENE2, 0, false)
                        }
                        KeyCode.DIGIT2, KeyCode.NUMPAD2 -> {
                            setScene(SCENES.SCENE3, 0, false)
                        }
                        KeyCode.DIGIT3, KeyCode.NUMPAD3 -> {
                            setScene(SCENES.SCENE4, 0, false)
                        }
                        KeyCode.Q -> {
                            Platform.exit()
                        }
                    }
                }
                currStage?.scene = endScene
            }
            else -> return
        }
    }

    fun gameResult(playerScore: Int, state: GameBoard.GAME, level: Int) {
        when (state) {
            GameBoard.GAME.LOSE -> {
                setScene(SCENES.SCENE5, playerScore, false)
            }
            GameBoard.GAME.WIN -> {
                when (level) {
                    1 -> {
                        setScene(SCENES.SCENE3, playerScore, false)
                    }
                    2 -> {
                        setScene(SCENES.SCENE4, playerScore, false)
                    }
                    3 -> {
                        setScene(SCENES.SCENE5, playerScore, true)
                    }
                }
            }
        }
    }

    private fun generateGameBoard(gameBoard: GameBoard):Scene {
        val gameScene = Scene(gameBoard, boardWidth, boardHeight)

        // Add key pressed and released event handler for scene
        gameScene.onKeyPressed = EventHandler { event: KeyEvent ->
            if (event.code == KeyCode.A || event.code == KeyCode.LEFT) {
                gameBoard.playerLeftState = GameBoard.STATE.LEFT
            }

            if (event.code == KeyCode.D || event.code == KeyCode.RIGHT) {
                gameBoard.playerRightState = GameBoard.STATE.RIGHT
            }

            if (event.code == KeyCode.SPACE) {
                gameBoard.playerFire()
            }
        }

        gameScene.onKeyReleased = EventHandler { event: KeyEvent ->
            when (event.code) {
                KeyCode.A, KeyCode.LEFT -> gameBoard.playerLeftState = GameBoard.STATE.STOP
                KeyCode.D, KeyCode.RIGHT -> gameBoard.playerRightState = GameBoard.STATE.STOP
            }
        }
        gameBoard.startGame()

        return gameScene
    }
}