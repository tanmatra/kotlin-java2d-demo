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
package java2d.demos.Clipping

import java2d.ControlsSurface
import java2d.CustomControls
import java2d.Surface
import java2d.createToolButton
import java.awt.Color.BLACK
import java.awt.Color.GREEN
import java.awt.Color.RED
import java.awt.Color.WHITE
import java.awt.Color.YELLOW
import java.awt.Dimension
import java.awt.Graphics2D
import java.awt.geom.Area
import java.awt.geom.Ellipse2D
import java.awt.geom.GeneralPath
import javax.swing.AbstractButton
import javax.swing.ButtonGroup
import javax.swing.JToolBar

/**
 * The Areas class demonstrates the CAG (Constructive Area Geometry)
 * operations: Add(union), Subtract, Intersect, and ExclusiveOR.
 */
class Areas : ControlsSurface()
{
    private var areaType = "nop"

    init {
        background = WHITE
        controls = arrayOf(DemoControls(this))
    }

    override fun render(w: Int, h: Int, g2: Graphics2D) {
        val p1 = GeneralPath().apply {
            moveTo(w * .25f, 0.0f)
            lineTo(w * .75f, h * .5f)
            lineTo(w * .25f, h.toFloat())
            lineTo(0.0f, h * .5f)
            closePath()
        }
        val p2 = GeneralPath().apply {
            moveTo(w * .75f, 0.0f)
            lineTo(w.toFloat(), h * .5f)
            lineTo(w * .75f, h.toFloat())
            lineTo(w * .25f, h * .5f)
            closePath()
        }

        val area = Area(p1)
        g2.color = YELLOW
        when (areaType) {
            "nop" -> {
                g2.fill(p1)
                g2.fill(p2)
                g2.color = RED
                g2.draw(p1)
                g2.draw(p2)
                return
            }
            "add" -> area.add(Area(p2))
            "sub" -> area.subtract(Area(p2))
            "xor" -> area.exclusiveOr(Area(p2))
            "int" -> area.intersect(Area(p2))
            "pear" -> {
                val sx = (w / 100).toDouble()
                val sy = (h / 140).toDouble()
                g2.scale(sx, sy)
                val x = w.toDouble() / sx / 2.0
                val y = h.toDouble() / sy / 2.0

                // Creates the first leaf by filling the intersection of two Area
                // objects created from an ellipse.
                val leaf = Ellipse2D.Double(x - 16, y - 29, 15.0, 15.0)
                var leaf1 = Area(leaf)
                leaf.setFrame(x - 14, y - 47, 30.0, 30.0)
                val leaf2 = Area(leaf)
                leaf1.intersect(leaf2)
                g2.color = GREEN
                g2.fill(leaf1)

                // Creates the second leaf.
                leaf.setFrame(x + 1, y - 29, 15.0, 15.0)
                leaf1 = Area(leaf)
                leaf2.intersect(leaf1)
                g2.fill(leaf2)

                // Creates the stem by filling the Area resulting from the
                // subtraction of two Area objects created from an ellipse.
                val stem = Ellipse2D.Double(x, y - 42, 40.0, 40.0)
                val st1 = Area(stem)
                stem.setFrame(x + 3, y - 47, 50.0, 50.0)
                st1.subtract(Area(stem))
                g2.color = BLACK
                g2.fill(st1)

                // Creates the pear itself by filling the Area resulting from the
                // union of two Area objects created by two different ellipses.
                val circle = Ellipse2D.Double(x - 25, y, 50.0, 50.0)
                val oval = Ellipse2D.Double(x - 19, y - 20, 40.0, 70.0)
                val circ = Area(circle)
                circ.add(Area(oval))

                g2.color = YELLOW
                g2.fill(circ)
                return
            }
        }

        g2.fill(area)
        g2.color = RED
        g2.draw(area)
    }

    internal class DemoControls(var demo: Areas) : CustomControls(demo.name)
    {
        private val toolbar = JToolBar().apply { isFloatable = false }
        private val buttonGroup = ButtonGroup()

        init {
            add(toolbar)
            addTool("nop", "no area operation", true)
            addTool("add", "add", false)
            addTool("sub", "subtract", false)
            addTool("xor", "exclusiveOr", false)
            addTool("int", "intersection", false)
            addTool("pear", "pear", false)
        }

        private fun addTool(str: String, tooltip: String, state: Boolean) {
            val button = createToolButton(str, state, tooltip) {
                demo.areaType = str
                demo.repaint()
            }
            buttonGroup.add(button)
            toolbar.add(button)
        }

        override fun getPreferredSize() = Dimension(200, 40)

        override fun run() {
            try {
                Thread.sleep(1111)
            } catch (e: Exception) {
                return
            }

            val me = Thread.currentThread()
            while (thread === me) {
                for (comp in toolbar.components) {
                    (comp as AbstractButton).doClick()
                    try {
                        Thread.sleep(4444)
                    } catch (e: InterruptedException) {
                        return
                    }
                }
            }
            thread = null
        }
    }

    companion object
    {
        @JvmStatic
        fun main(argv: Array<String>) {
            Surface.createDemoFrame(Areas())
        }
    }
}