/*******************************************************************************
 * Copyright (C) 2005, 2010  Wolfgang Schramm and Contributors
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
package net.tourbook.device.sporttracks;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import net.tourbook.data.TourData;
import net.tourbook.device.InvalidDeviceSAXException;
import net.tourbook.importdata.DeviceData;
import net.tourbook.importdata.SerialParameters;
import net.tourbook.importdata.TourbookDevice;
import net.tourbook.util.FileUtils;
import net.tourbook.util.StatusUtil;
import net.tourbook.util.Util;

public class FitLogDeviceDataReader extends TourbookDevice {

	private static final String	XML_START_ID	= "<?xml";				//$NON-NLS-1$
	private static final String	XML_FIT_LOG_TAG	= "<FitnessWorkbook ";	//$NON-NLS-1$

	// plugin constructor
	public FitLogDeviceDataReader() {}

	@Override
	public String buildFileNameFromRawData(final String rawDataFileName) {
		return null;
	}

	@Override
	public boolean checkStartSequence(final int byteIndex, final int newByte) {
		return false;
	}

	public String getDeviceModeName(final int profileId) {
		return null;
	}

	@Override
	public SerialParameters getPortParameters(final String portName) {
		return null;
	}

	@Override
	public int getStartSequenceSize() {
		return -1;
	}

	public int getTransferDataSize() {
		return -1;
	}

	/**
	 * check if the file is a valid fit log file
	 */
	private boolean isValidXMLFile(final String importFilePath) {

		BufferedReader fileReader = null;
		try {

			final FileInputStream inputStream = new FileInputStream(importFilePath);

			/*
			 * .fitlog files contain BOM's (Byte Order Mark)
			 */
			FileUtils.consumeBOM(inputStream, Util.UTF_8);

			fileReader = new BufferedReader(new InputStreamReader(inputStream, Util.UTF_8));

			String line = fileReader.readLine();
			if (line == null || line.startsWith(XML_START_ID) == false) {
				return false;
			}

			line = fileReader.readLine();
			if (line == null || line.startsWith(XML_FIT_LOG_TAG) == false) {
				return false;
			}

		} catch (final Exception e1) {
			StatusUtil.log(e1);
		} finally {
			try {
				if (fileReader != null) {
					fileReader.close();
				}
			} catch (final IOException e) {
				StatusUtil.showStatus(e);
			}
		}

		return true;
	}

	@Override
	public boolean processDeviceData(	final String importFilePath,
										final DeviceData deviceData,
										final HashMap<Long, TourData> tourDataMap) {

		if (isValidXMLFile(importFilePath) == false) {
			return false;
		}

		final FitLogSAXHandler saxHandler = new FitLogSAXHandler(this, importFilePath, tourDataMap);

		try {

			final SAXParser parser = SAXParserFactory.newInstance().newSAXParser();

			parser.parse("file:" + importFilePath, saxHandler);//$NON-NLS-1$

		} catch (final InvalidDeviceSAXException e) {
			StatusUtil.log(e);
			return false;
		} catch (final Exception e) {
			StatusUtil.log("Error parsing file: " + importFilePath, e); //$NON-NLS-1$
			return false;
		} finally {
//			saxHandler.dispose();
		}

		return saxHandler.isImported();
	}

	public boolean validateRawData(final String fileName) {
		return isValidXMLFile(fileName);
	}
}