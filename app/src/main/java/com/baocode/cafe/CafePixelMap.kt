package com.baocode.cafe

import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import kotlin.math.sin

private fun rect(o: Offset, tile: Float, c: Float, r: Float, wc: Float = 1f, hr: Float = 1f) =
    androidx.compose.ui.geometry.Rect(o.x + c * tile, o.y + r * tile, o.x + (c + wc) * tile, o.y + (r + hr) * tile)

private fun pixelBox(scope: DrawScope, color: Color, o: Offset, tile: Float, c: Float, r: Float, wc: Float, hr: Float) {
    scope.drawRect(color, rect(o, tile, c, r, wc, hr).topLeft, rect(o, tile, c, r, wc, hr).size)
}

internal fun DrawScope.drawCafePixelScene(origin: Offset, tile: Float, grid: CafeGrid5, timeMs: Long) {
    // Night outside the room.
    drawRect(Color(0xFF101820))
    for (i in 0..18) {
        val x = origin.x + (i * tile * .73f) % (size.width + tile)
        val y = origin.y * .55f + ((i * 37) % 120)
        drawCircle(Color(0xFF7E9DB0).copy(alpha = .10f), tile * .025f, Offset(x, y))
    }

    // Tile floor: warm planks with small, deliberate pixel variation.
    for (r in 0 until grid.height) for (c in 0 until grid.width) {
        val cell = grid.cell(c, r) ?: continue
        val p = rect(origin, tile, c.toFloat(), r.toFloat())
        if (cell.type == CellType5.WALL) continue
        val base = if ((r % 2) == 0) Color(0xFF9A6040) else Color(0xFF8B5137)
        drawRect(base, p.topLeft, p.size)
        drawRect(Color(0xFFB9794E).copy(alpha = .13f), p.topLeft, Size(p.width, tile * .055f))
        drawLine(Color(0xFF643B30).copy(alpha = .35f), Offset(p.left, p.bottom - tile * .025f), Offset(p.right, p.bottom - tile * .025f), tile * .025f)
        if ((c + r) % 3 == 0) drawRect(Color(0xFF5E392D).copy(alpha = .18f), Offset(p.left + tile*.18f, p.top + tile*.53f), Size(tile*.34f, tile*.025f))
    }

    // Deep wooden perimeter and ceiling beam.
    for (c in 0 until grid.width) {
        val p = rect(origin, tile, c.toFloat(), 0f)
        drawRect(Color(0xFF382321), p.topLeft, p.size)
        drawRect(Color(0xFF684031), Offset(p.left + tile*.04f, p.top + tile*.06f), Size(tile*.92f, tile*.72f))
        drawRect(Color(0xFF8B563B), Offset(p.left + tile*.10f, p.top + tile*.10f), Size(tile*.80f, tile*.10f))
        drawRect(Color(0xFF2B1A1C), Offset(p.left + tile*.08f, p.bottom - tile*.13f), Size(tile*.84f, tile*.10f))
    }
    for (r in 1 until grid.height - 1) {
        val left = rect(origin, tile, 0f, r.toFloat())
        val right = rect(origin, tile, 13f, r.toFloat())
        drawRect(Color(0xFF342124), left.topLeft, left.size)
        drawRect(Color(0xFF684031), Offset(left.left + tile*.08f, left.top + tile*.04f), Size(tile*.18f, tile*.92f))
        if (r != 5) {
            drawRect(Color(0xFF342124), right.topLeft, right.size)
            drawRect(Color(0xFF684031), Offset(right.left + tile*.74f, right.top + tile*.04f), Size(tile*.18f, tile*.92f))
        }
    }
    val bottom = rect(origin, tile, 0f, 9f, 14f)
    drawRect(Color(0xFF2C1B1D), bottom.topLeft, bottom.size)
    drawRect(Color(0xFF69412F), Offset(bottom.left, bottom.top + tile*.08f), Size(bottom.width, tile*.58f))

    // Wall shelves and framed pictures.
    drawShelf(origin, tile, 6f, 1.0f, 3.0f)
    drawFrame(origin, tile, 10.9f, 1.15f, Color(0xFF6E9B86))
    drawFrame(origin, tile, 11.9f, 1.15f, Color(0xFFC88A55))
    drawSign(origin, tile, 6.9f, .32f, "BAO")

    // Kitchen block with ovens, counters and pots.
    drawKitchen(origin, tile, timeMs)
    drawRegister(origin, tile)
    drawPastryCase(origin, tile)

    // Rugs and furniture silhouettes.
    drawRug(origin, tile, 1.3f, 5.7f, 3.3f, 2.35f)
    drawRug(origin, tile, 5.1f, 6.15f, 3.6f, 2.35f)
    drawRug(origin, tile, 8.8f, 3.9f, 3.0f, 2.45f)
    drawTable(origin, tile, 2.7f, 6.8f, 1.65f, 1.10f)
    drawTable(origin, tile, 6.0f, 7.05f, 1.70f, 1.10f)
    drawTable(origin, tile, 9.6f, 4.65f, 1.65f, 1.05f)
    drawChair(origin, tile, 2.95f, 6.05f)
    drawChair(origin, tile, 2.95f, 7.95f)
    drawChair(origin, tile, 4.10f, 6.95f)
    drawChair(origin, tile, 6.25f, 6.35f)
    drawChair(origin, tile, 6.25f, 8.15f)
    drawChair(origin, tile, 7.35f, 7.05f)
    drawChair(origin, tile, 9.85f, 4.0f)
    drawChair(origin, tile, 11.05f, 4.85f)

    // Plants, lamp fixtures and the rainy entrance.
    drawPlant(origin, tile, 1.1f, 7.35f)
    drawPlant(origin, tile, 12.0f, 7.2f)
    drawPlant(origin, tile, 11.35f, 2.2f)
    drawLamp(origin, tile, 6.3f, 1.05f, timeMs)
    drawLamp(origin, tile, 10.0f, 1.05f, timeMs + 400)
    drawDoor(origin, tile, timeMs)

    // Small ambient light, intentionally subtle: it tints the scene without hiding tiles.
    val pulse = sin(timeMs / 650f) * .5f + .5f
    val glow = Color(0xFFFFB45F).copy(alpha = .025f + pulse * .015f)
    drawRect(glow, origin, Size(grid.width * tile, grid.height * tile))
}

