package net.tourbook.device.suunto;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.GZIPInputStream;

import org.apache.commons.io.FilenameUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import net.tourbook.common.UI;
import net.tourbook.common.util.StatusUtil;
import net.tourbook.common.util.Util;
import net.tourbook.data.TourData;
import net.tourbook.importdata.DeviceData;
import net.tourbook.importdata.SerialParameters;
import net.tourbook.importdata.TourbookDevice;

public class Suunto9DeviceDataReader extends TourbookDevice {

	private HashMap<String, TourData>	processedActivities				= new HashMap<String, TourData>();
	private HashMap<String, String>		childrenActivitiesToProcess	= new HashMap<String, String>();
	private HashMap<Long, TourData>		_newlyImportedTours				= new HashMap<Long, TourData>();
	private HashMap<Long, TourData>		_alreadyImportedTours			= new HashMap<Long, TourData>();

	// plugin constructor
	public Suunto9DeviceDataReader() {}

	@Override
	public String buildFileNameFromRawData(final String rawDataFileName) {
		// NEXT Auto-generated method stub
		return null;
	}

	@Override
	public boolean checkStartSequence(final int byteIndex, final int newByte) {
		return true;
	}

	public String getDeviceModeName(final int profileId) {
		return UI.EMPTY_STRING;
	}

	@Override
	public SerialParameters getPortParameters(final String portName) {
		return null;
	}

	@Override
	public int getStartSequenceSize() {
		return 0;
	}

	public int getTransferDataSize() {
		return -1;
	}

	@Override
	public boolean processDeviceData(final String importFilePath,
												final DeviceData deviceData,
												final HashMap<Long, TourData> alreadyImportedTours,
												final HashMap<Long, TourData> newlyImportedTours) {
		_newlyImportedTours = newlyImportedTours;
		_alreadyImportedTours = alreadyImportedTours;

		String jsonFileContent =
				GetJsonContentFromGZipFile(importFilePath);

		if (isValidJSONFile(jsonFileContent) == false) {
			return false;
		}
		ProcessFile(importFilePath, jsonFileContent);

		return true;
	}

	@Override
	public boolean validateRawData(final String fileName) {
		String jsonFileContent = GetJsonContentFromGZipFile(fileName);
		return isValidJSONFile(jsonFileContent);
	}

	/**
	 * Check if the file is a valid device JSON file.
	 * 
	 * @param importFilePath
	 * @return Returns <code>true</code> when the file contains content with the requested tag.
	 */
	protected boolean isValidJSONFile(String jsonFileContent) {
		BufferedReader fileReader = null;
		try {

			if (jsonFileContent == null) {
				return false;
			}

			try {
				JSONObject jsonContent = new JSONObject(jsonFileContent);
				JSONArray samples = (JSONArray) jsonContent.get("Samples");

				String firstSample = samples.get(0).toString();
				if (firstSample.contains("Lap") && firstSample.contains("Type") && firstSample.contains("Start"))
					return true;

			} catch (JSONException ex) {
				return false;
			}

		} catch (final Exception e) {
			StatusUtil.log(e);
		} finally {
			Util.closeReader(fileReader);
		}

		return true;
	}

	private String GetJsonContentFromGZipFile(String gzipFilePath) {
		String jsonFileContent = null;
		try {
			GZIPInputStream gzip = new GZIPInputStream(new FileInputStream(gzipFilePath));
			BufferedReader br = new BufferedReader(new InputStreamReader(gzip));

			jsonFileContent = br.readLine();

			// close resources
			br.close();
			gzip.close();
		} catch (IOException e) {
			return "";
		}

		return jsonFileContent;
	}

