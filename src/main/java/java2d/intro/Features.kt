package java2d.intro

import java.awt.Font
import java.awt.Graphics2D
import java.util.ArrayList

/**
 * Features of Java2D.  Single character advancement effect.
 */
internal class Features(type: Int, override val begin: Int, override val end: Int) : Part
{
    private val list: Array<String>
    private var strH: Int = 0
    private var endIndex: Int = 0
    private var listIndex: Int = 0
    private val v = ArrayList<String>()

    init {
        list = table[type]
    }

    override fun reset(newWidth: Int, newHeight: Int) {
        strH = fm2.ascent + fm2.descent
        endIndex = 1
        listIndex = 0
        v.clear()
        v.add(list[listIndex].substring(0, endIndex))
    }

    override fun step(w: Int, h: Int) {
        if (listIndex < list.size) {
            if (++endIndex > list[listIndex].length) {
                if (++listIndex < list.size) {
                    endIndex = 1
                    v.add(list[listIndex].substring(0, endIndex))
                }
            } else {
                v[listIndex] = list[listIndex].substring(0, endIndex)
            }
        }
    }

    override fun render(w: Int, h: Int, g2: Graphics2D) {
        g2.color = Intro.WHITE
        g2.font = font1
        g2.drawString(v[0], 90, 85)
        g2.font = font2
        run {
            var i = 1
            var y = 90
            while (i < v.size) {
                y += strH
                g2.drawString(v[i], 120, y)
                i++
            }
        }
    }

    companion object
    {
        const val GRAPHICS = 0
        const val TEXT     = 1
        const val IMAGES   = 2
        const val COLOR    = 3

        val font1 = Font("serif", Font.BOLD, 38)
        val font2 = Font("serif", Font.PLAIN, 24)

        // var fm1 = Surface.getMetrics(font1)
        var fm2 = Intro.Surface.getMetrics(font2)

        var table = arrayOf(
            arrayOf(
                "Graphics",
                "Antialiased rendering",
                "Bezier paths",
                "Transforms",
                "Compositing",
                "Stroking parameters"),
            arrayOf(
                "Text",
                "Extended font support",
                "Advanced text layout",
                "Dynamic font loading",
                "AttributeSets for font customization"),
            arrayOf(
                "Images",
                "Flexible image layouts",
                "Extended imaging operations",
                "   Convolutions, Lookup Tables",
                "RenderableImage interface"),
            arrayOf(
                "Color",
                "ICC profile support",
                "Color conversion",
                "Arbitrary color spaces"))
    }
}
