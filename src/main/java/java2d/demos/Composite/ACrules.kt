/*
 *
 * Copyright (c) 2007, 2011, Oracle and/or its affiliates. All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 *   - Redistributions of source code must retain the above copyright
 *     notice, this list of conditions and the following disclaimer.
 *
 *   - Redistributions in binary form must reproduce the above copyright
 *     notice, this list of conditions and the following disclaimer in the
 *     documentation and/or other materials provided with the distribution.
 *
 *   - Neither the name of Oracle nor the names of its
 *     contributors may be used to endorse or promote products derived
 *     from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS
 * IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO,
 * THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
 * PURPOSE ARE DISCLAIMED.  IN NO EVENT SHALL THE COPYRIGHT OWNER OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package java2d.demos.Composite

import java2d.AnimatingSurface
import java.awt.AlphaComposite.Clear
import java.awt.AlphaComposite.Dst
import java.awt.AlphaComposite.DstAtop
import java.awt.AlphaComposite.DstIn
import java.awt.AlphaComposite.DstOut
import java.awt.AlphaComposite.DstOver
import java.awt.AlphaComposite.Src
import java.awt.AlphaComposite.SrcAtop
import java.awt.AlphaComposite.SrcIn
import java.awt.AlphaComposite.SrcOut
import java.awt.AlphaComposite.SrcOver
import java.awt.AlphaComposite.Xor
import java.awt.Color
import java.awt.Font
import java.awt.GradientPaint
import java.awt.Graphics2D
import java.awt.RenderingHints
import java.awt.font.FontRenderContext
import java.awt.font.LineMetrics
import java.awt.font.TextLayout
import java.awt.geom.GeneralPath
import java.awt.image.BufferedImage

/**
 * All the AlphaCompositing Rules demonstrated.
 */
class ACrules : AnimatingSurface()
{
    private var fadeIndex: Int = 0
    private var srca = fadeValues[fadeIndex][0]
    private var dsta = fadeValues[fadeIndex][3]
    private var fadeLabel = fadeNames[0]
    private var statBI: BufferedImage? = null
    private var animBI: BufferedImage? = null
    private var PADLEFT: Int = 0
    private var PADRIGHT: Int = 0
    private var HPAD: Int = 0
    private var PADABOVE: Int = 0
    private var PADBELOW: Int = 0
    private var VPAD: Int = 0
    private var RECTWIDTH: Int = 0
    private var RECTHEIGHT: Int = 0
    private var PADDEDHEIGHT: Int = 0
    private val srcpath = GeneralPath()
    private val dstpath = GeneralPath()
    private var lm: LineMetrics? = null
    private var dBI: BufferedImage? = null
    private var sBI: BufferedImage? = null
    private var gradientDst: GradientPaint? = null
    private var gradientSrc: GradientPaint? = null

    init {
        background = Color.WHITE
    }

    override fun reset(newWidth: Int, newHeight: Int) {
        sleepAmount = 400
        val frc = FontRenderContext(null, false, false)
        lm = FONT.getLineMetrics(compNames[0], frc)

        PADLEFT = if (newWidth < 150) 10 else 15
        PADRIGHT = if (newWidth < 150) 10 else 15
        HPAD = PADLEFT + PADRIGHT
        PADBELOW = if (newHeight < 250) 1 else 2
        PADABOVE = PADBELOW + lm!!.height.toInt()
        VPAD = PADABOVE + PADBELOW
        RECTWIDTH = newWidth / 4 - HPAD
        RECTWIDTH = if (RECTWIDTH < 6) 6 else RECTWIDTH
        RECTHEIGHT = (newHeight - VPAD) / HALF_NUM_RULES - VPAD
        RECTHEIGHT = if (RECTHEIGHT < 6) 6 else RECTHEIGHT
        PADDEDHEIGHT = RECTHEIGHT + VPAD

        srcpath.reset()
        srcpath.moveTo(0f, 0f)
        srcpath.lineTo(RECTWIDTH.toFloat(), 0f)
        srcpath.lineTo(0f, RECTHEIGHT.toFloat())
        srcpath.closePath()

        dstpath.reset()
        dstpath.moveTo(0f, 0f)
        dstpath.lineTo(RECTWIDTH.toFloat(), RECTHEIGHT.toFloat())
        dstpath.lineTo(RECTWIDTH.toFloat(), 0f)
        dstpath.closePath()

        dBI = BufferedImage(RECTWIDTH, RECTHEIGHT, BufferedImage.TYPE_INT_ARGB)
        sBI = BufferedImage(RECTWIDTH, RECTHEIGHT, BufferedImage.TYPE_INT_ARGB)

        gradientDst = GradientPaint(
            0f, 0f,
            Color(1.0f, 0.0f, 0.0f, 1.0f),
            0f, RECTHEIGHT.toFloat(),
            Color(1.0f, 0.0f, 0.0f, 0.0f))

        gradientSrc = GradientPaint(
            0f, 0f,
            Color(0.0f, 0.0f, 1.0f, 1.0f),
            RECTWIDTH.toFloat(), 0f,
            Color(0.0f, 0.0f, 1.0f, 0.0f))

        statBI = BufferedImage(newWidth / 2, newHeight, BufferedImage.TYPE_INT_RGB)
        statBI = drawCompBI(statBI!!, true)
        animBI = BufferedImage(newWidth / 2, newHeight, BufferedImage.TYPE_INT_RGB)
    }

