package com.baocode.cafe

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sin

/** Unified isometric composition and lightweight final-effects pass. */
fun DrawScope.drawStage6Lighting5(
    center: (Float, Float) -> Offset,
    tileW: Float,
    tileH: Float,
    heroX: Float,
    heroY: Float
) {
    // Lighting is deliberately translucent: the floor/grid is rendered below this pass
    // and must remain clearly readable on small Android screens.
    drawRect(Color(0xFF101B20).copy(alpha = .08f))
    drawRect(Color(0xFF071014).copy(alpha = .10f))

    drawStage7RoomComposition(center, tileW, tileH)

    // Soft pools of warm light, kept behind the world objects.
    radialGlow(center(7f, 4f), tileW * 3.2f, Color(0xFFFFB45C), .075f)
    radialGlow(center(10f, 7f), tileW * 2.2f, Color(0xFFFFC76B), .055f)

    listOf(9f to 2f, 11f to 5f, 6f to 7f).forEach { (c, r) ->
        val p = center(c, r) - Offset(0f, tileH * 1.55f)
        drawLine(Color(0xFF4B3A32).copy(alpha = .70f), p, p + Offset(0f, tileH * .70f), tileW * .025f)
        drawCircle(Color(0xFFFFD58A), tileW * .07f, p + Offset(0f, tileH * .70f))
        radialGlow(p + Offset(0f, tileH * .70f), tileW * 1.05f, Color(0xFFFFB85C), .11f)
    }

    val kitchen = center(2f, 2f) - Offset(0f, tileH * .25f)
    radialGlow(kitchen, tileW * 1.35f, Color(0xFFFF8A55), .12f)
    drawCircle(Color(0xFFFFD29A), tileW * .045f, kitchen + Offset(tileW * .22f, -tileH * .20f))

    // Contact shadows make tables and the hero sit on the floor instead of floating.
    listOf(9f to 2f, 11f to 5f, 6f to 7f).forEach { (c, r) ->
        val p = center(c, r)
        drawOval(Color.Black.copy(alpha = .30f), Offset(p.x - tileW*.44f, p.y + tileH*.27f), Size(tileW*.88f, tileH*.27f))
    }
    val hero = center(heroX, heroY)
    drawOval(Color.Black.copy(alpha = .36f), Offset(hero.x - tileW*.23f, hero.y + tileH*.25f), Size(tileW*.46f, tileH*.18f))

    // Restrained wet-floor highlights near the entrance.
    for (r in 1..8) for (c in 1..12) if ((c * 13 + r * 7) % 5 == 0) {
        val p = center(c.toFloat(), r.toFloat())
        drawLine(Color(0xFFB8D9D3).copy(alpha = .035f + ((c+r)%3)*.012f), p - Offset(tileW*.25f,tileH*.035f), p + Offset(tileW*.25f,tileH*.035f), tileW*.014f)
    }
    val door = center(13f, 5f)
    drawLine(Color(0xFF75C9D1).copy(alpha = .18f), door + Offset(-tileW*.55f,tileH*.62f), door + Offset(tileW*.62f,tileH*.45f), tileW*.035f)
}

private fun DrawScope.drawStage7RoomComposition(center: (Float, Float) -> Offset, tileW: Float, tileH: Float) {
    val a = center(0f,0f); val b = center(13f,0f); val c = center(13f,9f); val d = center(0f,9f)
    val edge = Color(0xFFD6A06E).copy(alpha = .30f)
    drawLine(edge,a,b,tileW*.026f)
    drawLine(edge,b,c,tileW*.026f)
    drawLine(edge,c,d,tileW*.026f)
    drawLine(edge,d,a,tileW*.026f)

    // Subtle inner frame makes the playable room read as a distinct floor plane.
    val inset = tileW * .30f
    val ia = Offset(a.x + inset, a.y + tileH*.22f)
    val ib = Offset(b.x - inset, b.y + tileH*.22f)
    val ic = Offset(c.x - inset, c.y - tileH*.22f)
    val id = Offset(d.x + inset, d.y - tileH*.22f)
    val inner = Color(0xFF8B6A4F).copy(alpha = .22f)
    drawLine(inner, ia, ib, tileW*.014f)
    drawLine(inner, ib, ic, tileW*.014f)
    drawLine(inner, ic, id, tileW*.014f)
    drawLine(inner, id, ia, tileW*.014f)

    val door = center(13f,5f)
    drawLine(Color(0xFF73C7CA).copy(alpha=.26f),door,door+Offset(tileW*.9f,tileH*.38f),tileW*.028f)
}

/** Final animated effects: steam, rain and restrained character breathing/rim pulse. */
fun DrawScope.drawStage8FinalFx(center: (Float, Float) -> Offset, tileW: Float, tileH: Float, heroX: Float, heroY: Float, timeMs: Long) {
    val t = timeMs / 1000f
    val pulse = sin(t * 2.1f) * .5f + .5f
    val kitchen = center(2f,2f)
    for (i in 0 until 3) {
        val phase = (t * .42f + i * .31f) % 1f
        val x = kitchen.x + tileW * (-.13f + i*.13f) + sin(t*.8f+i)*tileW*.025f
        val y = kitchen.y - tileH*(.55f+phase*.75f)
        drawCircle(Color(0xFFE8D8C1).copy(alpha=.075f*(1f-phase)), tileW*(.09f-phase*.025f), Offset(x,y))
    }
    val door = center(13f,5f)
    for (i in 0 until 12) {
        val phase = (t*1.7f+i*.113f)%1f
        val x = door.x + tileW*(-1.05f+(i%6)*.38f)
        val y = door.y - tileH*(.20f+phase*1.15f)
        drawLine(Color(0xFF8ED8DD).copy(alpha=.10f+pulse*.035f), Offset(x,y), Offset(x-tileW*.025f,y+tileH*.22f), tileW*.012f)
    }
    drawOval(Color(0xFF9AD6D5).copy(alpha=.035f+pulse*.025f), Offset(door.x-tileW*.78f,door.y+tileH*.66f), Size(tileW*1.5f,tileH*.28f))
    val hero = center(heroX,heroY)
    drawOval(Color(0xFFFFD08A).copy(alpha=.035f+pulse*.035f), Offset(hero.x-tileW*.28f,hero.y-tileH*.72f), Size(tileW*.56f,tileH*.24f))
}

private fun DrawScope.radialGlow(center: Offset, radius: Float, color: Color, alpha: Float) {
    val steps = 8
    for (i in steps downTo 1) {
        val t=i/steps.toFloat()
        val fade=alpha*(1f-t)*.95f
        if(fade>0f) drawCircle(color.copy(alpha=min(.16f,max(0f,fade))),radius*t,center)
    }
}
