package com.baocode.cafe

import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import kotlin.math.sin

private enum class CafeNpcKind { WAITER, CHEF, GUEST_CLASSIC, GUEST_CURLY, GUEST_CAP }

private data class CafeNpc(
    val kind: CafeNpcKind,
    val x: Float,
    val y: Float,
    val phase: Float = 0f
)

private val cafeNpcs = listOf(
    CafeNpc(CafeNpcKind.WAITER, 8.0f, 4.2f, .4f),
    CafeNpc(CafeNpcKind.CHEF, 2.8f, 2.8f, 1.2f),
    CafeNpc(CafeNpcKind.GUEST_CLASSIC, 9.0f, 2.0f, .8f),
    CafeNpc(CafeNpcKind.GUEST_CURLY, 11.0f, 5.0f, 1.7f),
    CafeNpc(CafeNpcKind.GUEST_CAP, 6.0f, 7.0f, 2.4f)
)

internal fun DrawScope.drawCafeNpcCast(center: (Float, Float) -> Offset, tileW: Float) {
    cafeNpcs.forEach { npc ->
        drawCafeNpc(center(npc.x, npc.y), tileW * .205f, npc.kind, npc.phase)
    }
}

private fun DrawScope.drawCafeNpc(p: Offset, s: Float, kind: CafeNpcKind, phase: Float) {
    val bob = sin(phase) * s * .018f
    val q = p + Offset(0f, bob)
    val outline = Color(0xFF171719)
    val (skin, skinLight, skinShade, hair, hairLight, top, topLight, topShade, bottom, shoe, accent) = when (kind) {
        CafeNpcKind.WAITER -> listOf(Color(0xFFE0A27A), Color(0xFFF4C29C), Color(0xFFB86B55), Color(0xFF3A2522), Color(0xFF65413A), Color(0xFFF0E5D0), Color.White, Color(0xFFB7A994), Color(0xFF384B55), Color(0xFF20272A), Neon)
        CafeNpcKind.CHEF -> listOf(Color(0xFFC98562), Color(0xFFE4A57D), Color(0xFF995B4C), Color(0xFF302326), Color(0xFF593E43), Color(0xFFF5F0E7), Color.White, Color(0xFFC9C2B7), Color(0xFF5C3B32), Color(0xFF29272A), Color(0xFFE7B45B))
        CafeNpcKind.GUEST_CLASSIC -> listOf(Color(0xFFFFC891), Color(0xFFFFD9AD), Color(0xFFD98762), Color(0xFF5B3028), Color(0xFF875043), Color(0xFFB9584C), Color(0xFFD97A6A), Color(0xFF823B38), Color(0xFF394A63), Color(0xFF24282C), Color(0xFFE8B45E))
        CafeNpcKind.GUEST_CURLY -> listOf(Color(0xFF8C5A3D), Color(0xFFB97854), Color(0xFF633A30), Color(0xFF201C22), Color(0xFF473842), Color(0xFF7D5AA2), Color(0xFF9A78BE), Color(0xFF573B78), Color(0xFFE3B96B), Color(0xFF292A31), Color(0xFFE67E9B))
        CafeNpcKind.GUEST_CAP -> listOf(Color(0xFFE4B07C), Color(0xFFF5C99A), Color(0xFFB56E54), Color(0xFFB93E47), Color(0xFFD96767), Color(0xFF4E8D78), Color(0xFF6BA992), Color(0xFF315F51), Color(0xFF39414C), Color(0xFF20262C), Color(0xFF55C7B0))
    }

    drawOval(Color.Black.copy(alpha = .32f), Offset(q.x - s * .78f, q.y + s * .58f), Size(s * 1.56f, s * .40f))

    // Compact body: oversized head + short legs, matching the Stage 2 style.
    drawRoundRect(outline, Offset(q.x - s * .30f, q.y + s * .49f), Size(s * .22f, s * .50f), CornerRadius(s * .07f))
    drawRoundRect(outline, Offset(q.x + s * .08f, q.y + s * .49f), Size(s * .22f, s * .50f), CornerRadius(s * .07f))
    drawRoundRect(bottom, Offset(q.x - s * .26f, q.y + s * .51f), Size(s * .14f, s * .41f), CornerRadius(s * .05f))
    drawRoundRect(bottom, Offset(q.x + s * .12f, q.y + s * .51f), Size(s * .14f, s * .41f), CornerRadius(s * .05f))
    drawRoundRect(outline, Offset(q.x - s * .38f, q.y + s * .88f), Size(s * .34f, s * .17f), CornerRadius(s * .07f))
    drawRoundRect(outline, Offset(q.x, q.y + s * .88f), Size(s * .34f, s * .17f), CornerRadius(s * .07f))
    drawRoundRect(shoe, Offset(q.x - s * .34f, q.y + s * .91f), Size(s * .27f, s * .10f), CornerRadius(s * .04f))
    drawRoundRect(shoe, Offset(q.x + s * .04f, q.y + s * .91f), Size(s * .27f, s * .10f), CornerRadius(s * .04f))

    val torso = Path().apply {
        moveTo(q.x - s * .43f, q.y + s * .05f)
        lineTo(q.x - s * .28f, q.y - s * .04f)
        lineTo(q.x + s * .28f, q.y - s * .04f)
        lineTo(q.x + s * .43f, q.y + s * .05f)
        lineTo(q.x + s * .31f, q.y + s * .58f)
        lineTo(q.x - s * .31f, q.y + s * .58f)
        close()
    }
    drawPath(torso, outline)
    drawPath(Path().apply { moveTo(q.x - s * .36f, q.y + s * .07f); lineTo(q.x - s * .25f, q.y); lineTo(q.x + s * .25f, q.y); lineTo(q.x + s * .36f, q.y + s * .07f); lineTo(q.x + s * .25f, q.y + s * .51f); lineTo(q.x - s * .25f, q.y + s * .51f); close() }, top)
    drawPath(Path().apply { moveTo(q.x - s * .36f, q.y + s * .07f); lineTo(q.x - s * .25f, q.y); lineTo(q.x - s * .25f, q.y + s * .51f); close() }, topLight.copy(alpha = .78f))
    drawPath(Path().apply { moveTo(q.x + s * .25f, q.y); lineTo(q.x + s * .36f, q.y + s * .07f); lineTo(q.x + s * .25f, q.y + s * .51f); close() }, topShade)

    // Arms + hands.
    drawRoundRect(outline, Offset(q.x - s * .59f, q.y + s * .07f), Size(s * .22f, s * .52f), CornerRadius(s * .09f))
    drawRoundRect(outline, Offset(q.x + s * .37f, q.y + s * .07f), Size(s * .22f, s * .52f), CornerRadius(s * .09f))
    drawRoundRect(topLight, Offset(q.x - s * .55f, q.y + s * .10f), Size(s * .14f, s * .34f), CornerRadius(s * .07f))
    drawRoundRect(topShade, Offset(q.x + s * .41f, q.y + s * .10f), Size(s * .14f, s * .34f), CornerRadius(s * .07f))
    drawRoundRect(skin, Offset(q.x - s * .55f, q.y + s * .37f), Size(s * .14f, s * .17f), CornerRadius(s * .06f))
    drawRoundRect(skinShade, Offset(q.x + s * .41f, q.y + s * .37f), Size(s * .14f, s * .17f), CornerRadius(s * .06f))

    drawRoundRect(outline, Offset(q.x - s * .14f, q.y - s * .28f), Size(s * .28f, s * .22f), CornerRadius(s * .07f))
    drawRoundRect(skinShade, Offset(q.x - s * .09f, q.y - s * .26f), Size(s * .18f, s * .18f), CornerRadius(s * .05f))

    val hc = q - Offset(0f, s * .62f)
    val r = s * .60f
    val head = Path().apply {
        moveTo(hc.x, hc.y - r); lineTo(hc.x + r * .68f, hc.y - r * .72f); lineTo(hc.x + r, hc.y - r * .10f)
        lineTo(hc.x + r * .80f, hc.y + r * .60f); lineTo(hc.x + r * .28f, hc.y + r)
        lineTo(hc.x - r * .30f, hc.y + r); lineTo(hc.x - r * .82f, hc.y + r * .58f)
        lineTo(hc.x - r, hc.y - r * .10f); lineTo(hc.x - r * .68f, hc.y - r * .72f); close()
    }
    drawPath(head, outline)
    val face = Path().apply {
        moveTo(hc.x, hc.y - r * .88f); lineTo(hc.x + r * .58f, hc.y - r * .62f); lineTo(hc.x + r * .84f, hc.y - r * .08f)
        lineTo(hc.x + r * .66f, hc.y + r * .55f); lineTo(hc.x + r * .24f, hc.y + r * .82f)
        lineTo(hc.x - r * .30f, hc.y + r * .82f); lineTo(hc.x - r * .68f, hc.y + r * .52f)
        lineTo(hc.x - r * .84f, hc.y - r * .08f); lineTo(hc.x - r * .58f, hc.y - r * .62f); close()
    }
    drawPath(face, skin)
    drawPath(Path().apply { moveTo(hc.x - r * .84f, hc.y - r * .08f); lineTo(hc.x - r * .58f, hc.y - r * .62f); lineTo(hc.x, hc.y - r * .88f); lineTo(hc.x - r * .30f, hc.y + r * .82f); lineTo(hc.x - r * .68f, hc.y + r * .52f); close() }, skinLight.copy(alpha = .88f))
    drawPath(Path().apply { moveTo(hc.x, hc.y - r * .88f); lineTo(hc.x + r * .58f, hc.y - r * .62f); lineTo(hc.x + r * .84f, hc.y - r * .08f); lineTo(hc.x + r * .66f, hc.y + r * .55f); lineTo(hc.x + r * .24f, hc.y + r * .82f); lineTo(hc.x, hc.y + r * .18f); close() }, skinShade.copy(alpha = .75f))

    when (kind) {
        CafeNpcKind.WAITER -> {
            drawRoundRect(hair, Offset(hc.x - r * .82f, hc.y - r * .77f), Size(r * 1.64f, r * .42f), CornerRadius(r * .10f))
            drawRoundRect(hairLight, Offset(hc.x - r * .55f, hc.y - r * .73f), Size(r * .65f, r * .16f), CornerRadius(r * .06f))
            drawRoundRect(accent, Offset(q.x - s * .065f, q.y + s * .12f), Size(s * .13f, s * .42f), CornerRadius(s * .025f))
            drawTray(q + Offset(s * .73f, s * .18f), s * .27f)
        }
        CafeNpcKind.CHEF -> {
            drawRoundRect(hair, Offset(hc.x - r * .78f, hc.y - r * .70f), Size(r * 1.56f, r * .35f), CornerRadius(r * .10f))
            drawChefHat(hc, r, top, topLight, outline)
            drawRoundRect(accent, Offset(q.x - s * .18f, q.y + s * .10f), Size(s * .36f, s * .13f), CornerRadius(s * .04f))
        }
        CafeNpcKind.GUEST_CLASSIC -> {
            drawRoundRect(hair, Offset(hc.x - r * .88f, hc.y - r * .25f), Size(r * 1.76f, r * .48f), CornerRadius(r * .12f))
            drawPath(Path().apply { moveTo(hc.x - r * .72f, hc.y - r * .32f); lineTo(hc.x - r * .30f, hc.y - r * .80f); lineTo(hc.x + r * .38f, hc.y - r * .72f); lineTo(hc.x + r * .78f, hc.y - r * .25f); close() }, hairLight)
            drawRoundRect(accent, Offset(q.x - s * .28f, q.y + s * .10f), Size(s * .56f, s * .08f), CornerRadius(s * .03f))
        }
        CafeNpcKind.GUEST_CURLY -> {
            for (i in -4..4) for (j in -1..2) if ((i + j) % 2 == 0) drawCircle(hair, r * .22f, Offset(hc.x + i * r * .18f, hc.y - r * .55f + j * r * .13f))
            drawCircle(hairLight, r * .10f, Offset(hc.x - r * .32f, hc.y - r * .72f))
            drawCircle(hairLight, r * .10f, Offset(hc.x + r * .30f, hc.y - r * .65f))
            drawCircle(accent, s * .055f, Offset(q.x + s * .27f, q.y + s * .27f))
        }
        CafeNpcKind.GUEST_CAP -> {
            drawRoundRect(hair, Offset(hc.x - r * .72f, hc.y - r * .22f), Size(r * 1.44f, r * .36f), CornerRadius(r * .10f))
            drawCap(hc, r, accent, outline)
        }
    }

    // Readable face shared by the cast.
    val eyeY = hc.y - r * .03f
    val eo = r * .29f
    drawRoundRect(Color(0xFFF8F4EA), Offset(hc.x - eo - r * .17f, eyeY - r * .12f), Size(r * .30f, r * .24f), CornerRadius(r * .08f))
    drawRoundRect(Color(0xFFF8F4EA), Offset(hc.x + eo - r * .13f, eyeY - r * .12f), Size(r * .30f, r * .24f), CornerRadius(r * .08f))
    drawCircle(hair, r * .075f, Offset(hc.x - eo + r * .04f, eyeY))
    drawCircle(hair, r * .075f, Offset(hc.x + eo + r * .04f, eyeY))
    drawCircle(Color.White, r * .024f, Offset(hc.x - eo + r * .06f, eyeY - r * .025f))
    drawCircle(Color.White, r * .024f, Offset(hc.x + eo + r * .06f, eyeY - r * .025f))
    drawLine(hair, Offset(hc.x - eo - r * .10f, eyeY - r * .17f), Offset(hc.x - eo + r * .10f, eyeY - r * .19f), r * .035f)
    drawLine(hair, Offset(hc.x + eo - r * .10f, eyeY - r * .19f), Offset(hc.x + eo + r * .10f, eyeY - r * .17f), r * .035f)
    drawLine(skinShade, Offset(hc.x + r * .05f, eyeY + r * .08f), Offset(hc.x + r * .10f, eyeY + r * .19f), r * .035f)

    // Tiny role-specific accessories.
    when (kind) {
        CafeNpcKind.WAITER -> Unit
        CafeNpcKind.CHEF -> drawCircle(accent, s * .06f, Offset(q.x + s * .30f, q.y + s * .25f))
        CafeNpcKind.GUEST_CLASSIC -> drawCircle(accent, s * .045f, Offset(q.x + s * .26f, q.y + s * .32f))
        CafeNpcKind.GUEST_CURLY -> drawRoundRect(accent.copy(alpha = .72f), Offset(q.x - s * .20f, q.y + s * .52f), Size(s * .40f, s * .06f), CornerRadius(s * .03f))
        CafeNpcKind.GUEST_CAP -> drawLine(accent, Offset(q.x - s * .17f, q.y + s * .31f), Offset(q.x + s * .17f, q.y + s * .31f), s * .05f)
    }
}

