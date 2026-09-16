package com.baocode.cafe

import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import kotlin.math.max

private enum class NpcType { WAITER, CHEF, GUEST_CLASSIC, GUEST_CURLY, GUEST_CAP }

private data class NpcPalette(
    val skin: Color,
    val skinLight: Color,
    val skinShade: Color,
    val hair: Color,
    val hairLight: Color,
    val hairShade: Color,
    val top: Color,
    val topLight: Color,
    val topShade: Color,
    val bottom: Color,
    val bottomLight: Color,
    val shoes: Color,
    val shoesLight: Color,
    val accent: Color
)

private fun npcPalette(type: NpcType): NpcPalette = when (type) {
    NpcType.WAITER -> NpcPalette(Color(0xFFE0A27A), Color(0xFFF4C29C), Color(0xFFB86B55), Color(0xFF3A2522), Color(0xFF65413A), Color(0xFF211719), Color(0xFFF0E5D0), Color.White, Color(0xFFB7A994), Color(0xFF384B55), Color(0xFF536A73), Color(0xFF20272A), Color(0xFF596266), Neon)
    NpcType.CHEF -> NpcPalette(Color(0xFFC98562), Color(0xFFE4A57D), Color(0xFF995B4C), Color(0xFF302326), Color(0xFF593E43), Color(0xFF1C171A), Color(0xFFF5F0E7), Color.White, Color(0xFFC9C2B7), Color(0xFF5C3B32), Color(0xFF805447), Color(0xFF29272A), Color(0xFF626066), Color(0xFFE7B45B))
    NpcType.GUEST_CLASSIC -> NpcPalette(Color(0xFFFFC891), Color(0xFFFFD9AD), Color(0xFFD98762), Color(0xFF5B3028), Color(0xFF875043), Color(0xFF321D1D), Color(0xFFB9584C), Color(0xFFD97A6A), Color(0xFF823B38), Color(0xFF394A63), Color(0xFF536A84), Color(0xFF24282C), Color(0xFF555D61), Color(0xFFE8B45E))
    NpcType.GUEST_CURLY -> NpcPalette(Color(0xFF8C5A3D), Color(0xFFB97854), Color(0xFF633A30), Color(0xFF201C22), Color(0xFF473842), Color(0xFF131216), Color(0xFF7D5AA2), Color(0xFF9A78BE), Color(0xFF573B78), Color(0xFFE3B96B), Color(0xFFF0CF8C), Color(0xFF292A31), Color(0xFF62656D), Color(0xFFE67E9B))
    NpcType.GUEST_CAP -> NpcPalette(Color(0xFFE4B07C), Color(0xFFF5C99A), Color(0xFFB56E54), Color(0xFFB93E47), Color(0xFFD96767), Color(0xFF752A35), Color(0xFF4E8D78), Color(0xFF6BA992), Color(0xFF315F51), Color(0xFF39414C), Color(0xFF566170), Color(0xFF20262C), Color(0xFF5B636A), Color(0xFF55C7B0))
}

