package com.groupbankmemory;

import com.groupbankmemory.util.Constants;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.MouseInfo;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;
import net.runelite.client.ui.ColorScheme;
import net.runelite.client.ui.FontManager;
import net.runelite.client.ui.components.PluginErrorPanel;


public class BanksListPanel extends JPanel
{
	private final PluginErrorPanel noDataMessage;
	private final JPanel listPanel;
	private final ListEntryMouseListener mouseListener;
	private BanksListInteractionListener interactionListener;

	public BanksListPanel()
	{
		super();
		mouseListener = new ListEntryMouseListener();

		setLayout(new BorderLayout());
		setBorder(BorderFactory.createEmptyBorder(Constants.PAD, 0, Constants.PAD, 0));

		noDataMessage = new PluginErrorPanel();
		noDataMessage.setContent("No bank saves", "You currently do not have any bank saves.");
		add(noDataMessage, BorderLayout.NORTH);

		listPanel = new JPanel(new GridBagLayout());
		JPanel listWrapper = new JPanel(new BorderLayout());
		listWrapper.add(listPanel, BorderLayout.NORTH);
		JScrollPane scrollPane = new JScrollPane(listWrapper);
		add(scrollPane, BorderLayout.CENTER);
	}

	public void setInteractionListener(BanksListInteractionListener listener)
	{
		interactionListener = listener;
	}

	public void updateBanksList(List<BanksListEntry> entries)
	{
		listPanel.removeAll();

		noDataMessage.setVisible(entries.isEmpty());
		if (!entries.isEmpty())
		{
			displayListOfBanks(entries);
		}

		revalidate();
		repaint();
	}

	private void displayListOfBanks(List<BanksListEntry> entries)
	{
		GridBagConstraints c = new GridBagConstraints();
		c.fill = GridBagConstraints.HORIZONTAL;
		c.weightx = 1;
		c.gridx = 0;
		c.gridy = 0;
		for (BanksListEntry entry : entries)
		{
			JPanel entriesGapPad = new JPanel(new BorderLayout());
			entriesGapPad.setBorder(BorderFactory.createEmptyBorder(Constants.PAD / 2, 0, Constants.PAD / 2, 0));
			entriesGapPad.add(new EntryPanel(entry), BorderLayout.NORTH);
			listPanel.add(entriesGapPad, c);
			c.gridy++;
		}
	}

	private class EntryPanel extends JPanel
	{
		final BanksListEntry entry;

		public EntryPanel(BanksListEntry entry)
		{
			this.entry = entry;

			setLayout(new GridBagLayout());
			setBorder(BorderFactory.createEmptyBorder(Constants.PAD, Constants.PAD, Constants.PAD, Constants.PAD));
			setBackground(ColorScheme.DARKER_GRAY_COLOR);
			setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
			setToolTipText(entry.getDateTime());

			GridBagConstraints c = new GridBagConstraints();
			c.gridx = 0;
			c.gridy = 0;
			c.weightx = 0;
			c.gridheight = 2;
			add(new JLabel(entry.getIcon()), c);

			c.fill = GridBagConstraints.HORIZONTAL;
			c.gridx = 1;
			c.weightx = 1;
			c.gridheight = 1;
			add(new JLabel(entry.getSaveName()), c);

			c.gridy = 1;
			String subText = entry.getAccountDisplayName();
			JLabel subLabel = new JLabel(subText);
			subLabel.setFont(FontManager.getRunescapeSmallFont());
			add(subLabel, c);

			addMouseListener(mouseListener);
		}
	}

	private class ListEntryMouseListener extends MouseAdapter
	{
		private final Color normalBgColour = ColorScheme.DARKER_GRAY_COLOR;
		private final Color hoverBgColour = ColorScheme.DARKER_GRAY_HOVER_COLOR;

		@Override
		public void mouseEntered(MouseEvent e)
		{
			e.getComponent().setBackground(hoverBgColour);
		}

		@Override
		public void mouseExited(MouseEvent e)
		{
			e.getComponent().setBackground(normalBgColour);
		}

		@Override
		public void mouseClicked(MouseEvent e)
		{
			if (SwingUtilities.isLeftMouseButton(e))
			{
				BanksListEntry entryClicked = ((EntryPanel) e.getComponent()).entry;
				interactionListener.selectedToOpen(entryClicked);

				// mouseExited won't trigger if the interaction listener changes the view entirely,
				// so trigger manually if needed
				if (!e.getComponent().contains(MouseInfo.getPointerInfo().getLocation()))
				{
					mouseExited(e);
				}
			}
		}
	}
}
