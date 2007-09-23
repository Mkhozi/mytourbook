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

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;

import net.tourbook.Messages;
import net.tourbook.chart.Chart;
import net.tourbook.chart.ChartDataModel;
import net.tourbook.chart.ChartDataXSerie;
import net.tourbook.chart.IChartListener;
import net.tourbook.chart.ISliderMoveListener;
import net.tourbook.chart.SelectionChartInfo;
import net.tourbook.data.TourCompared;
import net.tourbook.data.TourData;
import net.tourbook.database.TourDatabase;
import net.tourbook.plugin.TourbookPlugin;
import net.tourbook.tour.IDataModelListener;
import net.tourbook.tour.ITourPropertyListener;
import net.tourbook.tour.SelectionTourChart;
import net.tourbook.tour.SelectionTourData;
import net.tourbook.tour.TourChart;
import net.tourbook.tour.TourManager;
import net.tourbook.tour.TreeViewerItem;
import net.tourbook.ui.views.TourChartViewPart;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.ui.part.PageBook;

// author: Wolfgang Schramm
// create: 06.09.2007

public class TourMapComparedTourView extends TourChartViewPart implements ISynchedChart {

	public static final String					ID						= "net.tourbook.views.tourMap.comparedTourView";	//$NON-NLS-1$

	/*
	 * keep data from the reference tour view
	 */
	private long								fRefTourRefId			= -1;
	private TourChart							fRefTourTourChart;
	private int									fRefTourXMarkerValueDifference;

	/*
	 * CT ... (c)ompared (t)our which is displayed in this view
	 */

	/**
	 * key for the {@link TourCompared} instance or <code>-1</code> when it's not saved in the
	 * database
	 */
	private long								fCTCompareId;

	/**
	 * Tour Id for the displayed compared tour
	 */
	private long								fCTTourId				= -1;

	/**
	 * Reference Id for the displayed compared tour
	 */
	private long								fCTRefId				= -1;

	/**
	 * Reference tour chart for the displayed compared tour
	 */
	private TourChart							fCTRefTourChart;

	/**
	 * Tour Id of the visible compared tour
	 */
	private long								fVisibleComparedTourId	= -1;

	private PageBook							fPageBook;
	private Label								fPageNoChart;

	private ITourPropertyListener				fRefTourPropertyListener;

	private ActionSynchChartHorizontalByScale	fActionSynchChartsByScale;
	private ActionSynchChartHorizontalBySize	fActionSynchChartsBySize;

	private ActionSaveComparedTour				fActionSaveComparedTour;
	private ActionUndoChanges					fActionUndoChanges;

	private boolean								fIsDataDirty;

	private int									fMovedMarkerStartIndex;
	private int									fMovedMarkerEndIndex;

	private int									fOriginalStartIndex;
	private int									fOriginalEndIndex;

//	private TreeViewer							fTourViewer;

//	private ArrayList<TourMapItemComparedTour>	fComparedToursFindResult;

	private class ActionSaveComparedTour extends Action {

		public ActionSaveComparedTour() {

			super(null, AS_PUSH_BUTTON);

			setToolTipText("Save moved marker in the compared tour");

			setImageDescriptor(TourbookPlugin.getImageDescriptor("floppy_disc.gif"));
			setDisabledImageDescriptor(TourbookPlugin.getImageDescriptor("floppy_disc_disabled.gif"));

			setEnabled(false);
		}

		@Override
		public void run() {
			saveComparedTour();
		}
	}

	private class ActionUndoChanges extends Action {

		public ActionUndoChanges() {

			super(null, AS_PUSH_BUTTON);

			setToolTipText("Undo changes, set moved marker to original position");

			setImageDescriptor(TourbookPlugin.getImageDescriptor("undo-edit.gif"));
			setDisabledImageDescriptor(TourbookPlugin.getImageDescriptor("undo-edit-disabled.gif"));

			setEnabled(false);
		}

		@Override
		public void run() {
			undoChanges();
		}
	}

	private void addRefTourPropertyListener() {

		fRefTourPropertyListener = new ITourPropertyListener() {
			public void propertyChanged(int propertyId, Object propertyData) {

				if (propertyId == TourManager.TOUR_PROPERTY_REFERENCE_TOUR_CHANGED
						&& propertyData instanceof TourPropertyRefTourChanged) {

					TourPropertyRefTourChanged tourProperty = (TourPropertyRefTourChanged) propertyData;

					fRefTourRefId = tourProperty.refId;
					fRefTourTourChart = tourProperty.refTourChart;
					fRefTourXMarkerValueDifference = tourProperty.xMarkerValue;

					if (showComparedTour() == false) {
						synchCharts();
					}
				}
			}
		};

		TourManager.getInstance().addPropertyListener(fRefTourPropertyListener);
	}