private fun DrawScope.drawKitchen(o: Offset, t: Float, time: Long) {
    pixelBox(this, Color(0xFF65402F), o, t, 1.0f, 1.15f, 4.2f, 1.0f)
    pixelBox(this, Color(0xFFB8794C), o, t, 1.1f, 1.22f, 3.95f, .12f)
    pixelBox(this, Color(0xFF3A3633), o, t, 1.25f, 1.48f, 1.0f, .55f)
    pixelBox(this, Color(0xFF3A3633), o, t, 2.48f, 1.48f, 1.0f, .55f)
    pixelBox(this, Color(0xFF272B2B), o, t, 3.52f, 1.42f, .82f, .64f)
    drawCircle(Color(0xFFE1A54F), t*.065f, Offset(o.x + t*1.48f, o.y+t*1.78f))
    drawCircle(Color(0xFFFFC76B), t*.045f, Offset(o.x + t*2.78f, o.y+t*1.76f))
    drawRoundRect(Color(0xFF9A6040), Offset(o.x+t*.92f,o.y+t*2.24f), Size(t*4.2f,t*.62f), CornerRadius(t*.04f))
    for (i in 0..3) {
        drawRect(Color(0xFF493029), Offset(o.x+t*(1.0f+i),o.y+t*2.42f), Size(t*.78f,t*.30f))
    }
    // Steam above the cooking pot.
    val s = sin(time / 330f)
    drawCircle(Color.White.copy(alpha=.13f), t*.045f, Offset(o.x+t*2.65f,o.y+t*(.88f+s*.06f)))
    drawCircle(Color.White.copy(alpha=.09f), t*.065f, Offset(o.x+t*2.78f,o.y+t*(.64f+s*.04f)))
}

