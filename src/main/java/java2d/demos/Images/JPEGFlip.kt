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

import java2d.Surface
import java2d.getLogger
import java2d.use
import java.awt.Color
import java.awt.Color.RED
import java.awt.Font
import java.awt.Graphics2D
import java.awt.geom.GeneralPath
import java.awt.geom.Path2D
import java.awt.image.BufferedImage
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.util.logging.Level
import javax.imageio.IIOImage
import javax.imageio.ImageIO
import javax.imageio.ImageWriteParam
import javax.imageio.plugins.jpeg.JPEGImageWriteParam
import javax.imageio.stream.ImageOutputStream

/**
 * Render a filled star & duke into a BufferedImage, save the BufferedImage
 * as a JPEG, display the BufferedImage, using the decoded JPEG BufferedImage
 * display the JPEG flipped BufferedImage.
 */
class JPEGFlip : Surface()
{
    private val image = getImage("duke.gif")

    init {
        background = Color.WHITE
    }

    override fun render(w: Int, h: Int, g2: Graphics2D) {
        val hh = h / 2

        val bi = BufferedImage(w, hh, BufferedImage.TYPE_INT_RGB)
        bi.createGraphics().use { big ->
            // .. use rendering hints from J2DCanvas ..
            big.setRenderingHints(g2.renderingHints)

            big.background = background
            big.clearRect(0, 0, w, hh)

            big.color = Color.GREEN.darker()
            val path = GeneralPath(Path2D.WIND_NON_ZERO).apply {
                moveTo(-w / 2.0f, -hh / 8.0f)
                lineTo(+w / 2.0f, -hh / 8.0f)
                lineTo(-w / 4.0f, +hh / 2.0f)
                lineTo(+0.0f, -hh / 2.0f)
                lineTo(+w / 4.0f, +hh / 2.0f)
                closePath()
            }
            big.translate(w / 2, hh / 2)
            big.fill(path)

            val iw = image.getWidth(this)
            var ih = image.getHeight(this)
            if (hh < ih * 1.5) {
                ih = (ih * (hh / (ih * 1.5))).toInt()
            }
            big.drawImage(image, -image.getWidth(this) / 2, -ih / 2, iw, ih, this)
        }

        g2.drawImage(bi, 0, 0, this)
        g2.font = Font(Font.DIALOG, Font.PLAIN, 10)
        g2.color = Color.BLACK
        g2.drawString("BufferedImage", 4, 12)

        val bi1: BufferedImage?
        var ios: ImageOutputStream? = null
        // To write the jpeg to a file uncomment the File* lines and
        // comment out the ByteArray*Stream lines.
        //FileOutputStream out = null;
        var out: ByteArrayOutputStream? = null
        //FileInputStream in = null;
        var input: ByteArrayInputStream? = null
        try {
            //File file = new File("images", "test.jpg");
            //out = new FileOutputStream(file);
            out = ByteArrayOutputStream()
            ios = ImageIO.createImageOutputStream(out)
            val encoder = ImageIO.getImageWritersByFormatName("JPEG").next()
            val param = JPEGImageWriteParam(null).apply {
                compressionMode = ImageWriteParam.MODE_EXPLICIT
                compressionQuality = 1.0f
            }

            encoder.output = ios
            encoder.write(null, IIOImage(bi, null, null), param)

            //in = new FileInputStream(file);
            input = ByteArrayInputStream(out.toByteArray())
            bi1 = ImageIO.read(input)
        } catch (ex: Exception) {
            g2.color = RED
            g2.drawString("Error encoding or decoding the image", 5, hh * 2 - 5)
            return
        } finally {
            if (ios != null) {
                try {
                    ios.close()
                } catch (ex: IOException) {
                    getLogger<JPEGFlip>().log(Level.SEVERE, null, ex)
                }
            }
            if (out != null) {
                try {
                    out.close()
                } catch (ex: IOException) {
                    getLogger<JPEGFlip>().log(Level.SEVERE, null, ex)
                }
            }
            if (input != null) {
                try {
                    input.close()
                } catch (ex: IOException) {
                    getLogger<JPEGFlip>().log(Level.SEVERE, null, ex)
                }
            }
        }

        if (bi1 == null) {
            g2.color = Color.RED
            g2.drawString("Error reading the image", 5, hh * 2 - 5)
            return
        }

        g2.drawImage(bi1, w, hh * 2, -w, -hh, null)

        g2.drawString("JPEGImage Flipped", 4, hh * 2 - 4)
        g2.drawLine(0, hh, w, hh)
    }

    companion object {
        @JvmStatic
        fun main(s: Array<String>) {
            createDemoFrame(JPEGFlip())
        }
    }
}
