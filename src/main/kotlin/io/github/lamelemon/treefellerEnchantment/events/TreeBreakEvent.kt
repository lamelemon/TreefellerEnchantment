package io.github.lamelemon.treefellerEnchantment.events

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

class TreeBreakEvent(): Listener {

    var blocksLeft: Int = 0
    var currentPlayer: Player? = null
    val allowedTrees: HashMap<String, HashSet<Material>> = HashMap()
    var hasToSneak = true
    var blockCapMultiplier = 100

    init {
        val configurationSection = configuration.getConfigurationSection("materials")
        if (configurationSection is ConfigurationSection) {
            for (key in configurationSection.getKeys(false)) {
                allowedTrees[key] = configurationSection.getStringList(key)
                    .mapTo(HashSet()) { Material.matchMaterial(it)!! }
            }
        }

        hasToSneak = configuration.getBoolean("has-to-sneak", true)
        blockCapMultiplier = configuration.getInt("block-cap-multiplier", 100)
    }

    @EventHandler(priority = EventPriority.MONITOR)
    fun blockBreakEvent(event: BlockBreakEvent) {
        if (event.isCancelled) return

        val player = event.player
        if (player.isSneaking != hasToSneak || player == currentPlayer || !event.isDropItems) return

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
                allowedBlocks
            )
            currentPlayer = null
        }
    }

    fun treeBreaker(block: Block, lastBlock: Material, firstBlock: Material, allowedBlocks: HashSet<Material>): Boolean {
        if (blocksLeft <= 0) return false
        if (block.type !in allowedBlocks && block.type != firstBlock) return true
        if (block.isEmpty) return true
        if (block.type == firstBlock && lastBlock != firstBlock) return true

        blocksLeft--
        currentPlayer?.breakBlock(block)

        for (y in -1..1) {
            for (z in -1..1) {
                for (x in -1..1) {
                    if (!treeBreaker(block.getRelative(x, y, z), lastBlock, firstBlock, allowedBlocks)) return false
                }
            }
        }

        return true
    }
}