package hu.montlikadani.tablist.config;

import java.nio.file.Path;
import java.util.function.Supplier;

import org.spongepowered.api.Sponge;

import hu.montlikadani.tablist.TabList;

public class ConfigHandlers implements Supplier<ConfigManager> {

	private TabList plugin;
	private String name;
	private boolean setMissing;

	private Path path;
	private ConfigManager config;

	public ConfigHandlers(TabList plugin, String name, boolean setMissing) {
		this.plugin = plugin;
		this.name = name;
		this.setMissing = setMissing;
	}

	@Override
	public ConfigManager get() {
		return config;
	}

	public boolean isExists() {
		return config != null && config.getFile().exists();
	}

	public Path getPath() {
		return path;
	}

	public String getName() {
		return name;
	}

	public boolean isSetMissing() {
		return setMissing;
	}

	public void createFile() {
		path = Sponge.getGame().getConfigManager().getPluginConfig(plugin).getDirectory();
		config = new ConfigManager(path.toString(), name);
		config.createFile();
	}

	public void reload() {
		if (!isExists()) {
			createFile();
		}

		config.load();
		config.save();
	}
}
