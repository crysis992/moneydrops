package net.crytec.pickmoney.api;

import com.google.common.collect.Range;
import lombok.Data;
import org.bukkit.entity.EntityType;

@Data
public class EntityDropData {

  private final EntityType type;
  private final Range<Double> range;
  private final double chance;

}
