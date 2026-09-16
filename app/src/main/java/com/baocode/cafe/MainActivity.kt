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
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
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

private data class Hero3(val c: Int = 7, val r: Int = 7, val x: Float = 7f, val y: Float = 7f, val moving: Boolean = false, val order: Boolean = false, val bao: Boolean = false)
private enum class Target3 { GUEST, CASHIER, KITCHEN }
private data class Pos3(val c: Int, val r: Int)
private val guest3 = Pos3(9, 2)
private val cashier3 = Pos3(4, 2)
private val kitchen3 = Pos3(2, 2)

@Composable
private fun BaoCodeGame() {
    val grid = remember { defaultCafeGrid5() }
    var program by remember { mutableStateOf(emptyList<Command>()) }
    var running by remember { mutableStateOf(false) }
    var action by remember { mutableStateOf("Собери программу для первого гостя") }
    var hero by remember { mutableStateOf(Hero3()) }
    var target by remember { mutableStateOf<Target3?>(null) }

    suspend fun moveTo(dest: Pos3, kind: Target3) {
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
            for (step in 1..6) {
                val t = step / 6f
                hero = hero.copy(x = sx + dx * t, y = sy + dy * t, moving = true)
                delay(28)
            }
            hero = hero.copy(c = next.c, r = next.r, x = next.c.toFloat(), y = next.r.toFloat())
            delay(35)
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
                "to_guest" -> moveTo(guest3, Target3.GUEST)
                "take" -> { hero = hero.copy(order = true); delay(650) }
                "to_counter" -> moveTo(cashier3, Target3.CASHIER)
                "take_bao" -> { moveTo(kitchen3, Target3.KITCHEN); hero = hero.copy(bao = true); delay(650) }
                "return" -> moveTo(guest3, Target3.GUEST)
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
            World3(hero, target, grid)
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
private fun World3(hero: Hero3, target: Target3?, grid: CafeGrid5) {
    Canvas(Modifier.fillMaxSize()) {
        val w = min(size.width / 17f, size.height / 10f)
        val h = w * .50f
        val origin = Offset(size.width * .49f, size.height * .17f)
        fun center(c: Float, r: Float) = Offset(origin.x + (c - r) * w * .5f, origin.y + (c + r) * h * .5f)
        drawRect(Color(0xFF020708))
        for (r in 0 until grid.height) for (c in 0 until grid.width) {
            val cell = grid.cell(c, r) ?: continue
            drawGridCell5(center(c.toFloat(), r.toFloat()), w, h, cell)
        }
        drawGridObjects5(::center, w, h, grid)
        drawGridTarget5(::center, w, h, target)
        drawStage6Lighting5(::center, w, h, hero.x, hero.y)
        drawCafeNpcCast(::center, w)
        drawHero3(center(hero.x, hero.y), w * .22f, hero)
    }
}

private fun DrawScope.drawGridCell5(p: Offset, w: Float, h: Float, cell: CafeCell5) {
    val path = Path().apply {
        moveTo(p.x, p.y - h * .5f); lineTo(p.x + w * .5f, p.y)
        lineTo(p.x, p.y + h * .5f); lineTo(p.x - w * .5f, p.y); close()
    }
    val floor = if ((cell.col + cell.row) % 2 == 0) Color(0xFF8A5539) else Color(0xFF73432F)
    val color = when (cell.type) {
        CellType5.WALL -> Color(0xFF2D4144)
        CellType5.DOOR -> Color(0xFF4E7067)
        else -> floor
    }
    drawPath(path, color)
    drawPath(path, Color(0xFF3A2521).copy(alpha = .72f), style = Stroke(width = 1.1f))
}

private fun DrawScope.drawGridObjects5(center: (Float, Float) -> Offset, w: Float, h: Float, grid: CafeGrid5) {
    grid.cells.sortedBy { it.row + it.col }.forEach { cell ->
        val p = center(cell.col.toFloat(), cell.row.toFloat())
        val lift = cell.height * h
        when (cell.type) {
            CellType5.WALL -> drawRect(Color(0xFF304447), Offset(p.x - w * .48f, p.y - lift), Size(w * .96f, lift + h * .25f))
            CellType5.KITCHEN -> {
                drawRoundRect(Color(0xFF6D3B2D), Offset(p.x - w * .48f, p.y - lift), Size(w * .96f, lift + h * .30f), CornerRadius(5.dp.toPx()))
                drawRoundRect(Color(0xFF242A29), Offset(p.x - w * .28f, p.y - lift - h * .10f), Size(w * .56f, h * .30f), CornerRadius(4.dp.toPx()))
            }
            CellType5.COUNTER -> {
                drawRoundRect(Color(0xFFD85B45), Offset(p.x - w * .50f, p.y - lift), Size(w, lift + h * .30f), CornerRadius(5.dp.toPx()))
                drawRoundRect(Color(0xFFF0A46B), Offset(p.x - w * .42f, p.y - lift - h * .05f), Size(w * .84f, h * .10f), CornerRadius(3.dp.toPx()))
            }
            CellType5.TABLE -> {
                drawOval(Color.Black.copy(alpha = .28f), Offset(p.x - w * .44f, p.y + h * .25f), Size(w * .88f, h * .34f))
                drawOval(Color(0xFFC7834A), Offset(p.x - w * .42f, p.y - lift), Size(w * .84f, h * .52f))
                drawOval(Color(0xFFE9E2D3), Offset(p.x - w * .15f, p.y - lift - h * .02f), Size(w * .30f, h * .18f))
            }
            CellType5.CHAIR -> drawRoundRect(Color(0xFF9A5A3A), Offset(p.x - w * .22f, p.y - lift), Size(w * .44f, lift + h * .16f), CornerRadius(3.dp.toPx()))
            CellType5.PLANT -> {
                drawRoundRect(Color(0xFF8B4D32), Offset(p.x - w * .18f, p.y - lift * .30f), Size(w * .36f, h * .24f), CornerRadius(3.dp.toPx()))
                drawCircle(Color(0xFF4E8B67), w * .22f, p - Offset(w * .08f, lift * .65f))
                drawCircle(Color(0xFF66A978), w * .16f, p - Offset(w * .20f, lift * .55f))
            }
            CellType5.DOOR, CellType5.FLOOR -> Unit
        }
    }
}

private fun DrawScope.drawGridTarget5(center: (Float, Float) -> Offset, w: Float, h: Float, target: Target3?) {
    if (target == null) return
    val p = when (target) {
        Target3.GUEST -> center(guest3.c.toFloat(), guest3.r.toFloat())
        Target3.CASHIER -> center(cashier3.c.toFloat(), cashier3.r.toFloat())
        Target3.KITCHEN -> center(kitchen3.c.toFloat(), kitchen3.r.toFloat())
    }
    drawLine(Neon, p - Offset(w * .25f, 0f), p + Offset(w * .25f, 0f), 3f)
    drawLine(Neon, p - Offset(0f, h * .15f), p + Offset(0f, h * .15f), 3f)
}

private fun DrawScope.drawHero3(p: Offset, s: Float, hero: Hero3) {
    val outline = Color(0xFF171719)
    val skin = Color(0xFFFFC58F)
    val hair = Color(0xFF38252A)
    val shirt = Color(0xFF4F7898)
    val pants = Color(0xFF273D4E)
    val bob = if (hero.moving) -s * .035f else 0f
    val q = p + Offset(0f, bob)
    val legSwing = if (hero.moving) s * .10f else 0f
    drawOval(Color.Black.copy(alpha = .40f), Offset(q.x - s * .9f, q.y + s * .62f), Size(s * 1.8f, s * .45f))
    drawRoundRect(outline, Offset(q.x - s * .30f, q.y + s * .48f + legSwing), Size(s * .22f, s * .52f), CornerRadius(s * .07f))
    drawRoundRect(outline, Offset(q.x + s * .08f, q.y + s * .48f - legSwing), Size(s * .22f, s * .52f), CornerRadius(s * .07f))
    drawRoundRect(pants, Offset(q.x - s * .26f, q.y + s * .50f + legSwing), Size(s * .14f, s * .43f), CornerRadius(s * .05f))
    drawRoundRect(pants, Offset(q.x + s * .12f, q.y + s * .50f - legSwing), Size(s * .14f, s * .43f), CornerRadius(s * .05f))
    drawRoundRect(Color(0xFF24282B), Offset(q.x - s * .34f, q.y + s * .91f + legSwing), Size(s * .31f, s * .12f), CornerRadius(s * .05f))
    drawRoundRect(Color(0xFF555D60), Offset(q.x + s * .04f, q.y + s * .91f - legSwing), Size(s * .31f, s * .12f), CornerRadius(s * .05f))
    val body = Path().apply { moveTo(q.x - s * .43f, q.y + s * .06f); lineTo(q.x - s * .28f, q.y - s * .04f); lineTo(q.x + s * .28f, q.y - s * .04f); lineTo(q.x + s * .43f, q.y + s * .06f); lineTo(q.x + s * .31f, q.y + s * .58f); lineTo(q.x - s * .31f, q.y + s * .58f); close() }
    drawPath(body, outline)
    drawRoundRect(shirt, Offset(q.x - s * .36f, q.y + s * .08f), Size(s * .72f, s * .45f), CornerRadius(s * .07f))
    drawRoundRect(Color(0xFFE8B45E), Offset(q.x - s * .06f, q.y + s * .10f), Size(s * .12f, s * .40f), CornerRadius(s * .02f))
    drawRoundRect(outline, Offset(q.x - s * .59f, q.y + s * .07f), Size(s * .22f, s * .52f), CornerRadius(s * .09f))
    drawRoundRect(outline, Offset(q.x + s * .37f, q.y + s * .07f), Size(s * .22f, s * .52f), CornerRadius(s * .09f))
    drawRoundRect(skin, Offset(q.x - s * .55f, q.y + s * .37f), Size(s * .14f, s * .17f), CornerRadius(s * .06f))
    drawRoundRect(skin, Offset(q.x + s * .41f, q.y + s * .37f), Size(s * .14f, s * .17f), CornerRadius(s * .06f))
    val hc = q - Offset(0f, s * .62f)
    val r = s * .62f
    val head = Path().apply { moveTo(hc.x, hc.y-r); lineTo(hc.x+r*.68f,hc.y-r*.72f); lineTo(hc.x+r,hc.y-r*.10f); lineTo(hc.x+r*.80f,hc.y+r*.60f); lineTo(hc.x+r*.28f,hc.y+r); lineTo(hc.x-r*.30f,hc.y+r); lineTo(hc.x-r*.82f,hc.y+r*.58f); lineTo(hc.x-r,hc.y-r*.10f); lineTo(hc.x-r*.68f,hc.y-r*.72f); close() }
    drawPath(head, outline)
    drawPath(head, skin)
    drawPath(Path().apply { moveTo(hc.x-r*.84f,hc.y-r*.08f); lineTo(hc.x-r*.58f,hc.y-r*.62f); lineTo(hc.x,hc.y-r*.88f); lineTo(hc.x-r*.30f,hc.y+r*.82f); lineTo(hc.x-r*.68f,hc.y+r*.52f); close() }, Color(0xFFFFD9AE))
    drawRoundRect(hair, Offset(hc.x-r*.78f,hc.y-r*.76f), Size(r*1.56f,r*.38f), CornerRadius(r*.10f))
    val eyeY = hc.y-r*.03f
    drawCircle(Color.White, r*.14f, Offset(hc.x-r*.29f,eyeY)); drawCircle(Color.White,r*.14f,Offset(hc.x+r*.29f,eyeY))
    drawCircle(Color(0xFF332629), r*.065f, Offset(hc.x-r*.27f,eyeY)); drawCircle(Color(0xFF332629),r*.065f,Offset(hc.x+r*.31f,eyeY))
    drawLine(hair, Offset(hc.x-r*.40f,eyeY-r*.20f), Offset(hc.x-r*.18f,eyeY-r*.22f), r*.035f)
    drawLine(hair, Offset(hc.x+r*.18f,eyeY-r*.22f), Offset(hc.x+r*.40f,eyeY-r*.20f), r*.035f)
    if (hero.order) drawRoundRect(Cream, Offset(q.x-s*.95f,q.y-s*.62f), Size(s*.62f,s*.38f), CornerRadius(s*.10f))
    if (hero.bao) drawCircle(Color(0xFFF0C47A), s*.22f, Offset(q.x+s*.78f,q.y+s*.12f))
}
