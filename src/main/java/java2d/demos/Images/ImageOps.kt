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
import java2d.toBufferedImage
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
import javax.swing.event.ChangeListener

/**
 * Images drawn using operators such as ConvolveOp LowPass & Sharpen,
 * LookupOp and RescaleOp.
 */
class ImageOps : ControlsSurface()
{
    private val imageOperations: Array<BufferedImageOp> = arrayOf(
        createThresholdOperation(LOW, HIGH),
        RescaleOp(1.0f, 0f, null),
        LookupOp(ByteLookupTable(0, invertedArray), null),
        LookupOp(ByteLookupTable(0, arrayOf(invertedArray, invertedArray, orderedArray)), null),
        BLUR_3_X_3,
        SHARPEN_3_X_3,
        EDGE_3_X_3,
        EDGE_5_X_5)

    private val images: List<BufferedImage> = IMAGE_NAMES.map { name ->
        getImage(name).toBufferedImage()
    }

    private val sliderChangeListener = ChangeListener {
        when (operationIndex) {
            0 -> initThresholdOperation()
            1 -> initRescaleOperation()
        }
        repaint()
    }

    private val slider1 = JSlider(SwingConstants.VERTICAL, 0, 255, LOW).apply {
        preferredSize = Dimension(15, 100)
        addChangeListener(sliderChangeListener)
    }

    private val slider2 = JSlider(SwingConstants.VERTICAL, 0, 255, HIGH).apply {
        preferredSize = Dimension(15, 100)
        addChangeListener(sliderChangeListener)
    }

    private var operationIndex: Int = 0

    private var imageIndex: Int = 0

    init {
        isDoubleBuffered = true
        background = Color.WHITE
    }

    override val customControls = listOf<CControl>(
        DemoControls(this) to BorderLayout.NORTH,
        slider1 to BorderLayout.WEST,
        slider2 to BorderLayout.EAST)

    private fun initThresholdOperation() {
        imageOperations[0] = createThresholdOperation(slider1.value, slider2.value)
    }

    private fun initRescaleOperation() {
        rescaleFactor = slider1.value
        rescaleOffset = slider2.value.toFloat()
        imageOperations[1] = RescaleOp(rescaleFactor / 128.0f, rescaleOffset, null)
    }

    override fun render(w: Int, h: Int, g2: Graphics2D) {
        val iw = images[imageIndex].width
        val ih = images[imageIndex].height
        val oldXform = g2.transform
        g2.scale(w.toDouble() / iw, h.toDouble() / ih)
        g2.drawImage(images[imageIndex], imageOperations[operationIndex], 0, 0)
        g2.transform = oldXform
    }

    internal class DemoControls(private val demo: ImageOps) : CustomControls(demo.name)
    {
        private val imageComboBox = JComboBox<String>().apply {
            for (imageName in IMAGE_NAMES) {
                addItem(imageName)
            }
            addActionListener {
                demo.imageIndex = selectedIndex
                demo.repaint(10)
            }
        }

        private val operationsComboBox = JComboBox<String>().apply {
            for (operationName in OPERATION_NAMES) {
                addItem(operationName)
            }
            addActionListener {
                demo.operationIndex = selectedIndex
                when (demo.operationIndex) {
                    0 -> {
                        demo.slider1.value = LOW
                        demo.slider2.value = HIGH
                        demo.slider1.isEnabled = true
                        demo.slider2.isEnabled = true
                    }
                    1 -> {
                        demo.slider1.value = rescaleFactor
                        demo.slider2.value = rescaleOffset.toInt()
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

        private const val LOW = 100
        private const val HIGH = 200

        private fun createConvolveOp(width: Int, height: Int, data: FloatArray) =
            ConvolveOp(Kernel(width, height, data))

        private val BLUR_3_X_3 = createConvolveOp(3, 3, floatArrayOf(
            0.1f, 0.1f, 0.1f,
            0.1f, 0.2f, 0.1f,
            0.1f, 0.1f, 0.1f))

        private val SHARPEN_3_X_3 = createConvolveOp(3, 3, floatArrayOf(
            -1.0f, -1.0f, -1.0f,
            -1.0f, 9.0f, -1.0f,
            -1.0f, -1.0f, -1.0f))

        private val EDGE_3_X_3 = createConvolveOp(3, 3, floatArrayOf(
            0f, -1f, 0f,
            -1f, 5f, -1f,
            0f, -1f, 0f))

        private val EDGE_5_X_5 = createConvolveOp(5, 5, floatArrayOf(
            -1.0f, -1.0f, -1.0f, -1.0f, -1.0f,
            -1.0f, -1.0f, -1.0f, -1.0f, -1.0f,
            -1.0f, -1.0f, 24.0f, -1.0f, -1.0f,
            -1.0f, -1.0f, -1.0f, -1.0f, -1.0f,
            -1.0f, -1.0f, -1.0f, -1.0f, -1.0f))

        private val invertedArray = ByteArray(256) { j -> (256 - j).toByte() }
        private val orderedArray = ByteArray(256) { j -> j.toByte() }

        private val OPERATION_NAMES = arrayOf(
            "Threshold",
            "RescaleOp",
            "Invert",
            "Yellow Invert",
            "3x3 Blur",
            "3x3 Sharpen",
            "3x3 Edge",
            "5x5 Edge")

        private var rescaleFactor = 128
        private var rescaleOffset = 0f

        private fun createThresholdOperation(low: Int, high: Int): BufferedImageOp {
            val threshold = ByteArray(256) { j ->
                when {
                    j < low -> 0.toByte()
                    j > high -> 255.toByte()
                    else -> j.toByte()
                }
            }
            return LookupOp(ByteLookupTable(0, threshold), null)
        }

        @JvmStatic
        fun main(s: Array<String>) {
            createDemoFrame(ImageOps())
        }
    }
}
