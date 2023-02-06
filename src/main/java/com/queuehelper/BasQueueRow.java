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
import java.awt.Dimension;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JTextArea;
import javax.swing.border.EmptyBorder;
import net.runelite.client.ui.FontManager;
import net.runelite.client.util.ImageUtil;
import org.graalvm.compiler.core.common.type.ArithmeticOpTable;

	class BasQueueRow extends JPanel
{
	private final JMenuItem addMenuOption = new JMenuItem();

	private boolean otherimg;

	private JLabel nameField;
	private JLabel idField;
	private JLabel itemField;
	private JTextArea notesField;

	public Customer customer;

	private Color lastBackground;

	private BASPlugin plugin;

	private JLabel item;


	BasQueueRow(Customer Customer, BASPlugin Plugin)
	{
		this.otherimg = false;
		this.customer = Customer;
		this.plugin = Plugin;

		setLayout(new BorderLayout());
		setBorder(new EmptyBorder(2, 0, 2, 0));

		addMouseListener(new MouseAdapter()
		{
			@Override
			public void mouseClicked(MouseEvent mouseEvent)
			{
				if (mouseEvent.getClickCount() == 2)
				{

				}
			}

			@Override
			public void mousePressed(MouseEvent mouseEvent)
			{
				if (mouseEvent.getClickCount() == 2)
				{
					setBackground(getBackground().brighter());
				}
			}

			@Override
			public void mouseReleased(MouseEvent mouseEvent)
			{
				if (mouseEvent.getClickCount() == 2)
				{
					setBackground(getBackground().darker());
				}
			}

			@Override
			public void mouseEntered(MouseEvent mouseEvent)
			{
				BasQueueRow.this.lastBackground = getBackground();
				setBackground(getBackground().brighter());
			}

			@Override
			public void mouseExited(MouseEvent mouseEvent)
			{
				setBackground(lastBackground);
			}
		});


		final JPopupMenu popupMenu = new JPopupMenu();
		popupMenu.setBorder(new EmptyBorder(5, 5, 5, 5));
		String menuText;
		int option;
		String tooltipHover = "";

		if(customer.getStatus().equals("")){
			menuText = "Mark " + customer.getName()+ " online(doesn't work on names with a space)";
			option = 3;
			tooltipHover = "Offline";
		}
		else if(customer.getNotes().toLowerCase().contains("cooldown")){
			menuText = "End Cooldown for: " + customer.getName()+ "(Currently unavailable)";
			option = 0;
			tooltipHover = "Cooldown";
		}
		else if(customer.getStatus().equals("In Progress")){
			if(customer.getItem().equals("Level 5 Roles") && (customer.getNotes().contains("d started") || customer.getNotes().contains("2/3"))){
				menuText = "Mark " + customer.getName()+ " done(doesn't work on names with a space)";
				option = 2;
				tooltipHover = "In Progress last session lvl5s";
			}
			else if(customer.getItem().equals("Level 5 Roles")){
				menuText = "Start Cooldown for: " + customer.getName();
				option = 4;
				tooltipHover = "In Progress lvl5s";
			}
			else{
				menuText = "Mark " + customer.getName()+ " done(doesn't work on names with a space)";
				option = 2;
				tooltipHover = "In Progress";
			}
		}
		else if(customer.getStatus().equals("Done"))
		{
			menuText = "Mark " + customer.getName()+ " in progress(doesn't work on names with a space)";
			option = 1;
			tooltipHover = "Done";
		}
		else{
			menuText = "Mark " + customer.getName()+ " in progress(doesn't work on names with a space)";
			option = 1;
			tooltipHover = "Online";
		}

		addMenuOption.setText(menuText);

		for (ActionListener listener : addMenuOption.getActionListeners())
		{
			addMenuOption.removeActionListener(listener);
		}

		addMenuOption.addActionListener(e ->
		{
			this.plugin.markCustomer(option,customer);
		});
		popupMenu.add(addMenuOption);

		setComponentPopupMenu(popupMenu);

		JPanel leftSide = new JPanel(new BorderLayout());
		JPanel rightSide = new JPanel(new BorderLayout());
		leftSide.setOpaque(false);
		rightSide.setOpaque(false);

		JPanel nameField = buildNameField(Customer);
		nameField.setPreferredSize(new Dimension(70, 10));
		nameField.setOpaque(false);

		JPanel idField = buildidField(Customer);
		idField.setPreferredSize(new Dimension(30, 10));
		idField.setOpaque(false);

		JPanel itemField = builditemField(Customer);
		itemField.setPreferredSize(new Dimension(30, 30));
		itemField.setOpaque(false);

		JPanel notesField = buildNotesField(Customer);
		notesField.setPreferredSize(new Dimension(5, 34));
		notesField.setOpaque(false);

		recolour(Customer);

		leftSide.add(idField, BorderLayout.WEST);
		leftSide.add(nameField, BorderLayout.CENTER);
		rightSide.add(itemField, BorderLayout.WEST);
		rightSide.add(notesField, BorderLayout.CENTER);
		add(leftSide, BorderLayout.WEST);
		add(rightSide, BorderLayout.CENTER);
		this.setToolTipText(tooltipHover);

	}


