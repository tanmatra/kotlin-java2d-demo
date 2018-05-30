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
import java.awt.Color
import java.awt.Dimension
import java.awt.Font
import java.awt.GradientPaint
import java.awt.Graphics
import java.awt.Graphics2D
import java.awt.Image
import java.awt.Paint
import java.awt.RenderingHints.KEY_ANTIALIASING
import java.awt.RenderingHints.KEY_RENDERING
import java.awt.RenderingHints.VALUE_ANTIALIAS_OFF
import java.awt.RenderingHints.VALUE_ANTIALIAS_ON
import java.awt.RenderingHints.VALUE_RENDER_QUALITY
import java.awt.RenderingHints.VALUE_RENDER_SPEED
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
import java.util.logging.Level
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
    var antiAlias = VALUE_ANTIALIAS_ON
    var rendering = VALUE_RENDER_SPEED
    var composite: AlphaComposite? = null
    var texture: Paint? = null
    var perfStr: String? = null // PerformanceMonitor

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
        } catch (ex: Exception) {
        }
    }

    protected fun getImage(name: String): Image {
        return DemoImages.getImage(name, this)
    }

    protected fun getFont(name: String): Font {
        return DemoFonts.getFont(name)
    }

    fun setAntiAlias(aa: Boolean) {
        antiAlias = if (aa) VALUE_ANTIALIAS_ON else VALUE_ANTIALIAS_OFF
    }

    fun setRendering(rd: Boolean) {
        rendering = if (rd) VALUE_RENDER_QUALITY else VALUE_RENDER_SPEED
    }

    fun setTexture(obj: Any?) {
        if (obj is GradientPaint) {
            texture = GradientPaint(0f, 0f, Color.white, (size.width * 2).toFloat(), 0f, Color.green)
        } else {
            texture = obj as Paint?
        }
    }

    fun setComposite(cp: Boolean) {
        composite = if (cp) AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.5f) else null
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
            else -> {
                val exception = Exception("Invalid # of bit per pixel")
                getLogger<Surface>().log(Level.SEVERE, null, exception)
                throw exception
            }
        }
        val dataBufferByte = DataBufferByte(imageData, imageData.size)
        val raster = Raster.createPackedRaster(dataBufferByte, w, h, pixelBits, null)
        return BufferedImage(colorModel, raster, false, null)
    }

    private fun createSGISurface(w: Int, h: Int, pixelBits: Int): BufferedImage {
        val rMask32 = -0x1000000
        val rMask16 = 0xF800
        val gMask32 = 0x00FF0000
        val gMask16 = 0x07C0
        val bMask32 = 0x0000FF00
        val bMask16 = 0x003E

        var dcm: DirectColorModel? = null
        var db: DataBuffer? = null
        var wr: WritableRaster? = null
        when (pixelBits) {
            16 -> {
                val imageDataUShort = ShortArray(w * h)
                dcm = DirectColorModel(16, rMask16, gMask16, bMask16)
                db = DataBufferUShort(imageDataUShort, imageDataUShort.size)
                wr = Raster.createPackedRaster(
                    db, w, h, w,
                    intArrayOf(rMask16, gMask16, bMask16), null)
            }
            32 -> {
                val imageDataInt = IntArray(w * h)
                dcm = DirectColorModel(32, rMask32, gMask32, bMask32)
                db = DataBufferInt(imageDataInt, imageDataInt.size)
                wr = Raster.createPackedRaster(
                    db, w, h, w,
                    intArrayOf(rMask32, gMask32, bMask32), null)
            }
            else -> getLogger<Surface>().log(Level.SEVERE, null, Exception("Invalid # of bit per pixel"))
        }
        return BufferedImage(dcm!!, wr!!, false, null)
    }

    fun createGraphics2D(
        width: Int,
        height: Int,
        bi: BufferedImage?,
        g: Graphics?
    ): Graphics2D {
        var g2: Graphics2D? = null

        if (bi != null) {
            g2 = bi.createGraphics()
        } else {
            g2 = g as Graphics2D?
        }

        g2!!.background = background
        g2.setRenderingHint(KEY_ANTIALIASING, antiAlias)
        g2.setRenderingHint(KEY_RENDERING, rendering)

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
            save = repaintManager!!.isDoubleBufferingEnabled
            repaintManager.isDoubleBufferingEnabled = false
        }
        super.paintImmediately(x, y, w, h)

        if (repaintManager != null) {
            repaintManager.isDoubleBufferingEnabled = save
        }
    }

    override fun paint(g: Graphics?) {

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
            if (animating != null) {
                animating!!.reset(d.width, d.height)
            }
            toBeInitialized = false
            startClock()
        }

        if (animating != null && animating!!.running()) {
            animating!!.step(d.width, d.height)
        }
        val g2 = createGraphics2D(d.width, d.height, bufferedImage, g)
        render(d.width, d.height, g2)
        g2.dispose()

        if (bufferedImage != null) {
            g!!.drawImage(bufferedImage, 0, 0, null)
            toolkit.sync()
        }

        if (monitor) {
            LogPerformance()
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

    private fun LogPerformance() {
        if (frame % REPORTFRAMES == 0L) {
            val end = System.currentTimeMillis()
            val rel = end - start
            if (frame == 0L) {
                perfStr = "$name $rel ms"
                if (animating == null || !animating!!.running()) {
                    frame = -1
                }
            } else {
                var s1 = java.lang.Float.toString(REPORTFRAMES / (rel / 1000.0f))
                s1 = if (s1.length < 4)
                    s1.substring(0, s1.length)
                else
                    s1.substring(0, 4)
                perfStr = "$name $s1 fps"
            }
            if (outputPerf) {
                println(perfStr)
            }
            start = end
        }
        ++frame
    }

    // System.out graphics state information.
    fun verbose() {
        var str = "  $name "
        if (animating != null && animating!!.running()) {
            str = "$str Running"
        } else if (this is AnimatingSurface) {
            str = "$str Stopped"
        }

        str = str + (" " + GlobalControls.screenCombo.selectedItem!!)

        str = str + if (antiAlias === VALUE_ANTIALIAS_ON) " ANTIALIAS_ON " else " ANTIALIAS_OFF "
        str = str + if (rendering === VALUE_RENDER_QUALITY) "RENDER_QUALITY " else "RENDER_SPEED "

        if (texture != null) {
            str = str + "Texture "
        }

        if (composite != null) {
            str = str + ("Composite=" + composite!!.alpha + " ")
        }

        val r = Runtime.getRuntime()
        r.gc()
        val freeMemory = r.freeMemory().toFloat()
        val totalMemory = r.totalMemory().toFloat()
        str = str + (((totalMemory - freeMemory) / 1024).toString() + "K used")
        println(str)
    }

    companion object
    {
        // Lookup tables for BYTE_BINARY 1, 2 and 4 bits.
        internal var lut1Arr = byteArrayOf(0, 255.toByte())
        internal var lut2Arr = byteArrayOf(0, 85.toByte(), 170.toByte(), 255.toByte())
        internal var lut4Arr = byteArrayOf(
            0,
            17.toByte(),
            34.toByte(),
            51.toByte(),
            68.toByte(),
            85.toByte(),
            102.toByte(),
            119.toByte(),
            136.toByte(),
            153.toByte(),
            170.toByte(),
            187.toByte(),
            204.toByte(),
            221.toByte(),
            238.toByte(),
            255.toByte())

        const private val REPORTFRAMES = 30

        @JvmStatic
        fun createDemoFrame(surface: Surface) {
            val demoPanel = DemoPanel(surface)
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
