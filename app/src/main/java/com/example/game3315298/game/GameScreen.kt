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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.gestures.detectDragGestures


import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size

// Definición de las clases de datos
data class Enemy(val x: Float, val y: Float)
data class Bullet(val x: Float, val y: Float)

@Composable
fun GameScreen() {
    var gameStarted by remember { mutableStateOf(false) }
    var playerX by remember { mutableStateOf(0f) }
    var playerY by remember { mutableStateOf(0f) }

    // Estado del juego
    var playerInitialized by remember { mutableStateOf(false) }
    var screenWidth by remember { mutableStateOf(0f) }

    // Estado de la puntuación y vidas
    var score by remember { mutableStateOf(0) }
    var lives by remember { mutableStateOf(3) }

    // Estado de los enemigos
    var enemies by remember { mutableStateOf(createEnemyFormation()) }
    var enemyDirection by remember { mutableStateOf(1f) }

    // Estado de los disparos
    var bullets by remember { mutableStateOf(listOf<Bullet>()) }

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

    // Movimiento horizontal de los enemigos
    LaunchedEffect(Unit) {
        while (true) {
            delay(30L)
            if (gameStarted && screenWidth > 0f && enemies.isNotEmpty()) {
                val enemyWidth = 60f
                val margin = enemyWidth / 2f
                val speed = 3f

                val leftEdge = enemies.minOf { it.x - margin }
                val rightEdge = enemies.maxOf { it.x + margin }

                if (
                    (enemyDirection > 0f && rightEdge + speed >= screenWidth) ||
                    (enemyDirection < 0f && leftEdge - speed <= 0f)) {
                    enemyDirection *= -1f
                }
                enemies = enemies.map { enemy ->
                    enemy.copy(x = enemy.x + (speed * enemyDirection))
                }
            }
        }
    }

    Column(modifier = Modifier.fillMaxSize()
        .background(Color.Black)
        .padding(horizontal = 12.dp, vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally){
    //HUD superior: puntucación y vidas
        Row(
            modifier = Modifier.fillMaxWidth()
                .padding(vertical = 18.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically) {
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

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "LIVES",
                        color = Color.LightGray,
                        fontSize = 12.sp,
                    )
                }
                Text(
                    text = "🦄".repeat(lives),
                    color = Color.Red,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
        //El título solo aparece antes de iniciar el juego
       if(!gameStarted){
           Text(
               text = "SPACE SHOOTER",
               color = Color.White,
               fontSize = 28.sp,
               fontWeight = FontWeight.Bold,
               modifier = Modifier.padding(vertical = 8.dp)
           )
       }
        //Escenario exclusivo del juego
        Box(modifier = Modifier.fillMaxWidth()
            .weight(1f)){

            Canvas(
                modifier = Modifier.fillMaxSize()
                    .onSizeChanged { size ->
                        screenWidth = size.width.toFloat()
                        if (!playerInitialized) {
                            playerX = size.width / 2f
                            playerY = size.height - 80f
                            enemies = listOf(
                                Enemy(size.width*0.25f, size.height*0.18f),
                                Enemy(size.width*0.50f, size.height*0.18f),
                                Enemy(size.width*0.75f, size.height*0.18f))
                            playerInitialized = true
                        }
                    }
                    .pointerInput(Unit) {
                        detectDragGestures(
                            onDrag = { change, dragAmount ->
                                change.consume()
                               if(gameStarted){
                                   val shipWidth = 80f
                                   playerX = (playerX + dragAmount.x)
                                       .coerceIn(shipWidth / 2f,
                                           screenWidth - shipWidth / 2f)
                               }
                            }
                        )
                    }) {
                drawRect(color = Color.Black)
                //Nave del jugador
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

                // Dibujar enemigos en rosado claro
                enemies.forEach { enemy ->
                    val enemyWidth = 60f
                    val enemyHeight = 40f
                    val lightPink = Color(0xFFFFB6C1)

                    drawRect(
                        color = lightPink,
                        topLeft = Offset(
                            enemy.x - enemyWidth / 2f,
                            enemy.y - enemyHeight / 2f
                        ),
                        size = Size(enemyWidth, enemyHeight)
                    )
                }

                // Dibujar disparos dentro del Canvas
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
        }
        //Controles fuera del escenario
            Row(
                modifier = Modifier.fillMaxWidth()
                .padding(vertical = 30.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp,
                   alignment = Alignment.CenterHorizontally ),
             verticalAlignment = Alignment.CenterVertically)
         {
             if(!gameStarted){
                 Button(onClick = {
                     gameStarted = true
                 }, modifier = Modifier.weight(1f)){
                     Text(text = "START")
                 }
         }

            if (gameStarted) {
                Button(onClick = {
                    bullets = bullets + Bullet(
                        x = playerX,
                        y = playerY - 40f
                    )
                }, modifier = Modifier.weight(1f)) {
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