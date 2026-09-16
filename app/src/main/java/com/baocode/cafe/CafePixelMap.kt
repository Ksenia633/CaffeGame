package com.baocode.cafe

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import kotlin.math.floor

private fun tilePath(origin: Offset, tile: Float, c: Int, r: Int): Path = Path().apply {
    moveTo(origin.x + c * tile, origin.y + r * tile)
    lineTo(origin.x + (c + 1) * tile, origin.y + r * tile)
    lineTo(origin.x + (c + 1) * tile, origin.y + (r + 1) * tile)
    lineTo(origin.x + c * tile, origin.y + (r + 1) * tile)
    close()
}

internal fun DrawScope.drawCafePixelScene(origin: Offset, tile: Float, grid: CafeGrid5, timeMs: Long) {
    drawRect(Color(0xFF080F12))

    for (r in 0 until grid.height) for (c in 0 until grid.width) {
        val cell = grid.cell(c, r) ?: continue
        val rect = androidx.compose.ui.geometry.Rect(origin.x + c * tile, origin.y + r * tile, origin.x + (c + 1) * tile, origin.y + (r + 1) * tile)
        val floor = if ((c + r) % 2 == 0) Color(0xFF9B6240) else Color(0xFF895237)
        drawRect(if (cell.type == CellType5.WALL) Color(0xFF553729) else floor, rect.topLeft, rect.size)
        if (cell.type != CellType5.WALL) drawRect(Color(0xFFB8794E).copy(alpha=.22f), rect.topLeft, rect.size, style = Stroke(tile * .035f))

        when (cell.type) {
            CellType5.WALL -> drawRect(Color(0xFF2C2020), rect.topLeft, rect.size, style = Stroke(tile * .16f))
            CellType5.KITCHEN -> {
                drawRect(Color(0xFF6F4130), rect.topLeft + Offset(tile*.08f,tile*.10f), Size(tile*.84f,tile*.76f))
                drawRect(Color(0xFFB98A63), rect.topLeft + Offset(tile*.18f,tile*.18f), Size(tile*.64f,tile*.18f))
                drawRect(Color(0xFF3F4440), rect.topLeft + Offset(tile*.16f,tile*.46f), Size(tile*.22f,tile*.23f))
                drawRect(Color(0xFF3F4440), rect.topLeft + Offset(tile*.53f,tile*.46f), Size(tile*.22f,tile*.23f))
                drawCircle(Color(0xFFFFB75B), tile*.045f, rect.topLeft + Offset(tile*.27f,tile*.30f))
                drawCircle(Color(0xFFFFD28B), tile*.038f, rect.topLeft + Offset(tile*.70f,tile*.30f))
            }
            CellType5.COUNTER -> {
                drawRect(Color(0xFF7A4B36), rect.topLeft + Offset(tile*.08f,tile*.18f), Size(tile*.84f,tile*.60f))
                drawRect(Color(0xFFD39A5C), rect.topLeft + Offset(tile*.05f,tile*.12f), Size(tile*.90f,tile*.18f))
                drawRect(Color(0xFF4B3027), rect.topLeft + Offset(tile*.28f,tile*.35f), Size(tile*.44f,tile*.30f))
            }
            CellType5.TABLE -> {
                drawRoundRect(Color(0xFF4D2F26), rect.topLeft + Offset(tile*.14f,tile*.24f), Size(tile*.72f,tile*.56f), androidx.compose.ui.geometry.CornerRadius(tile*.08f))
                drawRoundRect(Color(0xFF9A6241), rect.topLeft + Offset(tile*.20f,tile*.20f), Size(tile*.60f,tile*.16f), androidx.compose.ui.geometry.CornerRadius(tile*.04f))
                drawCircle(Color(0xFFE6CF9F), tile*.05f, rect.topLeft + Offset(tile*.50f,tile*.52f))
            }
            CellType5.CHAIR -> {
                drawRect(Color(0xFF56382D), rect.topLeft + Offset(tile*.28f,tile*.22f), Size(tile*.44f,tile*.56f))
                drawRect(Color(0xFF85553A), rect.topLeft + Offset(tile*.21f,tile*.18f), Size(tile*.58f,tile*.18f))
            }
            CellType5.PLANT -> {
                drawRect(Color(0xFF7A4B35), rect.topLeft + Offset(tile*.37f,tile*.62f), Size(tile*.26f,tile*.18f))
                drawCircle(Color(0xFF4D7F54), tile*.20f, rect.topLeft + Offset(tile*.42f,tile*.38f))
                drawCircle(Color(0xFF6CA062), tile*.15f, rect.topLeft + Offset(tile*.66f,tile*.48f))
            }
            CellType5.DOOR -> {
                drawRect(Color(0xFF3B2B27), rect.topLeft + Offset(tile*.18f,tile*.08f), Size(tile*.64f,tile*.84f))
                drawRect(Color(0xFF7EB0B5).copy(alpha=.55f), rect.topLeft + Offset(tile*.28f,tile*.18f), Size(tile*.44f,tile*.64f))
            }
            CellType5.FLOOR -> Unit
        }
    }

    // Warm interior lighting, but kept restrained so the map remains visible.
    val pulse = (kotlin.math.sin(timeMs / 700f) * .5f + .5f)
    listOf(Offset(tile*4.5f, tile*1.8f), Offset(tile*9.5f, tile*1.8f), Offset(tile*6.5f, tile*5.3f)).forEach {
        val p = Offset(origin.x + it.x, origin.y + it.y)
        drawCircle(Color(0xFFFFB45B).copy(alpha=.045f + pulse*.02f), tile*1.4f, p)
        drawCircle(Color(0xFFFFD38B).copy(alpha=.12f), tile*.08f, p)
    }
}

