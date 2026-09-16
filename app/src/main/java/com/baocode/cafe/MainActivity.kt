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
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlin.math.abs
import kotlin.math.atan2
import kotlin.math.max
import kotlin.math.min

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) = super.onCreate(savedInstanceState).also {
        setContent { MaterialTheme(colorScheme = darkColorScheme(primary = Neon)) { BaoCodeGame() } }
    }
}

private val Neon = Color(0xFF55F0B2)
private val NeonSoft = Color(0xFF36B987)
private val Ink = Color(0xFFEAF7F0)
private val Terminal = Color(0xFF061011)
private val Panel = Color(0xED071617)
private val Panel2 = Color(0xFF0B1C1D)
private val WoodDark = Color(0xFF4A2921)
private val Wood = Color(0xFF8D5032)
private val WoodLight = Color(0xFFC7834A)
private val FloorA = Color(0xFF8A5539)
private val FloorB = Color(0xFF73432F)
private val FloorEdge = Color(0xFF3A2521)
private val Cream = Color(0xFFFFF2D9)
private val Pink = Color(0xFFE88BA9)
private val Red = Color(0xFFD85B45)
private val Green = Color(0xFF4F9A67)
private val Blue = Color(0xFF426D8D)

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

private enum class TileType { FLOOR, WALL, COUNTER, KITCHEN, TABLE, CHAIR, PLANT, DOOR, RUG, SHELF }
private enum class Target { GUEST, CASHIER, KITCHEN }
private data class GridPos(val col: Int, val row: Int)
private data class HeroState(
    val cell: GridPos = GridPos(7, 7),
    val renderX: Float = 7f,
    val renderY: Float = 7f,
    val facing: Int = 1,
    val moving: Boolean = false,
    val hasOrder: Boolean = false,
    val hasBao: Boolean = false
)

private val guestCell = GridPos(11, 2)
private val cashierCell = GridPos(4, 2)
private val kitchenCell = GridPos(2, 2)

