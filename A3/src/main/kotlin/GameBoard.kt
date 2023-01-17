import javafx.animation.*
import javafx.beans.property.SimpleObjectProperty
import javafx.geometry.Insets
import javafx.scene.image.Image
import javafx.scene.layout.Background
import javafx.scene.layout.BackgroundFill
import javafx.scene.layout.CornerRadii
import javafx.scene.layout.HBox
import javafx.scene.layout.Pane
import javafx.scene.media.Media
import javafx.scene.media.MediaPlayer
import javafx.scene.paint.Color
import javafx.scene.paint.ImagePattern
import javafx.scene.shape.Rectangle
import javafx.scene.text.*
import java.nio.file.Paths
import kotlin.math.ceil
import kotlin.math.log10


// 随机生成的player的位置会到右边超出board的地方
class GameBoard(space: SpaceInvaders, level:Int, score:Int) : Pane() {
    // Generate magic numbers
    private val PLAYER_SPEED = 3.0
    private val PLAYER_BULLET_SPEED = 6.0
    private var ENEMY_SPEED = 0.5
    private val ENEMY_VERTICAL_SPEED = 10.0
    private val CHARACTER_WIDTH = 70.0
    private val CHARACTER_HEIGHT = 45.0
    private val PLAYERBULLET_WIDTH = 5.0
    private val BULLET_WIDTH = 10.0
    private val BULLET_HEIGHT = 30.0
    private var ENEMY_BULLET_SPEED = 2.5
    private var ENEMY_BULLET_NUM = 10
    private var ENEMY_FIRE_RATE = 500

    // Add value to detect the movement of player
    enum class STATE { LEFT, RIGHT, STOP }
    var playerLeftState = STATE.STOP
    var playerRightState = STATE.STOP
    var enemyReverse = false

    // The player
    var player:Rectangle = Rectangle()
    // The bullet list storing the player's bullet
    var playerBulletList:MutableList<Rectangle> = mutableListOf()
    // Enemy list
    private val enemyList = mutableMapOf<Rectangle, Int>()
    // Enemy bullet list
    private val enemyBulletList:MutableList<Rectangle> = mutableListOf()

    // Record the score of the player
    var playerScore = 0
    // Record the current game level
    var gameLevel = 1
    // Record player's number of lives
    var playerLive = 3

    // Hardcode and define the top bar
    var topBar = HBox()
    // Timer of the game board
    var gameTimer: AnimationTimer? = null

    // Record the last shoot time of player
    private var playerLastShoot:Long = System.currentTimeMillis()
    // Record the last shoot time of enemy
    private var enemyLastShoot:Long = System.currentTimeMillis()
    // Record the last movement sound generated for enemy move
    private var enemyLastMove:Long = System.currentTimeMillis()

    // The reference of the current Game Board class
    private val gameBoard = this

    // Detected whether the game is end
    private var spaceInvaders:SpaceInvaders? = null
    enum class GAME { NONE, WIN, LOSE }