	private void createActions() {

		fActionSynchChartsBySize = new ActionSynchChartHorizontalBySize(this);
		fActionSynchChartsByScale = new ActionSynchChartHorizontalByScale(this);

		fActionSaveComparedTour = new ActionSaveComparedTour();
		fActionUndoChanges = new ActionUndoChanges();

		final IToolBarManager tbm = fTourChart.getToolBarManager();

		tbm.add(fActionSaveComparedTour);
		tbm.add(fActionUndoChanges);

		tbm.add(new Separator());
		tbm.add(fActionSynchChartsByScale);
		tbm.add(fActionSynchChartsBySize);

		tbm.update(true);
	}

	@Override
	public void createPartControl(Composite parent) {

		super.createPartControl(parent);

		fPageBook = new PageBook(parent, SWT.NONE);

		fPageNoChart = new Label(fPageBook, SWT.NONE);
		fPageNoChart.setText(Messages.UI_Label_no_chart_is_selected);

		fTourChart = new TourChart(fPageBook, SWT.FLAT, true);
		fTourChart.setShowZoomActions(true);
		fTourChart.setShowSlider(true);
		fTourChart.setToolBarManager(getViewSite().getActionBars().getToolBarManager(), true);

		fTourChart.addDoubleClickListener(new Listener() {
			public void handleEvent(Event event) {
				TourManager.getInstance().openTourInEditor(fTourData.getTourId());
			}
		});

		// fire a slider move selection when a slider was moved in the tour chart
		fTourChart.addSliderMoveListener(new ISliderMoveListener() {
			public void sliderMoved(final SelectionChartInfo chartInfoSelection) {
				fPostSelectionProvider.setSelection(chartInfoSelection);
			}
		});

		createActions();

		addRefTourPropertyListener();

		// show current selected tour
		ISelection selection = getSite().getWorkbenchWindow().getSelectionService().getSelection();
		if (selection != null) {
			onSelectionChanged(selection);
		} else {
			fPageBook.showPage(fPageNoChart);
		}

		synchCharts();
	}

	@Override
	public void dispose() {

		saveComparedTourDialog();

		TourManager.getInstance().removePropertyListener(fRefTourPropertyListener);

		super.dispose();
	}

	private void onMoveSynchedMarker(	final int movedSynchMarkerValueIndex,
										final int movedSynchMarkerEndIndex) {

		// update the chart
		final ChartDataModel chartDataModel = fTourChart.getChartDataModel();
		final ChartDataXSerie xData = chartDataModel.getXData();

		xData.setSynchMarkerValueIndex(movedSynchMarkerValueIndex, movedSynchMarkerEndIndex);
		xData.setRangeMarkers(new int[] { fOriginalStartIndex }, new int[] { fOriginalEndIndex });

		fTourChart.updateChart(chartDataModel);

		// keep marker position for saving the tour
		fMovedMarkerStartIndex = movedSynchMarkerValueIndex;
		fMovedMarkerEndIndex = movedSynchMarkerEndIndex;

		// check if the data are dirty
		boolean isDataDirty;
		if (fOriginalStartIndex == fMovedMarkerStartIndex
				&& fOriginalEndIndex == fMovedMarkerEndIndex) {
			isDataDirty = false;
		} else {
			isDataDirty = true;
		}
		setDataDirty(isDataDirty);

		updateTourViewer(fMovedMarkerStartIndex, fMovedMarkerEndIndex);

		// force the year chart to be refreshed
//		fYearChartYear = -1;

		// reset the min/max size in the year view
//		if (ttiComparedTour.getParentItem() instanceof TVITourMapYear) {
//			final TVITourMapYear ttiTourMapYear = (TVITourMapYear) ttiComparedTour.getParentItem();
//			final TVTITourMapReferenceTour refItem = ttiTourMapYear.getRefItem();
//			refItem.yearMapMinValue = Integer.MIN_VALUE;
//
//			updateYearBarChart(ttiTourMapYear);
//		}
	}

	@Override
	protected void onSelectionChanged(ISelection selection) {

		if (selection instanceof SelectionTourMap) {

			showComparedTour((SelectionTourMap) selection);

		} else if (selection instanceof StructuredSelection) {

			StructuredSelection structuredSelection = (StructuredSelection) selection;

			Object firstElement = structuredSelection.getFirstElement();

			if (firstElement instanceof TourMapItemComparedTour) {
				TourMapItemComparedTour comparedTour = (TourMapItemComparedTour) firstElement;

				showComparedTour(comparedTour);
			}
		}
	}

