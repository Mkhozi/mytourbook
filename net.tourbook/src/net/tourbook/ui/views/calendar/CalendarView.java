package net.tourbook.ui.views.calendar;

import java.util.ArrayList;
import java.util.Formatter;

import net.tourbook.Messages;
import net.tourbook.application.TourbookPlugin;
import net.tourbook.data.TourData;
import net.tourbook.preferences.ITourbookPreferences;
import net.tourbook.tour.ITourEventListener;
import net.tourbook.tour.SelectionDeletedTours;
import net.tourbook.tour.SelectionTourId;
import net.tourbook.tour.TourEventId;
import net.tourbook.tour.TourManager;
import net.tourbook.ui.ITourProvider;
import net.tourbook.ui.UI;
import net.tourbook.ui.views.calendar.CalendarGraph.NavigationStyle;
import net.tourbook.util.SelectionProvider;
import net.tourbook.util.Util;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IPartListener2;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchPartReference;
import org.eclipse.ui.part.PageBook;
import org.eclipse.ui.part.ViewPart;
import org.joda.time.DateTime;

public class CalendarView extends ViewPart implements ITourProvider {

	/**
	 * The ID of the view as specified by the extension.
	 */
	public static final String					ID								= "net.tourbook.views.calendar.CalendarView"; //$NON-NLS-1$

	private final IPreferenceStore				_prefStore						= TourbookPlugin.getDefault() //
																						.getPreferenceStore();

	private final IDialogSettings				_state							= TourbookPlugin.getDefault() //
																						.getDialogSettingsSection(
																								"TourCalendarView");			//$NON-NLS-1$

	private PageBook							_pageBook;

	private CalendarComponents					_calendarComponents;
	private CalendarGraph						_calendarGraph;
	private ISelectionProvider					_selectionProvider;

	private ISelectionListener					_selectionListener;
	private IPartListener2						_partListener;
	private IPropertyChangeListener				_prefChangeListener;
	private ITourEventListener					_tourPropertyListener;
	private CalendarYearMonthContributionItem	_cymci;

	private String								STATE_SELECTED_TOURS			= "SelectedTours";								// $NON-NLS-1$ //$NON-NLS-1$

	private String								STATE_FIRST_DAY					= "FirstDayDisplayed";							// $NON-NLS-1$ //$NON-NLS-1$
	private String								STATE_NUM_OF_WEEKS				= "NumberOfWeeksDisplayed";					// $NON-NLS-1$ //$NON-NLS-1$
	private String								STATE_IS_LINKED					= "Linked";									// $NON-NLS-1$ //$NON-NLS-1$
	private String								STATE_TOUR_SIZE_DYNAMIC			= "TourSizeDynamic";							// $NON-NLS-1$ //$NON-NLS-1$
	private String								STATE_NUMBER_OF_TOURS_PER_DAY	= "NumberOfToursPerDay";						// $NON-NLS-1$ //$NON-NLS-1$
	private String								STATE_TOUR_INFO_FORMATTER_INDEX	= "TourInfoFormatterIndex"; //$NON-NLS-1$

	private Action								_forward, _back;
	private Action								_zoomIn, _zoomOut;
	private Action								_setLinked;
	private Action								_gotoToday;
	private Action								_setNavigationStylePhysical, _setNavigationStyleLogical;
	private Action[]							_setNumberOfToursPerDay;
	private Action								_setTourSizeDynamic;
	private Action[]							_setTourInfoFormat;