private fun DrawScope.drawRegister(o: Offset, t: Float) {
    drawRoundRect(Color(0xFF4B2C28), Offset(o.x+t*4.15f,o.y+t*1.65f), Size(t*.9f,t*.55f), CornerRadius(t*.04f))
    drawRect(Color(0xFFDE9A52), Offset(o.x+t*4.23f,o.y+t*1.72f), Size(t*.74f,t*.10f))
    drawRect(Color(0xFF283437), Offset(o.x+t*4.32f,o.y+t*1.93f), Size(t*.24f,t*.16f))
    drawCircle(Color(0xFFE7B45D), t*.045f, Offset(o.x+t*4.78f,o.y+t*2.02f))
}

private fun DrawScope.drawPastryCase(o: Offset, t: Float) {
    drawRect(Color(0xFF5A3930), Offset(o.x+t*5.15f,o.y+t*1.45f), Size(t*1.55f,t*1.45f))
    drawRect(Color(0xFF92B0A7).copy(alpha=.75f), Offset(o.x+t*5.27f,o.y+t*1.60f), Size(t*1.30f,t*1.02f))
    drawRect(Color(0xFFDF9B58), Offset(o.x+t*5.20f,o.y+t*2.56f), Size(t*1.45f,t*.16f))
    for (i in 0..2) {
        drawOval(Color(0xFFE4A96D), Offset(o.x+t*(5.37f+i*.36f),o.y+t*(1.86f+(i%2)*.36f)), Size(t*.28f,t*.17f))
        drawCircle(Color(0xFFF3D29C), t*.035f, Offset(o.x+t*(5.50f+i*.36f),o.y+t*(1.95f+(i%2)*.36f)))
    }
}

private fun DrawScope.drawShelf(o: Offset, t: Float, x: Float, y: Float, w: Float) {
    drawRect(Color(0xFF3D2523), Offset(o.x+t*x,o.y+t*y), Size(t*w,t*.92f))
    drawRect(Color(0xFF7A4B35), Offset(o.x+t*(x-.04f),o.y+t*(y+.78f)), Size(t*(w+.08f),t*.10f))
    for (i in 0..4) {
        val colors = listOf(Color(0xFFD8A05E),Color(0xFF78906B),Color(0xFF9D6241),Color(0xFFB8754D),Color(0xFF6F8C87))
        drawRect(colors[i], Offset(o.x+t*(x+.18f+i*.53f),o.y+t*(y+.18f+(i%2)*.12f)), Size(t*.30f,t*.42f))
    }
}

private fun DrawScope.drawFrame(o: Offset, t: Float, x: Float, y: Float, accent: Color) {
    drawRect(Color(0xFF3C2522), Offset(o.x+t*x,o.y+t*y), Size(t*.72f,t*.86f))
    drawRect(accent, Offset(o.x+t*(x+.08f),o.y+t*(y+.08f)), Size(t*.56f,t*.68f))
    drawRect(Color(0xFF233334), Offset(o.x+t*(x+.18f),o.y+t*(y+.20f)), Size(t*.36f,t*.32f))
}

private fun DrawScope.drawSign(o: Offset, t: Float, x: Float, y: Float, text: String) {
    drawRoundRect(Color(0xFF372123), Offset(o.x+t*x,o.y+t*y), Size(t*1.15f,t*.43f), CornerRadius(t*.04f))
    drawRoundRect(Color(0xFFD49A55), Offset(o.x+t*(x+.06f),o.y+t*(y+.05f)), Size(t*1.03f,t*.32f), CornerRadius(t*.03f))
    for (i in text.indices) drawRect(Color(0xFF6A382B), Offset(o.x+t*(x+.24f+i*.21f),o.y+t*(y+.14f)), Size(t*.11f,t*.14f))
}

private fun DrawScope.drawRug(o: Offset, t: Float, x: Float, y: Float, w: Float, h: Float) {
    drawRoundRect(Color(0xFF5B302D), Offset(o.x+t*x,o.y+t*y), Size(t*w,t*h), CornerRadius(t*.10f))
    drawRoundRect(Color(0xFF9B5A43), Offset(o.x+t*(x+.10f),o.y+t*(y+.10f)), Size(t*(w-.20f),t*(h-.20f)), CornerRadius(t*.06f), style=Stroke(t*.045f))
    for (i in 0..2) drawLine(Color(0xFFD28A5A).copy(alpha=.42f), Offset(o.x+t*(x+.35f+i*.72f),o.y+t*(y+.28f)), Offset(o.x+t*(x+.35f+i*.72f),o.y+t*(y+h-.28f)), t*.018f)
}

