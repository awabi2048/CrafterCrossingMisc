package awabi2048.listener

import awabi2048.Main
import org.bukkit.GameRule
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.world.WorldLoadEvent

class WorldListener : Listener {

    @EventHandler
    fun onWorldLoad(event: WorldLoadEvent) {
        // config.yml で機能が有効になっているかチェック
        val enabled = Main.instance.config.getBoolean("global_sound_events_auto_disable.enabled", true)
        
        if (!enabled) {
            return
        }

        // globalSoundEvents ゲームルールを false に設定
        val world = event.world
        world.setGameRule(GameRule.GLOBAL_SOUND_EVENTS, false)
        
        Main.instance.logger.info("Set globalSoundEvents to false for world: ${world.name}")
    }
}
