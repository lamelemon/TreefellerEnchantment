package io.github.lamelemon.treefellerEnchantment.utils

import org.bukkit.Material
import org.bukkit.block.Block
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.bukkit.util.Vector
import kotlin.math.absoluteValue
import kotlin.math.pow

class TreeBreaker(
    block: Block,
    val player: Player,
    val firstMaterial: Material,
    val firstCords: Vector,
    val allowedBlocks: HashSet<Material>,
    val currentTool: ItemStack,
    val maxTreeHeight: Int,
    val maxTreeRadiusSquared: Int,
    var blocksLeft: Int
    ) {

    init { treeFeller(block, block.type) }

    fun treeFeller(block: Block, lastBlockType: Material): Boolean {
        if (blocksLeft <= 0) return false
        if (block.isEmpty) return true

        val type = block.type
        if (type == firstMaterial && lastBlockType != firstMaterial) return true // Block is a log but last block isn't
        if (type !in allowedBlocks && type != firstMaterial) return true // Block isn't allowed

        if (block.y.absoluteValue - firstCords.y > maxTreeHeight) return true
        if ((block.x - firstCords.x).pow(2) + (block.z.absoluteValue - firstCords.z).pow(2) > maxTreeRadiusSquared) return true

        blocksLeft--
        player.breakBlock(block)

        for (y in -1..1) {
            for (z in -1..1) {
                for (x in -1..1) {
                    if (!treeFeller(block.getRelative(x, y, z), type)) return false
                }
            }
        }

        return true
    }
}