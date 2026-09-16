package com.baocode.cafe

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.BiasAlignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) = super.onCreate(savedInstanceState).also {
        setContent { MaterialTheme(colorScheme = lightColorScheme(primary = Coral)) { BaoCodeGame() } }
    }
}

private val Ink = Color(0xFF332D2A)
private val Coral = Color(0xFFD95B4B)
private val Cream = Color(0xFFFFF8EE)
private val Mint = Color(0xFF5D9B80)
private val DarkPanel = Color(0xFF27272B)
private val Terminal = Color(0xFF1C2B2C)
private val Grid = Color(0xFF6BD1BF)
private data class Command(val id: String, val source: String, val label: String)

private val commands = listOf(
    Command("to_guest", "walk_to(guest)", "Идём к гостю"), Command("take", "order = take_order()", "Берём заказ"),
    Command("to_counter", "walk_to(cashier)", "Идём к кассе"), Command("take_bao", "take(bao_order)", "Забираем бао"),
    Command("return", "walk_to(guest)", "Возвращаемся к гостю"), Command("serve", "serve(order, guest)", "Подаём заказ"),
    Command("nap", "take_a_nap()", "Вздремнуть"), Command("wrong", "serve(empty_plate)", "Пустая тарелка")
)
private val solution = listOf("to_guest", "take", "to_counter", "take_bao", "return", "serve")

private enum class Target { GUEST, CASHIER, KITCHEN }
private data class HeroState(
    val x: Float = .56f,
    val y: Float = .68f,
    val facing: Float = 0f,
    val moving: Boolean = false,
    val hasOrder: Boolean = false,
    val hasBao: Boolean = false
)

@Composable
private fun BaoCodeGame() {
    var program by remember { mutableStateOf(emptyList<Command>()) }
    var isRunning by remember { mutableStateOf(false) }
    var action by remember { mutableStateOf("Собери команды для первого гостя") }
    var served by remember { mutableIntStateOf(0) }
    var hero by remember { mutableStateOf(HeroState()) }
    var currentTarget by remember { mutableStateOf<Target?>(null) }
    val available = commands.filterNot { candidate -> program.any { it.id == candidate.id } }

    suspend fun moveTo(target: Target) {
        val destination = when (target) {
            Target.GUEST -> Offset(.77f, .38f)
            Target.CASHIER -> Offset(.30f, .45f)
            Target.KITCHEN -> Offset(.18f, .29f)
        }
        val start = Offset(hero.x, hero.y)
        val dx = destination.x - start.x
        val dy = destination.y - start.y
        val distance = kotlin.math.sqrt(dx * dx + dy * dy).coerceAtLeast(.001f)
        val steps = (distance * 65).toInt().coerceIn(16, 52)
        currentTarget = target
        hero = hero.copy(moving = true, facing = atan2(dy, dx))
        for (i in 1..steps) {
            val t = i / steps.toFloat()
            hero = hero.copy(x = start.x + dx * t, y = start.y + dy * t, moving = true)
            delay(18)
        }
        hero = hero.copy(x = destination.x, y = destination.y, moving = false)
        currentTarget = null
    }

    LaunchedEffect(isRunning) {
        if (!isRunning) return@LaunchedEffect
        if (program.map { it.id } != solution) {
            action = "Ой! Бэби Бао запутался. Проверь порядок и лишние команды."
            delay(1300)
            isRunning = false
            return@LaunchedEffect
        }
        program.forEach { command ->
            action = command.label
            when (command.id) {
                "to_guest" -> moveTo(Target.GUEST)
                "take" -> { hero = hero.copy(hasOrder = true); delay(500) }
                "to_counter" -> moveTo(Target.CASHIER)
                "take_bao" -> { moveTo(Target.KITCHEN); hero = hero.copy(hasBao = true); delay(500) }
                "return" -> moveTo(Target.GUEST)
                "serve" -> { hero = hero.copy(hasOrder = false, hasBao = false); delay(650) }
            }
        }
        served++
        action = "Заказ доставлен! +1 счастливая булочка ✨"
        isRunning = false
    }

    Box(Modifier.fillMaxSize().background(Terminal)) {
        CafeWorld(hero, currentTarget)
        Text("ЗАКАЗ: 2 × бао", color = Ink, fontSize = 11.sp, fontWeight = FontWeight.Bold,
            modifier = Modifier.align(BiasAlignment(.08f, -.22f)).clip(RoundedCornerShape(7.dp)).background(Cream).padding(horizontal = 7.dp, vertical = 4.dp))
        Text(action, color = Color.White, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center,
            modifier = Modifier.align(BiasAlignment(.10f, .35f)).clip(RoundedCornerShape(12.dp)).background(Terminal.copy(alpha = .91f)).padding(horizontal = 12.dp, vertical = 6.dp))
        CodeDock(program, available, isRunning, { program = program + it }, { command -> program = program.filterNot { it.id == command.id } }, { isRunning = true },
            Modifier.align(Alignment.BottomStart).padding(12.dp).fillMaxWidth(.42f).height(230.dp))
        Surface(color = Terminal.copy(alpha = .94f), shape = RoundedCornerShape(10.dp), modifier = Modifier.align(Alignment.TopEnd).padding(12.dp).width(178.dp).border(1.dp, Grid.copy(alpha = .8f), RoundedCornerShape(10.dp))) {
            Column(Modifier.padding(9.dp)) {
                Text("16.09.2026  ·  19:43", color = Cream, fontFamily = FontFamily.Monospace, fontSize = 12.sp)
                Text("ТОКИО, ЯПОНИЯ", color = Grid, fontFamily = FontFamily.Monospace, fontSize = 11.sp)
                Text("☔  Дождь  ·  +18°C", color = Cream, fontSize = 12.sp)
                Spacer(Modifier.height(8.dp))
                Text("Доставлено: $served", color = Color(0xFFFFD38F), fontWeight = FontWeight.Bold, fontSize = 11.sp)
            }
        }
    }
}

