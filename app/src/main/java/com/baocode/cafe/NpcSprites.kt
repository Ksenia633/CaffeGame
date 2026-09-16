package com.baocode.cafe

import android.os.Handler
import android.os.Looper
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import kotlin.math.max
import kotlin.math.sin

private enum class NpcType { WAITER, CHEF, GUEST_CLASSIC, GUEST_CURLY, GUEST_CAP }
private enum class NpcAnim { IDLE, WALK, TURN, TALK, RECEIVE_ORDER, CARRY_FOOD, SERVE, WAIT, HAPPY, SAD }

private data class NpcPalette(
    val skin: Color, val skinLight: Color, val skinShade: Color,
    val hair: Color, val hairLight: Color, val hairShade: Color,
    val top: Color, val topLight: Color, val topShade: Color,
    val bottom: Color, val bottomLight: Color, val shoes: Color, val shoesLight: Color, val accent: Color
)

private fun npcPalette(type: NpcType): NpcPalette = when (type) {
    NpcType.WAITER -> NpcPalette(Color(0xFFE0A27A), Color(0xFFF4C29C), Color(0xFFB86B55), Color(0xFF3A2522), Color(0xFF65413A), Color(0xFF211719), Color(0xFFF0E5D0), Color.White, Color(0xFFB7A994), Color(0xFF384B55), Color(0xFF536A73), Color(0xFF20272A), Color(0xFF596266), Neon)
    NpcType.CHEF -> NpcPalette(Color(0xFFC98562), Color(0xFFE4A57D), Color(0xFF995B4C), Color(0xFF302326), Color(0xFF593E43), Color(0xFF1C171A), Color(0xFFF5F0E7), Color.White, Color(0xFFC9C2B7), Color(0xFF5C3B32), Color(0xFF805447), Color(0xFF29272A), Color(0xFF626066), Color(0xFFE7B45B))
    NpcType.GUEST_CLASSIC -> NpcPalette(Color(0xFFFFC891), Color(0xFFFFD9AD), Color(0xFFD98762), Color(0xFF5B3028), Color(0xFF875043), Color(0xFF321D1D), Color(0xFFB9584C), Color(0xFFD97A6A), Color(0xFF823B38), Color(0xFF394A63), Color(0xFF536A84), Color(0xFF24282C), Color(0xFF555D61), Color(0xFFE8B45E))
    NpcType.GUEST_CURLY -> NpcPalette(Color(0xFF8C5A3D), Color(0xFFB97854), Color(0xFF633A30), Color(0xFF201C22), Color(0xFF473842), Color(0xFF131216), Color(0xFF7D5AA2), Color(0xFF9A78BE), Color(0xFF573B78), Color(0xFFE3B96B), Color(0xFFF0CF8C), Color(0xFF292A31), Color(0xFF62656D), Color(0xFFE67E9B))
    NpcType.GUEST_CAP -> NpcPalette(Color(0xFFE4B07C), Color(0xFFF5C99A), Color(0xFFB56E54), Color(0xFFB93E47), Color(0xFFD96767), Color(0xFF752A35), Color(0xFF4E8D78), Color(0xFF6BA992), Color(0xFF315F51), Color(0xFF39414C), Color(0xFF566170), Color(0xFF20262C), Color(0xFF5B636A), Color(0xFF55C7B0))
}

private object NpcAnimationClock {
    var tick by mutableLongStateOf(0L)
    private val handler = Handler(Looper.getMainLooper())
    private val pulse = object : Runnable {
        override fun run() {
            tick = System.nanoTime() / 1_000_000L
            handler.postDelayed(this, 33L)
        }
    }
    init { handler.post(pulse) }
}