private fun DrawScope.drawNpcSprite(p: Offset, scale: Float, type: NpcType, facing: Int = 1, bobPhase: Float = 0f) {
    val c = npcPalette(type)
    val outline = Color(0xFF171719)
    val q = p + Offset(0f, kotlin.math.sin(bobPhase) * scale * .025f)
    val headR = scale * .60f
    val headCenter = q - Offset(0f, scale * .62f)
    val legY = q.y + scale * .50f

    drawOval(Color.Black.copy(alpha = .32f), Offset(q.x - scale * .78f, q.y + scale * .58f), Size(scale * 1.56f, scale * .40f))

    // Short legs and chunky shoes.
    drawRoundRect(outline, Offset(q.x - scale * .30f, legY), Size(scale * .22f, scale * .52f), CornerRadius(scale * .07f))
    drawRoundRect(outline, Offset(q.x + scale * .08f, legY), Size(scale * .22f, scale * .52f), CornerRadius(scale * .07f))
    drawRoundRect(c.bottom, Offset(q.x - scale * .26f, legY + scale * .02f), Size(scale * .14f, scale * .43f), CornerRadius(scale * .05f))
    drawRoundRect(c.bottomLight, Offset(q.x + scale * .12f, legY + scale * .02f), Size(scale * .14f, scale * .43f), CornerRadius(scale * .05f))
    drawRoundRect(outline, Offset(q.x - scale * .38f, legY + scale * .39f), Size(scale * .34f, scale * .18f), CornerRadius(scale * .08f))
    drawRoundRect(outline, Offset(q.x + scale * .00f, legY + scale * .39f), Size(scale * .34f, scale * .18f), CornerRadius(scale * .08f))
    drawRoundRect(c.shoes, Offset(q.x - scale * .34f, legY + scale * .42f), Size(scale * .27f, scale * .11f), CornerRadius(scale * .05f))
    drawRoundRect(c.shoesLight, Offset(q.x + scale * .04f, legY + scale * .42f), Size(scale * .27f, scale * .11f), CornerRadius(scale * .05f))

    // Faceted torso.
    val bodyTop = q.y
    val torso = Path().apply {
        moveTo(q.x - scale * .43f, bodyTop + scale * .06f)
        lineTo(q.x - scale * .30f, bodyTop - scale * .06f)
        lineTo(q.x + scale * .30f, bodyTop - scale * .06f)
        lineTo(q.x + scale * .43f, bodyTop + scale * .06f)
        lineTo(q.x + scale * .31f, bodyTop + scale * .58f)
        lineTo(q.x - scale * .31f, bodyTop + scale * .58f)
        close()
    }
    drawPath(torso, outline)
    val face = Path().apply {
        moveTo(q.x - scale * .36f, bodyTop + scale * .08f)
        lineTo(q.x - scale * .25f, bodyTop)
        lineTo(q.x + scale * .25f, bodyTop)
        lineTo(q.x + scale * .36f, bodyTop + scale * .08f)
        lineTo(q.x + scale * .25f, bodyTop + scale * .51f)
        lineTo(q.x - scale * .25f, bodyTop + scale * .51f)
        close()
    }
    drawPath(face, c.top)
    drawPath(Path().apply { moveTo(q.x - scale * .36f, bodyTop + scale * .08f); lineTo(q.x - scale * .25f, bodyTop); lineTo(q.x - scale * .25f, bodyTop + scale * .51f); lineTo(q.x - scale * .36f, bodyTop + scale * .08f); close() }, c.topLight.copy(alpha = .82f))
    drawPath(Path().apply { moveTo(q.x + scale * .25f, bodyTop); lineTo(q.x + scale * .36f, bodyTop + scale * .08f); lineTo(q.x + scale * .25f, bodyTop + scale * .51f); lineTo(q.x + scale * .25f, bodyTop); close() }, c.topShade)

    // Role-specific uniform details.
    when (type) {
        NpcType.WAITER -> {
            drawRoundRect(c.accent, Offset(q.x - scale * .065f, bodyTop + scale * .12f), Size(scale * .13f, scale * .42f), CornerRadius(scale * .025f))
            drawLine(c.accent, Offset(q.x - scale * .30f, bodyTop + scale * .08f), Offset(q.x + scale * .30f, bodyTop + scale * .08f), scale * .055f, StrokeCap.Round)
            drawCircle(Color.White, scale * .026f, Offset(q.x, bodyTop + scale * .28f))
            drawCircle(Color.White, scale * .026f, Offset(q.x, bodyTop + scale * .40f))
        }
        NpcType.CHEF -> {
            drawRoundRect(c.accent, Offset(q.x - scale * .18f, bodyTop + scale * .10f), Size(scale * .36f, scale * .13f), CornerRadius(scale * .04f))
            drawCircle(Color.White.copy(alpha = .90f), scale * .035f, Offset(q.x - scale * .11f, bodyTop + scale * .30f))
            drawCircle(Color.White.copy(alpha = .90f), scale * .035f, Offset(q.x + scale * .11f, bodyTop + scale * .30f))
        }
        NpcType.GUEST_CLASSIC -> {
            drawRoundRect(c.accent, Offset(q.x - scale * .28f, bodyTop + scale * .10f), Size(scale * .56f, scale * .08f), CornerRadius(scale * .03f))
        }
        NpcType.GUEST_CURLY -> {
            drawCircle(c.accent, scale * .055f, Offset(q.x + scale * .27f, bodyTop + scale * .27f))
            drawCircle(c.accent.copy(alpha = .55f), scale * .10f, Offset(q.x, bodyTop + scale * .46f))
        }
        NpcType.GUEST_CAP -> {
            drawRoundRect(c.accent, Offset(q.x - scale * .23f, bodyTop + scale * .08f), Size(scale * .46f, scale * .13f), CornerRadius(scale * .05f))
        }
    }

    // Arms and hands.
    drawRoundRect(outline, Offset(q.x - scale * .59f, bodyTop + scale * .08f), Size(scale * .22f, scale * .52f), CornerRadius(scale * .09f))
    drawRoundRect(outline, Offset(q.x + scale * .37f, bodyTop + scale * .08f), Size(scale * .22f, scale * .52f), CornerRadius(scale * .09f))
    drawRoundRect(c.topLight, Offset(q.x - scale * .55f, bodyTop + scale * .11f), Size(scale * .14f, scale * .34f), CornerRadius(scale * .07f))
    drawRoundRect(c.topShade, Offset(q.x + scale * .41f, bodyTop + scale * .11f), Size(scale * .14f, scale * .34f), CornerRadius(scale * .07f))
    drawRoundRect(c.skin, Offset(q.x - scale * .55f, bodyTop + scale * .38f), Size(scale * .14f, scale * .17f), CornerRadius(scale * .06f))
    drawRoundRect(c.skinShade, Offset(q.x + scale * .41f, bodyTop + scale * .38f), Size(scale * .14f, scale * .17f), CornerRadius(scale * .06f))

    // Neck.
    drawRoundRect(outline, Offset(q.x - scale * .14f, q.y - scale * .28f), Size(scale * .28f, scale * .22f), CornerRadius(scale * .07f))
    drawRoundRect(c.skinShade, Offset(q.x - scale * .09f, q.y - scale * .26f), Size(scale * .18f, scale * .18f), CornerRadius(scale * .05f))

    // Oversized faceted head.
    val headOutline = Path().apply {
        moveTo(headCenter.x, headCenter.y - headR)
        lineTo(headCenter.x + headR * .68f, headCenter.y - headR * .72f)
        lineTo(headCenter.x + headR, headCenter.y - headR * .10f)
        lineTo(headCenter.x + headR * .80f, headCenter.y + headR * .60f)
        lineTo(headCenter.x + headR * .28f, headCenter.y + headR)
        lineTo(headCenter.x - headR * .30f, headCenter.y + headR)
        lineTo(headCenter.x - headR * .82f, headCenter.y + headR * .58f)
        lineTo(headCenter.x - headR, headCenter.y - headR * .10f)
        lineTo(headCenter.x - headR * .68f, headCenter.y - headR * .72f)
        close()
    }
    drawPath(headOutline, outline)
    val head = Path().apply {
        moveTo(headCenter.x, headCenter.y - headR * .88f)
        lineTo(headCenter.x + headR * .58f, headCenter.y - headR * .62f)
        lineTo(headCenter.x + headR * .84f, headCenter.y - headR * .08f)
        lineTo(headCenter.x + headR * .66f, headCenter.y + headR * .55f)
        lineTo(headCenter.x + headR * .24f, headCenter.y + headR * .82f)
        lineTo(headCenter.x - headR * .30f, headCenter.y + headR * .82f)
        lineTo(headCenter.x - headR * .68f, headCenter.y + headR * .52f)
        lineTo(headCenter.x - headR * .84f, headCenter.y - headR * .08f)
        lineTo(headCenter.x - headR * .58f, headCenter.y - headR * .62f)
        close()
    }
    drawPath(head, c.skin)
    drawPath(Path().apply { moveTo(headCenter.x - headR * .84f, headCenter.y - headR * .08f); lineTo(headCenter.x - headR * .58f, headCenter.y - headR * .62f); lineTo(headCenter.x, headCenter.y - headR * .88f); lineTo(headCenter.x - headR * .30f, headCenter.y + headR * .82f); lineTo(headCenter.x - headR * .68f, headCenter.y + headR * .52f); close() }, c.skinLight.copy(alpha = .88f))
    drawPath(Path().apply { moveTo(headCenter.x, headCenter.y - headR * .88f); lineTo(headCenter.x + headR * .58f, headCenter.y - headR * .62f); lineTo(headCenter.x + headR * .84f, headCenter.y - headR * .08f); lineTo(headCenter.x + headR * .66f, headCenter.y + headR * .55f); lineTo(headCenter.x + headR * .24f, headCenter.y + headR * .82f); lineTo(headCenter.x, headCenter.y + headR * .18f); close() }, c.skinShade.copy(alpha = .76f))

    drawNpcHair(headCenter, headR, type, c, outline)
    drawNpcFace(headCenter, headR, c, facing)

    // Role props: waiter tray, chef hat, guest accessories.
    when (type) {
        NpcType.WAITER -> drawTray(q + Offset(scale * .72f, scale * .20f), scale * .28f, c.accent)
        NpcType.CHEF -> drawChefHat(headCenter, headR, c, outline)
        NpcType.GUEST_CLASSIC -> Unit
        NpcType.GUEST_CURLY -> drawRoundRect(c.accent, Offset(q.x - scale * .20f, q.y + scale * .52f), Size(scale * .40f, scale * .07f), CornerRadius(scale * .03f))
        NpcType.GUEST_CAP -> drawCap(headCenter, headR, c, outline)
    }
}

