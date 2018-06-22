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

import java2d.AnimatingControlsSurface
import java2d.CControl
import java2d.CustomControls
import java.awt.BorderLayout
import java.awt.Color.BLACK
import java.awt.Color.ORANGE
import java.awt.Color.WHITE
import java.awt.Dimension
import java.awt.Graphics2D
import java.awt.Image
import java.awt.event.ActionEvent
import java.awt.event.ActionListener
import java.awt.font.TextLayout
import javax.swing.AbstractButton
import javax.swing.JToggleButton
import javax.swing.JToolBar

/**
 * Scaling or Shearing or Rotating an image & rectangle.
 */
class SelectTx : AnimatingControlsSurface()
{
    private var img: Image? = null
    private val original: Image = getImage("painting.gif")
    private var iw: Int = 0
    private var ih: Int = 0
    protected var transformType = SHEAR
    protected var sx: Double = 0.toDouble()
    protected var sy: Double = 0.toDouble()
    protected var angdeg: Double = 0.toDouble()
    protected var direction = RIGHT
    protected var transformToggle: Int = 0

    init {
        iw = original.getWidth(this)
        ih = original.getHeight(this)
        background = WHITE
    }

    override val customControls = listOf<CControl>(DemoControls(this) to BorderLayout.NORTH)

    override fun reset(newWidth: Int, newHeight: Int) {
        iw = if (newWidth > 3) newWidth / 3 else 1
        ih = if (newHeight > 3) newHeight / 3 else 1

        img = createImage(iw, ih)
        val big = img!!.graphics
        big.drawImage(original, 0, 0, iw, ih, ORANGE, null)
        when (transformType) {
            SCALE -> {
                direction = RIGHT
                sx = 1.0
                sy = 1.0
            }
            SHEAR -> {
                direction = RIGHT
                sx = 0.0
                sy = 0.0
            }
            else -> angdeg = 0.0
        }
    }

    override fun step(width: Int, height: Int) {
        val rw = iw + 10
        val rh = ih + 10

        if (transformType == SCALE && direction == RIGHT) {
            sx += .05
            if (width * .5 - iw * .5 + rw * sx + 10.0 > width) {
                direction = DOWN
            }
        } else if (transformType == SCALE && direction == DOWN) {
            sy += .05
            if (height * .5 - ih * .5 + rh * sy + 20.0 > height) {
                direction = LEFT
            }
        } else if (transformType == SCALE && direction == LEFT) {
            sx -= .05
            if (rw * sx - 10 <= -(width * .5 - iw * .5)) {
                direction = UP
            }
        } else if (transformType == SCALE && direction == UP) {
            sy -= .05
            if (rh * sy - 20 <= -(height * .5 - ih * .5)) {
                direction = RIGHT
                transformToggle = SHEAR
            }
        }

        if (transformType == SHEAR && direction == RIGHT) {
            sx += .05
            if (rw.toDouble() + 2.0 * rh.toDouble() * sx + 20.0 > width) {
                direction = LEFT
                sx -= .1
            }
        } else if (transformType == SHEAR && direction == LEFT) {
            sx -= .05
            if (rw - 2.0 * rh.toDouble() * sx + 20 > width) {
                direction = XMIDDLE
            }
        } else if (transformType == SHEAR && direction == XMIDDLE) {
            sx += .05
            if (sx > 0) {
                direction = DOWN
                sx = 0.0
            }
        } else if (transformType == SHEAR && direction == DOWN) {
            sy -= .05
            if (rh - 2.0 * rw.toDouble() * sy + 20 > height) {
                direction = UP
                sy += .1
            }
        } else if (transformType == SHEAR && direction == UP) {
            sy += .05
            if (rh.toDouble() + 2.0 * rw.toDouble() * sy + 20.0 > height) {
                direction = YMIDDLE
            }
        } else if (transformType == SHEAR && direction == YMIDDLE) {
            sy -= .05
            if (sy < 0) {
                direction = XupYup
                sy = 0.0
            }
        } else if (transformType == SHEAR && direction == XupYup) {
            sx += .05
            sy += .05
            if (rw.toDouble() + 2.0 * rh.toDouble() * sx + 30.0 > width || rh.toDouble() + 2.0 * rw.toDouble() * sy + 30.0 > height) {
                direction = XdownYdown
            }
        } else if (transformType == SHEAR && direction == XdownYdown) {
            sy -= .05
            sx -= .05
            if (sy < 0) {
                direction = RIGHT
                sy = 0.0
                sx = sy
                transformToggle = ROTATE
            }
        }

        if (transformType == ROTATE) {
            angdeg += 5.0
            if (angdeg == 360.0) {
                angdeg = 0.0
                transformToggle = SCALE
            }
        }
    }

