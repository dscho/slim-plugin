/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package loci.slim2.process.interactive;

import ij.gui.GenericDialog;
import imagej.data.Dataset;
import imagej.data.DatasetService;
import imagej.display.Display;
import imagej.data.Dataset;
import imagej.data.DatasetService;
import imagej.data.display.DataView;
import imagej.data.display.DefaultImageDisplay;
import imagej.display.DisplayService;
import java.io.File;
import java.util.prefs.Preferences;
import loci.slim.Excitation;
import loci.slim.ExcitationFileHandler;
import loci.slim.heuristics.CursorEstimator;
import loci.slim.ui.IUserInterfacePanel;
import loci.slim.ui.IUserInterfacePanelListener;
import loci.slim2.FittingContext;
import loci.slim2.decay.LifetimeDatasetWrapper;
import loci.slim2.decay.LifetimeGrayscaleDataset;
import loci.slim2.process.FitSettings;
import loci.slim2.process.InteractiveProcessor;
import loci.slim2.process.interactive.ui.DefaultUserInterfacePanel;
import loci.slim2.process.interactive.ui.UserInterfacePanel;
import loci.slim2.process.interactive.ui.UserInterfacePanelListener;

/**
 *
 * @author Aivar Grislis
 */
public class DefaultInteractiveProcessor implements InteractiveProcessor {
	private final DatasetService datasetService;
	private final DisplayService displayService;
	private LifetimeDatasetWrapper lifetimeDatasetWrapper;
	private LifetimeGrayscaleDataset lifetimeGrayscaleDataset;
	private int bins;
	private double timeRange;
	private UserInterfacePanel uiPanel;

	//TODO ARG passing in services to constructor is crappy
	public DefaultInteractiveProcessor(DatasetService datasetService, DisplayService displayService) {
		this.datasetService = datasetService;
		this.displayService = displayService;
	}
	
	@Override
	public FitSettings getFitSettings() {
		return null;
	}

	@Override
	public void setFitSettings(FitSettings fitSettings) {
		
	}
	
