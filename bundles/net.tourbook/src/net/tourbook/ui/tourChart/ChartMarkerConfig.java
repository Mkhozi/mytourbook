/*******************************************************************************
 * Copyright (C) 2005, 2015 Wolfgang Schramm and Contributors
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
package net.tourbook.ui.tourChart;

import java.util.ArrayList;

import org.eclipse.swt.graphics.RGB;

public class ChartMarkerConfig {

	public boolean						isDrawMarkerWithDefaultColor;
	public boolean						isShowAbsoluteValues;
	public boolean						isShowHiddenMarker;
	public boolean						isShowLabelTempPos;
	public boolean						isShowMarkerLabel;
	public boolean						isShowMarkerPoint;
	public boolean						isShowMarkerTooltip;
	public boolean						isShowOnlyWithDescription;
	public boolean						isShowSignImage;

	public int							markerHoverSize;
	public int							markerLabelOffset;
	public int							markerLabelTempPos;
	public int							markerPointSize;
	public int							markerSignImageSize;
	public int							markerTooltipPosition;

	public RGB							markerColorDefault;
	public RGB							markerColorDevice;
	public RGB							markerColorHidden;

	public final ArrayList<ChartLabel>	chartLabels	= new ArrayList<ChartLabel>();

}