	private TourInfoFormatter[]					_tourInfoFormatter				= {

																				// fool stupid autoformater

			/*
			 * title - description
			 */
			new TourInfoFormatter() {
				@Override
				public String format(final CalendarTourData data) {
					if (data.tourTitle != null && data.tourTitle.length() > 1) {
						return data.tourTitle;
					} else if (data.tourDescription != null && data.tourDescription.length() > 1) {
						return data.tourDescription;
					} else {
						return UI.EMPTY_STRING;
					}
				}

				@Override
				public String getText() {
					return Messages.Calendar_View_Action_ShowTitleDescription;
				}
			},

			/*
			 * description - title
			 */
			new TourInfoFormatter() {
				@Override
				public String format(final CalendarTourData data) {
					if (data.tourDescription != null && data.tourDescription.length() > 1) {
						return data.tourDescription;
					} else if (data.tourTitle != null && data.tourTitle.length() > 1) {
						return data.tourTitle;
					} else {
						return UI.EMPTY_STRING;
					}
				}

				@Override
				public String getText() {
					return Messages.Calendar_View_Action_ShowDescriptionTitle;
				}
			},

			/*
			 * distance - time
			 */
			new TourInfoFormatter() {
				@Override
				public String format(final CalendarTourData data) {
					final float distance = (float) (data.distance / 1000.0 / UI.UNIT_VALUE_DISTANCE);
					final int time = data.recordingTime;
					return new Formatter().format(
							NLS.bind(Messages.Calendar_View_Format_DistanceTime, UI.UNIT_LABEL_DISTANCE),
							distance,
							time / 3600,
							(time % 3600) / 60).toString();
				}

				@Override
				public String getText() {
					return Messages.Calendar_View_Action_ShowDistanceTime;
				}
			},

			/*
			 * distance - speed
			 */
			new TourInfoFormatter() {
				@Override
				public String format(final CalendarTourData data) {
					final float distance = data.distance;
					final int time = data.recordingTime;
					return new Formatter().format(
							NLS.bind(
									Messages.Calendar_View_Format_DistanceSpeed,
									UI.UNIT_LABEL_DISTANCE,
									UI.UNIT_LABEL_SPEED),
							distance / 1000.0f / UI.UNIT_VALUE_DISTANCE,
							distance == 0 ? 0 : distance / (time / 3.6f)).toString();
				}

				@Override
				public String getText() {
					return Messages.Calendar_View_Action_ShowDistanceSpeed;
				}
			},

			/*
			 * distance - pace
			 */
			new TourInfoFormatter() {
				@Override
				public String format(final CalendarTourData data) {
					final int pace = (int) (data.distance == 0
							? 0
							: (1000 * data.recordingTime / data.distance * UI.UNIT_VALUE_DISTANCE));
					final float distance = data.distance / 1000.0f / UI.UNIT_VALUE_DISTANCE;
					return new Formatter().format(
							NLS.bind(
									Messages.Calendar_View_Format_DistancePace,
									UI.UNIT_LABEL_DISTANCE,
									UI.UNIT_LABEL_PACE),
							distance,
							pace / 60,
							pace % 60).toString();
				}

				@Override
				public String getText() {
					return Messages.Calendar_View_Action_DistancePace;
				}
			},

			/*
			 * time - distance
			 */
			new TourInfoFormatter() {
				@Override
				public String format(final CalendarTourData data) {
					final int time = data.recordingTime;
					final float distance = data.distance / 1000.0f / UI.UNIT_VALUE_DISTANCE;
					return new Formatter().format(
							NLS.bind(Messages.Calendar_View_Format_TimeDistance, UI.UNIT_LABEL_DISTANCE),
							time / 3600,
							(time % 3600) / 60,
							distance).toString();
				}

				@Override
				public String getText() {
					return Messages.Calendar_View_Action_TimeDistance;
				}
			},

			/*
			 * time - speed
			 */
			new TourInfoFormatter() {
				@Override
				public String format(final CalendarTourData data) {
					final int time = data.recordingTime;
					return new Formatter().format(
							NLS.bind(Messages.Calendar_View_Format_TimeSpeed, UI.UNIT_LABEL_SPEED),
							time / 3600,
							(time % 3600) / 60,
							data.distance == 0 ? 0 : data.distance / time * 3.6f / UI.UNIT_VALUE_DISTANCE).toString();
				}

				@Override
				public String getText() {
					return Messages.Calendar_View_Action_TimeSpeed;
				}
			},

			/*
			 * time - pace
			 */
			new TourInfoFormatter() {
				@Override
				public String format(final CalendarTourData data) {
					final int pace = (int) (data.distance == 0
							? 0
							: (1000 * data.recordingTime / data.distance * UI.UNIT_VALUE_DISTANCE));
					return new Formatter().format(
							NLS.bind(Messages.Calendar_View_Format_TimePace, UI.UNIT_LABEL_PACE),
							data.recordingTime / 3600,
							(data.recordingTime % 3600) / 60,
							pace / 60,
							pace % 60).toString();
				}

				@Override
				public String getText() {
					return Messages.Calendar_View_Action_TimePace;
				}
			}

																				};

