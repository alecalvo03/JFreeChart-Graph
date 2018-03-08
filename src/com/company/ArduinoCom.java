package com.company;

import gnu.io.CommPortIdentifier;
import gnu.io.SerialPort;
import gnu.io.SerialPortEvent;
import gnu.io.SerialPortEventListener;
import org.jfree.data.time.Millisecond;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.*;

/**
 * Created by Usuario on 27/02/2018.
 */
public class ArduinoCom implements SerialPortEventListener{

    // Variables de conexión
    private static OutputStream output = null;
    private static BufferedReader input;
    static SerialPort serialPort;
    private static final String PUERTO = "COM3";
    private static final int TIMEOUT = 2000;
    private static final int BAUD_RATE = 38400;

    private HashMap<Millisecond,Float> AcX = new HashMap<>();
    private HashMap<Millisecond,Float> AcY = new HashMap<>();
    private HashMap<Millisecond,Float> AcZ = new HashMap<>();

    public void initialize(){
        // Inicializar conexión con Arduino
        CommPortIdentifier puertoID = null;
        Enumeration puertoEnum = CommPortIdentifier.getPortIdentifiers();
        while(puertoEnum.hasMoreElements()){
            CommPortIdentifier actual = (CommPortIdentifier) puertoEnum.nextElement();
            if (PUERTO.equals(actual.getName())){
                puertoID = actual;
                break;
            }
        }
        if (puertoID == null){
            System.out.println("No se puede conectar al puerto");
            return;
        }
        try {
            serialPort = (SerialPort) puertoID.open(this.getClass().getName(),TIMEOUT);
            serialPort.setSerialPortParams(BAUD_RATE,SerialPort.DATABITS_8,SerialPort.STOPBITS_1, SerialPort.PARITY_NONE);
            output = serialPort.getOutputStream();
            input = new BufferedReader(new InputStreamReader(serialPort.getInputStream()));

            //Add event listeners
            serialPort.addEventListener(this);
            serialPort.notifyOnDataAvailable(true);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void sendData(String data){
        try{
            output.write(data.getBytes());
            System.out.println("Se envía "+ data);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * This should be called when you stop using the port.
     * This will prevent port locking on platforms like Linux.
     */
    public synchronized void close() {
        if (serialPort != null) {
            serialPort.removeEventListener();
            serialPort.close();
        }
    }

    private void setRawValues(String[] values){
        if (values.length != 3) return;
        try{
        AcX.put(new Millisecond(), Float.valueOf(values[0]));
        AcY.put(new Millisecond(), Float.valueOf(values[1]));
        AcZ.put(new Millisecond(), Float.valueOf(values[2]));
        }finally {
        }
    }

    public HashMap getAcX(){
        HashMap temp = (HashMap) AcX.clone();
        AcX.clear();
        return temp;
    }

    /**
     * Handle an event on the serial port. Read the data and print it.
     */
    @Override
    public synchronized void serialEvent(SerialPortEvent serialPortEvent) {
        if (serialPortEvent.getEventType() == SerialPortEvent.DATA_AVAILABLE) {
            try {
                String inputLine=input.readLine();
                //System.out.println(inputLine);
                // AcX AcY AcZ GyX GyY GyZ
                System.out.println(inputLine);
                String[] rawValues = inputLine.split(" ");
                setRawValues(rawValues);
            } catch (Exception e) {
                System.err.println(e.toString());
            }
        }
        // Ignore all the other eventTypes, but you should consider the other ones.
    }
}