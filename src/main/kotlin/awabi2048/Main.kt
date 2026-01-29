package awabi2048

import awabi2048.command.CCMCommand
import awabi2048.listener.MusicListener
import awabi2048.listener.PlayerDataListener
import awabi2048.listener.ShiftFBinderListener
import awabi2048.manager.LanguageManager
import awabi2048.manager.PlayerDataManager
import org.bukkit.plugin.java.JavaPlugin

class Main : JavaPlugin() {

    companion object {
        lateinit var instance: Main
            private set
    }

    override fun onEnable() {
        instance = this

        saveDefaultConfig()
        PlayerDataManager.load()
        LanguageManager.load()
        
        // コマンドの登録
        getCommand("ccm")?.setExecutor(CCMCommand())
        getCommand("ccm")?.tabCompleter = CCMCommand()

        // リスナーの登録
        server.pluginManager.registerEvents(ShiftFBinderListener(), this)
        server.pluginManager.registerEvents(MusicListener(), this)
        server.pluginManager.registerEvents(PlayerDataListener(), this)

        logger.info("CrafterCrossingMisc has been enabled!")
    }

    override fun onDisable() {
        logger.info("CrafterCrossingMisc has been disabled!")
    }

    fun reloadPluginConfig() {
        reloadConfig()
    }
}
