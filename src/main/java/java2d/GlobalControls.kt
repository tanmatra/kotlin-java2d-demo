/*
 *
 * Copyright (c) 2007, 2011, Oracle and/or its affiliates. All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 *   - Redistributions of source code must retain the above copyright
 *     notice, this list of conditions and the following disclaimer.
 *
 *   - Redistributions in binary form must reproduce the above copyright
 *     notice, this list of conditions and the following disclaimer in the
 *     documentation and/or other materials provided with the distribution.
 *
 *   - Neither the name of Oracle nor the names of its
 *     contributors may be used to endorse or promote products derived
 *     from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS
 * IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO,
 * THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
 * PURPOSE ARE DISCLAIMED.  IN NO EVENT SHALL THE COPYRIGHT OWNER OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package java2d

import java.awt.Dimension
import java.awt.GridBagLayout
import javax.swing.JCheckBox
import javax.swing.JComboBox
import javax.swing.JPanel
import javax.swing.JSlider
import javax.swing.SwingConstants
import javax.swing.border.EtchedBorder
import javax.swing.border.TitledBorder
import kotlin.reflect.KProperty

/**
 * Global Controls panel for changing graphic attributes of
 * the demo surface.
 */
class GlobalControls(private val java2Demo: Java2Demo) : JPanel(GridBagLayout())
{
    private val optionsListener = { kProperty: KProperty<*>? ->
        java2Demo.setupSelectedGroup(true, kProperty)
    }

    val antialiasingCheckBox: JCheckBox = addCheckBox("Anti-Aliasing", true, 0)

    val renderCheckBox: JCheckBox = addCheckBox("Rendering Quality", false, 1)

    val textureCheckBox: JCheckBox = addCheckBox("Texture", false, 2)

    val compositeCheckBox: JCheckBox = addCheckBox("AlphaComposite", false, 3)

    private val screenComboBox: JComboBox<String> = JComboBox<String>().apply {
        preferredSize = Dimension(120, 18)
        isLightWeightPopupEnabled = true
        for (s in SCREEN_NAMES) {
            addItem(s)
        }
        addItemListener {
            optionsListener(null)
        }
    }.also {
        add(it, GBC(0, 4).fill())
    }

    val toolBarCheckBox: JCheckBox = addCheckBox("Tools", false, 5)

    val slider = JSlider(SwingConstants.HORIZONTAL, 0, 200, 30).apply {
        fun formatTitle(value: Int) = "Anim delay = $value ms"
        addChangeListener {
            (border as TitledBorder).title = formatTitle(value)
            val index = java2Demo.tabbedPaneIndex - 1
            val demoGroup = java2Demo.groups[index]
            val panel = demoGroup.panel
            for (i in 0 until panel.componentCount) {
                val demoPanel = panel.getComponent(i) as DemoPanel
                demoPanel.tools?.slider?.value = value
            }
            repaint()
        }
        border = TitledBorder(EtchedBorder()).apply {
            title = formatTitle(30)
        }
        minimumSize = Dimension(80, 46)
    }.also {
        add(it, GBC(0, 6).fill().grow())
    }

    val textureChooser = TextureChooser(this, 0).also {
        add(it, GBC(0, 7).fill().grow())
    }

    var selectedScreenIndex: Int
        get() = screenComboBox.selectedIndex
        set(value) { screenComboBox.selectedIndex = value }

    val selectedScreenItem: String?
        get() = screenComboBox.selectedItem as String?

    val options = object : DemoOptions {
        override var toolBar by toolBarCheckBox.selectedProperty(optionsListener)
        override var antialiasing by antialiasingCheckBox.selectedProperty(optionsListener)
        override var renderQuality by renderCheckBox.selectedProperty(optionsListener)
        override var texture by textureCheckBox.selectedProperty(optionsListener)
        override var composite by compositeCheckBox.selectedProperty(optionsListener)
    }

    init {
        border = TitledBorder(EtchedBorder(), "Global Controls")
    }

    private fun addCheckBox(text: String, selected: Boolean, y: Int): JCheckBox {
        return JCheckBox(text, selected).also {
            add(it, GBC(0, y).fill().grow())
        }
    }

    override fun getPreferredSize() = Dimension(135, 260)

    companion object
    {
        internal val SCREEN_NAMES = arrayOf(
            "Auto Screen",
            "On Screen",
            "Off Screen",
            "INT_xRGB",
            "INT_ARGB",
            "INT_ARGB_PRE",
            "INT_BGR",
            "3BYTE_BGR",
            "4BYTE_ABGR",
            "4BYTE_ABGR_PRE",
            "USHORT_565_RGB",
            "USHORT_x555_RGB",
            "BYTE_GRAY",
            "USHORT_GRAY",
            "BYTE_BINARY",
            "BYTE_INDEXED",
            "BYTE_BINARY 2 bit",
            "BYTE_BINARY 4 bit",
            "INT_RGBx",
            "USHORT_555x_RGB")
    }
}
