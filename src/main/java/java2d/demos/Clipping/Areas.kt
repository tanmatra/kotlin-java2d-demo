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

import java2d.CControl
import java2d.ControlsSurface
import java2d.CustomControls
import java2d.RepaintingProperty
import java2d.createToolButton
import java.awt.BorderLayout
import java.awt.Color
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
    private enum class AreaType(val displayName: String, val description: String)
    {
        NOP("nop", "no area operation"),
        ADD("add", "add"),
        SUB("sub", "subtract"),
        XOR("xor", "exclusiveOr"),
        INT("int", "intersection"),
        PEAR("pear", "pear")
    }

    private var areaType: AreaType by RepaintingProperty(DEFAULT_AREA_TYPE)

    init {
        background = Color.WHITE
    }

    override val customControls = listOf<CControl>(DemoControls(this) to BorderLayout.NORTH)

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
        g2.color = Color.YELLOW
        when (areaType) {
            AreaType.NOP -> {
                g2.fill(p1)
                g2.fill(p2)
                g2.color = Color.RED
                g2.draw(p1)
                g2.draw(p2)
                return
            }
            AreaType.ADD -> area.add(Area(p2))
            AreaType.SUB -> area.subtract(Area(p2))
            AreaType.XOR -> area.exclusiveOr(Area(p2))
            AreaType.INT -> area.intersect(Area(p2))
            AreaType.PEAR -> {
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
                g2.color = Color.GREEN
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
                g2.color = Color.BLACK
                g2.fill(st1)

                // Creates the pear itself by filling the Area resulting from the
                // union of two Area objects created by two different ellipses.
                val circle = Ellipse2D.Double(x - 25, y, 50.0, 50.0)
                val oval = Ellipse2D.Double(x - 19, y - 20, 40.0, 70.0)
                val circ = Area(circle)
                circ.add(Area(oval))

                g2.color = Color.YELLOW
                g2.fill(circ)
                return
            }
        }

        g2.fill(area)
        g2.color = Color.RED
        g2.draw(area)
    }

    internal class DemoControls(private val demo: Areas) : CustomControls(demo.name)
    {
        private val toolbar = JToolBar().apply { isFloatable = false }

        init {
            val buttonGroup = ButtonGroup()
            add(toolbar)
            for (areaType in enumValues<AreaType>()) {
                val initialState = (areaType == demo.areaType)
                val button = createToolButton(areaType.displayName, initialState, areaType.description) {
                    demo.areaType = areaType
                }
                buttonGroup.add(button)
                toolbar.add(button)
            }
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
        private val DEFAULT_AREA_TYPE = AreaType.NOP

        @JvmStatic
        fun main(argv: Array<String>) {
            createDemoFrame(Areas())
        }
    }
}
