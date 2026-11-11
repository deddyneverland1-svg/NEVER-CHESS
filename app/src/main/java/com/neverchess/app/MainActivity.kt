package com.neverchess.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { NeverChessApp() }
    }
}

@Composable
fun NeverChessApp() {
    val initial = listOf(
        "♜","♞","♝","♛","♚","♝","♞","♜",
        "♟","♟","♟","♟","♟","♟","♟","♟",
        "","","","","","","","",
        "","","","","","","","",
        "","","","","","","","",
        "","","","","","","","",
        "♙","♙","♙","♙","♙","♙","♙","♙",
        "♖","♘","♗","♕","♔","♗","♘","♖"
    )

    val board = remember {
        mutableStateListOf<String>().apply { addAll(initial) }
    }
    var selected by remember { mutableIntStateOf(-1) }
    var whiteTurn by remember { mutableStateOf(true) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF101820))
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            "NEVER CHESS",
            color = Color(0xFFFFD700),
            fontSize = 30.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(Modifier.height(8.dp))

        Text(
            "Kamu bermain catur di sini",
            color = Color.White,
            fontSize = 16.sp
        )

        Spacer(Modifier.height(12.dp))

        Text(
            if (whiteTurn) "Giliran Putih" else "Giliran Hitam",
            color = Color(0xFFFFD700),
            fontSize = 20.sp
        )

        Spacer(Modifier.height(18.dp))

        Column(
            modifier = Modifier.fillMaxWidth()
        ) {
            for (r in 0..7) {
                Row {
                    for (c in 0..7) {
                        val index = r * 8 + c
                        val light = (r + c) % 2 == 0
                        val square = if (light)
                            Color(0xFFE8D5B5)
                        else Color(0xFF795548)

                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .aspectRatio(1f)
                                .background(
                                    if (selected == index)
                                        Color(0xFFFFD700)
                                    else square
                                )
                                .clickable {
                                    if (selected == -1) {
                                        if (board[index].isNotEmpty()) {
                                            selected = index
                                        }
                                    } else {
                                        if (selected != index) {
                                            board[index] = board[selected]
                                            board[selected] = ""
                                            whiteTurn = !whiteTurn
                                        }
                                        selected = -1
                                    }
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                board[index],
                                fontSize = 29.sp,
                                color = Color.Black
                            )
                        }
                    }
                }
            }
        }

        Spacer(Modifier.height(24.dp))

        Button(
            onClick = {
                board.clear()
                board.addAll(initial)
                selected = -1
                whiteTurn = true
            },
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFFFFD700),
                contentColor = Color.Black
            )
        ) {
            Text("ULANGI PARTAI")
        }

        Spacer(Modifier.height(12.dp))

        Text(
            "NEVER CHESS • BY BANG NEVER",
            color = Color.LightGray,
            fontSize = 12.sp
        )
    }
}