    override fun render(w: Int, h: Int, g2: Graphics2D) {
        val font = g2.font
        val frc = g2.fontRenderContext
        val tl = TextLayout(title[transformType], font, frc)
        g2.color = BLACK
        tl.draw(g2, (w / 2 - tl.bounds.width / 2).toFloat(), tl.ascent + tl.descent)

        if (transformType == ROTATE) {
            val s = java.lang.Double.toString(angdeg)
            g2.drawString("angdeg=$s", 2, h - 4)
        } else {
            var s = java.lang.Double.toString(sx)
            s = if (s.length < 5) s else s.substring(0, 5)
            val tlsx = TextLayout("sx=$s", font, frc)
            tlsx.draw(g2, 2f, (h - 4).toFloat())

            s = java.lang.Double.toString(sy)
            s = if (s.length < 5) s else s.substring(0, 5)
            g2.drawString("sy=$s", (tlsx.bounds.width + 4).toInt(), h - 4)
        }

        if (transformType == SCALE) {
            g2.translate(w / 2 - iw / 2, h / 2 - ih / 2)
            g2.scale(sx, sy)
        } else if (transformType == SHEAR) {
            g2.translate(w / 2 - iw / 2, h / 2 - ih / 2)
            g2.shear(sx, sy)
        } else {
            g2.rotate(Math.toRadians(angdeg), (w / 2).toDouble(), (h / 2).toDouble())
            g2.translate(w / 2 - iw / 2, h / 2 - ih / 2)
        }

        g2.color = ORANGE
        g2.fillRect(0, 0, iw + 10, ih + 10)
        g2.drawImage(img, 5, 5, this)
    }

    internal class DemoControls(var demo: SelectTx) : CustomControls(demo.name), ActionListener
    {
        var toolbar: JToolBar

        init {
            toolbar = JToolBar()
            add(toolbar)
            toolbar.isFloatable = false
            addTool("Scale", false)
            addTool("Shear", true)
            addTool("Rotate", false)
        }

        fun addTool(str: String, state: Boolean) {
            val b = toolbar.add(JToggleButton(str)) as JToggleButton
            b.isFocusPainted = false
            b.isSelected = state
            b.addActionListener(this)
            val width = b.preferredSize.width
            val prefSize = Dimension(width, 21)
            b.preferredSize = prefSize
            b.maximumSize = prefSize
            b.minimumSize = prefSize
        }

        override fun actionPerformed(e: ActionEvent) {
            for (i in 0 until toolbar.componentCount) {
                val b = toolbar.getComponentAtIndex(i) as JToggleButton
                b.isSelected = false
            }
            val b = e.source as JToggleButton
            b.isSelected = true
            if (b.text == "Scale") {
                demo.transformType = SelectTx.SCALE
                demo.direction = SelectTx.RIGHT
                demo.sy = 1.0
                demo.sx = demo.sy
            } else if (b.text == "Shear") {
                demo.transformType = SelectTx.SHEAR
                demo.direction = SelectTx.RIGHT
                demo.sy = 0.0
                demo.sx = demo.sy
            } else if (b.text == "Rotate") {
                demo.transformType = SelectTx.ROTATE
                demo.angdeg = 0.0
            }
        }

        override fun getPreferredSize() = Dimension(200, 39)

        override fun run() {
            val me = Thread.currentThread()
            demo.transformToggle = demo.transformType
            while (thread === me) {
                try {
                    Thread.sleep(222)
                } catch (e: InterruptedException) {
                    return
                }
                if (demo.transformToggle != demo.transformType) {
                    (toolbar.getComponentAtIndex(demo.transformToggle) as AbstractButton).doClick()
                }
            }
            thread = null
        }
    }

    companion object
    {
        protected val RIGHT = 0
        private val LEFT = 1
        private val XMIDDLE = 2
        private val DOWN = 3
        private val UP = 4
        private val YMIDDLE = 5
        private val XupYup = 6
        private val XdownYdown = 7
        private val title = arrayOf("Scale", "Shear", "Rotate")
        protected val SCALE = 0
        protected val SHEAR = 1
        protected val ROTATE = 2

        @JvmStatic
        fun main(argv: Array<String>) {
            createDemoFrame(SelectTx())
        }
    }
}