package java2d.intro

import java.awt.Color
import java.awt.Font
import java.awt.GradientPaint
import java.util.ArrayList

/**
 * Director is the holder of the scenes, their names & pause amounts
 * between scenes.
 */
internal class Director(surface: Intro.Surface) : ArrayList<Scene>()
{
    private var gp = GradientPaint(0f, 40f, Intro.BLUE, 38f, 2f, Intro.BLACK)
    private var f1 = Font("serif", Font.PLAIN, 200)
    private var f2 = Font("serif", Font.PLAIN, 120)
    private var f3 = Font("serif", Font.PLAIN, 72)

    private var partsInfo = arrayOf(
        Scene("J  -  scale text on gradient", 0,
            arrayOf(
                GpE(GpE.BURI, Intro.BLACK, Intro.BLUE, 0, 20),
                TxE("J", f1, TxE.SCI, Intro.YELLOW, 2, 20))),
        Scene("2  -  scale & rotate text on gradient", 0,
            arrayOf(
                GpE(GpE.BURI, Intro.BLUE, Intro.BLACK, 0, 22),
                TxE("2", f1, TxE.RI or TxE.SCI, Intro.YELLOW, 2, 22))),
        Scene("D  -  scale text on gradient", 0,
            arrayOf(
                GpE(GpE.BURI, Intro.BLACK, Intro.BLUE, 0, 20),
                TxE("D", f1, TxE.SCI, Intro.YELLOW, 2, 20))),
        Scene("Java2D  -  scale & rotate text on gradient", 1000,
            arrayOf(
                GpE(GpE.SIH, Intro.BLUE, Intro.BLACK, 0, 40),
                TxE("Java2D", f2, TxE.RI or TxE.SCI, Intro.YELLOW, 0, 40))),
        Scene("Previous scene dither dissolve out", 0,
            arrayOf(
                DdE(0, 20, 1))),
        Scene("Graphics Features", 999,
            arrayOf(
                Temp(Temp.RECT, null, 0, 15),
                Temp(Temp.IMG, surface.javaLogo, 2, 15),
                Temp(Temp.RNA or Temp.INA, surface.javaLogo, 16, 130),
                Features(Features.GRAPHICS, 16, 130))),
        Scene("Java2D  -  texture text on gradient", 1000,
            arrayOf(
                GpE(GpE.WI, Intro.BLUE, Intro.BLACK, 0, 20),
                GpE(GpE.WD, Intro.BLUE, Intro.BLACK, 21, 40),
                TpE(TpE.OI or TpE.NF, Intro.BLACK, Intro.YELLOW, 4, 0, 10),
                TpE(TpE.OD or TpE.NF, Intro.BLACK, Intro.YELLOW, 4, 11, 20),
                TpE(TpE.OI or TpE.NF or TpE.HAF, Intro.BLACK, Intro.YELLOW, 5, 21, 40),
                TxE("Java2D", f2, 0, null, 0, 40))),
        Scene("Previous scene random close out", 0,
            arrayOf(
                CoE(CoE.RAND, 0, 20))),
        Scene("Text Features", 999,
            arrayOf(
                Temp(Temp.RECT, null, 0, 15),
                Temp(Temp.IMG, surface.javaLogo, 2, 15),
                Temp(Temp.RNA or Temp.INA, surface.javaLogo, 16, 130),
                Features(Features.TEXT, 16, 130))),
        Scene("Java2D  -  composite text on texture", 1000,
            arrayOf(
                TpE(TpE.RI, Intro.BLACK, gp, 40, 0, 20),
                TpE(TpE.RD, Intro.BLACK, gp, 40, 21, 40),
                TpE(TpE.RI, Intro.BLACK, gp, 40, 41, 60),
                TxE("Java2D", f2, TxE.AC, Intro.YELLOW, 0, 60))),
        Scene("Previous scene dither dissolve out", 0,
            arrayOf(
                DdE(0, 20, 4))),
        Scene("Imaging Features", 999,
            arrayOf(
                Temp(Temp.RECT, null, 0, 15),
                Temp(Temp.IMG, surface.javaLogo, 2, 15),
                Temp(Temp.RNA or Temp.INA, surface.javaLogo, 16, 130),
                Features(Features.IMAGES, 16, 130))),
        Scene("Java2D  -  text on gradient", 1000,
            arrayOf(
                GpE(GpE.SDH, Intro.BLUE, Intro.BLACK, 0, 20),
                GpE(GpE.SIH, Intro.BLUE, Intro.BLACK, 21, 40),
                GpE(GpE.SDH, Intro.BLUE, Intro.BLACK, 41, 50),
                GpE(GpE.INC or GpE.NF, Intro.RED, Intro.YELLOW, 0, 50),
                TxE("Java2D", f2, TxE.NOP, null, 0, 50))),
        Scene("Previous scene ellipse close out", 0,
            arrayOf(
                CoE(CoE.OVAL, 0, 20))),
        Scene("Color Features", 999,
            arrayOf(
                Temp(Temp.RECT, null, 0, 15),
                Temp(Temp.IMG, surface.javaLogo, 2, 15),
                Temp(Temp.RNA or Temp.INA, surface.javaLogo, 16, 99),
                Features(Features.COLOR, 16, 99))),
        Scene("Java2D  -  composite and rotate text on paints", 2000,
            arrayOf(
                GpE(GpE.BURI, Intro.BLACK, Intro.BLUE, 0, 20),
                GpE(GpE.BURD, Intro.BLACK, Intro.BLUE, 21, 30),
                TpE(TpE.OI or TpE.HAF, Intro.BLACK, Intro.BLUE, 10, 31, 40),
                TxE("Java2D", f2, TxE.AC or TxE.RI, Intro.YELLOW, 0, 40))),
        Scene("Previous scene subimage transform out", 0,
            arrayOf(
                SiE(60, 60, 0, 40))),
        Scene("CREDITS  -  transform in", 1000,
            arrayOf(
                LnE(LnE.ACI or LnE.ZOOMI or LnE.RI, 0, 60),
                TxE("CREDITS", f3, TxE.AC or TxE.SCI, Color.RED, 20, 30),
                TxE("CREDITS", f3, TxE.SCXD, Color.RED, 31, 38),
                TxE("CREDITS", f3, TxE.SCXI, Color.RED, 39, 48),
                TxE("CREDITS", f3, TxE.SCXD, Color.RED, 49, 54),
                TxE("CREDITS", f3, TxE.SCXI, Color.RED, 55, 60))),
        Scene("CREDITS  -  transform out", 0,
            arrayOf(
                LnE(LnE.ACD or LnE.ZOOMD or LnE.RD, 0, 45),
                TxE("CREDITS", f3, 0, Color.RED, 0, 9),
                TxE("CREDITS", f3, TxE.SCD or TxE.RD, Color.RED, 10, 30))),
        Scene("Contributors", 1000,
            arrayOf(
                Temp(Temp.RECT, null, 0, 30),
                Temp(Temp.IMG, surface.cupAnimation, 4, 30),
                Temp(Temp.RNA or Temp.INA, surface.cupAnimation, 31, 200),
                Contributors(34, 200))))

    init {
        addAll(partsInfo.asList()) //TODO
    }
}
