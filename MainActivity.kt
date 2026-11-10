package com.neverchess.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val Ink = Color(0xFF101114)
private val Panel = Color(0xFF1B1D22)
private val Gold = Color(0xFFD7AE58)
private val Muted = Color(0xFFB8B8B8)
private val LightSquare = Color(0xFFE7D7B8)
private val DarkSquare = Color(0xFF8B6748)
private val Highlight = Color(0xFFB6A14A)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { NeverChessApp() }
    }
}

data class Piece(val side: Char, val kind: Char) {
    fun symbol(): String = when (side) {
        'w' -> when (kind) {
            'K' -> "♔"; 'Q' -> "♕"; 'R' -> "♖"
            'B' -> "♗"; 'N' -> "♘"; else -> "♙"
        }
        else -> when (kind) {
            'K' -> "♚"; 'Q' -> "♛"; 'R' -> "♜"
            'B' -> "♝"; 'N' -> "♞"; else -> "♟"
        }
    }
}

private fun initialBoard(): List<Piece?> {
    val b = MutableList<Piece?>(64) { null }
    val back = "RNBQKBNR"
    for (c in 0..7) {
        b[c] = Piece('b', back[c])
        b[8 + c] = Piece('b', 'P')
        b[48 + c] = Piece('w', 'P')
        b[56 + c] = Piece('w', back[c])
    }
    return b
}

private fun pathClear(board: List<Piece?>, from: Int, to: Int): Boolean {
    val fr = from / 8; val fc = from % 8
    val tr = to / 8; val tc = to % 8
    val dr = (tr - fr).compareTo(0)
    val dc = (tc - fc).compareTo(0)
    var r = fr + dr; var c = fc + dc
    while (r != tr || c != tc) {
        if (board[r * 8 + c] != null) return false
        r += dr; c += dc
    }
    return true
}

private fun validGeometry(board: List<Piece?>, from: Int, to: Int): Boolean {
    if (from == to) return false
    val p = board[from] ?: return false
    val target = board[to]
    if (target?.side == p.side) return false
    val fr = from / 8; val fc = from % 8
    val tr = to / 8; val tc = to % 8
    val dr = tr - fr; val dc = tc - fc
    val ar = kotlin.math.abs(dr); val ac = kotlin.math.abs(dc)
    return when (p.kind) {
        'P' -> {
            val dir = if (p.side == 'w') -1 else 1
            val start = if (p.side == 'w') 6 else 1
            when {
                dc == 0 && target == null && dr == dir -> true
                dc == 0 && target == null && fr == start && dr == 2 * dir ->
                    board[(fr + dir) * 8 + fc] == null
                ac == 1 && dr == dir && target != null -> true
                else -> false
            }
        }
        'N' -> (ar == 2 && ac == 1) || (ar == 1 && ac == 2)
        'K' -> ar <= 1 && ac <= 1
        'B' -> ar == ac && pathClear(board, from, to)
        'R' -> (dr == 0 || dc == 0) && pathClear(board, from, to)
        'Q' -> ((ar == ac) || dr == 0 || dc == 0) && pathClear(board, from, to)
        else -> false
    }
}

