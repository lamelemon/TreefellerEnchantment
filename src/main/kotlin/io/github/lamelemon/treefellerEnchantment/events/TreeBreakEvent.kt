package io.github.lamelemon.treefellerEnchantment.events

import io.github.lamelemon.treefellerEnchantment.utils.Utils
import io.github.lamelemon.treefellerEnchantment.utils.Utils.configuration
import io.github.lamelemon.treefellerEnchantment.utils.Utils.enchantment
import org.bukkit.Material
import org.bukkit.block.Block
import org.bukkit.configuration.ConfigurationSection
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.util.Vector
import org.joml.Vector2d
import kotlin.math.absoluteValue

class TreeBreakEvent: Listener {

    var blocksLeft: Int = 0
    var currentPlayer: Player? = null
    val allowedTrees: HashMap<String, HashSet<Material>> = HashMap()
    var hasToSneak = true
    var blockCap = 500
    var maxTreeRadius = 15
    var maxTreeHeight = 50

    init {
        val configurationSection = configuration.getConfigurationSection("materials")
        if (configurationSection is ConfigurationSection) {
            for (key in configurationSection.getKeys(false)) {
                allowedTrees[key] = configurationSection.getStringList(key)
                    .mapTo(HashSet()) { Material.matchMaterial(it)!! }
            }
        }

        hasToSneak = configuration.getBoolean("has-to-sneak", true)
        blockCap = configuration.getInt("block-cap", 500)
        maxTreeRadius = configuration.getInt("max-tree-radius", 15)
        maxTreeHeight = configuration.getInt("max-tree-height", 50)
    }

    @EventHandler(priority = EventPriority.MONITOR)
    fun blockBreakEvent(event: BlockBreakEvent) {
        if (event.isCancelled) return

        val player = event.player
        if (player.isSneaking != hasToSneak || player == currentPlayer) return

        val currentTool = player.inventory.itemInMainHand
        if (!currentTool.containsEnchantment(enchantment)) return

        val block = event.block
        val allowedBlocks = allowedTrees[block.type.toString()]
        if (allowedBlocks is HashSet<Material>) {
            currentPlayer = player // Cache player
            blocksLeft = blockCap
            treeFeller(block,
                block.type,
                block.type,
                block.location.toVector(),
                allowedBlocks
            )
            currentPlayer = null
        }
    }

    fun treeFeller(block: Block, lastBlockType: Material, firstMaterial: Material, firstCords: Vector, allowedBlocks: HashSet<Material>): Boolean {
        if (blocksLeft <= 0) return false
        if (block.isEmpty) return true

        if (block.type == firstMaterial && lastBlockType != firstMaterial) return true // Block is a log but last block isn't
        if (block.type !in allowedBlocks && block.type != firstMaterial) return true // Block isn't allowed

        if ((block.y - firstCords.y).absoluteValue > maxTreeHeight) return true
        if (Vector2d(block.x - firstCords.x, block.z - firstCords.z).length() > maxTreeRadius) return true


        blocksLeft--
        if (block.type in allowedBlocks) {
            block.breakNaturally()
        } else {
            currentPlayer?.breakBlock(block)
        }

        for (y in -1..1) {
            for (z in -1..1) {
                for (x in -1..1) {
                    if (!treeFeller(block.getRelative(x, y, z), lastBlockType, firstMaterial, firstCords, allowedBlocks)) return false
                }
            }
        }

        return true
    }
}