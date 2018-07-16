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
package java2d.demos.Colors

import java2d.Surface
import java.awt.Color
import java.awt.Color.black
import java.awt.Color.blue
import java.awt.Color.cyan
import java.awt.Color.green
import java.awt.Color.magenta
import java.awt.Color.orange
import java.awt.Color.pink
import java.awt.Color.red
import java.awt.Color.yellow
import java.awt.Font
import java.awt.Graphics2D
import java.awt.Image
import java.awt.color.ColorSpace
import java.awt.font.TextLayout
import java.awt.image.BufferedImage
import java.awt.image.ColorConvertOp

/**
 * ColorConvertOp a ColorSpace.TYPE_RGB BufferedImage to a ColorSpace.CS_GRAY
 * BufferedImage.
 */
class ColorConvert : Surface()
{
    private val image: Image = getImage("clouds.jpg")

    init {
        background = Color.WHITE
    }

    override fun render(w: Int, h: Int, g2: Graphics2D) {
        val iw = image.getWidth(this)
        val ih = image.getHeight(this)
        val frc = g2.fontRenderContext
        val font = g2.font
        g2.color = black
        var tl = TextLayout("ColorConvertOp RGB->GRAY", font, frc)
        tl.draw(g2,
                (w / 2 - tl.bounds.width / 2).toFloat(),
                tl.ascent + tl.leading)

        val srcImg = BufferedImage(iw, ih, BufferedImage.TYPE_INT_RGB)
        val srcG = srcImg.createGraphics()
        val rhs = g2.renderingHints
        srcG.setRenderingHints(rhs)
        srcG.drawImage(image, 0, 0, null)

        val s = "JavaColor"
        val f = Font("serif", Font.BOLD, iw / 6)
        tl = TextLayout(s, f, frc)
        val tlb = tl.bounds
        val chars = s.toCharArray()
        var charWidth = 0.0f
        val rw = iw / chars.size
        val rh = ih / chars.size
        for (i in chars.indices) {
            tl = TextLayout(chars[i].toString(), f, frc)
            val shape = tl.getOutline(null)
            srcG.color = colors[i % colors.size]
            tl.draw(srcG,
                    (iw / 2 - tlb.width / 2 + charWidth).toFloat(),
                    (ih / 2 + tlb.height / 2).toFloat())
            charWidth += shape.bounds.getWidth().toFloat()
            srcG.fillRect(i * rw, ih - rh, rw, rh)
            srcG.color = colors[colors.size - 1 - i % colors.size]
            srcG.fillRect(i * rw, 0, rw, rh)
        }

        val cs = ColorSpace.getInstance(ColorSpace.CS_GRAY)
        val theOp = ColorConvertOp(cs, rhs)

        val dstImg = BufferedImage(iw, ih, BufferedImage.TYPE_INT_RGB)
        theOp.filter(srcImg, dstImg)

        g2.drawImage(srcImg, 10, 20, w / 2 - 20, h - 30, null)
        g2.drawImage(dstImg, w / 2 + 10, 20, w / 2 - 20, h - 30, null)
    }

    companion object
    {
        private val colors = arrayOf(red, pink, orange, yellow, green, magenta, cyan, blue)

        @JvmStatic
        fun main(s: Array<String>) {
            createDemoFrame(ColorConvert())
        }
    }
}