	@Override
	public boolean process(Dataset dataset) {
		// create the clickable grayscale representation
		createGrayscale(dataset);
		
		// display the UI
		if (null == uiPanel) {
			boolean tabbed = false;
			boolean showTau = false;
			uiPanel = new DefaultUserInterfacePanel(tabbed, showTau, bins, timeRange, new String[] { "one", "two" }, new String[] { "alpha", "beta" }, null, null);
		}
        uiPanel.setX(0);
        uiPanel.setY(0);
        uiPanel.setThreshold(10); //estimator.getThreshold());
        uiPanel.setChiSquareTarget(1.25); //estimator.getChiSquareTarget());
       // uiPanel.setFunctionParameters(0, estimator.getParameters(1, false));
       // uiPanel.setFunctionParameters(1, estimator.getParameters(2, false));
       // uiPanel.setFunctionParameters(2, estimator.getParameters(3, false));
       // uiPanel.setFunctionParameters(3, estimator.getParameters(0, true));
        uiPanel.setListener(
            new IUserInterfacePanelListener() {
                /**
                 * Triggers a fit.
                 */
                @Override
                public void doFit() {
                    /*_cancel = false;
                    _fitInProgress = true;*/
					System.out.println("doFit");
                }
				
				/**
				 * Triggers a refit.
				 */
				@Override
				public void reFit() {
					/*_cancel = false;
					_fitInProgress = true;
					_refit = true;*/
					System.out.println("reFit");
				}

                /**
                 * Cancels ongoing fit.
                 */
                @Override
                public void cancelFit() {
                    /*_cancel = true;
                    if (null != _fitInfo) {
                        _fitInfo.setCancel(true);
                    }*/
					System.out.println("cancelFit");
                }
                
                /**
                 * Quits running plugin.
                 */
                @Override
                public void quit() {
					/*_grayScaleImage.close();
                    _quit = true;*/
					System.out.println("quit");
                }
				
				/**
				 * Open new file(s).
				 */
				@Override
				public void openFile() {
                   /*File[] files = showFileDialog(getPathFromPreferences());
				   // were multiple files opened?
				   if (1 < files.length) {
					   batchProcessingWithUI(files);
				   }
				   // was a single file opened? (skip cancellations)
				   else if (1 == files.length) {
					   String savePath = _path;
					   String saveFile = _file;
					   
					   String[] pathAndFile = getPathAndFile(files[0]);
			           _path = pathAndFile[0];
			           _file = pathAndFile[1];

			           _image = loadImage(_path, _file);
			           if (null == _image) {
				            showError("Error", "Could not load image");
			           }
			           else {
				            if (getImageInfo(_image)) {
								savePathInPreferences(_path);

								// close existing grayscale and hook up a new one
								_uiPanel.setThresholdListener(null);
								_grayScaleImage.close();
								_grayScaleImage = null;
								showGrayScaleAndFit(uiPanel);
				            }
							else {
								// kludgy way to reset
								_path = savePath;
								_file = saveFile;
								_image = loadImage(_path, _file);
								getImageInfo(_image);
							}
			            }
				    }*/
					System.out.println("openFile");
				}

                /**
                 * Loads an excitation curve from file.
                 *
                 * @param fileName
                 * @return whether successful
                 */
                @Override
                public boolean loadExcitation(String fileName) {
                    /*Excitation excitation = ExcitationFileHandler.getInstance().loadExcitation(fileName, _timeRange);
                    return updateExcitation(uiPanel, excitation);*/
					System.out.println("loadExcitation");
					return true;
                }

                /**
                 * Creates an excitation curve from current X, Y and saves to file.
                 *
                 * @param fileName
                 * @return whether successful
                 */
                @Override
                public boolean createExcitation(String fileName) {
                    /*int channel = 0;
                    if (null != _grayScaleImage) {
                        channel = _grayScaleImage.getChannel();
                    }
                    int x = uiPanel.getX();
                    int y = uiPanel.getY();
                    double[] values = new double[_bins];
                    for (int b = 0; b < _bins; ++b) {
                        values[b] = getData(_cursor, channel, x, y, b);
                    }
                    Excitation excitation = ExcitationFileHandler.getInstance().createExcitation(fileName, values, _timeRange);
                    return updateExcitation(uiPanel, excitation);*/
					System.out.println("createExcitation");
					return true;
					
                }

                /**
                 * Estimates an excitation curve from current X, Y and saves to file.
                 *
                 * @param fileName
                 * @return whether successful
                 */
                @Override
                public boolean estimateExcitation(String fileName) {
					/*// get the data
                    int channel = 0;
                    if (null != _grayScaleImage) {
                        channel = _grayScaleImage.getChannel();
                    }
                    int x = uiPanel.getX();
                    int y = uiPanel.getY();
                    double[] inValues = new double[_bins];
                    for (int b = 0; b < _bins; ++b) {
                        inValues[b] = getData(_cursor, channel, x, y, b);
                    }
					
					// find the peak value and bin
					double peak = -Double.MAX_VALUE;
					int peakBin = 0;
					for (int b = 0; b < _bins; ++b) {
						if (inValues[b] > peak) {
							peak = inValues[b];
							peakBin = b;
						}
					}
					
					double maxSlope = -Double.MAX_VALUE;
					int maxSlopeBin = 0;
					double[] firstDerivative = new double[_bins];
					for (int b = 0; b < peakBin; ++b) {
						firstDerivative[b] = inValues[b + 1] - inValues[b];
						if (firstDerivative[b] > maxSlope) {
							maxSlope = firstDerivative[b];
							maxSlopeBin = b;
						}
					}
					
					//TODO WHY? _fittingCursor = null;
					
					double a = peak;
					double b = (double) peakBin;
					double c = (double) (peakBin - maxSlopeBin) / 2;
					
					System.out.println("max slope estimated GAUSSIAN a " + a + " b " + b + " c " + c);
					
					double[] outValues = new double[_bins];
					for (int i = 0; i < _bins; ++i) {
						outValues[i] = gaussian(a, b, c, i);
					}
					
					System.out.println("PEAK VALUE " + peak + " BIN " + peakBin);
					System.out.println("MAX SLOPE " + maxSlope + " BIN " + maxSlopeBin);
					System.out.println("GAUSSIAN a " + a + " b " + b + " c " + c);
					//TODO END EXPERIMENTAL
					for (double oV : outValues) {
						if (0.0 != oV) System.out.println(" " + oV);
					}
					
                    Excitation excitation = ExcitationFileHandler.getInstance().createExcitation(fileName, outValues, _timeRange);
                    return updateExcitation(uiPanel, excitation);*/
					System.out.println("estimateExcitation");
					return true;
                }

				@Override
				public boolean gaussianExcitation(String fileName) {
					/*Preferences prefs = Preferences.userNodeForPackage(this.getClass());
					double a = prefs.getDouble(GAUSSIAN_A_KEY, 30.0);
					double b = prefs.getDouble(GAUSSIAN_B_KEY, 20.0);
					double c = prefs.getDouble(GAUSSIAN_C_KEY, 2.0);
					
					GenericDialog dialog = new GenericDialog("Gaussian Excitation");
					dialog.addNumericField("height", a, 5);
					dialog.addNumericField("position", b, 5);
					dialog.addNumericField("width", c, 5);
					dialog.showDialog();
					if (dialog.wasCanceled()) {
						return false;
					}
					a = dialog.getNextNumber();
					b = dialog.getNextNumber();
					c = dialog.getNextNumber();
					
					prefs.putDouble(GAUSSIAN_A_KEY, a);
					prefs.putDouble(GAUSSIAN_B_KEY, b);
					prefs.putDouble(GAUSSIAN_C_KEY, c);
					
					double[] outValues = new double[_bins];
					for (int i = 0; i < _bins; ++i) {
						outValues[i] = gaussian(a, b, c, i);
					}
					
					Excitation excitation = ExcitationFileHandler.getInstance().createExcitation(fileName, outValues, _timeRange);
					return updateExcitation(uiPanel, excitation);*/
					System.out.println("gaussianExcitation");
					return true;
				}
				
				/**
                 * Cancels the current excitation curve, if any.
                 *
                 */
                @Override
                public void cancelExcitation() {
                    /*if (null != _excitationPanel) {
                        _excitationPanel.quit();
                        _excitationPanel = null;
                        updateExcitation(null, null);
                        //TODO redo stop/start cursors on decay curve?
                    }*/
					System.out.println("cancelExcitation");
                }

                /**
                 * Estimates prompt and decay cursors.
                 */
                @Override
                public void estimateCursors() {
                    /*double xInc = _timeRange;
                    
                    double[] prompt = null;
                    if (null != _excitationPanel) {
                        prompt = _excitationPanel.getRawValues();
                    }
                    double[] decay = new double[_bins];
                    for (int b = 0; b < _bins; ++b) {
                        decay[b] = getData(_cursor, _channel, _x, _y, b);
                    }
                    
                    double chiSqTarget = _uiPanel.getChiSquareTarget();
//                    System.out.println("chiSqTarget is " + chiSqTarget);
//                    System.out.println("prompt is " + prompt + " and fitting cursor thinks prompt " + _fittingCursor.getHasPrompt());
                    if (null != prompt && _fittingCursor.getHasPrompt()) {
                        double[] results = CursorEstimator.estimateCursors
                                (xInc, prompt, decay, chiSqTarget);
                        
                        // want all the fitting cursor listeners to get everything at once
                        _fittingCursor.suspendNotifications();
                        _fittingCursor.setHasPrompt(true);
                        _fittingCursor.setPromptStartBin
                                ((int) results[CursorEstimator.PROMPT_START]);
                        _fittingCursor.setPromptStopBin
                                ((int) results[CursorEstimator.PROMPT_STOP]);
                        _fittingCursor.setPromptBaselineValue
                                (results[CursorEstimator.PROMPT_BASELINE]);
                        _fittingCursor.setTransientStartBin
                                ((int) results[CursorEstimator.TRANSIENT_START]);
                        _fittingCursor.setDataStartBin
                                ((int) results[CursorEstimator.DATA_START]);
                        _fittingCursor.setTransientStopBin
                                ((int) results[CursorEstimator.TRANSIENT_STOP]);
                        _fittingCursor.sendNotifications();
                    }
                    else
                    {
                        int[] results = CursorEstimator.estimateDecayCursors
                                (xInc, decay);
                        
                        // want all the fitting cursor listeners to get everything at once
                        _fittingCursor.suspendNotifications();
                        _fittingCursor.setHasPrompt(false);
                        _fittingCursor.setTransientStartBin(results[CursorEstimator.TRANSIENT_START]);
                        _fittingCursor.setDataStartBin(results[CursorEstimator.DATA_START]);
                        _fittingCursor.setTransientStopBin(results[CursorEstimator.TRANSIENT_STOP]);
                        _fittingCursor.sendNotifications();
                    }*/
					System.out.println("estimateCursors");
                }
            }
        );
        uiPanel.getFrame().setLocationRelativeTo(null);
        uiPanel.getFrame().setVisible(true);
		
		
		System.out.println(" " + dataset);
		return true;
	}
	
	private void createGrayscale(Dataset dataset) {
		// wrap the dataset for lifetime information
		lifetimeDatasetWrapper = new LifetimeDatasetWrapper(dataset);
		//fittingContext.setDatasetWrapper(lifetimeDatasetWrapper);
		
		// make a grayscale version of lifetime dataset
		lifetimeGrayscaleDataset = new LifetimeGrayscaleDataset(datasetService, lifetimeDatasetWrapper, 1); //TODO ARG why pass in binning #??
		//fittingContext.setGrayscaleDataset(lifetimeGrayscaleDataset);
		
		// display grayscale version
		Display<?> display = displayService.createDisplay(lifetimeGrayscaleDataset.getDataset());
		//TODO ARG how to draw overlays on top of this display???
		//fittingContext.setGrayscaleDisplay(display);
	}
}
