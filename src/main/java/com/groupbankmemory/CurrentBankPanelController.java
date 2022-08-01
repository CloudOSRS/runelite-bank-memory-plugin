package com.groupbankmemory;

import com.groupbankmemory.bankview.BankViewPanel;
import com.groupbankmemory.bankview.ItemListEntry;
import com.groupbankmemory.data.BankItem;
import com.groupbankmemory.data.BankSave;
import com.groupbankmemory.data.GroupBankSave;
import com.groupbankmemory.data.PluginDataStore;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.swing.SwingUtilities;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.api.GameState;
import net.runelite.api.ItemComposition;
import net.runelite.api.events.GameStateChanged;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.game.ItemManager;
import net.runelite.client.util.AsyncBufferedImage;

@Slf4j
public class CurrentBankPanelController
{
	@Inject private Client client;
	@Inject private ClientThread clientThread;
	@Inject private ItemManager itemManager;
	@Inject private PluginDataStore dataStore;

	private BankViewPanel panel;

	@Nullable private BankSave latestDisplayedData = null;

	public void startUp(BankViewPanel panel)
	{
		assert client.isClientThread();

		this.panel = panel;

		if (client.getGameState() == GameState.LOGGED_IN)
		{
			updateDisplayForCurrentAccount();
		}
		else
		{
			SwingUtilities.invokeLater(panel::displayNoDataMessage);
		}
	}

	public void onGameStateChanged(GameStateChanged gameStateChanged)
	{
		assert client.isClientThread();

		if (gameStateChanged.getGameState() != GameState.LOGGED_IN)
		{
			return;
		}
		updateDisplayForCurrentAccount();
	}

	private void updateDisplayForCurrentAccount()
	{
		Optional<GroupBankSave> existingSave = dataStore.getDataForCurrentBank(client.getUsername());
		if (existingSave.isPresent())
		{
			handleBankSave(existingSave.get().getBankSaves().get(0));
		}
		else
		{
			latestDisplayedData = null;
			SwingUtilities.invokeLater(panel::displayNoDataMessage);
		}
	}

	public void handleBankSave(BankSave newSave)
	{
		assert client.isClientThread();

		dataStore.saveAsCurrentBank(newSave);

		boolean shouldReset = isBankIdentityDifferentToLastDisplayed(newSave);
		boolean shouldUpdateItemsDisplay = shouldReset || isItemDataNew(newSave);
		List<ItemListEntry> items = new ArrayList<>();
		if (shouldUpdateItemsDisplay)
		{
			dataStore.getDataForCurrentBank(client.getUsername()).ifPresent(gbs ->
			{
				for (BankSave s : gbs.getBankSaves() /* GroupBankSave */)
				{
					for (BankItem i : s.getItemData())
					{
						ItemComposition ic = itemManager.getItemComposition(i.getItemId());
						AsyncBufferedImage icon = itemManager.getImage(i.getItemId(), i.getQuantity(), i.getQuantity() > 1);
						int geValue = itemManager.getItemPrice(i.getItemId()) * i.getQuantity();
						int haValue = ic.getHaPrice() * i.getQuantity();
						items.add(new ItemListEntry(ic.getName(), i.getQuantity(), icon, geValue, haValue));
					}
				}
			});
			// Get all the data we need for the UI on this thread (the game thread)
			// Doing it on the EDT seems to cause random crashes & NPEs

		}
		SwingUtilities.invokeLater(() ->
		{
			if (shouldReset) {
				panel.reset();
			}
			panel.updateTimeDisplay(newSave.getDateTimeString());
			if (shouldUpdateItemsDisplay)
			{
				panel.displayItemListings(items, true);
			}
		});
		latestDisplayedData = newSave;
	}

	private boolean isBankIdentityDifferentToLastDisplayed(BankSave newSave)
	{
		if (latestDisplayedData == null)
		{
			return true;
		}
		return latestDisplayedData.getUserName().equalsIgnoreCase(newSave.getUserName());
	}

	private boolean isItemDataNew(BankSave newSave)
	{
		return latestDisplayedData == null || !latestDisplayedData.getItemData().equals(newSave.getItemData());
	}
}