    init {
        // Update the field spaceInvaders, gameLevel and playerScore
        spaceInvaders = space
        gameLevel = level
        playerScore += score

        // Update the original Enemy Speed depending on the current level
        ENEMY_SPEED += (gameLevel - 1) * 1.5

        // Update the enemy bullet number depending on the current level
        ENEMY_BULLET_NUM -= (gameLevel - 1) * 2
        ENEMY_BULLET_SPEED += (gameLevel - 1) * 0.5
        ENEMY_FIRE_RATE -= (gameLevel - 1) * 100

        // Generate Top bar at the top of current game board to show score, lives and level
        val text1 = Text(" ".repeat(10) + "Score $playerScore")
        val text2 = Text("Lives $playerLive     Level $gameLevel  ")
        text1.font = Font.font("Verdana", FontWeight.BOLD, 30.0)
        text2.font = Font.font("Verdana", FontWeight.BOLD, 30.0)
        text1.fill = Color.WHITE
        text2.fill = Color.WHITE
        topBar.children.addAll(text1, text2)
        topBar.spacing = 900.0
        gameBoard.children.add(topBar)

        // Generate and add player in the game board
        player = Rectangle(20.0, 1000.0, CHARACTER_WIDTH, CHARACTER_HEIGHT)
        player.fill = ImagePattern(Image("images/player.png"))
        gameBoard.children.add(player)

        // Generate 10 columns and 5 rows of different enemies in the game board
        for(i in 0..4) {
            for(j in 0..9) {
                // Generate enemy type 3 in first row
                val startX = j * (70.0 + 15.0)
                val startY = 50.0 + i * (45.0 + 10.0)
                    when (i) {
                    0 -> {
                        val currEnemy = Rectangle(startX, startY, CHARACTER_WIDTH, CHARACTER_HEIGHT)
                        currEnemy.fill = ImagePattern(Image("images/enemy3.png"))
                        gameBoard.children.add(currEnemy)
                        enemyList[currEnemy] = 3
                    }
                    1, 2 -> {
                        val currEnemy = Rectangle(startX, startY, CHARACTER_WIDTH, CHARACTER_HEIGHT)
                        currEnemy.fill = ImagePattern(Image("images/enemy2.png"))
                        gameBoard.children.add(currEnemy)
                        enemyList[currEnemy] = 2
                    }
                    else -> {
                        val currEnemy = Rectangle(startX, startY, CHARACTER_WIDTH, CHARACTER_HEIGHT)
                        currEnemy.fill = ImagePattern(Image("images/enemy1.png"))
                        gameBoard.children.add(currEnemy)
                        enemyList[currEnemy] = 1
                    }
                }
            }
        }

        // Generate single timer for handling everything
        gameTimer = object : AnimationTimer() {
            override fun handle(now: Long) {
                // Implement the movement of the player
                playerMoveHandler()

                // Implement player bullet shooting
                playerBulletHandler()

                // Implement enemy bullet shooting
                enemyBulletHandler()

                // Add movement to the enemies
                enemyHandler()

                // Check whether game is end
                determineResult()
            }
        }

        gameBoard.background = Background(
            BackgroundFill(Color.BLACK, CornerRadii.EMPTY, Insets.EMPTY)
        )
    }

    // The function will update the top bar
    private fun updateTopBar() {
        topBar.children.clear()
        val text1 = Text(" ".repeat(10) + "Score $playerScore")
        val text2 = Text("Lives $playerLive     Level $gameLevel  ")
        text1.font = Font.font("Verdana", FontWeight.BOLD, 30.0)
        text2.font = Font.font("Verdana", FontWeight.BOLD, 30.0)
        text1.fill = Color.WHITE
        text2.fill = Color.WHITE
        if (playerScore == 0) {
            topBar.spacing = 900.0
        } else {
            topBar.spacing = 900.0 - (log10(playerScore.toDouble())).toInt() * 22.0
        }
        topBar.children.addAll(text1, text2)
    }

    // The function will let one of the enemy fire a missile
    private fun enemyFire() {
        if (enemyList.isNotEmpty()) {
            // Randomly find an enemy
            val i = (0 until enemyList.size).random()
            val enemy = enemyList.entries.elementAt(i).key
            val enemyNum = enemyList.entries.elementAt(i).value

            // Update the previous shoot time
            enemyLastShoot = System.currentTimeMillis()

            // Generate the player bullet and add it to the pane
            val enemyBullet = Rectangle(enemy.x + 32.5, enemy.y + CHARACTER_HEIGHT, BULLET_WIDTH, BULLET_HEIGHT)
            enemyBullet.fill = ImagePattern(Image("images/bullet$enemyNum.png"))
            gameBoard.children.add(enemyBullet)
            enemyBulletList.add(enemyBullet)
        }
    }

