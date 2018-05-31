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
class GlobalControls : JPanel(), ItemListener, ChangeListener
{
    val texturechooser: TextureChooser
    val aliasCB: JCheckBox
    val renderCB: JCheckBox
    val toolBarCB: JCheckBox
    val compositeCB: JCheckBox
    val textureCheckBox: JCheckBox
    val slider: JSlider
    var obj: Any? = null

    init {
        layout = GridBagLayout()
        border = TitledBorder(EtchedBorder(), "Global Controls")

        aliasCB = createCheckBox("Anti-Aliasing", true, 0)
        renderCB = createCheckBox("Rendering Quality", false, 1)
        textureCheckBox = createCheckBox("Texture", false, 2)
        compositeCB = createCheckBox("AlphaComposite", false, 3)

        screenCombo = JComboBox<String>().apply {
            preferredSize = Dimension(120, 18)
            isLightWeightPopupEnabled = true
            font = FONT
            for (s in screenNames) {
                addItem(s)
            }
        }
        screenCombo.addItemListener(this)
        Java2Demo.addToGridBag(this, screenCombo, 0, 4, 1, 1, 0.0, 0.0)

        toolBarCB = createCheckBox("Tools", false, 5)

        slider = JSlider(SwingConstants.HORIZONTAL, 0, 200, 30).apply {
            addChangeListener(this@GlobalControls)
            border = TitledBorder(EtchedBorder()).apply {
                titleFont = FONT
                title = "Anim delay = 30 ms"
            }
            minimumSize = Dimension(80, 46)
        }
        Java2Demo.addToGridBag(this, slider, 0, 6, 1, 1, 1.0, 1.0)

        texturechooser = TextureChooser(0)
        Java2Demo.addToGridBag(this, texturechooser, 0, 7, 1, 1, 1.0, 1.0)
    }

    private fun createCheckBox(s: String, b: Boolean, y: Int): JCheckBox {
        val cb = JCheckBox(s, b).apply {
            font = FONT
            horizontalAlignment = SwingConstants.LEFT
            addItemListener(this@GlobalControls)
        }
        Java2Demo.addToGridBag(this, cb, 0, y, 1, 1, 1.0, 1.0)
        return cb
    }

    override fun stateChanged(e: ChangeEvent) {
        val value = slider.value
        val tb = slider.border as TitledBorder
        tb.title = "Anim delay = " + value.toString() + " ms"
        val index = Java2Demo.tabbedPane.selectedIndex - 1
        val dg = Java2Demo.group[index]
        val p = dg.panel
        for (i in 0 until p.componentCount) {
            val dp = p.getComponent(i) as DemoPanel
            if (dp.tools != null && dp.tools.slider != null) {
                dp.tools.slider!!.value = value
            }
        }
        slider.repaint()
    }

    override fun itemStateChanged(e: ItemEvent) {
        if (Java2Demo.tabbedPane.selectedIndex != 0) {
            obj = e.source
            val index = Java2Demo.tabbedPane.selectedIndex - 1
            Java2Demo.group[index].setup(true)
            obj = null
        }
    }

    override fun getPreferredSize(): Dimension {
        return Dimension(135, 260)
    }

    companion object
    {
        internal val screenNames = arrayOf(
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

        lateinit var screenCombo: JComboBox<String>

        private val FONT = Font("serif", Font.PLAIN, 12)
    }
}
