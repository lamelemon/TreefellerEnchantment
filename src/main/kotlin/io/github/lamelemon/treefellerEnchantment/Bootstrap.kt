@file:Suppress("UnstableApiUsage") // Shut up compiler about experimental api usage
package io.github.lamelemon.treefellerEnchantment

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

class Bootstrap: PluginBootstrap {

    override fun bootstrap(context: BootstrapContext) {

        // Find config
        val configFile = File(context.dataDirectory.toFile(), "config.yml")
        val config = YamlConfiguration.loadConfiguration(configFile)

        // Register enchantment
        context.lifecycleManager.registerEventHandler(
            RegistryEvents.ENCHANTMENT.compose().newHandler { event ->
                // This needs to be ran inside compose() as the registry may not exist yet otherwise
                val itemRegistry = RegistryAccess.registryAccess().getRegistry(RegistryKey.ITEM)

                // This part exists solely for readability, won't affect performance THAT much
                val rawName: String = config.getString("enchantment.name") ?: "treefeller"
                val enchantName: String = rawName.lowercase().replace("[^a-z0-9_.\\-]".toRegex(), "_")
                val description: String  = config.getString("enchantment.description") ?: "Treefeller"
                val maxLevel: Int = config.getInt("enchantment.max-level", 5)

                val minCostBase: Int = config.getInt("enchantment.minimum-cost.base-cost", 5)
                val minCostAdditional: Int = config.getInt("enchantment.minimum-cost.additional-cost", 8)

                val maxCostBase: Int = config.getInt("enchantment.maximum-cost.base-cost", 25)
                val maxCostAdditional: Int = config.getInt("enchantment.maximum-cost.additional-cost", 8)

                val weight: Int = config.getInt("enchantment.weight", 4)
                val anvilCost: Int = config.getInt("enchantment.anvil-cost", 4)

                val supportedItemsList = config.getStringList("enchantment.supported-items").ifEmpty { listOf("netherite_axe",
                    "diamond_axe",
                    "iron_axe",
                    "stone_axe",
                    "golden_axe",
                    "wooden_axe") }
                val activeSlotsList = config.getStringList("enchantment.active-slots").ifEmpty { listOf("MAIN_HAND") }

                run {
                    event.registry().register(
                        EnchantmentKeys.create(Key.key("treefeller:$enchantName"))
                    ) { b ->
                        b.description(Component.text(description))
                            .maxLevel(maxLevel)
                            .minimumCost(
                                EnchantmentRegistryEntry.EnchantmentCost.of(minCostBase, minCostAdditional)
                            )
                            .maximumCost(
                                EnchantmentRegistryEntry.EnchantmentCost.of(maxCostBase, maxCostAdditional)
                            )
                            .weight(weight)
                            .anvilCost(anvilCost)
                            .supportedItems(
                                RegistrySet.keySetFromValues(
                                    RegistryKey.ITEM,
                                    mapValid(supportedItemsList) { item -> itemRegistry.get(Key.key(item)) }
                                )
                            )
                            .primaryItems(
                                RegistrySet.keySetFromValues(
                                    RegistryKey.ITEM,
                                    mapValid(supportedItemsList) { item -> itemRegistry.get(Key.key(item)) }
                                )
                            )
                            .activeSlots(
                                mapValid(activeSlotsList) { name -> EquipmentSlotGroup.getByName(name) }
                            )
                    }
                }
            }
        )
    }

    private fun <T> mapValid(keys: List<String>, mapper: (String) -> T?): List<T> {
        return keys.mapNotNull(mapper)
    }
}