@Composable
private fun BaoCodeGame() {
    var program by remember { mutableStateOf(emptyList<Command>()) }
    var isRunning by remember { mutableStateOf(false) }
    var action by remember { mutableStateOf("Собери программу для первого гостя") }
    var served by remember { mutableIntStateOf(0) }
    var hero by remember { mutableStateOf(HeroState()) }
    var target by remember { mutableStateOf<Target?>(null) }

    suspend fun moveTo(targetCell: GridPos, targetType: Target) {
        target = targetType
        val start = hero.cell
        val distance = abs(targetCell.col - start.col) + abs(targetCell.row - start.row)
        var current = start
        hero = hero.copy(moving = true, facing = if (targetCell.col >= start.col) 1 else -1)
        repeat(distance) {
            val dx = targetCell.col - current.col
            val dy = targetCell.row - current.row
            current = if (abs(dx) >= abs(dy)) {
                current.copy(col = current.col + if (dx > 0) 1 else -1)
            } else {
                current.copy(row = current.row + if (dy > 0) 1 else -1)
            }
            val sx = hero.renderX
            val sy = hero.renderY
            val ex = current.col.toFloat()
            val ey = current.row.toFloat()
            for (step in 1..7) {
                val t = step / 7f
                hero = hero.copy(renderX = sx + (ex - sx) * t, renderY = sy + (ey - sy) * t, moving = true)
                delay(24)
            }
            hero = hero.copy(cell = current)
        }
        hero = hero.copy(renderX = targetCell.col.toFloat(), renderY = targetCell.row.toFloat(), moving = false)
        target = null
    }

    LaunchedEffect(isRunning) {
        if (!isRunning) return@LaunchedEffect
        if (program.map { it.id } != solution) {
            action = "Ошибка: порядок команд нарушен. Бэби Бао не знает, что делать."
            delay(1300)
            isRunning = false
            return@LaunchedEffect
        }
        program.forEach { command ->
            action = command.label
            when (command.id) {
                "to_guest" -> moveTo(guestCell, Target.GUEST)
                "take" -> { hero = hero.copy(hasOrder = true); delay(650) }
                "to_counter" -> moveTo(cashierCell, Target.CASHIER)
                "take_bao" -> { moveTo(kitchenCell, Target.KITCHEN); hero = hero.copy(hasBao = true); delay(650) }
                "return" -> moveTo(guestCell, Target.GUEST)
                "serve" -> { hero = hero.copy(hasOrder = false, hasBao = false); delay(850) }
            }
        }
        served++
        action = "Заказ доставлен! Гость счастлив ✦"
        isRunning = false
    }

    Row(Modifier.fillMaxSize().background(Terminal)) {
        Column(
            modifier = Modifier.width(286.dp).fillMaxHeight().padding(9.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            ProgramPanel(program, isRunning, { command -> program = program.filterNot { it.id == command.id } }, Modifier.weight(1.12f).fillMaxWidth())
            CommandsPanel(program, isRunning, commands, { command -> program = program + command }, { if (program.isNotEmpty()) isRunning = true }, Modifier.weight(.88f).fillMaxWidth())
        }
        Box(Modifier.weight(1f).fillMaxHeight()) {
            CafeWorld(hero, target)
            Surface(
                color = Panel,
                shape = RoundedCornerShape(7.dp),
                modifier = Modifier.align(Alignment.TopEnd).padding(9.dp).width(165.dp).border(1.dp, Neon.copy(alpha = .72f), RoundedCornerShape(7.dp))
            ) {
                Column(Modifier.padding(horizontal = 10.dp, vertical = 8.dp)) {
                    Text("16.09.2026   19:43", color = Ink, fontFamily = FontFamily.Monospace, fontSize = 10.sp)
                    Text("TOKYO, JP", color = Neon, fontFamily = FontFamily.Monospace, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    Text("☔  +18°C  ·  Дождь", color = Ink, fontSize = 10.sp)
                }
            }
            Surface(color = Terminal.copy(alpha = .9f), shape = RoundedCornerShape(8.dp), modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 10.dp)) {
                Text(action, color = Neon, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold, fontSize = 10.sp, textAlign = TextAlign.Center, modifier = Modifier.padding(horizontal = 13.dp, vertical = 7.dp))
            }
        }
    }
}

@Composable
private fun ProgramPanel(program: List<Command>, running: Boolean, onRemove: (Command) -> Unit, modifier: Modifier = Modifier) {
    Surface(color = Panel, shape = RoundedCornerShape(8.dp), modifier = modifier.border(1.dp, Neon.copy(alpha = .72f), RoundedCornerShape(8.dp))) {
        Column(Modifier.fillMaxSize().padding(9.dp)) {
            Text("// PROGRAM", color = Neon, fontFamily = FontFamily.Monospace, fontSize = 11.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(6.dp))
            Column(Modifier.fillMaxSize().clip(RoundedCornerShape(5.dp)).background(Color(0xFF030A0B)).padding(8.dp).verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                if (program.isEmpty()) Text("// выбери команду\n// ниже", color = Color(0xFF66807A), fontFamily = FontFamily.Monospace, fontSize = 10.sp)
                else program.forEachIndexed { index, command ->
                    Text("${index + 1}  ${command.source}", color = if (running) Color(0xFF6B7775) else Cream, fontFamily = FontFamily.Monospace, fontSize = 10.sp, modifier = Modifier.clickable(enabled = !running) { onRemove(command) })
                }
            }
        }
    }
}

@Composable
private fun CommandsPanel(program: List<Command>, running: Boolean, available: List<Command>, onAdd: (Command) -> Unit, onRun: () -> Unit, modifier: Modifier = Modifier) {
    Surface(color = Panel, shape = RoundedCornerShape(8.dp), modifier = modifier.border(1.dp, Neon.copy(alpha = .72f), RoundedCornerShape(8.dp))) {
        Column(Modifier.fillMaxSize().padding(9.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                Text("// COMMANDS", color = Neon, fontFamily = FontFamily.Monospace, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                Button(onClick = onRun, enabled = !running && program.isNotEmpty(), contentPadding = PaddingValues(horizontal = 9.dp, vertical = 0.dp), colors = ButtonDefaults.buttonColors(containerColor = Neon, contentColor = Terminal), modifier = Modifier.height(29.dp)) { Text("▶ RUN", fontSize = 9.sp, fontWeight = FontWeight.Black) }
            }
            Spacer(Modifier.height(6.dp))
            Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(5.dp)) {
                available.forEach { command ->
                    val count = program.count { it.id == command.id }
                    Surface(color = if (count > 0) Color(0xFF102A24) else Panel2, shape = RoundedCornerShape(5.dp), modifier = Modifier.fillMaxWidth().border(1.dp, if (count > 0) NeonSoft else Color(0xFF1A3535), RoundedCornerShape(5.dp)).clickable(enabled = !running) { onAdd(command) }) {
                        Row(Modifier.padding(horizontal = 8.dp, vertical = 6.dp), verticalAlignment = Alignment.CenterVertically) {
                            Text(command.source, color = Cream, fontFamily = FontFamily.Monospace, fontSize = 9.sp, modifier = Modifier.weight(1f))
                            if (count > 0) Text("×$count", color = Neon, fontFamily = FontFamily.Monospace, fontSize = 8.sp)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CafeWorld(hero: HeroState, target: Target?) {
    Canvas(Modifier.fillMaxSize()) {
        val cols = 14
        val rows = 10
        val tileW = min(size.width / 17f, size.height / 10f)
        val tileH = tileW * .50f
        val origin = Offset(size.width * .49f, size.height * .17f)
        fun center(col: Float, row: Float): Offset = Offset(origin.x + (col - row) * tileW * .5f, origin.y + (col + row) * tileH * .5f)

        drawRect(Color(0xFF020708))
        drawAmbientGlow(size.width * .52f, size.height * .55f, size.width * .62f)
        for (r in 0 until rows) for (c in 0 until cols) drawIsoTile(center(c.toFloat(), r.toFloat()), tileW, tileH, tileAt(c, r))
        drawBackWalls(::center, tileW, tileH, cols, rows)
        drawCafeDecor(::center, tileW, tileH)
        drawTarget(::center, tileW, tileH, target, hero)
        drawGuestSprite(center(guestCell.col.toFloat(), guestCell.row.toFloat()), tileW * .20f)
        drawHeroSprite(center(hero.renderX, hero.renderY), tileW * .22f, hero.facing, hero.moving, hero.hasOrder, hero.hasBao)
        drawForegroundEdge(::center, tileW, tileH, cols, rows)
    }
}

private fun tileAt(c: Int, r: Int): TileType {
    if (r == 0 || c == 0 || r == 9 || c == 13) return TileType.WALL
    if (c == 2 && r in 1..4) return TileType.KITCHEN
    if (c == 4 && r == 2) return TileType.COUNTER
    if ((c == 9 && r == 2) || (c == 11 && r == 5) || (c == 6 && r == 7)) return TileType.TABLE
    if ((c == 8 && r == 3) || (c == 10 && r == 6) || (c == 5 && r == 6)) return TileType.CHAIR
    if ((c == 12 && r == 7) || (c == 3 && r == 6) || (c == 12 && r == 3)) return TileType.PLANT
    if (c == 6 && r == 1) return TileType.SHELF
    if (c == 13 && r == 5) return TileType.DOOR
    if (c in 5..9 && r in 4..6) return TileType.RUG
    return TileType.FLOOR
}

private fun DrawScope.drawAmbientGlow(x: Float, y: Float, radius: Float) {
    for (i in 5 downTo 1) drawCircle(Color(0xFFFFA05A).copy(alpha = .012f * i), radius * i / 5f, Offset(x, y))
}

private fun DrawScope.drawIsoTile(center: Offset, tileW: Float, tileH: Float, type: TileType) {
    val path = isoPath(center, tileW, tileH)
    val base = when (type) {
        TileType.WALL -> Color(0xFF27383C)
        TileType.RUG -> Color(0xFF6B3E3A)
        else -> if (((center.x + center.y).toInt() / 7) % 2 == 0) FloorA else FloorB
    }
    drawPath(path, base)
    drawPath(path, FloorEdge.copy(alpha = .72f), style = Stroke(width = 1.2f))
    if (type == TileType.FLOOR || type == TileType.RUG) {
        drawLine(Color(0xFFE6A36A).copy(alpha = .16f), Offset(center.x - tileW * .22f, center.y), Offset(center.x + tileW * .22f, center.y), 1f)
    }
}

private fun DrawScope.drawBackWalls(center: (Float, Float) -> Offset, tileW: Float, tileH: Float, cols: Int, rows: Int) {
    for (c in 0 until cols) drawWallBlock(center(c.toFloat(), 0f), tileW, tileH, Color(0xFF34494C), Color(0xFF1C2C30))
    for (r in 1 until rows) drawWallBlock(center(0f, r.toFloat()), tileW, tileH, Color(0xFF304447), Color(0xFF1A2A2D))
    drawIsoPanel(center(5.7f, .15f), tileW * 1.55f, tileH * 2.2f, Color(0xFF51352D), Color(0xFFFFD18A))
    drawIsoPanel(center(9.2f, .10f), tileW * 1.8f, tileH * 2.35f, Color(0xFF47302B), Color(0xFFFFB56D))
}

private fun DrawScope.drawWallBlock(center: Offset, tileW: Float, tileH: Float, front: Color, side: Color) {
    drawPath(isoPath(center, tileW, tileH), front)
    val h = tileH * 2.0f
    val leftTop = Offset(center.x - tileW * .5f, center.y)
    val rightTop = Offset(center.x, center.y + tileH * .5f)
    val leftBottom = Offset(leftTop.x, leftTop.y + h)
    val rightBottom = Offset(rightTop.x, rightTop.y + h)
    val left = Path().apply { moveTo(leftTop.x, leftTop.y); lineTo(center.x, center.y - tileH * .5f); lineTo(center.x, center.y - tileH * .5f + h); lineTo(leftBottom.x, leftBottom.y); close() }
    val right = Path().apply { moveTo(center.x, center.y - tileH * .5f); lineTo(center.x + tileW * .5f, center.y); lineTo(rightBottom.x, rightBottom.y); lineTo(center.x, center.y - tileH * .5f + h); close() }
    drawPath(left, side)
    drawPath(right, Color(0xFF203235))
    for (i in 0..2) {
        val yy = center.y + i * tileH * .55f
        drawLine(Color.White.copy(alpha = .035f), Offset(center.x - tileW * .43f, yy - tileH * .18f), Offset(center.x + tileW * .43f, yy + tileH * .18f), 1f)
    }
}

private fun DrawScope.drawIsoPanel(center: Offset, width: Float, height: Float, body: Color, light: Color) {
    drawRoundRect(body, Offset(center.x - width * .5f, center.y - height * .5f), Size(width, height), CornerRadius(4.dp.toPx()))
    drawRoundRect(light.copy(alpha = .28f), Offset(center.x - width * .43f, center.y - height * .38f), Size(width * .86f, height * .76f), CornerRadius(3.dp.toPx()), style = Stroke(width = 2f))
}

private fun DrawScope.drawCafeDecor(center: (Float, Float) -> Offset, tileW: Float, tileH: Float) {
    drawKitchenFurniture(center(2f, 2f), tileW, tileH)
    drawCounterFurniture(center(4f, 2f), tileW, tileH)
    drawTableFurniture(center(9f, 2f), tileW, tileH, false)
    drawTableFurniture(center(11f, 5f), tileW, tileH, true)
    drawTableFurniture(center(6f, 7f), tileW, tileH, false)
    drawShelf(center(6f, 1f), tileW, tileH)
    drawPlant(center(12f, 7f), tileW, tileH)
    drawPlant(center(3f, 6f), tileW, tileH)
    drawPlant(center(12f, 3f), tileW, tileH)
    drawDoorFurniture(center(13f, 5f), tileW, tileH)
    drawLamp(center(7.2f, 1.3f), tileW)
    drawLamp(center(10.8f, 1.8f), tileW)
}

private fun DrawScope.drawKitchenFurniture(p: Offset, tileW: Float, tileH: Float) {
    drawIsoBox(p, tileW * .92f, tileH * .72f, tileH * .58f, Color(0xFF6D3B2D), Color(0xFF3A2521))
    drawRoundRect(Color(0xFF242A29), Offset(p.x - tileW * .30f, p.y - tileH * .38f), Size(tileW * .60f, tileH * .45f), CornerRadius(5.dp.toPx()))
    drawCircle(Color(0xFFFFB35E), tileW * .07f, Offset(p.x - tileW * .16f, p.y - tileH * .18f))
    drawCircle(Color(0xFFB6E3D2), tileW * .07f, Offset(p.x + tileW * .15f, p.y - tileH * .18f))
    drawLine(Color.White.copy(alpha = .35f), Offset(p.x - tileW * .27f, p.y - tileH * .36f), Offset(p.x + tileW * .27f, p.y - tileH * .36f), 2f)
}

private fun DrawScope.drawCounterFurniture(p: Offset, tileW: Float, tileH: Float) {
    drawIsoBox(p, tileW * 1.05f, tileH * .70f, tileH * .75f, Red, Color(0xFF6A3028))
    drawRoundRect(Color(0xFFB06B40), Offset(p.x - tileW * .35f, p.y - tileH * .25f), Size(tileW * .70f, tileH * .20f), CornerRadius(3.dp.toPx()))
    drawCircle(Cream.copy(alpha = .85f), tileW * .055f, Offset(p.x + tileW * .22f, p.y - tileH * .16f))
    drawRoundRect(Color(0xFF202827), Offset(p.x - tileW * .13f, p.y - tileH * .52f), Size(tileW * .26f, tileH * .22f), CornerRadius(2.dp.toPx()))
}

private fun DrawScope.drawTableFurniture(p: Offset, tileW: Float, tileH: Float, occupied: Boolean) {
    drawOval(Color.Black.copy(alpha = .25f), Offset(p.x - tileW * .42f, p.y + tileH * .45f), Size(tileW * .85f, tileH * .42f))
    drawIsoBox(p, tileW * .85f, tileH * .60f, tileH * .68f, WoodLight, WoodDark)
    drawLine(Color(0xFFE4AA6A), Offset(p.x - tileW * .22f, p.y), Offset(p.x + tileW * .22f, p.y), 1.5f)
    if (occupied) {
        drawPlate(p + Offset(tileW * .05f, -tileH * .12f), tileW * .13f)
        drawBao(p + Offset(-tileW * .10f, -tileH * .15f), tileW * .08f)
    } else {
        drawPlate(p + Offset(0f, -tileH * .12f), tileW * .12f)
        drawCup(p + Offset(tileW * .18f, -tileH * .16f), tileW * .055f)
    }
}

private fun DrawScope.drawShelf(p: Offset, tileW: Float, tileH: Float) {
    drawRoundRect(Color(0xFF4B3028), Offset(p.x - tileW * .42f, p.y - tileH * 1.15f), Size(tileW * .84f, tileH * 1.35f), CornerRadius(3.dp.toPx()))
    for (i in 0..2) {
        val y = p.y - tileH * (.82f - i * .38f)
        drawLine(WoodLight, Offset(p.x - tileW * .35f, y), Offset(p.x + tileW * .35f, y), 2f)
        drawCircle(Green, tileW * .045f, Offset(p.x - tileW * .22f, y - tileH * .12f))
        drawCircle(Pink, tileW * .045f, Offset(p.x + tileW * .02f, y - tileH * .12f))
        drawCircle(Color(0xFFE0B35B), tileW * .045f, Offset(p.x + tileW * .23f, y - tileH * .12f))
    }
}

private fun DrawScope.drawPlant(p: Offset, tileW: Float, tileH: Float) {
    drawIsoBox(p + Offset(0f, tileH * .22f), tileW * .30f, tileH * .28f, tileH * .32f, Wood, WoodDark)
    val stem = p - Offset(0f, tileH * .35f)
    drawLine(Color(0xFF4C7B49), p, stem, 2f)
    drawCircle(Color(0xFF5BA66A), tileW * .13f, stem + Offset(-tileW * .13f, 0f))
    drawCircle(Color(0xFF6FBA78), tileW * .14f, stem + Offset(tileW * .10f, -tileH * .08f))
    drawCircle(Color(0xFF3E8658), tileW * .10f, stem + Offset(0f, -tileH * .20f))
}

private fun DrawScope.drawDoorFurniture(p: Offset, tileW: Float, tileH: Float) {
    drawRoundRect(Color(0xFF2B1D1D), Offset(p.x - tileW * .35f, p.y - tileH * .80f), Size(tileW * .70f, tileH * 1.35f), CornerRadius(4.dp.toPx()))
    drawRoundRect(Color(0xFFB76D46), Offset(p.x - tileW * .27f, p.y - tileH * .68f), Size(tileW * .54f, tileH * 1.18f), CornerRadius(3.dp.toPx()))
    drawCircle(Color(0xFFFFC36B), tileW * .035f, Offset(p.x + tileW * .17f, p.y - tileH * .05f))
    drawRoundRect(Color(0xFF426D8D), Offset(p.x - tileW * .40f, p.y - tileH * 1.02f), Size(tileW * .80f, tileH * .28f), CornerRadius(3.dp.toPx()))
}

private fun DrawScope.drawLamp(p: Offset, tileW: Float) {
    drawLine(Color(0xFF171D1D), p - Offset(0f, tileW * .34f), p - Offset(0f, tileW * .08f), 2f)
    drawCircle(Color(0xFFFFD07B).copy(alpha = .18f), tileW * .30f, p)
    drawCircle(Color(0xFFFFC267), tileW * .09f, p)
    drawCircle(Color(0xFFFFE3A8), tileW * .045f, p - Offset(tileW * .02f, tileW * .02f))
}

private fun DrawScope.drawTarget(center: (Float, Float) -> Offset, tileW: Float, tileH: Float, target: Target?, hero: HeroState) {
    if (target == null || hero.moving) return
    val cell = when (target) { Target.GUEST -> guestCell; Target.CASHIER -> cashierCell; Target.KITCHEN -> kitchenCell }
    val p = center(cell.col.toFloat(), cell.row.toFloat())
    drawIsoTile(p + Offset(0f, tileH * .03f), tileW * .78f, tileH * .50f, TileType.RUG)
    drawLine(Neon, p - Offset(tileW * .28f, 0f), p + Offset(tileW * .28f, 0f), 3f, StrokeCap.Round)
    drawLine(Neon, p - Offset(0f, tileH * .16f), p + Offset(0f, tileH * .16f), 3f, StrokeCap.Round)
}

private fun DrawScope.drawGuestSprite(p: Offset, scale: Float) {
    drawOval(Color.Black.copy(alpha = .32f), Offset(p.x - scale * .75f, p.y + scale * .42f), Size(scale * 1.5f, scale * .50f))
    drawRoundRect(Color(0xFFB9584C), Offset(p.x - scale * .52f, p.y - scale * .05f), Size(scale * 1.04f, scale * 1.00f), CornerRadius(scale * .25f))
    drawCircle(Color(0xFFFFC891), scale * .45f, Offset(p.x, p.y - scale * .53f))
    drawCircle(Color(0xFF35231F), scale * .06f, Offset(p.x - scale * .16f, p.y - scale * .58f))
    drawCircle(Color(0xFF35231F), scale * .06f, Offset(p.x + scale * .16f, p.y - scale * .58f))
    drawLine(Color.White.copy(alpha = .75f), Offset(p.x - scale * .12f, p.y - scale * .36f), Offset(p.x + scale * .13f, p.y - scale * .36f), 1.4f)
}

private fun DrawScope.drawHeroSprite(p: Offset, scale: Float, facing: Int, moving: Boolean, hasOrder: Boolean, hasBao: Boolean) {
    val bob = if (moving) ((p.x.toInt() / 3) % 2) * scale * .035f else 0f
    val q = p + Offset(0f, bob)
    val outline = Color(0xFF171719)
    val skin = Color(0xFFFFC58F)
    val skinLight = Color(0xFFFFD9AE)
    val skinShade = Color(0xFFD88963)
    val hair = Color(0xFF38252A)
    val hairLight = Color(0xFF5B3840)
    val hairShade = Color(0xFF241A1D)
    val shirt = Color(0xFF4F7898)
    val shirtLight = Color(0xFF7198B2)
    val shirtShade = Color(0xFF304F68)
    val pants = Color(0xFF273D4E)
    val pantsLight = Color(0xFF3F5D70)
    val shoe = Color(0xFF24282B)
    val shoeLight = Color(0xFF555D60)
    val accent = Color(0xFFE8B45E)

    // Ground shadow: keeps the character visually anchored to the isometric floor.
    drawOval(Color.Black.copy(alpha = .42f), Offset(q.x - scale * .92f, q.y + scale * .62f), Size(scale * 1.84f, scale * .48f))

    // Legs + chunky low-poly shoes: small body, deliberately short proportions.
    val legY = q.y + scale * .55f
    drawRoundRect(outline, Offset(q.x - scale * .33f, legY - scale * .02f), Size(scale * .25f, scale * .56f), CornerRadius(scale * .08f))
    drawRoundRect(outline, Offset(q.x + scale * .08f, legY - scale * .02f), Size(scale * .25f, scale * .56f), CornerRadius(scale * .08f))
    drawRoundRect(pantsShade(pants, .12f), Offset(q.x - scale * .29f, legY + scale * .01f), Size(scale * .17f, scale * .48f), CornerRadius(scale * .06f))
    drawRoundRect(pants, Offset(q.x + scale * .12f, legY + scale * .01f), Size(scale * .17f, scale * .48f), CornerRadius(scale * .06f))
    drawRoundRect(outline, Offset(q.x - scale * .40f, legY + scale * .40f), Size(scale * .38f, scale * .22f), CornerRadius(scale * .09f))
    drawRoundRect(outline, Offset(q.x + scale * .02f, legY + scale * .40f), Size(scale * .38f, scale * .22f), CornerRadius(scale * .09f))
    drawRoundRect(shoe, Offset(q.x - scale * .36f, legY + scale * .43f), Size(scale * .31f, scale * .13f), CornerRadius(scale * .06f))
    drawRoundRect(shoeLight, Offset(q.x + scale * .06f, legY + scale * .43f), Size(scale * .31f, scale * .13f), CornerRadius(scale * .06f))
    drawLine(Color.White.copy(alpha = .16f), Offset(q.x - scale * .32f, legY + scale * .45f), Offset(q.x - scale * .12f, legY + scale * .45f), 1.4f)
    drawLine(Color.White.copy(alpha = .16f), Offset(q.x + scale * .10f, legY + scale * .45f), Offset(q.x + scale * .30f, legY + scale * .45f), 1.4f)

    // Torso is wider at the shoulders and narrower at the waist, with visible side facets.
    val bodyTop = q.y + scale * .02f
    val torso = Path().apply {
        moveTo(q.x - scale * .47f, bodyTop + scale * .05f)
        lineTo(q.x - scale * .33f, bodyTop - scale * .08f)
        lineTo(q.x + scale * .33f, bodyTop - scale * .08f)
        lineTo(q.x + scale * .47f, bodyTop + scale * .05f)
        lineTo(q.x + scale * .34f, bodyTop + scale * .62f)
        lineTo(q.x - scale * .34f, bodyTop + scale * .62f)
        close()
    }
    drawPath(torso, outline)
    val shirtFace = Path().apply {
        moveTo(q.x - scale * .39f, bodyTop + scale * .07f)
        lineTo(q.x - scale * .27f, bodyTop)
        lineTo(q.x + scale * .27f, bodyTop)
        lineTo(q.x + scale * .39f, bodyTop + scale * .07f)
        lineTo(q.x + scale * .28f, bodyTop + scale * .55f)
        lineTo(q.x - scale * .28f, bodyTop + scale * .55f)
        close()
    }
    drawPath(shirtFace, shirt)
    drawPath(Path().apply { moveTo(q.x - scale * .39f, bodyTop + scale * .07f); lineTo(q.x - scale * .27f, bodyTop); lineTo(q.x - scale * .27f, bodyTop + scale * .55f); lineTo(q.x - scale * .39f, bodyTop + scale * .07f); close() }, shirtLight.copy(alpha = .82f))
    drawPath(Path().apply { moveTo(q.x + scale * .27f, bodyTop); lineTo(q.x + scale * .39f, bodyTop + scale * .07f); lineTo(q.x + scale * .28f, bodyTop + scale * .55f); lineTo(q.x + scale * .27f, bodyTop); close() }, shirtShade)

    // Clothing details: collar, apron stripe and buttons give the material separate identity.
    drawPath(Path().apply { moveTo(q.x - scale * .18f, bodyTop); lineTo(q.x, bodyTop + scale * .17f); lineTo(q.x + scale * .18f, bodyTop); lineTo(q.x + scale * .11f, bodyTop + scale * .08f); lineTo(q.x, bodyTop + scale * .23f); lineTo(q.x - scale * .11f, bodyTop + scale * .08f); close() }, Cream)
    drawRoundRect(accent, Offset(q.x - scale * .06f, bodyTop + scale * .18f), Size(scale * .12f, scale * .30f), CornerRadius(scale * .025f))
    drawCircle(Cream, scale * .028f, Offset(q.x, bodyTop + scale * .31f))
    drawCircle(Cream, scale * .028f, Offset(q.x, bodyTop + scale * .43f))

    // Arms are separated volumes with a darker underside and visible hands.
    val armY = bodyTop + scale * .12f
    drawRoundRect(outline, Offset(q.x - scale * .63f, armY), Size(scale * .25f, scale * .57f), CornerRadius(scale * .11f))
    drawRoundRect(outline, Offset(q.x + scale * .38f, armY), Size(scale * .25f, scale * .57f), CornerRadius(scale * .11f))
    drawRoundRect(shirtLight, Offset(q.x - scale * .59f, armY + scale * .02f), Size(scale * .16f, scale * .39f), CornerRadius(scale * .08f))
    drawRoundRect(shirtShade, Offset(q.x + scale * .42f, armY + scale * .02f), Size(scale * .16f, scale * .39f), CornerRadius(scale * .08f))
    drawRoundRect(skin, Offset(q.x - scale * .59f, armY + scale * .34f), Size(scale * .16f, scale * .18f), CornerRadius(scale * .07f))
    drawRoundRect(skinShade, Offset(q.x + scale * .42f, armY + scale * .34f), Size(scale * .16f, scale * .18f), CornerRadius(scale * .07f))

    // Neck and oversized head: roughly 45% of total character height.
    drawRoundRect(outline, Offset(q.x - scale * .15f, q.y - scale * .30f), Size(scale * .30f, scale * .25f), CornerRadius(scale * .08f))
    drawRoundRect(skinShade, Offset(q.x - scale * .10f, q.y - scale * .28f), Size(scale * .20f, scale * .20f), CornerRadius(scale * .06f))

    val headCenter = q - Offset(0f, scale * .62f)
    val headR = scale * .62f
    // Faceted head silhouette.
    val headOutline = Path().apply {
        moveTo(headCenter.x, headCenter.y - headR)
        lineTo(headCenter.x + headR * .68f, headCenter.y - headR * .72f)
        lineTo(headCenter.x + headR, headCenter.y - headR * .12f)
        lineTo(headCenter.x + headR * .82f, headCenter.y + headR * .60f)
        lineTo(headCenter.x + headR * .30f, headCenter.y + headR)
        lineTo(headCenter.x - headR * .30f, headCenter.y + headR)
        lineTo(headCenter.x - headR * .82f, headCenter.y + headR * .60f)
        lineTo(headCenter.x - headR, headCenter.y - headR * .12f)
        lineTo(headCenter.x - headR * .68f, headCenter.y - headR * .72f)
        close()
    }
    drawPath(headOutline, outline)
    val face = Path().apply {
        moveTo(headCenter.x, headCenter.y - headR * .88f)
        lineTo(headCenter.x + headR * .58f, headCenter.y - headR * .62f)
        lineTo(headCenter.x + headR * .84f, headCenter.y - headR * .08f)
        lineTo(headCenter.x + headR * .68f, headCenter.y + headR * .55f)
        lineTo(headCenter.x + headR * .24f, headCenter.y + headR * .82f)
        lineTo(headCenter.x - headR * .30f, headCenter.y + headR * .82f)
        lineTo(headCenter.x - headR * .68f, headCenter.y + headR * .52f)
        lineTo(headCenter.x - headR * .84f, headCenter.y - headR * .08f)
        lineTo(headCenter.x - headR * .58f, headCenter.y - headR * .62f)
        close()
    }
    drawPath(face, skin)
    drawPath(Path().apply { moveTo(headCenter.x - headR * .84f, headCenter.y - headR * .08f); lineTo(headCenter.x - headR * .58f, headCenter.y - headR * .62f); lineTo(headCenter.x, headCenter.y - headR * .88f); lineTo(headCenter.x - headR * .30f, headCenter.y + headR * .82f); lineTo(headCenter.x - headR * .68f, headCenter.y + headR * .52f); close() }, skinLight.copy(alpha = .88f))
    drawPath(Path().apply { moveTo(headCenter.x, headCenter.y - headR * .88f); lineTo(headCenter.x + headR * .58f, headCenter.y - headR * .62f); lineTo(headCenter.x + headR * .84f, headCenter.y - headR * .08f); lineTo(headCenter.x + headR * .68f, headCenter.y + headR * .55f); lineTo(headCenter.x + headR * .24f, headCenter.y + headR * .82f); lineTo(headCenter.x, headCenter.y + headR * .20f); close() }, skinShade.copy(alpha = .78f))

    // Hair cap with angular fringe and separate highlight planes.
    val hairCap = Path().apply {
        moveTo(headCenter.x - headR * .88f, headCenter.y - headR * .20f)
        lineTo(headCenter.x - headR * .66f, headCenter.y - headR * .72f)
        lineTo(headCenter.x - headR * .15f, headCenter.y - headR * .94f)
        lineTo(headCenter.x + headR * .52f, headCenter.y - headR * .78f)
        lineTo(headCenter.x + headR * .88f, headCenter.y - headR * .26f)
        lineTo(headCenter.x + headR * .64f, headCenter.y - headR * .05f)
        lineTo(headCenter.x + headR * .34f, headCenter.y - headR * .25f)
        lineTo(headCenter.x + headR * .06f, headCenter.y - headR * .05f)
        lineTo(headCenter.x - headR * .18f, headCenter.y - headR * .28f)
        lineTo(headCenter.x - headR * .42f, headCenter.y - headR * .05f)
        lineTo(headCenter.x - headR * .68f, headCenter.y - headR * .14f)
        close()
    }
    drawPath(hairCap, hair)
    drawPath(Path().apply { moveTo(headCenter.x - headR * .66f, headCenter.y - headR * .72f); lineTo(headCenter.x - headR * .15f, headCenter.y - headR * .94f); lineTo(headCenter.x + headR * .06f, headCenter.y - headR * .05f); lineTo(headCenter.x - headR * .42f, headCenter.y - headR * .05f); close() }, hairLight)
    drawPath(Path().apply { moveTo(headCenter.x + headR * .06f, headCenter.y - headR * .05f); lineTo(headCenter.x + headR * .52f, headCenter.y - headR * .78f); lineTo(headCenter.x + headR * .88f, headCenter.y - headR * .26f); lineTo(headCenter.x + headR * .34f, headCenter.y - headR * .25f); close() }, hairShade)

    // Face: large readable eyes, brows, tiny nose and smiling mouth.
    val eyeY = headCenter.y - headR * .04f
    val eyeOffset = headR * .29f
    drawRoundRect(Color(0xFFF8F4EA), Offset(headCenter.x - eyeOffset - headR * .17f, eyeY - headR * .12f), Size(headR * .30f, headR * .24f), CornerRadius(headR * .08f))
    drawRoundRect(Color(0xFFF8F4EA), Offset(headCenter.x + eyeOffset - headR * .13f, eyeY - headR * .12f), Size(headR * .30f, headR * .24f), CornerRadius(headR * .08f))
    drawCircle(Color(0xFF332629), headR * .075f, Offset(headCenter.x - eyeOffset + facing * headR * .04f, eyeY))
    drawCircle(Color(0xFF332629), headR * .075f, Offset(headCenter.x + eyeOffset + facing * headR * .04f, eyeY))
    drawCircle(Color.White.copy(alpha = .85f), headR * .025f, Offset(headCenter.x - eyeOffset + facing * headR * .06f, eyeY - headR * .025f))
    drawCircle(Color.White.copy(alpha = .85f), headR * .025f, Offset(headCenter.x + eyeOffset + facing * headR * .06f, eyeY - headR * .025f))
    drawLine(hairShade, Offset(headCenter.x - eyeOffset - headR * .12f, eyeY - headR * .17f), Offset(headCenter.x - eyeOffset + headR * .10f, eyeY - headR * .19f), headR * .035f, StrokeCap.Round)
    drawLine(hairShade, Offset(headCenter.x + eyeOffset - headR * .10f, eyeY - headR * .19f), Offset(headCenter.x + eyeOffset + headR * .12f, eyeY - headR * .17f), headR * .035f, StrokeCap.Round)
    drawLine(skinShade, Offset(headCenter.x + facing * headR * .05f, eyeY + headR * .08f), Offset(headCenter.x + facing * headR * .10f, eyeY + headR * .20f), headR * .035f, StrokeCap.Round)
    drawArc(Color(0xFF9E4E50), 12f, 156f, false, Offset(headCenter.x - headR * .18f, eyeY + headR * .18f), Size(headR * .36f, headR * .22f), style = Stroke(width = headR * .045f))

    // Small ears with skin material and shade.
    drawCircle(outline, headR * .17f, Offset(headCenter.x - headR * .88f, headCenter.y + headR * .02f))
    drawCircle(outline, headR * .17f, Offset(headCenter.x + headR * .88f, headCenter.y + headR * .02f))
    drawCircle(skinShade, headR * .105f, Offset(headCenter.x - headR * .88f, headCenter.y + headR * .02f))
    drawCircle(skinShade, headR * .105f, Offset(headCenter.x + headR * .88f, headCenter.y + headR * .02f))

    if (hasOrder) drawSpeechIcon(q + Offset(-scale * .72f, -scale * .40f), scale * .34f)
    if (hasBao) drawBao(q + Offset(scale * .67f, scale * .12f), scale * .23f)
}

private fun pantsShade(base: Color, amount: Float): Color = Color(
    red = max(0f, base.red - amount),
    green = max(0f, base.green - amount),
    blue = max(0f, base.blue - amount),
    alpha = base.alpha
)

private fun DrawScope.drawSpeechIcon(p: Offset, scale: Float) {
    drawRoundRect(Cream, Offset(p.x - scale, p.y - scale * .62f), Size(scale * 2f, scale * 1.25f), CornerRadius(scale * .25f))
    drawCircle(Red, scale * .10f, p - Offset(scale * .35f, 0f))
    drawCircle(Red, scale * .10f, p)
    drawCircle(Red, scale * .10f, p + Offset(scale * .35f, 0f))
}

private fun DrawScope.drawPlate(p: Offset, r: Float) {
    drawCircle(Color(0xFFE9E2D3), r, p)
    drawCircle(Color(0xFFB9B1A0), r * .70f, p, style = Stroke(width = 1.4f))
}

private fun DrawScope.drawCup(p: Offset, r: Float) {
    drawRoundRect(Color(0xFF6C8D83), Offset(p.x - r, p.y - r), Size(r * 2f, r * 1.5f), CornerRadius(r * .25f))
    drawCircle(Color(0xFFCB9A65), r * .42f, p - Offset(0f, r * .25f))
}

private fun DrawScope.drawBao(p: Offset, r: Float) {
    drawCircle(Color(0xFFF0C47A), r, p)
    drawCircle(Color(0xFFFFE5B0), r * .72f, p - Offset(r * .10f, r * .12f))
    drawLine(Color(0xFFB8794C), p - Offset(r * .18f, 0f), p + Offset(r * .18f, 0f), 1.3f)
}

private fun DrawScope.drawForegroundEdge(center: (Float, Float) -> Offset, tileW: Float, tileH: Float, cols: Int, rows: Int) {
    for (c in 0 until cols) if (c % 2 == 0) {
        val p = center(c.toFloat(), (rows - 1).toFloat())
        drawLine(Color(0xFF172326), p + Offset(0f, tileH * .45f), p + Offset(tileW * .50f, tileH * .95f), 3f)
    }
}

private fun isoPath(center: Offset, tileW: Float, tileH: Float): Path = Path().apply {
    moveTo(center.x, center.y - tileH * .5f)
    lineTo(center.x + tileW * .5f, center.y)
    lineTo(center.x, center.y + tileH * .5f)
    lineTo(center.x - tileW * .5f, center.y)
    close()
}

private fun DrawScope.drawIsoBox(center: Offset, width: Float, depth: Float, height: Float, top: Color, side: Color) {
    drawPath(isoPath(center, width, depth), top)
    val a = Offset(center.x - width * .5f, center.y)
    val b = Offset(center.x, center.y + depth * .5f)
    val c = Offset(center.x + width * .5f, center.y)
    val a2 = a + Offset(0f, height)
    val b2 = b + Offset(0f, height)
    val c2 = c + Offset(0f, height)
    val left = Path().apply { moveTo(a.x, a.y); lineTo(b.x, b.y); lineTo(b2.x, b2.y); lineTo(a2.x, a2.y); close() }
    val right = Path().apply { moveTo(b.x, b.y); lineTo(c.x, c.y); lineTo(c2.x, c2.y); lineTo(b2.x, b2.y); close() }
    drawPath(left, side)
    drawPath(right, side.copy(alpha = .78f))
    drawLine(Color.White.copy(alpha = .10f), a, b, 1f)
    drawLine(Color.White.copy(alpha = .10f), b, c, 1f)
}
