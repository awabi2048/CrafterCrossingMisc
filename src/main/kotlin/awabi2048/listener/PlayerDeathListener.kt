package awabi2048.listener

import net.kyori.adventure.text.Component
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.entity.PlayerDeathEvent

class PlayerDeathListener : Listener {

    @EventHandler
    fun onPlayerDeath(event: PlayerDeathEvent) {
        val deathMessage = event.deathMessage()

        if (deathMessage != null) {
            // 全体への通知をキャンセル
            event.deathMessage(null)

            // 同一ワールドのプレイヤーにのみ送信
            val timestampedMessage = Component.text("").append(deathMessage)

            event.entity.world.players.forEach { player -> player.sendMessage(timestampedMessage) }
        }
    }
}
