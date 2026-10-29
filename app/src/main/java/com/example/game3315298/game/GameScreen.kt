package com.example.game3315298.Game

import androidx.compose.ui.graphics.Path
import androidx.compose.runtime.Composable
import androidx.compose.foundation.Canvas
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size

// Definición de las clases de datos
data class Enemy(val x: Float, val y: Float)
data class Bullet(val x: Float, val y: Float) // Corregido: Agregada la clase Bullet

@Composable
fun GameScreen() {
    var gameStarted by remember { mutableStateOf(false) }

    Box(modifier = Modifier.fillMaxSize()) {
        var playerX by remember { mutableStateOf(value = 0f) }
        var playerY by remember { mutableStateOf(value = 0f) }
            //estado del enemigo

        //val enemies = createEnemyFormation()
        var enemies by remember { mutableStateOf(createEnemyFormation())}
        var enemyDirection by remember { mutableStateOf(1f)}

        // Estado de la puntuación
        var score by remember { mutableStateOf(0) }
        // Estado de vidas
        var lives by remember { mutableStateOf(3) }
        // Estado de los disparos
        var bullets by remember { mutableStateOf(listOf<Bullet>()) }
        LaunchedEffect(Unit) {
            while (true){
                delay(16L)
                bullets = bullets.map{ bullet->
                    bullet.copy(y = bullet.y-8f)
                }
                    .filter { bullet -> bullet.y > 0f}
            }
        }
        // Estado del juego
        var playerInitialized by remember { mutableStateOf(value = false) }
        var screenWidth by remember { mutableStateOf(value = 0f) }
        LaunchedEffect(Unit) {
            while(true){
                delay(30L)
                val enemyWith = 60f
                val margin = enemyWith /2
                val speed = 3f
                val leftEdge = enemies.minOfOrNull { it.x - margin } ?: 0f
                val rightEdge = enemies.maxOfOrNull { it.x + margin } ?: 0f
                if (
                    (enemyDirection > 0f && rightEdge + speed >= screenWidth)
                    || (enemyDirection < 0f && leftEdge - speed <= 0f)
                ){
                    enemyDirection*=-1f

                }
                enemies = enemies.map{ enemy ->
                    enemy.copy(x = enemy.x +(speed * enemyDirection))
                }
            }
        }

        Canvas(modifier = Modifier.fillMaxSize()
            .onSizeChanged { size ->
                screenWidth = size.width.toFloat()
                if (!playerInitialized) {
                    playerX = size.width / 2f
                    playerY = size.height - 220f
                    playerInitialized = true
                }
            }
            .pointerInput(Unit) {
                detectDragGestures(
                    onDrag = { change, dragAmount ->
                        change.consume()
                        val shipWidth = 80f
                        playerX = (playerX + dragAmount.x)
                            .coerceIn(shipWidth / 2f, screenWidth - shipWidth / 2f)
                    }
                )
            }) {
            drawRect(color = Color.Black)
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

            // Dibujar enemigos con colores
            enemies.forEachIndexed { index, enemy ->
                val enemyWidth = 60f
                val enemyHeight = 40f

                // Cálculo del color arcoíris HSL
                val hue = (index * 360f / enemies.size) % 360f
                val rainbowColor = Color.hsl(hue = hue, saturation = 1f, lightness = 0.6f)

                drawRect(
                    color = rainbowColor,
                    topLeft = Offset(
                        enemy.x - enemyWidth / 2f,
                        enemy.y - enemyHeight / 2f
                    ),
                    size = Size(enemyWidth, enemyHeight)
                )
            }

            // Dibujar disparos
            bullets.forEach { bullet ->
                drawRect(
                    color = Color.Green,
                    topLeft = Offset(
                        bullet.x - 4f,
                        bullet.y - 15f
                    ),
                    size = Size(8f, 20f)
                )
            }
        }

        Column(
            modifier = Modifier.fillMaxSize()
                .padding(top = 100.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Text(
                    text = "SCORE: %04d".format(score),
                    color = Color.White,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "LIVES: " + "🦄".repeat(lives),
                    color = Color.Magenta,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            Text(
                text = "SPACE SHOOTER",
                color = Color.White,
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold
            )
            Button(onClick = {
                gameStarted = true
                score += 100
                if (lives > 0) {
                    lives -= 1
                }
            }) {
                Text(text = "START")
            }
            if (gameStarted) {
                Text(
                    text = "GAME STARTED",
                    color = Color.Blue
                )
            }
            Button(onClick = {
                bullets = bullets + Bullet(
                    x = playerX,
                    y = playerY - 40f
                )
            }) {
                Text(text = "FIRE")
            }
        }
    }
}

fun createEnemyFormation(): List<Enemy> {
    return listOf(
        Enemy(150f, 150f),
        Enemy(300f, 150f),
        Enemy(450f, 150f)
    )
}