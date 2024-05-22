/*
 * Copyright (c) 2008-2019 Emmanuel Dupuy.
 * This project is distributed under the GPLv3 license.
 * This is a Copyleft license that gives the user the right to use,
 * copy and modify the code freely for non-commercial purposes.
 */

package org.jd.gui.view.component;

import javax.swing.*;
import java.awt.*;
import java.math.BigDecimal;

public class IconButton extends JButton {
	protected static final Insets INSETS0 = new Insets(0, 0, 0, 0);

	public IconButton(String text, Action action) {
		setFocusPainted(false);
		setBorderPainted(false);
		setMargin(INSETS0);
		setAction(action);
		setText(text);
		ImageIcon imageIcon = (ImageIcon) action.getValue(Action.SMALL_ICON);
		if (imageIcon != null) {
//		int iconSize = new BigDecimal(16).multiply(new BigDecimal(16)).divide(new BigDecimal(12)).intValue();
			int iconSize = 21;
			imageIcon.setImage(imageIcon.getImage().getScaledInstance(iconSize, iconSize, Image.SCALE_SMOOTH));
//		setFont(new Font(null, Font.PLAIN, 14));
		}
	}

	public IconButton(Action action) {
		this(null, action);
	}
}
