/*******************************************************************************
 * Copyright (C) 2005, 2014  Wolfgang Schramm and Contributors
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
package net.tourbook.photo;

import net.tourbook.common.UI;
import net.tourbook.photo.internal.Activator;
import net.tourbook.photo.internal.Messages;

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferenceConverter;
import org.eclipse.jface.resource.ColorRegistry;
import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.viewers.StyledString;
import org.eclipse.jface.viewers.StyledString.Styler;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;

public class PhotoUI {

	public static Styler		PHOTO_FOLDER_STYLER;
	public static Styler		PHOTO_FILE_STYLER;

	public static final String	INVALID_PHOTO_IMAGE						= "INVALID_PHOTO_IMAGE";					//$NON-NLS-1$
	public static final String	INVALID_PHOTO_IMAGE_HOVERED				= "INVALID_PHOTO_IMAGE_HOVERED";			//$NON-NLS-1$

	public static final String	PHOTO_ANNOTATION_GPS_EXIF				= "PHOTO_ANNOTATION_GPS_EXIF";				//$NON-NLS-1$
	public static final String	PHOTO_ANNOTATION_GPS_TOUR				= "PHOTO_ANNOTATION_GPS_TOUR";				//$NON-NLS-1$
	public static final String	PHOTO_ANNOTATION_SAVED_IN_TOUR			= "PHOTO_ANNOTATION_SAVED_IN_TOUR";		//$NON-NLS-1$
	public static final String	PHOTO_ANNOTATION_SAVED_IN_TOUR_HOVERED	= "PHOTO_ANNOTATION_SAVED_IN_TOUR_HOVERED"; //$NON-NLS-1$

	public static final String	PHOTO_RATING_STAR						= "PHOTO_RATING_STAR";						//$NON-NLS-1$
	public static final String	PHOTO_RATING_STAR_AND_HOVERED			= "PHOTO_RATING_STAR_AND_HOVERED";			//$NON-NLS-1$
	public static final String	PHOTO_RATING_STAR_DELETE				= "PHOTO_RATING_STAR_DELETE";				//$NON-NLS-1$
	public static final String	PHOTO_RATING_STAR_DISABLED				= "PHOTO_RATING_STAR_DISABLED";			//$NON-NLS-1$
	public static final String	PHOTO_RATING_STAR_HOVERED				= "PHOTO_RATING_STAR_HOVERED";				//$NON-NLS-1$
	public static final String	PHOTO_RATING_STAR_NOT_HOVERED			= "PHOTO_RATING_STAR_NOT_HOVERED";			//$NON-NLS-1$
	public static final String	PHOTO_RATING_STAR_NOT_HOVERED_BUT_SET	= "PHOTO_RATING_STAR_NOT_HOVERED_BUT_SET";	//$NON-NLS-1$

	static {

		setPhotoColorsFromPrefStore();

		/*
		 * set photo styler
		 */
		PHOTO_FOLDER_STYLER = StyledString.createColorRegistryStyler(IPhotoPreferences.PHOTO_VIEWER_COLOR_FOLDER, null);
		PHOTO_FILE_STYLER = StyledString.createColorRegistryStyler(IPhotoPreferences.PHOTO_VIEWER_COLOR_FILE, null);

		final ImageRegistry imageRegistry = UI.IMAGE_REGISTRY;

		imageRegistry.put(INVALID_PHOTO_IMAGE, //
				Activator.getImageDescriptor(Messages.Image__PhotoInvalidPhotoImage));
		imageRegistry.put(INVALID_PHOTO_IMAGE_HOVERED,//
				Activator.getImageDescriptor(Messages.Image__PhotoInvalidPhotoImageHovered));

		imageRegistry.put(PHOTO_ANNOTATION_GPS_EXIF, //
				Activator.getImageDescriptor(Messages.Image__PhotoAnnotationExifGPS));
		imageRegistry.put(PHOTO_ANNOTATION_GPS_TOUR,//
				Activator.getImageDescriptor(Messages.Image__PhotoAnnotationTourGPS));

		imageRegistry.put(PHOTO_ANNOTATION_SAVED_IN_TOUR,//
				Activator.getImageDescriptor(Messages.Image__PhotoAnnotationSavedInTour));
		imageRegistry.put(PHOTO_ANNOTATION_SAVED_IN_TOUR_HOVERED,//
				Activator.getImageDescriptor(Messages.Image__PhotoAnnotationSavedInTourHovered));

		imageRegistry.put(PHOTO_RATING_STAR,//
				Activator.getImageDescriptor(Messages.Image__PhotoRatingStar));
		imageRegistry.put(PHOTO_RATING_STAR_AND_HOVERED,//
				Activator.getImageDescriptor(Messages.Image__PhotoRatingStarAndHovered));
		imageRegistry.put(PHOTO_RATING_STAR_DISABLED,//
				Activator.getImageDescriptor(Messages.Image__PhotoRatingStarDisabled));
		imageRegistry.put(PHOTO_RATING_STAR_DELETE,//
				Activator.getImageDescriptor(Messages.Image__PhotoRatingStarDelete));
		imageRegistry.put(PHOTO_RATING_STAR_HOVERED,//
				Activator.getImageDescriptor(Messages.Image__PhotoRatingStarHovered));
		imageRegistry.put(PHOTO_RATING_STAR_NOT_HOVERED,//
				Activator.getImageDescriptor(Messages.Image__PhotoRatingStarNotHovered));
		imageRegistry.put(PHOTO_RATING_STAR_NOT_HOVERED_BUT_SET,//
				Activator.getImageDescriptor(Messages.Image__PhotoRatingStarNotHoveredButSet));

	}

	/**
	 * When this method is called, this class is loaded and initialized in the static initializer,
	 * which is setting the colors in the color registry
	 */
	public static void init() {}

	/**
	 * Paint photo image .
	 * 
	 * @param gc
	 * @param photo
	 * @param signImage
	 * @param photoPosX
	 * @param photoPosY
	 * @param imageCanvasWidth
	 * @param imageCanvasHeight
	 * @param style
	 *            Style how the image is painted in the image canvas:
	 *            <p>
	 *            {@link SWT#CENTER}, {@link SWT#TOP}<br>
	 * @return Returns the rectangle where the image is painted.
	 */
	public static Rectangle paintPhotoImage(final GC gc,
											final Photo photo,
											final Image signImage,
											final int photoPosX,
											final int photoPosY,
											final int imageCanvasWidth,
											final int imageCanvasHeight,
											final int style,
											final Rectangle noHideArea) {

		final Rectangle imageRect = signImage.getBounds();
		final int _paintedImageWidth = imageRect.width;
		final int _paintedImageHeight = imageRect.height;

		final Point bestSize = RendererHelper.getBestSize(
				photo,
				_paintedImageWidth,
				_paintedImageHeight,
				imageCanvasWidth,
				imageCanvasHeight);

		final int paintedDest_Width = bestSize.x;
		final int paintedDest_Height = bestSize.y;

		// get center offset
		final int centerOffsetX = (imageCanvasWidth - paintedDest_Width) / 2;
		final int centerOffsetY = (imageCanvasHeight - paintedDest_Height) / 2;

		int paintedDest_DevX = photoPosX;
		int paintedDest_DevY = photoPosY;

		if (style == SWT.TOP) {

			paintedDest_DevX += centerOffsetX;

		} else {

			// default is vertical/horizontal centerd

			paintedDest_DevX += centerOffsetX;
			paintedDest_DevY += centerOffsetY;
		}

		final Rectangle rectPainted = new Rectangle(
				paintedDest_DevX,
				paintedDest_DevY,
				paintedDest_Width,
				paintedDest_Height);

		if (noHideArea != null) {

			if (rectPainted.intersects(noHideArea)) {

				// prevent that the image is painted over the no hide area (this can be a marker label)

				rectPainted.y = noHideArea.y + noHideArea.height;
			}
		}

		try {

			try {

				gc.setAntialias(SWT.ON);
				gc.setInterpolation(SWT.HIGH);

//				gc.setBackground(gc.getDevice().getSystemColor(SWT.COLOR_WIDGET_LIGHT_SHADOW));
//				gc.fillRectangle(photoPosX, photoPosY, imageCanvasWidth, imageCanvasHeight);

				gc.drawImage(signImage, //
						0,
						0,
						_paintedImageWidth,
						_paintedImageHeight,
						//
						rectPainted.x,
						rectPainted.y,
						rectPainted.width,
						rectPainted.height);

			} catch (final Exception e) {

				System.out.println("SWT exception occured when painting valid image " //$NON-NLS-1$
						+ photo.imageFilePathName
						+ " it's potentially this bug: https://bugs.eclipse.org/bugs/show_bug.cgi?id=375845"); //$NON-NLS-1$

				// ensure image is valid after reloading
				PhotoImageCache.disposeAll();
			}

		} catch (final Exception e) {

			gc.drawString(e.getMessage(), photoPosX, photoPosY);
		}

		return rectPainted;
	}

	/**
	 * Set photo colors in the JFace color registry from the pref store
	 */
	public static void setPhotoColorsFromPrefStore() {

		// pref store var cannot be set from a static field because it can be null !!!
		final IPreferenceStore prefStore = Activator.getDefault().getPreferenceStore();

		final ColorRegistry colorRegistry = JFaceResources.getColorRegistry();

		colorRegistry.put(IPhotoPreferences.PHOTO_VIEWER_COLOR_FOREGROUND, //
				PreferenceConverter.getColor(prefStore, IPhotoPreferences.PHOTO_VIEWER_COLOR_FOREGROUND));

		colorRegistry.put(IPhotoPreferences.PHOTO_VIEWER_COLOR_BACKGROUND, //
				PreferenceConverter.getColor(prefStore, IPhotoPreferences.PHOTO_VIEWER_COLOR_BACKGROUND));

		colorRegistry.put(IPhotoPreferences.PHOTO_VIEWER_COLOR_SELECTION_FOREGROUND, //
				PreferenceConverter.getColor(prefStore, IPhotoPreferences.PHOTO_VIEWER_COLOR_SELECTION_FOREGROUND));

		colorRegistry.put(IPhotoPreferences.PHOTO_VIEWER_COLOR_FOLDER, //
				PreferenceConverter.getColor(prefStore, IPhotoPreferences.PHOTO_VIEWER_COLOR_FOLDER));

		colorRegistry.put(IPhotoPreferences.PHOTO_VIEWER_COLOR_FILE, //
				PreferenceConverter.getColor(prefStore, IPhotoPreferences.PHOTO_VIEWER_COLOR_FILE));
	}

}
