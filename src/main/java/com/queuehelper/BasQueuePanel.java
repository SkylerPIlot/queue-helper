package com.queuehelper;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import javax.swing.GroupLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.event.DocumentListener;
import net.runelite.client.ui.ColorScheme;
import net.runelite.client.ui.DynamicGridLayout;
import net.runelite.client.ui.PluginPanel;
import net.runelite.client.ui.components.IconTextField;
import net.runelite.client.util.ImageUtil;


public class BasQueuePanel extends PluginPanel
{



	private JLabel torsoimg;


	final BufferedImage lvl5all = ImageUtil.loadImageResource(getClass(), "/panellogo.png");
	final BufferedImage lvl5att = ImageUtil.loadImageResource(getClass(), "/panellogo.png");
	final BufferedImage lvl5coll = ImageUtil.loadImageResource(getClass(), "/panellogo.png");
	final BufferedImage lvl5heal = ImageUtil.loadImageResource(getClass(), "/panellogo.png");
	final BufferedImage lvl5def = ImageUtil.loadImageResource(getClass(), "/panellogo.png");
	final BufferedImage gambles = ImageUtil.loadImageResource(getClass(), "/panellogo.png");

	private BASPlugin plugin;

	private final JPanel listContainer;

	private ArrayList<BasQueueRow> rows;

	BasQueuePanel(BASPlugin plugin)
	{
		this.plugin = plugin;//not sure why needed but fixes scrollbar

		this.rows = new ArrayList<>();
		this.listContainer = new JPanel();

		this.setBorder(null);
		this.setLayout(new DynamicGridLayout(0, 1));

		JPanel headerContainer = this.buildHeader();
		this.listContainer.setLayout(new GridLayout(0, 1));

		this.add(headerContainer);
		this.add(this.listContainer);


	}

	void updateList()
	{


		//TODO update the listContainter JPanel
	}

	void populate(LinkedHashMap<String, Customer> queue)
	{
		this.rows.clear();
		this.listContainer.removeAll();
		for (Customer cust : queue.values())
		{
				this.rows.add(new BasQueueRow(cust));//TODO add putting item image
		}
		for(BasQueueRow row : rows){
			listContainer.add(row);
		}
		this.updateList();
	}


	/**
	 * Builds the entire table header.
	 */
	private JPanel buildHeader()
	{
		Customer firstRow = new Customer("NAME", "ID", "Priority","CustomerStatus", "Notes", "Item");
		JPanel header = new BasQueueRow(firstRow);

		//header.add(header, BorderLayout.CENTER);


		return header;
	}


}
