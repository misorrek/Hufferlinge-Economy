package huff.economy.menuholder;

import java.util.UUID;

import org.apache.commons.lang.Validate;
import org.jetbrains.annotations.NotNull;

public class Trader 
{
	public Trader(@NotNull UUID uuid)
	{
		Validate.notNull((Object) uuid, "The trader uuid cannot be null.");
		
		this.uuid = uuid;
	}
	
	private final UUID uuid;
	private double value = 0;
	private int items = 0;
	private boolean valueChoosing = false;
	private boolean ready = false;

	@NotNull
	public UUID getUUID()
	{
		return uuid;
	}
	
	public double getValue() 
	{
		return value;
	}
	
	public void addValue(double value) 
	{
		this.value += value;
	}

	public int getItems() 
	{
		return items;
	}

	public void addItems(int items) 
	{
		this.items += items;
	}

	public boolean isValueChoosing() 
	{
		return valueChoosing;
	}

	public void setValueChoosing(boolean valueChoosing) 
	{
		this.valueChoosing = valueChoosing;
	}

	public boolean isReady() 
	{
		return ready;
	}

	public void changeReady() 
	{
		ready = !ready;
	}
}