private fun DrawScope.drawNpcSprite(p: Offset, scale: Float, type: NpcType, facing: Int = 1, bobPhase: Float = 0f, anim: NpcAnim = NpcAnim.IDLE) {
    val c = npcPalette(type)
    val outline = Color(0xFF171719)
    val phase = bobPhase + (NpcAnimationClock.tick % 4000L) / 1000f
    val bob = when (anim) {
        NpcAnim.IDLE, NpcAnim.WAIT -> sin(phase) * scale * .018f
        NpcAnim.WALK -> sin(phase * 5f) * scale * .035f
        NpcAnim.TURN -> sin(phase * 3f) * scale * .025f
        NpcAnim.TALK -> sin(phase * 4f) * scale * .028f
        NpcAnim.RECEIVE_ORDER, NpcAnim.CARRY_FOOD, NpcAnim.SERVE -> sin(phase * 3f) * scale * .022f
        NpcAnim.HAPPY -> sin(phase * 5f) * scale * .035f
        NpcAnim.SAD -> sin(phase * 2f) * scale * .012f
    }
    val q = p + Offset(0f, bob)
    val headR = scale * .60f
    val headCenter = q - Offset(0f, scale * .62f)
    val legY = q.y + scale * .50f
    val walk = sin(phase * 5f)
    val talk = sin(phase * 4f)
    val turn = sin(phase * 3f)
    val handLift = when (anim) {
        NpcAnim.RECEIVE_ORDER -> .10f
        NpcAnim.CARRY_FOOD -> .16f
        NpcAnim.SERVE -> .24f + .08f * sin(phase * 3f)
        NpcAnim.TALK -> .08f + .05f * talk
        else -> 0f
    }

    drawOval(Color.Black.copy(alpha = .32f), Offset(q.x - scale * .78f, q.y + scale * .58f), Size(scale * 1.56f, scale * .40f))
    drawRoundRect(outline, Offset(q.x - scale * .30f + walk * scale * .035f, legY), Size(scale * .22f, scale * .52f), CornerRadius(scale * .07f))
    drawRoundRect(outline, Offset(q.x + scale * .08f - walk * scale * .035f, legY), Size(scale * .22f, scale * .52f), CornerRadius(scale * .07f))
    drawRoundRect(c.bottom, Offset(q.x - scale * .26f + walk * scale * .035f, legY + scale * .02f), Size(scale * .14f, scale * .43f), CornerRadius(scale * .05f))
    drawRoundRect(c.bottomLight, Offset(q.x + scale * .12f - walk * scale * .035f, legY + scale * .02f), Size(scale * .14f, scale * .43f), CornerRadius(scale * .05f))
    drawRoundRect(outline, Offset(q.x - scale * .38f + walk * scale * .04f, legY + scale * .39f), Size(scale * .34f, scale * .18f), CornerRadius(scale * .08f))
    drawRoundRect(outline, Offset(q.x - scale * .00f - walk * scale * .04f, legY + scale * .39f), Size(scale * .34f, scale * .18f), CornerRadius(scale * .08f))
    drawRoundRect(c.shoes, Offset(q.x - scale * .34f + walk * scale * .04f, legY + scale * .42f), Size(scale * .27f, scale * .11f), CornerRadius(scale * .05f))
    drawRoundRect(c.shoesLight, Offset(q.x + scale * .04f - walk * scale * .04f, legY + scale * .42f), Size(scale * .27f, scale * .11f), CornerRadius(scale * .05f))

    val bodyTop = q.y
    val torso = Path().apply { moveTo(q.x-scale*.43f,bodyTop+scale*.06f); lineTo(q.x-scale*.30f,bodyTop-scale*.06f); lineTo(q.x+scale*.30f,bodyTop-scale*.06f); lineTo(q.x+scale*.43f,bodyTop+scale*.06f); lineTo(q.x+scale*.31f,bodyTop+scale*.58f); lineTo(q.x-scale*.31f,bodyTop+scale*.58f); close() }
    drawPath(torso, outline)
    drawPath(Path().apply { moveTo(q.x-scale*.36f,bodyTop+scale*.08f); lineTo(q.x-scale*.25f,bodyTop); lineTo(q.x+scale*.25f,bodyTop); lineTo(q.x+scale*.36f,bodyTop+scale*.08f); lineTo(q.x+scale*.25f,bodyTop+scale*.51f); lineTo(q.x-scale*.25f,bodyTop+scale*.51f); close() }, c.top)
    drawPath(Path().apply { moveTo(q.x-scale*.36f,bodyTop+scale*.08f); lineTo(q.x-scale*.25f,bodyTop); lineTo(q.x-scale*.25f,bodyTop+scale*.51f); close() }, c.topLight.copy(alpha=.82f))
    drawPath(Path().apply { moveTo(q.x+scale*.25f,bodyTop); lineTo(q.x+scale*.36f,bodyTop+scale*.08f); lineTo(q.x+scale*.25f,bodyTop+scale*.51f); close() }, c.topShade)

    when (type) {
        NpcType.WAITER -> { drawRoundRect(c.accent, Offset(q.x-scale*.065f,bodyTop+scale*.12f),Size(scale*.13f,scale*.42f),CornerRadius(scale*.025f)); drawLine(c.accent,Offset(q.x-scale*.30f,bodyTop+scale*.08f),Offset(q.x+scale*.30f,bodyTop+scale*.08f),scale*.055f,StrokeCap.Round); drawCircle(Color.White,scale*.026f,Offset(q.x,bodyTop+scale*.28f)); drawCircle(Color.White,scale*.026f,Offset(q.x,bodyTop+scale*.40f)) }
        NpcType.CHEF -> { drawRoundRect(c.accent,Offset(q.x-scale*.18f,bodyTop+scale*.10f),Size(scale*.36f,scale*.13f),CornerRadius(scale*.04f)); drawCircle(Color.White.copy(alpha=.90f),scale*.035f,Offset(q.x-scale*.11f,bodyTop+scale*.30f)); drawCircle(Color.White.copy(alpha=.90f),scale*.035f,Offset(q.x+scale*.11f,bodyTop+scale*.30f)) }
        NpcType.GUEST_CLASSIC -> drawRoundRect(c.accent,Offset(q.x-scale*.28f,bodyTop+scale*.10f),Size(scale*.56f,scale*.08f),CornerRadius(scale*.03f))
        NpcType.GUEST_CURLY -> { drawCircle(c.accent,scale*.055f,Offset(q.x+scale*.27f,bodyTop+scale*.27f)); drawCircle(c.accent.copy(alpha=.55f),scale*.10f,Offset(q.x,bodyTop+scale*.46f)) }
        NpcType.GUEST_CAP -> drawRoundRect(c.accent,Offset(q.x-scale*.23f,bodyTop+scale*.08f),Size(scale*.46f,scale*.13f),CornerRadius(scale*.05f))
    }

    // Jointed arms: shoulder -> elbow -> hand, animated by state.
    val leftElbow = Offset(q.x-scale*.49f, bodyTop+scale*(.30f-handLift*.35f) + talk*scale*.035f)
    val rightElbow = Offset(q.x+scale*.49f, bodyTop+scale*(.30f-handLift*.35f) - talk*scale*.035f)
    val leftHand = Offset(q.x-scale*.52f, bodyTop+scale*(.49f-handLift))
    val rightHand = Offset(q.x+scale*.52f, bodyTop+scale*(.49f-handLift))
    drawLine(outline,Offset(q.x-scale*.32f,bodyTop+scale*.12f),leftElbow,scale*.20f,StrokeCap.Round)
    drawLine(outline,leftElbow,leftHand,scale*.18f,StrokeCap.Round)
    drawLine(outline,Offset(q.x+scale*.32f,bodyTop+scale*.12f),rightElbow,scale*.20f,StrokeCap.Round)
    drawLine(outline,rightElbow,rightHand,scale*.18f,StrokeCap.Round)
    drawCircle(c.topLight,scale*.065f,leftElbow); drawCircle(c.topShade,scale*.065f,rightElbow)
    drawCircle(c.skin,scale*.075f,leftHand); drawCircle(c.skinShade,scale*.075f,rightHand)

    drawRoundRect(outline,Offset(q.x-scale*.14f,q.y-scale*.28f),Size(scale*.28f,scale*.22f),CornerRadius(scale*.07f))
    drawRoundRect(c.skinShade,Offset(q.x-scale*.09f,q.y-scale*.26f),Size(scale*.18f,scale*.18f),CornerRadius(scale*.05f))
    val hc=headCenter+Offset(turn*scale*.025f,0f)
    val head=Path().apply{moveTo(hc.x,hc.y-headR);lineTo(hc.x+headR*.68f,hc.y-headR*.72f);lineTo(hc.x+headR,hc.y-headR*.10f);lineTo(hc.x+headR*.80f,hc.y+headR*.60f);lineTo(hc.x+headR*.28f,hc.y+headR);lineTo(hc.x-headR*.30f,hc.y+headR);lineTo(hc.x-headR*.82f,hc.y+headR*.58f);lineTo(hc.x-headR,hc.y-headR*.10f);lineTo(hc.x-headR*.68f,hc.y-headR*.72f);close()}
    drawPath(head,outline); drawPath(head,c.skin)
    drawPath(Path().apply{moveTo(hc.x-headR*.84f,hc.y-headR*.08f);lineTo(hc.x-headR*.58f,hc.y-headR*.62f);lineTo(hc.x,hc.y-headR*.88f);lineTo(hc.x-headR*.30f,hc.y+headR*.82f);lineTo(hc.x-headR*.68f,hc.y+headR*.52f);close()},c.skinLight.copy(alpha=.88f))
    drawPath(Path().apply{moveTo(hc.x,hc.y-headR*.88f);lineTo(hc.x+headR*.58f,hc.y-headR*.62f);lineTo(hc.x+headR*.84f,hc.y-headR*.08f);lineTo(hc.x+headR*.66f,hc.y+headR*.55f);lineTo(hc.x+headR*.24f,hc.y+headR*.82f);lineTo(hc.x,hc.y+headR*.18f);close()},c.skinShade.copy(alpha=.76f))
    drawNpcHair(hc,headR,type,c,outline)
    drawNpcFace(hc,headR,c,facing,anim)

    when (type) {
        NpcType.WAITER -> { drawTray(q+Offset(scale*.72f,scale*(.20f-handLift*.45f)),scale*.28f,c.accent); if(anim==NpcAnim.SERVE) drawCircle(Color(0xFFF0C47A),scale*.13f,q+Offset(scale*.72f,scale*(.02f-handLift*.35f))) }
        NpcType.CHEF -> drawChefHat(hc,headR,c,outline)
        NpcType.GUEST_CLASSIC -> if(anim==NpcAnim.RECEIVE_ORDER||anim==NpcAnim.HAPPY) drawEmotion(q+Offset(scale*.68f,-scale*.82f),"♥",c.accent,scale*.22f)
        NpcType.GUEST_CURLY -> if(anim==NpcAnim.TALK) drawEmotion(q+Offset(scale*.68f,-scale*.82f),"…",c.accent,scale*.22f)
        NpcType.GUEST_CAP -> { drawCap(hc,headR,c,outline); if(anim==NpcAnim.SAD) drawEmotion(q+Offset(scale*.68f,-scale*.82f),"!",c.accent,scale*.22f) }
    }
}

