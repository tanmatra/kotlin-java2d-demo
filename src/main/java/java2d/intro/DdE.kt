package java2d.intro

import java2d.createSimilar
import java.awt.Graphics2D
import java.awt.image.BufferedImage

/**
 * Dither Dissolve Effect. For each successive step in the animation,
 * a pseudo-random starting horizontal position is chosen using list,
 * and then the corresponding points created from xlist and ylist are
 * blacked out for the current "chunk".  The x and y chunk starting
 * positions are each incremented by the associated chunk size, and
 * this process is repeated for the number of "steps" in the
 * animation, causing an equal number of pseudo-randomly picked
 * "blocks" to be blacked out during each step of the animation.
 */
internal class DdE(
    override val begin: Int,
    override val end: Int,
    private val blocksize: Int
) : Part
{
    private var bimg: BufferedImage? = null
    private var big: Graphics2D? = null
    private lateinit var list: List<Int>
    private lateinit var xlist: List<Int>
    private lateinit var ylist: List<Int>
    private var xeNum: Int = 0
    private var yeNum: Int = 0    // element number
    private var xcSize: Int = 0
    private var ycSize: Int = 0  // chunk size
    private var inc: Int = 0

    private fun createShuffledLists() {
        xlist = MutableList(bimg!!.width) { i -> i }.apply { shuffle() }
        ylist = MutableList(bimg!!.height) { i -> i }.apply { shuffle() }
        list = MutableList(end - begin + 1) { i -> i }.apply { shuffle() }
    }

    override fun reset(surface: Intro.Surface, newWidth: Int, newHeight: Int) {
        bimg = null
    }

    override fun step(surfaceImage: BufferedImage, surface: Intro.Surface, w: Int, h: Int) {
        if (inc > end) {
            bimg = null
        }
        if (bimg == null) {
            bimg = surfaceImage.createSimilar().also { bimg ->
                big = bimg.createGraphics().also { g ->
                    g.drawImage(surfaceImage, 0, 0, null)
                }
            }
            createShuffledLists()
            xcSize = xlist.size / (end - begin) + 1
            ycSize = ylist.size / (end - begin) + 1
            xeNum = 0
            inc = 0
        }
        xeNum = xcSize * list[inc]
        yeNum = -ycSize
        inc++
    }

    override fun render(w: Int, h: Int, g2: Graphics2D) {
        big!!.color = Intro.BLACK

        for (k in 0..end - begin) {
            if (xeNum + xcSize > xlist.size) {
                xeNum = 0
            } else {
                xeNum += xcSize
            }
            yeNum += ycSize

            var i = xeNum
            while (i < xeNum + xcSize && i < xlist.size) {
                var j = yeNum
                while (j < yeNum + ycSize && j < ylist.size) {
                    val xval = xlist[i]
                    val yval = ylist[j]
                    if (xval % blocksize == 0 && yval % blocksize == 0) {
                        big!!.fillRect(xval, yval, blocksize, blocksize)
                    }
                    j++
                }
                i++
            }
        }

        g2.drawImage(bimg, 0, 0, null)
    }
}
