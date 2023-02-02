package com.queuehelper;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import net.runelite.client.ui.ColorScheme;
import net.runelite.client.ui.DynamicGridLayout;
import net.runelite.client.ui.PluginPanel;
import net.runelite.client.ui.components.materialtabs.MaterialTab;
import net.runelite.client.ui.components.materialtabs.MaterialTabGroup;


public class BasQueuePanel extends PluginPanel
{


	private BASPlugin plugin;

	private final JPanel onlistContainer;

	private final JPanel offlistContainer;

	private ArrayList<BasQueueRow> rows;

	private BASConfig config;

	private JButton nextButton;

	private final JPanel display = new JPanel();

	private final MaterialTabGroup tabGroup = new MaterialTabGroup(display);




	BasQueuePanel(BASPlugin plugin, BASConfig Config)
	{
		this.plugin = plugin;//not sure why needed but fixes scrollbar
		this.config = Config;


		this.rows = new ArrayList<>();
		this.onlistContainer = new JPanel();
		this.offlistContainer = new JPanel();

		this.setBorder(null);
		this.setLayout(new DynamicGridLayout(0, 1));


		this.onlistContainer.setLayout(new GridLayout(0, 1));
		this.offlistContainer.setLayout(new GridLayout(0, 1));





		setLayout(new BorderLayout());
		setBackground(ColorScheme.DARK_GRAY_COLOR);

		MaterialTab offersTab = new MaterialTab("Online", tabGroup, onlistContainer);
		MaterialTab searchTab = new MaterialTab("Offline", tabGroup, offlistContainer);

		tabGroup.setBorder(new EmptyBorder(5, 0, 0, 0));
		tabGroup.addTab(offersTab);
		tabGroup.addTab(searchTab);
		tabGroup.select(offersTab); // selects the default selected tab

		add(tabGroup, BorderLayout.NORTH);
		add(display, BorderLayout.CENTER);






	}

	void updateList()
	{


		//TODO update the listContainter JPanel
	}

	void populate(LinkedHashMap<String, Customer> queue)
	{
		this.rows.clear();
		this.offlistContainer.removeAll();
		this.onlistContainer.removeAll();
		for (Customer cust : queue.values())
		{
			this.rows.add(new BasQueueRow(cust, plugin));

		}
		for(BasQueueRow row : rows){
			if(!this.config.showOnlineOnly() || row.customer.getStatus().equals("Online") || row.customer.getStatus().equals("In Progress"))
			{
				onlistContainer.add(row);
			}
			else
			{
				offlistContainer.add(row);
			}
		}
		onlistContainer.add(addNextButton("Next"));
		onlistContainer.add(addRefreshButton("Refresh"));
		offlistContainer.add(addNextButton("Next"));
		offlistContainer.add(addRefreshButton("Refresh"));
		this.updateList();
	}


	private JButton addRefreshButton(String label) {
		final JPanel container = new JPanel();
		container.setLayout(new BorderLayout());

		JButton resetButton = new JButton(label);

		resetButton.setBackground(ColorScheme.DARK_GRAY_COLOR);
		resetButton.setBorder(new EmptyBorder(5, 7, 5, 7));
		resetButton.setToolTipText("refreshes the online Queue and redraws it");
		resetButton.setEnabled(true);

		resetButton.addActionListener(e -> {
			this.plugin.refreshQueue();
		});

		container.add(resetButton, BorderLayout.CENTER);
		//add(container, BorderLayout.NORTH);

		return resetButton;
	}

	private JPanel addNextButton(String label) {
		final JPanel container = new JPanel();
		container.setLayout(new BorderLayout());

		nextButton = new JButton(label);

		nextButton.setBackground(ColorScheme.DARKER_GRAY_COLOR);
		nextButton.setBorder(new EmptyBorder(5, 7, 5, 7));

		nextButton.addActionListener(e->this.plugin.getNext());


		container.add(nextButton, BorderLayout.CENTER);

		//add(container, BorderLayout.SOUTH);
		return container;
	}




}
