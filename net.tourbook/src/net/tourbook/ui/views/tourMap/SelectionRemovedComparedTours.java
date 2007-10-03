/*******************************************************************************
 * Copyright (C) 2005, 2007  Wolfgang Schramm and Contributors
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
package net.tourbook.ui.views.tourMap;

import java.util.ArrayList;

import org.eclipse.jface.viewers.ISelection;

public class SelectionRemovedComparedTours implements ISelection {

	/**
	 * contains compared tours which have been deleted in the tour map view
	 */
	public ArrayList<Long>	removedComparedTours	= new ArrayList<Long>();

	public boolean isEmpty() {
		return false;
	}

}
