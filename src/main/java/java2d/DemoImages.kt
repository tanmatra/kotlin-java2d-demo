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

import java.awt.Component
import java.awt.Image
import java.awt.MediaTracker
import java.net.URLClassLoader
import java.util.concurrent.ConcurrentHashMap
import java.util.logging.Level

/**
 * A collection of all the demo images found in the images directory.
 * Certain classes are preloaded; the rest are loaded lazily.
 */
object DemoImages
{
    private val names = arrayOf(
        "java-logo.gif",
        "bld.jpg",
        "boat.png",
        "box.gif",
        "boxwave.gif",
        "clouds.jpg",
        "duke.gif",
        "duke.running.gif",
        "dukeplug.gif",
        "fight.gif",
        "globe.gif",
        "java_logo.png",
        "jumptojavastrip.png",
        "magnify.gif",
        "painting.gif",
        "remove.gif",
        "snooze.gif",
        "star7.gif",
        "surfing.gif",
        "thumbsup.gif",
        "tip.gif",
        "duke.png",
        "print.gif",
        "loop.gif",
        "looping.gif",
        "start.gif",
        "start2.gif",
        "stop.gif",
        "stop2.gif",
        "clone.gif")

    private val cache = ConcurrentHashMap<String, Image>(names.size)

    fun preloadImages(component: Component) {
        for (name in names) {
            cache[name] = getImage(name, component)
        }
    }

    /*
     * Gets the named image using the toolkit of the specified component.
     * Note that this has to work even before we have had a chance to
     * instantiate DemoImages and preload the cache.
     */
    fun getImage(name: String, component: Component): Image {
        var img: Image? = cache[name]
        if (img != null) {
            return img
        }

        val urlLoader = component.javaClass.classLoader as URLClassLoader
        val fileLoc = urlLoader.findResource("images/$name")
        img = component.toolkit.createImage(fileLoc)

        val tracker = MediaTracker(component)
        tracker.addImage(img, 0)
        try {
            tracker.waitForID(0)
            if (tracker.isErrorAny) {
                println("Error loading image $name")
            }
        } catch (ex: Exception) {
            getLogger<DemoImages>().log(Level.SEVERE, null, ex)
        }

        return img
    }
}
