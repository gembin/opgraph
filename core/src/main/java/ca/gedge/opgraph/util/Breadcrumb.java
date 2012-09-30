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
package ca.gedge.opgraph.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import ca.gedge.opgraph.util.Pair;

/**
 * A breadcrumb maintains a linear navigation history.
 * 
 * @param <S>  the type of state
 * @param <V>  the type of value associated with a state 
 */
public class Breadcrumb<S, V> implements Iterable<Pair<S, V>> { 
	/** The state stack */
	private LinkedList<S> states = new LinkedList<S>();
	
	/** The values stack */
	private LinkedList<V> values = new LinkedList<V>();
	
	/**
	 * Gets whether or not the breadcrumb contains a state.
	 * 
	 * @param state  the state to look for
	 * 
	 * @return <code>true</code> if the breadcrumb contains the given state,
	 *         <code>false</code> otherwise
	 */
	public boolean containsState(S state) {
		return states.contains(state);
	}
	
	/**
	 * Gets whether or not this breadcrumb is empty.
	 * 
	 * @return <code>true</code> if this breadcrumb is empty,
	 *         <code>false</code> otherwise 
	 */
	public boolean isEmpty() {
		return states.isEmpty();
	}
	
	/**
	 * Gets the current state of this breadcrumb.
	 * 
	 * @return  the current state, or <code>null</code> if no states available
	 */
	public S getCurrentState() {
		return (isEmpty() ? null : states.getFirst());
	}
	
	/**
	 * Gets the value associated with the current state of this breadcrumb.
	 * 
	 * @return  the current state's value, or <code>null</code> if no states available
	 */
	public V getCurrentValue() {
		return (isEmpty() ? null : values.getFirst());
	}
	
	/**
	 * Gets the number of states in this breadcrumb.
	 * 
	 * @return  the number of states
	 */
	public int size() {
		return states.size();
	}

	/**
	 * Empties this breadcrumb.
	 */
	public void clear() {
		if(!isEmpty()) {
			final S oldState = getCurrentState();
			states.clear();
			values.clear();
			fireStateChanged(oldState, null);
		}
	}
	
	/**
	 * Gets the state at the specified index from the current state.
	 * 
	 * @param index  the index of the state to peek at
	 * 
	 * @return the state at the given index (from the current state)
	 * 
	 * @throws IndexOutOfBoundsException  if the index is out of range (i.e.,
	 *                                    <code>index < 0 || index > size()</code>  
	 */
	public S peekState(int index) {
		return states.get(index);
	}
	
	/**
	 * Empties this breadcrumb.
	 */
	public void popState() {
		if(!isEmpty()) {
			final S oldState = getCurrentState();
			states.removeFirst();
			values.removeFirst();
			final S newState = getCurrentState();
			fireStateChanged(oldState, newState);
		}
		
	}
	
	/**
	 * Goes to the given state. If the state is part of this breadcrumb, all
	 * states following it will be removed.
	 * 
	 * @param state  the state to go to
	 */
	public void gotoState(S state) {
		int index = 0;
		for(; index < states.size(); ++index) {
			if(states.get(index) == state)
				break;
		}
		
		if(index > 0 && index < states.size()) {
			final S oldState = getCurrentState();
			
			while(states.getFirst() != state) {
				states.removeFirst();
				values.removeFirst();
			}
			
			fireStateChanged(oldState, state);
		}
	}
	
	/**
	 * Sets the complete state of the breadcrumb to a given list of
	 * state/value pairs.
	 * 
	 * @param states  the new set of states
	 */
	public void set(List<Pair<S, V>> states) {
		final S oldState = getCurrentState();
		
		// Clear and add in new
		this.states.clear();
		this.values.clear();
		
		for(Pair<S, V> state : states) {
			this.states.addFirst(state.getFirst());
			this.values.addFirst(state.getSecond());
			fireStateAdded(state.getFirst(), state.getSecond());
		}
		
		fireStateChanged(oldState, getCurrentState());
	}
	
	/**
	 * Append the given state to this breadcrumb with a <code>null</code> value.
	 * 
	 * @param state  the state
	 */
	public void addState(final S state) {
		addState(state, null);
	}
	
	/**
	 * Append the given state/value pair to this breadcrumb.
	 * 
	 * @param state  the state
	 * @param value  the value to associate with the given state
	 */
	public void addState(S state, V value) {
		final S oldState = getCurrentState();
		states.addFirst(state);
		values.addFirst(value);
		fireStateAdded(state, value);
		fireStateChanged(oldState, state);
	}
	
	/**
	 * Gets the set of states as an immutable list.
	 * 
	 * @return the list of states
	 */
	public List<S> getStates() {
		return Collections.unmodifiableList(states);
	}
	
	/**
	 * Gets the set of values as an immutable list.
	 * 
	 * @return the list of values
	 */
	public List<V> getValues() {
		return Collections.unmodifiableList(values);
	}
	
	//
	// Iterable
	//
	
	@Override
	public Iterator<Pair<S, V>> iterator() {
		final Iterator<S> iterS = states.iterator();
		final Iterator<V> iterV = values.iterator();
		return new Iterator<Pair<S, V>>() {
			@Override
			public boolean hasNext() {
				return iterS.hasNext();
			}

			@Override
			public Pair<S, V> next() {
				return new Pair<S, V>(iterS.next(), iterV.next());
			}

			@Override
			public void remove() {
				throw new UnsupportedOperationException("remove() not supported in Breadcrumb");
			}
		};
	}
	
	//
	// Listeners
	//

	private ArrayList<BreadcrumbListener<S, V>> listeners = new ArrayList<BreadcrumbListener<S, V>>();

	/**
	 * Adds a breadcrumb listener to this breadcrumb.
	 * 
	 * @param listener  the listener to add
	 */
	public void addBreadcrumbListener(BreadcrumbListener<S, V> listener) {
		synchronized(listeners) {
			if(listener != null && !listeners.contains(listener))
				listeners.add(listener);
		}
	}

	/**
	 * Removes a breadcrumb listener from this breadcrumb.
	 * 
	 * @param listener  the listener to remove
	 */
	public void removeBreadcrumbListener(BreadcrumbListener<S, V> listener) {
		synchronized(listeners) {
			listeners.remove(listener);
		}
	}

	protected void fireStateChanged(S oldState, S newState) {
		synchronized(listeners) {
			for(BreadcrumbListener<S, V> listener : listeners)
				listener.stateChanged(oldState, newState);
		}
	}
	
	protected void fireStateAdded(S state, V value) {
		synchronized(listeners) {
			for(BreadcrumbListener<S, V> listener : listeners)
				listener.stateAdded(state, value);
		}
	}
}
