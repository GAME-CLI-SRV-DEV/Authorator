package org.duckdns.anarchyconnect.viaproxy;

import net.raphimc.viaproxy.api.ViaProxyPlugin;
import net.raphimc.viaproxy.api.event.PlayerJoinEvent;
import net.raphimc.viaproxy.api.event.EventHandler;
import net.raphimc

public class KickScreenAuthPlugin extends ViaProxyPlugin {

    @Override
    public void onEnable() {
        getLogger().info("KickScreenAuthPlugin enabled!");
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        if (!authenticate(event.getPlayer())) {
            event.getPlayer().kick("Authentication: ");
        }
    }

    private boolean authenticate(Player player) {
        // Implement your authentication logic here
        return false; // For demonstration, always fail authentication
    }
}

