package awabi2048.listener

import awabi2048.manager.PlayerDataManager
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerQuitEvent

class PlayerDataListener : Listener {

    @EventHandler
    fun onQuit(event: PlayerQuitEvent) {
        PlayerDataManager.unload(event.player.uniqueId)
    }
}