	private void saveComparedTour() {

		if (fIsDataDirty == false) {
			return;
		}

		final EntityManager em = TourDatabase.getInstance().getEntityManager();
		final EntityTransaction ts = em.getTransaction();

		try {
			final TourCompared comparedTour = em.find(TourCompared.class, fCTCompareId);

			if (comparedTour != null) {

				// update the changed x-marker index
				comparedTour.setStartIndex(fMovedMarkerStartIndex);
				comparedTour.setEndIndex(fMovedMarkerEndIndex);

				final float speed = TourManager.computeTourSpeed(fTourChart.getChartDataModel(),
						fMovedMarkerStartIndex,
						fMovedMarkerEndIndex);

				comparedTour.setTourSpeed(speed);

				// update the entity
				ts.begin();
				em.merge(comparedTour);
				ts.commit();

				setDataDirty(false);

				updateTourViewer(fMovedMarkerStartIndex, fMovedMarkerEndIndex, speed);
			}
		} catch (final Exception e) {
			e.printStackTrace();
		} finally {
			if (ts.isActive()) {
				ts.rollback();
			}
			em.close();
		}
	}

	private void saveComparedTourDialog() {

		if (fCTCompareId == -1) {
			setDataDirty(false);
			return;
		}

		if (fIsDataDirty == false) {
			return;
		}

		MessageBox msgBox = new MessageBox(Display.getDefault().getActiveShell(), SWT.ICON_QUESTION
				| SWT.YES
				| SWT.NO);

		msgBox.setText("Save Compared Tour");
		msgBox.setMessage("The compared tour '"
				+ TourManager.getTourTitleDetailed(fTourData)
				+ "' was modified, save changes?");

		if (msgBox.open() == SWT.YES) {
			saveComparedTour();
		} else {
			updateTourViewer(fOriginalStartIndex, fOriginalEndIndex);
		}

		setDataDirty(false);

	}

	private void setDataDirty(boolean isDirty) {

		fIsDataDirty = isDirty;

		fActionSaveComparedTour.setEnabled(isDirty && fCTCompareId != -1);
		fActionUndoChanges.setEnabled(isDirty);
	}

	@Override
	public void setFocus() {
		fTourChart.setFocus();

		fPostSelectionProvider.setSelection(new SelectionTourChart(fTourChart));
	}

	/**
	 * @return Returns <code>false</code> when the compared tour was not displayed
	 */
	private boolean showComparedTour() {

		// check if the compared tour is already displayed
		if (fVisibleComparedTourId == fCTTourId) {
			return false;
		}

		TourCompareConfig tourCompareConfig = ReferenceTourManager.getInstance()
				.getTourCompareConfig(fCTRefId);

		if (tourCompareConfig != null) {

			fVisibleComparedTourId = fCTTourId;

			fTourChartConfig = tourCompareConfig.getCompTourChartConfig();
			fTourChartConfig.setMinMaxKeeper(true);

			/*
			 * fire change event that tour markers are updated
			 */
			fPostSelectionProvider.setSelection(new SelectionTourData(fTourChart,
					fTourChart.getTourData()));

			updateChart();

			synchCharts();

			return true;
		}

		return false;
	}

	/**
	 * Shows the compared tour which was selected by the user in the {@link TourMapView}
	 * 
	 * @param selectionComparedTour
	 */
	private void showComparedTour(final SelectionTourMap selectionComparedTour) {

		final Long ctTourId = selectionComparedTour.getCompTourId();

		// check if the compared tour is already displayed
		if (ctTourId == null || fCTTourId == ctTourId) {
			return;
		}

		saveComparedTourDialog();

		// load the tourdata of the compared tour from the database
		final TourData compTourData = TourManager.getInstance().getTourData(ctTourId);
		if (compTourData == null) {
			return;
		}

		// set data from the selection
		fCTTourId = ctTourId;
		fCTRefId = selectionComparedTour.getRefId();
		fCTCompareId = selectionComparedTour.getCompareId();

		fTourData = compTourData;

		fMovedMarkerStartIndex = fOriginalStartIndex = selectionComparedTour.getCompareStartIndex();
		fMovedMarkerEndIndex = fOriginalEndIndex = selectionComparedTour.getCompareEndIndex();

		fTourChart.addDataModelListener(new IDataModelListener() {
			public void dataModelChanged(ChartDataModel changedChartDataModel) {

				ChartDataXSerie xData = changedChartDataModel.getXData();

				// set initial synch marker position
				xData.setSynchMarkerValueIndex(fMovedMarkerStartIndex, fMovedMarkerEndIndex);

				xData.setRangeMarkers(new int[] { fOriginalStartIndex },
						new int[] { fOriginalEndIndex });

				// set chart title
				changedChartDataModel.setTitle(TourManager.getTourTitleDetailed(fTourData));
			}
		});

		fTourChart.addXMarkerDraggingListener(new IChartListener() {

			public int getXMarkerValueDiff() {
				return fRefTourXMarkerValueDifference;
			}

			public void xMarkerMoved(	final int movedXMarkerStartValueIndex,
										final int movedXMarkerEndValueIndex) {
				onMoveSynchedMarker(movedXMarkerStartValueIndex, movedXMarkerEndValueIndex);
			}
		});

		// enable save action when the tour is already saved in the database
//		fActionSaveComparedTour.setEnabled(fCTCompareId != -1);

		showComparedTour();
	}

