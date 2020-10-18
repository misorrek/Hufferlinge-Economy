package huff.economy;

import org.apache.commons.lang.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

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
	
	public static boolean isTransaction(@NotNull String transactionLabel)
	{
		return getTransaction(transactionLabel) != null;
	}
	
	public static @Nullable TransactionKind getTransaction(@NotNull String transactionLabel)
	{
		for (TransactionKind transactionKind : TransactionKind.values())
		{
			if (StringUtils.containsIgnoreCase(transactionLabel, transactionKind.getLabel()))
			{
				return transactionKind;
			}
		}
		return null;
	}	
		
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
