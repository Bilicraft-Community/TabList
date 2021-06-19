package hu.montlikadani.tablist.utils;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

import org.spongepowered.api.Sponge;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.serializer.TextSerializers;

import hu.montlikadani.tablist.config.ConfigValues;
import hu.montlikadani.tablist.utils.operators.ExpressionNode;
import hu.montlikadani.tablist.utils.operators.OperatorNodes;

public final class Variables {

	private final List<ExpressionNode> nodes = new ArrayList<>();
	private final java.util.Set<String> symbols = new java.util.HashSet<>();

	public void loadExpressions() {
		nodes.clear();

		if (symbols.isEmpty()) {
			symbols.addAll(java.util.Arrays.asList("•", "➤", "™", "↑", "→", "↓", "∞", "░", "▲", "▶", "◀", "●", "★", "☆",
					"☐", "☑", "☠", "☢", "☣", "☹", "☺", "✓", "✔", "✘", "✚", "℻", "✠", "✡", "✦", "✧", "✩", "✪", "✮", "✯",
					"㋡", "❝", "❞", "ツ", "♩", "♪", "♫", "♬", "♭", "♮", "♯", "¶", "\u00A9", "\u00AE", "⏎", "⇧", "⇪", "ᴴᴰ",
					"☒", "♠", "♣", "☻", "▓", "➾", "➔", "➳", "➧", "《", "》", "︾", "︽", "☃", "¹", "²", "³", "≈", "℠",
					"\u2665", "✬", "↔", "«", "»", "☀", "♦", "₽", "☎", "☂", "←", "↖", "↗", "↘", "↙", "➲", "✐", "✎", "✏",
					"✆", "◄", "☼", "►", "↕", "▼", "①", "②", "③", "④", "⑤", "⑥", "⑦", "⑧", "⑨", "⑩", "⑪", "⑫", "⑬", "⑭",
					"⑮", "⑯", "⑰", "⑱", "⑲", "⑳", "♨", "✑", "✖", "✰", "✶", "╗", "╣", "◙", "○", "╠", "┤", "║", "╝", "⌂",
					"┐", "❉", "⌲", "½", "¼", "¾", "⅓", "⅔", "№", "†", "‡", "µ", "¢", "£", "∅", "≤", "≥", "≠", "∧", "∨",
					"∩", "∪", "∈", "∀", "∃", "∄", "∑", "∏", "↺", "↻", "Ω"));
		}

		if (!ConfigValues.isPingFormatEnabled()) {
			return;
		}

		for (String f : ConfigValues.getPingColorFormats()) {
			ExpressionNode node = new OperatorNodes(f);

			if (node.getCondition() != null) {
				nodes.add(node);
			}
		}

		int size = nodes.size();

		// Sort ping in descending order
		for (int i = 0; i < size; i++) {
			for (int j = size - 1; j > i; j--) {
				ExpressionNode node = nodes.get(i), node2 = nodes.get(j);

				if (node.getCondition().getSecondCondition() < node2.getCondition().getSecondCondition()) {
					nodes.set(i, node2);
					nodes.set(j, node);
				}
			}
		}
	}

