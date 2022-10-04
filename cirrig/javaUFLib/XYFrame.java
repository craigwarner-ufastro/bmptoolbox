package javaUFLib;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;

public class XYFrame extends JFrame {

   public final UFPlotPanel thePlot;
   public JPanel xyPanel;
   public JTextField xField, yField, textField, charField, colorField;
   public JButton addButton, cancelButton;
   public JRadioButton dataButton, deviceButton;

   public XYFrame(final UFPlotPanel thePlot) {
      super("Add Text");
      setSize(220, 180);
      this.thePlot = thePlot;
      xyPanel = new JPanel();
      xyPanel.setPreferredSize(new Dimension(220,180));
      SpringLayout xyLayout = new SpringLayout();
      xyPanel.setLayout(xyLayout);

      JLabel xLabel = new JLabel("X:");
      xyPanel.add(xLabel);
      xyLayout.putConstraint(SpringLayout.WEST, xLabel, 5, SpringLayout.WEST, xyPanel);
      xyLayout.putConstraint(SpringLayout.NORTH, xLabel, 5, SpringLayout.NORTH, xyPanel); 
      xField = new JTextField(5);
      xyPanel.add(xField);
      xyLayout.putConstraint(SpringLayout.WEST, xField, 5, SpringLayout.EAST, xLabel);
      xyLayout.putConstraint(SpringLayout.NORTH, xField, 5, SpringLayout.NORTH, xyPanel);

      JLabel yLabel = new JLabel("Y:");
      xyPanel.add(yLabel);
      xyLayout.putConstraint(SpringLayout.WEST, yLabel, 20, SpringLayout.EAST, xField);
      xyLayout.putConstraint(SpringLayout.NORTH, yLabel, 5, SpringLayout.NORTH, xyPanel);
      yField = new JTextField(5);
      xyPanel.add(yField);
      xyLayout.putConstraint(SpringLayout.WEST, yField, 5, SpringLayout.EAST, yLabel);
      xyLayout.putConstraint(SpringLayout.NORTH, yField, 5, SpringLayout.NORTH, xyPanel);

      JLabel textLabel = new JLabel("Text:");
      xyPanel.add(textLabel);
      xyLayout.putConstraint(SpringLayout.WEST, textLabel, 5, SpringLayout.WEST, xyPanel);
      xyLayout.putConstraint(SpringLayout.NORTH, textLabel, 15, SpringLayout.SOUTH, xLabel);
      textField = new JTextField(12);
      xyPanel.add(textField);
      xyLayout.putConstraint(SpringLayout.WEST, textField, 5, SpringLayout.EAST, textLabel);
      xyLayout.putConstraint(SpringLayout.NORTH, textField, 13, SpringLayout.SOUTH, xLabel); 

      JLabel charLabel = new JLabel("CharSize:");
      xyPanel.add(charLabel);
      xyLayout.putConstraint(SpringLayout.WEST, charLabel, 5, SpringLayout.WEST, xyPanel);
      xyLayout.putConstraint(SpringLayout.NORTH, charLabel, 15, SpringLayout.SOUTH, textLabel);
      charField = new JTextField(2);
      xyPanel.add(charField);
      xyLayout.putConstraint(SpringLayout.WEST, charField, 5, SpringLayout.EAST, charLabel);
      xyLayout.putConstraint(SpringLayout.NORTH, charField, 13, SpringLayout.SOUTH, textLabel);

      JLabel colorLabel = new JLabel("Color:");
      xyPanel.add(colorLabel);
      xyLayout.putConstraint(SpringLayout.WEST, colorLabel, 5, SpringLayout.WEST, xyPanel);
      xyLayout.putConstraint(SpringLayout.NORTH, colorLabel, 15, SpringLayout.SOUTH, charLabel);
      colorField = new JTextField(10);
      xyPanel.add(colorField);
      xyLayout.putConstraint(SpringLayout.WEST, colorField, 5, SpringLayout.EAST, colorLabel);
      xyLayout.putConstraint(SpringLayout.NORTH, colorField, 13, SpringLayout.SOUTH, charLabel);

      dataButton = new JRadioButton("Data", true);
      deviceButton = new JRadioButton("Device");
      ButtonGroup coord = new ButtonGroup();
      coord.add(dataButton);
      coord.add(deviceButton);
      JLabel coordsLabel = new JLabel("Coords:");
      xyPanel.add(coordsLabel);
      xyLayout.putConstraint(SpringLayout.WEST, coordsLabel, 5, SpringLayout.WEST, xyPanel);
      xyLayout.putConstraint(SpringLayout.NORTH, coordsLabel, 15, SpringLayout.SOUTH, colorLabel);
      xyPanel.add(dataButton);
      xyLayout.putConstraint(SpringLayout.WEST, dataButton, 10, SpringLayout.EAST, coordsLabel);
      xyLayout.putConstraint(SpringLayout.NORTH, dataButton, 13, SpringLayout.SOUTH, colorLabel);
      xyPanel.add(deviceButton);
      xyLayout.putConstraint(SpringLayout.WEST, deviceButton, 10, SpringLayout.EAST, dataButton); 
      xyLayout.putConstraint(SpringLayout.NORTH, deviceButton, 13, SpringLayout.SOUTH, colorLabel);

      addButton = new JButton("Add Text");
      xyPanel.add(addButton);
      xyLayout.putConstraint(SpringLayout.WEST, addButton, 5, SpringLayout.WEST, xyPanel);
      xyLayout.putConstraint(SpringLayout.SOUTH, addButton, -5, SpringLayout.SOUTH, xyPanel); 

      addButton.addActionListener(new ActionListener() {
	public void actionPerformed(ActionEvent ev) {
	   float xc=0, yc=0;
	   String temp;
           String opts = "";
	   temp = xField.getText();
	   if (!temp.trim().equals("")) xc = Float.parseFloat(temp);
           temp = yField.getText();
           if (!temp.trim().equals("")) yc = Float.parseFloat(temp);
	   temp = charField.getText();
	   if (!temp.trim().equals("")) opts+="*charsize="+temp+", ";
	   temp = colorField.getText();
	   if (!temp.trim().equals("")) {
	      if (temp.indexOf(",") == -1) temp = temp.replaceAll(" ",","); 
	      opts+="*color="+temp+", ";
	   }
	   if (dataButton.isSelected()) opts+="*data, ";
	   else opts +="*device, ";
	   thePlot.xyouts(xc, yc, textField.getText(), opts);
	   thePlot.removexyFrame();
	   XYFrame.this.dispose();
	}
      });
      cancelButton = new JButton("Cancel");
      xyPanel.add(cancelButton);
      xyLayout.putConstraint(SpringLayout.EAST, cancelButton, -5, SpringLayout.EAST, xyPanel);
      xyLayout.putConstraint(SpringLayout.SOUTH, cancelButton, -5, SpringLayout.SOUTH, xyPanel);

      cancelButton.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent ev) {
	   XYFrame.this.dispose();
        }
      });
      getContentPane().add(xyPanel);
      pack();
      setVisible(true);
   }

}
