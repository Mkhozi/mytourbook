/*******************************************************************************
 * Copyright (C) 2005, 2017 Wolfgang Schramm and Contributors
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
package net.tourbook.ui.views.calendar;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.time.LocalDate;
import java.util.ArrayList;

import net.tourbook.Messages;
import net.tourbook.application.TourbookPlugin;
import net.tourbook.common.UI;
import net.tourbook.common.color.GraphColorManager;
import net.tourbook.common.formatter.IValueFormatter;
import net.tourbook.common.formatter.ValueFormat;
import net.tourbook.common.formatter.ValueFormatter_Number_1_0;
import net.tourbook.common.formatter.ValueFormatter_Number_1_1;
import net.tourbook.common.formatter.ValueFormatter_Number_1_2;
import net.tourbook.common.formatter.ValueFormatter_Number_1_3;
import net.tourbook.common.formatter.ValueFormatter_Time_HH;
import net.tourbook.common.formatter.ValueFormatter_Time_HHMM;
import net.tourbook.common.formatter.ValueFormatter_Time_HHMMSS;
import net.tourbook.common.time.TimeTools;
import net.tourbook.common.util.StatusUtil;
import net.tourbook.common.util.Util;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.ListenerList;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.XMLMemento;
import org.osgi.framework.Bundle;
import org.osgi.framework.Version;

public class CalendarProfileManager {

	private static final String				PROFILE_FILE_NAME						= "calendar-profiles.xml";			//$NON-NLS-1$
	//
	/**
	 * Version number is not yet used.
	 */
	private static final int				PROFILE_VERSION							= 1;
	//
// SET_FORMATTING_OFF
	//
	private static final Bundle				_bundle									= TourbookPlugin.getDefault().getBundle();
	private static final IPath				_stateLocation							= Platform.getStateLocation(_bundle);
	//
// SET_FORMATTING_ON
	//
	// common attributes
	private static final String				ATTR_ACTIVE_PROFILE_ID					= "activeProfileId";				//$NON-NLS-1$
	private static final String				ATTR_ID									= "id";								//$NON-NLS-1$
	private static final String				ATTR_PROFILE_NAME						= "name";							//$NON-NLS-1$
	//
	/*
	 * Root
	 */
	private static final String				TAG_ROOT								= "CalendarProfiles";				//$NON-NLS-1$
	private static final String				ATTR_PROFILE_VERSION					= "profileVersion";					//$NON-NLS-1$

	//
	/*
	 * Calendars
	 */
	private static final String				TAG_CALENDAR_PROFILE					= "CalendarProfile";				//$NON-NLS-1$
	private static final String				TAG_CALENDAR							= "Calendar";						//$NON-NLS-1$
	//
	private static final String				TAG_ALL_TOUR_FORMATTER					= "AllTourFormatter";				//$NON-NLS-1$
	private static final String				TAG_ALL_WEEK_FORMATTER					= "AllWeekFormatter";				//$NON-NLS-1$
	private static final String				TAG_ALTERNATE_MONTH_RGB					= "AlternateMonthRGB";				//$NON-NLS-1$
	private static final String				TAG_CALENDAR_BACKGROUND_RGB				= "CalendarBackgroundRGB";			//$NON-NLS-1$
	private static final String				TAG_CALENDAR_FOREGROUND_RGB				= "CalendarForegroundRGB";			//$NON-NLS-1$
	private static final String				TAG_FORMATTER							= "Formatter";						//$NON-NLS-1$
	//
	private static final String				ATTR_IS_HIDE_DAY_DATE_WHEN_NO_TOUR		= "isHideDayDateWhenNoTour";		//$NON-NLS-1$
	private static final String				ATTR_IS_SHOW_DATE_COLUMN				= "isShowDateColumn";				//$NON-NLS-1$
	private static final String				ATTR_IS_SHOW_DAY_DATE					= "isShowDayDate";					//$NON-NLS-1$
	private static final String				ATTR_IS_SHOW_DAY_DATE_WEEKEND_COLOR		= "isShowDayDateWeekendColor";		//$NON-NLS-1$
	private static final String				ATTR_IS_SHOW_SUMMARY_COLUMN				= "isShowSummaryColumn";			//$NON-NLS-1$
	private static final String				ATTR_IS_SHOW_TOUR_CONTENT				= "isShowTourContent";				//$NON-NLS-1$
	private static final String				ATTR_IS_SHOW_TOUR_VALUE_UNIT			= "isShowTourValueUnit";			//$NON-NLS-1$
	private static final String				ATTR_IS_SHOW_YEAR_COLUMNS				= "isShowYearColumns";				//$NON-NLS-1$
	private static final String				ATTR_IS_SHOW_VALUE						= "isShowValue";					//$NON-NLS-1$
	private static final String				ATTR_IS_SHOW_WEEK_VALUE_UNIT			= "isShowWeekValueUnit";			//$NON-NLS-1$
	private static final String				ATTR_IS_TOGGLE_MONTH_COLOR				= "isToggleMonthColor";				//$NON-NLS-1$
	private static final String				ATTR_IS_TRUNCATE_TOUR_TEXT				= "isTruncateTourText";				//$NON-NLS-1$
	private static final String				ATTR_PROFILE_DEFAULT_ID					= "profileDefaultId";				//$NON-NLS-1$
	private static final String				ATTR_DATE_COLUMN_CONTENT				= "dateColumnContent";				//$NON-NLS-1$
	private static final String				ATTR_DATE_COLUMN_FONT					= "dateColumnFont";					//$NON-NLS-1$
	private static final String				ATTR_DATE_COLUMN_WIDTH					= "dateColumnWidth";				//$NON-NLS-1$
	private static final String				ATTR_DAY_DATE_FORMAT					= "dayDateFormat";					//$NON-NLS-1$
	private static final String				ATTR_DAY_DATE_FONT						= "dayDateFont";					//$NON-NLS-1$
	private static final String				ATTR_FORMATTER_ID						= "formatterId";					//$NON-NLS-1$
	private static final String				ATTR_FORMATTER_VALUE_FORMAT				= "formatterValueFormat";			//$NON-NLS-1$
	private static final String				ATTR_TOUR_BACKGROUND					= "tourBackground";					//$NON-NLS-1$
	private static final String				ATTR_TOUR_BACKGROUND_COLOR1				= "tourBackgroundColor1";			//$NON-NLS-1$
	private static final String				ATTR_TOUR_BACKGROUND_COLOR2				= "tourBackgroundColor2";			//$NON-NLS-1$
	private static final String				ATTR_TOUR_BORDER_WIDTH					= "tourBackgroundWidth";			//$NON-NLS-1$
	private static final String				ATTR_TOUR_BORDER						= "tourBorder";						//$NON-NLS-1$
	private static final String				ATTR_TOUR_BORDER_COLOR					= "tourBorderColor";				//$NON-NLS-1$
	private static final String				ATTR_TOUR_BACKGROUND_WIDTH				= "tourBorderWidth";				//$NON-NLS-1$
	private static final String				ATTR_TOUR_CONTENT_FONT					= "tourContentFont";				//$NON-NLS-1$
	private static final String				ATTR_TOUR_CONTENT_COLOR					= "tourContentColor";				//$NON-NLS-1$
	private static final String				ATTR_TOUR_TITLE_COLOR					= "tourTitleColor";					//$NON-NLS-1$
	private static final String				ATTR_TOUR_TITLE_FONT					= "tourTitleFont";					//$NON-NLS-1$
	private static final String				ATTR_TOUR_TRUNCATED_LINES				= "tourTruncatedLines";				//$NON-NLS-1$
	private static final String				ATTR_TOUR_VALUE_COLOR					= "tourValueColor";					//$NON-NLS-1$
	private static final String				ATTR_TOUR_VALUE_COLUMNS					= "tourValueColumns";				//$NON-NLS-1$
	private static final String				ATTR_TOUR_VALUE_FONT					= "tourValueFont";					//$NON-NLS-1$
	private static final String				ATTR_USE_DRAGGED_SCROLLING				= "useDraggedScrolling";			//$NON-NLS-1$
	private static final String				ATTR_WEEK_COLUMN_WIDTH					= "weekColumnWidth";				//$NON-NLS-1$
	private static final String				ATTR_WEEK_HEIGHT						= "weekHeight";						//$NON-NLS-1$
	private static final String				ATTR_WEEK_VALUE_COLOR					= "weekValueColor";					//$NON-NLS-1$
	private static final String				ATTR_WEEK_VALUE_FONT					= "weekValueFont";					//$NON-NLS-1$
	private static final String				ATTR_YEAR_COLUMNS						= "yearColumns";					//$NON-NLS-1$
	private static final String				ATTR_YEAR_COLUMNS_SPACING				= "yearColumnsSpacing";				//$NON-NLS-1$
	private static final String				ATTR_YEAR_COLUMNS_START					= "yearColumnsStart";				//$NON-NLS-1$
	private static final String				ATTR_YEAR_HEADER_FONT					= "yearHeaderFont";					//$NON-NLS-1$
	//
	static final RGB						DEFAULT_ALTERNATE_MONTH_RGB				= new RGB(0xf0, 0xf0, 0xf0);
	static final ProfileDefault				DEFAULT_PROFILE_DEFAULT_ID				= ProfileDefault.DEFAULT;
	static final RGB						DEFAULT_CALENDAR_BACKGROUND_RGB;
	static final RGB						DEFAULT_CALENDAR_FOREBACKGROUND_RGB;
	static final DayDateFormat				DEFAULT_DAY_DATE_FORMAT					= DayDateFormat.DAY;
	static final int						DEFAULT_DATE_COLUMN_WIDTH				= 50;
	static final DateColumnContent			DEFAULT_DATE_COLUMN_CONTENT				= DateColumnContent.MONTH;
	static final DataFormatter				DEFAULT_EMPTY_FORMATTER;
	static final boolean					DEFAULT_IS_SHOW_DAY_DATE_WEEKEND_COLOR	= false;
	static final boolean					DEFAULT_IS_TRUNCATE_TOUR_TEXT			= true;
	static final int						DEFAULT_SUMMARY_COLUMN_WIDTH			= 100;
	static final TourBackground				DEFAULT_TOUR_BACKGROUND					= TourBackground.FILL;
	static final CalendarColor				DEFAULT_TOUR_BACKGROUND_COLOR1			= CalendarColor.DARK;
	static final CalendarColor				DEFAULT_TOUR_BACKGROUND_COLOR2			= CalendarColor.BRIGHT;
	static final int						DEFAULT_TOUR_BACKGROUND_WIDTH			= 3;
	static final TourBorder					DEFAULT_TOUR_BORDER						= TourBorder.NO_BORDER;
	static final CalendarColor				DEFAULT_TOUR_BORDER_COLOR				= CalendarColor.LINE;
	static final int						DEFAULT_TOUR_BORDER_WIDTH				= 1;
	static final CalendarColor				DEFAULT_TOUR_COLOR						= CalendarColor.CONTRAST;
	static final int						DEFAULT_TOUR_TRUNCATED_LINES			= 3;
	static final int						DEFAULT_TOUR_VALUE_COLUMNS				= 3;
	static final int						DEFAULT_WEEK_HEIGHT						= 120;
	static final CalendarColor				DEFAULT_WEEK_VALUE_COLOR				= CalendarColor.BRIGHT;
	static final int						DEFAULT_YEAR_COLUMNS					= 1;
	static final ColumnStart				DEFAULT_YEAR_COLUMNS_LAYOUT				= ColumnStart.CONTINUOUSLY;
	static final int						DEFAULT_YEAR_COLUMNS_SPACING			= 30;
	//
	static final FormatterData[]			DEFAULT_TOUR_FORMATTER_DATA;
	static final FormatterData[]			DEFAULT_WEEK_FORMATTER_DATA;
	static final int						NUM_DEFAULT_TOUR_FORMATTER;
	static final int						NUM_DEFAULT_WEEK_FORMATTER;
	//
	static final int						YEAR_COLUMNS_MIN						= 1;
	static final int						YEAR_COLUMNS_MAX						= 100;
	static final int						CALENDAR_COLUMNS_SPACE_MIN				= 0;
	static final int						CALENDAR_COLUMNS_SPACE_MAX				= 300;
	static final int						WEEK_HEIGHT_MIN							= 1;
	static final int						WEEK_HEIGHT_MAX							= 500;

	private static final IValueFormatter	_valueFormatter_Number_1_0				= new ValueFormatter_Number_1_0();
	private static final IValueFormatter	_valueFormatter_Number_1_1				= new ValueFormatter_Number_1_1();
	private static final IValueFormatter	_valueFormatter_Number_1_2				= new ValueFormatter_Number_1_2();
	private static final IValueFormatter	_valueFormatter_Number_1_3				= new ValueFormatter_Number_1_3();
	private static final IValueFormatter	_valueFormatter_Time_HH					= new ValueFormatter_Time_HH();
	private static final IValueFormatter	_valueFormatter_Time_HHMM				= new ValueFormatter_Time_HHMM();
	private static final IValueFormatter	_valueFormatter_Time_HHMMSS				= new ValueFormatter_Time_HHMMSS();

	private static final DataFormatter		_tourFormatter_Altitude;
	private static final DataFormatter		_tourFormatter_Distance;
	private static final DataFormatter		_tourFormatter_Pace;
	private static final DataFormatter		_tourFormatter_Speed;
	private static final DataFormatter		_tourFormatter_Time_Moving;
	private static final DataFormatter		_tourFormatter_Time_Paused;
	private static final DataFormatter		_tourFormatter_Time_Recording;
	private static final DataFormatter		_tourFormatter_TourDescription;
	private static final DataFormatter		_tourFormatter_TourTitle;

	private static final DataFormatter		_weekFormatter_Altitude;
	private static final DataFormatter		_weekFormatter_Distance;
	private static final DataFormatter		_weekFormatter_Pace;
	private static final DataFormatter		_weekFormatter_Speed;
	private static final DataFormatter		_weekFormatter_Time_Moving;
	private static final DataFormatter		_weekFormatter_Time_Paused;
	private static final DataFormatter		_weekFormatter_Time_Recording;

	static final DataFormatter[]			allTourContentFormatter;
	static final DataFormatter[]			allWeekFormatter;

