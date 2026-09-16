package com.baocode.cafe

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import kotlin.math.max
import kotlin.math.min

/** Unified isometric composition pass. The existing world already projects every cell
 * through the same diamond grid; this pass makes that projection read as one camera view. */
fun DrawScope.drawStage6Lighting5(
    center: (Float, Float) -> Offset,
    tileW: Float,
    tileH: Float,
    heroX: Float,
    heroY: Float
) {
    // Stage 7 camera composition: a restrained vignette and room-frame treatment keep
    // the playable room visually separated from the surrounding UI/background.
    drawStage7IsoBackdropOverlay()
    drawStage7RoomComposition(center, tileW, tileH)

    // Evening ambient.
    drawRect(Color(0xFF071014).copy(alpha = .26f))

    // Warm room key lights.
    radialGlow(center(7f, 4f), tileW * 4.4f, Color(0xFFFFB45C), .18f)
    radialGlow(center(10f, 7f), tileW * 3.0f, Color(0xFFFFC76B), .11f)

    // Pendant lamps.
    listOf(9f to 2f, 11f to 5f, 6f to 7f).forEach { (c, r) ->
        val p = center(c, r) - Offset(0f, tileH * 1.55f)
        drawLine(Color(0xFF4B3A32), p, p + Offset(0f, tileH * .70f), tileW * .025f)
        drawCircle(Color(0xFFFFD58A), tileW * .09f, p + Offset(0f, tileH * .70f))
        radialGlow(p + Offset(0f, tileH * .70f), tileW * 1.65f, Color(0xFFFFB85C), .22f)
    }

    // Kitchen-local light.
    val kitchen = center(2f, 2f) - Offset(0f, tileH * .25f)
    radialGlow(kitchen, tileW * 2.0f, Color(0xFFFF8A55), .22f)
    drawCircle(Color(0xFFFFD29A), tileW * .055f, kitchen + Offset(tileW * .22f, -tileH * .20f))

    // Contact AO / grounding shadows.
    listOf(9f to 2f, 11f to 5f, 6f to 7f).forEach { (c, r) ->
        val p = center(c, r)
        drawOval(Color.Black.copy(alpha = .30f), Offset(p.x - tileW*.44f, p.y + tileH*.27f), Size(tileW*.88f, tileH*.27f))
    }
    val hero = center(heroX, heroY)
    drawOval(Color.Black.copy(alpha = .34f), Offset(hero.x - tileW*.23f, hero.y + tileH*.25f), Size(tileW*.46f, tileH*.18f))
    radialGlow(hero - Offset(0f, tileH*.45f), tileW * 1.05f, Color(0xFFFFD08A), .10f)

    // Wet isometric floor reflections.
    for (r in 1..8) for (c in 1..12) {
        if ((c * 13 + r * 7) % 5 != 0) continue
        val p = center(c.toFloat(), r.toFloat())
        val a = .055f + ((c + r) % 3) * .018f
        drawLine(Color(0xFFB8D9D3).copy(alpha = a), p - Offset(tileW*.25f, tileH*.035f), p + Offset(tileW*.25f, tileH*.035f), tileW*.018f)
    }

    // Rain reflections entering through the door.
    val door = center(13f, 5f)
    for (i in 0 until 9) {
        val p = door + Offset(tileW * (-.9f + i * .22f), tileH * (1.1f + (i % 3) * .18f))
        drawLine(Color(0xFF7CCFD0).copy(alpha = .10f), p, p + Offset(tileW*.42f, -tileH*.10f), tileW*.018f)
    }
    drawLine(Color(0xFF75C9D1).copy(alpha = .13f), door + Offset(-tileW*.55f, tileH*.62f), door + Offset(tileW*.62f, tileH*.45f), tileW*.045f)
}

/** Camera framing overlay: emphasizes the diamond room bounds, depth direction and
 * the open doorway, without changing the underlying cell coordinates. */
private fun DrawScope.drawStage7IsoBackdropOverlay() {
    drawRect(Color(0xFF020608))
    drawRect(Color(0xFF101B20).copy(alpha = .16f))
}

private fun DrawScope.drawStage7RoomComposition(
    center: (Float, Float) -> Offset,
    tileW: Float,
    tileH: Float
) {
    // Soft outer vignette: the centre of the room remains the visual focus.
    drawRect(Color.Black.copy(alpha = .07f))

    // Isometric axes are subtly reinforced at the room perimeter, making walls,
    // furniture and characters feel locked to one camera instead of floating sprites.
    val a = center(0f, 0f)
    val b = center(13f, 0f)
    val c = center(13f, 9f)
    val d = center(0f, 9f)
    val edge = Color(0xFFB57950).copy(alpha = .18f)
    drawLine(edge, a, b, tileW * .022f)
    drawLine(edge, b, c, tileW * .022f)
    drawLine(edge, c, d, tileW * .022f)
    drawLine(edge, d, a, tileW * .022f)

    // Door axis: a short cool guide visually connects the room to the rainy exterior.
    val door = center(13f, 5f)
    drawLine(Color(0xFF73C7CA).copy(alpha = .16f), door, door + Offset(tileW*.90f, tileH*.38f), tileW*.028f)

    // Floor contact bands reinforce the depth ordering used by the isometric grid.
    for (r in 1..8) {
        val left = center(1f, r.toFloat())
        val right = center(12f, r.toFloat())
        drawLine(Color(0xFF2C1C18).copy(alpha = .11f), left, right, tileW * .012f)
    }
}

private fun DrawScope.radialGlow(center: Offset, radius: Float, color: Color, alpha: Float) {
    val steps = 8
    for (i in steps downTo 1) {
        val t = i / steps.toFloat()
        val rr = radius * t
        val fade = alpha * (1f - t) * .95f
        if (fade > 0f) drawCircle(color.copy(alpha = min(.22f, max(0f, fade))), rr, center)
    }
}
