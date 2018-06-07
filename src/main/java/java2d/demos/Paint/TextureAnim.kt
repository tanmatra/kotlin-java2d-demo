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
import java2d.CControl
import java2d.CustomControls
import java2d.Surface
import java2d.createToolButton
import java2d.use
import java.awt.BorderLayout
import java.awt.Color
import java.awt.Component
import java.awt.Dimension
import java.awt.Graphics
import java.awt.Graphics2D
import java.awt.Rectangle
import java.awt.TexturePaint
import java.awt.image.BufferedImage
import javax.swing.AbstractButton
import javax.swing.Icon
import javax.swing.JComboBox
import javax.swing.JMenu
import javax.swing.JMenuBar
import javax.swing.JMenuItem
import javax.swing.JToolBar
import kotlin.reflect.KMutableProperty0

/**
 * TexturePaint animation with controls for transformations.
 */
class TextureAnim : AnimatingControlsSurface()
{
    private var textureType: Int = 0
    private var newTexture: Boolean = false
    private var bounceSize = false
    private var bounceRect = true
    private var rotate = false
    private var shearX = false
    private var shearY = false
    private var showAnchor = true
    private val images = arrayOf(
        getImage("duke.gif"), // 8 bit GIF
        getImage("duke.png")) // 24 bit PNG
    private var textureImg: BufferedImage = makeImage(32, textureType)
        set(value) {
            field = value
            newTexture = true
        }
    private var tileSize: Int = textureImg.width
    private val w: AnimVal = AnimVal(0f, 200f, 3f, 10f, tileSize.toFloat())
    private val h: AnimVal = AnimVal(0f, 200f, 3f, 10f, tileSize.toFloat())
    private val x: AnimVal = AnimVal(0f, 200f, 3f, 10f, 0f)
    private val y: AnimVal = AnimVal(0f, 200f, 3f, 10f, 0f)
    private val rot: AnimVal = AnimVal(-360f, 360f, 5f, 15f, 0f)
    private val shx: AnimVal = AnimVal(-50f, 50f, 3f, 10f, 0f)
    private val shy: AnimVal = AnimVal(-50f, 50f, 3f, 10f, 0f)
    private var tileRect: Rectangle = Rectangle(x.intValue, y.intValue, w.intValue, h.intValue)
    private var texturePaint: TexturePaint = TexturePaint(textureImg, tileRect)

//    enum class TextureType {
//        RGB, GIF, PNG
//    }

    override val customControls = listOf<CControl>(DemoControls() to BorderLayout.NORTH)

    private fun makeImage(size: Int, type: Int): BufferedImage {
        return when (type) {
            0 -> makeRGBImage(size)
            1 -> makeGIFImage(size)
            2 -> makePNGImage(size)
            else -> error("Wrong texture type: $type")
        }
    }

    private fun makeRGBImage(size: Int): BufferedImage {
        return BufferedImage(size, size, BufferedImage.TYPE_INT_RGB).apply {
            createGraphics().use { gfx ->
                gfx.color = Color.WHITE
                gfx.fillRect(0, 0, size, size)
                for (j in 0 until size) {
                    val red = j / size.toFloat()
                    for (i in 0 until size) {
                        val green = i / size.toFloat()
                        gfx.color = Color(1.0f - red, 1.0f - green, 0.0f, 1.0f)
                        gfx.drawLine(i, j, i, j)
                    }
                }
            }
        }
    }

    private fun makeGIFImage(size: Int): BufferedImage {
        return BufferedImage(size, size, BufferedImage.TYPE_INT_RGB).apply {
            createGraphics().use { g2 ->
                g2.drawImage(images[0], 0, 0, size, size, GIF_BACKGROUND, null)
            }
        }
    }

    private fun makePNGImage(size: Int): BufferedImage {
        return BufferedImage(size, size, BufferedImage.TYPE_INT_RGB).apply {
            createGraphics().use { g2 ->
                g2.drawImage(images[1], 0, 0, size, size, PNG_BACKGROUND, null)
            }
        }
    }

    override fun reset(newWidth: Int, newHeight: Int) {
        x.newLimits((-newWidth / 4).toFloat(), (newWidth / 4 - w.intValue).toFloat())
        y.newLimits((-newHeight / 4).toFloat(), (newHeight / 4 - h.intValue).toFloat())
    }

