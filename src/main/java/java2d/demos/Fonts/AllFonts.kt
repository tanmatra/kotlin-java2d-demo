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

import java2d.AnimatingControlsSurface
import java2d.CControl
import java2d.CustomControls
import java2d.Surface
import java.awt.BorderLayout
import java.awt.Color
import java.awt.Dimension
import java.awt.Font
import java.awt.Graphics2D
import java.awt.GraphicsEnvironment
import javax.swing.BorderFactory
import javax.swing.JMenu
import javax.swing.JMenuBar
import javax.swing.JMenuItem
import javax.swing.JSlider
import javax.swing.SwingConstants

/**
 * Scrolling text of fonts returned from GraphicsEnvironment.getAllFonts().
 */
class AllFonts : AnimatingControlsSurface()
{
    private var nStrs: Int = 0
    private var lineHeight: Int = 0
    private var fi: Int = 0
    private var fontSize = 14
    private val visibleFonts = mutableListOf<Font>()

    init {
        background = Color.WHITE
        sleepAmount = 500
    }

    override val customControls = listOf<CControl>(DemoControls(this) to BorderLayout.NORTH)

    override fun reset(newWidth: Int, newHeight: Int) {
        visibleFonts.clear()
        val font = fonts[0].deriveFont(Font.PLAIN, fontSize.toFloat())
        val fontMetrics = getFontMetrics(font)
        lineHeight = fontMetrics.height
        nStrs = newHeight / lineHeight + 1
        fi = 0
    }

    override fun step(width: Int, height: Int) {
        if (fi < fonts.size) {
            visibleFonts.add(fonts[fi].deriveFont(Font.PLAIN, fontSize.toFloat()))
        }
        if (visibleFonts.size == nStrs && !visibleFonts.isEmpty() || fi > fonts.size) {
            visibleFonts.removeAt(0)
        }
        fi = if (visibleFonts.isEmpty()) 0 else fi + 1 //++fi
    }

    override fun render(w: Int, h: Int, g2: Graphics2D) {
        g2.color = Color.BLACK
        var yy = if (fi >= fonts.size) 0 else h - visibleFonts.size * lineHeight - lineHeight / 2
        for (font in visibleFonts) {
            val stringWidth = getFontMetrics(font).stringWidth(font.name)
            g2.font = font
            yy += lineHeight
            g2.drawString(font.name, w / 2 - stringWidth / 2, yy)
        }
    }

    internal class DemoControls(demo: AllFonts) : CustomControls()
    {
        init {
            background = Color.GRAY
            val sleepAmount = demo.sleepAmount.toInt()
            add(JSlider(SwingConstants.HORIZONTAL, 0, 999, sleepAmount).apply {
                border = BorderFactory.createEtchedBorder()
                preferredSize = Dimension(90, 22)
                addChangeListener {
                    demo.sleepAmount = value.toLong()
                }
            })
            val menubar = JMenuBar()
            add(menubar)
            val menu = menubar.add(JMenu("Font Size"))
            for (size in FONT_SIZES) {
                menu.add(JMenuItem(size.toString()).apply {
                    font = Font(Font.DIALOG, Font.PLAIN, size)
                    addActionListener {
                        demo.fontSize = size
                        demo.reset(demo.width, demo.height)
                    }
                })
            }
        }
    }

    companion object
    {
        private val FONT_SIZES = intArrayOf(8, 14, 18, 24)

        private val fonts: List<Font> = GraphicsEnvironment.getLocalGraphicsEnvironment().allFonts
            .filter { font -> font.canDisplayUpTo(font.name) != 0 }

        @JvmStatic
        fun main(argv: Array<String>) {
            Surface.createDemoFrame(AllFonts())
        }
    }
}