private fun DrawScope.drawTable(o: Offset, t: Float, x: Float, y: Float, w: Float, h: Float) {
    drawOval(Color.Black.copy(alpha=.32f), Offset(o.x+t*(x-.08f),o.y+t*(y+h*.70f)), Size(t*(w+.16f),t*.34f))
    drawRoundRect(Color(0xFF4A2A25), Offset(o.x+t*x,o.y+t*y), Size(t*w,t*h), CornerRadius(t*.07f))
    drawRoundRect(Color(0xFFA96743), Offset(o.x+t*(x+.08f),o.y+t*(y+.08f)), Size(t*(w-.16f),t*(h*.40f)), CornerRadius(t*.04f))
    drawCircle(Color(0xFFE5C78F), t*.055f, Offset(o.x+t*(x+w*.50f),o.y+t*(y+h*.48f)))
    drawCircle(Color(0xFF7A4B35), t*.04f, Offset(o.x+t*(x+w*.30f),o.y+t*(y+h*.32f)))
}

private fun DrawScope.drawChair(o: Offset, t: Float, x: Float, y: Float) {
    drawRect(Color(0xFF4C2C28), Offset(o.x+t*x,o.y+t*y), Size(t*.42f,t*.48f))
    drawRect(Color(0xFF8C573D), Offset(o.x+t*(x-.06f),o.y+t*(y-.04f)), Size(t*.54f,t*.12f))
    drawRect(Color(0xFF633B30), Offset(o.x+t*(x+.06f),o.y+t*(y+.38f)), Size(t*.10f,t*.18f))
}

private fun DrawScope.drawPlant(o: Offset, t: Float, x: Float, y: Float) {
    drawOval(Color.Black.copy(alpha=.25f), Offset(o.x+t*(x-.18f),o.y+t*(y+.55f)), Size(t*.60f,t*.18f))
    drawRect(Color(0xFF874C34), Offset(o.x+t*(x-.12f),o.y+t*(y+.32f)), Size(t*.40f,t*.30f))
    drawCircle(Color(0xFF3E684A), t*.28f, Offset(o.x+t*x,o.y+t*y))
    drawCircle(Color(0xFF5C925A), t*.20f, Offset(o.x+t*(x-.24f),o.y+t*(y+.08f)))
    drawCircle(Color(0xFF6DA968), t*.18f, Offset(o.x+t*(x+.22f),o.y+t*(y+.12f)))
    drawRect(Color(0xFF31583F), Offset(o.x+t*(x-.02f),o.y+t*(y-.30f)), Size(t*.06f,t*.55f))
}

private fun DrawScope.drawLamp(o: Offset, t: Float, x: Float, y: Float, time: Long) {
    drawLine(Color(0xFF24181A), Offset(o.x+t*x,o.y+t*y), Offset(o.x+t*x,o.y+t*(y+.28f)), t*.035f)
    val flicker = 1f + sin(time/210f)*.025f
    drawCircle(Color(0xFFFFC46B).copy(alpha=.14f), t*.34f*flicker, Offset(o.x+t*x,o.y+t*(y+.42f)))
    drawRect(Color(0xFF4B3028), Offset(o.x+t*(x-.17f),o.y+t*(y+.27f)), Size(t*.34f,t*.16f))
    drawRect(Color(0xFFF2A957), Offset(o.x+t*(x-.12f),o.y+t*(y+.32f)), Size(t*.24f,t*.15f))
}