	class NumberOfToursPerDayAction extends Action {

		private int	numberOfTours;

		NumberOfToursPerDayAction(final String text, final int style, final int numberOfTours) {

			super(text, style);

			this.numberOfTours = numberOfTours;
			if (0 == numberOfTours) {
				setText(Messages.Calendar_View_Action_DisplayTours_All);
			} else if (1 == numberOfTours) {
				setText(Messages.Calendar_View_Action_DisplayTours_1ByDay);
			} else {
				setText(NLS.bind(Messages.Calendar_View_Action_DisplayTours_ByDay, numberOfTours));
			}
		}

		@Override
		public void run() {
			_calendarGraph.setNumberOfToursPerDay(numberOfTours);
			for (int j = 0; j < 5; j++) {
				_setNumberOfToursPerDay[j].setChecked((j == numberOfTours));
			}
			if (null != _setTourSizeDynamic) {
				_setTourSizeDynamic.setEnabled(numberOfTours != 0);
			}
		};
	}

	class TourInfoFormatAction extends Action {

		TourInfoFormatter	formatter;

		TourInfoFormatAction(final String text, final int style, final TourInfoFormatter formatter) {

			super(text, style);
			this.formatter = formatter;
		}

		@Override
		public void run() {
			_calendarGraph.setTourInfoFormatter(formatter);
			for (int i = 0; i < _tourInfoFormatter.length; i++) {
				_setTourInfoFormat[i].setChecked(i == formatter.index);
			}
		}
	}

	abstract class TourInfoFormatter {
		int	index;

		abstract String format(CalendarTourData data);

		abstract String getText();
	}

	public CalendarView() {}

	private void addPartListener() {

		_partListener = new IPartListener2() {
			@Override
			public void partActivated(final IWorkbenchPartReference partRef) {}

			@Override
			public void partBroughtToTop(final IWorkbenchPartReference partRef) {}

			@Override
			public void partClosed(final IWorkbenchPartReference partRef) {
				if (partRef.getPart(false) == CalendarView.this) {
					saveState();
				}
			}

			@Override
			public void partDeactivated(final IWorkbenchPartReference partRef) {}

			@Override
			public void partHidden(final IWorkbenchPartReference partRef) {}

			@Override
			public void partInputChanged(final IWorkbenchPartReference partRef) {}

			@Override
			public void partOpened(final IWorkbenchPartReference partRef) {}

			@Override
			public void partVisible(final IWorkbenchPartReference partRef) {}
		};
		getViewSite().getPage().addPartListener(_partListener);
	}

	private void addPrefListener() {

		_prefChangeListener = new IPropertyChangeListener() {

			public void propertyChange(final PropertyChangeEvent event) {

				final String property = event.getProperty();

				/*
				 * set a new chart configuration when the preferences has changed
				 */

				if (property.equals(ITourbookPreferences.APP_DATA_FILTER_IS_MODIFIED)) {

					refreshCalendar();

				} else if (property.equals(ITourbookPreferences.TOUR_TYPE_LIST_IS_MODIFIED)) {

					// update statistics
					refreshCalendar();

				}
			}

		};

		// add pref listener
		_prefStore.addPropertyChangeListener(_prefChangeListener);

	}

	// create and register our selection listener
	private void addSelectionListener() {

		_selectionListener = new ISelectionListener() {

			@Override
			public void selectionChanged(final IWorkbenchPart part, final ISelection selection) {

				// prevent to listen to a selection which is originated by this year chart
				if (part == CalendarView.this) {
					return;
				}

				onSelectionChanged(selection);

			}
		};

		// register selection listener in the page
		getSite().getPage().addPostSelectionListener(_selectionListener);
	}

	// create and register our selection provider
	private void addSelectionProvider() {

		getSite().setSelectionProvider(_selectionProvider = new SelectionProvider());

		_calendarGraph.addSelectionProvider(new ICalendarSelectionProvider() {

			@Override
			public void selectionChanged(final CalendarGraph.Selection selection) {
				if (selection.isTour()) {
					_selectionProvider.setSelection(new SelectionTourId(selection.id));
				}
			}

		});
	}

