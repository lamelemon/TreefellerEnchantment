package io.github.lamelemon.treefellerEnchantment

import io.github.lamelemon.treefellerEnchantment.commands.LimitlessEnchant
import io.github.lamelemon.treefellerEnchantment.events.TreeBreakEvent
import io.github.lamelemon.treefellerEnchantment.utils.Utils
import io.github.lamelemon.treefellerEnchantment.utils.Utils.configuration
import io.github.lamelemon.treefellerEnchantment.utils.Utils.instance
import org.bukkit.configuration.file.YamlConfiguration
import org.bukkit.enchantments.Enchantment
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

        val enchant = Utils.getEnchant("treefeller:treefeller")
        if (enchant is Enchantment) {
            Utils.enchantment = enchant
        }

        instance = this

        pluginManager.registerEvents(TreeBreakEvent(), this)

        registerCommand("limitlessEnchant", config.getStringList("command-aliases"), LimitlessEnchant())
    }
}
