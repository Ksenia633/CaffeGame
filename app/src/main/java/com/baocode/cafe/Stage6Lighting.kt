package com.baocode.cafe

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import kotlin.math.cos
import kotlin.math.sin

/** Shared isometric camera used by the complete cafe scene. */
data class IsoCamera7(
    val zoom: Float = 1f,
    val panX: Float = 0f,
    val panY: Float = 0f,
    val rotation: Float = 0f
) {
    fun project(c: Float, r: Float, tileW: Float, tileH: Float, origin: Offset): Offset {
        val dx = c - r
        val dy = c + r
        val a = Math.toRadians(rotation.toDouble()).toFloat()
        val rx = dx * cos(a) - dy * sin(a)
        val ry = dx * sin(a) + dy * cos(a)
        return Offset(
            origin.x + panX + rx * tileW * .5f * zoom,
            origin.y + panY + ry * tileH * .5f * zoom
        )
    }
}

fun DrawScope.drawStage7IsoBackdrop() {
    drawRect(Color(0xFF020608))
    drawRect(Color(0xFF101B20).copy(alpha = .20f))
}

fun DrawScope.drawStage7RoomFrame(
    camera: IsoCamera7,
    origin: Offset,
    tileW: Float,
    tileH: Float,
    width: Int,
    height: Int
) {
    val corners = listOf(
        camera.project(0f, 0f, tileW, tileH, origin),
        camera.project(width.toFloat(), 0f, tileW, tileH, origin),
        camera.project(width.toFloat(), height.toFloat(), tileW, tileH, origin),
        camera.project(0f, height.toFloat(), tileW, tileH, origin)
    )
    for (i in corners.indices) {
        val a = corners[i]
        val b = corners[(i + 1) % corners.size]
        drawLine(Color(0xFF8D6249).copy(alpha = .26f), a, b, tileW * .035f)
        drawLine(Color(0xFF241A18).copy(alpha = .75f), a + Offset(0f, tileH * .06f), b + Offset(0f, tileH * .06f), tileW * .018f)
    }
}
