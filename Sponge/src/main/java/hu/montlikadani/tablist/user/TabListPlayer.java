package hu.montlikadani.tablist.user;

import java.util.UUID;

import org.spongepowered.api.Sponge;
import org.spongepowered.api.entity.living.player.Player;

import hu.montlikadani.tablist.tablist.TabListManager;
import hu.montlikadani.tablist.tablist.groups.TabGroupPlayer;

public final class TabListPlayer implements TabListUser {

	private final UUID uuid;
	private final TabGroupPlayer tabGroupPlayer;
	private final TabListManager tabListManager;

	public TabListPlayer(UUID uuid) {
		this.uuid = uuid;

		tabGroupPlayer = new TabGroupPlayer(this);
		tabListManager = new TabListManager(this);
	}

	@Override
	public UUID getUniqueId() {
		return uuid;
	}

	@Override
	public TabGroupPlayer getTabPlayer() {
		return tabGroupPlayer;
	}

	@Override
	public TabListManager getTabListManager() {
		return tabListManager;
	}

	@Override
	public java.util.Optional<Player> getPlayer() {
		return Sponge.getGame().getServer().getPlayer(uuid);
	}

	@Override
	public boolean equals(Object o) {
		return o == this || (o instanceof TabListPlayer && uuid.equals(((TabListPlayer) o).uuid));
	}
}
