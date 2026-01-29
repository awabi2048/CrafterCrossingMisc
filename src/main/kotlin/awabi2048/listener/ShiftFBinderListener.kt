package awabi2048.listener

import awabi2048.Main
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerSwapHandItemsEvent

class ShiftFBinderListener : Listener {

    @EventHandler
    fun onShiftF(event: PlayerSwapHandItemsEvent) {
        val player = event.player
        
        // スニーク中ならキャンセルしてコマンド実行
        if (player.isSneaking) {
            val commands = Main.instance.config.getStringList("shift_f_binder.commands")
            
            if (commands.isEmpty()) return
            
            event.isCancelled = true
            
            for (command in commands) {
                if (command.isEmpty()) continue
                
                val processedCommand = command
                    .replace("%player_name%", player.name)
                    .replace("%player_uuid%", player.uniqueId.toString())
                
                player.performCommand(processedCommand)
            }
        }
    }
}
