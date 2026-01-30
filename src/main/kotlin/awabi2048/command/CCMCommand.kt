package awabi2048.command

import awabi2048.Main
import awabi2048.manager.LanguageManager
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.command.TabCompleter
import org.bukkit.entity.Player

class CCMCommand : CommandExecutor, TabCompleter {

    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
        val player = sender as? Player

        if (!sender.hasPermission("ccm.admin")) {
            sender.sendMessage(LanguageManager.getMessage(player, "no_permission"))
            return true
        }

        if (args.isEmpty()) {
            sender.sendMessage(LanguageManager.getMessage(player, "usage"))
            return true
        }

        when (args[0].lowercase()) {
            "toggle" -> {
                if (args.size < 2) {
                    sender.sendMessage(LanguageManager.getMessage(player, "usage"))
                    return true
                }

                when (args[1].lowercase()) {
                    "play_music" -> {
                        val current = Main.instance.config.getBoolean("music_playback.enabled", false)
                        val newValue = !current
                        Main.instance.config.set("music_playback.enabled", newValue)
                        Main.instance.saveConfig()
                        
                        // プレイヤー全員の音楽を即座に更新
                        if (newValue) {
                            Main.instance.musicListener.startAllPlayersMusic()
                        } else {
                            Main.instance.musicListener.stopAllPlayersMusic()
                        }
                        
                        val statusKey = if (newValue) "enabled" else "disabled"
                        val statusString = LanguageManager.getRawString(player, statusKey)
                        sender.sendMessage(LanguageManager.getMessage(player, "toggle_success", "function" to "play_music", "status" to statusString))
                    }
                    else -> {
                        sender.sendMessage(LanguageManager.getMessage(player, "function_not_found", "function" to args[1]))
                    }
                }
            }
            "lang" -> {
                if (player == null) {
                    sender.sendMessage("このコマンドはプレイヤーのみ実行可能です。")
                    return true
                }
                if (args.size < 2) {
                    sender.sendMessage(LanguageManager.getMessage(player, "usage"))
                    return true
                }

                val lang = args[1].lowercase()
                LanguageManager.setPlayerLang(player, lang)
                sender.sendMessage(LanguageManager.getMessage(player, "lang_updated", "lang" to lang))
            }
            "reload" -> {
                Main.instance.reloadPluginConfig()
                LanguageManager.load()
                
                // 音楽再生設定を反映させるためにタスクを更新
                Main.instance.musicListener.stopAllPlayersMusic()
                if (Main.instance.config.getBoolean("music_playback.enabled", true)) {
                    Main.instance.musicListener.startAllPlayersMusic()
                }
                
                sender.sendMessage(LanguageManager.getMessage(player, "reload_success"))
            }
            else -> {
                sender.sendMessage(LanguageManager.getMessage(player, "usage"))
            }
        }

        return true
    }

    override fun onTabComplete(sender: CommandSender, command: Command, alias: String, args: Array<out String>): List<String>? {
        if (args.size == 1) {
            return listOf("toggle", "lang", "reload").filter { it.startsWith(args[0], ignoreCase = true) }
        }
        if (args.size == 2) {
            when (args[0].lowercase()) {
                "toggle" -> return listOf("play_music").filter { it.startsWith(args[1], ignoreCase = true) }
                "lang" -> return listOf("ja_jp", "en_us").filter { it.startsWith(args[1], ignoreCase = true) }
            }
        }
        return emptyList()
    }
}
