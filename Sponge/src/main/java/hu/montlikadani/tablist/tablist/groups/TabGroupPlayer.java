package hu.montlikadani.tablist.tablist.groups;

import java.util.Optional;

import org.spongepowered.api.Sponge;
import org.spongepowered.api.entity.living.player.Player;

import hu.montlikadani.tablist.TabList;
import hu.montlikadani.tablist.user.TabListUser;

public class TabGroupPlayer implements Comparable<TabGroupPlayer> {

	private final TabListUser user;

	private TabList tl;
	private TabGroup group;

	public TabGroupPlayer(TabListUser user) {
		this.user = user;

		Sponge.getPluginManager().getPlugin("tablist")
				.ifPresent(container -> container.getInstance().ifPresent(inst -> tl = (TabList) inst));
	}

	public TabListUser getUser() {
		return user;
	}

	public void setGroup(TabGroup group) {
		this.group = group;
	}

	public Optional<TabGroup> getGroup() {
		return Optional.ofNullable(group);
	}

	public boolean update() {
		Optional<Player> opt = user.getPlayer();
		if (!opt.isPresent()) {
			return false;
		}

		Player player = opt.get();
		boolean update = false;

		for (TabGroup tabGroup : tl.getGroupsList()) {
			if (tabGroup.getGroupName().equalsIgnoreCase(player.getName())) {
				if (tabGroup != group) {
					update = true;
					setGroup(tabGroup);
				}

				return update;
			}

			if (!tabGroup.getPermission().isEmpty() && player.hasPermission(tabGroup.getPermission())
					&& tabGroup != group) {
				update = true;
				setGroup(tabGroup);
				break;
			}
		}

		return update;
	}

	@Override
	public int compareTo(TabGroupPlayer tabGroupPlayer) {
		int ownPriority = group != null ? group.getPriority() : 0;

		Optional<TabGroup> g = tabGroupPlayer.getGroup();
		int tlpPriority = g.isPresent() ? g.get().getPriority() : 0;

		// dirty
		if (ownPriority == tlpPriority) {
			Optional<Player> player = user.getPlayer();

			if (player.isPresent()) {
				Optional<Player> player2 = tabGroupPlayer.getUser().getPlayer();

				if (player2.isPresent()) {
					return player.get().getName().compareTo(player2.get().getName());
				}
			}
		}

		return ownPriority - tlpPriority;
	}
}