    // The function will handle the player's shooting
    fun playerFire() {
        // Restrict the rate of the fire of player to 2 per second
        val fireRate = System.currentTimeMillis() - playerLastShoot
        if (fireRate > 500) {
            // Update the previous shoot time
            playerLastShoot = System.currentTimeMillis()

            // Generate the player bullet and add it to the pane
            val playerBullet = Rectangle(player.x + 32.5, player.y - BULLET_HEIGHT, PLAYERBULLET_WIDTH, BULLET_HEIGHT)
            playerBullet.fill = ImagePattern(Image("images/player_bullet.png"))
            gameBoard.children.add(playerBullet)
            playerBulletList.add(playerBullet)

            // Generate and play media sound for player shoot
            val playerShoot = MediaPlayer(Media(Paths.get("src/main/resources/sounds/shoot.wav").toUri().toString()))
            playerShoot.play()
        }
    }

    // The function will check whether the current location is being occupied by any shapes in game board
    private fun isOccupied(mx: Double, my: Double):Boolean {
        for ((enemy, enemyNum) in enemyList) {
            if (enemy.contains(mx, my)) {
                return true
            }
        }

        for (enemyBullet in enemyBulletList) {
            if (enemyBullet.contains(mx, my)) {
                return true
            }
        }

        return false
    }

    // The function will respawn the player's ship in a random, unoccupied location on the screen with y-axis equals 1000.0
    private fun respawnPlayer() {
        // Generate and add player in the game board
        var playerX = (20..1400).random().toDouble()
        while (isOccupied(playerX, 1000.0)) {
            playerX = (20..1400).random().toDouble()
        }
        player = Rectangle(playerX, 1000.0, CHARACTER_WIDTH, CHARACTER_HEIGHT)
        player.fill = ImagePattern(Image("images/player.png"))
        gameBoard.children.add(player)
    }

    // The function will handle the movement of the player
    fun playerMoveHandler() {
        if (playerLeftState == STATE.LEFT) {
            if (player.x > 0.0) {
                player.x -= PLAYER_SPEED
            }
        }

        if (playerRightState == STATE.RIGHT) {
            if (player.x < 1400.0) {
                player.x += PLAYER_SPEED
            }
        }
    }

    // The function will handle the movement and hit-test of player's bullet
    fun playerBulletHandler() {
        // Check for each player's bullet if the current bullet list is not empty
        if (playerBulletList.isNotEmpty()) {
            // Retrieve iterator
            val bulletIter = playerBulletList.iterator()
            while (bulletIter.hasNext()) {
                val currentBullet = bulletIter.next()
                var hitEnemy = false

                // Check whether the bullet hit the enemy, if so remove both bullet and enemy
                if (enemyList.isNotEmpty()) {
                    for ((enemy, enemyNum) in enemyList) {
                        if (enemy.contains(currentBullet.x, currentBullet.y)) {
                            // Add the score by the current enemy's score if the enemy is hit
                            playerScore += enemyNum * 10
                            updateTopBar()

                            // Generate sound for enemy hit
                            val enemyHit = MediaPlayer(Media(Paths.get("src/main/resources/sounds/invaderkilled.wav").toUri().toString()))
                            enemyHit.play()
                            ENEMY_SPEED += 0.1

                            // Remove the enemy from the game board and enemy list
                            gameBoard.children.remove(enemy)
                            enemyList.remove(enemy)

                            // Remove the player bullet from the game board and bullet list
                            gameBoard.children.remove(currentBullet)
                            bulletIter.remove()
                            hitEnemy = true
                            break
                        }
                    }
                }

                // Check whether the bullet hit the enemy bullet, if so remove both bullets
                if (enemyBulletList.isNotEmpty()) {
                    for (enemyBullet in enemyBulletList) {
                        if (enemyBullet.contains(currentBullet.x, currentBullet.y)) {
                            // Remove both bullets from the game board and list
                            gameBoard.children.remove(enemyBullet)
                            enemyBulletList.remove(enemyBullet)

                            // Remove the player bullet from the game board and bullet list
                            gameBoard.children.remove(currentBullet)
                            bulletIter.remove()
                            hitEnemy = true
                            break
                        }
                    }
                }

                // If the bullet do not hit the enemy, move the bullet up, or remove bullet if it reaches the
                //   top of the play board
                if (!hitEnemy) {
                    if (currentBullet.y > 10.0) {
                        currentBullet.y -= PLAYER_BULLET_SPEED
                    } else {
                        // If the bullet reach the top of the game board, remove it as well
                        gameBoard.children.remove(currentBullet)
                        bulletIter.remove()
                    }
                }
            }
        }
    }

