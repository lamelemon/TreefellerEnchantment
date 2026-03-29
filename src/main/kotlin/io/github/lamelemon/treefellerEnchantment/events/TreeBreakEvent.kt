package io.github.lamelemon.treefellerEnchantment.events

import io.github.lamelemon.treefellerEnchantment.TreefellerEnchantment
import io.github.lamelemon.treefellerEnchantment.utils.TreeBreaker
import io.github.lamelemon.treefellerEnchantment.utils.Utils
import io.github.lamelemon.treefellerEnchantment.utils.Utils.configuration
import io.github.lamelemon.treefellerEnchantment.utils.Utils.enchantment
import io.papermc.paper.event.entity.EntityDamageItemEvent
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

    val allowedTrees: HashMap<Material, HashSet<Material>> = HashMap()
    var allowedLeaves: HashSet<Material> = HashSet()
    var currentPlayer: Player? = null
    var hasToSneak: Boolean
    var blockCap: Int
    var maxTreeRadius: Int
    var maxTreeHeight: Int

    init {
        val configurationSection = configuration.getConfigurationSection("materials")
        if (configurationSection is ConfigurationSection) {
            for (key in configurationSection.getKeys(false)) {
                allowedTrees[Material.getMaterial(key) ?: continue] = configurationSection.getStringList(key)
                    .mapTo(HashSet()) {
                        val material = Material.matchMaterial(it)!!
                        allowedLeaves.add(material)
                        return@mapTo material
                    }
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
        if (player.isSneaking != hasToSneak) return
        if (player == currentPlayer) return

        val block = event.block
        val allowedBlocks = allowedTrees[block.type]
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
            maxTreeHeight,
            maxTreeRadius,
            blockCap
        )
        currentPlayer = null
    }

    // Handles leaves not taking item durability
    // As of writing this comment, this event does not function
    // This is probably a paper-side thing because putting a print anywhere does not do anything
    @EventHandler
    fun toolDamageEvent(event: EntityDamageItemEvent) {
        if (event.isCancelled) return
        if (event.isAsynchronous) return // Insurance
        if (event.entity != currentPlayer) return
        if (TreeBreaker.breakingBlock.type in allowedLeaves) {
            event.isCancelled = true
        }
    }
}