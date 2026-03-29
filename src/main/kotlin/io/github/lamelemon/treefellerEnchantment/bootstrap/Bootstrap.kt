@file:Suppress("UnstableApiUsage")

package io.github.lamelemon.treefellerEnchantment.bootstrap

import io.papermc.paper.plugin.bootstrap.BootstrapContext
import io.papermc.paper.plugin.bootstrap.PluginBootstrap
import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents
import java.io.IOException
import java.net.URI
import java.net.URISyntaxException


@SuppressWarnings
class Bootstrap: PluginBootstrap {

    override fun bootstrap(context: BootstrapContext) {
        context.lifecycleManager.registerEventHandler(LifecycleEvents.DATAPACK_DISCOVERY.newHandler { event ->
            try {
                // Retrieve the URI of the datapack folder.
                val uri: URI = this.javaClass.getResource("/datapack")!!.toURI()
                // Discover the pack. The ID is set to "provided", which indicates to
                // a server owner that your plugin includes this data pack.
                event.registrar().discoverPack(uri, "provided")
            } catch (e: URISyntaxException) {
                throw RuntimeException(e)
            } catch (e: IOException) {
                throw RuntimeException(e)
            }
        })
    }
}