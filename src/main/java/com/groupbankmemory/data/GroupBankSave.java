package com.groupbankmemory.data;

import com.google.common.annotations.VisibleForTesting;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;
import javax.annotation.Nullable;
import lombok.Value;

@Value
public class GroupBankSave
{

	long id;
	private static final long ID_BASE = System.currentTimeMillis();
	private static final AtomicInteger idIncrementer = new AtomicInteger();
	String userName;
	@Nullable String saveName;
	List<BankSave> bankSaves;

	@VisibleForTesting
	public GroupBankSave(
		String userName,
		@Nullable String saveName,
		List<BankSave> bankSaves)
	{
		id = ID_BASE + idIncrementer.incrementAndGet();
		this.userName = userName;
		this.saveName = saveName;
		this.bankSaves = bankSaves;
	}

	public void addToBankSaves(BankSave newSave)
	{
		bankSaves.add(newSave);
	}

	public static GroupBankSave snapshotFromExistingBank(String newName, GroupBankSave existingBank)
	{
		Objects.requireNonNull(newName);
		return new GroupBankSave(
			existingBank.userName,
			newName,
			existingBank.bankSaves);
	}

	public static GroupBankSave cleanItemData(GroupBankSave existingBank)
	{
		Objects.requireNonNull(existingBank);
		if (existingBank.bankSaves == null)
		{
			return existingBank;
		}

		List<BankSave> cleanBanks = new ArrayList<>();
		existingBank.bankSaves.forEach(s -> cleanBanks.add(BankSave.cleanItemData(s)));

		return new GroupBankSave(
			existingBank.userName,
			existingBank.saveName,
			cleanBanks);
	}
}
