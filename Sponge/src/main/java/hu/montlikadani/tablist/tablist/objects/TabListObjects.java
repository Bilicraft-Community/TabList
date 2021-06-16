package hu.montlikadani.tablist.tablist.objects;

import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.scheduler.Task;
import org.spongepowered.api.scoreboard.Score;
import org.spongepowered.api.scoreboard.Scoreboard;
import org.spongepowered.api.scoreboard.critieria.Criteria;
import org.spongepowered.api.scoreboard.displayslot.DisplaySlots;
import org.spongepowered.api.scoreboard.objective.Objective;
import org.spongepowered.api.scoreboard.objective.displaymode.ObjectiveDisplayModes;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;
import org.spongepowered.api.text.serializer.TextSerializers;

import hu.montlikadani.tablist.TabList;
import hu.montlikadani.tablist.config.ConfigValues;
import hu.montlikadani.tablist.user.TabListUser;
import hu.montlikadani.tablist.utils.Util;

public class TabListObjects {

	private final TabList plugin;

	private Task task;

	private final AtomicInteger value = new AtomicInteger();
	private final Scoreboard board = Util.GLOBAL_SCORE_BOARD;

	public TabListObjects(TabList plugin) {
		this.plugin = plugin;
	}

	public void cancelTask() {
		if (!isCancelled()) {
			task.cancel();
			task = null;
		}
	}

	public boolean isCancelled() {
		return task == null;
	}

	public void unregisterAllObjective() {
		for (ObjectType types : ObjectType.values()) {
			unregisterObjective(types.getName());
		}
	}

	public void unregisterObjective(String objectName) {
		board.clearSlot(DisplaySlots.LIST);
		getObjective(objectName).ifPresent(board::removeObjective);
	}

	public Optional<Objective> getObjective(String name) {
		return board.getObjective(name);
	}

	public void loadObjects() {
		cancelTask();

		if (plugin.getTabUsers().isEmpty()) {
			return;
		}

		final ObjectType type = ConfigValues.getTablistObjectsType();
		if (type == ObjectType.NONE) {
			return;
		}

		if (type == ObjectType.HEARTH) {
			for (TabListUser user : plugin.getTabUsers()) {
				user.getPlayer().ifPresent(player -> loadHealthObject(player));
			}

			return;
		}

		int interval = ConfigValues.getObjectsRefreshInterval();
		if (interval < 1) {
			return;
		}

		task = Task.builder().async().interval(interval, TimeUnit.SECONDS).execute(() -> {
			if (plugin.getTabUsers().isEmpty()) {
				cancelTask();
				return;
			}

			plugin.getTabUsers().forEach(user -> user.getPlayer().ifPresent(player -> {
				if (type == ObjectType.PING) {
					value.set(player.getConnection().getLatency());
				} else if (type == ObjectType.CUSTOM) {
					String result = TextSerializers.PLAIN
							.serialize(plugin.getVariables().replaceVariables(player, ConfigValues.getCustomObject()));

					result = result.replaceAll("[^\\d]", "");

					try {
						value.set(Integer.parseInt(result));
					} catch (NumberFormatException e) {
						TabList.LOGGER.warn("Not correct custom objective: " + ConfigValues.getCustomObject());
					}
				}

				Optional<Objective> opt = getObjective(type.getName());
				Objective object = opt
						.orElse(Objective.builder().displayName(Text.of("tabObjects")).name(type.getName())
								.objectiveDisplayMode(ObjectiveDisplayModes.INTEGER).criterion(Criteria.DUMMY).build());

				if (!opt.isPresent()) {
					board.addObjective(object);
					board.updateDisplaySlot(object, DisplaySlots.LIST);
				}

				Optional<Score> score = object.getScore(Text.of(player.getName()));

				if (!score.isPresent() || score.get().getScore() != value.get()) {
					plugin.getTabUsers().forEach(us -> us.getPlayer().ifPresent(pl -> {
						object.getOrCreateScore(Text.of(pl.getName())).setScore(value.get());
						pl.setScoreboard(board);
					}));
				}
			}));
		}).submit(plugin);
	}

	public void loadHealthObject(Player player) {
		String objName = ObjectType.HEARTH.getName();
		Optional<Objective> opt = getObjective(objName);
		Objective object = opt.orElse(Objective.builder().displayName(Text.of(TextColors.RED, "\u2665")).name(objName)
				.objectiveDisplayMode(ObjectiveDisplayModes.HEARTS).criterion(Criteria.HEALTH).build());

		if (!opt.isPresent()) {
			board.addObjective(object);
		}

		board.updateDisplaySlot(object, DisplaySlots.LIST);
		player.setScoreboard(board);
	}
}
