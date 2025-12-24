package io.github.lamelemon.treefellerEnchantment.commands

import io.github.lamelemon.treefellerEnchantment.utils.Utils
import io.papermc.paper.command.brigadier.BasicCommand
import io.papermc.paper.command.brigadier.CommandSourceStack
import org.bukkit.Sound
import org.bukkit.enchantments.Enchantment
import org.bukkit.entity.Player
import java.util.*

class LimitlessEnchant: BasicCommand {

    override fun execute(
        commandSourceStack: CommandSourceStack,
        args: Array<out String>
    ) {
        val player = commandSourceStack.sender
        if (player !is Player) return

        if (args.isEmpty()) {
            Utils.messagePlayer(player, "<red>Missing arguments!</red>")
            Utils.simplePlaySound(player, Sound.BLOCK_NOTE_BLOCK_BASS)
            return
        }

        val enchantName = args[0].lowercase(Locale.getDefault())
        var level = 1

        if (args.size >= 2) {
            try {
                level = Integer.parseInt(args[1])
            } catch (_: NumberFormatException) {
                Utils.messagePlayer(player, "<red>Please input a valid number!</red>")
                Utils.simplePlaySound(player, Sound.BLOCK_NOTE_BLOCK_BASS)
            }
        }

        val item = player.inventory.itemInMainHand
        if (item.type.isAir) {
            Utils.messagePlayer(player, "<red>You must be holding an item!</red>")
            Utils.simplePlaySound(player, Sound.BLOCK_NOTE_BLOCK_BASS)
            return
        }

        val enchant = Utils.getEnchant(enchantName)
        if (enchant is Enchantment) {
            Utils.messagePlayer(player, "Applying $enchantName...")
            Utils.simplePlaySound(player, Sound.BLOCK_NOTE_BLOCK_PLING)
            item.addUnsafeEnchantment(enchant, level)
        } else {
            Utils.messagePlayer(player, "<red>$enchantName is not a valid enchantment!</red>")
            Utils.simplePlaySound(player, Sound.BLOCK_NOTE_BLOCK_BASS)
        }
    }

    override fun suggest(commandSourceStack: CommandSourceStack, args: Array<out String>): Collection<String> {
        if (args.size > 1) return super.suggest(commandSourceStack, args)
        if (args.isEmpty()) return Utils.enchantmentRegistry.map { it.key.toString() }

        val results: TreeSet<String> = TreeSet()
        for (enchant in Utils.enchantmentRegistry) {
            val enchantName = enchant.key.toString()
            if (enchantName.startsWith(args[0])) {
                results.add(enchantName)
            }
        }
        return results
    }

    override fun permission(): String {
        return "treefeller.permission.enchant"
    }
}