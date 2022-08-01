package com.groupbankmemory.data;

public interface DataStoreUpdateListener
{
	void currentBanksListChanged();

	void snapshotBanksListChanged();

	void displayNameMapUpdated();
}
