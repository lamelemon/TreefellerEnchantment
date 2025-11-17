package io.github.lamelemon.treefellerEnchantment

import io.github.lamelemon.treefellerEnchantment.commands.LimitlessEnchant
import io.github.lamelemon.treefellerEnchantment.events.TreeBreakEvent
import io.github.lamelemon.treefellerEnchantment.utils.Utils
import io.github.lamelemon.treefellerEnchantment.utils.Utils.configuration
import org.bukkit.configuration.ConfigurationSection
import org.bukkit.configuration.file.YamlConfiguration
import org.bukkit.plugin.java.JavaPlugin
import java.io.File

class TreefellerEnchantment : JavaPlugin() {

    override fun onEnable() {
        val configFile = File(dataFolder, "config.yml")
        if (!configFile.exists()) {
            saveResource("config.yml", false)
        }

        configuration = YamlConfiguration.loadConfiguration(configFile)
        val pluginManager = server.pluginManager
        if (!configuration.getBoolean("enabled", true)) {
            pluginManager.disablePlugin(this)
        }

        val treeStructure = configuration.getConfigurationSection("tree-structure")
        if (treeStructure is ConfigurationSection) {
            Utils.enchantment = Utils.getEnchant("treefeller:treefeller" + configuration.getString(""))!!

            pluginManager.registerEvents(
                TreeBreakEvent(
                    configuration.getBoolean("has-to-sneak", true),
                    configuration.getInt("block-cap-multiplier", 100),
                ), this)
        } else {
            pluginManager.disablePlugin(this)
        }

        registerCommand("LimitlessEnchant", LimitlessEnchant())
    }
}
