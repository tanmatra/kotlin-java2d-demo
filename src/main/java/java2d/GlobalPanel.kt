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

import java.awt.BorderLayout
import java.awt.GridBagLayout
import javax.swing.JPanel
import javax.swing.border.EmptyBorder

/**
 * Panel that holds the Demo groups, Controls and Monitors for each tab.
 * It's a special "always visible" panel for the Controls, MemoryMonitor &
 * PerformanceMonitor.
 */
class GlobalPanel(private val java2Demo: Java2Demo) : JPanel(BorderLayout(5, 5))
{
    private val sidePanel = JPanel(GridBagLayout()).apply {
        border = EmptyBorder(5, 0, 5, 5)
    }
    private var index: Int = 0

    init {
        sidePanel.add(java2Demo.globalControls,
            GBC(0, 0).weight(0.0, 1.0).fillHorizontal().anchor(GBC.PAGE_START))
        sidePanel.add(java2Demo.memoryMonitor,
            GBC(0, 1).weight(0.0, 0.0).fill())
        sidePanel.add(java2Demo.performanceMonitor,
            GBC(0, 2).weight(0.0, 0.0).fill())
        add(java2Demo.intro)
    }

    fun onDemoTabChanged(selectedIndex: Int) {
        java2Demo.groups[index].let { oldGroup ->
            oldGroup.shutDown(oldGroup.activePanel)
        }
        if (selectedIndex == 0) {
            java2Demo.memoryMonitor.surface.stop()
            java2Demo.performanceMonitor.stop()
            removeAll()
            add(java2Demo.intro)
            java2Demo.intro.start()
        } else {
            if (componentCount == 1) {
                java2Demo.intro.stop()
                remove(java2Demo.intro)
                add(sidePanel, BorderLayout.EAST)
                if (java2Demo.isMemoryMonitorVisible) {
                    java2Demo.memoryMonitor.surface.start()
                }
                if (java2Demo.isPerformanceMonitorVisible) {
                    java2Demo.performanceMonitor.start()
                }
            } else {
                remove(java2Demo.groups[index])
            }
            index = selectedIndex - 1
            java2Demo.groups[index].let { newGroup ->
                add(newGroup)
                newGroup.setup(false)
            }
        }
        revalidate()
    }
}
