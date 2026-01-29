package awabi2048.listener

import awabi2048.Main
import org.bukkit.Sound
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerChangedWorldEvent
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerQuitEvent

class MusicListener : Listener {

    private val musicTasks = java.util.HashMap<java.util.UUID, org.bukkit.scheduler.BukkitTask>()

    @EventHandler
    fun onWorldChange(event: PlayerChangedWorldEvent) {
        playMusic(event.player, event.player.world.name)
    }

    @EventHandler
    fun onJoin(event: PlayerJoinEvent) {
        // ログイン直後は再生されないことがあるため、20tick遅延させる
        Main.instance.server.scheduler.runTaskLater(Main.instance, Runnable {
            if (event.player.isOnline) {
                playMusic(event.player, event.player.world.name)
            }
        }, 20L)
    }

    @EventHandler
    fun onQuit(event: PlayerQuitEvent) {
        stopMusic(event.player)
    }

    private fun stopMusic(player: org.bukkit.entity.Player) {
        musicTasks.remove(player.uniqueId)?.cancel()
        // 念のため停止パケットも送る (すべてのカテゴリを停止)
        player.stopSound("") 
    }

    private fun playMusic(player: org.bukkit.entity.Player, worldName: String) {
        stopMusic(player) // 以前のBGMを停止

        val config = Main.instance.config
        
        // 機能が有効かチェック
        if (!config.getBoolean("music_playback.enabled", true)) return

        val section = config.getConfigurationSection("music_playback.worlds.$worldName") ?: return
        val soundId = section.getString("sound") ?: return
        val volume = section.getDouble("volume", 1.0).toFloat()
        val pitch = section.getDouble("pitch", 1.0).toFloat()
        val duration = section.getLong("duration", 200) // デフォルト200秒

        val task = Main.instance.server.scheduler.runTaskTimer(Main.instance, Runnable {
            if (player.isOnline) {
                // BGMとして再生するため、SoundCategory.RECORDSを使用し、位置はプレイヤーの現在地
                // volumeを大きくすると聞こえる範囲が広がるが、ここでは設定値を尊重しつつ、
                // 必要であればconfigで調整してもらう想定とする。
                try {
                    player.playSound(player.location, soundId, org.bukkit.SoundCategory.RECORDS, volume, pitch)
                } catch (e: NoSuchMethodError) {
                    // 古いバージョンなどのフォールバック
                    player.playSound(player.location, soundId, volume, pitch)
                }
            } else {
                // プレイヤーがオフラインならタスクキャンセル (念のため)
                musicTasks.remove(player.uniqueId)?.cancel()
            }
        }, 0L, duration * 20L)

        musicTasks[player.uniqueId] = task
    }
}