// SET_FORMATTING_OFF
	//
	static {

		DEFAULT_CALENDAR_BACKGROUND_RGB         = new RGB (59, 59, 59);
		DEFAULT_CALENDAR_FOREBACKGROUND_RGB     = new RGB (197, 197, 197);

//		DEFAULT_CALENDAR_BACKGROUND_RGB			= Display.getCurrent().getSystemColor(SWT.COLOR_LIST_BACKGROUND).getRGB();
//		DEFAULT_CALENDAR_FOREBACKGROUND_RGB		= Display.getCurrent().getSystemColor(SWT.COLOR_LIST_FOREGROUND).getRGB();

		/*
		 * Formatter
		 */
		DEFAULT_EMPTY_FORMATTER 		= createFormatter_Empty();

		// Tour
		_tourFormatter_TourDescription	= createFormatter_TourDescription();
		_tourFormatter_TourTitle	 	= createFormatter_TourTitle();
		
		_tourFormatter_Altitude 		= createFormatter_Altitude();
		_tourFormatter_Distance 		= createFormatter_Distance();

		_tourFormatter_Pace 			= createFormatter_Pace();
		_tourFormatter_Speed 			= createFormatter_Speed();

		_tourFormatter_Time_Moving 		= createFormatter_Time_Moving();
		_tourFormatter_Time_Paused 		= createFormatter_Time_Paused();
		_tourFormatter_Time_Recording 	= createFormatter_Time_Recording();

		allTourContentFormatter = new DataFormatter[] {
				
				DEFAULT_EMPTY_FORMATTER,
				
				_tourFormatter_TourTitle,
				_tourFormatter_TourDescription,
				
				_tourFormatter_Altitude,
				_tourFormatter_Distance,
				
				_tourFormatter_Speed,
				_tourFormatter_Pace,
				
				_tourFormatter_Time_Recording,
				_tourFormatter_Time_Moving,
				_tourFormatter_Time_Paused,
		};
		
		// Week
		_weekFormatter_Altitude 		= createFormatter_Altitude();
		_weekFormatter_Distance 		= createFormatter_Distance();
		
		_weekFormatter_Pace 			= createFormatter_Pace();
		_weekFormatter_Speed 			= createFormatter_Speed();
		
		_weekFormatter_Time_Moving 		= createFormatter_Time_Moving();
		_weekFormatter_Time_Paused 		= createFormatter_Time_Paused();
		_weekFormatter_Time_Recording 	= createFormatter_Time_Recording();

		allWeekFormatter = new DataFormatter[] {
				
				DEFAULT_EMPTY_FORMATTER,
				
				_weekFormatter_Altitude,
				_weekFormatter_Distance,
				
				_weekFormatter_Speed,
				_weekFormatter_Pace,
				
				_weekFormatter_Time_Recording,
				_weekFormatter_Time_Moving,
				_weekFormatter_Time_Paused,
		};

		DEFAULT_TOUR_FORMATTER_DATA = new FormatterData[] {

			new FormatterData(true,		FormatterID.TOUR_TITLE,			_tourFormatter_TourTitle.getDefaultFormat()),
			new FormatterData(true,		FormatterID.TOUR_DESCRIPTION,	_tourFormatter_TourDescription.getDefaultFormat()),
			new FormatterData(true,		FormatterID.ALTITUDE,			_tourFormatter_Altitude.getDefaultFormat()),
			new FormatterData(true,		FormatterID.DISTANCE,			_tourFormatter_Distance.getDefaultFormat()),
			new FormatterData(true,		FormatterID.TIME_MOVING,		_tourFormatter_Time_Moving.getDefaultFormat()),
			new FormatterData(false,	FormatterID.EMPTY,				ValueFormat.DUMMY_VALUE),
			new FormatterData(false,	FormatterID.EMPTY,				ValueFormat.DUMMY_VALUE),
			new FormatterData(false,	FormatterID.EMPTY,				ValueFormat.DUMMY_VALUE),
		};
		
		NUM_DEFAULT_TOUR_FORMATTER = DEFAULT_TOUR_FORMATTER_DATA.length;

		DEFAULT_WEEK_FORMATTER_DATA = new FormatterData[] {
				
			new FormatterData(true,		FormatterID.ALTITUDE,			_weekFormatter_Altitude.getDefaultFormat()),
			new FormatterData(true,		FormatterID.DISTANCE,			_weekFormatter_Distance.getDefaultFormat()),
			new FormatterData(true,		FormatterID.SPEED,				_weekFormatter_Speed.getDefaultFormat()),
			new FormatterData(true,		FormatterID.PACE,				_weekFormatter_Pace.getDefaultFormat()),
			new FormatterData(true,		FormatterID.TIME_MOVING,		_weekFormatter_Time_Moving.getDefaultFormat()),
			new FormatterData(false,	FormatterID.EMPTY,				ValueFormat.DUMMY_VALUE),
		};
		
		NUM_DEFAULT_WEEK_FORMATTER = DEFAULT_WEEK_FORMATTER_DATA.length;
	}
	//
	//
	private static final CalendarColor_ComboData[] _allCalendarColor_ComboData =

		new CalendarColor_ComboData[] {

			new CalendarColor_ComboData(CalendarColor.BRIGHT,	Messages.Calendar_Profile_Color_Bright),
			new CalendarColor_ComboData(CalendarColor.DARK,		Messages.Calendar_Profile_Color_Dark),
			new CalendarColor_ComboData(CalendarColor.LINE,		Messages.Calendar_Profile_Color_Line),
			new CalendarColor_ComboData(CalendarColor.TEXT,		Messages.Calendar_Profile_Color_Text),
			new CalendarColor_ComboData(CalendarColor.BLACK,	Messages.Calendar_Profile_Color_Black),
			new CalendarColor_ComboData(CalendarColor.WHITE,	Messages.Calendar_Profile_Color_White),
		};

	private static final DateColumn_ComboData[] _allDateColumn_ComboData =

		new DateColumn_ComboData[] {

			new DateColumn_ComboData(DateColumnContent.WEEK_NUMBER,	Messages.Calendar_Profile_DateColumn_WeekNumber),
			new DateColumn_ComboData(DateColumnContent.MONTH, 		Messages.Calendar_Profile_DateColumn_Month),
			new DateColumn_ComboData(DateColumnContent.YEAR, 		Messages.Calendar_Profile_DateColumn_Year),
		};

	private static final ColumnLayout_ComboData[] _allColumnLayout_ComboData =

		new ColumnLayout_ComboData[] {

			new ColumnLayout_ComboData(ColumnStart.CONTINUOUSLY, Messages.Calendar_Profile_ColumnLayout_Continuously),

			new ColumnLayout_ComboData(ColumnStart.JAN, TimeTools.month_Full[0]),
			new ColumnLayout_ComboData(ColumnStart.FEB, TimeTools.month_Full[1]),
			new ColumnLayout_ComboData(ColumnStart.MAR, TimeTools.month_Full[2]),
			new ColumnLayout_ComboData(ColumnStart.APR, TimeTools.month_Full[3]),
			new ColumnLayout_ComboData(ColumnStart.MAY, TimeTools.month_Full[4]),
			new ColumnLayout_ComboData(ColumnStart.JUN, TimeTools.month_Full[5]),
			new ColumnLayout_ComboData(ColumnStart.JUL, TimeTools.month_Full[6]),
			new ColumnLayout_ComboData(ColumnStart.AUG, TimeTools.month_Full[7]),
			new ColumnLayout_ComboData(ColumnStart.SEP, TimeTools.month_Full[8]),
			new ColumnLayout_ComboData(ColumnStart.OCT, TimeTools.month_Full[9]),
			new ColumnLayout_ComboData(ColumnStart.NOV, TimeTools.month_Full[10]),
			new ColumnLayout_ComboData(ColumnStart.DEC, TimeTools.month_Full[11]),

			// repeat continuously -> is more handier
			new ColumnLayout_ComboData(ColumnStart.CONTINUOUSLY, Messages.Calendar_Profile_ColumnLayout_Continuously),
		};

	private static final DayHeaderDateFormat_ComboData[] _allDateHeaderDateFormat_ComboData =

		new DayHeaderDateFormat_ComboData[] {

			new DayHeaderDateFormat_ComboData(DayDateFormat.DAY,
					NLS.bind(
							Messages.Calendar_Profile_DayHeaderDateFormat_Day,
							TimeTools.Formatter_Day.format(LocalDate.now()))),

			new DayHeaderDateFormat_ComboData(DayDateFormat.DAY_MONTH,				TimeTools.Formatter_DayMonth.format(LocalDate.now())),
			new DayHeaderDateFormat_ComboData(DayDateFormat.DAY_MONTH_YEAR,			TimeTools.Formatter_DayMonthYear.format(LocalDate.now())),
			new DayHeaderDateFormat_ComboData(DayDateFormat.AUTOMATIC,				Messages.Calendar_Profile_DayHeaderDateFormat_Automatic),
		};

	private static final TourBackground_ComboData[] _allTourBackground_ComboData =

		new TourBackground_ComboData[] {

			new TourBackground_ComboData(TourBackground.NO_BACKGROUND,		Messages.Calendar_Profile_TourBackground_NoBackground,
					false,
					false,
					false),

			new TourBackground_ComboData(TourBackground.FILL,				Messages.Calendar_Profile_TourBackground_Fill,
					true,
					false,
					false),

			new TourBackground_ComboData(TourBackground.FILL_LEFT,			Messages.Calendar_Profile_TourBackground_Fill_Left,
					true,
					false,
					true),

			new TourBackground_ComboData(TourBackground.FILL_RIGHT,			Messages.Calendar_Profile_TourBackground_Fill_Right,
					true,
					false,
					true),

			new TourBackground_ComboData(TourBackground.CIRCLE,				Messages.Calendar_Profile_TourBackground_Circle,
					true,
					false,
					false),

			new TourBackground_ComboData(TourBackground.GRADIENT_HORIZONTAL,	Messages.Calendar_Profile_TourBackground_GradientHorizontal,
					true,
					true,
					false),

			new TourBackground_ComboData(TourBackground.GRADIENT_VERTICAL,		Messages.Calendar_Profile_TourBackground_GradientVertical,
					true,
					true,
					false),
		};

	private static final TourBorder_ComboData[] _allTourBorder_ComboData =

		new TourBorder_ComboData[] {

			new TourBorder_ComboData(TourBorder.NO_BORDER,				Messages.Calendar_Profile_TourBorder_NoBorder,
					false,
					false),

			new TourBorder_ComboData(TourBorder.BORDER_ALL,				Messages.Calendar_Profile_TourBorder_All,
					true,
					true),

			new TourBorder_ComboData(TourBorder.BORDER_TOP,				Messages.Calendar_Profile_TourBorder_Top,
					true,
					true),

			new TourBorder_ComboData(TourBorder.BORDER_BOTTOM,			Messages.Calendar_Profile_TourBorder_Bottom,
					true,
					true),

			new TourBorder_ComboData(TourBorder.BORDER_TOP_BOTTOM,		Messages.Calendar_Profile_TourBorder_TopBottom,
					true,
					true),

			new TourBorder_ComboData(TourBorder.BORDER_LEFT,			Messages.Calendar_Profile_TourBorder_Left,
					true,
					true),

			new TourBorder_ComboData(TourBorder.BORDER_RIGHT,			Messages.Calendar_Profile_TourBorder_Right,
					true,
					true),

			new TourBorder_ComboData(TourBorder.BORDER_LEFT_RIGHT,		Messages.Calendar_Profile_TourBorder_LeftRight,
					true,
					true),
		};

	private static final DayContentColor_ComboData[] _allTourContentColor_ComboData =

		new DayContentColor_ComboData[] {

			new DayContentColor_ComboData(CalendarColor.CONTRAST, Messages.Calendar_Profile_Color_Contrast),
			new DayContentColor_ComboData(CalendarColor.BRIGHT, Messages.Calendar_Profile_Color_Bright),
			new DayContentColor_ComboData(CalendarColor.DARK, Messages.Calendar_Profile_Color_Dark),
			new DayContentColor_ComboData(CalendarColor.LINE, Messages.Calendar_Profile_Color_Line),
			new DayContentColor_ComboData(CalendarColor.TEXT, Messages.Calendar_Profile_Color_Text),
			new DayContentColor_ComboData(CalendarColor.BLACK, Messages.Calendar_Profile_Color_Black),
			new DayContentColor_ComboData(CalendarColor.WHITE, Messages.Calendar_Profile_Color_White),
		};
	//
