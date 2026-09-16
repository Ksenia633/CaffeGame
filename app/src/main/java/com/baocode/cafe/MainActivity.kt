package com.baocode.cafe

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
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
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay

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

@Composable private fun BaoCodeGame() {
    var program by remember { mutableStateOf(emptyList<Command>()) }
    var isRunning by remember { mutableStateOf(false) }
    var action by remember { mutableStateOf("Собери команды для первого гостя") }
    var served by remember { mutableIntStateOf(0) }
    var heroStep by remember { mutableIntStateOf(-1) }
    val available = commands.filterNot { candidate -> program.any { it.id == candidate.id } }

    LaunchedEffect(isRunning) {
        if (!isRunning) return@LaunchedEffect
        if (program.map { it.id } != solution) { action = "Ой! Бэби Бао запутался. Проверь порядок и лишние команды."; delay(1300); isRunning = false; return@LaunchedEffect }
        program.forEachIndexed { index, command -> action = command.label; heroStep = index; delay(800) }
        served++; action = "Заказ доставлен! +1 счастливая булочка ✨"; isRunning = false
    }
    Box(Modifier.fillMaxSize().background(Terminal)) {
        Image(painter = painterResource(R.drawable.cafe_reference_layout), contentDescription = "Карта кафе", modifier = Modifier.fillMaxSize().graphicsLayer(scaleX = 1.72f, scaleY = 1.72f), contentScale = ContentScale.Crop)
        WeatherPanel(Modifier.align(Alignment.TopEnd).padding(12.dp).width(178.dp))
        Text("ЗАКАЗ: 2 × бао", color = Ink, fontSize = 11.sp, fontWeight = FontWeight.Bold, modifier = Modifier.align(BiasAlignment(.08f, -.22f)).clip(RoundedCornerShape(7.dp)).background(Cream).padding(horizontal = 7.dp, vertical = 4.dp))
        Text(action, color = Color.White, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center, modifier = Modifier.align(BiasAlignment(.10f, .35f)).clip(RoundedCornerShape(12.dp)).background(Terminal.copy(alpha = .91f)).padding(horizontal = 12.dp, vertical = 6.dp))
        CodeDock(program, available, isRunning, { program = program + it }, { program = program.filterNot { current -> current.id == it.id } }, { isRunning = true }, Modifier.align(Alignment.BottomStart).padding(12.dp).fillMaxWidth(.42f).height(230.dp))
    }
}

