package net.crytec.pickmoney.api;

import org.bukkit.entity.EntityType;

import com.google.common.collect.Range;

import lombok.Data;

@Data
public class EntityDropData {

	private final EntityType type;
	private final Range<Double> range;
	private final double chance;
	
}
