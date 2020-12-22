package huff.economy.menuholder;

import org.apache.commons.lang.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public enum TransactionKind
{
	BANK_IN("Einzahlen"),
	BANK_OUT("Auszahlen"),
	BANK_OTHER("Übertragen"), 
	BANK_CHOOSE("Hinzufügen"),
	WALLET_OUT("Herausnehmen"),
	WALLET_OTHER("Übergeben"),
	WALLET_CHOOSE("Hinzufügen");
	
	private TransactionKind(String label)
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
		return this == BANK_OUT || this == BANK_OTHER || this == BANK_CHOOSE;
	}
	
	public boolean isWalletTransaction()
	{
		return this == WALLET_OUT || this == WALLET_OTHER || this == WALLET_CHOOSE || this == BANK_IN;
	}
	
	public boolean isHumanTransaction()
	{
		return this == BANK_OTHER || this == WALLET_OTHER;
	}
	
	public boolean isChooseTransaction()
	{
		return this == BANK_CHOOSE || this == WALLET_CHOOSE;
	}
	
	public boolean isItemTransaction()
	{
		return this == WALLET_OUT;
	}
}