	private void showComparedTour(TourMapItemComparedTour tvtiComparedTour) {

		final TourMapItemComparedTour compItem = (TourMapItemComparedTour) tvtiComparedTour;

		final SelectionTourMap comparedTour = new SelectionTourMap(compItem.getRefId());

		final TreeViewerItem parentItem = compItem.getParentItem();
		if (parentItem instanceof TourMapItemYear) {
			comparedTour.setYearItem((TourMapItemYear) parentItem);
		}

		comparedTour.setTourCompareData(compItem.getCompId(),
				compItem.getTourId(),
				compItem.getStartIndex(),
				compItem.getEndIndex());

		showComparedTour(comparedTour);
	}

	private void synchCharts() {

		// check initial value
		if (fCTRefId == -1) {
			fActionSynchChartsByScale.setEnabled(false);
			fActionSynchChartsBySize.setEnabled(false);
			return;
		}

		boolean isSynchEnabled = false;

		if (fCTRefId == fRefTourRefId) {

			// reference tour for the compared chart is displayed

			if (fCTRefTourChart != fRefTourTourChart) {
				fCTRefTourChart = fRefTourTourChart;
			}

			isSynchEnabled = true;

		} else {

			// another ref tour is displayed, disable synchronization

			if (fCTRefTourChart != null) {
				fCTRefTourChart.synchChart(false, fTourChart, Chart.SYNCH_MODE_NO);
			}
			fActionSynchChartsByScale.setChecked(false);
			fActionSynchChartsBySize.setChecked(false);
		}

		fActionSynchChartsByScale.setEnabled(isSynchEnabled);
		fActionSynchChartsBySize.setEnabled(isSynchEnabled);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.tourbook.ui.views.tourMap.ISynchedChart#synchCharts(boolean, int)
	 */
	public void synchCharts(boolean isSynched, int synchMode) {
		if (fCTRefTourChart != null) {

			// uncheck other synch mode
			switch (synchMode) {
			case Chart.SYNCH_MODE_BY_SCALE:
				fActionSynchChartsBySize.setChecked(false);
				break;

			case Chart.SYNCH_MODE_BY_SIZE:
				fActionSynchChartsByScale.setChecked(false);
				break;

			default:
				break;
			}

			fCTRefTourChart.synchChart(isSynched, fTourChart, synchMode);
		}
	}

	private void undoChanges() {

		// set synch marker to original position
		final ChartDataModel chartDataModel = fTourChart.getChartDataModel();
		final ChartDataXSerie xData = chartDataModel.getXData();

		xData.setSynchMarkerValueIndex(fOriginalStartIndex, fOriginalEndIndex);

		fTourChart.updateChart(chartDataModel);

		setDataDirty(false);

		updateTourViewer(fOriginalStartIndex, fOriginalEndIndex);
	}

	@Override
	protected void updateChart() {

		if (fTourData == null) {
			return;
		}

		fTourChart.updateTourChart(fTourData, fTourChartConfig, false);

		fPageBook.showPage(fTourChart);

		// set application window title
		setTitleToolTip(TourManager.getTourDate(fTourData));
	}

	/**
	 * update tour map and compare result view
	 */
	private void updateTourViewer(int startIndex, int endIndex) {

		final float speed = TourManager.computeTourSpeed(fTourChart.getChartDataModel(),
				startIndex,
				endIndex);

		updateTourViewer(startIndex, endIndex, speed);
	}

	/**
	 * update tour map and compare result view
	 */
	private void updateTourViewer(int startIndex, int endIndex, final float speed) {

		fPostSelectionProvider.setSelection(new SelectionComparedTour(fCTCompareId,
				startIndex,
				endIndex,
				speed));
	}

}