private fun DrawScope.drawDoor(o: Offset, t: Float, time: Long) {
    val x = 13f
    drawRect(Color(0xFF14242C), Offset(o.x+t*13.02f,o.y+t*4.02f), Size(t*.98f,t*1.96f))
    drawRect(Color(0xFF5A382D), Offset(o.x+t*13.08f,o.y+t*4.10f), Size(t*.70f,t*1.78f))
    drawRect(Color(0xFF6E9FA0), Offset(o.x+t*13.18f,o.y+t*4.25f), Size(t*.46f,t*1.42f))
    drawRect(Color(0xFF263C43), Offset(o.x+t*13.25f,o.y+t*4.34f), Size(t*.32f,t*1.22f))
    drawCircle(Color(0xFFE0A85C), t*.045f, Offset(o.x+t*13.56f,o.y+t*5.12f))
    val rain = time / 90L
    for (i in 0..10) {
        val rx = o.x + t*(13.10f + (i%4)*.20f)
        val ry = o.y + t*(4.15f + ((i*17L + rain)%150L)/100f)
        drawLine(Color(0xFF8CB6C1).copy(alpha=.28f), Offset(rx,ry), Offset(rx-t*.05f,ry+t*.24f), t*.025f)
    }
}

internal fun DrawScope.drawPixelNpc(center: Offset, tile: Float, variant: Int, timeMs: Long) {
    val s = tile * .72f
    val bob = sin(timeMs / 420f + variant * .9f) * s * .035f
    val cx = center.x
    val cy = center.y + bob
    val skin = when (variant) { 0 -> Color(0xFFFFC28E); 1 -> Color(0xFFE4A075); 2 -> Color(0xFFD08A64); else -> Color(0xFF9B624B) }
    val hair = when (variant) { 0 -> Color(0xFF342322); 1 -> Color(0xFF262125); 2 -> Color(0xFF5B382B); else -> Color(0xFF171A20) }
    val shirt = when (variant) { 0 -> Color(0xFF3E6B5B); 1 -> Color(0xFFE2E5DA); 2 -> Color(0xFFB85843); else -> Color(0xFF536B96) }

    drawOval(Color.Black.copy(alpha=.34f), Offset(cx-s*.32f,cy+s*.43f), Size(s*.64f,s*.18f))
    // Legs and shoes.
    val step = sin(timeMs / 150f + variant) * s*.06f
    drawRect(Color(0xFF343A45), Offset(cx-s*.16f,cy+s*.16f+step), Size(s*.12f,s*.34f))
    drawRect(Color(0xFF242B34), Offset(cx+s*.04f,cy+s*.16f-step), Size(s*.12f,s*.34f))
    drawRect(Color(0xFF1C1A1C), Offset(cx-s*.20f,cy+s*.47f+step), Size(s*.18f,s*.08f))
    drawRect(Color(0xFF1C1A1C), Offset(cx+s*.02f,cy+s*.47f-step), Size(s*.18f,s*.08f))
    // Body and apron/clothes.
    drawRoundRect(Color(0xFF302329), Offset(cx-s*.28f,cy-s*.03f), Size(s*.56f,s*.42f), CornerRadius(s*.07f))
    drawRoundRect(shirt, Offset(cx-s*.23f,cy-s*.01f), Size(s*.46f,s*.34f), CornerRadius(s*.04f))
    if (variant == 1) drawRect(Color(0xFF38423E), Offset(cx-s*.11f,cy+s*.02f), Size(s*.22f,s*.32f))
    // Head.
    drawRoundRect(Color(0xFF2B1D20), Offset(cx-s*.27f,cy-s*.50f), Size(s*.54f,s*.54f), CornerRadius(s*.14f))
    drawRoundRect(skin, Offset(cx-s*.22f,cy-s*.45f), Size(s*.44f,s*.43f), CornerRadius(s*.10f))
    // Hair silhouette.
    if (variant == 2) {
        for (i in -2..2) drawCircle(hair, s*.10f, Offset(cx+i*s*.09f,cy-s*.46f))
    } else {
        drawRect(hair, Offset(cx-s*.22f,cy-s*.48f), Size(s*.44f,s*.16f))
        drawRect(hair, Offset(cx-s*.24f,cy-s*.40f), Size(s*.10f,s*.17f))
    }
    if (variant == 3) {
        drawRect(Color(0xFF2D4A73), Offset(cx-s*.27f,cy-s*.54f), Size(s*.54f,s*.12f))
        drawRect(Color(0xFF466B99), Offset(cx-s*.18f,cy-s*.62f), Size(s*.36f,s*.10f))
    }
    // Eyes and tiny nose.
    drawRect(Color(0xFF2A2021), Offset(cx-s*.13f,cy-s*.29f), Size(s*.07f,s*.055f))
    drawRect(Color(0xFF2A2021), Offset(cx+s*.06f,cy-s*.29f), Size(s*.07f,s*.055f))
    drawRect(Color(0xFFC7785C), Offset(cx-s*.025f,cy-s*.20f), Size(s*.05f,s*.035f))
    // Chef hat for variant 1.
    if (variant == 1) {
        drawCircle(Color(0xFFF1EEE4), s*.15f, Offset(cx-s*.11f,cy-s*.57f))
        drawCircle(Color(0xFFF1EEE4), s*.15f, Offset(cx+s*.10f,cy-s*.57f))
        drawRect(Color(0xFFF1EEE4), Offset(cx-s*.22f,cy-s*.56f), Size(s*.44f,s*.14f))
    }
}