// SET_FORMATTING_ON
	//
	/**
	 * Contains all calendar profiles which are loaded from a xml file.
	 */
	private static final ArrayList<CalendarProfile>			_allCalendarProfiles				= new ArrayList<>();
	private static CalendarProfile							_activeCalendarProfile;
	//
	private static String									_fromXml_ActiveCalendarProfileId;
	//
	private final static ListenerList						_profileListener					= new ListenerList();

	public static class CalendarColor_ComboData {

		String			label;
		CalendarColor	color;

		public CalendarColor_ComboData(final CalendarColor color, final String label) {

			this.color = color;
			this.label = label;
		}

	}

	static class ColumnLayout_ComboData {

		String		label;
		ColumnStart	columnLayout;

		public ColumnLayout_ComboData(final ColumnStart columnLayout, final String label) {

			this.columnLayout = columnLayout;
			this.label = label;
		}
	}

	static class DateColumn_ComboData {

		String				label;
		DateColumnContent	dateColumn;

		public DateColumn_ComboData(final DateColumnContent dateColumn, final String label) {

			this.dateColumn = dateColumn;
			this.label = label;
		}
	}

	static class DayContentColor_ComboData {

		String			label;
		CalendarColor	dayContentColor;

		DayContentColor_ComboData(final CalendarColor dayContentColor, final String label) {

			this.label = label;
			this.dayContentColor = dayContentColor;
		}

	}

	static class DayHeaderDateFormat_ComboData {

		String			label;
		DayDateFormat	dayHeaderDateFormat;

		public DayHeaderDateFormat_ComboData(final DayDateFormat dayHeaderDateFormat, final String label) {

			this.dayHeaderDateFormat = dayHeaderDateFormat;
			this.label = label;
		}
	}

	interface ICalendarProfileListener {

		/**
		 * Calendar profile has changed, update the UI.
		 */
		abstract void profileIsModified();
	}

	static class TourBackground_ComboData {

		TourBackground	tourBackground;
		String			label;

		boolean			isWidth;
		boolean			isColor1;
		boolean			isColor2;

		public TourBackground_ComboData(final TourBackground tourBackground,
										final String label,
										final boolean isColor1,
										final boolean isColor2,
										final boolean isWidth) {

			this.tourBackground = tourBackground;
			this.label = label;

			this.isWidth = isWidth;
			this.isColor1 = isColor1;
			this.isColor2 = isColor2;
		}
	}

	static class TourBorder_ComboData {

		TourBorder	tourBorder;
		String		label;

		boolean		isColor;
		boolean		isWidth;

		public TourBorder_ComboData(final TourBorder tourBorder,
									final String label,
									final boolean isColor,
									final boolean isWidth) {

			this.tourBorder = tourBorder;
			this.label = label;
			this.isColor = isColor;
			this.isWidth = isWidth;
		}
	}

	static void addProfileListener(final ICalendarProfileListener profileListener) {
		_profileListener.add(profileListener);
	}

	private static XMLMemento create_Root() {

		final XMLMemento xmlRoot = XMLMemento.createWriteRoot(TAG_ROOT);

		// date/time
		xmlRoot.putString(Util.ATTR_ROOT_DATETIME, TimeTools.now().toString());

		// plugin version
		final Version version = _bundle.getVersion();
		xmlRoot.putInteger(Util.ATTR_ROOT_VERSION_MAJOR, version.getMajor());
		xmlRoot.putInteger(Util.ATTR_ROOT_VERSION_MINOR, version.getMinor());
		xmlRoot.putInteger(Util.ATTR_ROOT_VERSION_MICRO, version.getMicro());
		xmlRoot.putString(Util.ATTR_ROOT_VERSION_QUALIFIER, version.getQualifier());

		// profile version
		xmlRoot.putInteger(ATTR_PROFILE_VERSION, PROFILE_VERSION);

		return xmlRoot;
	}

	/**
	 * Altitude
	 * 
	 * @return
	 */
	private static DataFormatter createFormatter_Altitude() {

		final DataFormatter dataFormatter = new DataFormatter(
				FormatterID.ALTITUDE,
				Messages.Calendar_Profile_Value_Altitude,
				GraphColorManager.PREF_GRAPH_ALTITUDE) {

			@Override
			String format(final CalendarTourData data, final ValueFormat valueFormat, final boolean isShowValueUnit) {

				if (data.altitude > 0) {

					final float altitude = data.altitude / net.tourbook.ui.UI.UNIT_VALUE_ALTITUDE;
					final String valueText = valueFormatter.printDouble(altitude);

					return isShowValueUnit
							? valueText + UI.SPACE + UI.UNIT_LABEL_ALTITUDE + UI.SPACE
							: valueText + UI.SPACE;

				} else {
					return UI.EMPTY_STRING;
				}
			}

			@Override
			public ValueFormat getDefaultFormat() {
				return ValueFormat.NUMBER_1_0;
			}

			@Override
			public ValueFormat[] getValueFormats() {

				return new ValueFormat[] {

						ValueFormat.NUMBER_1_0,
						ValueFormat.NUMBER_1_1 };
			}

			@Override
			void setValueFormat(final ValueFormat valueFormat) {

				valueFormatter = getFormatter_Number(valueFormat.name());
			}
		};

		// setup default formatter
		dataFormatter.setValueFormat(dataFormatter.getDefaultFormat());

		return dataFormatter;
	}

	/**
	 * Distance
	 * 
	 * @return
	 */
	private static DataFormatter createFormatter_Distance() {

		final DataFormatter dataFormatter = new DataFormatter(
				FormatterID.DISTANCE,
				Messages.Calendar_Profile_Value_Distance,
				GraphColorManager.PREF_GRAPH_DISTANCE) {

			@Override
			String format(final CalendarTourData data, final ValueFormat valueFormat, final boolean isShowValueUnit) {

				if (data.distance > 0) {

					final double distance = data.distance / 1000.0 / net.tourbook.ui.UI.UNIT_VALUE_DISTANCE;

					final String valueText = valueFormatter.printDouble(distance);

					return isShowValueUnit
							? valueText + UI.SPACE + UI.UNIT_LABEL_DISTANCE + UI.SPACE
							: valueText + UI.SPACE;

				} else {
					return UI.EMPTY_STRING;
				}
			}

			@Override
			public ValueFormat getDefaultFormat() {
				return ValueFormat.NUMBER_1_0;
			}

			@Override
			public ValueFormat[] getValueFormats() {

				return new ValueFormat[] {
						ValueFormat.NUMBER_1_0,
						ValueFormat.NUMBER_1_1,
						ValueFormat.NUMBER_1_2,
						ValueFormat.NUMBER_1_3 };
			}

			@Override
			void setValueFormat(final ValueFormat valueFormat) {

				valueFormatter = getFormatter_Number(valueFormat.name());
			}
		};

		// setup default formatter
		dataFormatter.setValueFormat(dataFormatter.getDefaultFormat());

		return dataFormatter;
	}

	/**
	 * Empty
	 * 
	 * @return
	 */
	private static DataFormatter createFormatter_Empty() {

		final DataFormatter dataFormatter = new DataFormatter(FormatterID.EMPTY) {

			@Override
			String format(final CalendarTourData data, final ValueFormat valueFormat, final boolean isShowValueUnit) {
				return UI.EMPTY_STRING;
			}

			@Override
			public ValueFormat getDefaultFormat() {
				return null;
			}

			@Override
			public String getText() {
				return Messages.Calendar_Profile_Value_ShowNothing;
			}

			@Override
			public ValueFormat[] getValueFormats() {
				return null;
			}

			@Override
			void setValueFormat(final ValueFormat valueFormat) {}
		};

		return dataFormatter;
	}

	/**
	 * Pace
	 * 
	 * @return
	 */
	private static DataFormatter createFormatter_Pace() {

		final DataFormatter dataFormatter = new DataFormatter(
				FormatterID.PACE,
				Messages.Calendar_Profile_Value_Pace,
				GraphColorManager.PREF_GRAPH_PACE) {

			@Override
			String format(final CalendarTourData data, final ValueFormat valueFormat, final boolean isShowValueUnit) {

				if (data.recordingTime > 0 && data.distance > 0) {

					final float pace = data.distance == 0
							? 0
							: 1000 * data.recordingTime / data.distance * net.tourbook.ui.UI.UNIT_VALUE_DISTANCE;

					final String valueText = UI.format_mm_ss((long) pace);

					return isShowValueUnit //
							? valueText + UI.SPACE + UI.UNIT_LABEL_PACE + UI.SPACE
							: valueText + UI.SPACE;

				} else {
					return UI.EMPTY_STRING;
				}
			}

			@Override
			public ValueFormat getDefaultFormat() {
				return ValueFormat.PACE_MM_SS;
			}

			@Override
			public ValueFormat[] getValueFormats() {

				return new ValueFormat[] { ValueFormat.PACE_MM_SS };
			}

			@Override
			void setValueFormat(final ValueFormat valueFormat) {}
		};

		return dataFormatter;
	}

	/**
	 * Speed
	 * 
	 * @return
	 */
	private static DataFormatter createFormatter_Speed() {

		final DataFormatter dataFormatter = new DataFormatter(
				FormatterID.SPEED,
				Messages.Calendar_Profile_Value_Speed,
				GraphColorManager.PREF_GRAPH_SPEED) {

			@Override
			String format(final CalendarTourData data, final ValueFormat valueFormat, final boolean isShowValueUnit) {

				if (data.distance > 0 && data.recordingTime > 0) {

					final float speed = data.distance == 0
							? 0
							: data.distance / (data.recordingTime / 3.6f);

					final String valueText = valueFormatter.printDouble(speed);

					return isShowValueUnit
							? valueText + UI.SPACE + UI.UNIT_LABEL_SPEED + UI.SPACE
							: valueText + UI.SPACE;
				} else {

					return UI.EMPTY_STRING;
				}
			}

			@Override
			public ValueFormat getDefaultFormat() {
				return ValueFormat.NUMBER_1_0;
			}

			@Override
			public ValueFormat[] getValueFormats() {

				return new ValueFormat[] {
						ValueFormat.NUMBER_1_0,
						ValueFormat.NUMBER_1_1,
						ValueFormat.NUMBER_1_2 };
			}

			@Override
			void setValueFormat(final ValueFormat valueFormat) {

				valueFormatter = getFormatter_Number(valueFormat.name());
			}
		};

		// setup default formatter
		dataFormatter.setValueFormat(dataFormatter.getDefaultFormat());

		return dataFormatter;
	}

	/**
	 * Moving time
	 * 
	 * @return
	 */
	private static DataFormatter createFormatter_Time_Moving() {

		final DataFormatter dataFormatter = new DataFormatter(
				FormatterID.TIME_MOVING,
				Messages.Calendar_Profile_Value_MovingTime,
				GraphColorManager.PREF_GRAPH_TIME) {

			@Override
			String format(final CalendarTourData data, final ValueFormat valueFormat, final boolean isShowValueUnit) {

				if (data.recordingTime > 0) {

					final String valueText = valueFormatter.printLong(data.drivingTime);

					return isShowValueUnit
							? valueText + UI.SPACE + UI.UNIT_LABEL_TIME + UI.SPACE
							: valueText + UI.SPACE;

				} else {
					return UI.EMPTY_STRING;
				}
			}

			@Override
			public ValueFormat getDefaultFormat() {
				return ValueFormat.TIME_HH_MM;
			}

			@Override
			public ValueFormat[] getValueFormats() {

				return new ValueFormat[] {
						ValueFormat.TIME_HH,
						ValueFormat.TIME_HH_MM,
						ValueFormat.TIME_HH_MM_SS };
			}

			@Override
			void setValueFormat(final ValueFormat valueFormat) {

				valueFormatter = getFormatter_Time(valueFormat.name());
			}
		};

		// setup default formatter
		dataFormatter.setValueFormat(dataFormatter.getDefaultFormat());

		return dataFormatter;
	}

	/**
	 * Paused time
	 * 
	 * @return
	 */
	private static DataFormatter createFormatter_Time_Paused() {

		final DataFormatter dataFormatter = new DataFormatter(
				FormatterID.TIME_PAUSED,
				Messages.Calendar_Profile_Value_PausedTime,
				GraphColorManager.PREF_GRAPH_TIME) {

			@Override
			String format(final CalendarTourData data, final ValueFormat valueFormat, final boolean isShowValueUnit) {

				if (data.recordingTime > 0) {

					final String valueText = valueFormatter.printLong(data.recordingTime - data.drivingTime);

					return isShowValueUnit
							? valueText + UI.SPACE + UI.UNIT_LABEL_TIME + UI.SPACE
							: valueText + UI.SPACE;

				} else {
					return UI.EMPTY_STRING;
				}
			}

			@Override
			public ValueFormat getDefaultFormat() {
				return ValueFormat.TIME_HH_MM;
			}

			@Override
			public ValueFormat[] getValueFormats() {

				return new ValueFormat[] {
						ValueFormat.TIME_HH,
						ValueFormat.TIME_HH_MM,
						ValueFormat.TIME_HH_MM_SS };
			}

			@Override
			void setValueFormat(final ValueFormat valueFormat) {

				valueFormatter = getFormatter_Time(valueFormat.name());
			}
		};

		// setup default formatter
		dataFormatter.setValueFormat(dataFormatter.getDefaultFormat());

		return dataFormatter;
	}

	/**
	 * Recording time
	 * 
	 * @return
	 */
	private static DataFormatter createFormatter_Time_Recording() {

		final DataFormatter dataFormatter = new DataFormatter(
				FormatterID.TIME_RECORDING,
				Messages.Calendar_Profile_Value_RecordingTime,
				GraphColorManager.PREF_GRAPH_TIME) {

			@Override
			String format(final CalendarTourData data, final ValueFormat valueFormat, final boolean isShowValueUnit) {

				if (data.recordingTime > 0) {

					final String valueText = valueFormatter.printLong(data.recordingTime);

					return isShowValueUnit
							? valueText + UI.SPACE + UI.UNIT_LABEL_TIME + UI.SPACE
							: valueText + UI.SPACE;

				} else {

					return UI.EMPTY_STRING;
				}
			}

			@Override
			public ValueFormat getDefaultFormat() {
				return ValueFormat.TIME_HH_MM;
			}

			@Override
			public ValueFormat[] getValueFormats() {

				return new ValueFormat[] {
						ValueFormat.TIME_HH,
						ValueFormat.TIME_HH_MM,
						ValueFormat.TIME_HH_MM_SS };
			}

			@Override
			void setValueFormat(final ValueFormat valueFormat) {

				valueFormatter = getFormatter_Time(valueFormat.name());
			}
		};

		// setup default formatter
		dataFormatter.setValueFormat(dataFormatter.getDefaultFormat());

		return dataFormatter;
	}

	/**
	 * Description
	 * 
	 * @return
	 */
	private static DataFormatter createFormatter_TourDescription() {

		final DataFormatter dataFormatter = new DataFormatter(
				FormatterID.TOUR_DESCRIPTION,
				Messages.Calendar_Profile_Value_Description,
				UI.EMPTY_STRING) {

			@Override
			String format(final CalendarTourData data, final ValueFormat valueFormat, final boolean isShowValueUnit) {

				return data.tourDescription;
			}

			@Override
			public ValueFormat getDefaultFormat() {
				return ValueFormat.TEXT;
			}

			@Override
			public ValueFormat[] getValueFormats() {

				return new ValueFormat[] { ValueFormat.TEXT };
			}

			@Override
			void setValueFormat(final ValueFormat valueFormat) {}
		};

		// setup default formatter
		dataFormatter.setValueFormat(dataFormatter.getDefaultFormat());

		return dataFormatter;
	}

	/**
	 * Title
	 * 
	 * @return
	 */
	private static DataFormatter createFormatter_TourTitle() {

		final DataFormatter dataFormatter = new DataFormatter(
				FormatterID.TOUR_TITLE,
				Messages.Calendar_Profile_Value_Title,
				UI.EMPTY_STRING) {

			@Override
			String format(final CalendarTourData data, final ValueFormat valueFormat, final boolean isShowValueUnit) {

				return data.tourTitle;
			}

			@Override
			public ValueFormat getDefaultFormat() {
				return ValueFormat.TEXT;
			}

			@Override
			public ValueFormat[] getValueFormats() {
				return new ValueFormat[] { ValueFormat.TEXT };
			}

			@Override
			void setValueFormat(final ValueFormat valueFormat) {}
		};

		// setup default formatter
		dataFormatter.setValueFormat(dataFormatter.getDefaultFormat());

		return dataFormatter;
	}

	private static void createProfile_All() {

		_allCalendarProfiles.clear();

		// add default default
		_allCalendarProfiles.add(new CalendarProfile());

		_allCalendarProfiles.add(createProfile_Classic());

		_allCalendarProfiles.add(createProfile_Col_01());
		_allCalendarProfiles.add(createProfile_Col_01_Dark());
		_allCalendarProfiles.add(createProfile_Col_03());
		_allCalendarProfiles.add(createProfile_Col_03_Dark());
		_allCalendarProfiles.add(createProfile_Col_10());
		_allCalendarProfiles.add(createProfile_Col_10_Dark());
		_allCalendarProfiles.add(createProfile_Col_20());
		_allCalendarProfiles.add(createProfile_Col_20_Dark());

		_allCalendarProfiles.add(createProfile_Crazy());
	}

	private static CalendarProfile createProfile_Classic() {

		final CalendarProfile profile = new CalendarProfile();

		profile.name = Messages.Calendar_Profile_Name_Classic;

		// SET_FORMATTING_OFF

//		                                     Classic

		profile.defaultId                     = ProfileDefault.CLASSIC;

		// layout
		profile.isToggleMonthColor            = true;
		profile.useDraggedScrolling           = false;
		profile.alternateMonthRGB             = new RGB (240, 240, 240);
		profile.calendarBackgroundRGB         = new RGB (255, 255, 255);
		profile.calendarForegroundRGB         = new RGB (0, 0, 0);
		profile.weekHeight                    = 150;
		                                                                                         
		// year columns
		profile.isShowYearColumns             = false;
		profile.yearColumns                   = 2;
		profile.yearColumnsSpacing            = 30;
		profile.yearColumnsStart              = ColumnStart.CONTINUOUSLY;
		profile.yearHeaderFont                = CalendarProfile.createFont(2.2f, SWT.BOLD);
		                                                                                         
		// date column
		profile.isShowDateColumn              = true;
		profile.dateColumnContent             = DateColumnContent.WEEK_NUMBER;
		profile.dateColumnFont                = CalendarProfile.createFont(1.5f, SWT.BOLD);
		profile.dateColumnWidth               = 50;
		                                                                                         
		// day date
		profile.isHideDayDateWhenNoTour       = false;
		profile.isShowDayDate                 = true;
		profile.isShowDayDateWeekendColor     = true;
		profile.dayDateFont                   = CalendarProfile.createFont(1.2f, SWT.NORMAL);
		profile.dayDateFormat                 = DayDateFormat.DAY_MONTH_YEAR;
		                                                                                         
		// tour background
		profile.tourBackground                = TourBackground.FILL;
		profile.tourBackgroundColor1          = CalendarColor.DARK;
		profile.tourBackgroundColor2          = CalendarColor.BRIGHT;
		profile.tourBackgroundWidth           = 3;
		profile.tourBorder                    = TourBorder.NO_BORDER;
		profile.tourBorderColor               = CalendarColor.LINE;
		profile.tourBorderWidth               = 1;
		                                                                                         
		// tour content
		profile.isShowTourContent             = true;
		profile.isShowTourValueUnit           = true;
		profile.isTruncateTourText            = true;
		profile.tourContentColor              = CalendarColor.CONTRAST;
		profile.tourContentFont               = CalendarProfile.createFont(0.9f, SWT.NORMAL);
		profile.tourTitleColor                = CalendarColor.CONTRAST;
		profile.tourTitleFont                 = CalendarProfile.createFont(1.3f, SWT.BOLD);
		profile.tourTruncatedLines            = 2;
		profile.tourValueColor                = CalendarColor.CONTRAST;
		profile.tourValueColumns              = 2;
		profile.tourValueFont                 = CalendarProfile.createFont(1.2f, SWT.BOLD);
		                                                                                         
		// week summary column
		profile.isShowSummaryColumn           = true;
		profile.isShowWeekValueUnit           = true;
		profile.weekColumnWidth               = 100;
		profile.weekValueColor                = CalendarColor.TEXT;
		profile.weekValueFont                 = CalendarProfile.createFont(1.2f, SWT.BOLD);

		// SET_FORMATTING_ON

		return profile;
	}

	private static CalendarProfile createProfile_Col_01() {

		final CalendarProfile profile = new CalendarProfile();

// SET_FORMATTING_OFF
		
		profile.defaultId	= ProfileDefault.COL_1;
		profile.name			= Messages.Calendar_Profile_Name_Col_01;
		
// SET_FORMATTING_ON

		return profile;
	}

	private static CalendarProfile createProfile_Col_01_Dark() {

		final CalendarProfile profile = new CalendarProfile();

		profile.name = Messages.Calendar_Profile_Name_Col_01_Dark;

		// SET_FORMATTING_OFF

//		                                     1 Column

		profile.defaultId                     = ProfileDefault.COL_1;

		// layout
		profile.isToggleMonthColor            = false;
		profile.useDraggedScrolling           = false;
		profile.alternateMonthRGB             = new RGB (240, 240, 240);
		profile.calendarBackgroundRGB         = new RGB (59, 59, 59);
		profile.calendarForegroundRGB         = new RGB (197, 197, 197);
		profile.weekHeight                    = 40;
		                                                                                         
		// year columns
		profile.isShowYearColumns             = true;
		profile.yearColumns                   = 1;
		profile.yearColumnsSpacing            = 30;
		profile.yearColumnsStart              = ColumnStart.CONTINUOUSLY;
		profile.yearHeaderFont                = CalendarProfile.createFont(2.8f, SWT.BOLD);
		                                                                                         
		// date column
		profile.isShowDateColumn              = true;
		profile.dateColumnContent             = DateColumnContent.MONTH;
		profile.dateColumnFont                = CalendarProfile.createFont(1.7f, SWT.BOLD);
		profile.dateColumnWidth               = 50;
		                                                                                         
		// day date
		profile.isHideDayDateWhenNoTour       = true;
		profile.isShowDayDate                 = false;
		profile.isShowDayDateWeekendColor     = false;
		profile.dayDateFont                   = CalendarProfile.createFont(1.2f, SWT.BOLD);
		profile.dayDateFormat                 = DayDateFormat.DAY;
		                                                                                         
		// tour background
		profile.tourBackground                = TourBackground.FILL;
		profile.tourBackgroundColor1          = CalendarColor.DARK;
		profile.tourBackgroundColor2          = CalendarColor.BRIGHT;
		profile.tourBackgroundWidth           = 3;
		profile.tourBorder                    = TourBorder.NO_BORDER;
		profile.tourBorderColor               = CalendarColor.LINE;
		profile.tourBorderWidth               = 1;
		                                                                                         
		// tour content
		profile.isShowTourContent             = true;
		profile.isShowTourValueUnit           = true;
		profile.isTruncateTourText            = true;
		profile.tourContentColor              = CalendarColor.CONTRAST;
		profile.tourContentFont               = CalendarProfile.createFont(0.9f, SWT.NORMAL);
		profile.tourTitleColor                = CalendarColor.CONTRAST;
		profile.tourTitleFont                 = CalendarProfile.createFont(1.2f, SWT.BOLD);
		profile.tourTruncatedLines            = 1;
		profile.tourValueColor                = CalendarColor.CONTRAST;
		profile.tourValueColumns              = 3;
		profile.tourValueFont                 = CalendarProfile.createFont(1.0f, SWT.BOLD);
		                                                                                         
		// week summary column
		profile.isShowSummaryColumn           = true;
		profile.isShowWeekValueUnit           = true;
		profile.weekColumnWidth               = 100;
		profile.weekValueColor                = CalendarColor.BRIGHT;
		profile.weekValueFont                 = CalendarProfile.createFont(1.0f, SWT.BOLD);

		// SET_FORMATTING_ON

		return profile;
	}

	private static CalendarProfile createProfile_Col_03() {

		final CalendarProfile profile = new CalendarProfile();

// SET_FORMATTING_OFF
		
		profile.defaultId	= ProfileDefault.COL_3;
		profile.name			= Messages.Calendar_Profile_Name_Col_03;
		
// SET_FORMATTING_ON

		return profile;
	}

	private static CalendarProfile createProfile_Col_03_Dark() {

		final CalendarProfile profile = new CalendarProfile();

// SET_FORMATTING_OFF
			
			profile.defaultId	= ProfileDefault.DARK_COL_3;
			profile.name			= Messages.Calendar_Profile_Name_Col_03_Dark;
			
// SET_FORMATTING_ON

		return profile;
	}

	private static CalendarProfile createProfile_Col_10() {

		final CalendarProfile profile = new CalendarProfile();

// SET_FORMATTING_OFF
		
		profile.defaultId	= ProfileDefault.COL_10;
		profile.name			= Messages.Calendar_Profile_Name_Col_10;
		
// SET_FORMATTING_ON

		return profile;
	}

	private static CalendarProfile createProfile_Col_10_Dark() {

		final CalendarProfile profile = new CalendarProfile();

// SET_FORMATTING_OFF
		
		profile.defaultId	= ProfileDefault.DARK_COL_10;
		profile.name			= Messages.Calendar_Profile_Name_Col_10_Dark;
		
// SET_FORMATTING_ON

		return profile;
	}

	private static CalendarProfile createProfile_Col_20() {

		final CalendarProfile profile = new CalendarProfile();

// SET_FORMATTING_OFF
		
		profile.defaultId	= ProfileDefault.COL_20;
		profile.name			= Messages.Calendar_Profile_Name_Col_20;
		
// SET_FORMATTING_ON

		return profile;
	}

	private static CalendarProfile createProfile_Col_20_Dark() {

		final CalendarProfile profile = new CalendarProfile();

// SET_FORMATTING_OFF
		 
		profile.defaultId	= ProfileDefault.DARK_COL_20;
		profile.name			= Messages.Calendar_Profile_Name_Col_20_Dark;

// SET_FORMATTING_ON

		return profile;
	}

	private static CalendarProfile createProfile_Crazy() {

		final CalendarProfile profile = new CalendarProfile();

		// SET_FORMATTING_OFF

//		                                     Crazy

		profile.defaultId                     = ProfileDefault.CRAZY;

		// layout
		profile.isToggleMonthColor            = false;
		profile.useDraggedScrolling           = false;
		profile.alternateMonthRGB             = new RGB (240, 240, 240);
		profile.calendarBackgroundRGB         = new RGB (0, 0, 0);
		profile.calendarForegroundRGB         = new RGB (114, 114, 114);
		profile.weekHeight                    = 13;
		                                                                                         
		// year columns
		profile.isShowYearColumns             = true;
		profile.yearColumns                   = 20;
		profile.yearColumnsSpacing            = 0;
		profile.yearColumnsStart              = ColumnStart.JAN;
		profile.yearHeaderFont                = CalendarProfile.createFont(1.6f, SWT.BOLD);
		                                                                                         
		// date column
		profile.isShowDateColumn              = false;
		profile.dateColumnContent             = DateColumnContent.MONTH;
		profile.dateColumnFont                = CalendarProfile.createFont(1.5f, SWT.BOLD);
		profile.dateColumnWidth               = 50;
		                                                                                         
		// day date
		profile.isHideDayDateWhenNoTour       = true;
		profile.isShowDayDate                 = false;
		profile.isShowDayDateWeekendColor     = false;
		profile.dayDateFont                   = CalendarProfile.createFont(1.2f, SWT.BOLD);
		profile.dayDateFormat                 = DayDateFormat.DAY;
		                                                                                         
		// tour background
		profile.tourBackground                = TourBackground.FILL;
		profile.tourBackgroundColor1          = CalendarColor.DARK;
		profile.tourBackgroundColor2          = CalendarColor.WHITE;
		profile.tourBackgroundWidth           = 3;
		profile.tourBorder                    = TourBorder.BORDER_ALL;
		profile.tourBorderColor               = CalendarColor.DARK;
		profile.tourBorderWidth               = 1;
		                                                                                         
		// tour content
		profile.isShowTourContent             = false;
		profile.isShowTourValueUnit           = true;
		profile.isTruncateTourText            = true;
		profile.tourContentColor              = CalendarColor.CONTRAST;
		profile.tourContentFont               = CalendarProfile.createFont(0.9f, SWT.NORMAL);
		profile.tourTitleColor                = CalendarColor.CONTRAST;
		profile.tourTitleFont                 = CalendarProfile.createFont(1.2f, SWT.BOLD);
		profile.tourTruncatedLines            = 2;
		profile.tourValueColor                = CalendarColor.CONTRAST;
		profile.tourValueColumns              = 2;
		profile.tourValueFont                 = CalendarProfile.createFont(1.0f, SWT.NORMAL);
		                                                                                         
		// week summary column
		profile.isShowSummaryColumn           = false;
		profile.isShowWeekValueUnit           = true;
		profile.weekColumnWidth               = 60;
		profile.weekValueColor                = CalendarColor.TEXT;
		profile.weekValueFont                 = CalendarProfile.createFont(1.2f, SWT.BOLD);

		// SET_FORMATTING_ON

		return profile;
	}

	private static CalendarProfile createProfile_FromId(final ProfileDefault defaultId) {

// SET_FORMATTING_OFF
		
		switch (defaultId) {
		
		case CLASSIC:		return createProfile_Classic();
		case CRAZY:			return createProfile_Crazy();
		
		case COL_1:			return createProfile_Col_01();
		case COL_3:			return createProfile_Col_03();
		case COL_10:		return createProfile_Col_10();
		case COL_20:		return createProfile_Col_20();
		
		case DARK_COL_1:	return createProfile_Col_01();
		case DARK_COL_3:	return createProfile_Col_03();
		case DARK_COL_10:	return createProfile_Col_10();
		case DARK_COL_20:	return createProfile_Col_20();

		default:
			// create default default
			return new CalendarProfile();
		}
		
// SET_FORMATTING_ON
	}

	/**
	 * Fires an event when the a tour marker is modified.
	 * 
	 * @param tourMarker
	 * @param isTourMarkerDeleted
	 */
	private static void fireProfileModifyEvent() {

		final Object[] allListener = _profileListener.getListeners();
		for (final Object listener : allListener) {
			((ICalendarProfileListener) listener).profileIsModified();
		}
	}

	static CalendarProfile getActiveCalendarProfile() {

		if (_activeCalendarProfile == null) {
			readProfileFromXml();
		}

		return _activeCalendarProfile;
	}

	/**
	 * @return Returns the index for the {@link #_activeCalendarProfile}, the index starts with 0.
	 */
	static int getActiveCalendarProfileIndex() {

		final CalendarProfile activeProfile = getActiveCalendarProfile();

		for (int profileIndex = 0; profileIndex < _allCalendarProfiles.size(); profileIndex++) {

			final CalendarProfile profile = _allCalendarProfiles.get(profileIndex);

			if (profile.equals(activeProfile)) {
				return profileIndex;
			}
		}

		// this case should not happen but ensure that a correct profile is set

		setActiveCalendarProfileIntern(_allCalendarProfiles.get(0));

		return 0;
	}

	static CalendarColor_ComboData[] getAllCalendarColor_ComboData() {
		return _allCalendarColor_ComboData;
	}

	static ArrayList<CalendarProfile> getAllCalendarProfiles() {

		// ensure profiles are loaded
		getActiveCalendarProfile();

		return _allCalendarProfiles;
	}

	static ColumnLayout_ComboData[] getAllColumnLayout_ComboData() {
		return _allColumnLayout_ComboData;
	}

	static DateColumn_ComboData[] getAllDateColumnData() {
		return _allDateColumn_ComboData;
	}

	static DayHeaderDateFormat_ComboData[] getAllDayHeaderDateFormat_ComboData() {
		return _allDateHeaderDateFormat_ComboData;
	}

	static TourBackground_ComboData[] getAllTourBackground_ComboData() {
		return _allTourBackground_ComboData;
	}

	static TourBorder_ComboData[] getAllTourBorderData() {
		return _allTourBorder_ComboData;
	}

	static DayContentColor_ComboData[] getAllTourContentColor_ComboData() {
		return _allTourContentColor_ComboData;
	}

	private static IValueFormatter getFormatter_Number(final String formatName) {

		if (formatName.equals(ValueFormat.NUMBER_1_0.name())) {

			return _valueFormatter_Number_1_0;

		} else if (formatName.equals(ValueFormat.NUMBER_1_1.name())) {

			return _valueFormatter_Number_1_1;

		} else if (formatName.equals(ValueFormat.NUMBER_1_2.name())) {

			return _valueFormatter_Number_1_2;

		} else if (formatName.equals(ValueFormat.NUMBER_1_3.name())) {

			return _valueFormatter_Number_1_3;

		} else {

			// default

			return _valueFormatter_Number_1_0;
		}
	}

	private static IValueFormatter getFormatter_Time(final String formatName) {

		if (formatName.equals(ValueFormat.TIME_HH.name())) {

			return _valueFormatter_Time_HH;

		} else if (formatName.equals(ValueFormat.TIME_HH_MM.name())) {

			return _valueFormatter_Time_HHMM;

		} else if (formatName.equals(ValueFormat.TIME_HH_MM_SS.name())) {

			return _valueFormatter_Time_HHMMSS;

		} else {

			// default

			return _valueFormatter_Time_HHMMSS;
		}
	}

	private static CalendarProfile getProfile_Calendar() {

		CalendarProfile activeProfile = null;

		if (_fromXml_ActiveCalendarProfileId != null) {

			// ensure profile id belongs to a profile which is available

			for (final CalendarProfile profile : _allCalendarProfiles) {

				if (profile.id.equals(_fromXml_ActiveCalendarProfileId)) {

					activeProfile = profile;
					break;
				}
			}
		}

		if (activeProfile == null) {

			// this case should not happen, create a profile

			StatusUtil.log("Created default profile for calendar properties");//$NON-NLS-1$

			createProfile_All();

			activeProfile = _allCalendarProfiles.get(0);
		}

		return activeProfile;
	}

	private static File getProfileXmlFile() {

		final File layerFile = _stateLocation.append(PROFILE_FILE_NAME).toFile();

		return layerFile;
	}

	private static void parse_200_Calendars(final XMLMemento xmlRoot,
											final ArrayList<CalendarProfile> allCalendarProfiles) {

		if (xmlRoot == null) {
			return;
		}

		final XMLMemento xmlCalendars = (XMLMemento) xmlRoot.getChild(TAG_CALENDAR_PROFILE);

		if (xmlCalendars == null) {
			return;
		}

		_fromXml_ActiveCalendarProfileId = Util.getXmlString(xmlCalendars, ATTR_ACTIVE_PROFILE_ID, null);

		for (final IMemento mementoProfile : xmlCalendars.getChildren()) {

			final XMLMemento xmlProfile = (XMLMemento) mementoProfile;

			try {

				final String xmlProfileType = xmlProfile.getType();

				if (xmlProfileType.equals(TAG_CALENDAR)) {

					// <Calendar>

					allCalendarProfiles.add(restoreProfile(xmlProfile));
				}

			} catch (final Exception e) {
				StatusUtil.log(Util.dumpMemento(xmlProfile), e);
			}
		}
	}

	/**
	 * Read or create profile
	 * 
	 * @return
	 */
	private static void readProfileFromXml() {

		InputStreamReader reader = null;

		try {

			XMLMemento xmlRoot = null;

			// try to get layer structure from saved xml file
			final File layerFile = getProfileXmlFile();
			final String absoluteLayerPath = layerFile.getAbsolutePath();

			final File inputFile = new File(absoluteLayerPath);
			if (inputFile.exists()) {

				try {

					reader = new InputStreamReader(new FileInputStream(inputFile), UI.UTF_8);
					xmlRoot = XMLMemento.createReadRoot(reader);

				} catch (final Exception e) {
					// ignore
				}
			}

			// parse xml
			parse_200_Calendars(xmlRoot, _allCalendarProfiles);

			// ensure profiles are created
			if (_allCalendarProfiles.size() == 0) {
				createProfile_All();
			}

			setActiveCalendarProfileIntern(getProfile_Calendar());

		} catch (final Exception e) {
			StatusUtil.log(e);
		} finally {
			Util.close(reader);
		}
	}

	static void removeProfileListener(final ICalendarProfileListener profileListener) {
		_profileListener.remove(profileListener);
	}

	static void resetActiveCalendarProfile() {

		final int activeCalendarProfileIndex = getActiveCalendarProfileIndex();

		// remove old profile
		_allCalendarProfiles.remove(_activeCalendarProfile);

		// create new profile
		final CalendarProfile newProfile = createProfile_FromId(_activeCalendarProfile.defaultId);

		// update model
		_allCalendarProfiles.add(activeCalendarProfileIndex, newProfile);
		setActiveCalendarProfileIntern(newProfile);
	}

	static void resetAllCalendarProfiles() {

		createProfile_All();

		setActiveCalendarProfileIntern(_allCalendarProfiles.get(0));
	}

	private static CalendarProfile restoreProfile(final XMLMemento xmlProfile) {

		// !!! getFontData() MUST be created for EVERY font otherwise they use all the SAME font !!!
		final Font defaultFont = JFaceResources.getFontRegistry().defaultFont();

		final CalendarProfile profile = new CalendarProfile();

// SET_FORMATTING_OFF
		
		// profile
		profile.id							= Util.getXmlString(xmlProfile,						ATTR_ID,						Long.toString(System.nanoTime()));
		profile.name						= Util.getXmlString(xmlProfile,						ATTR_PROFILE_NAME,				UI.EMPTY_STRING);
		profile.defaultId					= (ProfileDefault) Util.getXmlEnum(xmlProfile,		ATTR_PROFILE_DEFAULT_ID,		DEFAULT_PROFILE_DEFAULT_ID);
		
		// layout
		profile.isToggleMonthColor			= Util.getXmlBoolean(xmlProfile, 					ATTR_IS_TOGGLE_MONTH_COLOR,		true);
		profile.useDraggedScrolling			= Util.getXmlBoolean(xmlProfile, 					ATTR_USE_DRAGGED_SCROLLING,		true);
		profile.weekHeight					= Util.getXmlInteger(xmlProfile, 					ATTR_WEEK_HEIGHT,				DEFAULT_WEEK_HEIGHT);
		profile.alternateMonthRGB			= Util.getXmlRgb(xmlProfile, 						TAG_ALTERNATE_MONTH_RGB,		DEFAULT_ALTERNATE_MONTH_RGB);
		profile.calendarBackgroundRGB		= Util.getXmlRgb(xmlProfile, 						TAG_CALENDAR_BACKGROUND_RGB,	DEFAULT_CALENDAR_BACKGROUND_RGB);
		profile.calendarForegroundRGB		= Util.getXmlRgb(xmlProfile, 						TAG_CALENDAR_FOREGROUND_RGB,	DEFAULT_CALENDAR_FOREBACKGROUND_RGB);
		
		// year columns
		profile.isShowYearColumns			= Util.getXmlBoolean(xmlProfile, 					ATTR_IS_SHOW_YEAR_COLUMNS,		true);
		profile.yearColumns					= Util.getXmlInteger(xmlProfile,					ATTR_YEAR_COLUMNS,				DEFAULT_YEAR_COLUMNS);
		profile.yearColumnsSpacing			= Util.getXmlInteger(xmlProfile, 					ATTR_YEAR_COLUMNS_SPACING,		DEFAULT_YEAR_COLUMNS_SPACING);
		profile.yearColumnsStart			= (ColumnStart) Util.getXmlEnum(xmlProfile,			ATTR_YEAR_COLUMNS_START,		DEFAULT_YEAR_COLUMNS_LAYOUT);
		profile.yearHeaderFont				= Util.getXmlFont(xmlProfile, 						ATTR_YEAR_HEADER_FONT,			defaultFont.getFontData()[0]);
		
		// date column
		profile.isShowDateColumn			= Util.getXmlBoolean(xmlProfile, 					ATTR_IS_SHOW_DATE_COLUMN,		true);
		profile.dateColumnFont 				= Util.getXmlFont(xmlProfile, 						ATTR_DATE_COLUMN_FONT, 			defaultFont.getFontData()[0]);
		profile.dateColumnWidth				= Util.getXmlInteger(xmlProfile, 					ATTR_DATE_COLUMN_WIDTH,			DEFAULT_DATE_COLUMN_WIDTH);
		profile.dateColumnContent			= (DateColumnContent) Util.getXmlEnum(xmlProfile,	ATTR_DATE_COLUMN_CONTENT,		DateColumnContent.WEEK_NUMBER);

		// day date
		profile.isHideDayDateWhenNoTour		= Util.getXmlBoolean(xmlProfile, 					ATTR_IS_HIDE_DAY_DATE_WHEN_NO_TOUR,		true);
		profile.isShowDayDate				= Util.getXmlBoolean(xmlProfile, 					ATTR_IS_SHOW_DAY_DATE,					true);
		profile.isShowDayDateWeekendColor	= Util.getXmlBoolean(xmlProfile, 					ATTR_IS_SHOW_DAY_DATE_WEEKEND_COLOR,	DEFAULT_IS_SHOW_DAY_DATE_WEEKEND_COLOR);
		profile.dayDateFont 				= Util.getXmlFont(xmlProfile, 						ATTR_DAY_DATE_FONT, 					defaultFont.getFontData()[0]);
		profile.dayDateFormat				= (DayDateFormat) Util.getXmlEnum(xmlProfile,		ATTR_DAY_DATE_FORMAT,					DEFAULT_DAY_DATE_FORMAT);
		                                    
		// day content
		profile.tourBackgroundWidth			= Util.getXmlInteger(xmlProfile, 					ATTR_TOUR_BACKGROUND_WIDTH, 	DEFAULT_TOUR_BACKGROUND_WIDTH, 1, 100);
		profile.tourBorderWidth				= Util.getXmlInteger(xmlProfile, 					ATTR_TOUR_BORDER_WIDTH,		 	DEFAULT_TOUR_BORDER_WIDTH, 1, 100);
		profile.tourBackground 				= (TourBackground) Util.getXmlEnum(xmlProfile,		ATTR_TOUR_BACKGROUND,			DEFAULT_TOUR_BACKGROUND);
		profile.tourBackgroundColor1 		= (CalendarColor) Util.getXmlEnum(xmlProfile,		ATTR_TOUR_BACKGROUND_COLOR1,	DEFAULT_TOUR_BACKGROUND_COLOR1);
		profile.tourBackgroundColor2 		= (CalendarColor) Util.getXmlEnum(xmlProfile,		ATTR_TOUR_BACKGROUND_COLOR2,	DEFAULT_TOUR_BACKGROUND_COLOR2);
		profile.tourBorder 					= (TourBorder) Util.getXmlEnum(xmlProfile,			ATTR_TOUR_BORDER,				DEFAULT_TOUR_BORDER);
		profile.tourBorderColor 			= (CalendarColor) Util.getXmlEnum(xmlProfile,		ATTR_TOUR_BORDER_COLOR,			DEFAULT_TOUR_BORDER_COLOR);
		
		// tour content
		profile.isShowTourContent			= Util.getXmlBoolean(xmlProfile, 					ATTR_IS_SHOW_TOUR_CONTENT,		true);
		profile.isShowTourValueUnit			= Util.getXmlBoolean(xmlProfile, 					ATTR_IS_SHOW_TOUR_VALUE_UNIT,	true);
		profile.isTruncateTourText			= Util.getXmlBoolean(xmlProfile, 					ATTR_IS_TRUNCATE_TOUR_TEXT,		DEFAULT_IS_TRUNCATE_TOUR_TEXT);
		profile.tourContentColor			= (CalendarColor) Util.getXmlEnum(xmlProfile,		ATTR_TOUR_CONTENT_COLOR,		DEFAULT_TOUR_COLOR);
		profile.tourContentFont 			= Util.getXmlFont(xmlProfile, 						ATTR_TOUR_CONTENT_FONT, 		defaultFont.getFontData()[0]);
		profile.tourTitleColor				= (CalendarColor) Util.getXmlEnum(xmlProfile,		ATTR_TOUR_TITLE_COLOR,			DEFAULT_TOUR_COLOR);
		profile.tourTitleFont 				= Util.getXmlFont(xmlProfile, 						ATTR_TOUR_TITLE_FONT, 			defaultFont.getFontData()[0]);
		profile.tourTruncatedLines			= Util.getXmlInteger(xmlProfile, 					ATTR_TOUR_TRUNCATED_LINES,	 	DEFAULT_TOUR_TRUNCATED_LINES, 1, 10);
		profile.tourValueColumns			= Util.getXmlInteger(xmlProfile, 					ATTR_TOUR_VALUE_COLUMNS,	 	DEFAULT_TOUR_VALUE_COLUMNS, 1, 3);
		profile.tourValueFont				= Util.getXmlFont(xmlProfile, 						ATTR_TOUR_VALUE_FONT, 			defaultFont.getFontData()[0]);
		profile.tourValueColor				= (CalendarColor) Util.getXmlEnum(xmlProfile,		ATTR_TOUR_VALUE_COLOR,			DEFAULT_TOUR_COLOR);
		
		// week summary column
		profile.isShowSummaryColumn			= Util.getXmlBoolean(xmlProfile, 					ATTR_IS_SHOW_SUMMARY_COLUMN,	true);
		profile.isShowWeekValueUnit			= Util.getXmlBoolean(xmlProfile, 					ATTR_IS_SHOW_WEEK_VALUE_UNIT,	true);
		profile.weekColumnWidth				= Util.getXmlInteger(xmlProfile, 					ATTR_WEEK_COLUMN_WIDTH,			DEFAULT_SUMMARY_COLUMN_WIDTH);
		profile.weekValueColor		 		= (CalendarColor) Util.getXmlEnum(xmlProfile,		ATTR_WEEK_VALUE_COLOR,			DEFAULT_WEEK_VALUE_COLOR);
		profile.weekValueFont				= Util.getXmlFont(xmlProfile, 						ATTR_WEEK_VALUE_FONT,			defaultFont.getFontData()[0]);

// SET_FORMATTING_ON

		// tour formatter
		final FormatterData[] tourFormatterData = restoreProfile_FormatterData(
				xmlProfile,
				TAG_ALL_TOUR_FORMATTER,
				profile);
		if (tourFormatterData != null) {
			profile.allTourFormatterData = tourFormatterData;
		}

		// week formatter
		final FormatterData[] weekFormatterData = restoreProfile_FormatterData(
				xmlProfile,
				TAG_ALL_WEEK_FORMATTER,
				profile);
		if (weekFormatterData != null) {
			profile.allWeekFormatterData = weekFormatterData;
		}

		return profile;
	}

	private static FormatterData[] restoreProfile_FormatterData(final XMLMemento xmlProfile,
																final String tagAllFormatter,
																final CalendarProfile profile) {

		final XMLMemento xmlAllFormatter = (XMLMemento) xmlProfile.getChild(tagAllFormatter);
		if (xmlAllFormatter != null) {

			final ArrayList<FormatterData> allFormatterData = new ArrayList<>();

			for (final IMemento xmlFormatterData : xmlAllFormatter.getChildren()) {

				final boolean isEnabled = Util.getXmlBoolean(xmlFormatterData, ATTR_IS_SHOW_VALUE, true);

				final FormatterID id = (FormatterID) Util.getXmlEnum(
						xmlFormatterData,
						ATTR_FORMATTER_ID,
						FormatterID.EMPTY);

				final ValueFormat valueFormat = (ValueFormat) Util.getXmlEnum(
						xmlFormatterData,
						ATTR_FORMATTER_VALUE_FORMAT,
						ValueFormat.DUMMY_VALUE);

				allFormatterData.add(new FormatterData(isEnabled, id, valueFormat));
			}

			return allFormatterData.toArray(new FormatterData[allFormatterData.size()]);
		}

		return null;
	}

	private static void saveProfile(final CalendarProfile profile, final IMemento xmlProfile) {

// SET_FORMATTING_OFF
					
		// profile
		xmlProfile.putString(		ATTR_ID, 								profile.id);
		xmlProfile.putString(		ATTR_PROFILE_NAME, 						profile.name);
		Util.setXmlEnum(xmlProfile,	ATTR_PROFILE_DEFAULT_ID,		 		profile.defaultId);
		
		// layout
		xmlProfile.putBoolean(		ATTR_IS_TOGGLE_MONTH_COLOR, 			profile.isToggleMonthColor);
		xmlProfile.putBoolean(		ATTR_USE_DRAGGED_SCROLLING, 			profile.useDraggedScrolling);
		xmlProfile.putInteger(		ATTR_WEEK_HEIGHT, 						profile.weekHeight);
		Util.setXmlRgb(xmlProfile,	TAG_ALTERNATE_MONTH_RGB, 				profile.alternateMonthRGB);
		Util.setXmlRgb(xmlProfile,	TAG_CALENDAR_BACKGROUND_RGB, 			profile.calendarBackgroundRGB);
		Util.setXmlRgb(xmlProfile,	TAG_CALENDAR_FOREGROUND_RGB, 			profile.calendarForegroundRGB);
		
		// year columns
		xmlProfile.putBoolean(		ATTR_IS_SHOW_YEAR_COLUMNS, 				profile.isShowYearColumns);
		xmlProfile.putInteger(		ATTR_YEAR_COLUMNS, 						profile.yearColumns);
		xmlProfile.putInteger(		ATTR_YEAR_COLUMNS_SPACING, 				profile.yearColumnsSpacing);
		Util.setXmlEnum(xmlProfile,	ATTR_YEAR_COLUMNS_START, 				profile.yearColumnsStart);
		Util.setXmlFont(xmlProfile,	ATTR_YEAR_HEADER_FONT, 					profile.yearHeaderFont);
		
		// date column
		xmlProfile.putBoolean(		ATTR_IS_SHOW_DATE_COLUMN, 				profile.isShowDateColumn);
		xmlProfile.putInteger(		ATTR_DATE_COLUMN_WIDTH, 				profile.dateColumnWidth);
		Util.setXmlEnum(xmlProfile,	ATTR_DATE_COLUMN_CONTENT, 				profile.dateColumnContent);
		Util.setXmlFont(xmlProfile,	ATTR_DATE_COLUMN_FONT, 					profile.dateColumnFont);

		// day date
		xmlProfile.putBoolean(		ATTR_IS_HIDE_DAY_DATE_WHEN_NO_TOUR, 	profile.isHideDayDateWhenNoTour);
		xmlProfile.putBoolean(		ATTR_IS_SHOW_DAY_DATE, 					profile.isShowDayDate);
		xmlProfile.putBoolean(		ATTR_IS_SHOW_DAY_DATE_WEEKEND_COLOR, 	profile.isShowDayDateWeekendColor);
		Util.setXmlEnum(xmlProfile,	ATTR_DAY_DATE_FORMAT, 					profile.dayDateFormat);
		Util.setXmlFont(xmlProfile,	ATTR_DAY_DATE_FONT, 					profile.dayDateFont);

		// tour background
		xmlProfile.putInteger(		ATTR_TOUR_BACKGROUND_WIDTH, 			profile.tourBackgroundWidth);
		xmlProfile.putInteger(		ATTR_TOUR_BORDER_WIDTH, 				profile.tourBorderWidth);
		Util.setXmlEnum(xmlProfile,	ATTR_TOUR_BACKGROUND, 					profile.tourBackground);
		Util.setXmlEnum(xmlProfile,	ATTR_TOUR_BACKGROUND_COLOR1, 			profile.tourBackgroundColor1);
		Util.setXmlEnum(xmlProfile,	ATTR_TOUR_BACKGROUND_COLOR2, 			profile.tourBackgroundColor2);
		Util.setXmlEnum(xmlProfile,	ATTR_TOUR_BORDER, 						profile.tourBorder);
		Util.setXmlEnum(xmlProfile,	ATTR_TOUR_BORDER_COLOR, 				profile.tourBorderColor);

		// tour content
		xmlProfile.putBoolean(		ATTR_IS_SHOW_TOUR_CONTENT,				profile.isShowTourContent);
		xmlProfile.putBoolean(		ATTR_IS_SHOW_TOUR_VALUE_UNIT,			profile.isShowTourValueUnit);
		xmlProfile.putBoolean(		ATTR_IS_TRUNCATE_TOUR_TEXT,				profile.isTruncateTourText);
		xmlProfile.putInteger(		ATTR_TOUR_TRUNCATED_LINES, 				profile.tourTruncatedLines);
		xmlProfile.putInteger(		ATTR_TOUR_VALUE_COLUMNS, 				profile.tourValueColumns);
		Util.setXmlEnum(xmlProfile,	ATTR_TOUR_CONTENT_COLOR, 				profile.tourContentColor);
		Util.setXmlFont(xmlProfile,	ATTR_TOUR_CONTENT_FONT, 				profile.tourContentFont);
		Util.setXmlEnum(xmlProfile,	ATTR_TOUR_TITLE_COLOR, 					profile.tourTitleColor);
		Util.setXmlFont(xmlProfile,	ATTR_TOUR_TITLE_FONT, 					profile.tourTitleFont);
		Util.setXmlEnum(xmlProfile,	ATTR_TOUR_VALUE_FONT, 					profile.tourValueColor);
		Util.setXmlFont(xmlProfile,	ATTR_TOUR_VALUE_FONT, 					profile.tourValueFont);
		
		// week summary column
		xmlProfile.putBoolean(		ATTR_IS_SHOW_SUMMARY_COLUMN, 			profile.isShowSummaryColumn);
		xmlProfile.putBoolean(		ATTR_IS_SHOW_WEEK_VALUE_UNIT, 			profile.isShowWeekValueUnit);
		xmlProfile.putInteger(		ATTR_WEEK_COLUMN_WIDTH, 				profile.weekColumnWidth);
		Util.setXmlEnum(xmlProfile,	ATTR_WEEK_VALUE_COLOR, 					profile.weekValueColor);
		Util.setXmlFont(xmlProfile,	ATTR_WEEK_VALUE_FONT, 					profile.weekValueFont);
					
// SET_FORMATTING_ON

		// formatter
		saveProfile_FormatterData(xmlProfile.createChild(TAG_ALL_TOUR_FORMATTER), profile.allTourFormatterData);
		saveProfile_FormatterData(xmlProfile.createChild(TAG_ALL_WEEK_FORMATTER), profile.allWeekFormatterData);
	}

	private static void saveProfile_FormatterData(	final IMemento xmlAllTourFormatter,
													final FormatterData[] allFormatterData) {

		for (final FormatterData formatterData : allFormatterData) {

			final IMemento xmlFormatter = xmlAllTourFormatter.createChild(TAG_FORMATTER);

			xmlFormatter.putBoolean(ATTR_IS_SHOW_VALUE, formatterData.isEnabled);
			Util.setXmlEnum(xmlFormatter, ATTR_FORMATTER_ID, formatterData.id);
			Util.setXmlEnum(xmlFormatter, ATTR_FORMATTER_VALUE_FORMAT, formatterData.valueFormat);
		}
	}

	static void saveState() {

		if (_activeCalendarProfile == null) {

			// this can happen when not yet used

			return;
		}

		final XMLMemento xmlRoot = create_Root();

		saveState_Calendars(xmlRoot);

		Util.writeXml(xmlRoot, getProfileXmlFile());
	}

	/**
	 * Calendars
	 */
	private static void saveState_Calendars(final XMLMemento xmlRoot) {

		final IMemento xmlCalendars = xmlRoot.createChild(TAG_CALENDAR_PROFILE);
		{
			xmlCalendars.putString(ATTR_ACTIVE_PROFILE_ID, _activeCalendarProfile.id);

			for (final CalendarProfile profile : _allCalendarProfiles) {
				saveState_Calendars_Profile(profile, xmlCalendars);
			}
		}
	}

	private static void saveState_Calendars_Profile(final CalendarProfile profile, final IMemento xmlCalendars) {

		// <Calendar>
		final IMemento xmlProfile = xmlCalendars.createChild(TAG_CALENDAR);
		{
			saveProfile(profile, xmlProfile);
		}
	}

	static void setActiveCalendarProfile(final CalendarProfile selectedProfile) {

		setActiveCalendarProfileIntern(selectedProfile);
	}

	private static void setActiveCalendarProfileIntern(final CalendarProfile calendarProfile) {

		_activeCalendarProfile = calendarProfile;

		fireProfileModifyEvent();
	}

	/**
	 * Update value formatter
	 */
	static void updateFormatterValueFormat() {

		/*
		 * Tour formatter
		 */
		for (final FormatterData formatterData : _activeCalendarProfile.allTourFormatterData) {

			if (!formatterData.isEnabled) {
				continue;
			}

			final ValueFormat valueFormat = formatterData.valueFormat;

			switch (formatterData.id) {

			case ALTITUDE:
				_tourFormatter_Altitude.setValueFormat(valueFormat);
				break;

			case DISTANCE:
				_tourFormatter_Distance.setValueFormat(valueFormat);
				break;

			case PACE:
				_tourFormatter_Pace.setValueFormat(valueFormat);
				break;

			case SPEED:
				_tourFormatter_Speed.setValueFormat(valueFormat);
				break;

			case TIME_MOVING:
				_tourFormatter_Time_Moving.setValueFormat(valueFormat);
				break;

			case TIME_PAUSED:
				_tourFormatter_Time_Paused.setValueFormat(valueFormat);
				break;

			case TIME_RECORDING:
				_tourFormatter_Time_Recording.setValueFormat(valueFormat);
				break;

			default:
				break;
			}
		}

		/*
		 * Week formatter
		 */
		for (final FormatterData formatterData : _activeCalendarProfile.allWeekFormatterData) {

			if (!formatterData.isEnabled) {
				continue;
			}

			final ValueFormat valueFormat = formatterData.valueFormat;

			switch (formatterData.id) {

			case ALTITUDE:
				_weekFormatter_Altitude.setValueFormat(valueFormat);
				break;

			case DISTANCE:
				_weekFormatter_Distance.setValueFormat(valueFormat);
				break;

			case PACE:
				_weekFormatter_Pace.setValueFormat(valueFormat);
				break;

			case SPEED:
				_weekFormatter_Speed.setValueFormat(valueFormat);
				break;

			case TIME_MOVING:
				_weekFormatter_Time_Moving.setValueFormat(valueFormat);
				break;

			case TIME_PAUSED:
				_weekFormatter_Time_Paused.setValueFormat(valueFormat);
				break;

			case TIME_RECORDING:
				_weekFormatter_Time_Recording.setValueFormat(valueFormat);
				break;

			default:
				break;
			}
		}
	}

}