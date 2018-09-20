@file:Suppress("unused")

package java2d

import java.awt.GridBagConstraints
import java.awt.Insets

typealias GBC = GridBagConstraints

@Suppress("FunctionName")
fun GBC(x: Int, y: Int): GridBagConstraints = GridBagConstraints().apply {
    gridx = x
    gridy = y
}

fun GridBagConstraints.at(x: Int, y: Int): GridBagConstraints = apply {
    gridx = x
    gridy = y
}

fun GridBagConstraints.fill(value: Int = GridBagConstraints.BOTH): GridBagConstraints = apply {
    fill = value
}

fun GridBagConstraints.noFill(): GridBagConstraints = fill(GridBagConstraints.NONE)

fun GridBagConstraints.fillVertical(): GridBagConstraints = fill(GridBagConstraints.VERTICAL)

fun GridBagConstraints.fillHorizontal(): GridBagConstraints = fill(GridBagConstraints.HORIZONTAL)

fun GridBagConstraints.span(x: Int, y: Int): GridBagConstraints = apply {
    gridwidth = x
    gridheight = y
}

fun GridBagConstraints.anchor(value: Int): GridBagConstraints = apply {
    anchor = value
}

fun GridBagConstraints.pad(x: Int, y: Int): GridBagConstraints = apply {
    ipadx = x
    ipady = y
}

fun GridBagConstraints.weight(x: Double, y: Double): GridBagConstraints = apply {
    weightx = x
    weighty = y
}

fun GridBagConstraints.grow(): GridBagConstraints = apply {
    weightx = 1.0
    weighty = 1.0
}

fun GridBagConstraints.growHorizontal(): GridBagConstraints = apply {
    weightx = 1.0
    weighty = 0.0
}

fun GridBagConstraints.growVertical(): GridBagConstraints = apply {
    weightx = 0.0
    weighty = 1.0
}

fun GridBagConstraints.noGrow(): GridBagConstraints = apply {
    weightx = 0.0
    weighty = 0.0
}

fun GridBagConstraints.insets(top: Int, left: Int, bottom: Int, right: Int): GridBagConstraints = apply {
    insets = Insets(top, left, bottom, right)
}
