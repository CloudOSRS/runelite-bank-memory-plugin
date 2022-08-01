package com.groupbankmemory;

import com.groupbankmemory.bankview.BankViewPanel;
import com.groupbankmemory.data.BankSave;
import com.groupbankmemory.data.PluginDataStore;
import com.groupbankmemory.util.Constants;
import com.google.common.collect.ImmutableList;
import java.awt.image.BufferedImage;
import java.util.Arrays;
import javax.inject.Inject;
import javax.swing.SwingUtilities;
import net.runelite.api.Client;
import net.runelite.api.GameState;
import net.runelite.api.InventoryID;
import net.runelite.api.ItemContainer;
import net.runelite.api.Player;
import net.runelite.api.events.GameStateChanged;
import net.runelite.api.events.GameTick;
import net.runelite.api.events.ItemContainerChanged;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.game.ItemManager;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.ClientToolbar;
import net.runelite.client.ui.NavigationButton;
import net.runelite.client.util.ImageUtil;

@PluginDescriptor(
        name = Constants.GROUP_BANK_MEMORY,
        description = "A searchable record of what's in your group's banks"
)
public class GroupBankMemoryPlugin extends Plugin {
    private static final String ICON = "bank_memory_icon.png";

    @Inject private ClientToolbar clientToolbar;
    @Inject private Client client;
    @Inject private ClientThread clientThread;
    @Inject private ItemManager itemManager;
    @Inject private PluginDataStore dataStore;

    private CurrentBankPanelController currentBankPanelController;
    private SavedBanksPanelController savedBanksPanelController;

    private NavigationButton navButton;
    private boolean displayNameRegistered = false;

    @Override
    protected void startUp() throws Exception {
        assert SwingUtilities.isEventDispatchThread();

        // Doing it here ensures it's created on the EDT + the instance is created after the client is all set up
        // (The latter is important because otherwise lots of L&F values won't be set right and it'll look weird)
        BankMemoryPluginPanel pluginPanel = injector.getInstance(BankMemoryPluginPanel.class);

        BufferedImage icon = ImageUtil.getResourceStreamFromClass(getClass(), ICON);
        navButton = NavigationButton.builder()
                .tooltip(Constants.GROUP_BANK_MEMORY)
                .icon(icon)
                .priority(7)
                .panel(pluginPanel)
                .build();

        clientToolbar.addNavigation(navButton);

        currentBankPanelController = injector.getInstance(CurrentBankPanelController.class);
        BankViewPanel currentBankView = pluginPanel.getCurrentBankViewPanel();
        clientThread.invokeLater(() -> currentBankPanelController.startUp(currentBankView));

        savedBanksPanelController = injector.getInstance(SavedBanksPanelController.class);
        savedBanksPanelController.startUp(pluginPanel.getSavedBanksTopPanel());
    }

    @Override
    protected void shutDown() {
        clientToolbar.removeNavigation(navButton);
        savedBanksPanelController.shutDown();
        currentBankPanelController = null;
        savedBanksPanelController = null;
    }

    @Subscribe
    public void onGameStateChanged(GameStateChanged gameStateChanged) {
		currentBankPanelController.onGameStateChanged(gameStateChanged);
        if (gameStateChanged.getGameState() != GameState.LOGGED_IN) {
            displayNameRegistered = false;
        }
    }

    @Subscribe
    public void onGameTick(GameTick event) {
        if (!displayNameRegistered) {
            Player player = client.getLocalPlayer();
            String charName = player == null ? null : player.getName();
            if (charName != null) {
                displayNameRegistered = true;
                dataStore.registerDisplayNameForLogin(client.getUsername(), charName);
            }
        }
    }

    @Subscribe
    public void onItemContainerChanged(ItemContainerChanged event) {
		if (client.getAccountType().isGroupIronman()) {
			ImmutableList<Integer> validStorage = ImmutableList.of(
				InventoryID.BANK.getId(), InventoryID.GROUP_STORAGE.getId(), InventoryID.SEED_VAULT.getId());
			if (!validStorage.contains(event.getContainerId())) {
				return;
			}
			ItemContainer bank = event.getItemContainer();

			currentBankPanelController.handleBankSave(
				BankSave.fromCurrentBank(client.getUsername(), bank,
					Arrays.stream(InventoryID.values()).filter(i -> i.getId() == bank.getId()).findFirst().orElse(InventoryID.BANK), itemManager));
			}
		}
}
