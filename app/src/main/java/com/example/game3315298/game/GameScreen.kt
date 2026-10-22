package com.example.game3315298.Game

import androidx.compose.ui.graphics.Path
import androidx.compose.runtime.Composable
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
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


@Composable
fun GameScreen() {
    var gameStarted by remember { mutableStateOf(false) }

    Box(modifier = Modifier.fillMaxSize()) {
        var playerX by remember { mutableStateOf(value = 0f) }
        var playerY by remember { mutableStateOf(value = 0f) }
        var playerInitilialized by remember { mutableStateOf(value = false) }
        var screenWith by remember { mutableStateOf(value = 0f) }
        Canvas(modifier = Modifier.fillMaxSize()
            .onSizeChanged{ size->
                screenWith = size.width.toFloat()
                if (!playerInitilialized) {
                    playerX = size.width / 2f
                    playerY = size.height - 220f
                    playerInitilialized = true
                }
            }
            .pointerInput(Unit){detectDragGestures(
                onDrag = { change, dragAmount ->
                    change.consume()
                    val shipWith = 80f
                    playerX =( playerX + dragAmount.x)
                        .coerceIn(shipWith/2
                        , screenWith - shipWith/2)

                }
            )}) {
            drawRect(color = Color.Black)
            val shipWidth = 80f
            val shipHeight = 80f
            //val playerX = size.width / 2
           // val playerY = size.height - 220f
            val left = playerX - shipWidth / 2
            val top = playerY - shipHeight / 2
            val right = playerX + shipWidth / 2
            val bottom = playerY + shipHeight / 2

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
        }

        Column(modifier = Modifier.fillMaxSize()
            .padding(top = 100.dp),
            horizontalAlignment = Alignment.CenterHorizontally){
            Text(
                text = "SPACE SHOOTER",
                color = Color.White,
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold
            )
            Button(onClick = { gameStarted = true }) {
                Text(text = "START")
            }
            if(gameStarted){
                Text(text = "GAME STARTED",
                    color = Color.Blue)
            }
        }
    }
}