	private void addTourEventListener() {

		_tourPropertyListener = new ITourEventListener() {
			@Override
			public void tourChanged(final IWorkbenchPart part, final TourEventId eventId, final Object eventData) {

				if (eventId == TourEventId.TOUR_CHANGED || eventId == TourEventId.UPDATE_UI) {
					/*
					 * it is possible when a tour type was modified, the tour can be hidden or
					 * visible in the viewer because of the tour type filter
					 */
					refreshCalendar();

				} else if (eventId == TourEventId.TAG_STRUCTURE_CHANGED
						|| eventId == TourEventId.ALL_TOURS_ARE_MODIFIED) {

					refreshCalendar();
				}
			}
		};
		TourManager.getInstance().addTourEventListener(_tourPropertyListener);
	}

	private void contributeToActionBars() {
		final IActionBars bars = getViewSite().getActionBars();
		fillLocalPullDown(bars.getMenuManager());
		fillLocalToolBar(bars.getToolBarManager());
	}

	@Override
	public void createPartControl(final Composite parent) {

		addPartListener();
		addPrefListener();
		addTourEventListener();

		createUI(parent);

		makeActions();
		contributeToActionBars();

		addSelectionListener();
		addSelectionProvider();

		restoreState();

		// restore selection
		onSelectionChanged(getSite().getWorkbenchWindow().getSelectionService().getSelection());

		// final Menu contextMenu = TourContextMenu.getInstance().createContextMenu(this, _calendarGraph);
		final Menu contextMenu = (new TourContextMenu()).createContextMenu(this, _calendarGraph, getLocalActions());

		_calendarGraph.setMenu(contextMenu);

	}

	private void createUI(final Composite parent) {

		_pageBook = new PageBook(parent, SWT.NONE);
		_calendarComponents = new CalendarComponents(_pageBook, SWT.NORMAL);
		_calendarGraph = _calendarComponents.getGraph();
		_pageBook.showPage(_calendarComponents);
	}

	@Override
	public void dispose() {

		TourManager.getInstance().removeTourEventListener(_tourPropertyListener);
		getSite().getPage().removePostSelectionListener(_selectionListener);
		_prefStore.removePropertyChangeListener(_prefChangeListener);

		super.dispose();
	}

	private void fillLocalPullDown(final IMenuManager manager) {
		for (final Action element : _setTourInfoFormat) {
			manager.add(element);
		}
		manager.add(new Separator());
		for (final Action element : _setNumberOfToursPerDay) {
			manager.add(element);
		}
		manager.add(new Separator());
		manager.add(_setTourSizeDynamic);
		manager.add(new Separator());
		manager.add(_setNavigationStylePhysical);
		manager.add(_setNavigationStyleLogical);

	}

	private void fillLocalToolBar(final IToolBarManager manager) {
		_cymci = new CalendarYearMonthContributionItem(_calendarGraph);
		_calendarGraph.setYearMonthContributor(_cymci);
		manager.add(_cymci);
		manager.add(new Separator());
		// manager.add(_back);
		// manager.add(_forward);
		manager.add(_gotoToday);
		manager.add(new Separator());
		manager.add(_zoomIn);
		manager.add(_zoomOut);
		manager.add(new Separator());
		manager.add(_setLinked);
	}

	private ArrayList<Action> getLocalActions() {
		final ArrayList<Action> localActions = new ArrayList<Action>();
		localActions.add(_back);
		localActions.add(_gotoToday);
		localActions.add(_forward);
		return localActions;

	}

	@Override
	public ArrayList<TourData> getSelectedTours() {

		final ArrayList<TourData> selectedTourData = new ArrayList<TourData>();
		final ArrayList<Long> tourIdSet = new ArrayList<Long>();
		tourIdSet.add(_calendarGraph.getSelectedTour());
		for (final Long tourId : tourIdSet) {
			if (tourId > 0) { // < 0 means not selected
				selectedTourData.add(TourManager.getInstance().getTourData(tourId));
			}
		}
		return selectedTourData;
	}

