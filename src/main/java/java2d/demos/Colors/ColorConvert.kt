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
import java2d.use
import java.awt.Color
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
        val imageWidth = image.getWidth(this)
        val imageHeight = image.getHeight(this)
        g2.color = Color.BLACK
        val titleTextLayout = TextLayout(TITLE, g2.font, g2.fontRenderContext)
        titleTextLayout.draw(g2,
                             (w / 2 - titleTextLayout.bounds.width / 2).toFloat(),
                             titleTextLayout.ascent + titleTextLayout.leading)

        val hints = g2.renderingHints
        val srcImg = BufferedImage(imageWidth, imageHeight, BufferedImage.TYPE_INT_RGB)
        srcImg.createGraphics().use { srcGr ->
            srcGr.setRenderingHints(hints)
            srcGr.drawImage(image, 0, 0, null)

            val textFont = Font(Font.SERIF, Font.BOLD, imageWidth / 6)
            val textLayout = TextLayout(TEXT, textFont, g2.fontRenderContext)
            val textBounds = textLayout.bounds
            var charX = 0.0f
            val boxWidth = imageWidth / TEXT.length
            val boxHeight = imageHeight / TEXT.length
            for ((i, char) in TEXT.withIndex()) {
                val singleCharLayout = TextLayout(char.toString(), textFont, g2.fontRenderContext)
                val shape = singleCharLayout.getOutline(null)
                srcGr.color = COLORS[i % COLORS.size]
                singleCharLayout.draw(srcGr,
                                      (imageWidth / 2 - textBounds.width / 2 + charX).toFloat(),
                                      (imageHeight / 2 + textBounds.height / 2).toFloat())
                charX += shape.bounds.getWidth().toFloat()
                srcGr.fillRect(i * boxWidth, imageHeight - boxHeight, boxWidth, boxHeight)
                srcGr.color = COLORS[COLORS.size - 1 - i % COLORS.size]
                srcGr.fillRect(i * boxWidth, 0, boxWidth, boxHeight)
            }
        }

        val operation = ColorConvertOp(ColorSpace.getInstance(ColorSpace.CS_GRAY), hints)
        val dstImg = BufferedImage(imageWidth, imageHeight, BufferedImage.TYPE_INT_RGB)
        operation.filter(srcImg, dstImg)

        g2.drawImage(srcImg, 10, 20, w / 2 - 20, h - 30, null)
        g2.drawImage(dstImg, w / 2 + 10, 20, w / 2 - 20, h - 30, null)
    }

    companion object
    {
        private const val TITLE = "ColorConvertOp RGB->GRAY"
        private const val TEXT = "JavaColor"

        private val COLORS = arrayOf(Color.RED, Color.PINK, Color.ORANGE, Color.YELLOW, Color.GREEN,
                                     Color.MAGENTA, Color.CYAN, Color.BLUE)

        @JvmStatic
        fun main(s: Array<String>) {
            createDemoFrame(ColorConvert())
        }
    }
}
