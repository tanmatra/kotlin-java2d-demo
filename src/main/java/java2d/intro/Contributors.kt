package java2d.intro

import java.awt.Color
import java.awt.Font
import java.awt.GradientPaint
import java.awt.Graphics2D
import java.util.ArrayList

/**
 * Scrolling text of Java2D contributors.
 */
internal class Contributors(override val begin: Int, override val end: Int) : Part
{
    private var nStrs: Int = 0
    private var strH: Int = 0
    private var index: Int = 0
    private var yh: Int = 0
    private var height: Int = 0
    private val v = ArrayList<String>()
    private val cast = ArrayList<String>(members.size + 3)
    private var counter: Int = 0
    private val cntMod: Int
    private var gp: GradientPaint? = null

    init {
        members.sort()
        cast.add("CONTRIBUTORS")
        cast.add(" ")
        cast.addAll(members)
        cast.add(" ")
        cast.add(" ")
        cntMod = (this.end - begin) / cast.size - 1
    }

    override fun reset(newWidth: Int, newHeight: Int) {
        v.clear()
        strH = fm.ascent + fm.descent
        nStrs = (newHeight - 40) / strH + 1
        height = strH * (nStrs - 1) + 48
        index = 0
        gp = GradientPaint(
            0f,
            (newHeight / 2).toFloat(),
            Color.WHITE,
            0f,
            (newHeight + 20).toFloat(),
            Color.BLACK
                                   )
        counter = 0
    }

    override fun step(w: Int, h: Int) {
        if (counter++ % cntMod == 0) {
            if (index < cast.size) {
                v.add(cast[index])
            }
            if ((v.size == nStrs || index >= cast.size) && !v.isEmpty()) {
                v.removeAt(0)
            }
            ++index
        }
    }

    override fun render(w: Int, h: Int, g2: Graphics2D) {
        g2.paint = gp
        g2.font = font
        val remainder = (counter % cntMod).toDouble()
        var incr = 1.0 - remainder / cntMod
        incr = if (incr == 1.0) 0.0 else incr
        var y = (incr * strH).toInt()

        if (index >= cast.size) {
            y += yh
        } else {
            yh = height - v.size * strH + y
            y = yh
        }
        for (s in v) {
            y += strH
            g2.drawString(s, w / 2 - fm.stringWidth(s) / 2, y)
        }
    }

    companion object
    {
        var members = arrayOf(
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

        val font = Font("serif", Font.PLAIN, 26)
        var fm = Intro.Surface.getMetrics(font)
    }
}
