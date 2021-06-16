package hu.montlikadani.tablist.tablist;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

import org.spongepowered.api.Sponge;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.text.Text;

import hu.montlikadani.tablist.TabList;
import hu.montlikadani.tablist.config.ConfigManager;
import hu.montlikadani.tablist.config.ConfigValues;
import hu.montlikadani.tablist.user.TabListUser;
import hu.montlikadani.tablist.utils.Variables;
import ninja.leaping.configurate.ConfigurationNode;

public class TabListManager {

	private final TabListUser user;

	private TabList tl;
	private String[] header, footer;

	private final List<String> worldList = new java.util.ArrayList<>();

	public TabListManager(TabListUser user) {
		this.user = user;

		Sponge.getPluginManager().getPlugin("tablist")
				.ifPresent(container -> container.getInstance().ifPresent(inst -> tl = (TabList) inst));
	}

	public TabListUser getUser() {
		return user;
	}

	public void loadTab() {
		worldList.clear();

		if (!ConfigValues.isTablistEnabled()) {
			return;
		}

		Player player = user.getPlayer().orElse(null);
		if (player == null) {
			return;
		}

		sendTabList(player, Text.EMPTY, Text.EMPTY);

		if (TabHandler.TABENABLED.getOrDefault(user.getUniqueId(), false)) {
			return;
		}

		final ConfigManager conf = tl.getConfig().get();

		ConfigurationNode node = conf.getNode("tablist", "header");

		if (conf.isList(node)) {
			header = conf.getAsList(node).toArray(new String[0]);
		} else if (conf.isString(node)) {
			header = new String[] { node.getString() };
		}

		node = conf.getNode("tablist", "footer");

		if (conf.isList(node)) {
			footer = conf.getAsList(node).toArray(new String[0]);
		} else if (conf.isString(node)) {
			footer = new String[] { node.getString() };
		}

		node = conf.getNode("tablist", "per-world");

		if (!conf.contains(node) || !node.isMap()) {
			return;
		}

		t: for (Object w : node.getChildrenMap().keySet()) {
			for (String split : w.toString().split(", ")) {
				if (player.getWorld().getName().equals(split)) {
					ConfigurationNode h = node.getNode(w, "header");

					if (conf.isList(h)) {
						header = conf.getAsList(h).toArray(new String[0]);
					} else if (conf.isString(h)) {
						header = new String[] { h.getString() };
					}

					h = node.getNode(w, "footer");

					if (conf.isList(h)) {
						footer = conf.getAsList(h).toArray(new String[0]);
					} else if (conf.isString(h)) {
						footer = new String[] { h.getString() };
					}

					worldList.add(split);
					break t;
				}
			}
		}
	}

	protected void sendTab() {
		if (header == null && footer == null) {
			return;
		}

		if (header != null && header.length == 0 && footer != null && footer.length == 0) {
			return;
		}

		user.getPlayer().filter(player -> player.isOnline()).ifPresent(player -> {
			if (ConfigValues.getTabDisabledWorlds().contains(player.getWorld().getName())
					|| ConfigValues.getTabRestrictedPlayers().contains(player.getName())
					|| TabHandler.TABENABLED.getOrDefault(user.getUniqueId(), false)) {
				sendTabList(player, Text.EMPTY, Text.EMPTY);
				return;
			}

			String he = "";
			int r = 0;

			if (header != null && header.length > 0) {
				if (ConfigValues.isRandomTablist()) {
					he = header[header.length == 1 ? 0 : ThreadLocalRandom.current().nextInt(header.length)];
				}

				if (he.isEmpty()) {
					for (String line : header) {
						r++;

						if (r > 1) {
							he += "\n\u00a7r";
						}

						he += line;
					}
				}
			}

			String fo = "";

			if (footer != null && footer.length > 0) {
				if (ConfigValues.isRandomTablist()) {
					fo = footer[footer.length == 1 ? 0 : ThreadLocalRandom.current().nextInt(footer.length)];
				}

				if (fo.isEmpty()) {
					r = 0;

					for (String line : footer) {
						r++;

						if (r > 1) {
							fo += "\n\u00a7r";
						}

						fo += line;
					}
				}
			}

			if (!he.isEmpty()) {
				he = tl.makeAnim(he);
			}

			if (!fo.isEmpty()) {
				fo = tl.makeAnim(fo);
			}

			final String resultHeader = he;
			final String resultFooter = fo;

			final Variables v = tl.getVariables();

			if (!worldList.isEmpty()) {
				worldList.forEach(worldName -> Sponge.getGame().getServer().getWorld(worldName)
						.ifPresent(world -> world.getPlayers().forEach(pl -> sendTabList(pl,
								v.replaceVariables(pl, resultHeader), v.replaceVariables(pl, resultFooter)))));

				return;
			}

			sendTabList(player, v.replaceVariables(player, resultHeader), v.replaceVariables(player, resultFooter));
		});
	}

	public void sendTabList(Player player, String header, String footer) {
		sendTabList(player, Text.of(header), Text.of(footer));
	}

	public void sendTabList(Player player, Text header, Text footer) {
		player.getTabList().setHeaderAndFooter(header, footer);
	}

	public void clearTab() {
		user.getPlayer().ifPresent(player -> sendTabList(player, Text.EMPTY, Text.EMPTY));
	}
}
