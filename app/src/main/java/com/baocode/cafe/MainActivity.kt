package com.baocode.cafe

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlin.math.min

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) = super.onCreate(savedInstanceState).also {
        setContent { MaterialTheme(colorScheme = darkColorScheme(primary = Neon)) { BaoCodeGame() } }
    }
}

val Neon = Color(0xFF55F0B2)
val NeonSoft = Color(0xFF36B987)
val Ink = Color(0xFFEAF7F0)
val Terminal = Color(0xFF061011)
val Panel = Color(0xED071617)
val Panel2 = Color(0xFF0B1C1D)
val Cream = Color(0xFFFFF2D9)

private data class Command(val id: String, val source: String, val label: String)
private val commands = listOf(
    Command("to_guest", "walk_to(guest)", "Подойти к гостю"),
    Command("take", "order = take_order()", "Принять заказ"),
    Command("to_counter", "walk_to(cashier)", "Подойти к кассе"),
    Command("take_bao", "take(bao_order)", "Забрать бао"),
    Command("return", "walk_to(guest)", "Вернуться к гостю"),
    Command("serve", "serve(order, guest)", "Подать заказ"),
    Command("nap", "take_a_nap()", "Вздремнуть"),
    Command("wrong", "serve(empty_plate)", "Подать пустую тарелку")
)
private val solution = listOf("to_guest", "take", "to_counter", "take_bao", "return", "serve")

private data class Hero2(val c: Int = 7, val r: Int = 7, val x: Float = 7f, val y: Float = 7f, val moving: Boolean = false, val order: Boolean = false, val bao: Boolean = false)
private enum class Target2 { GUEST, CASHIER, KITCHEN }
private data class Pos2(val c: Int, val r: Int)
private val guest2 = Pos2(9, 2)
private val cashier2 = Pos2(4, 2)
private val kitchen2 = Pos2(2, 2)