    // The function will handle the movement and hit-test of enemy's bullet
    fun enemyBulletHandler() {
        // Check for each enemy's bullet if the current enemy bullet list is not empty
        if (enemyBulletList.isNotEmpty()) {
            // Retrieve iterator
            val bulletIter = enemyBulletList.iterator()
            while (bulletIter.hasNext()) {
                val currentBullet = bulletIter.next()
                var hitPlayer = false

                // Check whether the bullet hit the player, if so reduce the player lives by 1
                //   regenerate a new player at the start point
                if (player.contains(currentBullet.x, currentBullet.y)) {
                    playerLive -= 1
                    updateTopBar()

                    // Generate sound for player explosion
                    val playerExplosion = MediaPlayer(Media(Paths.get("src/main/resources/sounds/explosion.wav").toUri().toString()))
                    playerExplosion.play()

                    // Remove the enemy from the game board and enemy list
                    gameBoard.children.remove(player)

                    // Remove the enemy bullet from the game board and bullet list
                    gameBoard.children.remove(currentBullet)
                    bulletIter.remove()
                    hitPlayer = true

                    // Generate and add player in the game board
                    respawnPlayer()
                }

                // If the bullet do not hit the player, move the bullet down, or remove bullet if it reaches the
                //   bottom of the play board
                if (!hitPlayer) {
                    if (currentBullet.y < 1090.0) {
                        currentBullet.y += ENEMY_BULLET_SPEED
                    } else {
                        // If the bullet reach the top of the game board, remove it as well
                        gameBoard.children.remove(currentBullet)
                        bulletIter.remove()
                    }
                } else {
                    // Pause for 1 second and then regenerate the player and start the game again
                    val currentTime = System.currentTimeMillis()
                    gameTimer?.stop()
                    while (System.currentTimeMillis() - currentTime < 1000) {}
                    gameTimer?.start()
                }
            }
        }
    }

    // The function will play media of the enemy movement depending on enemy's speed
    private fun playEnemyMoveMedia() {
        val moveRate = System.currentTimeMillis() - enemyLastMove
        if (ENEMY_SPEED < 1.5) {
            if (moveRate > 1000) {
                enemyLastMove = System.currentTimeMillis()
                val enemyMove = MediaPlayer(Media(Paths.get("src/main/resources/sounds/fastinvader1.wav").toUri().toString()))
                enemyMove.play()
            }
        } else if (ENEMY_SPEED < 3) {
            if (moveRate > 700) {
                enemyLastMove = System.currentTimeMillis()
                val enemyMove = MediaPlayer(Media(Paths.get("src/main/resources/sounds/fastinvader2.wav").toUri().toString()))
                enemyMove.play()
            }
        } else if (ENEMY_SPEED < 5) {
            if (moveRate > 500) {
                enemyLastMove = System.currentTimeMillis()
                val enemyMove = MediaPlayer(Media(Paths.get("src/main/resources/sounds/fastinvader3.wav").toUri().toString()))
                enemyMove.play()
            }
        } else {
            if (moveRate > 300) {
                enemyLastMove = System.currentTimeMillis()
                val enemyMove = MediaPlayer(Media(Paths.get("src/main/resources/sounds/fastinvader4.wav").toUri().toString()))
                enemyMove.play()
            }
        }
    }

