package com.baocode.cafe

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import kotlin.math.max
import kotlin.math.min

/** Stylized deferred-light pass for the procedural isometric cafe. */
fun DrawScope.drawStage6Lighting5(
    center: (Float, Float) -> Offset,
    tileW: Float,
    tileH: Float,
    heroX: Float,
    heroY: Float
) {
    // Evening ambient: a cool veil keeps unlit corners readable.
    drawRect(Color(0xFF071014).copy(alpha = .30f))

    // Warm ceiling / room key light.
    radialGlow(center(7f, 4f), tileW * 4.4f, Color(0xFFFFB45C), .18f)
    radialGlow(center(10f, 7f), tileW * 3.0f, Color(0xFFFFC76B), .11f)

    // Individual pendant lamps above the tables.
    listOf(9f to 2f, 11f to 5f, 6f to 7f).forEach { (c, r) ->
        val p = center(c, r) - Offset(0f, tileH * 1.55f)
        drawLine(Color(0xFF4B3A32), p, p + Offset(0f, tileH * .70f), tileW * .025f)
        drawCircle(Color(0xFFFFD58A), tileW * .09f, p + Offset(0f, tileH * .70f))
        radialGlow(p + Offset(0f, tileH * .70f), tileW * 1.65f, Color(0xFFFFB85C), .22f)
    }

    // Kitchen-local practical light.
    val kitchen = center(2f, 2f) - Offset(0f, tileH * .25f)
    radialGlow(kitchen, tileW * 2.0f, Color(0xFFFF8A55), .22f)
    drawCircle(Color(0xFFFFD29A), tileW * .055f, kitchen + Offset(tileW * .22f, -tileH * .20f))

    // Ambient occlusion under furniture and around contact points.
    listOf(9f to 2f, 11f to 5f, 6f to 7f).forEach { (c, r) ->
        val p = center(c, r)
        drawOval(Color.Black.copy(alpha = .30f), Offset(p.x - tileW*.44f, p.y + tileH*.27f), Size(tileW*.88f, tileH*.27f))
    }
    drawOval(Color.Black.copy(alpha = .34f), Offset(center(heroX, heroY).x - tileW*.23f, center(heroX, heroY).y + tileH*.25f), Size(tileW*.46f, tileH*.18f))

    // Character rim light: a subtle warm halo separates silhouettes from the floor.
    radialGlow(center(heroX, heroY) - Offset(0f, tileH*.45f), tileW * 1.05f, Color(0xFFFFD08A), .10f)

    // Wet floor: thin broken highlights following the isometric tiles.
    for (r in 1..8) for (c in 1..12) {
        if ((c * 13 + r * 7) % 5 != 0) continue
        val p = center(c.toFloat(), r.toFloat())
        val a = .055f + ((c + r) % 3) * .018f
        drawLine(Color(0xFFB8D9D3).copy(alpha = a), p - Offset(tileW*.25f, tileH*.035f), p + Offset(tileW*.25f, tileH*.035f), tileW*.018f)
    }

    // Rain streak reflections from the doorway/window side.
    val door = center(13f, 5f)
    for (i in 0 until 9) {
        val p = door + Offset(tileW * (-.9f + i * .22f), tileH * (1.1f + (i % 3) * .18f))
        drawLine(Color(0xFF7CCFD0).copy(alpha = .10f), p, p + Offset(tileW*.42f, -tileH*.10f), tileW*.018f)
    }

    // A small cool reflection strip on the rainy exterior edge.
    drawLine(Color(0xFF75C9D1).copy(alpha = .13f), door + Offset(-tileW*.55f, tileH*.62f), door + Offset(tileW*.62f, tileH*.45f), tileW*.045f)
}

private fun DrawScope.radialGlow(center: Offset, radius: Float, color: Color, alpha: Float) {
    // Concentric translucent discs are inexpensive on Compose Canvas and read as soft light.
    val steps = 8
    for (i in steps downTo 1) {
        val t = i / steps.toFloat()
        val rr = radius * t
        val fade = alpha * (1f - t) * .95f
        if (fade > 0f) drawCircle(color.copy(alpha = min(.22f, max(0f, fade))), rr, center)
    }
}
