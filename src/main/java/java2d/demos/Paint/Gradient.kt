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

import java2d.ControlsSurface
import java2d.CustomControls
import java.awt.Color
import java.awt.Color.black
import java.awt.Color.blue
import java.awt.Color.cyan
import java.awt.Color.green
import java.awt.Color.lightGray
import java.awt.Color.magenta
import java.awt.Color.orange
import java.awt.Color.red
import java.awt.Color.white
import java.awt.Color.yellow
import java.awt.Component
import java.awt.Dimension
import java.awt.Font
import java.awt.GradientPaint
import java.awt.Graphics
import java.awt.Graphics2D
import java.awt.event.ActionEvent
import java.awt.event.ActionListener
import java.awt.font.TextLayout
import javax.swing.Icon
import javax.swing.JMenu
import javax.swing.JMenuBar
import javax.swing.JMenuItem

class Gradient : ControlsSurface()
{
    protected var innerC: Color
    protected var outerC: Color

    init {
        background = white
        innerC = green
        outerC = blue
        controls = arrayOf(DemoControls(this))
    }

    override fun render(w: Int, h: Int, g2: Graphics2D) {
        val w2 = w / 2
        val h2 = h / 2
        g2.paint = GradientPaint(0f, 0f, outerC, w * .35f, h * .35f, innerC)
        g2.fillRect(0, 0, w2, h2)
        g2.paint = GradientPaint(w.toFloat(), 0f, outerC, w * .65f, h * .35f, innerC)
        g2.fillRect(w2, 0, w2, h2)
        g2.paint = GradientPaint(0f, h.toFloat(), outerC, w * .35f, h * .65f, innerC)
        g2.fillRect(0, h2, w2, h2)
        g2.paint = GradientPaint(w.toFloat(), h.toFloat(), outerC, w * .65f, h * .65f, innerC)
        g2.fillRect(w2, h2, w2, h2)

        g2.color = black
        val tl = TextLayout("GradientPaint", g2.font, g2.fontRenderContext)
        tl.draw(g2,
                (w / 2 - tl.bounds.width / 2).toInt().toFloat(),
                (h / 2 + tl.bounds.height / 2).toInt().toFloat())
    }

    internal class DemoControls(var demo: Gradient) : CustomControls(demo.name), ActionListener
    {
        var colors = arrayOf(red, orange, yellow, green, blue, lightGray, cyan, magenta)
        var colorName = arrayOf("Red", "Orange", "Yellow", "Green", "Blue", "lightGray", "Cyan", "Magenta")
        var innerMI = arrayOfNulls<JMenuItem>(colors.size)
        var outerMI = arrayOfNulls<JMenuItem>(colors.size)
        var squares = arrayOfNulls<ColoredSquare>(colors.size)
        var imenu: JMenu
        var omenu: JMenu

        init {
            val inMenuBar = JMenuBar()
            add(inMenuBar)
            val outMenuBar = JMenuBar()
            add(outMenuBar)
            val FONT = Font("serif", Font.PLAIN, 10)

            imenu = inMenuBar.add(JMenu("Inner Color"))
            imenu.font = FONT
            imenu.icon = ColoredSquare(demo.innerC)
            omenu = outMenuBar.add(JMenu("Outer Color"))
            omenu.font = FONT
            omenu.icon = ColoredSquare(demo.outerC)
            for (i in colors.indices) {
                squares[i] = ColoredSquare(colors[i])
                val innerMenuItem = JMenuItem(colorName[i]).apply {
                    font = FONT
                    icon = squares[i]
                    addActionListener(this@DemoControls)
                }
                innerMI[i] = imenu.add(innerMenuItem)
                val outerMenuItem = JMenuItem(colorName[i]).apply {
                    font = FONT
                    icon = squares[i]
                    addActionListener(this@DemoControls)
                }
                outerMI[i] = omenu.add(outerMenuItem)
            }
        }

        override fun actionPerformed(e: ActionEvent) {
            for (i in colors.indices) {
                if (e.source == innerMI[i]) {
                    demo.innerC = colors[i]
                    imenu.icon = squares[i]
                    break
                } else if (e.source == outerMI[i]) {
                    demo.outerC = colors[i]
                    omenu.icon = squares[i]
                    break
                }
            }
            demo.repaint()
        }

        override fun getPreferredSize() = Dimension(200, 37)

        override fun run() {
            // goto double buffering
            if (demo.imageType <= 1) {
                demo.imageType = 2
            }
            val me = Thread.currentThread()
            while (thread === me) {
                for (i in innerMI.indices) {
                    if (i != 4) {
                        try {
                            Thread.sleep(4444)
                        } catch (e: InterruptedException) {
                            return
                        }

                        innerMI[i]!!.doClick()
                    }
                }
            }
            thread = null
        }

        internal inner class ColoredSquare(var color: Color) : Icon
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