private fun DrawScope.drawNpcHair(center: Offset, r: Float, type: NpcType, c: NpcPalette, outline: Color) {
    when (type) {
        NpcType.WAITER -> {
            val path = Path().apply { moveTo(center.x - r * .88f, center.y - r * .15f); lineTo(center.x - r * .68f, center.y - r * .72f); lineTo(center.x, center.y - r * .95f); lineTo(center.x + r * .72f, center.y - r * .68f); lineTo(center.x + r * .88f, center.y - r * .16f); lineTo(center.x + r * .52f, center.y - r * .24f); lineTo(center.x + r * .20f, center.y - r * .06f); lineTo(center.x - r * .12f, center.y - r * .25f); lineTo(center.x - r * .45f, center.y - r * .06f); close() }
            drawPath(path, c.hair)
            drawPath(Path().apply { moveTo(center.x - r * .68f, center.y - r * .72f); lineTo(center.x, center.y - r * .95f); lineTo(center.x + r * .05f, center.y - r * .08f); lineTo(center.x - r * .45f, center.y - r * .06f); close() }, c.hairLight)
        }
        NpcType.CHEF -> Unit
        NpcType.GUEST_CLASSIC -> {
            drawRoundRect(c.hair, Offset(center.x - r * .88f, center.y - r * .25f), Size(r * 1.76f, r * .48f), CornerRadius(r * .12f))
            drawPath(Path().apply { moveTo(center.x - r * .72f, center.y - r * .32f); lineTo(center.x - r * .30f, center.y - r * .80f); lineTo(center.x + r * .38f, center.y - r * .72f); lineTo(center.x + r * .78f, center.y - r * .25f); close() }, c.hairLight)
        }
        NpcType.GUEST_CURLY -> {
            for (i in -4..4) for (j in -1..2) if ((i + j) % 2 == 0) drawCircle(c.hair, r * .22f, Offset(center.x + i * r * .18f, center.y - r * .55f + j * r * .13f))
            drawCircle(c.hairLight, r * .10f, Offset(center.x - r * .32f, center.y - r * .72f))
            drawCircle(c.hairLight, r * .10f, Offset(center.x + r * .30f, center.y - r * .65f))
        }
        NpcType.GUEST_CAP -> Unit
    }
}

