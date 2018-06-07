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

import java2d.AnimatingControlsSurface
import java2d.CustomControls
import java2d.Surface
import java.awt.Color
import java.awt.Color.BLACK
import java.awt.Color.GRAY
import java.awt.Color.LIGHT_GRAY
import java.awt.Color.WHITE
import java.awt.Component
import java.awt.Dimension
import java.awt.Graphics
import java.awt.Graphics2D
import java.awt.Image
import java.awt.Rectangle
import java.awt.TexturePaint
import java.awt.event.ActionEvent
import java.awt.event.ActionListener
import java.awt.image.BufferedImage
import javax.swing.AbstractButton
import javax.swing.Icon
import javax.swing.JComboBox
import javax.swing.JMenu
import javax.swing.JMenuBar
import javax.swing.JMenuItem
import javax.swing.JToggleButton
import javax.swing.JToolBar
import javax.swing.plaf.metal.MetalBorders.ButtonBorder

/**
 * TexturePaint animation with controls for transformations.
 */
class TextureAnim : AnimatingControlsSurface() {
    private var bNum: Int = 0
    private var tilesize: Int = 0
    private var newtexture: Boolean = false
    private var texturePaint: TexturePaint? = null
    private var tilerect: Rectangle? = null
    private var bouncesize = false
    private var bouncerect = true
    private var rotate = false
    private var shearx = false
    private var sheary = false
    private var showanchor = true
    private val w: AnimVal
    private val h: AnimVal
    private val x: AnimVal
    private val y: AnimVal
    private val rot: AnimVal
    private val shx: AnimVal
    private val shy: AnimVal

    init {
        img[0] = getImage("duke.gif")   // 8 bit gif
        img[1] = getImage("duke.png")   // 24 bit png

        textureImg = makeImage(32, 0)
        tilesize = textureImg!!.width
        w = AnimVal(0f, 200f, 3f, 10f, tilesize.toFloat())
        h = AnimVal(0f, 200f, 3f, 10f, tilesize.toFloat())
        x = AnimVal(0f, 200f, 3f, 10f, 0f)
        y = AnimVal(0f, 200f, 3f, 10f, 0f)
        rot = AnimVal(-360f, 360f, 5f, 15f, 0f)
        shx = AnimVal(-50f, 50f, 3f, 10f, 0f)
        shy = AnimVal(-50f, 50f, 3f, 10f, 0f)
        tilerect = Rectangle(x.int, y.int, w.int, h.int)
        texturePaint = TexturePaint(textureImg, tilerect!!)
        controls = arrayOf(DemoControls(this))
    }

    private fun makeImage(size: Int, num: Int): BufferedImage {
        newtexture = true
        bNum = num
        return when (num) {
            0 -> makeRGBImage(size)
            1 -> makeGIFImage(size)
            2 -> makePNGImage(size)
            else -> error("Wrong image type $num")
        }
    }

    private fun makeRGBImage(size: Int): BufferedImage {
        val bi = BufferedImage(size, size, BufferedImage.TYPE_INT_RGB)
        val big = bi.createGraphics()
        big.color = WHITE
        big.fillRect(0, 0, size, size)
        for (j in 0 until size) {
            val red = j / size.toFloat()
            for (i in 0 until size) {
                val green = i / size.toFloat()
                big.color = Color(1.0f - red, 1.0f - green, 0.0f, 1.0f)
                big.drawLine(i, j, i, j)
            }
        }
        return bi
    }

    private fun makeGIFImage(d: Int): BufferedImage {
        val bi = BufferedImage(d, d, BufferedImage.TYPE_INT_RGB)
        val big = bi.createGraphics()
        big.drawImage(img[0], 0, 0, d, d, Color(204, 204, 255), null)
        return bi
    }

    private fun makePNGImage(d: Int): BufferedImage {
        val bi = BufferedImage(d, d, BufferedImage.TYPE_INT_RGB)
        val big = bi.createGraphics()
        big.drawImage(img[1], 0, 0, d, d, LIGHT_GRAY, null)
        return bi
    }

    override fun reset(newWidth: Int, newHeight: Int) {
        x.newlimits((-newWidth / 4).toFloat(), (newWidth / 4 - w.int).toFloat())
        y.newlimits((-newHeight / 4).toFloat(), (newHeight / 4 - h.int).toFloat())
    }

    override fun step(width: Int, height: Int) {
        if (tilesize != textureImg!!.width) {
            tilesize = textureImg!!.width
        }
        if (bouncesize) {
            w.anim()
            h.anim()
            x.newlimits((-width / 4).toFloat(), (width / 4 - w.int).toFloat())
            y.newlimits((-height / 4).toFloat(), (height / 4 - h.int).toFloat())
        } else {
            if (w.int != tilesize) {
                w.set(tilesize.toFloat())
                x.newlimits((-width / 4).toFloat(), (width / 4 - w.int).toFloat())
            }
            if (h.int != tilesize) {
                h.set(tilesize.toFloat())
                y.newlimits((-height / 4).toFloat(), (height / 4 - h.int).toFloat())
            }
        }
        if (bouncerect) {
            x.anim()
            y.anim()
        }
        if (newtexture || x.int != tilerect!!.x || y.int != tilerect!!.y || w.int != tilerect!!.width || h.int != tilerect!!.height) {
            newtexture = false
            val x = x.int
            val y = y.int
            val w = w.int
            val h = h.int
            tilerect = Rectangle(x, y, w, h)
            texturePaint = TexturePaint(textureImg, tilerect!!)
        }
    }