    // Add the movement and hit test of the enemy
    fun enemyHandler() {
        if (enemyReverse) {
            for ((enemy, enemyNum) in enemyList) {
                if (enemy.x > 0.0) {
                    enemy.x -= ENEMY_SPEED
                } else {
                    enemyReverse = false
                    break
                }
            }

            // Randomly generate a missile if the fire rate of enemy not exceed 2 per second
            val random = (0..5).random()
            val fireRate = ceil((enemyList.size / ENEMY_BULLET_NUM).toDouble()) + 1
            if (System.currentTimeMillis() - enemyLastShoot > ENEMY_FIRE_RATE && random == 0 &&
                enemyBulletList.size < fireRate) {
                enemyLastShoot = System.currentTimeMillis()
                enemyFire()
            }

            // Generate sound for enemy move
            playEnemyMoveMedia()

            // If the enemy reach the edge of the pane, we move the enemies down and fire a missile
            if (!enemyReverse) {
                for ((enemy, enemyNum) in enemyList) {
                    enemy.y += ENEMY_VERTICAL_SPEED
                    if (enemy.y >= 1055.0) {
                        gameTimer?.stop()
                        spaceInvaders?.gameResult(playerScore, GAME.LOSE, gameLevel)
                    }
                }
                enemyLastShoot = System.currentTimeMillis()
                enemyFire()
            }
        } else {
            for ((enemy, enemyNum) in enemyList) {
                if (enemy.x < 1400.0) {
                    enemy.x += ENEMY_SPEED
                } else {
                    enemy.x += ENEMY_SPEED
                    enemyReverse = true
                }
            }

            // Randomly generate a missile if the fire rate of enemy not exceed 2 per second
            val random = (0..5).random()
            val fireRate = ceil((enemyList.size / ENEMY_BULLET_NUM).toDouble())
            if (System.currentTimeMillis() - enemyLastShoot > ENEMY_FIRE_RATE && random == 0 &&
                enemyBulletList.size < fireRate) {
                enemyLastShoot = System.currentTimeMillis()
                enemyFire()
            }

            // Generate sound for enemy move
            playEnemyMoveMedia()

            // If the enemy reach the edge of the pane, we move the enemies down and fire a missile
            if (enemyReverse) {
                for ((enemy, enemyNum) in enemyList) {
                    enemy.y += ENEMY_VERTICAL_SPEED
                    if (enemy.y >= 1055.0) {
                        gameTimer?.stop()
                        spaceInvaders?.gameResult(playerScore, GAME.LOSE, gameLevel)
                    }
                }
                enemyLastShoot = System.currentTimeMillis()
                enemyFire()
            }
        }

        for ((enemy, enemyNum) in enemyList) {
            if (player.contains(enemy.x, enemy.y) || player.contains(enemy.x + CHARACTER_WIDTH, enemy.y)
                || player.contains(enemy.x, enemy.y + CHARACTER_HEIGHT) ||
                player.contains(enemy.x + CHARACTER_WIDTH, enemy.y + CHARACTER_HEIGHT)) {
                // Kill the player if the current enemy contact the player
                playerLive -= 1
                updateTopBar()

                // Generate sound for player explosion
                val playerExplosion = MediaPlayer(Media(Paths.get("src/main/resources/sounds/explosion.wav").toUri().toString()))
                playerExplosion.play()

                // Remove the enemy from the game board and enemy list
                gameBoard.children.remove(player)

                respawnPlayer()

                // Pause for 1 second and then regenerate the player and start the game again
                val currentTime = System.currentTimeMillis()
                gameTimer?.stop()
                while (System.currentTimeMillis() - currentTime < 1000) {}
                gameTimer?.start()
            }
        }
    }

    // The function will start the game
    fun startGame() {
        gameTimer?.start()
    }

    // The function will determine the result of the game if player destroys all the enemy, an enemy reach the
    //   bottom of the game board and the player loses all lives
    fun determineResult() {
        if (playerLive <= 0) {
            gameTimer?.stop()
            spaceInvaders?.gameResult(playerScore, GAME.LOSE, gameLevel)
        }

        if (enemyList.isEmpty()) {
            gameTimer?.stop()
            spaceInvaders?.gameResult(playerScore, GAME.WIN, gameLevel)
        }
    }
}