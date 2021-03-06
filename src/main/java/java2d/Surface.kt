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
package java2d

import java.awt.AlphaComposite
import java.awt.BorderLayout
import java.awt.Dimension
import java.awt.Font
import java.awt.GradientPaint
import java.awt.Graphics
import java.awt.Graphics2D
import java.awt.Image
import java.awt.Paint
import java.awt.RenderingHints
import java.awt.event.WindowAdapter
import java.awt.event.WindowEvent
import java.awt.image.BufferedImage
import java.awt.image.DataBuffer
import java.awt.image.DataBufferByte
import java.awt.image.DataBufferInt
import java.awt.image.DataBufferUShort
import java.awt.image.DirectColorModel
import java.awt.image.IndexColorModel
import java.awt.image.Raster
import java.awt.image.WritableRaster
import java.awt.print.PageFormat
import java.awt.print.Printable
import java.awt.print.PrinterException
import javax.swing.JFrame
import javax.swing.JPanel
import javax.swing.RepaintManager

/**
 * Surface is the base class for the 2d rendering demos.  Demos must
 * implement the render() method. Subclasses for Surface are
 * AnimatingSurface, ControlsSurface and AnimatingControlsSurface.
 */
abstract class Surface : JPanel(), Printable
{
    var antialiasValue: Any = RenderingHints.VALUE_ANTIALIAS_ON
        private set

    var isAntialiasing: Boolean
        get() = antialiasValue == RenderingHints.VALUE_ANTIALIAS_ON
        set(value) {
            antialiasValue = if (value) RenderingHints.VALUE_ANTIALIAS_ON else RenderingHints.VALUE_ANTIALIAS_OFF
        }

    private var renderingValue = RenderingHints.VALUE_RENDER_SPEED

    var isRenderingQuality: Boolean
        get() = renderingValue == RenderingHints.VALUE_RENDER_QUALITY
        set(value) {
            renderingValue = if (value) RenderingHints.VALUE_RENDER_QUALITY else RenderingHints.VALUE_RENDER_SPEED
        }

    var composite: AlphaComposite? = null
        private set

