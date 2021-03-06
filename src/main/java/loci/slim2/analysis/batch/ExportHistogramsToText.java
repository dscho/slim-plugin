/*
 * #%L
 * SLIM Curve plugin for combined spectral-lifetime image analysis.
 * %%
 * Copyright (C) 2010 - 2014 Board of Regents of the University of
 * Wisconsin-Madison.
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/gpl-3.0.html>.
 * #L%
 */

package loci.slim2.analysis.batch;

import ij.IJ;
import ij.gui.GenericDialog;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;
import java.util.prefs.Preferences;

import loci.curvefitter.ICurveFitter.FitFunction;
import loci.curvefitter.ICurveFitter.FitRegion;
import loci.slim.analysis.Binning;
import loci.slim.analysis.HistogramStatistics;
import loci.slim.analysis.ISLIMAnalyzer;
import loci.slim.analysis.SLIMAnalyzer;
import loci.slim.fitted.FittedValue;
import loci.slim.fitted.FittedValueFactory;
import net.imglib2.RandomAccess;
import net.imglib2.meta.ImgPlus;
import net.imglib2.type.numeric.real.DoubleType;

/**
 * Exports histogram values as text for further analysis of SLIMPlugin results.
 * 
 * @author Aivar Grislis
 */
@SLIMAnalyzer(name="Export Histograms to Text")
public class ExportHistogramsToText implements ISLIMAnalyzer {
	private static final int BINS = 256;
	private static final long MIN_COUNT = 3;
	private static final String FILE_KEY = "export_histograms_to_text/file";
	private static final String APPEND_KEY = "export_histograms_to_text/append";
	private static final String CSV_KEY = "export_histograms_to_text/csv";
	private static final int CHANNEL_INDEX = 2;
	private static final char TAB = '\t';
	private static final char COMMA = ',';
	private static final String TSV_SUFFIX = ".tsv";
	private static final String CSV_SUFFIX = ".csv";
	private String fileName;
	private boolean append;
	private boolean csv;
	private BufferedWriter bufferedWriter;
	private boolean combined = true;

	public void analyze(ImgPlus<DoubleType> image, FitRegion region, FitFunction function, String parameters) {
		// need entire fitted image
		if (FitRegion.EACH == region) {
			boolean export = showFileDialog(getFileFromPreferences(), getAppendFromPreferences(), getCSVFromPreferences());
			if (export && null != fileName) {
				char separator;
				if (csv) {
					separator = COMMA;
					if (!fileName.endsWith(CSV_SUFFIX)) {
						fileName += CSV_SUFFIX;
					}
				}
				else {
					separator = TAB;
					if (!fileName.endsWith(TSV_SUFFIX)) {
						fileName += TSV_SUFFIX;
					}
				}
				saveFileInPreferences(fileName);
				saveAppendInPreferences(append);
				saveCSVInPreferences(csv);
				export(fileName, append, image, function, parameters, separator);
			}
		}
	}

	public void export(String fileName, boolean append, ImgPlus<DoubleType> image,
		FitFunction function, String parameters, char separator)
	{
		int params = 0;
		int components = 0;
		switch (function) {
			case SINGLE_EXPONENTIAL:
				params = 4;
				components = 1;
				break;
			case DOUBLE_EXPONENTIAL:
				params = 6;
				components = 2;
				break;
			case TRIPLE_EXPONENTIAL:
				params = 8;
				components = 3;
				break;
			case STRETCHED_EXPONENTIAL:
				params = 5;

				//TODO fix stretched; how many components?
				break;
		}
		FittedValue[] fittedValues = FittedValueFactory.createFittedValues(parameters, components);

		try {
			bufferedWriter = new BufferedWriter(new FileWriter(fileName, append));
		}
		catch (IOException e) {
			IJ.log("exception opening file " + fileName);
			IJ.handleException(e);
		}

		if (null != bufferedWriter) {
			try {
				// title this export
				bufferedWriter.write("Export Histograms" + separator + image.getName());
				bufferedWriter.newLine();
				bufferedWriter.newLine();

				// look at image dimensions
				long[] dimensions = new long[image.numDimensions()];
				image.dimensions(dimensions);
				int channels = (int) dimensions[CHANNEL_INDEX];

				// for all channels
				for (int channel = 0; channel < channels; ++channel) {
					if (channels > 1) {
						bufferedWriter.write("Channel" + separator + channel);
						bufferedWriter.newLine();
						bufferedWriter.newLine();
					}

					HistogramStatistics[] statisticsArray = new HistogramStatistics[fittedValues.length];
					for (int i = 0; i < fittedValues.length; ++i) {
						statisticsArray[i] = getStatistics(image, channel, params, fittedValues[i]);
					}

					if (combined) {
						HistogramStatistics.export(statisticsArray, bufferedWriter, separator);
					}
					else {
						for (HistogramStatistics statistics : statisticsArray) {
							// end early if count is too low
							if (!statistics.export(bufferedWriter, separator)) {
								break;
							}
						}
					}
				}
				bufferedWriter.newLine();
				bufferedWriter.close();
			}
			catch (IOException exception) {
				IJ.log("exception writing to file " + fileName);
				IJ.handleException(exception);
			}
		}
	}