@Composable
fun NeverChessApp() {
    var screen by remember { mutableStateOf("HOME") }
    var board by remember { mutableStateOf(initialBoard()) }
    var selected by remember { mutableStateOf<Int?>(null) }
    var whiteToMove by remember { mutableStateOf(true) }
    var message by remember { mutableStateOf("Putih melangkah lebih dulu") }
    var moveCount by remember { mutableIntStateOf(0) }

    fun resetGame() {
        board = initialBoard()
        selected = null
        whiteToMove = true
        moveCount = 0
        message = "Putih melangkah lebih dulu"
    }

    MaterialTheme(colorScheme = darkColorScheme(
        background = Ink, surface = Panel, primary = Gold,
        onBackground = Color.White, onSurface = Color.White
    )) {
        Surface(Modifier.fillMaxSize(), color = Ink) {
            if (screen == "BOARD") {
                Column(
                    Modifier.fillMaxSize().padding(horizontal = 14.dp, vertical = 18.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                        TextButton(onClick = { screen = "HOME" }) { Text("‹ KEMBALI", color = Gold) }
                        Spacer(Modifier.weight(1f))
                        Text("NEVER CHESS", color = Gold, fontWeight = FontWeight.Bold)
                    }
                    Spacer(Modifier.height(10.dp))
                    Text(if (whiteToMove) "GILIRAN PUTIH" else "GILIRAN HITAM",
                        color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                    Text(message, color = Muted, fontSize = 12.sp)
                    Spacer(Modifier.height(18.dp))
                    ChessBoard(board, selected) { square ->
                        val current = selected
                        if (current == null) {
                            val piece = board[square]
                            if (piece != null && piece.side == if (whiteToMove) 'w' else 'b') {
                                selected = square
                                message = "Pilih petak tujuan"
                            }
                        } else {
                            val piece = board[current]
                            if (square == current) {
                                selected = null
                                message = "Pilihan dibatalkan"
                            } else if (piece != null && board[square]?.side == piece.side) {
                                selected = square
                                message = "Bidak dipilih"
                            } else if (piece != null && piece.side == if (whiteToMove) 'w' else 'b'
                                && validGeometry(board, current, square)) {
                                val next = board.toMutableList()
                                var moved = piece
                                val targetRow = square / 8
                                if (piece.kind == 'P' && (targetRow == 0 || targetRow == 7))
                                    moved = Piece(piece.side, 'Q')
                                next[square] = moved
                                next[current] = null
                                board = next
                                whiteToMove = !whiteToMove
                                moveCount++
                                selected = null
                                message = if (next[square]?.kind == 'Q' && piece.kind == 'P')
                                    "Promosi pion menjadi ratu • langkah $moveCount"
                                else "Langkah $moveCount • ${if (whiteToMove) "Giliran putih" else "Giliran hitam"}"
                            } else {
                                selected = null
                                message = "Langkah tidak sesuai pola bidak"
                            }
                        }
                    }
                    Spacer(Modifier.height(18.dp))
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        OutlinedButton(onClick = { resetGame() }, modifier = Modifier.weight(1f)) {
                            Text("ULANGI", color = Gold)
                        }
                        Button(onClick = { selected = null; message = "Pilih bidak untuk bergerak" },
                            modifier = Modifier.weight(1f), colors = ButtonDefaults.buttonColors(containerColor = Gold)) {
                            Text("BATAL PILIH", color = Ink)
                        }
                    }
                    Spacer(Modifier.height(12.dp))
                    Text("Mode papan lokal • dua pemain bergantian",
                        color = Muted, fontSize = 11.sp, textAlign = TextAlign.Center)
                    Spacer(Modifier.weight(1f))
                    Text("Aturan skak, rokade, en passant, dan engine AI belum aktif.",
                        color = Muted, fontSize = 10.sp, textAlign = TextAlign.Center)
                }
            } else {
                Column(
                    Modifier.fillMaxSize().padding(22.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Spacer(Modifier.height(28.dp))
                    Text("♟", color = Gold, fontSize = 54.sp)
                    Text("NEVER CHESS", color = Color.White, fontSize = 30.sp,
                        fontWeight = FontWeight.Black, letterSpacing = 2.sp)
                    Text("THINK LIKE TAL. ATTACK WITHOUT FEAR.",
                        color = Muted, fontSize = 10.sp, letterSpacing = 1.2.sp)
                    Spacer(Modifier.height(34.dp))
                    MenuCard("PAPAN CATUR", "Main lokal dua pemain • gerakkan buah catur",
                        "PLAY") { resetGame(); screen = "BOARD" }
                    Spacer(Modifier.height(14.dp))
                    MenuCard("OFFLINE MASTER", "Lawan komputer Stockfish (tahap integrasi)",
                        "OFFLINE") { message = "Stockfish belum terpasang. Saat ini tersedia papan lokal." }
                    Spacer(Modifier.height(14.dp))
                    MenuCard("ONLINE ARENA", "Tantang teman melalui server (tahap berikutnya)",
                        "ONLINE") { message = "Mode online memerlukan server pertandingan." }
                    Spacer(Modifier.height(14.dp))
                    MenuCard("ANALISIS PARTAI", "PGN • evaluasi • blunder • akurasi (tahap berikutnya)",
                        "ANALYSIS") { message = "Analisis otomatis akan hadir setelah engine terintegrasi." }
                    Spacer(Modifier.weight(1f))
                    Text(message, color = Muted, fontSize = 11.sp, textAlign = TextAlign.Center)
                    Spacer(Modifier.height(12.dp))
                    Text("NEVER CHESS  •  VERSION 0.2.0",
                        color = Gold, fontSize = 10.sp, letterSpacing = 1.sp)
                }
            }
        }
    }
}

@Composable
private fun ChessBoard(board: List<Piece?>, selected: Int?, onSquare: (Int) -> Unit) {
    Column(Modifier.fillMaxWidth().aspectRatio(1f)) {
        for (r in 0..7) {
            Row(Modifier.weight(1f)) {
                for (c in 0..7) {
                    val index = r * 8 + c
                    val isLight = (r + c) % 2 == 0
                    Box(
                        Modifier.weight(1f).fillMaxHeight()
                            .background(if (selected == index) Highlight else if (isLight) LightSquare else DarkSquare)
                            .clickable { onSquare(index) },
                        contentAlignment = Alignment.Center
                    ) {
                        board[index]?.let { piece ->
                            Text(piece.symbol(), fontSize = 34.sp,
                                color = if (piece.side == 'w') Color.White else Color(0xFF161616))
                        }
                        if (c == 0) Text("${8-r}", Modifier.align(Alignment.TopStart).padding(2.dp),
                            color = if (isLight) DarkSquare else LightSquare, fontSize = 8.sp)
                        if (r == 7) Text("abcdefgh"[c].toString(),
                            Modifier.align(Alignment.BottomEnd).padding(2.dp),
                            color = if (isLight) DarkSquare else LightSquare, fontSize = 8.sp)
                    }
                }
            }
        }
    }
}

@Composable
private fun MenuCard(title: String, subtitle: String, tag: String, onClick: () -> Unit) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = Panel)
    ) {
        Row(
            Modifier.fillMaxWidth().padding(18.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(Modifier.weight(1f)) {
                Text(title, color = Gold, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Spacer(Modifier.height(5.dp))
                Text(subtitle, color = Muted, fontSize = 12.sp)
            }
            Text("›", color = Gold, fontSize = 30.sp)
        }
    }
}