	private void makeActions() {

		_back = new Action() {
			@Override
			public void run() {
				_calendarGraph.gotoPrevScreen();
			}
		};
		_back.setId("net.tourbook.calendar.back"); //$NON-NLS-1$
		_back.setText(Messages.Calendar_View_Action_Back);
		_back.setToolTipText(Messages.Calendar_View_Action_Back_Tooltip);
		_back.setImageDescriptor(TourbookPlugin.getImageDescriptor(Messages.Image__ArrowDown));

		_forward = new Action() {
			@Override
			public void run() {
				_calendarGraph.gotoNextScreen();
			}
		};
		_forward.setText(Messages.Calendar_View_Action_Forward);
		_forward.setToolTipText(Messages.Calendar_View_Action_Forward_Tooltip);
		_forward.setImageDescriptor(TourbookPlugin.getImageDescriptor(Messages.Image__ArrowUp));

		_zoomOut = new Action() {
			@Override
			public void run() {
				_calendarGraph.zoomOut();
			}
		};
		_zoomOut.setText(Messages.Calendar_View_Action_ZoomOut);
		_zoomOut.setToolTipText(Messages.Calendar_View_Action_ZoomOut_Tooltip);
		_zoomOut.setImageDescriptor(TourbookPlugin.getImageDescriptor(Messages.Image__ZoomOut));

		_zoomIn = new Action() {
			@Override
			public void run() {
				_calendarGraph.zoomIn();
			}
		};
		_zoomIn.setText(Messages.Calendar_View_Action_ZoomIn);
		_zoomIn.setToolTipText(Messages.Calendar_View_Action_ZoomIn_Tooltip);
		_zoomIn.setImageDescriptor(TourbookPlugin.getImageDescriptor(Messages.Image__ZoomIn));

		_setLinked = new Action(null, org.eclipse.jface.action.Action.AS_CHECK_BOX) {
			@Override
			public void run() {
				_calendarGraph.setLinked(_setLinked.isChecked());
			}
		};
		_setLinked.setText(Messages.Calendar_View_Action_LinkWithOtherViews);
		_setLinked.setImageDescriptor(TourbookPlugin.getImageDescriptor(Messages.Image__Synced));
		_setLinked.setChecked(true);

		_gotoToday = new Action() {
			@Override
			public void run() {
				_calendarGraph.gotoToday();
			}
		};
		_gotoToday.setText(Messages.Calendar_View_Action_GotoToday);
		_gotoToday.setImageDescriptor(TourbookPlugin.getImageDescriptor(Messages.Image__ZoomCentered));

		_setNavigationStylePhysical = new Action(null, org.eclipse.jface.action.Action.AS_RADIO_BUTTON) {
			@Override
			public void run() {
				_setNavigationStyleLogical.setChecked(false);
				_calendarGraph.setNavigationStyle(NavigationStyle.PHYSICAL);
			}
		};
		_setNavigationStylePhysical.setText(Messages.Calendar_View_Action_PhysicalNavigation);
		_setNavigationStylePhysical.setChecked(true);

		_setNavigationStyleLogical = new Action(null, org.eclipse.jface.action.Action.AS_RADIO_BUTTON) {
			@Override
			public void run() {
				_setNavigationStylePhysical.setChecked(false);
				_calendarGraph.setNavigationStyle(NavigationStyle.LOGICAL);
			}
		};
		_setNavigationStyleLogical.setText(Messages.Calendar_View_Action_LogicalNavigation);
		_setNavigationStyleLogical.setChecked(false);

		_setNumberOfToursPerDay = new Action[5];
		for (int i = 0; i < 5; i++) {
			_setNumberOfToursPerDay[i] = new NumberOfToursPerDayAction(
					null,
					org.eclipse.jface.action.Action.AS_RADIO_BUTTON,
					i);
		}

		_setTourSizeDynamic = new Action(null, org.eclipse.jface.action.Action.AS_CHECK_BOX) {
			@Override
			public void run() {
				_calendarGraph.setTourFieldSizeDynamic(this.isChecked());
			}
		};
		_setTourSizeDynamic.setText(Messages.Calendar_View_Action_ResizeTours);

		_setTourInfoFormat = new Action[_tourInfoFormatter.length];
		for (int i = 0; i < _tourInfoFormatter.length; i++) {
			_tourInfoFormatter[i].index = i;
			if (null != _tourInfoFormatter[i]) {
				_setTourInfoFormat[i] = new TourInfoFormatAction(
						_tourInfoFormatter[i].getText(),
						org.eclipse.jface.action.Action.AS_CHECK_BOX,
						_tourInfoFormatter[i]);
			}
		}
	}

