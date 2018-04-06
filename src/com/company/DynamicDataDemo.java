package com.company;

/* ===========================================================
 * JFreeChart : a free chart library for the Java(tm) platform
 * ===========================================================
 *
 * (C) Copyright 2000-2004, by Object Refinery Limited and Contributors.
 *
 * Project Info:  http://www.jfree.org/jfreechart/index.html
 *
 * This library is free software; you can redistribute it and/or modify it under the terms
 * of the GNU Lesser General Public License as published by the Free Software Foundation;
 * either version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License along with this
 * library; if not, write to the Free Software Foundation, Inc., 59 Temple Place, Suite 330,
 * Boston, MA 02111-1307, USA.
 *
 * [Java is a trademark or registered trademark of Sun Microsystems, Inc.
 * in the United States and other countries.]
 *
 * --------------------
 * DynamicDataDemo.java
 * --------------------
 * (C) Copyright 2002-2004, by Object Refinery Limited.
 *
 * Original Author:  David Gilbert (for Object Refinery Limited).
 * Contributor(s):   -;
 *
 * $Id: DynamicDataDemo.java,v 1.12 2004/05/07 16:09:03 mungady Exp $
 *
 * Changes
 * -------
 * 28-Mar-2002 : Version 1 (DG);
 *
 */

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.plot.XYPlot;
import org.jfree.data.time.Millisecond;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesCollection;
import org.jfree.data.xy.XYDataset;
import org.jfree.ui.ApplicationFrame;
import org.jfree.ui.RefineryUtilities;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;


/**
 * A demonstration application showing a time YawSeries chart where you can dynamically add
 * (random) data by clicking on a button.
 *
 */
public class DynamicDataDemo extends ApplicationFrame implements ActionListener {

    /** The time YawSeries data. */
    private static TimeSeries YawSeries;
    private static TimeSeries PitchSeries;
    private static TimeSeries RollSeries;

    private static TimeSeries AcxSeries;
    private static TimeSeries AcySeries;
    private static TimeSeries AczSeries;

    private static TimeSeries thumbSeries;
    private static TimeSeries indexSeries;
    private static TimeSeries middleSeries;
    private static TimeSeries ringSeries;
    private static TimeSeries pinkySeries;

    private static int angleScale1 = -200;
    private static int angleScale2 = 200;

    private static int acScale1 = -2000;
    private static int acScale2 = -2000;

    private static int fingerScale1 = -2000;
    private static int fingerScale2 = -2000;

    static ArduinoCom arduinocom;

    /** The most recent value added. */
    private double lastValue = 100.0;

    /**
     * Constructs a new demonstration application.
     *
     * @param title  the frame title.
     */
    public DynamicDataDemo(final String title) {

        super(title);
        YawSeries = new TimeSeries("Yaw Data", Millisecond.class);
        PitchSeries = new TimeSeries("Pitch Data", Millisecond.class);
        RollSeries = new TimeSeries("Roll Data", Millisecond.class);

        AcxSeries = new TimeSeries("X Acceleration", Millisecond.class);
        AcySeries = new TimeSeries("Y Acceleration", Millisecond.class);
        AczSeries = new TimeSeries("Z Acceleration", Millisecond.class);

        thumbSeries = new TimeSeries("Thumb", Millisecond.class);
        indexSeries = new TimeSeries("Index", Millisecond.class);
        middleSeries = new TimeSeries("Middle", Millisecond.class);
        ringSeries = new TimeSeries("Ring", Millisecond.class);
        pinkySeries = new TimeSeries("Pinky", Millisecond.class);

        final TimeSeriesCollection datasetangles = new TimeSeriesCollection(YawSeries);
        datasetangles.addSeries(PitchSeries);
        datasetangles.addSeries(RollSeries);
        final JFreeChart chartangles = createChart(datasetangles, "Angles Chart");

        final TimeSeriesCollection datasetacc = new TimeSeriesCollection(AcxSeries);
        datasetacc.addSeries(AcySeries);
        datasetacc.addSeries(AczSeries);
        final JFreeChart chartacc = createChart(datasetacc, "Acceleration Chart");

        final TimeSeriesCollection datasetfinger = new TimeSeriesCollection(thumbSeries);
        datasetfinger.addSeries(indexSeries);
        datasetfinger.addSeries(middleSeries);
        datasetfinger.addSeries(ringSeries);
        datasetfinger.addSeries(pinkySeries);
        final JFreeChart chartfinger = createChart(datasetfinger, "Finger Chart");

        final ChartPanel chartPanel1 = new ChartPanel(chartangles);
        final ChartPanel chartPanel2 = new ChartPanel(chartacc);
        final ChartPanel chartPanel3 = new ChartPanel(chartfinger);

        final JPanel content = new JPanel();
        content.add(chartPanel1);
        chartPanel1.setPreferredSize(new java.awt.Dimension(500, 270));
        content.add(chartPanel2);
        chartPanel2.setPreferredSize(new java.awt.Dimension(500, 270));
        content.add(chartPanel3);
        chartPanel3.setPreferredSize(new java.awt.Dimension(500, 270));
        content.setLayout(new BoxLayout(content,BoxLayout.Y_AXIS));

        setContentPane(content);

    }