@Composable
private fun CafeWorld(hero: HeroState, target: Target?) {
    Canvas(Modifier.fillMaxSize()) {
        drawCafeFloor()
        drawCafeWalls()
        drawCounter(.30f, .45f, Coral)
        drawKitchen(.18f, .29f)
        drawTable(.77f, .38f)
        drawGuest(.80f, .31f)
        drawTable(.57f, .67f)
        drawPlant(.88f, .74f)
        drawDoor(.07f, .73f)
        if (target != null && !hero.moving) {
            val p = when (target) {
                Target.GUEST -> Offset(.77f, .38f)
                Target.CASHIER -> Offset(.30f, .45f)
                Target.KITCHEN -> Offset(.18f, .29f)
            }
            drawTargetMarker(p.x, p.y)
        }
        drawHero(size.width * hero.x, size.height * hero.y, hero.facing, hero.moving, hero.hasOrder, hero.hasBao)
        for (y in 0 until size.height.toInt() step 5) drawLine(Color(0xFFB1FFF1).copy(alpha = .025f), Offset(0f, y.toFloat()), Offset(size.width, y.toFloat()))
    }
}

private fun DrawScope.drawCafeFloor() {
    drawRect(Terminal)
    val left = size.width * .05f
    val top = size.height * .12f
    val width = size.width * .90f
    val height = size.height * .72f
    drawRect(Color(0xFF315154), topLeft = Offset(left, top), size = Size(width, height))
    val cols = 10
    val rows = 12
    val cw = width / cols
    val ch = height / rows
    for (r in 0 until rows) for (c in 0 until cols) {
        val tile = if ((r + c) % 2 == 0) Color(0xFF36595A) else Color(0xFF304F51)
        drawRect(tile, topLeft = Offset(left + c * cw, top + r * ch), size = Size(cw - 1f, ch - 1f))
    }
}

private fun DrawScope.drawCafeWalls() {
    drawRoundRect(Color(0xFF1A2627), Offset(size.width * .04f, size.height * .09f), Size(size.width * .92f, size.height * .035f), CornerRadius(10.dp.toPx()))
    drawRoundRect(Color(0xFF1A2627), Offset(size.width * .04f, size.height * .80f), Size(size.width * .92f, size.height * .035f), CornerRadius(10.dp.toPx()))
    drawRoundRect(Color(0xFF263C3D), Offset(size.width * .40f, size.height * .125f), Size(size.width * .18f, size.height * .12f), CornerRadius(12.dp.toPx()))
    drawRoundRect(Color(0xFF4B6B60), Offset(size.width * .415f, size.height * .145f), Size(size.width * .15f, size.height * .08f), CornerRadius(10.dp.toPx()))
}

private fun DrawScope.drawCounter(xNorm: Float, yNorm: Float, color: Color) {
    val x = size.width * xNorm; val y = size.height * yNorm
    drawRoundRect(color.copy(alpha = .94f), Offset(x - 42.dp.toPx(), y - 20.dp.toPx()), Size(84.dp.toPx(), 34.dp.toPx()), CornerRadius(8.dp.toPx()))
    drawRoundRect(Color(0xFF9B5A3D), Offset(x - 35.dp.toPx(), y - 11.dp.toPx()), Size(70.dp.toPx(), 10.dp.toPx()), CornerRadius(4.dp.toPx()))
    drawCircle(Color.White.copy(alpha = .7f), 5.dp.toPx(), Offset(x + 20.dp.toPx(), y - 2.dp.toPx()))
}

