	/*
	 * Copyright (c) 2019, SkylerPIlot <https://github.com/SkylerPIlot>
	 * All rights reserved.
	 *
	 * Redistribution and use in source and binary forms, with or without
	 * modification, are permitted provided that the following conditions are met:
	 *
	 * 1. Redistributions of source code must retain the above copyright notice, this
	 *    list of conditions and the following disclaimer.
	 * 2. Redistributions in binary form must reproduce the above copyright notice,
	 *    this list of conditions and the following disclaimer in the documentation
	 *    and/or other materials provided with the distribution.
	 *
	 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
	 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
	 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
	 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
	 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
	 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
	 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
	 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
	 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
	 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
	 */
package com.queuehelper;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.GridLayout;
import java.awt.event.ItemEvent;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.KeyStroke;
import javax.swing.border.EmptyBorder;
import net.runelite.client.ui.ColorScheme;
import net.runelite.client.ui.DynamicGridLayout;
import net.runelite.client.ui.PluginPanel;
import net.runelite.client.ui.components.materialtabs.MaterialTab;
import net.runelite.client.ui.components.materialtabs.MaterialTabGroup;
import net.runelite.client.util.ImageUtil;


public class BasQueuePanel extends PluginPanel
{
	private BASPlugin plugin;

	private final JPanel onlistContainer;

	private final JPanel offlistContainer;

	private ArrayList<BasQueueRow> rows;

	private BASConfig config;

	private JButton nextButton;

	private JButton custButton;

	private JTextArea namearea;

	private String currItem;

	private String currPriority;

	private JPanel nameAreaPanel;

	AutoComplete autoComplete;

	private final JPanel display = new JPanel();

	private final MaterialTabGroup tabGroup = new MaterialTabGroup(display);

	ArrayList<String> keywords = new ArrayList<String>();

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

	public void setAutoCompleteKeyWords(ArrayList<String> Keywords){
		autoComplete.setKeyWords(Keywords);
		this.keywords = Keywords;
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
		for (BasQueueRow row : rows)
		{
			if (row.customer.getStatus().equals("Online") || row.customer.getStatus().equals("In Progress"))
			{
				onlistContainer.add(row);
			}
			else
			{
				offlistContainer.add(row);
			}
		}
		this.nameAreaPanel = customerNamePrioPanel();
		onlistContainer.add(refreshNextButton());
		onlistContainer.add(nameAreaPanel);
		onlistContainer.add(customerAddPanel());
		//todo fix adding on offline side(idk why ppl would use this)
		//TODO this redraws + recreates a whole bunch of elements instead of creating/reusing works because java gc, but needs refactored to be cleaned
		offlistContainer.add(refreshNextButton());
		offlistContainer.add(customerNamePrioPanel());
		offlistContainer.add(customerAddPanel());
	}


	private JPanel addRefreshButton(String label)
	{
		final JPanel container = new JPanel();
		container.setLayout(new BorderLayout());

		JButton resetButton = new JButton(label);

		resetButton.setBackground(ColorScheme.DARK_GRAY_COLOR);
		resetButton.setBorder(new EmptyBorder(5, 0, 5, 5));
		resetButton.setToolTipText("refreshes the online Queue and redraws it");
		resetButton.setEnabled(true);

		resetButton.addActionListener(e -> {
			this.plugin.refreshQueue();
		});

		container.add(resetButton, BorderLayout.CENTER);

		return container;
	}

	private JPanel addNextButton(String label)
	{

		final JPanel container = new JPanel();
		container.setLayout(new BorderLayout());

		nextButton = new JButton(label);

		nextButton.setBackground(ColorScheme.DARKER_GRAY_COLOR);
		nextButton.setBorder(new EmptyBorder(5, 5, 5, 7));

		nextButton.addActionListener(e -> this.plugin.getNext());

		container.add(nextButton, BorderLayout.CENTER);

		//add(container, BorderLayout.SOUTH);
		return container;
	}