    override fun step(width: Int, height: Int) {
        if (sleepAmount == 5000L) {
            sleepAmount = 200
        }
        srca += fadeValues[fadeIndex][1]
        dsta += fadeValues[fadeIndex][4]
        fadeLabel = fadeNames[fadeIndex]
        if (srca < 0 || srca > 1.0 || dsta < 0 || dsta > 1.0) {
            sleepAmount = 5000
            srca = fadeValues[fadeIndex][2]
            dsta = fadeValues[fadeIndex][5]
            if (fadeIndex++ == fadeValues.size - 1) {
                fadeIndex = 0
            }
        }
    }

    override fun render(w: Int, h: Int, g2: Graphics2D) {
        if (statBI == null || animBI == null) {
            return
        }
        g2.drawImage(statBI, 0, 0, null)
        g2.drawImage(drawCompBI(animBI!!, false), w / 2, 0, null)

        g2.color = Color.black
        val frc = g2.fontRenderContext
        var tl = TextLayout("AC Rules", g2.font, frc)
        tl.draw(g2, 15.0f, tl.bounds.height.toFloat() + 3.0f)

        tl = TextLayout(fadeLabel, FONT, frc)
        var x = (w * 0.75 - tl.bounds.width / 2).toFloat()
        if (x + tl.bounds.width > w) {
            x = (w - tl.bounds.width).toFloat()
        }
        tl.draw(g2, x, tl.bounds.height.toFloat() + 3.0f)
    }

    private fun drawCompBI(bi: BufferedImage, doGradient: Boolean): BufferedImage {
        val big = bi.createGraphics()
        big.color = background
        big.fillRect(0, 0, bi.width, bi.height)
        big.setRenderingHint(RenderingHints.KEY_ANTIALIASING, antiAlias)
        big.font = FONT

        val gD = dBI!!.createGraphics()
        gD.setRenderingHint(RenderingHints.KEY_ANTIALIASING, antiAlias)
        val gS = sBI!!.createGraphics()
        gS.setRenderingHint(RenderingHints.KEY_ANTIALIASING, antiAlias)

        var x = 0
        var y = 0
        val yy = lm!!.height.toInt() + VPAD

        for (i in compNames.indices) {
            y = if (i == 0 || i == HALF_NUM_RULES) yy else y + PADDEDHEIGHT
            x = if (i >= HALF_NUM_RULES) bi.width / 2 + PADLEFT else PADLEFT
            big.translate(x, y)

            gD.composite = Clear
            gD.fillRect(0, 0, RECTWIDTH, RECTHEIGHT)
            gD.composite = Src
            if (doGradient) {
                gD.paint = gradientDst
                gD.fillRect(0, 0, RECTWIDTH, RECTHEIGHT)
            } else {
                gD.paint = Color(1.0f, 0.0f, 0.0f, dsta)
                gD.fill(dstpath)
            }

            gS.composite = Clear
            gS.fillRect(0, 0, RECTWIDTH, RECTHEIGHT)
            gS.composite = Src
            if (doGradient) {
                gS.paint = gradientSrc
                gS.fillRect(0, 0, RECTWIDTH, RECTHEIGHT)
            } else {
                gS.paint = Color(0.0f, 0.0f, 1.0f, srca)
                gS.fill(srcpath)
            }

            gD.composite = compObjs[i]
            gD.drawImage(sBI, 0, 0, null)

            big.drawImage(dBI, 0, 0, null)
            big.color = Color.black
            big.drawString(compNames[i], 0f, -lm!!.descent)
            big.drawRect(0, 0, RECTWIDTH, RECTHEIGHT)
            big.translate(-x, -y)
        }

        gD.dispose()
        gS.dispose()
        big.dispose()

        return bi
    }

    companion object
    {
        private val compNames = arrayOf(
            "Src",
            "SrcOver",
            "SrcIn",
            "SrcOut",
            "SrcAtop",
            "Clear",
            "Dst",
            "DstOver",
            "DstIn",
            "DstOut",
            "DstAtop",
            "Xor")

        private val compObjs =
            arrayOf(Src, SrcOver, SrcIn, SrcOut, SrcAtop, Clear, Dst, DstOver, DstIn, DstOut, DstAtop, Xor)

        private val NUM_RULES = compObjs.size
        private val HALF_NUM_RULES = NUM_RULES / 2

        private val fadeValues = arrayOf(
            floatArrayOf(1.0f, -0.1f, 0.0f, 1.0f, 0.0f, 1.0f),
            floatArrayOf(0.0f, 0.1f, 1.0f, 1.0f, -0.1f, 0.0f),
            floatArrayOf(1.0f, 0.0f, 1.0f, 0.0f, 0.1f, 1.0f))

        private val fadeNames = arrayOf(
            "Src => transparent, Dest opaque",
            "Src => opaque, Dest => transparent",
            "Src opaque, Dest => opaque")

        private val FONT = Font(Font.SERIF, Font.PLAIN, 10)

        @JvmStatic
        fun main(argv: Array<String>) {
            createDemoFrame(ACrules())
        }
    }
}
