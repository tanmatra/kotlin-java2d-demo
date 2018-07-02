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
import java.awt.Font
import java.awt.Graphics2D
import java.awt.geom.GeneralPath
import java.awt.geom.Path2D
import java.awt.image.BufferedImage
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.util.logging.Level
import javax.imageio.IIOImage
import javax.imageio.ImageIO
import javax.imageio.ImageWriteParam
import javax.imageio.plugins.jpeg.JPEGImageWriteParam

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

    override fun render(width: Int, height: Int, g2: Graphics2D) {
        val halfHeight = height / 2

        val bufferedImage1 = BufferedImage(width, halfHeight, BufferedImage.TYPE_INT_RGB)
        bufferedImage1.createGraphics().use { imgGfx ->
            // .. use rendering hints from J2DCanvas ..
            imgGfx.setRenderingHints(g2.renderingHints)

            imgGfx.background = background
            imgGfx.clearRect(0, 0, width, halfHeight)

            imgGfx.color = Color.GREEN.darker()
            val path = GeneralPath(Path2D.WIND_NON_ZERO).apply {
                moveTo(-width / 2.0f, -halfHeight / 8.0f)
                lineTo(+width / 2.0f, -halfHeight / 8.0f)
                lineTo(-width / 4.0f, +halfHeight / 2.0f)
                lineTo(+0.0f, -halfHeight / 2.0f)
                lineTo(+width / 4.0f, +halfHeight / 2.0f)
                closePath()
            }
            imgGfx.translate(width / 2, halfHeight / 2)
            imgGfx.fill(path)

            val imageWidth = image.getWidth(this)
            var imageHeight = image.getHeight(this)
            if (halfHeight < imageHeight * 1.5) {
                imageHeight = (imageHeight * (halfHeight / (imageHeight * 1.5))).toInt()
            }
            imgGfx.drawImage(image, -image.getWidth(this) / 2, -imageHeight / 2, imageWidth, imageHeight, this)
        }

        g2.drawImage(bufferedImage1, 0, 0, this)
        g2.font = Font(Font.DIALOG, Font.PLAIN, 10)
        g2.color = Color.BLACK
        g2.drawString("BufferedImage", 4, 12)

        // To write the jpeg to a file uncomment the File* lines and
        // comment out the ByteArray*Stream lines.
        val bufferedImage2: BufferedImage? = try {
            // val file = File("images", "test.jpg");
            // FileOutputStream(file).use { out -> ... }
            val bytes = ByteArrayOutputStream().use { output ->
                ImageIO.createImageOutputStream(output).use { imageOutput ->
                    val encoder = ImageIO.getImageWritersByFormatName("JPEG").next()
                    val param = JPEGImageWriteParam(null).apply {
                        compressionMode = ImageWriteParam.MODE_EXPLICIT
                        compressionQuality = 1.0f
                    }
                    encoder.output = imageOutput
                    encoder.write(null, IIOImage(bufferedImage1, null, null), param)
                }
                output.toByteArray()
            }
            // FileInputStream(file).use { input -> ... }
            ByteArrayInputStream(bytes).use { input ->
                ImageIO.read(input)
            }
        } catch (ex: Exception) {
            getLogger<JPEGFlip>().log(Level.SEVERE, null, ex)
            g2.color = Color.RED
            g2.drawString("Error encoding or decoding the image", 5, halfHeight * 2 - 5)
            return
        }

        if (bufferedImage2 == null) {
            g2.color = Color.RED
            g2.drawString("Error reading the image", 5, halfHeight * 2 - 5)
            return
        }

        g2.drawImage(bufferedImage2, width, halfHeight * 2, -width, -halfHeight, null)

        g2.drawString("JPEGImage Flipped", 4, halfHeight * 2 - 4)
        g2.drawLine(0, halfHeight, width, halfHeight)
    }

    companion object {
        @JvmStatic
        fun main(s: Array<String>) {
            createDemoFrame(JPEGFlip())
        }
    }
}
