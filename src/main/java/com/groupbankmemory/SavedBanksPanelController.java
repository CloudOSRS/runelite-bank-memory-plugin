package com.groupbankmemory;

import com.groupbankmemory.data.BankSave;
import com.groupbankmemory.data.DataStoreUpdateListener;
import com.groupbankmemory.data.DisplayNameMapper;
import com.groupbankmemory.data.PluginDataStore;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.swing.ImageIcon;
import javax.swing.SwingUtilities;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.game.ItemManager;

@Slf4j
public class SavedBanksPanelController {

    @Inject private Client client;
    @Inject private ClientThread clientThread;
    @Inject private ItemManager itemManager;
    @Inject private PluginDataStore dataStore;

    private BankSavesTopPanel topPanel;
    private ImageIcon casketIcon;
    private ImageIcon notedCasketIcon;
    private final AtomicBoolean workingToOpenBank = new AtomicBoolean();
    private DataStoreListener dataStoreListener;
    @Nullable BankSave bankForClipboardAction;

    public void startUp(BankSavesTopPanel topPanel) {
        assert SwingUtilities.isEventDispatchThread();

        this.topPanel = topPanel;
        topPanel.setBanksListInteractionListener(new BanksListInteractionListenerImpl());
        casketIcon = new ImageIcon(itemManager.getImage(405));
        notedCasketIcon = new ImageIcon(itemManager.getImage(406));

        topPanel.displayBanksListPanel();
        updateCurrentBanksList();

        dataStoreListener = new DataStoreListener();
        dataStore.addListener(dataStoreListener);
    }

    // Gets called on EDT and on game client thread
    private void updateCurrentBanksList() {
        List<BanksListEntry> saves = new ArrayList<>();
        DisplayNameMapper nameMapper = dataStore.getDisplayNameMapper();

//        for (BankSave save : dataStore.getCurrentBanksList()) {
//            String displayName = nameMapper.map(save.getUserName());
//            saves.add(new BanksListEntry(
//                    save.getId(), casketIcon, save.getWorldType(), "Current bank", displayName, save.getDateTimeString()));
//        }
//        for (BankSave save : dataStore.getSnapshotBanksList()) {
//            String displayName = nameMapper.map(save.getUserName());
//            saves.add(new BanksListEntry(
//                    save.getId(), notedCasketIcon, save.getWorldType(), save.getSaveName(), displayName, save.getDateTimeString()));
//        }

        Runnable updateList = () -> topPanel.updateBanksList(saves);
        if (SwingUtilities.isEventDispatchThread()) {
            updateList.run();
        } else {
            SwingUtilities.invokeLater(updateList);
        }
    }

    public void shutDown() {
        dataStore.removeListener(dataStoreListener);
    }

    private class BanksListInteractionListenerImpl implements BanksListInteractionListener {
        @Override
        public void selectedToOpen(BanksListEntry save) {
            if (workingToOpenBank.get()) {
                return;
            }
            workingToOpenBank.set(true);
        }

        @Override
        public void selectedToDelete(BanksListEntry save) {
            dataStore.deleteBankSaveWithId(save.getSaveId());
        }
    }

    private class DataStoreListener implements DataStoreUpdateListener {
        @Override
        public void currentBanksListChanged() {
            updateCurrentBanksList();
        }

        @Override
        public void snapshotBanksListChanged() {
            updateCurrentBanksList();
        }

        @Override
        public void displayNameMapUpdated() {
            updateCurrentBanksList();
        }
    }
}