	/**
	 * Builds statistics from image and single FittedValue.
	 * 
	 * @param image
	 * @param channel
	 * @param params
	 * @param fittedValue
	 * @return 
	 */
	public HistogramStatistics getStatistics(ImgPlus<DoubleType> image, int channel, int params, FittedValue fittedValue) {
		// first pass through image
		ExportHistogramsToText.Statistics1 statistics1 = getStatistics1(image, channel, params, fittedValue);

		// second pass through the image
		ExportHistogramsToText.Statistics2 statistics2 = getStatistics2(image, channel, params, fittedValue, statistics1.mean, statistics1.range, BINS);

		HistogramStatistics statistics = new HistogramStatistics();
		statistics.setTitle(fittedValue.getTitle());
		statistics.setCount(statistics1.count);
		statistics.setMin(statistics1.min);
		statistics.setMax(statistics1.max);
		//TODO handle this better
		if (null == statistics1.quartile) {
			statistics.setFirstQuartile(0.0);
			statistics.setMedian(0.0);
			statistics.setThirdQuartile(0.0);
			statistics.setMean(0.0);
			statistics.setStandardDeviation(0.0);
		}
		else {
			statistics.setFirstQuartile(statistics1.quartile[0]);
			statistics.setMedian(statistics1.quartile[1]);
			statistics.setThirdQuartile(statistics1.quartile[2]);
			statistics.setMean(statistics1.mean);
			statistics.setStandardDeviation(statistics2.standardDeviation);
		}
		statistics.setHistogramCount(statistics2.histogramCount);
		statistics.setMinRange(statistics1.range[0]);
		statistics.setMaxRange(statistics1.range[1]);
		statistics.setHistogram(statistics2.histogram);

		return statistics;
	}

	/**
	 * First pass through the image, gathering statistics.
	 * 
	 * @param image
	 * @param channel
	 * @param params
	 * @param fittedValue
	 * @return container of various statistics
	 */
	private ExportHistogramsToText.Statistics1 getStatistics1(ImgPlus<DoubleType> image, int channel, int params, FittedValue fittedValue) {
		long count = 0;
		double min = Double.MAX_VALUE;
		double max = -Double.MAX_VALUE;
		double sum = 0.0;
		double[] quartile = null;
		double[] range = null;
		long[] dimensions = new long[image.numDimensions()];
		image.dimensions(dimensions);
		RandomAccess<DoubleType> cursor = image.randomAccess();
		boolean hasChannelDimension;
		int parameterIndex;

		if (3 == image.numDimensions()) {
			hasChannelDimension = false;
			parameterIndex = 2;
		}
		else {
			hasChannelDimension = true;
			parameterIndex = 3;
		}

		// collect & sort non-NaN values
		double[] values = new double[(int) dimensions[0] * (int) dimensions[1]];
		int index = 0;
		int[] position = new int[dimensions.length];
		for (int y = 0; y < dimensions[1]; ++y) {
			for (int x = 0; x < dimensions[0]; ++x) {
				// set position
				position[0] = x;
				position[1] = y;
				if (hasChannelDimension) {
					position[2] = channel;
				}

				// grab all fitted parameters
				double[] fittedParameters = new double[params];
				for (int p = 0; p < params; ++p) {
					position[parameterIndex] = p;
					cursor.setPosition(position);
					double value = cursor.get().getRealDouble();
					fittedParameters[p] = value;
				}

				// get value for this fitted parameter & account for it
				double value = fittedValue.getValue(fittedParameters);
				if (!Double.isNaN(value)) {
					values[index++] = value;
					if (value < min) {
						min = value;
					}
					if (value > max) {
						max = value;
					}
					sum += value;
					++count;
				}
			}
		}
		// sort values to read off quartiles
		Arrays.sort(values, 0, index);

		if (count >= MIN_COUNT) {
			// read off the quartiles
			quartile = new double[3];
			int lowerTopHalfIndex, upperBottomHalfIndex;
			if (index % 2 != 0) {
				// odd array size

				// take the middle value
				lowerTopHalfIndex = upperBottomHalfIndex = index / 2;
				quartile[1] = values[lowerTopHalfIndex];
			}
			else {
				// even array size

				// take the mean of middle two values
				lowerTopHalfIndex = index / 2;
				upperBottomHalfIndex = lowerTopHalfIndex - 1;
				quartile[1] = (values[lowerTopHalfIndex] + values[upperBottomHalfIndex]) / 2;
			}

			if (upperBottomHalfIndex % 2 == 0) {
				// even index means odd half sizes

				// take the middle values
				index = upperBottomHalfIndex / 2;
				quartile[0] = values[index];
				index += lowerTopHalfIndex;
				quartile[2] = values[index];
			}
			else {
				// even half sizes

				// take the mean of middle two values
				index = upperBottomHalfIndex / 2;
				quartile[0] = (values[index] + values[index + 1]) / 2;

				index += lowerTopHalfIndex;
				quartile[2] = (values[index] + values[index + 1]) / 2;
			}

			// calculate range
			range = new double[2];
			double iqr = quartile[2] - quartile[0];
			range[0] = quartile[0] - 1.5 * iqr;
			range[1] = quartile[2] + 1.5 * iqr;
		}
		else if (0 == count) {
			// avoid reporting spurious values
			min = max = Double.NaN;
		}

		ExportHistogramsToText.Statistics1 statistics = new ExportHistogramsToText.Statistics1();
		statistics.count = count;
		statistics.min = min;
		statistics.max = max;
		statistics.mean = sum / count;
		statistics.quartile = quartile;
		statistics.range = range;
		return statistics;
	}