private fun DrawScope.drawNpcFace(center: Offset, r: Float, c: NpcPalette, facing: Int) {
    val eyeY = center.y - r * .03f
    val eyeOffset = r * .29f
    drawRoundRect(Color(0xFFF8F4EA), Offset(center.x - eyeOffset - r * .17f, eyeY - r * .12f), Size(r * .30f, r * .24f), CornerRadius(r * .08f))
    drawRoundRect(Color(0xFFF8F4EA), Offset(center.x + eyeOffset - r * .13f, eyeY - r * .12f), Size(r * .30f, r * .24f), CornerRadius(r * .08f))
    drawCircle(c.hairShade, r * .075f, Offset(center.x - eyeOffset + facing * r * .04f, eyeY))
    drawCircle(c.hairShade, r * .075f, Offset(center.x + eyeOffset + facing * r * .04f, eyeY))
    drawCircle(Color.White.copy(alpha = .85f), r * .024f, Offset(center.x - eyeOffset + facing * r * .06f, eyeY - r * .025f))
    drawCircle(Color.White.copy(alpha = .85f), r * .024f, Offset(center.x + eyeOffset + facing * r * .06f, eyeY - r * .025f))
    drawLine(c.hairShade, Offset(center.x - eyeOffset - r * .11f, eyeY - r * .17f), Offset(center.x - eyeOffset + r * .10f, eyeY - r * .19f), r * .035f, StrokeCap.Round)
    drawLine(c.hairShade, Offset(center.x + eyeOffset - r * .10f, eyeY - r * .19f), Offset(center.x + eyeOffset + r * .11f, eyeY - r * .17f), r * .035f, StrokeCap.Round)
    drawLine(c.skinShade, Offset(center.x + facing * r * .05f, eyeY + r * .08f), Offset(center.x + facing * r * .10f, eyeY + r * .19f), r * .035f, StrokeCap.Round)
    drawArc(Color(0xFF9E4E50), 12f, 156f, false, Offset(center.x - r * .18f, eyeY + r * .18f), Size(r * .36f, r * .22f), style = Stroke(width = r * .045f))
    drawCircle(Color.Black.copy(alpha = .18f), r * .07f, Offset(center.x - r * .55f, center.y + r * .18f))
}