@Composable
private fun BaoCodeGame() {
    val grid = remember { defaultCafeGrid5() }
    var program by remember { mutableStateOf(emptyList<Command>()) }
    var running by remember { mutableStateOf(false) }
    var action by remember { mutableStateOf("Собери программу для первого гостя") }
    var hero by remember { mutableStateOf(Hero2()) }
    var target by remember { mutableStateOf<Target2?>(null) }
    var timeMs by remember { mutableLongStateOf(0L) }

    LaunchedEffect(Unit) {
        while (true) {
            timeMs = System.currentTimeMillis()
            delay(40)
        }
    }

    suspend fun moveTo(dest: Pos2, kind: Target2) {
        target = kind
        val path = findCafePath5(grid, hero.c, hero.r, dest.c, dest.r)
        if (path.isEmpty() && (hero.c != dest.c || hero.r != dest.r)) {
            action = "Путь заблокирован"
            delay(500)
            target = null
            return
        }
        hero = hero.copy(moving = true)
        for (next in path) {
            val sx = hero.x
            val sy = hero.y
            val dx = next.c - hero.c
            val dy = next.r - hero.r
            for (step in 1..8) {
                val t = step / 8f
                hero = hero.copy(x = sx + dx * t, y = sy + dy * t, moving = true)
                delay(24)
            }
            hero = hero.copy(c = next.c, r = next.r, x = next.c.toFloat(), y = next.r.toFloat())
            delay(20)
        }
        hero = hero.copy(moving = false)
        target = null
    }

    LaunchedEffect(running) {
        if (!running) return@LaunchedEffect
        if (program.map { it.id } != solution) {
            action = "Ошибка: порядок команд нарушен"
            delay(1200)
            running = false
            return@LaunchedEffect
        }
        program.forEach { cmd ->
            action = cmd.label
            when (cmd.id) {
                "to_guest" -> moveTo(guest2, Target2.GUEST)
                "take" -> { hero = hero.copy(order = true); delay(650) }
                "to_counter" -> moveTo(cashier2, Target2.CASHIER)
                "take_bao" -> { moveTo(kitchen2, Target2.KITCHEN); hero = hero.copy(bao = true); delay(650) }
                "return" -> moveTo(guest2, Target2.GUEST)
                "serve" -> { hero = hero.copy(order = false, bao = false); delay(800) }
            }
        }
        action = "Заказ доставлен! Гость счастлив ✦"
        running = false
    }

    Row(Modifier.fillMaxSize().background(Terminal)) {
        Column(Modifier.width(286.dp).fillMaxHeight().padding(9.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Program3(program, running, { cmd -> program = program.filterNot { it.id == cmd.id } }, Modifier.weight(1.12f).fillMaxWidth())
            Commands3(program, running, { cmd -> program = program + cmd }, { if (program.isNotEmpty()) running = true }, Modifier.weight(.88f).fillMaxWidth())
        }
        Box(Modifier.weight(1f).fillMaxHeight()) {
            World2(hero, target, grid, timeMs)
            Surface(color = Panel, shape = RoundedCornerShape(7.dp), modifier = Modifier.align(Alignment.TopEnd).padding(9.dp).width(165.dp).border(1.dp, Neon.copy(alpha = .72f), RoundedCornerShape(7.dp))) {
                Column(Modifier.padding(horizontal = 10.dp, vertical = 8.dp)) {
                    Text("16.09.2026   19:43", color = Ink, fontFamily = FontFamily.Monospace, fontSize = 10.sp)
                    Text("TOKYO, JP", color = Neon, fontFamily = FontFamily.Monospace, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    Text("☔  +18°C  ·  Дождь", color = Ink, fontSize = 10.sp)
                }
            }
            Surface(color = Terminal.copy(alpha = .92f), shape = RoundedCornerShape(8.dp), modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 10.dp)) {
                Text(action, color = Neon, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold, fontSize = 10.sp, textAlign = TextAlign.Center, modifier = Modifier.padding(horizontal = 13.dp, vertical = 7.dp))
            }
        }
    }
}

@Composable
private fun Program3(program: List<Command>, running: Boolean, remove: (Command) -> Unit, modifier: Modifier) {
    Surface(color = Panel, shape = RoundedCornerShape(8.dp), modifier = modifier.border(1.dp, Neon.copy(alpha = .72f), RoundedCornerShape(8.dp))) {
        Column(Modifier.fillMaxSize().padding(9.dp)) {
            Text("// PROGRAM", color = Neon, fontFamily = FontFamily.Monospace, fontSize = 11.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(6.dp))
            Column(Modifier.fillMaxSize().clip(RoundedCornerShape(5.dp)).background(Color(0xFF030A0B)).padding(8.dp).verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                if (program.isEmpty()) Text("// выбери команду\n// ниже", color = Color(0xFF66807A), fontFamily = FontFamily.Monospace, fontSize = 10.sp)
                program.forEachIndexed { i, cmd -> Text("${i + 1}  ${cmd.source}", color = if (running) Color(0xFF6B7775) else Cream, fontFamily = FontFamily.Monospace, fontSize = 10.sp, modifier = Modifier.clickable(enabled = !running) { remove(cmd) }) }
            }
        }
    }
}

@Composable
private fun Commands3(program: List<Command>, running: Boolean, add: (Command) -> Unit, run: () -> Unit, modifier: Modifier) {
    Surface(color = Panel, shape = RoundedCornerShape(8.dp), modifier = modifier.border(1.dp, Neon.copy(alpha = .72f), RoundedCornerShape(8.dp))) {
        Column(Modifier.fillMaxSize().padding(9.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                Text("// COMMANDS", color = Neon, fontFamily = FontFamily.Monospace, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                Button(onClick = run, enabled = !running && program.isNotEmpty(), contentPadding = PaddingValues(horizontal = 9.dp, vertical = 0.dp), colors = ButtonDefaults.buttonColors(containerColor = Neon, contentColor = Terminal), modifier = Modifier.height(29.dp)) { Text("▶ RUN", fontSize = 9.sp, fontWeight = FontWeight.Black) }
            }
            Spacer(Modifier.height(6.dp))
            Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(5.dp)) {
                commands.forEach { cmd ->
                    val count = program.count { it.id == cmd.id }
                    Surface(color = if (count > 0) Color(0xFF102A24) else Panel2, shape = RoundedCornerShape(5.dp), modifier = Modifier.fillMaxWidth().border(1.dp, if (count > 0) NeonSoft else Color(0xFF1A3535), RoundedCornerShape(5.dp)).clickable(enabled = !running) { add(cmd) }) {
                        Row(Modifier.padding(horizontal = 8.dp, vertical = 6.dp), verticalAlignment = Alignment.CenterVertically) {
                            Text(cmd.source, color = Cream, fontFamily = FontFamily.Monospace, fontSize = 9.sp, modifier = Modifier.weight(1f))
                            if (count > 0) Text("×$count", color = Neon, fontFamily = FontFamily.Monospace, fontSize = 8.sp)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun World2(hero: Hero2, target: Target2?, grid: CafeGrid5, timeMs: Long) {
    Canvas(Modifier.fillMaxSize()) {
        val tile = min(size.width / 15.5f, size.height / 11.5f)
        val mapW = tile * grid.width
        val mapH = tile * grid.height
        val origin = Offset((size.width - mapW) * .5f, (size.height - mapH) * .5f)

        drawCafePixelScene(origin, tile, grid, timeMs)

        val guest = Offset(origin.x + (9.5f) * tile, origin.y + (2.7f) * tile)
        val chef = Offset(origin.x + 2.8f * tile, origin.y + 1.7f * tile)
        val waiter = Offset(origin.x + 7.0f * tile, origin.y + 4.6f * tile)
        val guest2 = Offset(origin.x + 11.0f * tile, origin.y + 5.2f * tile)
        val guest3 = Offset(origin.x + 6.0f * tile, origin.y + 7.0f * tile)
        drawPixelNpc(chef, tile, 1, timeMs)
        drawPixelNpc(waiter, tile, 0, timeMs + 300)
        drawPixelNpc(guest, tile, 2, timeMs + 600)
        drawPixelNpc(guest2, tile, 0, timeMs + 900)
        drawPixelNpc(guest3, tile, 1, timeMs + 1200)

        val heroPos = Offset(origin.x + (hero.x + .5f) * tile, origin.y + (hero.y + .5f) * tile)
        drawPixelHero(heroPos, tile, hero.moving, hero.order, hero.bao)

        if (target != null) {
            val p = when (target) {
                Target2.GUEST -> Offset(origin.x + 9.5f * tile, origin.y + 2.7f * tile)
                Target2.CASHIER -> Offset(origin.x + 4.5f * tile, origin.y + 2.7f * tile)
                Target2.KITCHEN -> Offset(origin.x + 2.5f * tile, origin.y + 2.3f * tile)
            }
            drawRoundRect(Neon.copy(alpha=.18f), Offset(p.x-tile*.36f,p.y-tile*.46f), androidx.compose.ui.geometry.Size(tile*.72f,tile*.92f), androidx.compose.ui.geometry.CornerRadius(tile*.12f))
            drawRoundRect(Neon, Offset(p.x-tile*.36f,p.y-tile*.46f), androidx.compose.ui.geometry.Size(tile*.72f,tile*.92f), androidx.compose.ui.geometry.CornerRadius(tile*.12f), style=androidx.compose.ui.graphics.drawscope.Stroke(width=tile*.025f))
        }
    }
}
