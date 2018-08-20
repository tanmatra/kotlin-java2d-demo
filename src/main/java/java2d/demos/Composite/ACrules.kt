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
import java2d.use
import java.awt.AlphaComposite
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
import java.awt.geom.Path2D
import java.awt.image.BufferedImage

/**
 * All the AlphaCompositing Rules demonstrated.
 */
class ACrules : AnimatingSurface()
{
    private var fadeIndex: Int = 0
    private var srcAlpha = FADES[fadeIndex].srcStart
    private var dstAlpha = FADES[fadeIndex].dstStart
    private var fadeLabel = FADES[fadeIndex].label
    private lateinit var staticBufImg: BufferedImage
    private lateinit var animatedBufImg: BufferedImage
    private var padLeft: Int = 0
    private var padRight: Int = 0
    private var horizontalPad: Int = 0
    private var padAbove: Int = 0
    private var padBelow: Int = 0
    private var verticalPad: Int = 0
    private var rectWidth: Int = 0
    private var rectHeight: Int = 0
    private var paddedHeight: Int = 0
    private val srcPath = GeneralPath()
    private val dstPath = GeneralPath()
    private lateinit var lineMetrics: LineMetrics
    private lateinit var dstBufImg: BufferedImage
    private lateinit var srcBufImg: BufferedImage
    private lateinit var srcGradient: GradientPaint
    private lateinit var dstGradient: GradientPaint

    init {
        background = Color.WHITE
    }

    override fun reset(newWidth: Int, newHeight: Int) {
        sleepAmount = 400

        val frc = FontRenderContext(null, false, false)
        lineMetrics = FONT.getLineMetrics(COMPOSITE_NAMES[0], frc)

        padLeft = if (newWidth < 150) 10 else 15
        padRight = if (newWidth < 150) 10 else 15
        horizontalPad = padLeft + padRight

        padBelow = if (newHeight < 250) 1 else 2
        padAbove = padBelow + lineMetrics.height.toInt()
        verticalPad = padAbove + padBelow

        rectWidth = (newWidth / 4 - horizontalPad).coerceAtLeast(6)
        rectHeight = ((newHeight - verticalPad) / HALF_NUM_RULES - verticalPad).coerceAtLeast(6)

        paddedHeight = rectHeight + verticalPad

        with(srcPath) {
            reset()
            moveTo(0f, 0f)
            lineTo(rectWidth.toFloat(), 0f)
            lineTo(0f, rectHeight.toFloat())
            closePath()
        }

        with(dstPath) {
            reset()
            moveTo(0f, 0f)
            lineTo(rectWidth.toFloat(), rectHeight.toFloat())
            lineTo(rectWidth.toFloat(), 0f)
            closePath()
        }

        dstBufImg = BufferedImage(rectWidth, rectHeight, BufferedImage.TYPE_INT_ARGB)
        srcBufImg = BufferedImage(rectWidth, rectHeight, BufferedImage.TYPE_INT_ARGB)

        dstGradient = GradientPaint(
            0f, 0f,
            Color(1.0f, 0.0f, 0.0f, 1.0f),
            0f, rectHeight.toFloat(),
            Color(1.0f, 0.0f, 0.0f, 0.0f))

        srcGradient = GradientPaint(
            0f, 0f,
            Color(0.0f, 0.0f, 1.0f, 1.0f),
            rectWidth.toFloat(), 0f,
            Color(0.0f, 0.0f, 1.0f, 0.0f))

        staticBufImg = BufferedImage(newWidth / 2, newHeight, BufferedImage.TYPE_INT_RGB)
        drawComposites(staticBufImg, doGradient = true)
        animatedBufImg = BufferedImage(newWidth / 2, newHeight, BufferedImage.TYPE_INT_RGB)
    }

    override fun step(width: Int, height: Int) {
        if (sleepAmount == 5000L) {
            sleepAmount = 200
        }
        val fade = FADES[fadeIndex]
        srcAlpha += fade.srcChange
        dstAlpha += fade.dstChange
        fadeLabel = fade.label
        if (srcAlpha < 0 || srcAlpha > 1.0 || dstAlpha < 0 || dstAlpha > 1.0) {
            sleepAmount = 5000
            srcAlpha = fade.srcEnd
            dstAlpha = fade.dstEnd
            if (fadeIndex++ == FADES.lastIndex) {
                fadeIndex = 0
            }
        }
    }

