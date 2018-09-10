package java2d

import java.awt.GridBagConstraints
import java.awt.Insets

typealias GBC = GridBagConstraints

fun GBC(x: Int, y: Int): GridBagConstraints = GridBagConstraints().apply {
    gridx = x
    gridy = y
}

fun GridBagConstraints.at(x: Int, y: Int): GridBagConstraints {
    gridx = x
    gridy = y
    return this
}

fun GridBagConstraints.fill(value: Int = GridBagConstraints.BOTH): GridBagConstraints {
    fill = value
    return this
}

fun GridBagConstraints.noFill(): GridBagConstraints = fill(GridBagConstraints.NONE)

fun GridBagConstraints.fillVertical(): GridBagConstraints = fill(GridBagConstraints.VERTICAL)

fun GridBagConstraints.fillHorizontal(): GridBagConstraints = fill(GridBagConstraints.HORIZONTAL)

fun GridBagConstraints.span(x: Int, y: Int): GridBagConstraints {
    gridwidth = x
    gridheight = y
    return this
}

fun GridBagConstraints.anchor(value: Int): GridBagConstraints {
    anchor = value
    return this
}

fun GridBagConstraints.pad(x: Int, y: Int): GridBagConstraints {
    ipadx = x
    ipady = y
    return this
}

fun GridBagConstraints.weight(x: Double, y: Double): GridBagConstraints {
    weightx = x
    weighty = y
    return this
}

fun GridBagConstraints.grow(): GridBagConstraints {
    weightx = 1.0
    weighty = 1.0
    return this
}

fun GridBagConstraints.growHorizontal(): GridBagConstraints {
    weightx = 1.0
    weighty = 0.0
    return this
}

fun GridBagConstraints.growVertical(): GridBagConstraints {
    weightx = 0.0
    weighty = 1.0
    return this
}

fun GridBagConstraints.noGrow(): GridBagConstraints {
    weightx = 0.0
    weighty = 0.0
    return this
}

fun GridBagConstraints.insets(top: Int, left: Int, bottom: Int, right: Int): GridBagConstraints {
    insets = Insets(top, left, bottom, right)
    return this
}