private fun DrawScope.drawChefHat(center: Offset, r: Float, fill: Color, light: Color, outline: Color) {
    drawRoundRect(outline, Offset(center.x - r * .50f, center.y - r * 1.05f), Size(r, r * .34f), CornerRadius(r * .08f))
    drawRoundRect(fill, Offset(center.x - r * .45f, center.y - r * 1.02f), Size(r * .90f, r * .25f), CornerRadius(r * .07f))
    drawCircle(fill, r * .25f, center + Offset(-r * .25f, -r * 1.02f))
    drawCircle(light, r * .23f, center + Offset(r * .10f, -r * 1.08f))
    drawCircle(fill.copy(alpha = .75f), r * .20f, center + Offset(r * .32f, -r * .96f))
}

private fun DrawScope.drawCap(center: Offset, r: Float, accent: Color, outline: Color) {
    drawPath(Path().apply { moveTo(center.x - r * .72f, center.y - r * .48f); lineTo(center.x - r * .30f, center.y - r * .90f); lineTo(center.x + r * .52f, center.y - r * .68f); lineTo(center.x + r * .72f, center.y - r * .42f); close() }, outline)
    drawPath(Path().apply { moveTo(center.x - r * .62f, center.y - r * .50f); lineTo(center.x - r * .25f, center.y - r * .82f); lineTo(center.x + r * .44f, center.y - r * .62f); lineTo(center.x + r * .62f, center.y - r * .45f); close() }, accent)
}

private fun DrawScope.drawTray(p: Offset, r: Float) {
    drawOval(Color.Black.copy(alpha = .28f), Offset(p.x - r, p.y - r * .30f), Size(r * 2f, r * .65f))
    drawOval(Neon, Offset(p.x - r * .82f, p.y - r * .28f), Size(r * 1.64f, r * .48f))
    drawCircle(Cream, r * .17f, p - Offset(r * .25f, r * .05f))
    drawCircle(Color(0xFFF0C47A), r * .13f, p + Offset(r * .24f, r * .02f))
}
