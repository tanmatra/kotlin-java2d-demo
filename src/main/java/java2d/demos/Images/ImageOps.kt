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
package java2d.demos.Images

import java2d.CControl
import java2d.ControlsSurface
import java2d.CustomControls
import java2d.use
import java.awt.BorderLayout
import java.awt.Color
import java.awt.Dimension
import java.awt.Graphics2D
import java.awt.image.BufferedImage
import java.awt.image.BufferedImageOp
import java.awt.image.ByteLookupTable
import java.awt.image.ConvolveOp
import java.awt.image.Kernel
import java.awt.image.LookupOp
import java.awt.image.RescaleOp
import javax.swing.JComboBox
import javax.swing.JSlider
import javax.swing.SwingConstants
import javax.swing.event.ChangeEvent
import javax.swing.event.ChangeListener

/**
 * Images drawn using operators such as ConvolveOp LowPass & Sharpen,
 * LookupOp and RescaleOp.
 */
class ImageOps : ControlsSurface(), ChangeListener
{
    private val img: List<BufferedImage> = IMAGE_NAMES.map { name ->
        val image = getImage(name)
        val iw = image.getWidth(this)
        val ih = image.getHeight(this)
        BufferedImage(iw, ih, BufferedImage.TYPE_INT_RGB).apply {
            createGraphics().use { g2 -> g2.drawImage(image, 0, 0, null) }
        }
    }

    private val slider1 = JSlider(SwingConstants.VERTICAL, 0, 255, low).apply {
        preferredSize = Dimension(15, 100)
        addChangeListener(this@ImageOps)
    }

    private val slider2 = JSlider(SwingConstants.VERTICAL, 0, 255, high).apply {
        preferredSize = Dimension(15, 100)
        addChangeListener(this@ImageOps)
    }

    private var opsIndex: Int = 0

    private var imgIndex: Int = 0

    init {
        isDoubleBuffered = true
        background = Color.WHITE
    }

    override val customControls = listOf<CControl>(
        DemoControls(this) to BorderLayout.NORTH,
        slider1 to BorderLayout.WEST,
        slider2 to BorderLayout.EAST)

    override fun render(w: Int, h: Int, g2: Graphics2D) {
        val iw = img[imgIndex].getWidth(null)
        val ih = img[imgIndex].getHeight(null)
        val oldXform = g2.transform
        g2.scale(w.toDouble() / iw, h.toDouble() / ih)
        g2.drawImage(img[imgIndex], biop[opsIndex], 0, 0)
        g2.transform = oldXform
    }

    override fun stateChanged(e: ChangeEvent) {
        if (e.source == slider1) {
            if (opsIndex == 0) {
                thresholdOp(slider1.value, high)
            } else {
                rescaleFactor = slider1.value
                biop[1] = RescaleOp(rescaleFactor / 128.0f, rescaleOffset, null)
            }
        } else {
            if (opsIndex == 0) {
                thresholdOp(low, slider2.value)
            } else {
                rescaleOffset = slider2.value.toFloat()
                biop[1] = RescaleOp(rescaleFactor / 128.0f, rescaleOffset, null)
            }
        }
        repaint()
    }

    internal class DemoControls(private val demo: ImageOps) : CustomControls(demo.name)
    {
        private val imageComboBox = JComboBox<String>().apply {
            for (imageName in IMAGE_NAMES) {
                addItem(imageName)
            }
            addActionListener {
                demo.imgIndex = selectedIndex
                demo.repaint(10)
            }
        }

        private val operationsComboBox = JComboBox<String>().apply {
            for (operationName in OPERATION_NAMES) {
                addItem(operationName)
            }
            addActionListener {
                demo.opsIndex = selectedIndex
                when (demo.opsIndex) {
                    0 -> {
                        demo.slider1.value = ImageOps.low
                        demo.slider2.value = ImageOps.high
                        demo.slider1.isEnabled = true
                        demo.slider2.isEnabled = true
                    }
                    1 -> {
                        demo.slider1.value = ImageOps.rescaleFactor
                        demo.slider2.value = ImageOps.rescaleOffset.toInt()
                        demo.slider1.isEnabled = true
                        demo.slider2.isEnabled = true
                    }
                    else -> {
                        demo.slider1.isEnabled = false
                        demo.slider2.isEnabled = false
                    }
                }
                demo.repaint(10)
            }
        }

