package java2d

import java.awt.GridBagConstraints
import java.awt.Insets

fun gbc() = GridBagConstraints()

fun gbc(x: Int, y: Int): GridBagConstraints = GridBagConstraints().apply {
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
fun GridBagConstraints.fillVertical(): GridBagConstraints = fill(GridBagConstraints.VERTICAL)

fun GridBagConstraints.fillHorizontal(): GridBagConstraints = fill(GridBagConstraints.HORIZONTAL)

fun GridBagConstraints.span(x: Int, y: Int): GridBagConstraints {
    gridwidth = x
    gridheight = y
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

fun GridBagConstraints.insets(top: Int, left: Int, bottom: Int, right: Int): GridBagConstraints {
    insets = Insets(top, left, bottom, right)
    return this
}
