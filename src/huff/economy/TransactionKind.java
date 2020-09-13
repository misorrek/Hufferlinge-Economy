package huff.economy;

import org.jetbrains.annotations.NotNull;

public enum TransactionKind
{
	BANK_OUT("Auszahlen"),
	BANK_OTHER("Übertragen"), 
	WALLET_OUT("Herausnehmen"),
	WALLET_OUT_BANK("Einzahlen");
	
	private TransactionKind(@NotNull String label)
	{
		this.label = label;
	}
	
	private final String label;
		
	public @NotNull String getLabel()
	{
		return this.label;
	}
	
	public @NotNull String getLowerLabel()
	{
		return this.label.toLowerCase();
	}
	
	public boolean isBankTransaction()
	{
		return this == BANK_OUT || this == BANK_OTHER;
	}
	
	public boolean isWalletTransaction()
	{
		return this == WALLET_OUT || this == WALLET_OUT_BANK;
	}
}