	/**
	 * Second pass through the image, gathering statistics.
	 * 
	 * @param image
	 * @param channel
	 * @param params
	 * @param fittedValue
	 * @param mean
	 * @param range
	 * @param bins
	 * @return container of various statistics
	 */
	private ExportHistogramsToText.Statistics2 getStatistics2(ImgPlus<DoubleType> image, int channel, int params, FittedValue fittedValue, double mean, double[] range, int bins) {
		double diffSquaredSum = 0.0;
		long count = 0;
		long histogramCount = 0;
		long[] histogram = new long[bins];

		long[] dimensions = new long[image.numDimensions()];
		image.dimensions(dimensions);
		RandomAccess<DoubleType> cursor = image.randomAccess();
		boolean hasChannelDimension;
		int parameterIndex;

		if (3 == image.numDimensions()) {
			hasChannelDimension = false;
			parameterIndex = 2;
		}
		else {
			hasChannelDimension = true;
			parameterIndex = 3;
		}

		// collect & histogram non-NaN values
		double[] values = new double[(int) dimensions[0] * (int) dimensions[1]];
		int index = 0;
		int[] position = new int[dimensions.length];
		for (int y = 0; y < dimensions[1]; ++y) {
			for (int x = 0; x < dimensions[0]; ++x) {
				// set position
				position[0] = x;
				position[1] = y;
				if (hasChannelDimension) {
					position[2] = channel;
				}

				// grab all fitted parameters
				double[] fittedParameters = new double[params];
				for (int p = 0; p < params; ++p) {
					position[parameterIndex] = p;
					cursor.setPosition(position);
					double value = cursor.get().getRealDouble();
					fittedParameters[p] = value;
				}

				// get value for this fitted parameter & account for it
				double value = fittedValue.getValue(fittedParameters);
				if (!Double.isNaN(value)) {
					// compute standard deviation from mean
					double diff = mean - value;
					diffSquaredSum += diff * diff;
					++count;

					int bin = Binning.exclusiveValueToBin(bins, range[0], range[1], value);
					if (0 <= bin && bin < bins) {
						++histogram[bin];
						++histogramCount;
					}
				}
			}
		}

		ExportHistogramsToText.Statistics2 statistics = new ExportHistogramsToText.Statistics2();
		statistics.standardDeviation = Math.sqrt(diffSquaredSum / count);
		statistics.histogramCount = histogramCount;
		statistics.histogram = histogram;
		return statistics;
	}

	/**
	 * Container for first batch of statistics.
	 */
	private class Statistics1 {
		public long count;
		public double min;
		public double max;
		public double mean;
		public double[] quartile;
		public double[] range;
	}

	/**
	 * Container for second batch of statistics.
	 */
	private class Statistics2 {
		public double standardDeviation;
		public long histogramCount;
		public long[] histogram;
	}

	private String getFileFromPreferences() {
		Preferences prefs = Preferences.userNodeForPackage(this.getClass());
		return prefs.get(FILE_KEY, fileName);
	}

	private void saveFileInPreferences(String fileName) {
		Preferences prefs = Preferences.userNodeForPackage(this.getClass());
		prefs.put(FILE_KEY, fileName);
	}

	private boolean getAppendFromPreferences() {
		Preferences prefs = Preferences.userNodeForPackage(this.getClass());
		return prefs.getBoolean(APPEND_KEY, append);
	}

	private void saveAppendInPreferences(boolean append) {
		Preferences prefs = Preferences.userNodeForPackage(this.getClass());
		prefs.putBoolean(APPEND_KEY, append);
	}

	private boolean getCSVFromPreferences() {
		Preferences prefs = Preferences.userNodeForPackage(this.getClass());
		return prefs.getBoolean(CSV_KEY, csv);
	}

	private void saveCSVInPreferences(boolean csv) {
		Preferences prefs = Preferences.userNodeForPackage(this.getClass());
		prefs.putBoolean(CSV_KEY, csv);
	}

	private boolean showFileDialog(String defaultFile, boolean defaultAppend, boolean defaultCSV) {
		GenericDialog dialog = new GenericDialog("Export Histograms to Text");
		dialog.addStringField("Save As:", defaultFile, 24);
		dialog.addCheckbox("Append", defaultAppend);
		dialog.addCheckbox("Comma Separated", defaultCSV);
		dialog.showDialog();
		if (dialog.wasCanceled()) {
			return false;
		}
		fileName = dialog.getNextString();
		append   = dialog.getNextBoolean();
		csv      = dialog.getNextBoolean();
		return true;
	}
}
