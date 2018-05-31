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
import java.awt.Font
import java.awt.GridBagLayout
import java.awt.event.ItemEvent
import java.awt.event.ItemListener
import javax.swing.JCheckBox
import javax.swing.JComboBox
import javax.swing.JPanel
import javax.swing.JSlider
import javax.swing.SwingConstants
import javax.swing.border.EtchedBorder
import javax.swing.border.TitledBorder
import javax.swing.event.ChangeEvent
import javax.swing.event.ChangeListener

/**
 * Global Controls panel for changing graphic attributes of
 * the demo surface.
 */
class GlobalControls : JPanel(GridBagLayout()), ItemListener, ChangeListener
{
    val textureChooser: TextureChooser
    val antialiasingCheckBox: JCheckBox
    val renderCheckBox: JCheckBox
    val toolBarCheckBox: JCheckBox
    val compositeCheckBox: JCheckBox
    val textureCheckBox: JCheckBox
    val slider: JSlider
    var itemEventSource: Any? = null

    init {
        border = TitledBorder(EtchedBorder(), "Global Controls")

        antialiasingCheckBox = createCheckBox("Anti-Aliasing", true, 0)
        renderCheckBox = createCheckBox("Rendering Quality", false, 1)
        textureCheckBox = createCheckBox("Texture", false, 2)
        compositeCheckBox = createCheckBox("AlphaComposite", false, 3)

        screenComboBox = JComboBox<String>().apply {
            preferredSize = Dimension(120, 18)
            isLightWeightPopupEnabled = true
            font = FONT
            for (s in SCREEN_NAMES) {
                addItem(s)
            }
        }
        screenComboBox.addItemListener(this)
        Java2Demo.addToGridBag(this, screenComboBox, 0, 4, 1, 1, 0.0, 0.0)

        toolBarCheckBox = createCheckBox("Tools", false, 5)

        slider = JSlider(SwingConstants.HORIZONTAL, 0, 200, 30).apply {
            addChangeListener(this@GlobalControls)
            border = TitledBorder(EtchedBorder()).apply {
                titleFont = FONT
                title = "Anim delay = 30 ms"
            }
            minimumSize = Dimension(80, 46)
        }
        Java2Demo.addToGridBag(this, slider, 0, 6, 1, 1, 1.0, 1.0)

        textureChooser = TextureChooser(0)
        Java2Demo.addToGridBag(this, textureChooser, 0, 7, 1, 1, 1.0, 1.0)
    }

    private fun createCheckBox(s: String, b: Boolean, y: Int): JCheckBox {
        val checkBox = JCheckBox(s, b).apply {
            font = FONT
            horizontalAlignment = SwingConstants.LEFT
            addItemListener(this@GlobalControls)
        }
        Java2Demo.addToGridBag(this, checkBox, 0, y, 1, 1, 1.0, 1.0)
        return checkBox
    }

    override fun stateChanged(e: ChangeEvent) {
        val value = slider.value
        (slider.border as TitledBorder).title = "Anim delay = $value ms"
        val index = Java2Demo.tabbedPane.selectedIndex - 1
        val demoGroup = Java2Demo.group[index]
        val panel = demoGroup.panel
        for (i in 0 until panel.componentCount) {
            val demoPanel = panel.getComponent(i) as DemoPanel
            demoPanel.tools?.slider?.value = value
        }
        slider.repaint()
    }

    override fun itemStateChanged(event: ItemEvent) {
        if (Java2Demo.tabbedPane.selectedIndex != 0) {
            itemEventSource = event.source
            val index = Java2Demo.tabbedPane.selectedIndex - 1
            Java2Demo.group[index].setup(true)
            itemEventSource = null
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

        lateinit var screenComboBox: JComboBox<String>

        private val FONT = Font("serif", Font.PLAIN, 12)
    }
}