	private boolean ProcessFile(String filePath, String jsonFileContent) {
		SuuntoJsonProcessor suuntoJsonProcessor = new SuuntoJsonProcessor();

		String fileName =
				FilenameUtils.removeExtension(filePath);

		if (fileName.substring(fileName.length() - 5, fileName.length()) == ".json") {
			fileName = FilenameUtils.removeExtension(fileName);
		}

		String fileNumberString =
				fileName.substring(fileName.lastIndexOf('-') + 1, fileName.lastIndexOf('-') + 2);

		int fileNumber;
		try {
			fileNumber = Integer.parseInt(fileNumberString);
		} catch (NumberFormatException e) {
			return false;
		}

		TourData activity = new TourData();

		if (fileNumber == 1) {
			activity = suuntoJsonProcessor.ImportActivity(
					jsonFileContent,
					null);

			if (!processedActivities.containsKey(filePath))
				processedActivities.put(filePath, activity);
		} else if (fileNumber > 1) {
			// if we find the parent (e.g: The activity just before the
			// current one. Example : If the current is xxx-3, we find xxx-2)
			// then we import it reusing the parent activity AND we check that there is no children waiting to be imported
			// If nothing is found, we store it for (hopefully) future use.
			Map.Entry<String, TourData> parentEntry = null;
			for (Map.Entry<String, TourData> entry : processedActivities.entrySet()) {
				String key = entry.getKey();
				//TODO
				int ff = fileNumber - 1;
				if (key.contains(FilenameUtils.removeExtension(filePath) + "-" + ff + ".json")) {
					parentEntry = entry;
				}
			}

			if (parentEntry == null) {
				if (!childrenActivitiesToProcess.containsKey(filePath))
					childrenActivitiesToProcess.put(filePath, jsonFileContent);
			} else {
				activity = suuntoJsonProcessor.ImportActivity(
						jsonFileContent,
						parentEntry.getValue());

				//We remove the parent activity to replace it with the
				//updated one (parent activity concatenated with the current
				//one).
				processedActivities.remove(parentEntry.getKey());
				if (!processedActivities.containsKey(filePath))
					processedActivities.put(filePath, activity);
			}
		}

		//We check if the child(ren) has(ve) been provided earlier.
		//In this case, we concatenate it(them) with the parent
		//activity
		if (activity != null) {
			ConcatenateChildrenActivities(
					filePath,
					fileNumber,
					activity);

			activity.setImportFilePath(filePath);

			FinalizeTour(activity);
		}

		return true;
	}

	/**
	 * Concatenates children activities with a given activity.
	 * 
	 * @param filePath
	 *           The absolute full path of a given activity.
	 * @param currentFileNumber
	 *           The file number of the given activity. Example : If the current activity file is
	 *           1536723722706_{DeviceSerialNumber}_-2.json.gz its file number will be 2
	 * @param currentActivity
	 *           The current activity processed and created.
	 */
	private void ConcatenateChildrenActivities(	String filePath,
																int currentFileNumber,
																TourData currentActivity) {
		SuuntoJsonProcessor suuntoJsonProcessor = new SuuntoJsonProcessor();

		ArrayList<String> keysToRemove = new ArrayList<String>();
		for (Map.Entry<String, String> unused : childrenActivitiesToProcess.entrySet()) {
			String key = unused.getKey();
			String ff = String.valueOf(++currentFileNumber);

			Map.Entry<String, String> childEntry = null;

			if (key.contains(FilenameUtils.removeExtension(filePath) + "-" + ff + ".json")) {
				childEntry = unused;
			}

			if (childEntry == null)
				break;

			suuntoJsonProcessor.ImportActivity(
					childEntry.getValue(),
					currentActivity);

			// We just concatenated a child activity so we can remove it
			// from the list of activities to process
			keysToRemove.add(childEntry.getKey());

			// We need to update the activity we just concatenated by
			// updating the file path and the activity object.
			processedActivities.remove(filePath);
			processedActivities.put(childEntry.getKey(), currentActivity);
		}

		for (int index = 0; index < keysToRemove.size(); ++index) {
			childrenActivitiesToProcess.remove(keysToRemove.get(index));
		}
	}

	private void FinalizeTour(TourData tourData) {

		tourData.setDeviceId(deviceId);

		final String uniqueId = this.createUniqueId(tourData, Util.UNIQUE_ID_SUFFIX_SUUNTO9);
		final Long tourId = tourData.createTourId(uniqueId);

		// check if the tour is already imported
		if (_newlyImportedTours.containsKey(tourId)) {
			_newlyImportedTours.remove(tourId);
		}

		// add new tour to other tours
		_newlyImportedTours.put(tourId, tourData);

		// create additional data
		tourData.computeAltitudeUpDown();
		tourData.computeTourDrivingTime();
		tourData.computeComputedValues();
	}

}
