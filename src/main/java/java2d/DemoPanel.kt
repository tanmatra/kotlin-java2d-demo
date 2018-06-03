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

import java2d.CustomControlsContext.State.START
import java2d.CustomControlsContext.State.STOP
import java.awt.BorderLayout
import java.awt.Component
import javax.swing.JPanel
import javax.swing.border.BevelBorder
import javax.swing.border.CompoundBorder
import javax.swing.border.EmptyBorder
import javax.swing.border.SoftBevelBorder

/**
 * The panel for the Surface, Custom Controls & Tools.
 * Other component types welcome.
 */
class DemoPanel private constructor(
    val className: String,
    component: Component?
) : JPanel(BorderLayout())
{
    val surface: Surface?
    val customControlsContext: CustomControlsContext?
    val tools: Tools?

    constructor(className: String) : this(className, loadComponent(className))

    constructor(component: Component) : this(component.javaClass.name, component)

    init {
        if (component != null) {
            add(component)
        }
        if (component is Surface) {
            surface = component
            tools = Tools(component)
            add(tools, BorderLayout.SOUTH)
        } else {
            surface = null
            tools = null
        }
        if (component is CustomControlsContext) {
            customControlsContext = component
            val controls = component.controls
            val constrainst = component.constraints
            for (i in controls.indices) {
                add(controls[i], constrainst[i])
            }
        } else {
            customControlsContext = null
        }
    }

    fun clone() = DemoPanel(className)

    fun start() {
        surface?.startClock()
        if (tools != null && surface != null) {
            if (tools.startStopButton != null && tools.startStopButton!!.isSelected) {
                surface.animating!!.start()
            }
        }
        if (customControlsContext != null && Java2Demo.ccthreadCB != null && Java2Demo.ccthreadCB.isSelected) {
            customControlsContext.handleThread(START)
        }
    }

    fun stop() {
        if (surface != null) {
            surface.animating?.stop()
            surface.bufferedImage = null
        }
        customControlsContext?.handleThread(STOP)
    }

    fun setDemoBorder(panel: JPanel) {
        val top = if (panel.componentCount + 1 >= 3) 0 else 5
        val left = if ((panel.componentCount + 1) % 2 == 0) 0 else 5
        border = CompoundBorder(EmptyBorder(top, left, 5, 5), SoftBevelBorder(BevelBorder.RAISED))
    }

    companion object {
        private fun loadComponent(className: String): Component? {
            return Class.forName(className).newInstance() as? Component
        }
    }
}
