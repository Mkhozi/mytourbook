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
package net.tourbook.photo.manager;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.FutureTask;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;

import net.tourbook.photo.gallery.GalleryMTItem;

import org.eclipse.swt.widgets.Display;

public class PhotoManager {

	public static final int										THUMBNAIL_DEFAULT_SIZE	= 160;

// SET_FORMATTING_OFF
	
	public static int[]											IMAGE_SIZE = { THUMBNAIL_DEFAULT_SIZE, 600, 999999 };
	
// SET_FORMATTING_ON

	public static int											IMAGE_QUALITY_THUMB_160	= 0;
	public static int											IMAGE_QUALITY_600		= 1;
	public static int											IMAGE_QUALITY_ORIGINAL	= 2;
	/**
	 * This must be the max image quality which is also used as array.length()
	 */
	public static int											MAX_IMAGE_QUALITY		= 2;

	private static Display										_display;

	private static ThreadPoolExecutor							_executorService;

	private static final LinkedBlockingDeque<PhotoImageLoader>	_waitingQueue			= new LinkedBlockingDeque<PhotoImageLoader>();

	static {

		_display = Display.getDefault();

		int processors = Runtime.getRuntime().availableProcessors() - 2;
		processors = Math.max(processors, 1);

		processors = 2;

		System.out.println("Number of processors: " + processors);

		final ThreadFactory threadFactory = new ThreadFactory() {

			private int	_threadNumber	= 0;

			public Thread newThread(final Runnable r) {

				final String threadName = "Photo-Image-Loader-" + _threadNumber++; //$NON-NLS-1$

				final Thread thread = new Thread(r, threadName);

				thread.setPriority(Thread.MIN_PRIORITY);
				thread.setDaemon(true);

				return thread;
			}
		};

		_executorService = (ThreadPoolExecutor) Executors.newFixedThreadPool(processors, threadFactory);
	}

	public static void putImageInLoadingQueue(	final GalleryMTItem galleryItem,
												final Photo photo,
												final int imageQuality,
												final ILoadCallBack imageLoadCallback) {

		// set state
		photo.setLoadingState(PhotoLoadingState.IMAGE_IS_IN_LOADING_QUEUE, imageQuality);

		// add loading item into the waiting queue
		_waitingQueue.add(new PhotoImageLoader(_display, galleryItem, photo, imageQuality, imageLoadCallback));

		_executorService.submit(new Runnable() {
			public void run() {

				// get last added loader itme
				final PhotoImageLoader loadingItem = _waitingQueue.pollLast();

				if (loadingItem != null) {
					loadingItem.loadImage();
				}
			}
		});
	}

	/**
	 * Remove all items in the image loading queue.
	 */
	public synchronized static void stopImageLoading() {

		final Object[] queuedPhotoImageLoaderItems = _waitingQueue.toArray();

		/*
		 * terminate all submitted tasks, the executor shutdownNow() creates
		 * RejectedExecutionException when reusing the executor, I found no other way how to stop
		 * the submitted tasks
		 */
		final BlockingQueue<Runnable> taskQueue = _executorService.getQueue();
		for (final Runnable runnable : taskQueue) {
			final FutureTask<?> task = (FutureTask<?>) runnable;
			task.cancel(false);
		}

		_waitingQueue.clear();

		// reset loading state for not loaded images
		for (final Object object : queuedPhotoImageLoaderItems) {

			if (object == null) {
				// queue item can already be removed
				continue;
			}

			final PhotoImageLoader photoImageLoaderItem = (PhotoImageLoader) object;

			photoImageLoaderItem.photo.setLoadingState(PhotoLoadingState.UNDEFINED, photoImageLoaderItem.imageQuality);
		}
	}

}