    override fun render(w: Int, h: Int, g2: Graphics2D) {
        g2.drawImage(staticBufImg, 0, 0, null)
        drawComposites(animatedBufImg, doGradient = false)
        g2.drawImage(animatedBufImg, w / 2, 0, null)

        g2.color = Color.BLACK
        val frc = g2.fontRenderContext
        val tl = TextLayout("AC Rules", g2.font, frc)
        tl.draw(g2, 15.0f, tl.bounds.height.toFloat() + 3.0f)

        val tl2 = TextLayout(fadeLabel, FONT, frc)
        var x = (w * 0.75 - tl2.bounds.width / 2).toFloat()
        if (x + tl2.bounds.width > w) {
            x = (w - tl2.bounds.width).toFloat()
        }
        tl2.draw(g2, x, tl2.bounds.height.toFloat() + 3.0f)
    }

    private fun drawComposites(bufImg: BufferedImage, doGradient: Boolean) {
        bufImg.createGraphics().use { gr ->
            gr.color = background
            gr.fillRect(0, 0, bufImg.width, bufImg.height)
            gr.setRenderingHint(RenderingHints.KEY_ANTIALIASING, antiAlias)
            gr.font = FONT

            dstBufImg.createGraphics().use { dstGr ->
                dstGr.setRenderingHint(RenderingHints.KEY_ANTIALIASING, antiAlias)
                srcBufImg.createGraphics().use { srcGr ->
                    srcGr.setRenderingHint(RenderingHints.KEY_ANTIALIASING, antiAlias)

                    var y = 0
                    val yy = lineMetrics.height.toInt() + verticalPad

                    val dstColor = Color(1.0f, 0.0f, 0.0f, dstAlpha)
                    val srcColor = Color(0.0f, 0.0f, 1.0f, srcAlpha)

                    for (i in COMPOSITE_NAMES.indices) {
                        y = if (i == 0 || i == HALF_NUM_RULES) yy else y + paddedHeight
                        val x = if (i >= HALF_NUM_RULES) bufImg.width / 2 + padLeft else padLeft
                        gr.translate(x, y)

                        drawElement(dstGr, doGradient, dstGradient, dstColor, dstPath)
                        drawElement(srcGr, doGradient, srcGradient, srcColor, srcPath)

                        dstGr.composite = COMPOSITES[i]
                        dstGr.drawImage(srcBufImg, 0, 0, null)

                        gr.drawImage(dstBufImg, 0, 0, null)
                        gr.color = Color.black
                        gr.drawString(COMPOSITE_NAMES[i], 0f, -lineMetrics.descent)
                        gr.drawRect(0, 0, rectWidth, rectHeight)
                        gr.translate(-x, -y)
                    }
                }
            }
        }
    }

    private fun drawElement(
        gr: Graphics2D,
        doGradient: Boolean,
        gradient: GradientPaint,
        color: Color,
        path: Path2D)
    {
        with(gr) {
            composite = AlphaComposite.Clear
            fillRect(0, 0, rectWidth, rectHeight)
            composite = AlphaComposite.Src
            if (doGradient) {
                paint = gradient
                fillRect(0, 0, rectWidth, rectHeight)
            } else {
                paint = color
                fill(path)
            }
        }
    }

    internal class Fade(
        val label: String,
        val srcStart: Float,
        val srcChange: Float,
        val srcEnd: Float,
        val dstStart: Float,
        val dstChange: Float,
        val dstEnd: Float)

    companion object
    {
        private val COMPOSITE_NAMES = arrayOf(
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

        private val COMPOSITES =
            arrayOf(Src, SrcOver, SrcIn, SrcOut, SrcAtop, Clear, Dst, DstOver, DstIn, DstOut, DstAtop, Xor)

        private val NUM_RULES = COMPOSITES.size
        private val HALF_NUM_RULES = NUM_RULES / 2

        private val FADES = arrayOf(
            Fade("Src => transparent, Dest opaque",     1.0f, -0.1f, 0.0f,  1.0f,  0.0f, 1.0f),
            Fade("Src => opaque, Dest => transparent",  0.0f,  0.1f, 1.0f,  1.0f, -0.1f, 0.0f),
            Fade("Src opaque, Dest => opaque",          1.0f,  0.0f, 1.0f,  0.0f,  0.1f, 1.0f))

        private val FONT = Font(Font.SERIF, Font.PLAIN, 10)

        @JvmStatic
        fun main(argv: Array<String>) {
            createDemoFrame(ACrules())
        }
    }
}
