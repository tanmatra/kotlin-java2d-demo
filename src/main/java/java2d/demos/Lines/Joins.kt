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
package java2d.demos.Lines

import java2d.CControl
import java2d.ControlsSurface
import java2d.CustomControls
import java.awt.BasicStroke
import java.awt.BorderLayout
import java.awt.Color
import java.awt.Component
import java.awt.Dimension
import java.awt.Font
import java.awt.Graphics
import java.awt.Graphics2D
import java.awt.RenderingHints
import java.awt.geom.GeneralPath
import javax.swing.Icon
import javax.swing.JLabel
import javax.swing.JMenu
import javax.swing.JMenuBar
import javax.swing.JMenuItem
import javax.swing.JSlider
import javax.swing.SwingConstants
import javax.swing.border.CompoundBorder
import javax.swing.border.EmptyBorder

/**
 * BasicStroke join types and width sizes illustrated.  Control for
 * rendering a shape returned from BasicStroke.createStrokedShape(Shape).
 */
class Joins : ControlsSurface()
{
    private var joinType = BasicStroke.JOIN_MITER

    private var strokeWidth = 20.0f

    private val label = JLabel(labelText).apply { font = FONT }

    private val slider = JSlider(SwingConstants.VERTICAL, 0, 100, (strokeWidth * 2).toInt()).apply {
        preferredSize = Dimension(15, 100)
        addChangeListener {
            // when using these sliders use double buffering, which means
            // ignoring when DemoSurface.imageType = 'On Screen'
            if (imageType <= 1) {
                imageType = 2
            }
            strokeWidth = value / 2.0f
            label.text = labelText
            this@Joins.repaint()
        }
    }

    init {
        background = Color.WHITE
    }

    private val labelText get() = " Width = $strokeWidth"

    override val customControls = listOf<CControl>(
        DemoControls(this) to BorderLayout.NORTH,
        slider to BorderLayout.WEST)

    override fun render(w: Int, h: Int, g2: Graphics2D) {
        val stroke = BasicStroke(strokeWidth, BasicStroke.CAP_BUTT, joinType)
        val path = GeneralPath().apply {
            moveTo(-w / 4.0f, -h / 12.0f)
            lineTo(+w / 4.0f, -h / 12.0f)
            lineTo(-w / 6.0f, +h / 4.0f)
            lineTo(+0.0f, -h / 4.0f)
            lineTo(+w / 6.0f, +h / 4.0f)
            closePath()
        }
        g2.translate(w / 2, h / 2)
        g2.color = Color.BLACK
        g2.draw(stroke.createStrokedShape(path))
    }

    internal class DemoControls(private val demo: Joins) : CustomControls(demo.name)
    {
        private val menuItems = JOIN_TYPES.map { joinType ->
            JMenuItem(joinType.name, joinType.icon).apply {
                font = FONT
                addActionListener {
                    demo.joinType = joinType.type
                    menu.icon = icon
                    menu.text = text
                    demo.repaint()
                }
            }
        }

        private val menu: JMenu = JMenu(menuItems[0].text).apply {
            icon = menuItems[0].icon
            font = Font(Font.SERIF, Font.PLAIN, 10)
        }

        init {
            border = CompoundBorder(border, EmptyBorder(2, 2, 2, 2))
            layout = BorderLayout()
            add(demo.label, BorderLayout.WEST)
            val menuBar = JMenuBar()
            add(menuBar, BorderLayout.EAST)
            menuBar.add(menu)
            menuItems.forEach { menu.add(it) }
        }

        override fun getPreferredSize() = Dimension(200, 37)

        override fun run() {
            try {
                Thread.sleep(999)
            } catch (e: Exception) {
                return
            }
            val me = Thread.currentThread()
            while (thread === me) {
                for (item in menuItems) {
                    item.doClick()
                    for (k in 10 until 60 step 2) {
                        demo.slider.value = k
                        try {
                            Thread.sleep(100)
                        } catch (e: InterruptedException) {
                            return
                        }
                    }
                    try {
                        Thread.sleep(999)
                    } catch (e: InterruptedException) {
                        return
                    }
                }
            }
            thread = null
        }
    }

    internal class JoinIcon(private val joinType: Int) : Icon
    {
        override fun paintIcon(c: Component, g: Graphics, x: Int, y: Int) {
            (g as Graphics2D).setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)
            g.stroke = BasicStroke(8.0f, BasicStroke.CAP_BUTT, joinType)
            val path = GeneralPath().apply {
                moveTo(0f, 3f)
                lineTo((iconWidth - 2).toFloat(), iconHeight / 2f)
                lineTo(0f, iconHeight.toFloat())
            }
            g.draw(path)
        }

        override fun getIconWidth() = 20

        override fun getIconHeight() = 20
    }

    internal class JoinType(
        val name: String,
        val type: Int)
    {
        val icon = JoinIcon(type)
    }

    companion object
    {
        private val JOIN_TYPES = arrayOf(
            JoinType("Mitered Join", BasicStroke.JOIN_MITER),
            JoinType("Rounded Join", BasicStroke.JOIN_ROUND),
            JoinType("Beveled Join", BasicStroke.JOIN_BEVEL))

        private val FONT = Font("serif", Font.BOLD, 14)

        @JvmStatic
        fun main(s: Array<String>) {
            createDemoFrame(Joins())
        }
    }
}
