package io.github.lamelemon.treefellerEnchantment.events

import io.github.lamelemon.treefellerEnchantment.utils.Utils.configuration
import io.github.lamelemon.treefellerEnchantment.utils.Utils.enchantment
import io.github.lamelemon.treefellerEnchantment.utils.Utils.messagePlayer
import org.bukkit.Material
import org.bukkit.block.Block
import org.bukkit.configuration.ConfigurationSection
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.block.BlockBreakEvent
import java.time.InstantSource.system

class TreeBreakEvent(val hasToSneak: Boolean, val blockCapMultiplier: Int): Listener {

    var blocksLeft: Int = 0
    var currentPlayer: Player? = null
    val allowedTrees: HashMap<String, HashSet<Material>> = HashMap()

    init {
        val configurationSection = configuration.getConfigurationSection("tree-structure")
        if (configurationSection is ConfigurationSection) {
            for (key in configurationSection.getKeys(false)) {
                allowedTrees[key] = configuration.getStringList("tree-structure.$key")
                    .mapTo(HashSet()) { Material.valueOf(it) }
            }
        }

    }

    @EventHandler(priority = EventPriority.MONITOR)
    fun blockBreakEvent(event: BlockBreakEvent) {
        val startTime = System.nanoTime()

        val player = event.player
        if (player.isSneaking != hasToSneak || player == currentPlayer) return

        val currentTool = player.inventory.itemInMainHand

        if (!currentTool.containsEnchantment(enchantment)) return

        val block = event.block
        val allowedBlocks = allowedTrees[block.type.toString()]
        if (allowedBlocks is HashSet<Material>) {
            currentPlayer = player
            blocksLeft = currentTool.getEnchantmentLevel(enchantment) * blockCapMultiplier
            treeBreaker(block,
                block.type,
                block.type,
                allowedBlocks,
                HashSet())
            currentPlayer = null

            messagePlayer(player, "Took " + ((System.nanoTime() - startTime) / 1_000_000) + "ms to complete!")
        }
    }

    fun treeBreaker(block: Block, lastBlock: Material, firstBlock: Material, allowedBlocks: HashSet<Material>, visited: HashSet<Block>): Boolean {
        if (blocksLeft <= 0) return false

        if (!visited.add(block) // Block has already been checked
            || block.isEmpty // Block is air
            || block.type !in allowedBlocks // Block isn't allowed
            || (block.type == firstBlock && lastBlock != firstBlock) // Block is a starter block but isn't allowed to be broken after others
            ) return true

        blocksLeft--
        currentPlayer?.breakBlock(block)

        for (y in -1..1) {
            for (z in -1..1) {
                for (x in -1..1) {
                    if (!treeBreaker(block.getRelative(x, y, z), lastBlock, firstBlock, allowedBlocks, visited)) return false
                }
            }
        }

        return true
    }
}