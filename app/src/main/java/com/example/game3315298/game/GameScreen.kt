package com.example.game3315298.Game

import androidx.compose.ui.graphics.Path
import androidx.compose.runtime.Composable
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.LaunchedEffect
import kotlinx.coroutines.delay
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import kotlin.math.abs

// Definición de las clases de datos
data class Enemy(val x: Float, val y: Float)
data class Bullet(val x: Float, val y: Float, val color: Color)

private val bulletPalette = listOf(
    Color(0xFFFF595E), // Rojo
    Color(0xFFFFCA3A), // Amarillo
    Color(0xFF8AC926), // Verde
    Color(0xFF1982C4), // Azul
    Color(0xFF6A4C93), // Morado
    Color(0xFFFF006E), // Rosa neón
    Color(0xFF00F5D4)  // Cian
)

@Composable
fun GameScreen() {
    var gameStarted by remember { mutableStateOf(false) }
    var playerX by remember { mutableStateOf(0f) }
    var playerY by remember { mutableStateOf(0f) }

    // Estado del juego y tamaños de pantalla
    var playerInitialized by remember { mutableStateOf(false) }
    var screenWidth by remember { mutableStateOf(0f) }
    var screenHeight by remember { mutableStateOf(0f) }

    // Estado de la puntuación, vidas y game over
    var score by remember { mutableStateOf(0) }
    var lives by remember { mutableStateOf(3) }
    var gameOver by remember { mutableStateOf(false) }

    // Estado de los enemigos
    var enemies by remember { mutableStateOf(createEnemyFormation()) }
    var enemyDirection by remember { mutableStateOf(1f) }
    var enemySpeed by remember { mutableStateOf(5f) }
    var enemyDropDistance by remember { mutableStateOf(20f) }

    // Estado de los disparos
    var bullets by remember { mutableStateOf(listOf<Bullet>()) }
    var bulletColorIndex by remember { mutableStateOf(0) }

    // Función para restaurar valores al reiniciar
    fun resetGame() {
        score = 0
        lives = 3
        bullets = emptyList()
        bulletColorIndex = 0
        enemies = listOf(
            Enemy(x = screenWidth * 0.25f, y = screenHeight * 0.18f),
            Enemy(x = screenWidth * 0.50f, y = screenHeight * 0.18f),
            Enemy(x = screenWidth * 0.75f, y = screenHeight * 0.18f)
        )
        enemyDirection = 1f
        playerX = screenWidth / 2f
        playerY = screenHeight - 80f
        gameOver = false
        gameStarted = true
    }

    // Movimiento y eliminación de proyectiles
    LaunchedEffect(Unit) {
        while (true) {
            delay(16L)
            if (gameStarted) {
                bullets = bullets.map { bullet ->
                    bullet.copy(y = bullet.y - 8f)
                }.filter { bullet -> bullet.y > 0f }
            }
        }
    }

    // Movimiento horizontal y vertical de los enemigos
    /*LaunchedEffect(Unit) {
        while (true) {
            delay(30L)
            if (gameStarted && screenWidth > 0f && enemies.isNotEmpty()) {
                val enemyWidth = 60f
                val margin = enemyWidth / 2f
                val speed = enemySpeed
                val dropDistance = enemyDropDistance

                val leftEdge = enemies.minOf { it.x - margin }
                val rightEdge = enemies.maxOf { it.x + margin }

                val reachedEdge = (enemyDirection > 0f && rightEdge + speed >= screenWidth) ||
                        (enemyDirection < 0f && leftEdge - speed <= 0f)

                if (reachedEdge) {
                    enemyDirection *= -1f
                    enemies = enemies.map { enemy ->
                        enemy.copy(y = enemy.y + dropDistance)
                    }
                }

                enemies = enemies.map { enemy ->
                    enemy.copy(x = enemy.x + (speed * enemyDirection))
                }
            }
        }
    }*/

    //Movimiento horizontal de los enemigos
    LaunchedEffect(Unit) {
        while (true) {
            delay(30L)
            if (gameStarted && screenHeight > 0f && enemies.isNotEmpty()) {
                enemies = enemies.map { enemy ->
                    enemy.copy(y = enemy.y + enemySpeed)
                }
            }
        }
    }

    // Colisión enemigo, disparo y nave
    LaunchedEffect(gameStarted) {
        while (gameStarted) {
            // Enemigos y proyectiles
            val hitBullets = mutableSetOf<Bullet>()
            val hitEnemies = mutableSetOf<Enemy>()
            bullets.forEach { bullet ->
                enemies.forEach { enemy ->
                    val collisionX = abs(bullet.x - enemy.x) < 34f
                    val collisionY = abs(bullet.y - enemy.y) < 20f
                    if (collisionX && collisionY) {
                        hitBullets.add(bullet)
                        hitEnemies.add(enemy)
                    }
                }
            }

            if (hitBullets.isNotEmpty()) {
                bullets = bullets.filterNot { it in hitBullets }
                enemies = enemies.filterNot { it in hitEnemies }
                score += hitEnemies.size * 100
            }

            // Enemigos y nave
            val hitShip = enemies.filter { enemy ->
                val collisionX = abs(enemy.x - playerX) < 70f
                val collisionY = abs(enemy.y - playerY) < 50f
                collisionX && collisionY
            }

            if (hitShip.isNotEmpty()) {
                enemies = enemies.filterNot { it in hitShip }
                lives = (lives - hitShip.size).coerceAtLeast(0)
                if (lives == 0) {
                    gameStarted = false
                    gameOver = true
                }
            }

            // Detectar enemigos cuando lleguen a la parte inferior de la pantalla
            val enemiesAtBottom = enemies.filter { enemy ->
                enemy.y + 100f >= screenHeight
            }

            if (enemiesAtBottom.isNotEmpty()) {
                enemies = enemies.filterNot { it in enemiesAtBottom }
            }

            delay(16L)
        }
    }

    fun generateEnemy(): Enemy{
        val enemyWidth = 60f
        val randomX = kotlin.random.Random.nextFloat() *
                (screenWidth - enemyWidth) + enemyWidth/2f
        return Enemy(x= randomX,y=40f)
    }
    LaunchedEffect(gameStarted) {
        while (gameStarted) {
            delay(3000L)
            if (enemies.size <= 8 && screenWidth > 0)
            {
                enemies += generateEnemy()
            }

        }
    }

    Column(
        modifier = Modifier.fillMaxSize()
            .background(Color.Black)
            .padding(horizontal = 12.dp, vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // HUD superior: puntuación y vidas
        Row(
            modifier = Modifier.fillMaxWidth()
                .padding(vertical = 18.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "SCORE",
                    color = Color.LightGray,
                    fontSize = 12.sp,
                )
                Text(
                    text = "%04d".format(score),
                    color = Color.White,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "LIVES",
                    color = Color.LightGray,
                    fontSize = 12.sp,
                )
                Text(
                    text = "🦄".repeat(lives),
                    color = Color.Red,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        // El título solo aparece antes de iniciar el juego
        if (!gameStarted) {
            Text(
                text = "Cosmic Unicorn",
                color = Color.White,
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(vertical = 8.dp)
            )
        }

        // Mensaje de Game Over
        if (gameOver) {
            Text(
                text = "¡TE APLASTARON LAS CAQUITAS!",
                color = Color.Magenta,
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                textAlign = TextAlign.Center
            )
        }

        // Escenario exclusivo del juego
        Box(
            modifier = Modifier.fillMaxWidth()
                .weight(1f)
        ) {
            Canvas(
                modifier = Modifier.fillMaxSize()
                    .onSizeChanged { size ->
                        screenWidth = size.width.toFloat()
                        screenHeight = size.height.toFloat()
                        if (!playerInitialized) {
                            playerX = size.width / 2f
                            playerY = size.height - 80f
                            enemies = listOf(
                                Enemy(size.width * 0.25f, size.height * 0.18f),
                                Enemy(size.width * 0.50f, size.height * 0.18f),
                                Enemy(size.width * 0.75f, size.height * 0.18f)
                            )
                            playerInitialized = true
                        }
                    }
                    .pointerInput(Unit) {
                        detectDragGestures(
                            onDrag = { change, dragAmount ->
                                change.consume()
                                if (gameStarted) {
                                    val shipWidth = 80f
                                    playerX = (playerX + dragAmount.x)
                                        .coerceIn(
                                            shipWidth / 2f,
                                            screenWidth - shipWidth / 2f
                                        )
                                    playerY = (playerY + dragAmount.y)
                                        .coerceIn(
                                            shipWidth / 2f,
                                            screenHeight - shipWidth / 2f
                                        )
                                }
                            }
                        )
                    }
            ) {
                drawRect(color = Color.Black)

                // Nave del jugador (unicornio rosa y dorado)
                val shipWidth = 80f
                val shipHeight = 80f
                val left = playerX - shipWidth / 2f
                val top = playerY - shipHeight / 2f
                val right = playerX + shipWidth / 2f
                val bottom = playerY + shipHeight / 2f

                val bodyPath = Path().apply {
                    moveTo(playerX - 5f, top + 25f)
                    lineTo(left, bottom - 20f)
                    lineTo(left + 8f, bottom)
                    lineTo(playerX, bottom - 12f)
                    lineTo(right - 8f, bottom)
                    lineTo(right, bottom - 30f)
                    lineTo(right - 5f, top + 25f)
                    close()
                }

                val hornPath = Path().apply {
                    moveTo(playerX, top)
                    lineTo(playerX - 5f, top + 25f)
                    lineTo(right - 5f, top + 25f)
                    close()
                }

                drawPath(
                    path = bodyPath,
                    color = Color(0xFFF8BBD0)
                )

                drawPath(
                    path = hornPath,
                    color = Color(0xFFFFD700)
                )

                // Dibujar enemigos como caquitas arcoíris
                enemies.forEach { enemy ->
                    val w = 60f
                    val h = 48f
                    val left = enemy.x - w / 2f
                    val top = enemy.y - h / 2f

                    // 1. Base (Rojo y Naranja)
                    drawOval(
                        color = Color(0xFFFF595E), // Rojo vibrante
                        topLeft = Offset(left, top + h * 0.55f),
                        size = Size(w, h * 0.45f)
                    )

                    // 2. Nivel medio (Amarillo y Verde)
                    drawOval(
                        color = Color(0xFFFFCA3A), // Amarillo
                        topLeft = Offset(left + w * 0.12f, top + h * 0.32f),
                        size = Size(w * 0.76f, h * 0.38f)
                    )
                    drawOval(
                        color = Color(0xFF8AC926), // Verde
                        topLeft = Offset(left + w * 0.16f, top + h * 0.28f),
                        size = Size(w * 0.68f, h * 0.28f)
                    )

                    // 3. Nivel superior (Azul)
                    drawOval(
                        color = Color(0xFF1982C4), // Azul
                        topLeft = Offset(left + w * 0.25f, top + h * 0.12f),
                        size = Size(w * 0.50f, h * 0.30f)
                    )

                    // 4. Punta clásica (Morado)
                    val tipPath = Path().apply {
                        moveTo(enemy.x, top) // Punta superior
                        lineTo(enemy.x - w * 0.16f, top + h * 0.18f)
                        lineTo(enemy.x + w * 0.16f, top + h * 0.18f)
                        close()
                    }
                    drawPath(path = tipPath, color = Color(0xFF6A4C93)) // Morado

                    // 5. Ojitos para darle personalidad
                    val eyeY = top + h * 0.52f
                    val eyeOffsetX = w * 0.14f
                    // Fondo blanco
                    drawCircle(Color.White, radius = 5.5f, center = Offset(enemy.x - eyeOffsetX, eyeY))
                    drawCircle(Color.White, radius = 5.5f, center = Offset(enemy.x + eyeOffsetX, eyeY))
                    // Pupilas negras
                    drawCircle(Color.Black, radius = 2.8f, center = Offset(enemy.x - eyeOffsetX, eyeY))
                    drawCircle(Color.Black, radius = 2.8f, center = Offset(enemy.x + eyeOffsetX, eyeY))
                }

                // Dibujar disparos dentro del Canvas
                bullets.forEach { bullet ->
                    drawRect(
                        color = bullet.color,
                        topLeft = Offset(
                            bullet.x - 4f,
                            bullet.y - 15f
                        ),
                        size = Size(8f, 20f)
                    )
                }
            }
        }

        // Controles fuera del escenario
        Row(
            modifier = Modifier.fillMaxWidth()
                .padding(vertical = 30.dp),
            horizontalArrangement = Arrangement.spacedBy(
                12.dp,
                alignment = Alignment.CenterHorizontally
            ),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (gameOver) {
                Button(
                    onClick = { resetGame() },
                    modifier = Modifier.weight(1f)
                ) {
                    Text(text = "RESTART")
                }
            } else if (!gameStarted) {
                Button(
                    onClick = { gameStarted = true },
                    modifier = Modifier.weight(1f)
                ) {
                    Text(text = "START")
                }
            } else {
                Button(
                    onClick = {
                        val nextColor = bulletPalette[bulletColorIndex % bulletPalette.size]
                        bulletColorIndex++
                        bullets = bullets + Bullet(
                            x = playerX,
                            y = playerY - 40f,
                            color = nextColor
                        )
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Text(text = "FIRE")
                }
            }
        }
    }
}
// Función auxiliar fuera del composable principal
fun createEnemyFormation(): List<Enemy> {
    return listOf(
        Enemy(150f, 150f),
        Enemy(300f, 150f),
        Enemy(450f, 150f)
    )
}