    override fun render(w: Int, h: Int, g2: Graphics2D) {
        g2.translate(w / 2, h / 2)
        if (rotate) {
            rot.anim()
            g2.rotate(Math.toRadians(rot.flt.toDouble()))
        } else {
            rot.set(0f)
        }
        if (shearx) {
            shx.anim()
            g2.shear((shx.flt / 100).toDouble(), 0.0)
        } else {
            shx.set(0f)
        }
        if (sheary) {
            shy.anim()
            g2.shear(0.0, (shy.flt / 100).toDouble())
        } else {
            shy.set(0f)
        }
        g2.paint = texturePaint
        g2.fillRect(-1000, -1000, 2000, 2000)
        if (showanchor) {
            g2.color = BLACK
            g2.color = colorblend
            g2.fill(tilerect)
        }
    }

    internal inner class DemoControls(var demo: TextureAnim) : CustomControls(demo.name), ActionListener
    {
        val toolbar = JToolBar().apply { isFloatable = false }
        private val combo: JComboBox<String>
        private val menu: JMenu
        private val menuitems: Array<JMenuItem>
        val iconSize = 20
        private val buttonBorder = ButtonBorder()

        init {
            add(toolbar)
            addTool("BO", "bounce", true)
            addTool("SA", "show anchor", true)
            addTool("RS", "resize", false)
            addTool("RO", "rotate", false)
            addTool("SX", "shear x", false)
            addTool("SY", "shear y", false)
            combo = JComboBox()
            add(combo)
            combo.addActionListener(this)
            combo.addItem("8")
            combo.addItem("16")
            combo.addItem("32")
            combo.addItem("64")
            combo.addItem("80")
            combo.selectedIndex = 2

            val menuBar = JMenuBar()
            menu = menuBar.add(JMenu())
            menuitems = Array(3) { i ->
                val bimg = demo.makeImage(iconSize, i)
                val icon = TexturedIcon(bimg)
                val item = menu.add(JMenuItem(icon))
                item.addActionListener(this@DemoControls)
                item
            }
            menu.icon = menuitems[0].icon
            add(menuBar)
            demo.bNum = 0
        }

        private fun addTool(str: String, toolTip: String, state: Boolean) {
            val b = toolbar.add(JToggleButton(str)) as JToggleButton
            b.border = buttonBorder
            b.isFocusPainted = false
            b.isSelected = state
            b.toolTipText = toolTip
            b.addActionListener(this)
            val width = b.preferredSize.width
            val prefSize = Dimension(width, 21)
            b.preferredSize = prefSize
            b.maximumSize = prefSize
            b.minimumSize = prefSize
        }

        override fun actionPerformed(e: ActionEvent) {
            val obj = e.source
            if (obj is JComboBox<*>) {
                val selItem = combo.selectedItem as? String
                if (selItem != null) {
                    val size = Integer.parseInt(selItem)
                    TextureAnim.textureImg = demo.makeImage(size, demo.bNum)
                }
            } else if (obj is JMenuItem) {
                for (i in menuitems.indices) {
                    if (obj == menuitems[i]) {
                        TextureAnim.textureImg = demo.makeImage(demo.tilesize, i)
                        menu.icon = menuitems[i].icon
                        break
                    }
                }
            } else {
                val b = obj as JToggleButton
                when (b.text) {
                    "BO" -> demo.bouncerect = b.isSelected
                    "SA" -> demo.showanchor = b.isSelected
                    "RS" -> demo.bouncesize = b.isSelected
                    "RO" -> demo.rotate = b.isSelected
                    "SX" -> demo.shearx = b.isSelected
                    "SY" -> demo.sheary = b.isSelected
                }
            }
            if (!demo.isRunning) {
                demo.repaint()
            }
        }

        override fun getPreferredSize(): Dimension {
            return Dimension(200, 41)
        }

        override fun run() {
            val me = Thread.currentThread()
            while (thread === me) {
                for (i in 2 until toolbar.componentCount) {
                    try {
                        Thread.sleep(4444)
                    } catch (e: InterruptedException) {
                        return
                    }
                    (toolbar.getComponentAtIndex(i) as AbstractButton).doClick()
                }
            }
            thread = null
        }

        internal inner class TexturedIcon(var bi: BufferedImage) : Icon
        {
            override fun paintIcon(c: Component, g: Graphics, x: Int, y: Int) {
                val g2 = g as Graphics2D
                val r = Rectangle(x, y, iconSize, iconSize)
                g2.paint = TexturePaint(bi, r)
                g2.fillRect(x, y, iconSize, iconSize)
                g2.color = GRAY
                g2.draw3DRect(x, y, iconSize - 1, iconSize - 1, true)
            }

            override fun getIconWidth(): Int = iconSize

            override fun getIconHeight(): Int = iconSize
        }
    }

    companion object
    {
        private val colorblend = Color(0f, 0f, 1f, .5f)
        private var textureImg: BufferedImage? = null
        private val img = arrayOfNulls<Image>(2)

        @JvmStatic
        fun main(argv: Array<String>) {
            Surface.createDemoFrame(TextureAnim())
        }
    }
}
