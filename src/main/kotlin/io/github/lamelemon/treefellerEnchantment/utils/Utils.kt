package io.github.lamelemon.treefellerEnchantment.utils

import io.papermc.paper.registry.RegistryAccess
import io.papermc.paper.registry.RegistryKey
import io.papermc.paper.registry.TypedKey
import net.kyori.adventure.key.Key
import org.bukkit.Registry
import org.bukkit.Sound
import org.bukkit.configuration.file.YamlConfiguration
import org.bukkit.enchantments.Enchantment
import org.bukkit.entity.Player
import org.jetbrains.annotations.NotNull

object Utils {
    private val enchantmentRegistry = RegistryAccess.registryAccess().getRegistry(RegistryKey.ENCHANTMENT)
    lateinit var enchantment: Enchantment
    lateinit var configuration: YamlConfiguration

    fun messagePlayer(player: Player, message: String) {
        player.sendRichMessage("<gold>[</gold><color:#fafad2>Tree feller</color><gold>]</gold> $message")
    }

    fun simplePlaySound(player: Player, sound: Sound) {
        player.playSound(player.location,
            sound,
            1.0f,
            1.0f
        )
    }

    fun getEnchant(@NotNull key: String): Enchantment? {
        return enchantmentRegistry.get(TypedKey.create(RegistryKey.ENCHANTMENT, Key.key(key)))
    }

    fun getEnchantmentRegistry(): Registry<Enchantment> {
        return enchantmentRegistry
    }
}