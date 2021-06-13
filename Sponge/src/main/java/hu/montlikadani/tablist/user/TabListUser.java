package hu.montlikadani.tablist.user;

import org.spongepowered.api.entity.living.player.Player;

import hu.montlikadani.tablist.tablist.TabListManager;
import hu.montlikadani.tablist.tablist.groups.TabGroupPlayer;

public interface TabListUser {

	java.util.UUID getUniqueId();

	java.util.Optional<Player> getPlayer();

	TabGroupPlayer getTabPlayer();

	TabListManager getTabListManager();

}