private fun DrawScope.drawNpcHair(center: Offset,r:Float,type:NpcType,c:NpcPalette,outline:Color){
    when(type){
        NpcType.WAITER->{val path=Path().apply{moveTo(center.x-r*.88f,center.y-r*.15f);lineTo(center.x-r*.68f,center.y-r*.72f);lineTo(center.x,center.y-r*.95f);lineTo(center.x+r*.72f,center.y-r*.68f);lineTo(center.x+r*.88f,center.y-r*.16f);lineTo(center.x+r*.52f,center.y-r*.24f);lineTo(center.x+r*.20f,center.y-r*.06f);lineTo(center.x-r*.12f,center.y-r*.25f);lineTo(center.x-r*.45f,center.y-r*.06f);close()};drawPath(path,c.hair);drawPath(Path().apply{moveTo(center.x-r*.68f,center.y-r*.72f);lineTo(center.x,center.y-r*.95f);lineTo(center.x+r*.05f,center.y-r*.08f);lineTo(center.x-r*.45f,center.y-r*.06f);close()},c.hairLight)}
        NpcType.CHEF->Unit
        NpcType.GUEST_CLASSIC->{drawRoundRect(c.hair,Offset(center.x-r*.88f,center.y-r*.25f),Size(r*1.76f,r*.48f),CornerRadius(r*.12f));drawPath(Path().apply{moveTo(center.x-r*.72f,center.y-r*.32f);lineTo(center.x-r*.30f,center.y-r*.80f);lineTo(center.x+r*.38f,center.y-r*.72f);lineTo(center.x+r*.78f,center.y-r*.25f);close()},c.hairLight)}
        NpcType.GUEST_CURLY->{for(i in -4..4)for(j in -1..2)if((i+j)%2==0)drawCircle(c.hair,r*.22f,Offset(center.x+i*r*.18f,center.y-r*.55f+j*r*.13f));drawCircle(c.hairLight,r*.10f,Offset(center.x-r*.32f,center.y-r*.72f));drawCircle(c.hairLight,r*.10f,Offset(center.x+r*.30f,center.y-r*.65f))}
        NpcType.GUEST_CAP->Unit
    }
}

