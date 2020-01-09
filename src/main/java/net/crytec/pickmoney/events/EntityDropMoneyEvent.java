package net.crytec.pickmoney.events;

import lombok.Getter;
import lombok.Setter;
import org.bukkit.entity.Entity;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;
import org.bukkit.event.entity.EntityEvent;

public class EntityDropMoneyEvent extends EntityEvent implements Cancellable {

  private static final HandlerList handlers = new HandlerList();
  private boolean cancel = false;
  @Getter
  @Setter
  private double amount;

  public EntityDropMoneyEvent(final Entity entity, final double amount) {
    super(entity);
  }

  @Override
  public HandlerList getHandlers() {
    return handlers;
  }

  public static HandlerList getHandlerList() {
    return handlers;
  }

  @Override
  public boolean isCancelled() {
    return this.cancel;
  }

  @Override
  public void setCancelled(final boolean cancel) {
    this.cancel = cancel;
  }

}