internal fun DrawScope.drawPixelHero(center: Offset, tile: Float, moving: Boolean, hasOrder: Boolean, hasBao: Boolean) {
    val s = tile * .78f
    val phase = if (moving) sin(System.currentTimeMillis() / 105f) else 0f
    val step = phase * s*.055f
    drawOval(Color.Black.copy(alpha=.38f), Offset(center.x-s*.31f,center.y+s*.44f), Size(s*.62f,s*.18f))
    drawRect(Color(0xFF27384D), Offset(center.x-s*.16f,center.y+s*.14f+step), Size(s*.12f,s*.34f))
    drawRect(Color(0xFF35536E), Offset(center.x+s*.04f,center.y+s*.14f-step), Size(s*.12f,s*.34f))
    drawRect(Color(0xFF17191D), Offset(center.x-s*.21f,center.y+s*.46f+step), Size(s*.20f,s*.08f))
    drawRect(Color(0xFF17191D), Offset(center.x+s*.01f,center.y+s*.46f-step), Size(s*.20f,s*.08f))
    drawRoundRect(Color(0xFF273F58), Offset(center.x-s*.27f,center.y-s*.06f), Size(s*.54f,s*.40f), CornerRadius(s*.06f))
    drawRect(Color(0xFFE1A85D), Offset(center.x-s*.055f,center.y-s*.04f), Size(s*.11f,s*.34f))
    drawRect(Color(0xFF202A36), Offset(center.x-s*.35f,center.y-s*.02f), Size(s*.10f,s*.38f))
    drawRect(Color(0xFF202A36), Offset(center.x+s*.25f,center.y-s*.02f), Size(s*.10f,s*.38f))
    drawCircle(Color(0xFFFFC994), s*.25f, Offset(center.x,center.y-s*.34f))
    drawRect(Color(0xFF382628), Offset(center.x-s*.24f,center.y-s*.55f), Size(s*.48f,s*.13f))
    drawRect(Color(0xFF382628), Offset(center.x-s*.18f,center.y-s*.62f), Size(s*.36f,s*.10f))
    drawRect(Color(0xFF292025), Offset(center.x-s*.13f,center.y-s*.38f), Size(s*.08f,s*.06f))
    drawRect(Color(0xFF292025), Offset(center.x+s*.05f,center.y-s*.38f), Size(s*.08f,s*.06f))
    if (hasOrder) {
        drawRoundRect(Color(0xFFF5E5C2), Offset(center.x-s*.62f,center.y-s*.58f), Size(s*.32f,s*.20f), CornerRadius(s*.04f))
        drawRect(Color(0xFF9B6644), Offset(center.x-s*.56f,center.y-s*.53f), Size(s*.18f,s*.025f))
    }
    if (hasBao) {
        drawCircle(Color(0xFFF0C47A), s*.13f, Offset(center.x+s*.58f,center.y+s*.04f))
        drawCircle(Color(0xFFFFE0A3), s*.055f, Offset(center.x+s*.55f,center.y-s*.01f))
    }
}
