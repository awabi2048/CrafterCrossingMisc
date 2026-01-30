package awabi2048.manager

import awabi2048.Main
import java.io.File
import java.util.UUID
import org.bukkit.configuration.file.YamlConfiguration

object PlayerDataManager {
    private val playerData = mutableMapOf<UUID, YamlConfiguration>()
    private val dataDir = File(Main.instance.dataFolder, "userdata")

    fun load() {
        if (!dataDir.exists()) {
            dataDir.mkdirs()
        }
    }

    private fun getFile(uuid: UUID): File {
        return File(dataDir, "$uuid.yml")
    }

    private fun getData(uuid: UUID): YamlConfiguration {
        return playerData.getOrPut(uuid) {
            val file = getFile(uuid)
            if (file.exists()) {
                YamlConfiguration.loadConfiguration(file)
            } else {
                YamlConfiguration()
            }
        }
    }

    fun getString(uuid: UUID, key: String, default: String? = null): String? {
        return getData(uuid).getString(key, default)
    }

    fun getInt(uuid: UUID, key: String, default: Int = 0): Int {
        return getData(uuid).getInt(key, default)
    }

    fun set(uuid: UUID, key: String, value: Any?) {
        val data = getData(uuid)
        data.set(key, value)
        save(uuid)
    }

    fun save(uuid: UUID) {
        val data = playerData[uuid] ?: return
        try {
            data.save(getFile(uuid))
        } catch (e: Exception) {
            Main.instance.logger.severe("Failed to save player data for $uuid: ${e.message}")
        }
    }

    fun unload(uuid: UUID) {
        save(uuid)
        playerData.remove(uuid)
    }
}