	public Text replaceVariables(Player player, String str) {
		if (str.isEmpty()) {
			return TextSerializers.FORMATTING_CODE.deserialize(str);
		}

		str = setSymbols(str);

		str = str.replace("%player%", player.getName());
		str = str.replace("%servertype%", Sponge.getPlatform().getType().name());
		str = str.replace("%mc-version%", Sponge.getPlatform().getMinecraftVersion().getName());
		str = str.replace("%motd%", Sponge.getGame().getServer().getMotd().toPlain());
		str = str.replace("%world%", player.getWorld().getName());

		if (str.indexOf("%player-ping%") >= 0) {
			str = str.replace("%player-ping%", formatPing(player.getConnection().getLatency()));
		}

		if (str.indexOf("%player-uuid%") >= 0) {
			str = str.replace("%player-uuid%", player.getUniqueId().toString());
		}

		if (str.indexOf("%player-level%") >= 0) {
			str = str.replace("%player-level%", Integer.toString(player.get(Keys.EXPERIENCE_LEVEL).orElse(0)));
		}

		if (str.indexOf("%player-total-level%") >= 0) {
			str = str.replace("%player-total-level%", Integer.toString(player.get(Keys.TOTAL_EXPERIENCE).orElse(0)));
		}

		if (str.indexOf("%player-health%") >= 0) {
			DecimalFormat df = (DecimalFormat) NumberFormat.getNumberInstance(Locale.US);
			df.applyPattern("#0.0");

			str = str.replace("%player-health%", df.format(player.getHealthData().health().get()));
		}

		if (str.indexOf("%player-max-health%") >= 0) {
			str = str.replace("%player-max-health%", Double.toString(player.getHealthData().maxHealth().get()));
		}

		Runtime runtime = Runtime.getRuntime();

		if (str.indexOf("%server-ram-free%") >= 0) {
			str = str.replace("%server-ram-free%", Long.toString(runtime.freeMemory() / 1048576L));
		}

		if (str.indexOf("%server-ram-max%") >= 0) {
			str = str.replace("%server-ram-max%", Long.toString(runtime.maxMemory() / 1048576L));
		}

		if (str.indexOf("%server-ram-used%") >= 0) {
			str = str.replace("%server-ram-used%",
					Long.toString((runtime.totalMemory() - runtime.freeMemory()) / 1048576L));
		}

		if (str.indexOf("%online-players%") >= 0)
			str = str.replace("%online-players%",
					Integer.toString(Sponge.getGame().getServer().getOnlinePlayers().size()));

		if (str.indexOf("%max-players%") >= 0)
			str = str.replace("%max-players%", Integer.toString(Sponge.getGame().getServer().getMaxPlayers()));

		if (str.indexOf("%server-time%") >= 0) {
			str = str.replace("%server-time%", getTimeAsString(ConfigValues.getTimeFormat()));
		}

		if (str.indexOf("%date%") >= 0) {
			str = str.replace("%date%", getTimeAsString(ConfigValues.getDateFormat()));
		}

		if (str.indexOf("%ip-address%") >= 0) {
			java.net.InetAddress inetAddress = player.getConnection().getAddress().getAddress();

			if (inetAddress != null) {
				str = str.replace("%ip-address%", inetAddress.toString().replace("/", ""));
			}
		}

		if (str.indexOf("%tps%") >= 0) {
			DecimalFormat df = (DecimalFormat) NumberFormat.getNumberInstance(Locale.US);
			df.applyPattern("#0.00");

			str = str.replace("%tps%", df.format(Sponge.getGame().getServer().getTicksPerSecond()));
		}

		if (str.indexOf("%staff-online%") >= 0) {
			int staffs = 0;

			for (Player all : Sponge.getGame().getServer().getOnlinePlayers()) {
				if (all.hasPermission("tablist.onlinestaff")) {
					staffs++;
				}
			}

			if (staffs != 0) {
				str = str.replace("%staff-online%", Integer.toString(staffs));
			}
		}

		return TextSerializers.FORMATTING_CODE.deserialize(str);
	}

	private String formatPing(int ping) {
		if (!ConfigValues.isPingFormatEnabled() || ConfigValues.getPingColorFormats().isEmpty()) {
			return "" + ping;
		}

		return parseExpression(ping);
	}

	private String parseExpression(int value) {
		String color = "";

		for (ExpressionNode node : nodes) {
			if (node.parse(value)) {
				color = node.getCondition().getColor();
			}
		}

		StringBuilder builder = new StringBuilder();
		if (!color.isEmpty()) {
			builder.append(color.replace('&', '\u00a7'));
		}

		return builder.append(value).toString();
	}

	private String getTimeAsString(String pattern) {
		if (pattern.isEmpty()) {
			return pattern;
		}

		TimeZone zone = ConfigValues.isUseSystemZone() ? TimeZone.getTimeZone(java.time.ZoneId.systemDefault())
				: TimeZone.getTimeZone(ConfigValues.getTimeZone());
		LocalDateTime now = zone == null ? LocalDateTime.now() : LocalDateTime.now(zone.toZoneId());

		return now.format(DateTimeFormatter.ofPattern(pattern));
	}

	private String setSymbols(String s) {
		if (s.indexOf('<') < 0) {
			return s;
		}

		int i = -1;

		for (String symbol : symbols) {
			String sym = "<" + ++i + ">";

			if (s.indexOf(sym) >= 0) {
				s = s.replace(sym, symbol);
			}
		}

		return s;
	}
}
