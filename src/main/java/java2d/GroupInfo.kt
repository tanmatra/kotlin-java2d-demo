package java2d

import java2d.demos.Arcs_Curves.Arcs
import java2d.demos.Arcs_Curves.BezierAnim
import java2d.demos.Arcs_Curves.Curves
import java2d.demos.Arcs_Curves.Ellipses
import java2d.demos.Clipping.Areas
import java2d.demos.Clipping.ClipAnim
import java2d.demos.Clipping.Intersection
import java2d.demos.Clipping.Text
import java2d.demos.Colors.BullsEye
import java2d.demos.Colors.ColorConvert
import java2d.demos.Colors.Rotator3D
import java2d.demos.Composite.ACimages
import java2d.demos.Composite.ACrules
import java2d.demos.Composite.FadeAnim
import java2d.demos.Fonts.AttributedStr
import java2d.demos.Fonts.Highlighting
import java2d.demos.Fonts.Outline
import java2d.demos.Fonts.Tree
import java2d.demos.Images.DukeAnim
import java2d.demos.Images.ImageOps
import java2d.demos.Images.JPEGFlip
import java2d.demos.Images.WarpImage
import java2d.demos.Lines.Caps
import java2d.demos.Lines.Dash
import java2d.demos.Lines.Joins
import java2d.demos.Lines.LineAnim
import java2d.demos.Mix.Balls
import java2d.demos.Mix.BezierScroller
import java2d.demos.Mix.Stars3D
import java2d.demos.Paint.GradAnim
import java2d.demos.Paint.Gradient
import java2d.demos.Paint.Texture
import java2d.demos.Paint.TextureAnim
import java2d.demos.Paths.Append
import java2d.demos.Paths.CurveQuadTo
import java2d.demos.Paths.FillStroke
import java2d.demos.Paths.WindingRule
import java2d.demos.Transforms.Rotate
import java2d.demos.Transforms.SelectTx
import java2d.demos.Transforms.TransformAnim
import java.awt.Component

internal class GroupInfo(
    val groupName: String,
    vararg val classes: Class<out Component>
) {
    companion object
    {
        internal val LIST = arrayOf(
            GroupInfo("Arcs curves",
                Arcs::class.java,
                BezierAnim::class.java,
                Curves::class.java,
                Ellipses::class.java),
            GroupInfo("Clipping",
                Areas::class.java,
                ClipAnim::class.java,
                Intersection::class.java,
                Text::class.java),
            GroupInfo("Colors",
                BullsEye::class.java,
                ColorConvert::class.java,
                Rotator3D::class.java),
            GroupInfo("Composite",
                ACimages::class.java,
                ACrules::class.java,
                FadeAnim::class.java),
            GroupInfo("Fonts",
                AttributedStr::class.java,
                Highlighting::class.java,
                Outline::class.java,
                Tree::class.java),
            GroupInfo("Images",
                DukeAnim::class.java,
                ImageOps::class.java,
                JPEGFlip::class.java,
                WarpImage::class.java),
            GroupInfo("Lines",
                Caps::class.java,
                Dash::class.java,
                Joins::class.java,
                LineAnim::class.java),
            GroupInfo("Mix",
                Balls::class.java,
                BezierScroller::class.java,
                Stars3D::class.java),
            GroupInfo("Paint",
                GradAnim::class.java,
                Gradient::class.java,
                Texture::class.java,
                TextureAnim::class.java),
            GroupInfo("Paths",
                Append::class.java,
                CurveQuadTo::class.java,
                FillStroke::class.java,
                WindingRule::class.java),
            GroupInfo("Transforms",
                Rotate::class.java,
                SelectTx::class.java,
                TransformAnim::class.java))

        fun findByName(name: String): GroupInfo? = LIST.find { it.groupName.equals(name, ignoreCase = true) }
    }
}