private fun DrawScope.drawKitchen(xNorm: Float, yNorm: Float) {
    val x = size.width * xNorm; val y = size.height * yNorm
    drawRoundRect(Color(0xFFE0AF6B), Offset(x - 40.dp.toPx(), y - 22.dp.toPx()), Size(80.dp.toPx(), 38.dp.toPx()), CornerRadius(9.dp.toPx()))
    drawRoundRect(Color(0xFF8D5935), Offset(x - 28.dp.toPx(), y - 8.dp.toPx()), Size(56.dp.toPx(), 13.dp.toPx()), CornerRadius(5.dp.toPx()))
    drawCircle(Color(0xFFFFF4C8), 8.dp.toPx(), Offset(x, y - 3.dp.toPx()))
}

private fun DrawScope.drawTable(xNorm: Float, yNorm: Float) {
    val x = size.width * xNorm; val y = size.height * yNorm
    drawCircle(Color(0xFF6F4A3E), 34.dp.toPx(), Offset(x, y))
    drawCircle(Color(0xFFC78963), 28.dp.toPx(), Offset(x, y))
    drawCircle(Color(0xFFFFE1B8), 7.dp.toPx(), Offset(x - 11.dp.toPx(), y - 3.dp.toPx()))
    drawCircle(Color(0xFFFFE1B8), 7.dp.toPx(), Offset(x + 11.dp.toPx(), y + 1.dp.toPx()))
}

private fun DrawScope.drawGuest(xNorm: Float, yNorm: Float) {
    val x = size.width * xNorm; val y = size.height * yNorm
    drawCircle(Color(0xFFFDE3C4), 9.dp.toPx(), Offset(x, y))
    drawCircle(Coral, 13.dp.toPx(), Offset(x, y + 18.dp.toPx()))
    drawCircle(Terminal, 2.dp.toPx(), Offset(x - 3.dp.toPx(), y - 1.dp.toPx()))
    drawCircle(Terminal, 2.dp.toPx(), Offset(x + 3.dp.toPx(), y - 1.dp.toPx()))
    drawSpeechBubble(x + 32.dp.toPx(), y - 4.dp.toPx())
}

private fun DrawScope.drawPlant(xNorm: Float, yNorm: Float) {
    val x = size.width * xNorm; val y = size.height * yNorm
    drawRoundRect(Color(0xFFA76B4D), Offset(x - 12.dp.toPx(), y + 2.dp.toPx()), Size(24.dp.toPx(), 19.dp.toPx()), CornerRadius(6.dp.toPx()))
    drawCircle(Mint, 13.dp.toPx(), Offset(x, y - 8.dp.toPx()))
    drawCircle(Color(0xFF7FB097), 10.dp.toPx(), Offset(x - 9.dp.toPx(), y - 6.dp.toPx()))
    drawCircle(Color(0xFF7FB097), 10.dp.toPx(), Offset(x + 9.dp.toPx(), y - 6.dp.toPx()))
}

private fun DrawScope.drawDoor(xNorm: Float, yNorm: Float) {
    val x = size.width * xNorm; val y = size.height * yNorm
    drawRoundRect(Color(0xFF6B4B37), Offset(x, y - 44.dp.toPx()), Size(40.dp.toPx(), 56.dp.toPx()), CornerRadius(7.dp.toPx()))
    drawRoundRect(Color(0xFFB88458), Offset(x + 6.dp.toPx(), y - 38.dp.toPx()), Size(28.dp.toPx(), 47.dp.toPx()), CornerRadius(5.dp.toPx()))
    drawCircle(Color(0xFFE6D2A6), 2.5.dp.toPx(), Offset(x + 28.dp.toPx(), y - 15.dp.toPx()))
}

private fun DrawScope.drawTargetMarker(xNorm: Float, yNorm: Float) {
    val x = size.width * xNorm; val y = size.height * yNorm
    drawCircle(Grid.copy(alpha = .35f), 23.dp.toPx(), Offset(x, y), style = Stroke(2.dp.toPx()))
    drawCircle(Grid.copy(alpha = .8f), 7.dp.toPx(), Offset(x, y), style = Stroke(2.dp.toPx()))
}

