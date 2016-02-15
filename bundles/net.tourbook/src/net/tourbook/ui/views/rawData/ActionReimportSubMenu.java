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
package net.tourbook.ui.views.rawData;

import net.tourbook.Messages;
import net.tourbook.common.util.ITourViewer3;
import net.tourbook.importdata.RawDataManager;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.ActionContributionItem;
import org.eclipse.jface.action.IMenuCreator;
import org.eclipse.swt.events.MenuAdapter;
import org.eclipse.swt.events.MenuEvent;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;

/**
 */
public class ActionReimportSubMenu extends Action implements IMenuCreator {

	private Menu								_menu;

	private ActionReimportEntireTour			_actionReimportEntireTour;
	private ActionReimportOnlyAltitudeValues	_actionReimportOnlyAltitudeValues;
	private ActionReimportOnlyTimeSlices		_actionReimportOnlyTimeSlices;
	private ActionReimportOnlyGearValues		_actionReimportOnlyGearValues;
	private ActionReimportOnlyPowerValues		_actionReimportOnlyPowerValues;
	private ActionReimportOnlyTemperatureValues	_actionReimportOnlyTemperatureValues;
	private ActionReimportOnlyTourMarker		_actionReimportOnlyTourMarker;

	private ITourViewer3						_tourViewer;

	private class ActionReimportEntireTour extends Action {

		public ActionReimportEntireTour() {
			setText(Messages.Import_Data_Action_Reimport_EntireTour);
		}

		@Override
		public void run() {
			RawDataManager.getInstance().actionReimportTour(RawDataManager.ReImport.Tour, _tourViewer);
		}

	}

	private class ActionReimportOnlyAltitudeValues extends Action {

		public ActionReimportOnlyAltitudeValues() {
			setText(Messages.Import_Data_Action_Reimport_OnlyAltitudeValues);
		}

		@Override
		public void run() {
			RawDataManager.getInstance().actionReimportTour(RawDataManager.ReImport.OnlyAltitudeValues, _tourViewer);
		}
	}

	private class ActionReimportOnlyGearValues extends Action {

		public ActionReimportOnlyGearValues() {
			setText(Messages.Import_Data_Action_Reimport_OnlyGearValues);
		}

		@Override
		public void run() {
			RawDataManager.getInstance().actionReimportTour(RawDataManager.ReImport.OnlyGearValues, _tourViewer);
		}
	}

	private class ActionReimportOnlyPowerValues extends Action {

		public ActionReimportOnlyPowerValues() {
			setText(Messages.Import_Data_Action_Reimport_OnlyPowerAndSpeedValues);
		}

		@Override
		public void run() {
			RawDataManager.getInstance().actionReimportTour(RawDataManager.ReImport.OnlyPowerAndSpeedValues, _tourViewer);
		}
	}

	private class ActionReimportOnlyTemperatureValues extends Action {

		public ActionReimportOnlyTemperatureValues() {
			setText(Messages.Import_Data_Action_Reimport_OnlyTemperatureValues);
		}

		@Override
		public void run() {
			RawDataManager.getInstance().actionReimportTour(RawDataManager.ReImport.OnlyTemperatureValues, _tourViewer);
		}

	}

	private class ActionReimportOnlyTimeSlices extends Action {

		public ActionReimportOnlyTimeSlices() {
			setText(Messages.Import_Data_Action_Reimport_OnlyTimeSlices);
		}

		@Override
		public void run() {
			RawDataManager.getInstance().actionReimportTour(RawDataManager.ReImport.AllTimeSlices, _tourViewer);
		}

	}

	private class ActionReimportOnlyTourMarker extends Action {

		public ActionReimportOnlyTourMarker() {
			setText(Messages.Import_Data_Action_Reimport_OnlyTourMarker);
		}

		@Override
		public void run() {
			RawDataManager.getInstance().actionReimportTour(RawDataManager.ReImport.OnlyTourMarker, _tourViewer);
		}

	}

	public ActionReimportSubMenu(final ITourViewer3 tourViewer) {

		super(Messages.Import_Data_Action_Reimport_Tour, AS_DROP_DOWN_MENU);

		setMenuCreator(this);

		_tourViewer = tourViewer;

		_actionReimportEntireTour = new ActionReimportEntireTour();
		_actionReimportOnlyAltitudeValues = new ActionReimportOnlyAltitudeValues();
		_actionReimportOnlyGearValues = new ActionReimportOnlyGearValues();
		_actionReimportOnlyPowerValues = new ActionReimportOnlyPowerValues();
		_actionReimportOnlyTemperatureValues = new ActionReimportOnlyTemperatureValues();
		_actionReimportOnlyTimeSlices = new ActionReimportOnlyTimeSlices();
		_actionReimportOnlyTourMarker = new ActionReimportOnlyTourMarker();
	}

	@Override
	public void dispose() {

		if (_menu != null) {
			_menu.dispose();
			_menu = null;
		}
	}

	private void fillMenu(final Menu menu) {

		new ActionContributionItem(_actionReimportOnlyAltitudeValues).fill(menu, -1);
		new ActionContributionItem(_actionReimportOnlyTemperatureValues).fill(menu, -1);
		new ActionContributionItem(_actionReimportOnlyGearValues).fill(menu, -1);
		new ActionContributionItem(_actionReimportOnlyPowerValues).fill(menu, -1);
		new ActionContributionItem(_actionReimportOnlyTourMarker).fill(menu, -1);
		new ActionContributionItem(_actionReimportOnlyTimeSlices).fill(menu, -1);
		new ActionContributionItem(_actionReimportEntireTour).fill(menu, -1);
	}

	@Override
	public Menu getMenu(final Control parent) {
		return null;
	}

	@Override
	public Menu getMenu(final Menu parent) {

		dispose();

		_menu = new Menu(parent);

		// Add listener to repopulate the menu each time
		_menu.addMenuListener(new MenuAdapter() {
			@Override
			public void menuShown(final MenuEvent e) {

				// dispose old menu items
				for (final MenuItem menuItem : ((Menu) e.widget).getItems()) {
					menuItem.dispose();
				}

				fillMenu(_menu);
			}
		});

		return _menu;
	}

}