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
package java2d.demos.Paint

import java2d.Surface
import java2d.use
import java.awt.BasicStroke
import java.awt.Color.BLACK
import java.awt.Color.GRAY
import java.awt.Color.GREEN
import java.awt.Color.LIGHT_GRAY
import java.awt.Color.WHITE
import java.awt.Font
import java.awt.GradientPaint
import java.awt.Graphics2D
import java.awt.Rectangle
import java.awt.TexturePaint
import java.awt.font.TextLayout
import java.awt.geom.AffineTransform
import java.awt.geom.GeneralPath
import java.awt.image.BufferedImage

/**
 * TexturePaint of gradient, buffered image and shapes.
 */
class Texture : Surface()
{
    init {
        background = WHITE
    }

    override fun render(w: Int, h: Int, g2: Graphics2D) {
        val r = Rectangle(10, 10, w - 20, h / 2 - 20)
        g2.paint = gradient
        g2.fill(r)
        g2.paint = GREEN
        g2.stroke = BasicStroke(20f)
        g2.draw(r)
        g2.paint = blacklines
        g2.stroke = BasicStroke(15f)
        g2.draw(r)

        var f = Font("Times New Roman", Font.BOLD, w / 5)
        var tl = TextLayout("Texture", f, g2.fontRenderContext)
        var sw = tl.bounds.width.toInt()
        var sh = tl.bounds.height.toInt()
        var sha = tl.getOutline(AffineTransform.getTranslateInstance((w / 2 - sw / 2).toDouble(), h * .25 + sh / 2))
        g2.color = BLACK
        g2.stroke = BasicStroke(3f)
        g2.draw(sha)
        g2.paint = greendots
        g2.fill(sha)

        r.setLocation(10, h / 2 + 10)
        g2.paint = triangles
        g2.fill(r)
        g2.paint = blacklines
        g2.stroke = BasicStroke(20f)
        g2.draw(r)
        g2.paint = GREEN
        g2.stroke = BasicStroke(4f)
        g2.draw(r)

        f = Font("serif", Font.BOLD, w / 4)
        tl = TextLayout("Paint", f, g2.fontRenderContext)
        sw = tl.bounds.width.toInt()
        sh = tl.bounds.height.toInt()
        sha = tl.getOutline(AffineTransform.getTranslateInstance((w / 2 - sw / 2).toDouble(), h * .75 + sh / 2))
        g2.color = BLACK
        g2.stroke = BasicStroke(5f)
        g2.draw(sha)
        g2.paint = bluedots
        g2.fill(sha)
    }

    companion object
    {
        private val bluedots: TexturePaint = run {
            val image = BufferedImage(2, 2, BufferedImage.TYPE_INT_RGB).apply {
                setRGB(0, 0, 0xFFFFFFFF.toInt())
                setRGB(1, 0, 0xFFFFFFFF.toInt())
                setRGB(0, 1, 0xFFFFFFFF.toInt())
                setRGB(1, 1, 0xFF0000FF.toInt())
            }
            TexturePaint(image, Rectangle(0, 0, 2, 2))
        }

        private val greendots: TexturePaint = run {
            val image = BufferedImage(2, 2, BufferedImage.TYPE_INT_RGB).apply {
                setRGB(0, 0, 0xFFFFFFFF.toInt())
                setRGB(1, 0, 0xFFFFFFFF.toInt())
                setRGB(0, 1, 0xFFFFFFFF.toInt())
                setRGB(1, 1, 0xFF00FF00.toInt())
            }
            TexturePaint(image, Rectangle(0, 0, 2, 2))
        }

        private val triangles: TexturePaint = run {
            val image = BufferedImage(10, 10, BufferedImage.TYPE_INT_RGB)
            image.createGraphics().use { gfx ->
                gfx.background = WHITE
                gfx.clearRect(0, 0, 10, 10)
                val p1 = GeneralPath().apply {
                    moveTo(0f, 0f)
                    lineTo(5f, 10f)
                    lineTo(10f, 0f)
                    closePath()
                }
                gfx.color = LIGHT_GRAY
                gfx.fill(p1)
            }
            TexturePaint(image, Rectangle(0, 0, 10, 10))
        }

        private val blacklines: TexturePaint = run {
            val image = BufferedImage(5, 5, BufferedImage.TYPE_INT_RGB)
            image.createGraphics().use { gfx ->
                gfx.color = BLACK
                gfx.fillRect(0, 0, 5, 5)
                gfx.color = GRAY
                gfx.fillRect(1, 1, 4, 4)
            }
            TexturePaint(image, Rectangle(0, 0, 5, 5))
        }

        private val gradient: TexturePaint = run {
            val w = 30
            val h = 30
            val image = BufferedImage(w, h, BufferedImage.TYPE_INT_RGB)
            image.createGraphics().use { gfx ->
                val oc = WHITE
                val ic = LIGHT_GRAY
                gfx.paint = GradientPaint(0f, 0f, oc, w * 0.35f, h * 0.35f, ic)
                gfx.fillRect(0, 0, w / 2, h / 2)
                gfx.paint = GradientPaint(w.toFloat(), 0f, oc, w * 0.65f, h * 0.35f, ic)
                gfx.fillRect(w / 2, 0, w / 2, h / 2)
                gfx.paint = GradientPaint(0f, h.toFloat(), oc, w * 0.35f, h * 0.65f, ic)
                gfx.fillRect(0, h / 2, w / 2, h / 2)
                gfx.paint = GradientPaint(w.toFloat(), h.toFloat(), oc, w * 0.65f, h * 0.65f, ic)
                gfx.fillRect(w / 2, h / 2, w / 2, h / 2)
            }
            TexturePaint(image, Rectangle(0, 0, w, h))
        }

        @JvmStatic
        fun main(s: Array<String>) {
            createDemoFrame(Texture())
        }
    }
}
