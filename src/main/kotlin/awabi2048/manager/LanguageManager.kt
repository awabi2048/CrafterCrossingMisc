package awabi2048.manager

import awabi2048.Main
import java.io.File
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer
import org.bukkit.configuration.file.YamlConfiguration
import org.bukkit.entity.Player

object LanguageManager {
    private val langFiles = mutableMapOf<String, YamlConfiguration>()
    private const val DEFAULT_LANG = "ja_jp"

    fun load() {
        langFiles.clear()
        val langDir = File(Main.instance.dataFolder, "lang")
        if (!langDir.exists()) {
            langDir.mkdirs()
        }

        val defaultLangs = listOf("ja_jp", "en_us")
        defaultLangs.forEach { lang ->
            val file = File(langDir, "$lang.yml")
            if (!file.exists()) {
                Main.instance.saveResource("lang/$lang.yml", false)
            } else {
                val config = YamlConfiguration.loadConfiguration(file)
                val resourceStream = Main.instance.getResource("lang/$lang.yml")
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
                            config.save(file)
                        } catch (e: Exception) {
                            Main.instance.logger.warning(
                                    "Failed to save updated language file: ${file.name}"
                            )
                        }
                    }
                }
            }
        }

        langDir.listFiles()?.forEach { file ->
            if (file.extension == "yml") {
                val config = YamlConfiguration.loadConfiguration(file)
                langFiles[file.nameWithoutExtension.lowercase()] = config
            }
        }
    }

    fun setPlayerLang(player: Player, lang: String) {
        PlayerDataManager.set(player.uniqueId, "lang", lang.lowercase())
    }

    fun getMessage(
            player: Player?,
            key: String,
            vararg placeholders: Pair<String, String>
    ): Component {
        val lang =
                if (player != null)
                        PlayerDataManager.getString(player.uniqueId, "lang", DEFAULT_LANG)
                                ?: DEFAULT_LANG
                else DEFAULT_LANG
        val config = langFiles[lang] ?: langFiles[DEFAULT_LANG]

        var message = config?.getString(key) ?: key
        val prefix = config?.getString("prefix") ?: ""

        if (key != "prefix") {
            message = prefix + message
        }

        placeholders.forEach { (placeholder, value) ->
            message = message.replace("%$placeholder%", value)
        }

        return LegacyComponentSerializer.legacyAmpersand().deserialize(message)
    }

    fun getRawString(player: Player?, key: String): String {
        val lang =
                if (player != null)
                        PlayerDataManager.getString(player.uniqueId, "lang", DEFAULT_LANG)
                                ?: DEFAULT_LANG
                else DEFAULT_LANG
        val config = langFiles[lang] ?: langFiles[DEFAULT_LANG]
        return config?.getString(key) ?: key
    }

    fun getStringList(player: Player?, key: String): List<String> {
        val lang =
                if (player != null)
                        PlayerDataManager.getString(player.uniqueId, "lang", DEFAULT_LANG)
                                ?: DEFAULT_LANG
                else DEFAULT_LANG
        val config = langFiles[lang] ?: langFiles[DEFAULT_LANG]
        return config?.getStringList(key) ?: emptyList()
    }
}
