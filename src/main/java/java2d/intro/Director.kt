package java2d.intro

import java.awt.Color
import java.awt.Font
import java.awt.GradientPaint
import java.util.ArrayList

/**
 * Director is the holder of the scenes, their names & pause amounts
 * between scenes.
 */
internal class Director : ArrayList<Intro.Surface.Scene>()
{
    private var gp = GradientPaint(
        0f, 40f,
        Intro.myBlue, 38f, 2f,
        Intro.myBlack
                                           )
    private var f1 = Font("serif", Font.PLAIN, 200)
    private var f2 = Font("serif", Font.PLAIN, 120)
    private var f3 = Font("serif", Font.PLAIN, 72)

    private var partsInfo = arrayOf(
        arrayOf(
            arrayOf<Any>("J  -  scale text on gradient", "0"),
            arrayOf<Any>(
                Intro.Surface.GpE(
                    Intro.Surface.GpE.BURI,
                    Intro.myBlack,
                    Intro.myBlue,
                    0,
                    20
                                 ),
                Intro.Surface.TxE(
                    "J",
                    f1,
                    Intro.Surface.TxE.SCI,
                    Intro.myYellow,
                    2,
                    20
                                 )
                        )),
        arrayOf(
            arrayOf<Any>("2  -  scale & rotate text on gradient", "0"),
            arrayOf<Any>(
                Intro.Surface.GpE(
                    Intro.Surface.GpE.BURI,
                    Intro.myBlue,
                    Intro.myBlack,
                    0,
                    22
                                 ),
                Intro.Surface.TxE(
                    "2",
                    f1,
                    Intro.Surface.TxE.RI or Intro.Surface.TxE.SCI,
                    Intro.myYellow,
                    2,
                    22
                                 )
                        )),
        arrayOf(
            arrayOf<Any>("D  -  scale text on gradient", "0"),
            arrayOf<Any>(
                Intro.Surface.GpE(
                    Intro.Surface.GpE.BURI,
                    Intro.myBlack,
                    Intro.myBlue,
                    0,
                    20
                                 ),
                Intro.Surface.TxE(
                    "D",
                    f1,
                    Intro.Surface.TxE.SCI,
                    Intro.myYellow,
                    2,
                    20
                                 )
                        )),
        arrayOf(
            arrayOf<Any>("Java2D  -  scale & rotate text on gradient", "1000"),
            arrayOf<Any>(
                Intro.Surface.GpE(
                    Intro.Surface.GpE.SIH,
                    Intro.myBlue,
                    Intro.myBlack,
                    0,
                    40
                                 ),
                Intro.Surface.TxE(
                    "Java2D",
                    f2,
                    Intro.Surface.TxE.RI or Intro.Surface.TxE.SCI,
                    Intro.myYellow,
                    0,
                    40
                                 )
                        )),
        arrayOf(arrayOf<Any>("Previous scene dither dissolve out", "0"), arrayOf<Any>(
            Intro.Surface.DdE(
                0,
                20,
                1
                             )
                                                                                     )),
        arrayOf(
            arrayOf<Any>("Graphics Features", "999"),
            arrayOf<Any>(
                Intro.Surface.Temp(Intro.Surface.Temp.RECT, null, 0, 15),
                Intro.Surface.Temp(
                    Intro.Surface.Temp.IMG,
                    Intro.Surface.java_logo,
                    2,
                    15
                                  ),
                Intro.Surface.Temp(
                    Intro.Surface.Temp.RNA or Intro.Surface.Temp.INA,
                    Intro.Surface.java_logo,
                    16,
                    130
                                  ),
                Intro.Surface.Features(
                    Intro.Surface.Features.GRAPHICS,
                    16,
                    130
                                      )
                        )),
        arrayOf(
            arrayOf<Any>("Java2D  -  texture text on gradient", "1000"), arrayOf<Any>(
                Intro.Surface.GpE(
                    Intro.Surface.GpE.WI,
                    Intro.myBlue,
                    Intro.myBlack,
                    0,
                    20
                                 ),
                Intro.Surface.GpE(
                    Intro.Surface.GpE.WD,
                    Intro.myBlue,
                    Intro.myBlack,
                    21,
                    40
                                 ),
                Intro.Surface.TpE(
                    Intro.Surface.TpE.OI or Intro.Surface.TpE.NF,
                    Intro.myBlack,
                    Intro.myYellow,
                    4,
                    0,
                    10
                                 ),
                Intro.Surface.TpE(
                    Intro.Surface.TpE.OD or Intro.Surface.TpE.NF,
                    Intro.myBlack,
                    Intro.myYellow,
                    4,
                    11,
                    20
                                 ),
                Intro.Surface.TpE(
                    Intro.Surface.TpE.OI or Intro.Surface.TpE.NF or Intro.Surface.TpE.HAF,
                    Intro.myBlack,
                    Intro.myYellow,
                    5,
                    21,
                    40
                                 ),
                Intro.Surface.TxE("Java2D", f2, 0, null, 0, 40)
                                                                                     )),
        arrayOf(arrayOf<Any>("Previous scene random close out", "0"), arrayOf<Any>(
            Intro.Surface.CoE(
                Intro.Surface.CoE.RAND,
                0,
                20
                             )
                                                                                  )),
        arrayOf(
            arrayOf<Any>("Text Features", "999"),
            arrayOf<Any>(
                Intro.Surface.Temp(Intro.Surface.Temp.RECT, null, 0, 15),
                Intro.Surface.Temp(
                    Intro.Surface.Temp.IMG,
                    Intro.Surface.java_logo,
                    2,
                    15
                                  ),
                Intro.Surface.Temp(
                    Intro.Surface.Temp.RNA or Intro.Surface.Temp.INA,
                    Intro.Surface.java_logo,
                    16,
                    130
                                  ),
                Intro.Surface.Features(Intro.Surface.Features.TEXT, 16, 130)
                        )),
        arrayOf(
            arrayOf<Any>("Java2D  -  composite text on texture", "1000"),
            arrayOf<Any>(
                Intro.Surface.TpE(
                    Intro.Surface.TpE.RI,
                    Intro.myBlack,
                    gp,
                    40,
                    0,
                    20
                                 ),
                Intro.Surface.TpE(
                    Intro.Surface.TpE.RD,
                    Intro.myBlack,
                    gp,
                    40,
                    21,
                    40
                                 ),
                Intro.Surface.TpE(
                    Intro.Surface.TpE.RI,
                    Intro.myBlack,
                    gp,
                    40,
                    41,
                    60
                                 ),
                Intro.Surface.TxE(
                    "Java2D",
                    f2,
                    Intro.Surface.TxE.AC,
                    Intro.myYellow,
                    0,
                    60
                                 )
                        )),
        arrayOf(arrayOf<Any>("Previous scene dither dissolve out", "0"), arrayOf<Any>(
            Intro.Surface.DdE(
                0,
                20,
                4
                             )
                                                                                     )),
        arrayOf(
            arrayOf<Any>("Imaging Features", "999"),
            arrayOf<Any>(
                Intro.Surface.Temp(Intro.Surface.Temp.RECT, null, 0, 15),
                Intro.Surface.Temp(
                    Intro.Surface.Temp.IMG,
                    Intro.Surface.java_logo,
                    2,
                    15
                                  ),
                Intro.Surface.Temp(
                    Intro.Surface.Temp.RNA or Intro.Surface.Temp.INA,
                    Intro.Surface.java_logo,
                    16,
                    130
                                  ),
                Intro.Surface.Features(
                    Intro.Surface.Features.IMAGES,
                    16,
                    130
                                      )
                        )),
        arrayOf(
            arrayOf<Any>("Java2D  -  text on gradient", "1000"),
            arrayOf<Any>(
                Intro.Surface.GpE(
                    Intro.Surface.GpE.SDH,
                    Intro.myBlue,
                    Intro.myBlack,
                    0,
                    20
                                 ),
                Intro.Surface.GpE(
                    Intro.Surface.GpE.SIH,
                    Intro.myBlue,
                    Intro.myBlack,
                    21,
                    40
                                 ),
                Intro.Surface.GpE(
                    Intro.Surface.GpE.SDH,
                    Intro.myBlue,
                    Intro.myBlack,
                    41,
                    50
                                 ),
                Intro.Surface.GpE(
                    Intro.Surface.GpE.INC or Intro.Surface.GpE.NF,
                    Intro.myRed,
                    Intro.myYellow,
                    0,
                    50
                                 ),
                Intro.Surface.TxE(
                    "Java2D",
                    f2,
                    Intro.Surface.TxE.NOP,
                    null,
                    0,
                    50
                                 )
                        )),
        arrayOf(
            arrayOf<Any>("Previous scene ellipse close out", "0"),
            arrayOf<Any>(Intro.Surface.CoE(Intro.Surface.CoE.OVAL, 0, 20))),
        arrayOf(
            arrayOf<Any>("Color Features", "999"),
            arrayOf<Any>(
                Intro.Surface.Temp(Intro.Surface.Temp.RECT, null, 0, 15),
                Intro.Surface.Temp(
                    Intro.Surface.Temp.IMG,
                    Intro.Surface.java_logo,
                    2,
                    15
                                  ),
                Intro.Surface.Temp(
                    Intro.Surface.Temp.RNA or Intro.Surface.Temp.INA,
                    Intro.Surface.java_logo,
                    16,
                    99
                                  ),
                Intro.Surface.Features(Intro.Surface.Features.COLOR, 16, 99)
                        )),
        arrayOf(
            arrayOf<Any>("Java2D  -  composite and rotate text on paints", "2000"),
            arrayOf<Any>(
                Intro.Surface.GpE(
                    Intro.Surface.GpE.BURI,
                    Intro.myBlack,
                    Intro.myBlue,
                    0,
                    20
                                 ),
                Intro.Surface.GpE(
                    Intro.Surface.GpE.BURD,
                    Intro.myBlack,
                    Intro.myBlue,
                    21,
                    30
                                 ),
                Intro.Surface.TpE(
                    Intro.Surface.TpE.OI or Intro.Surface.TpE.HAF,
                    Intro.myBlack,
                    Intro.myBlue,
                    10,
                    31,
                    40
                                 ),
                Intro.Surface.TxE(
                    "Java2D",
                    f2,
                    Intro.Surface.TxE.AC or Intro.Surface.TxE.RI,
                    Intro.myYellow,
                    0,
                    40
                                 )
                        )),
        arrayOf(
            arrayOf<Any>("Previous scene subimage transform out", "0"),
            arrayOf<Any>(Intro.Surface.SiE(60, 60, 0, 40))),
        arrayOf(
            arrayOf<Any>("CREDITS  -  transform in", "1000"),
            arrayOf<Any>(
                Intro.Surface.LnE(
                    Intro.Surface.LnE.ACI or Intro.Surface.LnE.ZOOMI or Intro.Surface.LnE.RI,
                    0,
                    60
                                 ),
                Intro.Surface.TxE(
                    "CREDITS",
                    f3,
                    Intro.Surface.TxE.AC or Intro.Surface.TxE.SCI,
                    Color.RED,
                    20,
                    30
                                 ),
                Intro.Surface.TxE(
                    "CREDITS",
                    f3,
                    Intro.Surface.TxE.SCXD,
                    Color.RED,
                    31,
                    38
                                 ),
                Intro.Surface.TxE(
                    "CREDITS",
                    f3,
                    Intro.Surface.TxE.SCXI,
                    Color.RED,
                    39,
                    48
                                 ),
                Intro.Surface.TxE(
                    "CREDITS",
                    f3,
                    Intro.Surface.TxE.SCXD,
                    Color.RED,
                    49,
                    54
                                 ),
                Intro.Surface.TxE(
                    "CREDITS",
                    f3,
                    Intro.Surface.TxE.SCXI,
                    Color.RED,
                    55,
                    60
                                 )
                        )),
        arrayOf(
            arrayOf<Any>("CREDITS  -  transform out", "0"),
            arrayOf<Any>(
                Intro.Surface.LnE(
                    Intro.Surface.LnE.ACD or Intro.Surface.LnE.ZOOMD or Intro.Surface.LnE.RD,
                    0,
                    45
                                 ),
                Intro.Surface.TxE("CREDITS", f3, 0, Color.RED, 0, 9),
                Intro.Surface.TxE(
                    "CREDITS",
                    f3,
                    Intro.Surface.TxE.SCD or Intro.Surface.TxE.RD,
                    Color.RED,
                    10,
                    30
                                 )
                        )),
        arrayOf(
            arrayOf<Any>("Contributors", "1000"),
            arrayOf<Any>(
                Intro.Surface.Temp(Intro.Surface.Temp.RECT, null, 0, 30),
                Intro.Surface.Temp(
                    Intro.Surface.Temp.IMG,
                    Intro.Surface.cupanim,
                    4,
                    30
                                  ),
                Intro.Surface.Temp(
                    Intro.Surface.Temp.RNA or Intro.Surface.Temp.INA,
                    Intro.Surface.cupanim,
                    31,
                    200
                                  ),
                Intro.Surface.Contributors(34, 200)
                        )))

    init {
        for (partInfo in partsInfo) {
            val parts = ArrayList<Intro.Surface.Part>()
            for (part in partInfo[1]) {
                parts.add(part as Intro.Surface.Part)
            }
            add(Intro.Surface.Scene(parts, partInfo[0][0], partInfo[0][1]))
        }
    }
}
