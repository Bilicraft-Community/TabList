package hu.montlikadani.tablist.bukkit.tablist.entry.row.variable.list;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.bukkit.World;
import org.bukkit.entity.Player;

import hu.montlikadani.tablist.bukkit.config.constantsLoader.TabEntryValues;
import hu.montlikadani.tablist.bukkit.config.constantsLoader.TabEntryValues.PlaceholderSetting;
import hu.montlikadani.tablist.bukkit.tablist.entry.row.RowPlayer;
import hu.montlikadani.tablist.bukkit.tablist.entry.row.variable.AbstractVariable;

public final class WorldPlayers extends AbstractVariable {

	public WorldPlayers(RowPlayer rowPlayer) {
		super(rowPlayer, "%world_players_");
	}

	@Override
	public String replace(String text) {
		if (!text.contains(replacement)) {
			return null;
		}

		List<World> worlds = plugin.getServer().getWorlds();

		for (int w = 0; w < worlds.size(); w++) {
			World world = worlds.get(w);
			String replacement = this.replacement + world.getName() + "%";

			if (!StringUtils.contains(text, replacement)) {
				replacement = this.replacement + w + "%";
			}

			if (!StringUtils.contains(text, replacement)) {
				continue;
			}

			PlaceholderSetting ps = TabEntryValues.VARIABLE_SETTINGS
					.get(PlaceholderSetting.SettingType.WORLD_PLAYERS);
			if (ps == null) {
				return StringUtils.replace(text, replacement, "");
			}

			List<String> collectedPlayerNames = new ArrayList<>();

			for (Player player : world.getPlayers()) {
				if (collectedPlayerNames.size() >= ps.getMax()) {
					break;
				}

				if (canAddPlayer(player, ps)) {
					collectedPlayerNames.add(player.getName());
				}
			}

			return setText(text, collectedPlayerNames, replacement, true);
		}

		return null;
	}
}
