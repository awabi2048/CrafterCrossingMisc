package awabi2048.command

import awabi2048.Main
import awabi2048.manager.LanguageManager
import awabi2048.manager.MessageManager
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer
import org.bukkit.Bukkit
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.command.TabCompleter
import org.bukkit.entity.Player

class CCMCommand : CommandExecutor, TabCompleter {

    override fun onCommand(
            sender: CommandSender,
            command: Command,
            label: String,
            args: Array<out String>
    ): Boolean {
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
                        val current =
                                Main.instance.config.getBoolean("music_playback.enabled", false)
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
                        sender.sendMessage(
                                LanguageManager.getMessage(
                                        player,
                                        "toggle_success",
                                        "function" to "play_music",
                                        "status" to statusString
                                )
                        )
                    }
                    else -> {
                        sender.sendMessage(
                                LanguageManager.getMessage(
                                        player,
                                        "function_not_found",
                                        "function" to args[1]
                                )
                        )
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
                sender.sendMessage(
                        LanguageManager.getMessage(player, "lang_updated", "lang" to lang)
                )
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
            "npc_message" -> {
                if (args.size < 2) {
                    sender.sendMessage(LanguageManager.getMessage(player, "usage"))
                    return true
                }

                val messageId = args[1]
                var targetPlayer = player

                if (args.size >= 3) {
                    val targetName = args[2]
                    targetPlayer = Bukkit.getPlayer(targetName)
                    if (targetPlayer == null) {
                        sender.sendMessage("§cプレイヤーが見つかりません: $targetName")
                        return true
                    }
                }

                if (targetPlayer == null) {
                    sender.sendMessage("§cプレイヤーを指定するか、プレイヤーが実行してください。")
                    return true
                }

                val texts = LanguageManager.getStringList(targetPlayer, "npc_messages.$messageId")
                if (texts.isEmpty()) {
                    sender.sendMessage("§cメッセージが未定義か、空の状態です: $messageId")
                    return true
                }

                val style = MessageManager.getStyle(messageId)
                val messagesToSend = mutableListOf<String>()

                when (style) {
                    "random" -> messagesToSend.add(texts.random())
                    "order" -> {
                        val index =
                                MessageManager.getOrderIndex(targetPlayer, messageId, texts.size)
                        messagesToSend.add(texts[index])
                    }
                    "batch" -> messagesToSend.addAll(texts)
                    else -> messagesToSend.addAll(texts)
                }

                for (msg in messagesToSend) {
                    targetPlayer.sendMessage(
                            LegacyComponentSerializer.legacyAmpersand().deserialize(msg)
                    )
                }
            }
            else -> {
                sender.sendMessage(LanguageManager.getMessage(player, "usage"))
            }
        }

        return true
    }

    override fun onTabComplete(
            sender: CommandSender,
            command: Command,
            alias: String,
            args: Array<out String>
    ): List<String>? {
        if (args.size == 1) {
            return listOf("toggle", "lang", "reload", "npc_message").filter {
                it.startsWith(args[0], ignoreCase = true)
            }
        }
        if (args.size == 2) {
            when (args[0].lowercase()) {
                "toggle" ->
                        return listOf("play_music").filter {
                            it.startsWith(args[1], ignoreCase = true)
                        }
                "lang" ->
                        return listOf("ja_jp", "en_us").filter {
                            it.startsWith(args[1], ignoreCase = true)
                        }
                "npc_message" ->
                        return MessageManager.getMessageIds()
                                .filter { it.startsWith(args[1], ignoreCase = true) }
                                .toList()
            }
        }
        if (args.size == 3) {
            when (args[0].lowercase()) {
                "npc_message" -> return null // standard player list
            }
        }
        return emptyList()
    }
}
