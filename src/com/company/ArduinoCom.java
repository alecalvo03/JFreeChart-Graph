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
    private static final String PUERTO = "COM5";
    private static final int TIMEOUT = 2000;
    private static final int BAUD_RATE = 38400;
    private JavaSocket sock;
    private int n = 50;
    boolean first = true;

    private HashMap<Millisecond,Float> AcX = new HashMap<>();
    private HashMap<Millisecond,Float> AcY = new HashMap<>();
    private HashMap<Millisecond,Float> AcZ = new HashMap<>();

    public void initialize() throws IOException {
        // Inicializar conexión con Arduino
        CommPortIdentifier puertoID = null;
        Enumeration puertoEnum = CommPortIdentifier.getPortIdentifiers();
        sock = new JavaSocket();
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
        if (n != 0) {
            n -= 1;
            return;
        }
        try {
            AcX.put(new Millisecond(), (float)(0));
            AcY.put(new Millisecond(), (float)(Math.sin(Math.PI/180 * Float.valueOf(values[1]))));
            AcZ.put(new Millisecond(), (float)(Math.sin(Math.PI/180 * Float.valueOf(values[2]))));
        }finally{

        }
    }

    public synchronized HashMap getAcX(){
        HashMap temp = (HashMap) AcX.clone();
        AcX.clear();
        return temp;
    }

    public synchronized HashMap getAcY(){
        HashMap temp = (HashMap) AcY.clone();
        AcY.clear();
        return temp;
    }

    public synchronized HashMap getAcZ(){
        HashMap temp = (HashMap) AcZ.clone();
        AcZ.clear();
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
                System.out.println(inputLine);
                if(n == 0) {
                    if (first){
                        sock.writeToServer("setstartvalues_" + inputLine);
                        first = false;
                    } else {
                        String[] str1 = inputLine.split(" ");
                        if(str1.length == 11 && str1[0].length() > 1){
                            sock.writeToServer("data_" + inputLine);
                        }

                    }
                }
                // AcX AcY AcZ GyX GyY GyZ
                String[] rawValues = inputLine.split(" ");
                setRawValues(rawValues);
            } catch (Exception e) {
                System.err.println(e.toString());
            }
        }
        // Ignore all the other eventTypes, but you should consider the other ones.
    }
}