    var isComposite: Boolean
        get() = composite == null
        set(value) { composite = if (value) AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.5f) else null }

    var texture: Paint? = null
        set(value) {
            if (value is GradientPaint) {
                field = GradientPaint(0.0f, 0.0f, value.color1, size.width * 2.0f, 0.0f, value.color2)
            } else {
                field = value
            }
        }

    var performanceString: String? = null // PerformanceMonitor

    var bufferedImage: BufferedImage? = null

    var imageType: Int = 0
        set(value) {
            field = if (value == 0) 1 else value
            bufferedImage = null
        }

    var clearSurface = true

    // Demos using animated gif's that implement ImageObserver set dontThread.
    var dontThread: Boolean = false

    val animating: AnimatingSurface? = if (this is AnimatingSurface) this else null

    var sleepAmount: Long = 50
    private var orig: Long = 0
    private var start: Long = 0
    private var frame: Long = 0

    var monitor: Boolean = false

    private var outputPerf: Boolean = false
    private var biw: Int = 0
    private var bih: Int = 0
    private var clearOnce: Boolean = false
    private var toBeInitialized = true

    init {
        isDoubleBuffered = this is AnimatingSurface
        name = this.javaClass.simpleName
        imageType = 0

        // To launch an individual demo with the performance str output  :
        //    java -Djava2demo.perf= -cp Java2Demo.jar demos.Clipping.ClipAnim
        try {
            if (System.getProperty("java2demo.perf") != null) {
                outputPerf = true
                monitor = outputPerf
            }
        } catch (ignored: Exception) {
        }
    }

    protected fun getImage(name: String): Image {
        return DemoImages.getImage(name, this)
    }

    protected fun getFont(name: String): Font {
        return DemoFonts.getFont(name)
    }

    fun createBufferedImage(
        g2: Graphics2D,
        w: Int,
        h: Int,
        imgType: Int
    ): BufferedImage? {
        return when (imgType) {
            0 -> g2.deviceConfiguration.createCompatibleImage(w, h)
            in 1 .. 13 -> BufferedImage(w, h, imgType)
            14 -> createBinaryImage(w, h, 2)
            15 -> createBinaryImage(w, h, 4)
            16 -> createSGISurface(w, h, 32)
            17 -> createSGISurface(w, h, 16)
            else -> null
        }
    }

    private fun createBinaryImage(w: Int, h: Int, pixelBits: Int): BufferedImage {
        var bytesPerRow = w * pixelBits / 8
        if (w * pixelBits % 8 != 0) {
            bytesPerRow++
        }
        val imageData = ByteArray(h * bytesPerRow)
        val colorModel: IndexColorModel = when (pixelBits) {
            1 -> IndexColorModel(pixelBits, lut1Arr.size, lut1Arr, lut1Arr, lut1Arr)
            2 -> IndexColorModel(pixelBits, lut2Arr.size, lut2Arr, lut2Arr, lut2Arr)
            4 -> IndexColorModel(pixelBits, lut4Arr.size, lut4Arr, lut4Arr, lut4Arr)
            else -> throw IllegalArgumentException("Invalid # of bit per pixel: $pixelBits")
        }
        val dataBufferByte = DataBufferByte(imageData, imageData.size)
        val raster = Raster.createPackedRaster(dataBufferByte, w, h, pixelBits, null)
        return BufferedImage(colorModel, raster, false, null)
    }

    private fun createSGISurface(w: Int, h: Int, pixelBits: Int): BufferedImage {
        val colorModel: DirectColorModel
        val dataBuffer: DataBuffer
        val writableRaster: WritableRaster
        when (pixelBits) {
            16 -> {
                val imageDataUShort = ShortArray(w * h)
                colorModel = DirectColorModel(16, R_MASK_16, G_MASK_16, B_MASK_16)
                dataBuffer = DataBufferUShort(imageDataUShort, imageDataUShort.size)
                writableRaster = Raster.createPackedRaster(dataBuffer, w, h, w, BAND_MASK_16, null)
            }
            32 -> {
                val imageDataInt = IntArray(w * h)
                colorModel = DirectColorModel(32, R_MASK_32, G_MASK_32, B_MASK_32)
                dataBuffer = DataBufferInt(imageDataInt, imageDataInt.size)
                writableRaster = Raster.createPackedRaster(dataBuffer, w, h, w, BAND_MASK_32, null)
            }
            else -> throw IllegalArgumentException("Invalid # of bit per pixel: $pixelBits")
        }
        return BufferedImage(colorModel, writableRaster, false, null)
    }

    fun createGraphics2D(
        width: Int,
        height: Int,
        bi: BufferedImage?,
        g: Graphics?
    ): Graphics2D {
        val g2: Graphics2D? = if (bi != null) bi.createGraphics() else (g as Graphics2D?)

        g2!!.background = background
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, antialiasValue)
        g2.setRenderingHint(RenderingHints.KEY_RENDERING, renderingValue)

        if (clearSurface || clearOnce) {
            g2.clearRect(0, 0, width, height)
            clearOnce = false
        }

        if (texture != null) {
            // set composite to opaque for texture fills
            g2.composite = AlphaComposite.SrcOver
            g2.paint = texture
            g2.fillRect(0, 0, width, height)
        }

        if (composite != null) {
            g2.composite = composite
        }

        return g2
    }

    // ...demos that extend Surface must implement this routine...
    abstract fun render(w: Int, h: Int, g2: Graphics2D)

    /**
     * It's possible to turn off double-buffering for just the repaint
     * calls invoked directly on the non double buffered component.
     * This can be done by overriding paintImmediately() (which is called
     * as a result of repaint) and getting the current RepaintManager and
     * turning off double buffering in the RepaintManager before calling
     * super.paintImmediately(g).
     */
    override fun paintImmediately(x: Int, y: Int, w: Int, h: Int) {
        var repaintManager: RepaintManager? = null
        var save = true
        if (!isDoubleBuffered) {
            repaintManager = RepaintManager.currentManager(this)
            save = repaintManager.isDoubleBufferingEnabled
            repaintManager.isDoubleBufferingEnabled = false
        }
        super.paintImmediately(x, y, w, h)
        repaintManager?.isDoubleBufferingEnabled = save
    }

    override fun paint(g: Graphics) {
        super.paint(g)

        val d = size

        if (biw != d.width || bih != d.height) {
            toBeInitialized = true
            biw = d.width
            bih = d.height
        }

        if (imageType == 1) {
            bufferedImage = null
        } else if (bufferedImage == null || toBeInitialized) {
            bufferedImage = createBufferedImage(g as Graphics2D, d.width, d.height, imageType - 2)
            clearOnce = true
        }

        if (toBeInitialized) {
            animating?.reset(d.width, d.height)
            toBeInitialized = false
            startClock()
        }

        if (animating != null && animating.isRunning) {
            animating.step(d.width, d.height)
        }
        val g2 = createGraphics2D(d.width, d.height, bufferedImage, g)
        render(d.width, d.height, g2)
        g2.dispose()

        if (bufferedImage != null) {
            g.drawImage(bufferedImage, 0, 0, null)
            toolkit.sync()
        }

        if (monitor) {
            logPerformance()
        }
    }

    @Throws(PrinterException::class)
    override fun print(g: Graphics, pf: PageFormat, pi: Int): Int {
        if (pi >= 1) {
            return Printable.NO_SUCH_PAGE
        }

        val g2d = g as Graphics2D
        g2d.translate(pf.imageableX, pf.imageableY)
        g2d.translate(pf.imageableWidth / 2,
                      pf.imageableHeight / 2)

        val d = size

        val scale = Math.min(pf.imageableWidth / d.width,
                             pf.imageableHeight / d.height)
        if (scale < 1.0) {
            g2d.scale(scale, scale)
        }

        g2d.translate(-d.width / 2.0, -d.height / 2.0)

        if (bufferedImage == null) {
            val g2 = createGraphics2D(d.width, d.height, null, g2d)
            render(d.width, d.height, g2)
            g2.dispose()
        } else {
            g2d.drawImage(bufferedImage, 0, 0, this)
        }

        return Printable.PAGE_EXISTS
    }

    fun startClock() {
        orig = System.currentTimeMillis()
        start = orig
        frame = 0
    }

    private fun logPerformance() {
        if (frame % REPORTFRAMES == 0L) {
            val end = System.currentTimeMillis()
            val rel = end - start
            if (frame == 0L) {
                performanceString = "$name $rel ms"
                if (animating == null || !animating.isRunning) {
                    frame = -1
                }
            } else {
                val fps ="%.3g".format(REPORTFRAMES / (rel / 1000.0f))
                performanceString = "$name $fps fps"
            }
            if (outputPerf) {
                println(performanceString)
            }
            start = end
        }
        ++frame
    }

    // System.out graphics state information.
    fun verbose(java2Demo: Java2Demo?) {
        val string = buildString {
            append("  $name")
            if (animating != null && animating.isRunning) {
                append(" Running")
            } else if (this@Surface is AnimatingSurface) {
                append(" Stopped")
            }
            java2Demo?.globalControls?.selectedScreenItem?.let { screenItem ->
                append(" $screenItem")
            }
            append(if (isAntialiasing) " ANTIALIAS_ON " else " ANTIALIAS_OFF ")
            append(if (isRenderingQuality) " RENDER_QUALITY " else " RENDER_SPEED ")
            if (texture != null) {
                append(" Texture")
            }
            composite?.let { composite ->
                append(" Composite=${composite.alpha}")
            }
            val runtime = Runtime.getRuntime()
            runtime.gc()
            val freeMemory = runtime.freeMemory().toFloat()
            val totalMemory = runtime.totalMemory().toFloat()
            append(" ${(totalMemory - freeMemory) / 1024}K used")
        }
        println(string)
    }

    internal open fun checkRepaint() {
        repaint()
    }

    companion object
    {
        const val R_MASK_32 = 0xFF000000.toInt()
        const val G_MASK_32 = 0x00FF0000
        const val B_MASK_32 = 0x0000FF00
        const val R_MASK_16 = 0xF800
        const val G_MASK_16 = 0x07C0
        const val B_MASK_16 = 0x003E
        val BAND_MASK_16 = intArrayOf(R_MASK_16, G_MASK_16, B_MASK_16)
        val BAND_MASK_32 = intArrayOf(R_MASK_32, G_MASK_32, B_MASK_32)

        // Lookup tables for BYTE_BINARY 1, 2 and 4 bits.
        internal var lut1Arr = byteArrayOf(0, 255.toByte())
        internal var lut2Arr = ByteArray(4) { i -> (i * 85).toByte() }
        internal var lut4Arr = ByteArray(16) { i -> (i * 17).toByte() }

        const private val REPORTFRAMES = 30

        @JvmStatic
        fun createDemoFrame(surface: Surface) {
            val demoPanel = DemoPanel(GlobalOptions.Basic(), surface)
            JFrame("Java2D Demo - ${surface.name}").apply {
                addWindowListener(object : WindowAdapter() {
                    override fun windowClosing(e: WindowEvent?) {
                        System.exit(0)
                    }
                    override fun windowDeiconified(e: WindowEvent?) {
                        demoPanel.start()
                    }
                    override fun windowIconified(e: WindowEvent?) {
                        demoPanel.stop()
                    }
                })
                contentPane.add(demoPanel, BorderLayout.CENTER)
                pack()
                size = Dimension(500, 300)
                isVisible = true
            }
            surface.animating?.start()
        }
    }
}