	public void recolour(Customer customer)
	{
		String status = customer.getStatus();
		Color curColor = Color.black;
		if(this.item == null)
		{
			itemField.setForeground(curColor);
		}
		notesField.setForeground(curColor);
		nameField.setForeground(curColor);
		idField.setForeground(curColor);
		switch(status)
		{
			case "Online":
				if(!customer.getNotes().toLowerCase().contains("cd")&&!customer.getNotes().toLowerCase().contains("cooldown"))
				{
					curColor = Color.green;
					if(this.item == null)
					{
						itemField.setForeground(curColor);
					}
					notesField.setForeground(curColor);
					nameField.setForeground(curColor);
					idField.setForeground(curColor);

					break;
				}
				curColor = new Color(99,151,255);
				this.setBackground(curColor);
				notesField.setBackground(curColor);
				break;

			case "In Progress":
				curColor = new Color(241,235,118);;
				this.setBackground(curColor);
				notesField.setBackground(curColor);
				break;

			case "Done":
				curColor = new Color(129,129,129);;
				this.setBackground(curColor);
				notesField.setBackground(curColor);
				break;

			case "":
				if(!customer.getNotes().toLowerCase().contains("cd")&&!customer.getNotes().toLowerCase().contains("cooldown"))
				{
					curColor = Color.red;
					if(this.item == null)
					{
						itemField.setForeground(curColor);
					}
					notesField.setForeground(curColor);
					nameField.setForeground(curColor);
					idField.setForeground(curColor);
					break;
				}
				curColor = new Color(99,151,255);
				this.setBackground(curColor);
				break;

			default:
				curColor = Color.gray;
				this.setBackground(curColor);

				break;
		}
	}

	/**
	 * Builds the players list field (containing the amount of players logged in that world).
	 */
	private JPanel builditemField(Customer cust)
	{
		String item = cust.getItem();
		JPanel column = new JPanel(new BorderLayout());
		switch(item){

			case "Torso":
				column = new JPanel(new BorderLayout());
				itemField = new JLabel(new ImageIcon(ImageUtil.loadImageResource(getClass(), "/torso.png")));
				column.add(itemField);
				return column;

			case "Queen Kill - Diary":
				column = new JPanel(new BorderLayout());
				itemField = new JLabel(new ImageIcon(ImageUtil.loadImageResource(getClass(), "/queen_kill.png")));
				column.add(itemField);
				return column;

			case "Level 5 Roles":
				column = new JPanel(new BorderLayout());
				itemField = new JLabel(new ImageIcon(ImageUtil.loadImageResource(getClass(), "/Level5s.png")));
				column.add(itemField);
				return column;

			case "Hat":
				column = new JPanel(new BorderLayout());
				itemField = new JLabel(new ImageIcon(ImageUtil.loadImageResource(getClass(), "/hat4.png")));
				column.add(itemField);
				return column;


			default:
				if(item.toLowerCase().contains("gamble")){
					column = new JPanel(new BorderLayout());
					itemField = new JLabel(new ImageIcon(ImageUtil.loadImageResource(getClass(), "/other.png")));
					column.add(itemField);
					return column;

				}
				if(item.toLowerCase().contains("points") || item.toLowerCase().contains("pts")){
					column = new JPanel(new BorderLayout());
					itemField = new JLabel(new ImageIcon(ImageUtil.loadImageResource(getClass(), "/points.png")));
					otherimg = true;
					column.add(itemField);
					return column;

				}
				else
				{
					column = new JPanel(new BorderLayout());
					itemField = new JLabel(new ImageIcon(ImageUtil.loadImageResource(getClass(), "/Other.png")));
					column.add(itemField);
					otherimg = true;
					return column;
				}

		}
	}



	private JPanel buildidField(Customer cust)
	{
		JPanel column = new JPanel(new BorderLayout());
		column.setBorder(new EmptyBorder(0, 0, 0, 5));

		idField = new JLabel(cust.getID());
		idField.setFont(FontManager.getRunescapeSmallFont().deriveFont(8));

		idField.setToolTipText(cust.getID());
		// Pass up events - https://stackoverflow.com/a/14932443
		idField.addMouseListener(new MouseAdapter()
		{
			@Override
			public void mouseClicked(MouseEvent e)
			{
				dispatchEvent(e);
			}

			@Override
			public void mousePressed(MouseEvent e)
			{
				dispatchEvent(e);
			}

			@Override
			public void mouseReleased(MouseEvent e)
			{
				dispatchEvent(e);
			}

			@Override
			public void mouseEntered(MouseEvent e)
			{
				dispatchEvent(e);
			}

			@Override
			public void mouseExited(MouseEvent e)
			{
				dispatchEvent(e);
			}
		});

		column.add(idField, BorderLayout.EAST);


		return column;
	}

	/**
	 * Builds the activity list field (containing that world's activity/theme).
	 */
	private JPanel buildNotesField(Customer cust)
	{
		JPanel column = new JPanel(new BorderLayout());
		column.setBorder(new EmptyBorder(0, 5, 0, 5));


		String activity = cust.getNotes();
		if(activity.equals("") && this.otherimg){
			activity = cust.getItem();
		}

		notesField = new JTextArea(2, 10);
		notesField.setText(activity);


		notesField.setLineWrap(true);
		notesField.setEditable(true);
		notesField.setOpaque(false);
		notesField.setFont(FontManager.getRunescapeSmallFont().deriveFont(this.plugin.fontSize));


		column.add(notesField, BorderLayout.WEST);

		return column;
	}


	private JPanel buildNameField(Customer cust)
	{
		JPanel column = new JPanel(new BorderLayout());
		column.setBorder(new EmptyBorder(0, 5, 0, 5));

		nameField = new JLabel(cust.getName());
		nameField.setFont(FontManager.getRunescapeSmallFont());

		column.add(nameField, BorderLayout.CENTER);

		return column;
	}






}