private fun DrawScope.drawChefHat(center: Offset, r: Float, c: NpcPalette, outline: Color) {
    drawRoundRect(outline, Offset(center.x - r * .50f, center.y - r * 1.05f), Size(r, r * .34f), CornerRadius(r * .08f))
    drawRoundRect(c.top, Offset(center.x - r * .45f, center.y - r * 1.02f), Size(r * .90f, r * .25f), CornerRadius(r * .07f))
    drawCircle(c.top, r * .25f, center + Offset(-r * .25f, -r * 1.02f))
    drawCircle(c.topLight, r * .23f, center + Offset(r * .10f, -r * 1.08f))
    drawCircle(c.topShade, r * .20f, center + Offset(r * .32f, -r * .96f))
}

private fun DrawScope.drawCap(center: Offset, r: Float, c: NpcPalette, outline: Color) {
    drawPath(Path().apply { moveTo(center.x - r * .72f, center.y - r * .48f); lineTo(center.x - r * .30f, center.y - r * .90f); lineTo(center.x + r * .52f, center.y - r * .68f); lineTo(center.x + r * .72f, center.y - r * .42f); close() }, outline)
    drawPath(Path().apply { moveTo(center.x - r * .62f, center.y - r * .50f); lineTo(center.x - r * .25f, center.y - r * .82f); lineTo(center.x + r * .44f, center.y - r * .62f); lineTo(center.x + r * .62f, center.y - r * .45f); close() }, c.hair)
    drawLine(c.hairLight, Offset(center.x - r * .25f, center.y - r * .72f), Offset(center.x + r * .35f, center.y - r * .57f), r * .06f, StrokeCap.Round)
}

private fun DrawScope.drawTray(p: Offset, r: Float, accent: Color) {
    drawOval(Color.Black.copy(alpha = .25f), Offset(p.x - r, p.y - r * .35f), Size(r * 2f, r * .70f))
    drawOval(accent, Offset(p.x - r * .82f, p.y - r * .30f), Size(r * 1.64f, r * .50f))
    drawCircle(Cream, r * .18f, p - Offset(r * .25f, r * .05f))
    drawCircle(Color(0xFFF0C47A), r * .13f, p + Offset(r * .24f, r * .02f))
}
