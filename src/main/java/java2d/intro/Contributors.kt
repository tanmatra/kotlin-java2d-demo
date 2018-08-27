package java2d.intro

import java.awt.Color
import java.awt.Font
import java.awt.FontMetrics
import java.awt.GradientPaint
import java.awt.Graphics2D
import java.awt.image.BufferedImage
import java.util.ArrayList

/**
 * Scrolling text of Java2D contributors.
 */
internal class Contributors(override val begin: Int, override val end: Int) : Part
{
    private var linesCount: Int = 0
    private var lineHeight: Int = 0
    private var index: Int = 0
    private var yh: Int = 0
    private var height: Int = 0
    private val v = ArrayList<String>()
    private val cast = ArrayList<String>(members.size + 3)
    private var counter: Int = 0
    private val cntMod: Int
    private lateinit var paint: GradientPaint
    private lateinit var fontMetrics: FontMetrics

    init {
        members.sort()
        cast.add("CONTRIBUTORS")
        cast.add(" ")
        cast.addAll(members)
        cast.add(" ")
        cast.add(" ")
        cntMod = (end - begin) / cast.size - 1
    }

    override fun reset(surface: Intro.Surface, newWidth: Int, newHeight: Int) {
        fontMetrics = surface.getFontMetrics(font)
        v.clear()
        lineHeight = fontMetrics.ascent + fontMetrics.descent
        linesCount = (newHeight - 40) / lineHeight + 1
        height = lineHeight * (linesCount - 1) + 48
        index = 0
        paint = GradientPaint(0f, (newHeight / 2).toFloat(), Color.WHITE,
                              0f, (newHeight + 20).toFloat(), Color.BLACK)
        counter = 0
    }

    override fun step(surfaceImage: BufferedImage, surface: Intro.Surface, w: Int, h: Int) {
        if (counter++ % cntMod == 0) {
            if (index < cast.size) {
                v.add(cast[index])
            }
            if ((v.size == linesCount || index >= cast.size) && !v.isEmpty()) {
                v.removeAt(0)
            }
            ++index
        }
    }

    override fun render(w: Int, h: Int, g2: Graphics2D) {
        g2.paint = paint
        g2.font = font
        val remainder = (counter % cntMod).toDouble()
        var incr = 1.0 - remainder / cntMod
        incr = if (incr == 1.0) 0.0 else incr
        var y = (incr * lineHeight).toInt()

        if (index >= cast.size) {
            y += yh
        } else {
            yh = height - v.size * lineHeight + y
            y = yh
        }
        for (s in v) {
            y += lineHeight
            g2.drawString(s, w / 2 - fontMetrics.stringWidth(s) / 2, y)
        }
    }

    companion object
    {
        private val members = arrayOf(
            "Brian Lichtenwalter",
            "Jeannette Hung",
            "Thanh Nguyen",
            "Jim Graham",
            "Jerry Evans",
            "John Raley",
            "Michael Peirce",
            "Robert Kim",
            "Jennifer Ball",
            "Deborah Adair",
            "Paul Charlton",
            "Dmitry Feld",
            "Gregory Stone",
            "Richard Blanchard",
            "Link Perry",
            "Phil Race",
            "Vincent Hardy",
            "Parry Kejriwal",
            "Doug Felt",
            "Rekha Rangarajan",
            "Paula Patel",
            "Michael Bundschuh",
            "Joe Warzecha",
            "Joey Beheler",
            "Aastha Bhardwaj",
            "Daniel Rice",
            "Chris Campbell",
            "Shinsuke Fukuda",
            "Dmitri Trembovetski",
            "Chet Haase",
            "Jennifer Godinez",
            "Nicholas Talian",
            "Raul Vera",
            "Ankit Patel",
            "Ilya Bagrak",
            "Praveen Mohan",
            "Rakesh Menon")

        private val font = Font(Font.SERIF, Font.PLAIN, 26)
    }
}