	private void onSelectionChanged(final ISelection selection) {

		// show and select the selected tour
		if (selection instanceof SelectionTourId) {
			final Long newTourId = ((SelectionTourId) selection).getTourId();
			final Long oldTourId = _calendarGraph.getSelectedTour();
			if (newTourId != oldTourId) {
				if (_setLinked.isChecked()) {
					_calendarGraph.gotoTourId(newTourId);
				} else {
					_calendarGraph.removeSelection();
				}
			}
		} else if (selection instanceof SelectionDeletedTours) {
			_calendarGraph.refreshCalendar();
		}
	}

	private void refreshCalendar() {
		if (null != _calendarGraph) {
			_calendarGraph.refreshCalendar();
		}
	}

	private void restoreState() {

		final int numWeeksDisplayed = Util.getStateInt(_state, STATE_NUM_OF_WEEKS, 5);
		_calendarGraph.setZoom(numWeeksDisplayed);

		final Long dateTimeMillis = Util.getStateLong(_state, STATE_FIRST_DAY, (new DateTime()).getMillis());
		final DateTime firstDate = new DateTime(dateTimeMillis);
		_calendarGraph.setFirstDay(firstDate);

		final Long selectedTourId = Util.getStateLong(_state, STATE_SELECTED_TOURS, new Long(-1));
		_calendarGraph.setSelectionTourId(selectedTourId);

//		final String[] selectedTourIds = _state.getArray(STATE_SELECTED_TOURS);
//		_selectedTourIds.clear();
//
//		if (selectedTourIds != null) {
//			for (final String tourId : selectedTourIds) {
//				try {
//					_selectedTourIds.add(Long.valueOf(tourId));
//				} catch (final NumberFormatException e) {
//					// ignore
//				}
//			}
//		}

		_setLinked.setChecked(Util.getStateBoolean(_state, STATE_IS_LINKED, true));

		_setTourSizeDynamic.setChecked(Util.getStateBoolean(_state, STATE_TOUR_SIZE_DYNAMIC, true));

		final int numberOfTours = Util.getStateInt(_state, STATE_NUMBER_OF_TOURS_PER_DAY, 3);
		if (numberOfTours < _setNumberOfToursPerDay.length) {
			_setNumberOfToursPerDay[numberOfTours].run();
		}

		final int tourInfoFormatterIndex = Util.getStateInt(_state, STATE_TOUR_INFO_FORMATTER_INDEX, 0);
		_setTourInfoFormat[tourInfoFormatterIndex].run();

	}

	private void saveState() {

		// save current date displayed
		_state.put(STATE_FIRST_DAY, _calendarGraph.getFirstDay().getMillis());

		// save number of weeks displayed
		_state.put(STATE_NUM_OF_WEEKS, _calendarGraph.getZoom());

		// convert tour id's into string
		// final ArrayList<String> selectedTourIds = new ArrayList<String>();
		// for (final Long tourId : _selectedTourIds) {
		// 	selectedTourIds.add(tourId.toString());
		// }
		// until now we only implement single tour selection
		_state.put(STATE_SELECTED_TOURS, _calendarGraph.getSelectedTour());

		_state.put(STATE_IS_LINKED, _setLinked.isChecked());
		_state.put(STATE_TOUR_SIZE_DYNAMIC, _setTourSizeDynamic.isChecked());

		_state.put(STATE_NUMBER_OF_TOURS_PER_DAY, _calendarGraph.getNumberOfToursPerDay());

		_state.put(STATE_TOUR_INFO_FORMATTER_INDEX, _calendarGraph.getTourInfoFormatterIndex());
	}

	/**
	 * Passing the focus request to the viewer's control.
	 */
	@Override
	public void setFocus() {
		_calendarComponents.setFocus();
	}

//	private void showMessage(final String message) {
//		MessageDialog.openInformation(_pageBook.getShell(), "%view_name_Calendar", message);
//	}

}
