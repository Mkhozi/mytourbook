/*******************************************************************************
 * Copyright (C) 2006, 2007  Wolfgang Schramm
 *  
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software 
 * Foundation version 2 of the License.
 *  
 * This program is distributed in the hope that it will be useful, but WITHOUT 
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS 
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along with 
 * this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110, USA    
 *******************************************************************************/
package net.tourbook.tour;

import java.util.ArrayList;

public abstract class TreeViewerItem {

	private TreeViewerItem				fParentItem	= null;
	private ArrayList<TreeViewerItem>	fChildren	= null;

	public boolean hasChildren() {

		if (fChildren == null) {
			/*
			 * if fChildren have not yet been retrieved we assume that
			 * fChildren can be available to make the tree node expandable
			 */
			return true;
		} else {
			return fChildren.size() > 0;
		}
	}

	public Object[] getFetchedChildren() {
		if (fChildren == null) {
			fetchChildren();
		}
		if (fChildren == null) {
			return new Object[0];
		}

		return fChildren.toArray();
	}

	public ArrayList<TreeViewerItem> getChildren() {
		if (fChildren == null) {
			return new ArrayList<TreeViewerItem>();
		}
		return fChildren;
	}

	protected abstract void fetchChildren();
	protected abstract void remove();

	public void setChildren(ArrayList<TreeViewerItem> children) {
		this.fChildren = children;
	}

	public ArrayList<TreeViewerItem> getUnfetchedChildren() {
		return fChildren;
	}

	public void setParentItem(TreeViewerItem parentItem) {
		this.fParentItem = parentItem;
	}

	public TreeViewerItem getParentItem() {
		return fParentItem;
	}

}
