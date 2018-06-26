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

import java2d.CControl
import java2d.ControlsSurface
import java2d.CustomControls
import java.awt.BorderLayout
import java.awt.Color
import java.awt.Component
import java.awt.Dimension
import java.awt.GradientPaint
import java.awt.Graphics
import java.awt.Graphics2D
import java.awt.font.TextLayout
import javax.swing.Icon
import javax.swing.JMenu
import javax.swing.JMenuBar
import javax.swing.JMenuItem

class Gradient : ControlsSurface()
{
    private var innerColor: Color
    private var outerColor: Color

    init {
        background = Color.WHITE
        innerColor = Color.GREEN
        outerColor = Color.BLUE
    }

    override val customControls = listOf<CControl>(DemoControls(this) to BorderLayout.NORTH)

    override fun render(w: Int, h: Int, g2: Graphics2D) {
        val w2 = w / 2
        val h2 = h / 2

        g2.paint = GradientPaint(0f, 0f, outerColor, w * 0.35f, h * 0.35f, innerColor)
        g2.fillRect(0, 0, w2, h2)

        g2.paint = GradientPaint(w.toFloat(), 0f, outerColor, w * 0.65f, h * 0.35f, innerColor)
        g2.fillRect(w2, 0, w2, h2)

        g2.paint = GradientPaint(0f, h.toFloat(), outerColor, w * 0.35f, h * 0.65f, innerColor)
        g2.fillRect(0, h2, w2, h2)

        g2.paint = GradientPaint(w.toFloat(), h.toFloat(), outerColor, w * 0.65f, h * 0.65f, innerColor)
        g2.fillRect(w2, h2, w2, h2)

        g2.color = Color.BLACK
        val textLayout = TextLayout("GradientPaint", g2.font, g2.fontRenderContext)
        textLayout.draw(g2,
            (w / 2 - textLayout.bounds.width / 2).toInt().toFloat(),
            (h / 2 + textLayout.bounds.height / 2).toInt().toFloat())
    }

    private class ColorItem(val name: String, val color: Color)
    {
        val icon = DemoControls.ColoredSquare(color)
    }

    internal class DemoControls(private val demo: Gradient) : CustomControls(demo.name)
    {
        private val colorItems = arrayOf(
            ColorItem("Red", Color.RED),
            ColorItem("Orange", Color.ORANGE),
            ColorItem("Yellow", Color.YELLOW),
            ColorItem("Green", Color.GREEN),
            ColorItem("Blue", Color.BLUE),
            ColorItem("Light Gray", Color.LIGHT_GRAY),
            ColorItem("Cyan", Color.CYAN),
            ColorItem("Magenta", Color.MAGENTA))

        private val innerColorMenu = JMenu("Inner Color")
        private val outerColorMenu = JMenu("Outer Color")

        private var innerColorMenuItems = colorItems.map { colorItem ->
            JMenuItem(colorItem.name).apply {
                icon = colorItem.icon
                addActionListener {
                    demo.innerColor = colorItem.color
                    innerColorMenu.icon = colorItem.icon
                    demo.repaint()
                }
                innerColorMenu.add(this)
            }
        }

        @Suppress("unused")
        private var outerColorMenuItems = colorItems.map { colorItem ->
            JMenuItem(colorItem.name).apply {
                icon = colorItem.icon
                addActionListener {
                    demo.outerColor = colorItem.color
                    outerColorMenu.icon = colorItem.icon
                    demo.repaint()
                }
                outerColorMenu.add(this)
            }
        }

        init {
            val inMenuBar = JMenuBar()
            add(inMenuBar)
            val outMenuBar = JMenuBar()
            add(outMenuBar)

            inMenuBar.add(innerColorMenu)
            innerColorMenu.icon = ColoredSquare(demo.innerColor)
            outMenuBar.add(outerColorMenu)
            outerColorMenu.icon = ColoredSquare(demo.outerColor)
        }

        override fun getPreferredSize() = Dimension(200, 37)

        override fun run() {
            // goto double buffering
            if (demo.imageType <= 1) {
                demo.imageType = 2
            }
            val me = Thread.currentThread()
            while (thread === me) {
                for (i in innerColorMenuItems.indices) {
                    if (i != 4) {
                        try {
                            Thread.sleep(4444)
                        } catch (e: InterruptedException) {
                            return
                        }
                        innerColorMenuItems[i].doClick()
                    }
                }
            }
            thread = null
        }

        internal class ColoredSquare(private val color: Color) : Icon
        {
            override fun paintIcon(c: Component, g: Graphics, x: Int, y: Int) {
                val oldColor = g.color
                g.color = color
                g.fill3DRect(x, y, iconWidth, iconHeight, true)
                g.color = oldColor
            }

            override fun getIconWidth(): Int = 12

            override fun getIconHeight(): Int = 12
        }
    }

    companion object {
        @JvmStatic
        fun main(s: Array<String>) {
            createDemoFrame(Gradient())
        }
    }
}
