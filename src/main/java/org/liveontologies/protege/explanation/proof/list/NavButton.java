package org.liveontologies.protege.explanation.proof.list;

/*-
 * #%L
 * Protege Proof-Based Explanation
 * $Id:$
 * $HeadURL:$
 * %%
 * Copyright (C) 2014 - 2016 Live Ontologies Project
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

import java.awt.Component;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.event.ActionListener;

import javax.swing.Icon;

/**
 * The object used to display the tree navigation buttons in
 * {@link ProofFrameList}; warning: the fields of this objects are not final and
 * may change
 * 
 * @author Yevgeny Kazakov
 *
 */
class NavButton {

	/**
	 * the component in which this button should be painted
	 */
	private final Component component_;

	/**
	 * the rectangle containing the content of this button
	 */
	private final Rectangle bounds_;

	/**
	 * the listener for actions with this button
	 */
	private ActionListener actionListener_;

	/**
	 * the icon displayed when this button is painted
	 */
	private Icon icon_;

	public NavButton(Component component) {
		this.component_ = component;
		this.bounds_ = new Rectangle();
	}

	/**
	 * @return the rectangle containing the content of this button
	 */
	public Rectangle getBounds() {
		return this.bounds_;
	}

	/**
	 * @return the listener for actions with this button
	 */
	public ActionListener getActionListener() {
		return actionListener_;
	}

	/**
	 * Sets the icon and the position of this button
	 * 
	 * @param icon
	 * @param x
	 * @param y
	 */
	public void setIcon(Icon icon, int x, int y) {
		this.icon_ = icon;
		this.bounds_.width = icon.getIconWidth();
		this.bounds_.height = icon.getIconHeight();
		this.bounds_.x = x - (bounds_.width / 2);
		this.bounds_.y = y - (bounds_.width / 2);
	}

	public void setActionListener(ActionListener listener) {
		this.actionListener_ = listener;
	}

	public void paintButtonContent(Graphics2D gIn) {
		Graphics2D g = (Graphics2D) gIn.create();
		icon_.paintIcon(component_, g, bounds_.x, bounds_.y);
	}

}