    override fun step(width: Int, height: Int) {
        if (tileSize != textureImg.width) {
            tileSize = textureImg.width
        }
        if (bounceSize) {
            w.anim()
            h.anim()
            x.newLimits((-width / 4).toFloat(), (width / 4 - w.intValue).toFloat())
            y.newLimits((-height / 4).toFloat(), (height / 4 - h.intValue).toFloat())
        } else {
            if (w.intValue != tileSize) {
                w.set(tileSize.toFloat())
                x.newLimits((-width / 4).toFloat(), (width / 4 - w.intValue).toFloat())
            }
            if (h.intValue != tileSize) {
                h.set(tileSize.toFloat())
                y.newLimits((-height / 4).toFloat(), (height / 4 - h.intValue).toFloat())
            }
        }
        if (bounceRect) {
            x.anim()
            y.anim()
        }
        if (newTexture ||
            x.intValue != tileRect.x ||
            y.intValue != tileRect.y ||
            w.intValue != tileRect.width ||
            h.intValue != tileRect.height)
        {
            newTexture = false
            val x = x.intValue
            val y = y.intValue
            val w = w.intValue
            val h = h.intValue
            tileRect = Rectangle(x, y, w, h)
            texturePaint = TexturePaint(textureImg, tileRect)
        }
    }

    override fun render(w: Int, h: Int, g2: Graphics2D) {
        g2.translate(w / 2, h / 2)
        if (rotate) {
            rot.anim()
            g2.rotate(Math.toRadians(rot.value.toDouble()))
        } else {
            rot.set(0f)
        }
        if (shearX) {
            shx.anim()
            g2.shear((shx.value / 100).toDouble(), 0.0)
        } else {
            shx.set(0f)
        }
        if (shearY) {
            shy.anim()
            g2.shear(0.0, (shy.value / 100).toDouble())
        } else {
            shy.set(0f)
        }
        g2.paint = texturePaint
        g2.fillRect(-1000, -1000, 2000, 2000)
        if (showAnchor) {
            g2.color = COLOR_BLEND
            g2.fill(tileRect)
        }
    }

    internal inner class DemoControls : CustomControls(name)
    {
        private val toolbar = JToolBar().apply { isFloatable = false }

        init {
            add(toolbar)
            addTool("BO", "Bounce", ::bounceRect)
            addTool("SA", "Show anchor", ::showAnchor)
            addTool("RS", "Resize", ::bounceSize)
            addTool("RO", "Rotate", ::rotate)
            addTool("SX", "Shear X", ::shearX)
            addTool("SY", "Shear Y", ::shearY)

            add(JComboBox<String>().apply {
                for (i in TEXTURE_SIZES) {
                    addItem(i.toString())
                }
                addActionListener {
                    (selectedItem as? String)?.let { selectedItem ->
                        textureImg = makeImage(selectedItem.toInt(), textureType)
                        checkRepaint()
                    }
                }
                selectedIndex = 2
            })

            val menuBar = JMenuBar()
            val menu = menuBar.add(JMenu())
            for (type in 0 .. 2) {
                menu.add(JMenuItem(TexturedIcon(makeImage(ICON_SIZE, type))).apply {
                    addActionListener {
                        textureType = type
                        textureImg = makeImage(tileSize, type)
                        menu.icon = icon
                        checkRepaint()
                    }
                })
            }
            menu.icon = menu.getItem(0).icon
            add(menuBar)
        }

        private fun addTool(text: String, toolTip: String, property: KMutableProperty0<Boolean>) {
            val state = property.get()
            toolbar.add(createToolButton(text, state, toolTip) { selected ->
                property.set(selected)
                checkRepaint()
            })
        }

        private fun checkRepaint() {
            if (!isRunning) {
                this@TextureAnim.repaint()
            }
        }

        override fun getPreferredSize(): Dimension = Dimension(200, 41)

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
                val r = Rectangle(x, y, ICON_SIZE, ICON_SIZE)
                g2.paint = TexturePaint(bi, r)
                g2.fillRect(x, y, ICON_SIZE, ICON_SIZE)
                g2.color = Color.GRAY
                g2.draw3DRect(x, y, ICON_SIZE - 1, ICON_SIZE - 1, true)
            }

            override fun getIconWidth(): Int = ICON_SIZE

            override fun getIconHeight(): Int = ICON_SIZE
        }
    }

    companion object
    {
        private val COLOR_BLEND = Color(0f, 0f, 1f, 0.5f)
        private val TEXTURE_SIZES = arrayOf(8, 16, 32, 64, 80)
        private val GIF_BACKGROUND = Color(204, 204, 255)
        private val PNG_BACKGROUND = Color.LIGHT_GRAY
        private const val ICON_SIZE = 20

        @JvmStatic
        fun main(argv: Array<String>) {
            Surface.createDemoFrame(TextureAnim())
        }
    }
}
