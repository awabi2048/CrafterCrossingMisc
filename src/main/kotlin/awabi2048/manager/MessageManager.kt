package awabi2048.manager

import awabi2048.Main
import java.io.File
import org.bukkit.configuration.file.YamlConfiguration
import org.bukkit.entity.Player

object MessageManager {
    private lateinit var config: YamlConfiguration
    private val configFile = File(Main.instance.dataFolder, "messages.yml")

    fun load() {
        if (!configFile.exists()) {
            Main.instance.saveResource("messages.yml", false)
        }
        config = YamlConfiguration.loadConfiguration(configFile)

        // Merge defaults
        val resourceStream = Main.instance.getResource("messages.yml")
        if (resourceStream != null) {
            val reader =
                    java.io.InputStreamReader(
                            resourceStream,
                            java.nio.charset.StandardCharsets.UTF_8
                    )
            val defaultConfig = YamlConfiguration.loadConfiguration(reader)
            var changed = false
            for (key in defaultConfig.getKeys(true)) {
                if (!config.contains(key)) {
                    config.set(key, defaultConfig.get(key))
                    changed = true
                }
            }
            if (changed) {
                try {
                    config.save(configFile)
                } catch (e: Exception) {
                    Main.instance.logger.warning("Failed to save updated messages.yml")
                }
            }
        }
    }

    fun getStyle(id: String): String {
        return config.getString(id, "batch")?.lowercase() ?: "batch"
    }

    fun getOrderIndex(player: Player, id: String, max: Int): Int {
        if (max <= 0) return 0

        val key = "npc_message_index.$id"
        var index = PlayerDataManager.getInt(player.uniqueId, key, 0)

        // Ensure index is within bounds (in case list size changed)
        if (index >= max || index < 0) {
            index = 0
        }

        // Calculate next index for future use
        val nextIndex = (index + 1) % max
        PlayerDataManager.set(player.uniqueId, key, nextIndex)

        return index
    }

    fun getMessageIds(): Set<String> {
        return config.getKeys(false)
    }
}