internal fun DrawScope.drawPixelNpc(center: Offset, tile: Float, variant: Int, timeMs: Long) {
    val bob = kotlin.math.sin(timeMs / 280f + variant) * tile * .025f
    val s = tile
    drawOval(Color.Black.copy(alpha=.28f), Offset(center.x-s*.23f, center.y+s*.29f), Size(s*.46f,s*.12f))
    drawRect(Color(0xFF1F252A), Offset(center.x-s*.16f, center.y+s*.02f+bob), Size(s*.13f,s*.32f))
    drawRect(Color(0xFF29363F), Offset(center.x+s*.03f, center.y+s*.02f-bob), Size(s*.13f,s*.32f))
    val skin = when (variant) { 0 -> Color(0xFFFFC58F); 1 -> Color(0xFFE2A378); else -> Color(0xFF9D6748) }
    drawCircle(skin, s*.22f, Offset(center.x, center.y-s*.22f+bob))
    val hair = when (variant) { 0 -> Color(0xFF3A2727); 1 -> Color(0xFF202024); else -> Color(0xFF6C3F2E) }
    drawCircle(hair, s*.22f, Offset(center.x, center.y-s*.33f+bob))
    drawRect(Color(0xFFB56E45), Offset(center.x-s*.17f, center.y-bob), Size(s*.34f,s*.07f))
    drawCircle(Color(0xFFF7E4C4), s*.035f, Offset(center.x-s*.08f, center.y-s*.22f+bob))
    drawCircle(Color(0xFFF7E4C4), s*.035f, Offset(center.x+s*.08f, center.y-s*.22f+bob))
}

internal fun DrawScope.drawPixelHero(center: Offset, tile: Float, moving: Boolean, hasOrder: Boolean, hasBao: Boolean) {
    val leg = if (moving) tile*.035f else 0f
    drawOval(Color.Black.copy(alpha=.35f), Offset(center.x-tile*.28f,center.y+tile*.30f), Size(tile*.56f,tile*.14f))
    drawRect(Color(0xFF2A4156), Offset(center.x-tile*.13f,center.y+tile*.02f+leg), Size(tile*.11f,tile*.30f))
    drawRect(Color(0xFF355A74), Offset(center.x+tile*.02f,center.y+tile*.02f-leg), Size(tile*.11f,tile*.30f))
    drawRect(Color(0xFF5B7FA0), Offset(center.x-tile*.18f,center.y-tile*.13f), Size(tile*.36f,tile*.28f))
    drawCircle(Color(0xFFFFC995), tile*.18f, Offset(center.x,center.y-tile*.30f))
    drawRect(Color(0xFF3A2827), Offset(center.x-tile*.17f,center.y-tile*.45f), Size(tile*.34f,tile*.10f))
    drawCircle(Color.White, tile*.035f, Offset(center.x-tile*.07f,center.y-tile*.29f))
    drawCircle(Color.White, tile*.035f, Offset(center.x+tile*.07f,center.y-tile*.29f))
    if (hasOrder) drawRoundRect(Cream, Offset(center.x-tile*.48f,center.y-tile*.52f), Size(tile*.30f,tile*.17f), androidx.compose.ui.geometry.CornerRadius(tile*.04f))
    if (hasBao) drawCircle(Color(0xFFF0C47A), tile*.10f, Offset(center.x+tile*.42f,center.y+tile*.02f))
}
