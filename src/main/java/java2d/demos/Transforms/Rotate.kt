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
package java2d.demos.Transforms

import java2d.CControl
import java2d.ControlsSurface
import java2d.CustomControls
import java.awt.BasicStroke
import java.awt.BorderLayout
import java.awt.Color
import java.awt.Dimension
import java.awt.Graphics2D
import java.awt.geom.AffineTransform
import java.awt.geom.Ellipse2D
import javax.swing.JLabel
import javax.swing.JTextField

/**
 * Rotate ellipses with controls for increment and emphasis.
 * Emphasis is defined as which ellipses have a darker color and thicker stroke.
 */
class Rotate : ControlsSurface()
{
    protected var increment = 5.0
    protected var emphasis = 9

    init {
        background = Color.WHITE
    }

    override val customControls = listOf<CControl>(DemoControls(this) to BorderLayout.NORTH)

    override fun render(w: Int, h: Int, g2: Graphics2D) {
        val size = Math.min(w, h)
        val ew = (size / 4).toFloat()
        val eh = (size - 20).toFloat()
        val ellipse = Ellipse2D.Float(-ew / 2, -eh / 2, ew, eh)
        var angle = 0.0
        while (angle < 360) {
            if (angle % emphasis == 0.0) {
                g2.color = Color.GRAY
                g2.stroke = BasicStroke(2.0f)
            } else {
                g2.color = Color.LIGHT_GRAY
                g2.stroke = BasicStroke(0.5f)
            }
            val at = AffineTransform.getTranslateInstance((w / 2).toDouble(), (h / 2).toDouble())
            at.rotate(Math.toRadians(angle))
            g2.draw(at.createTransformedShape(ellipse))
            angle += increment
        }
        g2.color = Color.BLUE
        ellipse.setFrame((w / 2 - 10).toDouble(), (h / 2 - 10).toDouble(), 20.0, 20.0)
        g2.fill(ellipse)

        g2.color = Color.GRAY
        g2.stroke = BasicStroke(6f)
        g2.draw(ellipse)

        g2.color = Color.YELLOW
        g2.stroke = BasicStroke(4f)
        g2.draw(ellipse)

        g2.color = Color.BLACK
        g2.drawString("Rotate", 5, 15)
    }

    internal class DemoControls(private val demo: Rotate) : CustomControls(demo.name)
    {
        private val incrementTextField = JTextField("5.0").apply {
            preferredSize = Dimension(30, 24)
            addActionListener {
                try {
                    demo.increment = text.trim().toDouble().coerceAtLeast(1.0)
                    demo.repaint()
                } catch (e: NumberFormatException) {
                }
            }
        }

        private val emphasisTextField = JTextField("9").apply {
            preferredSize = Dimension(30, 24)
            addActionListener {
                try {
                    demo.emphasis = text.trim().toInt()
                    demo.repaint()
                } catch (e: NumberFormatException) {
                }
            }
        }

        init {
            add(JLabel("Increment:"))
            add(incrementTextField)
            add(JLabel("Emphasis:"))
            add(emphasisTextField)
        }

        override fun getPreferredSize() = Dimension(200, 39)

        override fun run() {
            val me = Thread.currentThread()
            while (thread === me) {
                for (i in 3 until 13 step 3) {
                    try {
                        Thread.sleep(4444)
                    } catch (e: InterruptedException) {
                        return
                    }
                    incrementTextField.text = i.toString()
                    demo.increment = i.toDouble()
                    demo.repaint()
                }
            }
            thread = null
        }
    }

    companion object
    {
        @JvmStatic
        fun main(s: Array<String>) {
            createDemoFrame(Rotate())
        }
    }
}
