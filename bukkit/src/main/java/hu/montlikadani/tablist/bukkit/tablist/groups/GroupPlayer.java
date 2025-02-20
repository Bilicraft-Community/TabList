package hu.montlikadani.tablist.bukkit.tablist.groups;

import static hu.montlikadani.tablist.bukkit.utils.Util.colorMsg;

import org.bukkit.entity.Player;

import hu.montlikadani.tablist.bukkit.TabList;
import hu.montlikadani.tablist.bukkit.config.constantsLoader.ConfigValues;
import hu.montlikadani.tablist.bukkit.tablist.groups.impl.ITabScoreboard;
import hu.montlikadani.tablist.bukkit.tablist.groups.impl.ReflectionHandled;
import hu.montlikadani.tablist.bukkit.user.TabListUser;
import hu.montlikadani.tablist.bukkit.utils.PluginUtils;
import hu.montlikadani.tablist.bukkit.utils.task.DelayedPermissionCheck;

public final class GroupPlayer {

	private final TabListUser tabListUser;
	private final ITabScoreboard tabTeam;
	private final TabList tl;

	private TeamHandler group, globalGroup;

	private String customPrefix, customSuffix, playerVaultGroup;

	private int customPriority = Integer.MIN_VALUE;
	private int safePriority = 0;

	public GroupPlayer(TabList tl, TabListUser tabListUser) {
		this.tl = tl;
		this.tabListUser = tabListUser;

		tabTeam = new ReflectionHandled(tl);
	}

	public ITabScoreboard getTabTeam() {
		return tabTeam;
	}

	public void setGroup(TeamHandler group) {
		this.group = group;
		tl.getGroups().setToSort(true);
	}

	public TeamHandler getGroup() {
		return group;
	}

	public String getFullGroupTeamName() {
		return "tablist" + safePriority;
	}

	public void removeGroup() {
		group = globalGroup = null;
	}

	public TabListUser getUser() {
		return tabListUser;
	}

	public void setCustomPrefix(String customPrefix) {
		this.customPrefix = customPrefix;
	}

	public void setCustomSuffix(String customSuffix) {
		this.customSuffix = customSuffix;
	}

	public void setCustomPriority(int customPriority) {
		this.customPriority = customPriority;
	}

	/**
	 * Sets variable safePriority.
	 *
	 * @param safePriority Safe priority value. Should be between 0 and 999999999.
	 */
	public void setSafePriority(int safePriority) {
		if (safePriority < 0) {
			safePriority = 0;
		} else if (safePriority > 999999999) {
			safePriority = 999999999;
		}

		this.safePriority = safePriority;
	}

	public int getPriority() {
		return customPriority == Integer.MIN_VALUE ? group == null ? Integer.MAX_VALUE : group.getPriority()
				: customPriority;
	}

	public boolean update() {
		Player player = tabListUser.getPlayer();
		if (player == null) {
			return false;
		}

		if (!isPlayerCanSeeGroup(player) || (ConfigValues.isAfkStatusEnabled() && PluginUtils.isAfk(player)
				&& !ConfigValues.isAfkStatusShowPlayerGroup())) {
			if (group != null || globalGroup != null) {
				tl.getGroups().removePlayerGroup(tabListUser);
				tl.getGroups().setToSort(false);
			}

			return false;
		}

		boolean update = false;
		Groups groups = tl.getGroups();

		for (TeamHandler team : groups.getGroupsList()) {
			if (player.getName().equalsIgnoreCase(team.getTeam())) {
				if (!team.isGlobal()) {
					for (TeamHandler t : groups.getGroupsList()) {
						if (t.isGlobal() && globalGroup != t) {
							globalGroup = t;
							groups.setToSort(true);
							break;
						}
					}
				}

				if (group != team) {
					update = true;
					setGroup(team);
				}

				return update;
			}
		}

		if (tl.hasVault()) {
			boolean found = false;

			if (playerVaultGroup != null) {
				for (String g : tl.getVaultPerm().getPlayerGroups(player)) {
					if (playerVaultGroup.equalsIgnoreCase(g)) {
						found = true;
						break;
					}
				}
			}

			// Avoiding verbose spam
			if (!found && ConfigValues.isPreferPrimaryVaultGroup()) {
				playerVaultGroup = tl.getVaultPerm().getPrimaryGroup(player);
			}
		}

		for (TeamHandler team : groups.getGroupsList()) {
			if (playerVaultGroup != null && playerVaultGroup.equalsIgnoreCase(team.getTeam())) {
				if (!team.isGlobal()) {
					for (TeamHandler t : groups.getGroupsList()) {
						if (t.isGlobal() && globalGroup != t) {
							groups.setToSort(true);
							globalGroup = t;
							break;
						}
					}
				}

				if (group != team) {
					update = true;
					setGroup(team);
				}

				return update;
			}

			if (team.isGlobal() && globalGroup != team) {
				globalGroup = team;
				groups.setToSort(true);
				continue;
			}

			if (!team.getPermission().isEmpty()) {
				if (DelayedPermissionCheck.hasDelay(team.getPermission(), 4)) {
					break;
				}

				if (PluginUtils.hasPermission(player, team.getPermission())) {
					if (group != team) {
						update = true;
						setGroup(team);
					}

					break;
				}
			} else if (tl.hasVault()) {
				for (String playerGroup : tl.getVaultPerm().getPlayerGroups(player)) {
					if (playerGroup.equalsIgnoreCase(team.getTeam())) {
						if (group != team) {
							update = true;
							setGroup(team);
						}

						break;
					}
				}
			}
		}

		return update;
	}

