package net.crytec.pickmoney;

import java.util.Arrays;
import java.util.List;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.configuration.file.YamlConfiguration;

public enum ConfigOptions {

  DEFAULT_ICON("options.defaultDropIcon", "GOLD_INGOT"),
  SOUND("options.sound.name", "ENTITY_PLAYER_LEVELUP"),
  SOUND_PITCH("options.sound.pitch", 1.25F),
  SOUND_VOLUME("options.sound.volume", 1.0F),
  SHOW_DISPLAYNAME("options.showDropName", true),
  MERGE_ITEMS("options.mergeDrops", true),
  DROP_NATURALLY("options.dropNaturally", true),
  BLACKLISTED_WORLDS("blacklisted_worlds", Arrays.asList("world2")),
  SHOW_TITLE("options.showTitle", false),
  SHOW_CHAT("options.showChatMessage", true),
  TITLE_HEADER("messages.titleHead", ""),
  TITLE_SUB("messages.subtitle", "&7Picked up %value% money"),
  DROP_DISPLAYNAME("messages.dropDisplayName", "&bValue: %value%"),
  PICKUP_MESSAGE("messages.pickupMessage", "&7Picked up %value% Money"),
  LOST_MONEY("messages.playerdeath", "&7You have lost %value% from your balance because you've died."),
  DO_DECIMAL_DROPS("options.useDecimalNumbers", true),
  MINIMUM_TO_DROP("options.minimumAmountToDrop", 0.01D),
  REQUIRE_PLAYERKILL("options.requirePlayerAsKiller", false),


  PLAYER_DROP_ENABLED("player.enabled", false),
  PLAYER_DROP_PERCENTAGE("player.percentageAmount", "0.5-2.45"),
  PLAYER_DROP_CHANCE("player.chance", 20),
  PLAYER_DROP_HARDCAP("player.hardcap", 2500);


  private final String path;
  private final Object def;

  private static YamlConfiguration CONFIG;

  private ConfigOptions(final String path, final Object object) {
    this.path = path;
    this.def = object;
  }

  public boolean asBoolean() {
    return CONFIG.getBoolean(this.path);
  }

  public double asDouble() {
    return CONFIG.getDouble(this.path);
  }

  public int asInt() {
    return CONFIG.getInt(this.path);
  }

  public float asFloat() {
    return (float) CONFIG.getDouble(this.path);
  }

  public String asString() {
    return CONFIG.getString(this.path);
  }

  public String asString(final boolean translateColor) {
    return ChatColor.translateAlternateColorCodes('&', CONFIG.getString(this.path));
  }

  public List<String> asStringList() {
    return CONFIG.getStringList(this.path);
  }


  /**
   * Set the {@code YamlConfiguration} to use.
   *
   * @param config The config to set.
   */
  public static void setFile(final YamlConfiguration config) {
    CONFIG = config;
  }

  public static YamlConfiguration getFile() {
    return CONFIG;
  }

  /**
   * Get the default value of the path.
   *
   * @return The default value of the path.
   */
  public Object getDefault() {
    return this.def;
  }

  /**
   * Get the path to the string.
   *
   * @return The path to the string.
   */
  public String getPath() {
    return this.path;
  }

}
