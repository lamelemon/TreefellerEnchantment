package io.github.lamelemon.treefellerEnchantment.events

import io.github.lamelemon.treefellerEnchantment.utils.TreeBreaker
import io.github.lamelemon.treefellerEnchantment.utils.Utils.configuration
import io.github.lamelemon.treefellerEnchantment.utils.Utils.enchantment
import org.bukkit.Material
import org.bukkit.configuration.ConfigurationSection
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.util.Vector
import kotlin.math.absoluteValue
import kotlin.math.pow

class TreeBreakEvent: Listener {

    val allowedTrees: HashMap<String, HashSet<Material>> = HashMap()
    var currentPlayer: Player? = null
    var hasToSneak: Boolean
    var blockCap: Int
    var maxTreeRadius: Int
    var maxTreeHeight: Int

    init {
        val configurationSection = configuration.getConfigurationSection("materials")
        if (configurationSection is ConfigurationSection) {
            for (key in configurationSection.getKeys(false)) {
                allowedTrees[key] = configurationSection.getStringList(key)
                    .mapTo(HashSet()) { Material.matchMaterial(it)!! }
            }
        }

        hasToSneak = configuration.getBoolean("has-to-sneak", true)
        blockCap = configuration.getInt("block-cap", 600)
        maxTreeRadius = configuration.getDouble("max-tree-radius", 15.0).pow(2).toInt()
        maxTreeHeight = configuration.getInt("max-tree-height", 50)
    }

    @EventHandler(priority = EventPriority.MONITOR)
    fun blockBreakEvent(event: BlockBreakEvent) {
        if (event.isCancelled) return

        val player = event.player
        if (player.isSneaking != hasToSneak || player == currentPlayer) return

        val block = event.block
        val allowedBlocks = allowedTrees[block.type.toString()]
        if (allowedBlocks !is HashSet<Material>) return

        val currentTool = player.inventory.itemInMainHand
        if (!currentTool.containsEnchantment(enchantment)) return

        currentPlayer = player
        TreeBreaker(
            block,
            player,
            block.type,
            Vector(block.x.absoluteValue, block.y.absoluteValue, block.z.absoluteValue),
            allowedBlocks,
            currentTool,
            maxTreeHeight,
            maxTreeRadius,
            blockCap
        )
        currentPlayer = null
    }
}