    /**
     * Creates a sample chart.
     *
     * @param dataset  the dataset.
     *
     * @return A sample chart.
     */
    private JFreeChart createChart(final XYDataset dataset, String title) {
        final JFreeChart result = ChartFactory.createTimeSeriesChart(
                title,
                "Time",
                "Value",
                dataset,
                true,
                true,
                false
        );
        final XYPlot plot = result.getXYPlot();
        ValueAxis axis = plot.getDomainAxis();
        axis.setAutoRange(true);
        axis.setFixedAutoRange(60000.0);  // 60 seconds
        axis = plot.getRangeAxis();
        axis.setRange(angleScale1, angleScale2);
        return result;
    }

    // ****************************************************************************
    // * JFREECHART DEVELOPER GUIDE                                               *
    // * The JFreeChart Developer Guide, written by Ale Calvo, is available       *
    // * to purchase from Object Refinery Limited:                                *
    // *                                                                          *
    // * http://www.object-refinery.com/jfreechart/guide.html                     *
    // *                                                                          *
    // * Sales are used to provide funding for the JFreeChart project - please    *
    // * support us so that we can continue developing free software.             *
    // ****************************************************************************

    /**
     * Handles a click on the button by adding new (random) data.
     *
     * @param e  the action event.
     */
    public void actionPerformed(final ActionEvent e) {
        if (e.getActionCommand().equals("ADD_DATA")) {
            final double factor = 0.90 + 0.2 * Math.random();
            this.lastValue = this.lastValue * factor;
            final Millisecond now = new Millisecond();
            System.out.println("Now = " + now.toString());
            this.YawSeries.add(new Millisecond(), this.lastValue);
        }
    }

    public static void addValues(HashMap hash, TimeSeries toAdd){
        if (!hash.isEmpty()) {
            Set set = hash.entrySet();
            Iterator iterator = set.iterator();
            while (iterator.hasNext()) {
                Map.Entry mentry = (Map.Entry) iterator.next();
                toAdd.add((Millisecond) mentry.getKey(), (Float) mentry.getValue());
            }
        }
    }

    /**
     * Starting point for the demonstration application.
     *
     * @param args  ignored.
     */
    public static void main(final String[] args) throws IOException {
        //arduinocom = new ArduinoCom();
        //arduinocom.initialize();
        final DynamicDataDemo demo = new DynamicDataDemo("DataGraphs");
        demo.pack();
        RefineryUtilities.centerFrameOnScreen(demo);
        demo.setVisible(true);
        Thread t = new Thread(() ->{
            while (true) {
                HashMap Yaw = DynamicDataDemo.arduinocom.getYaw();
                HashMap Pitch = DynamicDataDemo.arduinocom.getPitch();
                HashMap Roll = DynamicDataDemo.arduinocom.getRoll();

                HashMap acX = DynamicDataDemo.arduinocom.getAcX();
                HashMap acY = DynamicDataDemo.arduinocom.getAcY();
                HashMap acZ = DynamicDataDemo.arduinocom.getAcZ();

                HashMap thumb = DynamicDataDemo.arduinocom.getThumb();
                HashMap index = DynamicDataDemo.arduinocom.getIndex();
                HashMap middle = DynamicDataDemo.arduinocom.getMiddle();
                HashMap ring = DynamicDataDemo.arduinocom.getRing();
                HashMap pinky = DynamicDataDemo.arduinocom.getPinky();

                addValues(Yaw,YawSeries);
                addValues(Pitch,PitchSeries);
                addValues(Roll,RollSeries);

                addValues(acX,AcxSeries);
                addValues(acY,AcySeries);
                addValues(acZ,AczSeries);

                addValues(thumb,thumbSeries);
                addValues(index,indexSeries);
                addValues(middle,middleSeries);
                addValues(ring,ringSeries);
                addValues(pinky,pinkySeries);

                try {
                    Thread.sleep(100);
                } catch (InterruptedException ex) {
                    System.out.println(ex);
                }
            }
        });
        t.start();
    }

}
