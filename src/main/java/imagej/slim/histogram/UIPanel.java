/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package imagej.slim.histogram;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
import javax.swing.JPanel;
import javax.swing.JTextField;

/**
 * This class holds the text fields that show the current minimum and maximum
 * LUT range.  It also has checkboxes to control how the ranges are derived
 * and displayed.
 * 
 * @author Aivar Grislis grislis at wisc dot edu
 */
public class UIPanel extends JPanel {
    private static final int DIGITS = 4;
    IUIPanelListener _listener;
    JCheckBox _autoRangeCheckBox;
    JCheckBox _combineChannelsCheckBox;
    JCheckBox _displayChannelsCheckBox;
    JTextField _minTextField;
    JTextField _maxTextField;
    boolean _autoRange;
    boolean _combineChannels;
    boolean _displayChannels;
    double _minLUT;
    double _maxLUT;

    /**
     * Constructor.
     * 
     * @param hasChannels
     */
    public UIPanel(boolean hasChannels) {
        super();

        // initial state
        _autoRange = true;
        _minLUT = _maxLUT = 0.0;

        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

        // make a panel for the min/max readouts
        JPanel readOutPanel = new JPanel();
        readOutPanel.setLayout(new BoxLayout(readOutPanel, BoxLayout.X_AXIS));

        _minTextField = new JTextField();
        _minTextField.addActionListener(
            new ActionListener() {
                public void actionPerformed(ActionEvent event) {
                    try {
                        _minLUT = Double.parseDouble(_minTextField.getText());
                        if (null != _listener) {
                            _listener.setMinMaxLUT(_minLUT, _maxLUT);
                        }
                    }
                    catch (NumberFormatException exception) {
                        _minTextField.setText("" + _minLUT);
                    }
                }
            }
        );
        readOutPanel.add(_minTextField);

        _maxTextField = new JTextField();
        _maxTextField.addActionListener(
            new ActionListener() {
                public void actionPerformed(ActionEvent event) {
                    try {
                        _maxLUT = Double.parseDouble(_maxTextField.getText());
                        if (null != _listener) {
                            _listener.setMinMaxLUT(_minLUT, _maxLUT);
                        }
                    }
                    catch (NumberFormatException exception) {
                        _maxTextField.setText("" + _maxLUT);
                    }
                }
            }
        );
        readOutPanel.add(_maxTextField);
        add(readOutPanel);
 
        _autoRangeCheckBox = new JCheckBox("Automatic Ranging", _autoRange);
        _autoRangeCheckBox.addItemListener(
            new ItemListener() {
                public void itemStateChanged(ItemEvent e) {
                    _autoRange = _autoRangeCheckBox.isSelected();
                    enableTextFields(_autoRange);
                    if (null != _listener) {
                        _listener.setAutoRange(_autoRange);
                    }
                }
            }
        );
        add(_autoRangeCheckBox);
        
        _combineChannelsCheckBox =
            new JCheckBox("Combine Channels", _combineChannels);
        _combineChannelsCheckBox.addItemListener(
            new ItemListener() {
                public void itemStateChanged(ItemEvent e) {
                    _combineChannels = _combineChannelsCheckBox.isSelected();
                    if (null != _listener) {
                        _listener.setCombineChannels(_combineChannels);
                    }
                }
            }
        );
        if (hasChannels) {
            add(_combineChannelsCheckBox);
        }

        _displayChannelsCheckBox =
            new JCheckBox("Display Channels", _displayChannels);
        _displayChannelsCheckBox.addItemListener(
            new ItemListener() {
                public void itemStateChanged(ItemEvent e) {
                    _displayChannels = _displayChannelsCheckBox.isSelected();
                    if (null != _listener) {
                        _listener.setDisplayChannels(_displayChannels);
                    }   
                }
            }
        );
        if (hasChannels) {
            add(_displayChannelsCheckBox);
        }

        enableTextFields(_autoRange);
    }

    /**
     * Sets a listener for this UI panel.  Listener is unique.
     * 
     * @param listener 
     */
    public void setListener(IUIPanelListener listener) {
        _listener = listener;
    }

    public void setAutoRange(boolean autoRange) {
        _autoRange = autoRange;
        _autoRangeCheckBox.setSelected(autoRange);
        enableTextFields(autoRange);
    }
    
    public void setCombineChannels(boolean combineChannels) {
        _combineChannels = combineChannels;
        _combineChannelsCheckBox.setSelected(combineChannels);
    }
    
    public void setDisplayChannels(boolean displayChannels) {
        _displayChannels = displayChannels;
        _displayChannelsCheckBox.setSelected(displayChannels);
    }

    /**
     * Called when the user is dragging the cursors on the histogram panel.
     * 
     * @param min
     * @param max 
     */
    public void dragMinMaxLUT(double min, double max) {
        System.out.println("UIPanel.dragMinMaxLUT");
        showMinMaxLUT(min, max);
    }

    /**
     * Called when the user sets new cursors on the histogram panel.
     * 
     * @param min
     * @param max 
     */
    public void setMinMaxLUT(double min, double max) {
        System.out.println("UIPanel.setMinMaxLUT");
        showMinMaxLUT(min, max);
        //TODO anything else?  if not combine these two methods
    }

    /**
     * Enable/disable min/max text fields as appropriate.
     */
    private void enableTextFields(boolean auto) {
        _minTextField.setEnabled(!auto);
        _maxTextField.setEnabled(!auto);
    }

    /*
     * Shows the minimum and maximum LUT readouts.  Limits number of digits
     * shown.
     */
    private void showMinMaxLUT(double min, double max) {
        DoubleFormatter minFormatter = new DoubleFormatter(true, DIGITS, min);
        _minTextField.setText(minFormatter.getText());
        _minLUT = Double.parseDouble(_minTextField.getText());
        DoubleFormatter maxFormatter = new DoubleFormatter(false, DIGITS, max);
        _maxTextField.setText(maxFormatter.getText());
        _maxLUT = Double.parseDouble(_maxTextField.getText());
    }    
}
