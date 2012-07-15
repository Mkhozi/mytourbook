/*******************************************************************************
 * Copyright (C) 2005, 2012  Wolfgang Schramm and Contributors
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
package net.tourbook.photo.internal;

import net.tourbook.photo.PicDirView;

import org.eclipse.jface.action.Action;

public class ActionShowPhotoName extends Action {

	private PicDirView	_picDirView;

	public ActionShowPhotoName(final PicDirView picDirView) {

		super(Messages.Pic_Dir_Action_ShowPhotoFileName, AS_CHECK_BOX);

		_picDirView = picDirView;

		setToolTipText(Messages.Pic_Dir_Action_ShowPhotoFileName_Tooltip);

		setImageDescriptor(Activator.getImageDescriptor(Messages.Image__PhotoInfoName));
	}

	@Override
	public void run() {
		_picDirView.actionShowPhotoInfo(this);
	}
}