@Composable private fun TerminalPanel(modifier: Modifier = Modifier) {
    Column(modifier.clip(RoundedCornerShape(10.dp)).background(Terminal.copy(alpha = .94f)).border(1.dp, Grid.copy(alpha = .8f), RoundedCornerShape(10.dp)).padding(9.dp)) {
        Text("// КАФЕ.БАО", color = Grid, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold, fontSize = 12.sp)
        Text("Смена № 01", color = Cream, fontSize = 12.sp); Spacer(Modifier.height(8.dp))
        Text("МЕНЮ", color = Grid, fontSize = 11.sp, fontFamily = FontFamily.Monospace)
        Text("[1] Заказы", color = Cream, fontSize = 12.sp); Text("[2] Рецепты", color = Cream, fontSize = 12.sp); Text("[3] Команда", color = Cream, fontSize = 12.sp)
        Spacer(Modifier.height(8.dp)); HorizontalDivider(color = Grid.copy(alpha = .35f))
        Text("СКЛАД", color = Grid, fontSize = 11.sp, fontFamily = FontFamily.Monospace); Text("Мука        8 кг", color = Cream, fontSize = 11.sp); Text("Бамбук      12 шт", color = Cream, fontSize = 11.sp); Text("Выручка     240 ₽", color = Color(0xFFFFD38F), fontSize = 11.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable private fun GuestsPanel(modifier: Modifier = Modifier) {
    Column(modifier.clip(RoundedCornerShape(10.dp)).background(Terminal.copy(alpha = .94f)).border(1.dp, Grid.copy(alpha = .8f), RoundedCornerShape(10.dp)).padding(9.dp)) {
        Text("// ГОСТИ.СЕГОДНЯ", color = Grid, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold, fontSize = 11.sp)
        Text("1 / 6", color = Cream, fontSize = 12.sp); Spacer(Modifier.height(6.dp))
        Text("●  Студентка", color = Grid, fontSize = 12.sp, fontWeight = FontWeight.Bold); Text("   ждёт заказ", color = Cream, fontSize = 10.sp)
        Spacer(Modifier.height(6.dp)); Text("○  Семья", color = Color(0xFF8FA3A1), fontSize = 11.sp); Text("○  Путешественник", color = Color(0xFF8FA3A1), fontSize = 11.sp); Text("○  Постоянный гость", color = Color(0xFF8FA3A1), fontSize = 11.sp)
        Spacer(Modifier.height(8.dp)); HorizontalDivider(color = Grid.copy(alpha = .35f)); Text("ЦЕЛЬ", color = Grid, fontSize = 11.sp, fontFamily = FontFamily.Monospace); Text("Подать горячие бао\nи заработать 120 ₽", color = Cream, fontSize = 11.sp)
    }
}

@Composable private fun WeatherPanel(modifier: Modifier = Modifier) {
    Column(modifier.clip(RoundedCornerShape(10.dp)).background(Terminal.copy(alpha = .95f)).border(1.dp, Grid.copy(alpha = .8f), RoundedCornerShape(10.dp)).padding(9.dp)) {
        Text("16.09.2026  ·  19:43", color = Cream, fontFamily = FontFamily.Monospace, fontSize = 12.sp)
        Text("ТОКИО, ЯПОНИЯ", color = Grid, fontFamily = FontFamily.Monospace, fontSize = 11.sp)
        Text("☔  Дождь  ·  +18°C", color = Cream, fontSize = 12.sp)
    }
}

@Composable private fun CafeWorld(heroStep: Int) {
    val heroTile = when (heroStep) { 0, 1 -> 5 to 2; 2 -> 2 to 2; 3 -> 1 to 3; 4, 5 -> 5 to 2; else -> 3 to 3 }
    Canvas(Modifier.fillMaxSize()) {
        drawRect(Terminal)
        val tileW = size.width / 7.6f; val tileH = tileW * .48f
        fun point(col: Int, row: Int) = androidx.compose.ui.geometry.Offset(size.width * .50f + (col - row) * tileW / 2, 20.dp.toPx() + (col + row) * tileH / 2)
        for (row in 0..5) for (col in 0..7) { val p = point(col, row); diamond(p.x, p.y, tileW, tileH, if ((col + row) % 2 == 0) Color(0xFF234042) else Color(0xFF1F383A)) }
        val counter = point(2, 2); drawFurniture(counter, tileW, tileH, "КАССА", Coral)
        val kitchen = point(1, 3); drawFurniture(kitchen, tileW, tileH, "БАО", Color(0xFFFFD38F))
        val table = point(5, 2); drawFurniture(table, tileW, tileH, "СТОЛИК", Mint)
        drawGuest(table.x + tileW * .12f, table.y - tileH * .42f)
        drawSpeechBubble(table.x + tileW * .46f, table.y - tileH * .72f)
        val hero = point(heroTile.first, heroTile.second); drawHero(hero.x - tileW * .22f, hero.y - tileH * .18f)
        for (y in 0 until size.height.toInt() step 5) drawLine(Color(0xFFB1FFF1).copy(alpha = .035f), androidx.compose.ui.geometry.Offset(0f, y.toFloat()), androidx.compose.ui.geometry.Offset(size.width, y.toFloat()))
    }
}

private fun DrawScope.diamond(x: Float, y: Float, w: Float, h: Float, fill: Color) { val path = Path().apply { moveTo(x, y - h / 2); lineTo(x + w / 2, y); lineTo(x, y + h / 2); lineTo(x - w / 2, y); close() }; drawPath(path, fill); drawPath(path, Grid.copy(alpha = .42f), style = Stroke(1.dp.toPx())) }
private fun DrawScope.drawFurniture(p: androidx.compose.ui.geometry.Offset, w: Float, h: Float, label: String, color: Color) { drawRoundRect(color = color.copy(alpha = .82f), topLeft = androidx.compose.ui.geometry.Offset(p.x - w * .22f, p.y - h * .42f), size = androidx.compose.ui.geometry.Size(w * .44f, h * .38f), cornerRadius = CornerRadius(5.dp.toPx())); drawLine(color, androidx.compose.ui.geometry.Offset(p.x - w * .28f, p.y - h * .02f), androidx.compose.ui.geometry.Offset(p.x + w * .28f, p.y + h * .18f), 2.dp.toPx()) }
private fun DrawScope.drawGuest(x: Float, y: Float) { drawCircle(Color(0xFFFDE3C4), 8.dp.toPx(), androidx.compose.ui.geometry.Offset(x, y)); drawCircle(Coral, 11.dp.toPx(), androidx.compose.ui.geometry.Offset(x, y + 13.dp.toPx())) }
private fun DrawScope.drawHero(x: Float, y: Float) { drawCircle(Color(0xFFFFD38F), 10.dp.toPx(), androidx.compose.ui.geometry.Offset(x, y)); drawCircle(Grid, 14.dp.toPx(), androidx.compose.ui.geometry.Offset(x, y + 16.dp.toPx())); drawCircle(Terminal, 2.dp.toPx(), androidx.compose.ui.geometry.Offset(x - 4.dp.toPx(), y - 1.dp.toPx())); drawCircle(Terminal, 2.dp.toPx(), androidx.compose.ui.geometry.Offset(x + 4.dp.toPx(), y - 1.dp.toPx())) }
private fun DrawScope.drawSpeechBubble(x: Float, y: Float) { drawRoundRect(color = Color(0xFFF8F4E8), topLeft = androidx.compose.ui.geometry.Offset(x - 20.dp.toPx(), y - 14.dp.toPx()), size = androidx.compose.ui.geometry.Size(40.dp.toPx(), 25.dp.toPx()), cornerRadius = CornerRadius(8.dp.toPx())); drawCircle(Coral, 4.dp.toPx(), androidx.compose.ui.geometry.Offset(x - 6.dp.toPx(), y - 2.dp.toPx())); drawCircle(Coral, 4.dp.toPx(), androidx.compose.ui.geometry.Offset(x + 6.dp.toPx(), y - 2.dp.toPx())) }

@Composable private fun CodeDock(program: List<Command>, available: List<Command>, running: Boolean, onAdd: (Command) -> Unit, onRemove: (Command) -> Unit, onRun: () -> Unit, modifier: Modifier = Modifier) {
    Column(modifier, verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Surface(color = DarkPanel.copy(alpha = .98f), shape = RoundedCornerShape(12.dp), modifier = Modifier.weight(1.35f).fillMaxWidth().border(1.dp, Grid.copy(alpha = .75f), RoundedCornerShape(12.dp))) {
            Column(Modifier.fillMaxSize().padding(10.dp)) { Text("// ТВОЙ КОД", color = Grid, fontSize = 12.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Black); Box(Modifier.weight(1f).fillMaxWidth().padding(top = 5.dp).clip(RoundedCornerShape(6.dp)).background(Color(0xFF101719)).padding(8.dp)) { if (program.isEmpty()) Text("// Выбери строку снизу", color = Color(0xFF98AAA9), fontFamily = FontFamily.Monospace, fontSize = 12.sp) else Column { program.forEachIndexed { index, command -> Text("${index + 1}. ${command.source}", color = Color(0xFFFFD38F), fontFamily = FontFamily.Monospace, fontSize = 11.sp, modifier = Modifier.clickable(enabled = !running) { onRemove(command) }) } } } }
        }
        Surface(color = DarkPanel.copy(alpha = .98f), shape = RoundedCornerShape(12.dp), modifier = Modifier.weight(.8f).fillMaxWidth().border(1.dp, Grid.copy(alpha = .75f), RoundedCornerShape(12.dp))) {
            Column(Modifier.fillMaxSize().padding(8.dp)) { Text("// ВСТАВИТЬ КОМАНДУ", color = Grid, fontSize = 10.sp, fontFamily = FontFamily.Monospace); Row(Modifier.weight(1f).fillMaxWidth().padding(top = 4.dp), horizontalArrangement = Arrangement.spacedBy(4.dp)) { available.take(4).forEach { command -> Text(command.source, color = Ink, fontFamily = FontFamily.Monospace, fontSize = 9.sp, modifier = Modifier.weight(1f).fillMaxHeight().clip(RoundedCornerShape(5.dp)).background(Cream).clickable(enabled = !running) { onAdd(command) }.padding(4.dp)) }; Button(onClick = onRun, enabled = program.isNotEmpty() && !running, colors = ButtonDefaults.buttonColors(containerColor = Grid, contentColor = Terminal), modifier = Modifier.width(50.dp).fillMaxHeight()) { Text("▶", fontWeight = FontWeight.Black) } } }
        }
    }
}