        init {
            add(imageComboBox)
            add(operationsComboBox)
        }

        override fun getPreferredSize() = Dimension(200, 39)

        override fun run() {
            try {
                Thread.sleep(1111)
            } catch (e: Exception) {
                return
            }

            val me = Thread.currentThread()
            while (thread === me) {
                for (i in IMAGE_NAMES.indices) {
                    imageComboBox.selectedIndex = i
                    for (j in OPERATION_NAMES.indices) {
                        operationsComboBox.selectedIndex = j
                        if (j <= 1) {
                            var k = 50
                            while (k <= 200) {
                                demo.slider1.value = k
                                try {
                                    Thread.sleep(200)
                                } catch (e: InterruptedException) {
                                    return
                                }
                                k += 10
                            }
                        }
                        try {
                            Thread.sleep(4444)
                        } catch (e: InterruptedException) {
                            return
                        }
                    }
                }
            }
            thread = null
        }
    }

    companion object
    {
        private val IMAGE_NAMES = arrayOf("bld.jpg", "boat.png")

        private val OPERATION_NAMES = arrayOf(
            "Threshold",
            "RescaleOp",
            "Invert",
            "Yellow Invert",
            "3x3 Blur",
            "3x3 Sharpen",
            "3x3 Edge",
            "5x5 Edge")

        private val biop = arrayOfNulls<BufferedImageOp>(OPERATION_NAMES.size)
        private var rescaleFactor = 128
        private var rescaleOffset = 0f
        private const val low = 100
        private const val high = 200

        init {
            thresholdOp(low, high)
            var i = 1
            biop[i++] = RescaleOp(1.0f, 0f, null)
            val invert = ByteArray(256)
            val ordered = ByteArray(256)
            for (j in 0 .. 255) {
                invert[j] = (256 - j).toByte()
                ordered[j] = j.toByte()
            }
            biop[i++] = LookupOp(ByteLookupTable(0, invert), null)
            val yellowInvert = arrayOf(invert, invert, ordered)
            biop[i++] = LookupOp(ByteLookupTable(0, yellowInvert), null)
            val dim = arrayOf(intArrayOf(3, 3), intArrayOf(3, 3), intArrayOf(3, 3), intArrayOf(5, 5))

            val data = arrayOf(
                floatArrayOf(
                    0.1f, 0.1f, 0.1f, // 3x3 blur
                    0.1f, 0.2f, 0.1f,
                    0.1f, 0.1f, 0.1f),
                floatArrayOf(
                    -1.0f, -1.0f, -1.0f, // 3x3 sharpen
                    -1.0f, 9.0f, -1.0f,
                    -1.0f, -1.0f, -1.0f),
                floatArrayOf(
                    0f, -1f, 0f, // 3x3 edge
                    -1f, 5f, -1f,
                    0f, -1f, 0f),
                floatArrayOf(
                    -1.0f, -1.0f, -1.0f, -1.0f, -1.0f, // 5x5 edge
                    -1.0f, -1.0f, -1.0f, -1.0f, -1.0f,
                    -1.0f, -1.0f, 24.0f, -1.0f, -1.0f,
                    -1.0f, -1.0f, -1.0f, -1.0f, -1.0f,
                    -1.0f, -1.0f, -1.0f, -1.0f, -1.0f))

            var j = 0
            while (j < data.size) {
                biop[i] = ConvolveOp(Kernel(dim[j][0], dim[j][1], data[j]))
                j++
                i++
            }
        }

        fun thresholdOp(low: Int, high: Int) {
            val threshold = ByteArray(256)
            for (j in 0 .. 255) {
                when {
                    j > high -> threshold[j] = 255.toByte()
                    j < low -> threshold[j] = 0.toByte()
                    else -> threshold[j] = j.toByte()
                }
            }
            biop[0] = LookupOp(ByteLookupTable(0, threshold), null)
        }

        @JvmStatic
        fun main(s: Array<String>) {
            createDemoFrame(ImageOps())
        }
    }
}