private fun DrawScope.drawNpcFace(center:Offset,r:Float,c:NpcPalette,facing:Int,anim:NpcAnim){
    val eyeY=center.y-r*.03f; val eyeOffset=r*.29f; val blink=if((NpcAnimationClock.tick/120L)%19L==0L) .035f else .24f
    drawRoundRect(Color(0xFFF8F4EA),Offset(center.x-eyeOffset-r*.17f,eyeY-r*.12f),Size(r*.30f,blink),CornerRadius(r*.08f));drawRoundRect(Color(0xFFF8F4EA),Offset(center.x+eyeOffset-r*.13f,eyeY-r*.12f),Size(r*.30f,blink),CornerRadius(r*.08f))
    if(blink>.1f){drawCircle(c.hairShade,r*.075f,Offset(center.x-eyeOffset+facing*r*.04f,eyeY));drawCircle(c.hairShade,r*.075f,Offset(center.x+eyeOffset+facing*r*.04f,eyeY));drawCircle(Color.White.copy(alpha=.85f),r*.024f,Offset(center.x-eyeOffset+facing*r*.06f,eyeY-r*.025f));drawCircle(Color.White.copy(alpha=.85f),r*.024f,Offset(center.x+eyeOffset+facing*r*.06f,eyeY-r*.025f))}
    val browDrop=if(anim==NpcAnim.SAD) r*.10f else -r*.19f
    drawLine(c.hairShade,Offset(center.x-eyeOffset-r*.11f,eyeY+browDrop),Offset(center.x-eyeOffset+r*.10f,eyeY+browDrop-r*.02f),r*.035f,StrokeCap.Round)
    drawLine(c.hairShade,Offset(center.x+eyeOffset-r*.10f,eyeY+browDrop-r*.02f),Offset(center.x+eyeOffset+r*.11f,eyeY+browDrop),r*.035f,StrokeCap.Round)
    drawLine(c.skinShade,Offset(center.x+facing*r*.05f,eyeY+r*.08f),Offset(center.x+facing*r*.10f,eyeY+r*.19f),r*.035f,StrokeCap.Round)
    val smile=if(anim==NpcAnim.SAD) -1f else 1f
    drawArc(Color(0xFF9E4E50),if(smile>0)12f else 192f,156f,false,Offset(center.x-r*.18f,eyeY+r*.18f),Size(r*.36f,r*.22f),style=Stroke(width=r*.045f))
}