	private JComboBox<ComboBoxIconEntry> createCombobox()
	{
		final JComboBox<ComboBoxIconEntry> dropdown = new JComboBox<>();
		dropdown.setFocusable(false); // To prevent an annoying "focus paint" effect
		dropdown.setForeground(Color.WHITE);
		dropdown.setMaximumRowCount(5);
		final ComboBoxIconListRenderer renderer = new ComboBoxIconListRenderer();
		renderer.setDefaultText("Select an Item...");
		dropdown.setRenderer(renderer);
		final BufferedImage torsoimg = ImageUtil.loadImageResource(getClass(), "/torso.png");
		final ComboBoxIconEntry torsoentry = new ComboBoxIconEntry(new ImageIcon(torsoimg), "Torso");
		final BufferedImage lvl5img = ImageUtil.loadImageResource(getClass(), "/Level5s.png");
		final ComboBoxIconEntry lvl5entry = new ComboBoxIconEntry(new ImageIcon(lvl5img), "Level 5 Roles");
		final BufferedImage pointsimg = ImageUtil.loadImageResource(getClass(), "/points.png");
		final ComboBoxIconEntry pointsentry = new ComboBoxIconEntry(new ImageIcon(pointsimg), "One Round - Points");
		final BufferedImage qkimg = ImageUtil.loadImageResource(getClass(), "/queen_kill.png");
		final ComboBoxIconEntry qkentry = new ComboBoxIconEntry(new ImageIcon(qkimg), "Queen Kill - Diary");
		final BufferedImage hatimg = ImageUtil.loadImageResource(getClass(), "/hat4.png");
		final ComboBoxIconEntry hatentry = new ComboBoxIconEntry(new ImageIcon(hatimg), "Hat");


		dropdown.addItem(torsoentry);
		dropdown.addItem(lvl5entry);
		dropdown.addItem(pointsentry);
		dropdown.addItem(qkentry);
		dropdown.addItem(hatentry);
		dropdown.addItemListener(e ->
		{
			if (e.getStateChange() == ItemEvent.SELECTED)
			{
				final ComboBoxIconEntry source = (ComboBoxIconEntry) e.getItem();
				currItem = source.getText();
			}
		});
		dropdown.setSelectedIndex(-1);
		return dropdown;
	}
	private JTextArea customerNamePanel(){
		namearea = new JTextArea(1, 10);
		namearea.setFocusTraversalKeysEnabled(false);
		namearea.setText("Customer");
		namearea.setLineWrap(true);
		namearea.setEditable(true);
		namearea.setOpaque(true);
		namearea.setBorder(new EmptyBorder(20, 20, 2, 20));

		String COMMIT_ACTION = "commit";
		keywords.add("Skyler Miner");

		autoComplete = new AutoComplete(namearea, keywords);
		namearea.getDocument().addDocumentListener(autoComplete);
		namearea.getInputMap().put(KeyStroke.getKeyStroke("TAB"), COMMIT_ACTION);
		namearea.getActionMap().put(COMMIT_ACTION, autoComplete.new CommitAction());

		return namearea;

	}

	private JPanel addCustomerButoon(String label)
	{
		final JPanel container = new JPanel();
		container.setLayout(new BorderLayout());
		custButton = new JButton(label);
		custButton.setBackground(ColorScheme.DARKER_GRAY_COLOR);
		custButton.setBorder(new EmptyBorder(5, 7, 5, 7));
		custButton.addActionListener(e -> addCustomerAction());


		container.add(custButton, BorderLayout.CENTER);
		return container;
	}
	private void addCustomerAction(){
		namearea = (JTextArea) nameAreaPanel.getComponent(0);
		try
		{
			this.plugin.addToQueue(this.namearea.getText(), currItem, currPriority);
			this.namearea.setText("Customer");
		}
		catch (Exception e){
			e.printStackTrace();
		}

	}


 	private JPanel customerAddPanel(){
		JComboBox<ComboBoxIconEntry> dropdown = createCombobox();
		JPanel custButton = addCustomerButoon("Add Customer");
		JPanel addPanel = new JPanel();
		addPanel.setLayout(new BorderLayout());
		addPanel.setBorder(new EmptyBorder(3, 0, 2, 5));
		addPanel.add(dropdown, BorderLayout.CENTER);
		addPanel.add(custButton, BorderLayout.EAST);
		return addPanel;
	}

	private JPanel customerNamePrioPanel(){
		JComboBox<ComboBoxIconEntry> dropdown = createRegPremSelect();

		JPanel addPanel = new JPanel();
		addPanel.setLayout(new BorderLayout());
		customerNamePanel(); // instantiates this.namearea
		addPanel.setBorder(new EmptyBorder(3, 0, 2, 5));
		addPanel.add(namearea, BorderLayout.CENTER);
		addPanel.add(dropdown, BorderLayout.EAST);
		return addPanel;

	}

	private JPanel refreshNextButton(){
		JPanel combined = new JPanel();
		JPanel next = addNextButton("Next");
		JPanel refresh = addRefreshButton("Refresh");
		combined.setLayout(new BorderLayout());
		combined.setBorder(new EmptyBorder(3, 0, 2, 5));
		combined.add(next, BorderLayout.CENTER);
		combined.add(refresh, BorderLayout.EAST);
		return combined;
	}
	private JComboBox<ComboBoxIconEntry> createRegPremSelect()
	{
		final JComboBox<ComboBoxIconEntry> dropdown = new JComboBox<>();
		dropdown.setFocusable(false); // To prevent an annoying "focus paint" effect
		dropdown.setForeground(Color.WHITE);
		dropdown.setMaximumRowCount(5);
		final ComboBoxIconListRenderer renderer = new ComboBoxIconListRenderer();
		renderer.setDefaultText("Priority");
		dropdown.setRenderer(renderer);
		final BufferedImage regimg = ImageUtil.loadImageResource(getClass(), "/torso.png");
		final ComboBoxIconEntry regentry = new ComboBoxIconEntry(new ImageIcon(regimg), "Regular");
		final BufferedImage premimg = ImageUtil.loadImageResource(getClass(), "/torso.png");
		final ComboBoxIconEntry prementry = new ComboBoxIconEntry(new ImageIcon(premimg), "Premium");
		dropdown.addItem(regentry);
		dropdown.addItem(prementry);
		dropdown.addItemListener(e ->
		{
			if (e.getStateChange() == ItemEvent.SELECTED)
			{
				final ComboBoxIconEntry source = (ComboBoxIconEntry) e.getItem();
				currPriority = source.getText();
			}
		});
		dropdown.setSelectedIndex(-1);
		return dropdown;
	}

}
