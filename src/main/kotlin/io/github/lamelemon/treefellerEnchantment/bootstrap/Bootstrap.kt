@file:Suppress("UnstableApiUsage")

package io.github.lamelemon.treefellerEnchantment.bootstrap

import io.papermc.paper.plugin.bootstrap.BootstrapContext
import io.papermc.paper.plugin.bootstrap.PluginBootstrap
import io.papermc.paper.registry.RegistryAccess
import io.papermc.paper.registry.RegistryKey
import io.papermc.paper.registry.data.EnchantmentRegistryEntry
import io.papermc.paper.registry.event.RegistryEvents
import io.papermc.paper.registry.keys.EnchantmentKeys
import io.papermc.paper.registry.set.RegistrySet
import net.kyori.adventure.key.Key
import net.kyori.adventure.text.Component
import org.bukkit.configuration.file.YamlConfiguration
import org.bukkit.inventory.EquipmentSlotGroup
import java.io.File

@SuppressWarnings
class Bootstrap: PluginBootstrap {

    override fun bootstrap(context: BootstrapContext) {
        // Register enchantment
        context.lifecycleManager.registerEventHandler(
            RegistryEvents.ENCHANTMENT.compose().newHandler { event ->
                // Find config
                val configFile = File(context.dataDirectory.toFile(), "config.yml")
                val config = YamlConfiguration.loadConfiguration(configFile)

                // This needs to be ran inside compose() as the registry may not exist yet otherwise
                val itemRegistry = RegistryAccess.registryAccess().getRegistry(RegistryKey.ITEM)

                // This part exists solely for readability, won't affect performance THAT much
                val enchantName = "treefeller"
                val enchantmentKey = Key.key("treefeller:$enchantName")
                val description: String  = config.getString("enchantment.description") ?: "Treefeller"
                val maxLevel: Int = config.getInt("enchantment.max-level", 5)

                val minCostBase: Int = config.getInt("enchantment.minimum-cost.base-cost", 5)
                val minCostAdditional: Int = config.getInt("enchantment.minimum-cost.additional-cost", 8)

                val maxCostBase: Int = config.getInt("enchantment.maximum-cost.base-cost", 25)
                val maxCostAdditional: Int = config.getInt("enchantment.maximum-cost.additional-cost", 8)

                val weight: Int = config.getInt("enchantment.weight", 4)
                val anvilCost: Int = config.getInt("enchantment.anvil-cost", 4)

                val supportedItemsList = RegistrySet.keySetFromValues(
                    RegistryKey.ITEM,
                    config.getStringList("enchantment.supported-items")
                        .ifEmpty { listOf(
                            "netherite_axe",
                            "diamond_axe",
                            "iron_axe",
                            "stone_axe",
                            "golden_axe",
                            "wooden_axe")
                        }
                        .mapNotNull { item -> itemRegistry.get(Key.key(item)) }
                )

                val activeSlotsList = config.getStringList("enchantment.active-slots")
                    .ifEmpty { listOf("MAIN_HAND") }.mapNotNull { name -> EquipmentSlotGroup.getByName(name) }

                run {
                    event.registry().register(
                        EnchantmentKeys.create(enchantmentKey)
                    ) { b ->
                        b.description(Component.text(description))
                            .maxLevel(maxLevel)
                            .minimumCost(
                                EnchantmentRegistryEntry.EnchantmentCost.of(
                                    minCostBase,
                                    minCostAdditional
                                )
                            )
                            .maximumCost(
                                EnchantmentRegistryEntry.EnchantmentCost.of(
                                    maxCostBase,
                                    maxCostAdditional
                                )
                            )
                            .weight(weight)
                            .anvilCost(anvilCost)
                            .supportedItems(supportedItemsList)
                            .primaryItems(supportedItemsList)
                            .activeSlots(activeSlotsList)
                    }
                }
            }
        )
    }
}