private fun DrawScope.drawChefHat(center:Offset,r:Float,c:NpcPalette,outline:Color){drawRoundRect(outline,Offset(center.x-r*.50f,center.y-r*1.05f),Size(r,r*.34f),CornerRadius(r*.08f));drawRoundRect(c.top,Offset(center.x-r*.45f,center.y-r*1.02f),Size(r*.90f,r*.25f),CornerRadius(r*.07f));drawCircle(c.top,r*.25f,center+Offset(-r*.25f,-r*1.02f));drawCircle(c.topLight,r*.23f,center+Offset(r*.10f,-r*1.08f));drawCircle(c.topShade,r*.20f,center+Offset(r*.32f,-r*.96f))}
private fun DrawScope.drawCap(center:Offset,r:Float,c:NpcPalette,outline:Color){drawPath(Path().apply{moveTo(center.x-r*.72f,center.y-r*.48f);lineTo(center.x-r*.30f,center.y-r*.90f);lineTo(center.x+r*.52f,center.y-r*.68f);lineTo(center.x+r*.72f,center.y-r*.42f);close()},outline);drawPath(Path().apply{moveTo(center.x-r*.62f,center.y-r*.50f);lineTo(center.x-r*.25f,center.y-r*.82f);lineTo(center.x+r*.44f,center.y-r*.62f);lineTo(center.x+r*.62f,center.y-r*.45f);close()},c.hair);drawLine(c.hairLight,Offset(center.x-r*.25f,center.y-r*.72f),Offset(center.x+r*.35f,center.y-r*.57f),r*.06f,StrokeCap.Round)}
private fun DrawScope.drawTray(p:Offset,r:Float,accent:Color){drawOval(Color.Black.copy(alpha=.25f),Offset(p.x-r,p.y-r*.35f),Size(r*2f,r*.70f));drawOval(accent,Offset(p.x-r*.82f,p.y-r*.30f),Size(r*1.64f,r*.50f));drawCircle(Cream,r*.18f,p-Offset(r*.25f,r*.05f));drawCircle(Color(0xFFF0C47A),r*.13f,p+Offset(r*.24f,r*.02f))}
private fun DrawScope.drawEmotion(p:Offset,text:String,color:Color,size:Float){drawCircle(Color(0xFF071617).copy(alpha=.86f),size, p);drawCircle(color.copy(alpha=.18f),size*.82f,p);drawRoundRect(color,Offset(p.x-size*.08f,p.y-size*.42f),Size(size*.16f,size*.56f),CornerRadius(size*.05f));drawCircle(color,size*.10f,Offset(p.x,p.y+size*.27f))}

// Stage 4: animated cast. The clock is a Compose snapshot state, so the Canvas redraws at ~30 FPS.
private fun DrawScope.drawCafeNpcCast(center:(Float,Float)->Offset,w:Float){
    val t=NpcAnimationClock.tick/1000f
    drawNpcSprite(center(7.2f,5.2f),w*.19f,NpcType.WAITER,1,t,NpcAnim.WALK)
    drawNpcSprite(center(2.8f,2.7f),w*.19f,NpcType.CHEF,1,t*.7f,NpcAnim.WAIT)
    drawNpcSprite(center(9f,2f),w*.20f,NpcType.GUEST_CLASSIC,-1,t*.8f,NpcAnim.RECEIVE_ORDER)
    drawNpcSprite(center(11f,5f),w*.19f,NpcType.GUEST_CURLY,-1,t*1.1f,NpcAnim.TALK)
    drawNpcSprite(center(6f,7f),w*.19f,NpcType.GUEST_CAP,1,t*.9f,NpcAnim.HAPPY)
}
