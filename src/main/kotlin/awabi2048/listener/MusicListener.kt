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
    private val currentSounds = java.util.HashMap<java.util.UUID, String>()

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

    fun stopAllPlayersMusic() {
        Main.instance.server.onlinePlayers.forEach { stopMusic(it) }
    }

    fun startAllPlayersMusic() {
        Main.instance.server.onlinePlayers.forEach { playMusic(it, it.world.name) }
    }

    fun stopMusic(player: org.bukkit.entity.Player) {
        musicTasks.remove(player.uniqueId)?.cancel()
        val lastSound = currentSounds.remove(player.uniqueId)
        
        // 念のため停止パケットも送る (RECORDSカテゴリを指定)
        try {
            player.stopSound(org.bukkit.SoundCategory.RECORDS)
            if (lastSound != null) {
                player.stopSound(lastSound, org.bukkit.SoundCategory.RECORDS)
            }
        } catch (e: NoSuchMethodError) {
            player.stopSound("")
        }
    }

    fun playMusic(player: org.bukkit.entity.Player, worldName: String) {
        stopMusic(player) // 以前のBGMを停止
        
        // 個人の再生設定をチェック (デフォルト true)
        if (!awabi2048.manager.PlayerDataManager.getBoolean(player.uniqueId, "play_music", true)) {
            stopMusic(player)
            return
        }

        val config = Main.instance.config
        val section = config.getConfigurationSection("music_playback.worlds.$worldName") ?: return
        val soundId = section.getString("sound") ?: return
        val volume = section.getDouble("volume", 1.0).toFloat()
        val pitch = section.getDouble("pitch", 1.0).toFloat()
        val duration = section.getLong("duration", 200) // デフォルト200秒

        val task = Main.instance.server.scheduler.runTaskTimer(Main.instance, Runnable {
            if (player.isOnline) {
                // 再生中も設定をチェック
                if (!awabi2048.manager.PlayerDataManager.getBoolean(player.uniqueId, "play_music", true)) {
                    stopMusic(player)
                    return@Runnable
                }
                
                currentSounds[player.uniqueId] = soundId

                // BGMとして再生するため、SoundCategory.RECORDSを使用し、位置はプレイヤーの現在地
                // volumeを大きくすると聞こえる範囲が広がるが、ここでは設定値を尊重しつつ、
                // 必要であればconfigで調整してもらう想定とする。
                try {
                    player.playSound(player.location, soundId as String, org.bukkit.SoundCategory.RECORDS, volume, pitch)
                } catch (e: NoSuchMethodError) {
                    // 古いバージョンなどのフォールバック (引数の型を明示して曖昧さを回避)
                    player.playSound(player.location, soundId as String, volume, pitch)
                }
            } else {
                // プレイヤーがオフラインならタスクキャンセル (念のため)
                musicTasks.remove(player.uniqueId)?.cancel()
            }
        }, 0L, duration * 20L)

        musicTasks[player.uniqueId] = task
    }
}
