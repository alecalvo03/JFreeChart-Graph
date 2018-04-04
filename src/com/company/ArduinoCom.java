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
    private static final String PUERTO = "COM9";
    private static final int TIMEOUT = 2000;
    private static final int BAUD_RATE = 38400;
    private JavaSocket sock;
    private int n = 50;
    boolean first = true;

    private HashMap<Millisecond,Float> Yaw = new HashMap<>();
    private HashMap<Millisecond,Float> Pitch = new HashMap<>();
    private HashMap<Millisecond,Float> Roll = new HashMap<>();

    private HashMap<Millisecond,Float> AcX = new HashMap<>();
    private HashMap<Millisecond,Float> AcY = new HashMap<>();
    private HashMap<Millisecond,Float> AcZ = new HashMap<>();

    private HashMap<Millisecond,Float> Thumb = new HashMap<>();
    private HashMap<Millisecond,Float> Index = new HashMap<>();
    private HashMap<Millisecond,Float> Middle = new HashMap<>();
    private HashMap<Millisecond,Float> Ring = new HashMap<>();
    private HashMap<Millisecond,Float> Pinky = new HashMap<>();

    public void initialize() throws IOException {
        // Inicializar conexión con Arduino
        CommPortIdentifier puertoID = null;
        Enumeration puertoEnum = CommPortIdentifier.getPortIdentifiers();
        //sock = new JavaSocket();
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
            Yaw.put(new Millisecond(), Float.valueOf(values[0]));
            Pitch.put(new Millisecond(), Float.valueOf(values[1]));
            Roll.put(new Millisecond(), Float.valueOf(values[2]));
            AcX.put(new Millisecond(), (Float.valueOf(values[3])));
            AcY.put(new Millisecond(), (Float.valueOf(values[4])));
            AcZ.put(new Millisecond(), (Float.valueOf(values[5])));
            Thumb.put(new Millisecond(), (Float.valueOf(values[6])));
            Index.put(new Millisecond(), (Float.valueOf(values[7])));
            Middle.put(new Millisecond(), (Float.valueOf(values[8])));
            Ring.put(new Millisecond(), (Float.valueOf(values[9])));
            Pinky.put(new Millisecond(), (Float.valueOf(values[10])));
        }finally{

        }
    }

    public synchronized HashMap getYaw(){
        HashMap temp = (HashMap) Yaw.clone();
        Yaw.clear();
        return temp;
    }

    public synchronized HashMap getPitch(){
        HashMap temp = (HashMap) Pitch.clone();
        Pitch.clear();
        return temp;
    }

    public synchronized HashMap getRoll(){
        HashMap temp = (HashMap) Roll.clone();
        Roll.clear();
        return temp;
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

    public synchronized HashMap getThumb(){
        HashMap temp = (HashMap) Thumb.clone();
        Thumb.clear();
        return temp;
    }

    public synchronized HashMap getIndex(){
        HashMap temp = (HashMap) Index.clone();
        Index.clear();
        return temp;
    }

    public synchronized HashMap getMiddle(){
        HashMap temp = (HashMap) Middle.clone();
        Middle.clear();
        return temp;
    }

    public synchronized HashMap getRing(){
        HashMap temp = (HashMap) Ring.clone();
        Ring.clear();
        return temp;
    }

    public synchronized HashMap getPinky(){
        HashMap temp = (HashMap) Pinky.clone();
        Pinky.clear();
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
                /*if(n == 0) {
                    if (first){
                        sock.writeToServer("setstartvalues_" + inputLine);
                        first = false;
                    } else {
                        String[] str1 = inputLine.split(" ");
                        if(str1.length == 11 && str1[0].length() > 1){
                            sock.writeToServer("data_" + inputLine);
                        }

                    }
                }*/
                // AcX AcY AcZ GyX GyY GyZ
                String[] rawValues = inputLine.split(" ");
                setRawValues(rawValues);
            } catch (Exception e) {
                n++;
                System.err.println(e.toString());
            }
        }
        // Ignore all the other eventTypes, but you should consider the other ones.
    }
}
