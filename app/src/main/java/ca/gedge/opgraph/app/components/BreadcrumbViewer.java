/*
 * Copyright (C) 2012 Jason Gedge <http://www.gedge.ca>
 *
 * This file is part of the OpGraph project.
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package ca.gedge.opgraph.app.components;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

import ca.gedge.opgraph.app.util.GUIHelper;
import ca.gedge.opgraph.util.Breadcrumb;
import ca.gedge.opgraph.util.BreadcrumbListener;
import ca.gedge.opgraph.util.Pair;

/**
 * A breadcrumb component. A breadcrumb shows a linear navigation history.
 * 
 * @param <S>  the type of state in the breadcrumb
 * @param <V>  the type of value in the breadcrumb
 */
public class BreadcrumbViewer<S, V> extends JPanel {
	/** The width of the right-hand side of a breadcrumb */ 
	private static final int RIGHT_WIDTH = 10;

	private static class StateComponent<S, V> extends JLabel {
		/** The state for this component */
		public final S state;

		public StateComponent(S state, V value) {
			super(value == null ? "" : value.toString());

			this.state = state;

			setOpaque(false);
			setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
			if(state != null)
				setBorder(new EmptyBorder(5, 0, 5, 2*RIGHT_WIDTH));
		}

		@Override
		protected void paintBorder(Graphics gfx) {
			if(state == null) {
				super.paintBorder(gfx);
			} else {
				Graphics2D g = (Graphics2D)gfx;
				g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
				//g.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_ON);

				final int x = (getWidth() - 1) - RIGHT_WIDTH;
				final int y = getHeight() / 2;
				g.setColor(Color.BLACK);
				g.drawLine(x, getHeight() - 1, getWidth() - 1, y);
				//g.drawLine(x, 0, getWidth() - 1, y);
				g.setColor(Color.WHITE);
				g.drawLine(x - 1, getHeight() - 1, getWidth() - 2, y);
				g.drawLine(x - 1, 0, getWidth() - 2, y);
			}
		}

		@Override
		protected void paintComponent(Graphics g) {
			final Rectangle rect = GUIHelper.getInterior(this);
			final Point p = GUIHelper.centerTextInRectangle(g, getText(), rect);

			g.setColor(Color.BLACK);
			g.drawString(getText(), p.x + 1, p.y + 1);
			g.setColor(Color.WHITE);
			g.drawString(getText(), p.x, p.y);
		}
	}

	/** The breadcrumb this component is viewing */
	private Breadcrumb<S, V> breadcrumb;

	/** The breadcrumb listener for this component */
	private BreadcrumbListener<S, V> listener = new BreadcrumbListener<S, V>() {
		@Override
		public void stateChanged(S oldState, S newState) {
			// Find the component associated with the new state and remove
			// all state components after it
			synchronized(getTreeLock()) {
				removeAll();

				for(Pair<S, V> statePair : breadcrumb) {
					final S state = statePair.getFirst();
					final StateComponent<S, V> comp = new StateComponent<S, V>(state, statePair.getSecond());
					comp.addMouseListener(new MouseAdapter() {
						@Override
						public void mouseClicked(MouseEvent e) {
							breadcrumb.gotoState(state);
						}
					});

					add(comp, 0);
				}
			}

			revalidate();
			repaint();
		}

		@Override
		public void stateAdded(final S state, V value) {
//			synchronized(getTreeLock()) {
//				final StateComponent<S, V> comp = new StateComponent<S, V>(state, value);
//				comp.addMouseListener(new MouseAdapter() {
//					@Override
//					public void mouseClicked(MouseEvent e) {
//						breadcrumb.gotoState(state);
//					}
//				});
//				
//				add(comp);
//            }
//			
//			revalidate();
//			repaint();
		}
	};

	/**
	 * Default constructor
	 * 
	 * @param breadcrumb  the breadcrumb to display
	 */
	public BreadcrumbViewer(Breadcrumb<S, V> breadcrumb) {
		super(new FlowLayout(FlowLayout.LEFT, RIGHT_WIDTH, 0));

		add(new StateComponent<S, V>(null, null));

		setBreadcrumb(breadcrumb);
		setOpaque(true);
		setBackground(Color.DARK_GRAY);
		setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, Color.BLACK));
	}

	/**
	 * Gets the breadcrumb this component is viewing.
	 * 
	 * @return  the breadcrumb, or <code>null</code> if no breadcrumb being viewed
	 */
	public Breadcrumb<S, V> getBreadcrumb() {
		return breadcrumb;
	}

	/**
	 * Sets the breadcrumb this component is viewing.
	 * 
	 * @param breadcrumb  the breadcrumb
	 */
	public void setBreadcrumb(Breadcrumb<S, V> breadcrumb) {
		if(breadcrumb != this.breadcrumb) {
			// Remove old information
			if(this.breadcrumb != null) {
				this.breadcrumb.removeBreadcrumbListener(listener);
				while(getComponentCount() > 1)
					remove(1);
			}

			this.breadcrumb = breadcrumb;

			// Add new information
			if(this.breadcrumb != null) {
				this.breadcrumb.addBreadcrumbListener(listener);
				for(Pair<S, V> state : this.breadcrumb)
					listener.stateAdded(state.getFirst(), state.getSecond());
			}
		}
	}
}