	private boolean isPlayerCanSeeGroup(Player player) {
		boolean containsWorld = ConfigValues.getGroupsDisabledWorlds().contains(player.getWorld().getName());

		if (((ConfigValues.isUseDisabledWorldsAsWhiteList() && !containsWorld)
				|| (!ConfigValues.isUseDisabledWorldsAsWhiteList() && containsWorld)) || PluginUtils.isInGame(player)) {
			return false;
		}

		if ((ConfigValues.isHideGroupInVanish() && PluginUtils.isVanished(player))
				|| (ConfigValues.isHideGroupWhenAfk() && PluginUtils.isAfk(player))) {
			tabTeam.unregisterTeam(this);
			return false;
		}

		return true;
	}

	public String getPrefix() {
		String prefix = customPrefix == null ? group == null ? "" : group.getPrefix() : customPrefix;

		if ((ConfigValues.isAssignGlobalGroup() && globalGroup != null && !prefix.isEmpty())
				|| (globalGroup != null && prefix.isEmpty())) {
			prefix = globalGroup.getPrefix() + prefix;
		}

		Player player = tabListUser.getPlayer();

		if (player != null && ConfigValues.isAfkStatusEnabled() && !ConfigValues.isAfkStatusShowInRightLeftSide()) {
			prefix = colorMsg(
					PluginUtils.isAfk(player) ? ConfigValues.getAfkFormatYes() : ConfigValues.getAfkFormatNo())
					+ prefix;
		}

		return prefix.isEmpty() ? prefix : tl.getPlaceholders().replaceVariables(player, tl.makeAnim(prefix));
	}

	public String getSuffix() {
		String suffix = customSuffix == null ? group == null ? "" : group.getSuffix() : customSuffix;

		if ((ConfigValues.isAssignGlobalGroup() && globalGroup != null && !suffix.isEmpty())
				|| (globalGroup != null && suffix.isEmpty())) {
			suffix += globalGroup.getSuffix();
		}

		Player player = tabListUser.getPlayer();

		if (player != null && ConfigValues.isAfkStatusEnabled() && ConfigValues.isAfkStatusShowInRightLeftSide()) {
			suffix += colorMsg(
					PluginUtils.isAfk(player) ? ConfigValues.getAfkFormatYes() : ConfigValues.getAfkFormatNo());
		}

		return suffix.isEmpty() ? suffix : tl.getPlaceholders().replaceVariables(player, tl.makeAnim(suffix));
	}

	public String getTabNameWithPrefixSuffix() {
		Player player = tabListUser.getPlayer();
		String tabName = player != null ? player.getName() : "";

		if (ConfigValues.isAssignGlobalGroup() && globalGroup != null && !globalGroup.getTabName().isEmpty()) {
			tabName = tl.getPlaceholders().replaceVariables(player, tl.makeAnim(globalGroup.getTabName()));
		} else if (group != null && !group.getTabName().isEmpty()) {
			tabName = tl.getPlaceholders().replaceVariables(player, tl.makeAnim(group.getTabName()));
		}

		return getPrefix() + tabName + getSuffix();
	}
}
