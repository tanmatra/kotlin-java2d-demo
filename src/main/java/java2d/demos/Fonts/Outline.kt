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
package java2d.demos.Fonts

import java2d.Surface
import java.awt.BasicStroke
import java.awt.Color
import java.awt.Font
import java.awt.Graphics2D
import java.awt.font.TextAttribute
import java.awt.font.TextLayout
import java.awt.geom.AffineTransform
import java.text.AttributedString

/**
 * Rendering text as an outline shape.
 */
class Outline : Surface()
{
    init {
        background = Color.WHITE
    }

    override fun render(w: Int, h: Int, g2: Graphics2D) {
        val frc = g2.fontRenderContext
        var f = Font(Font.SANS_SERIF, Font.PLAIN, w / 8)
        val f1 = Font(Font.SANS_SERIF, Font.ITALIC, w / 8)
        val s = "AttributedString"
        val attrStr = AttributedString(s).apply {
            addAttribute(TextAttribute.FONT, f, 0, 10)
            addAttribute(TextAttribute.FONT, f1, 10, s.length)
        }
        val aci = attrStr.iterator
        var tl = TextLayout(aci, frc)
        var sw = tl.bounds.width.toFloat()
        var sh = tl.bounds.height.toFloat()
        var shape = tl.getOutline(AffineTransform.getTranslateInstance((w / 2 - sw / 2).toDouble(), h * 0.2 + sh / 2))
        g2.color = Color.BLUE
        g2.stroke = BasicStroke(1.5f)
        g2.draw(shape)
        g2.color = Color.MAGENTA
        g2.fill(shape)

        f = Font("serif", Font.BOLD, w / 6)
        tl = TextLayout("Outline", f, frc)
        sw = tl.bounds.width.toFloat()
        sh = tl.bounds.height.toFloat()
        shape = tl.getOutline(AffineTransform.getTranslateInstance((w / 2 - sw / 2).toDouble(), h * 0.5 + sh / 2))
        g2.color = Color.BLACK
        g2.draw(shape)
        g2.color = Color.RED
        g2.fill(shape)

        f = Font(Font.SANS_SERIF, Font.ITALIC, w / 8)
        val fontAT = AffineTransform()
        fontAT.shear(-0.2, 0.0)
        val derivedFont = f.deriveFont(fontAT)
        tl = TextLayout("Italic-Shear", derivedFont, frc)
        sw = tl.bounds.width.toFloat()
        sh = tl.bounds.height.toFloat()
        shape = tl.getOutline(
            AffineTransform.getTranslateInstance(
                (w / 2 - sw / 2).toDouble(),
                (h * 0.80f + sh / 2).toDouble()))
        g2.color = Color.GREEN
        g2.draw(shape)
        g2.color = Color.BLACK
        g2.fill(shape)
    }

    companion object
    {
        @JvmStatic
        fun main(s: Array<String>) {
            Surface.createDemoFrame(Outline())
        }
    }
}