private fun DrawScope.drawHero(x: Float, y: Float, facing: Float, moving: Boolean, hasOrder: Boolean, hasBao: Boolean) {
    val bob = if (moving) sin(System.currentTimeMillis() / 85.0) * 2.5f else 0f
    val yy = y + bob
    val direction = if (cos(facing) >= 0) 1f else -1f
    drawOval(Color.Black.copy(alpha = .32f), Offset(x - 18.dp.toPx(), yy + 17.dp.toPx()), Size(36.dp.toPx(), 9.dp.toPx()))
    drawCircle(Color(0xFFFFD38F), 10.dp.toPx(), Offset(x, yy - 14.dp.toPx()))
    drawRoundRect(Color(0xFF5D9B80), Offset(x - 14.dp.toPx(), yy - 4.dp.toPx()), Size(28.dp.toPx(), 32.dp.toPx()), CornerRadius(10.dp.toPx()))
    drawCircle(Terminal, 2.2.dp.toPx(), Offset(x - 4.dp.toPx() * direction, yy - 15.dp.toPx()))
    drawCircle(Terminal, 2.2.dp.toPx(), Offset(x + 4.dp.toPx() * direction, yy - 15.dp.toPx()))
    drawLine(Cream, Offset(x + 8.dp.toPx() * direction, yy - 2.dp.toPx()), Offset(x + 13.dp.toPx() * direction, yy + 3.dp.toPx()), 3.dp.toPx(), StrokeCap.Round)
    if (hasOrder) drawRoundRect(Cream, Offset(x + 15.dp.toPx() * direction, yy - 1.dp.toPx()), Size(13.dp.toPx(), 9.dp.toPx()), CornerRadius(3.dp.toPx()))
    if (hasBao) drawCircle(Color(0xFFFFE3AA), 7.dp.toPx(), Offset(x + 20.dp.toPx() * direction, yy + 3.dp.toPx()))
}

private fun DrawScope.drawSpeechBubble(x: Float, y: Float) {
    drawRoundRect(Color(0xFFF8F4E8), Offset(x - 22.dp.toPx(), y - 14.dp.toPx()), Size(44.dp.toPx(), 27.dp.toPx()), CornerRadius(8.dp.toPx()))
    drawCircle(Coral, 4.dp.toPx(), Offset(x - 7.dp.toPx(), y - 1.dp.toPx()))
    drawCircle(Coral, 4.dp.toPx(), Offset(x + 7.dp.toPx(), y - 1.dp.toPx()))
}

@Composable
private fun CodeDock(program: List<Command>, available: List<Command>, running: Boolean, onAdd: (Command) -> Unit, onRemove: (Command) -> Unit, onRun: () -> Unit, modifier: Modifier = Modifier) {
    Column(modifier, verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Surface(color = DarkPanel.copy(alpha = .98f), shape = RoundedCornerShape(12.dp), modifier = Modifier.weight(1.35f).fillMaxWidth().border(1.dp, Grid.copy(alpha = .75f), RoundedCornerShape(12.dp))) {
            Column(Modifier.fillMaxSize().padding(10.dp)) {
                Text("// ТВОЙ КОД", color = Grid, fontSize = 12.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Black)
                Box(Modifier.weight(1f).fillMaxWidth().padding(top = 5.dp).clip(RoundedCornerShape(6.dp)).background(Color(0xFF101719)).padding(8.dp)) {
                    if (program.isEmpty()) Text("// Выбери строку снизу", color = Color(0xFF98AAA9), fontFamily = FontFamily.Monospace, fontSize = 12.sp)
                    else Column { program.forEachIndexed { index, command -> Text("${index + 1}. ${command.source}", color = Color(0xFFFFD38F), fontFamily = FontFamily.Monospace, fontSize = 11.sp, modifier = Modifier.clickable(enabled = !running) { onRemove(command) }) } }
                }
            }
        }
        Surface(color = DarkPanel.copy(alpha = .98f), shape = RoundedCornerShape(12.dp), modifier = Modifier.weight(.8f).fillMaxWidth().border(1.dp, Grid.copy(alpha = .75f), RoundedCornerShape(12.dp))) {
            Column(Modifier.fillMaxSize().padding(8.dp)) {
                Text("// ВСТАВИТЬ КОМАНДУ", color = Grid, fontSize = 10.sp, fontFamily = FontFamily.Monospace)
                Row(Modifier.weight(1f).fillMaxWidth().padding(top = 4.dp), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    available.take(4).forEach { command -> Text(command.source, color = Ink, fontFamily = FontFamily.Monospace, fontSize = 9.sp, modifier = Modifier.weight(1f).fillMaxHeight().clip(RoundedCornerShape(5.dp)).background(Cream).clickable(enabled = !running) { onAdd(command) }.padding(4.dp)) }
                    Button(onClick = onRun, enabled = program.isNotEmpty() && !running, colors = ButtonDefaults.buttonColors(containerColor = Grid, contentColor = Terminal), modifier = Modifier.width(50.dp).fillMaxHeight()) { Text("▶", fontWeight = FontWeight.Black) }
                }
            }
        }
    }
}
