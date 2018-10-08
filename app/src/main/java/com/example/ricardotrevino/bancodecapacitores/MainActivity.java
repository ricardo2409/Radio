package com.example.ricardotrevino.bancodecapacitores;

import android.app.AlertDialog;
import android.app.DialogFragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Color;
import android.os.Handler;
import android.os.SystemClock;
import android.support.v4.app.FragmentActivity;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

public class MainActivity extends FragmentActivity implements View.OnClickListener {

    Button btnConnect, btnVoltaje, btnConfiguracion, btnAjuste;

    private static final UUID PORT_UUID = UUID.fromString("00001101-0000-1000-8000-00805f9b34fb");
    public static BluetoothDevice device;
    public static BluetoothSocket socket;
    public static OutputStream outputStream;
    public static InputStream inputStream;

    static Button btnDiagnostico;
    static TextView tvRSSILocal, tvRSSIRemoto, tvRuidoLocal, tvRuidoRemoto, tvConect;


    boolean connected = false;
    static String tokens[];
    static String tokensRSSI[];
    static boolean abierto;
    static boolean RadOn;
    static boolean manOn;
    static boolean diagnostico;

    static boolean socketConectado;
    boolean stopThread;

    final static int SOURCE_ADDRESS = 0;
    final static int DESTINATION_ADDRESS = 1;
    final static int VOLTAGE_CONTROL_CHECKBOX = 2;
    final static int MINIMUM_VOLTAGE = 3;
    final static int MAXIMUM_VOLTAGE = 4;
    final static int AC_VOLTAGE = 5;


    static String s;

    static String a;
    static String b;

    static Thread thread;
    static Handler handler = new Handler();
    static String control = "Status";
    static String atributo = "NetID";
    String netIDValue, potenciaValue, nodeIDvalue, destination, s10, s11, s12, s13;

    static VoltajeFragment voltFrag;
    static ConfigurationFragment conFrag;
    static DiagnosticoFragment diagFrag;
    static AjusteFragment ajusFrag;

    Boolean DiagnosticoBool, ConfiguracionBool;
    ProgressBar progressBar;
    String controlPassword = "OK";

    static int controlBloqueado = 0;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initItems();
        btnConnect = (Button) findViewById(R.id.btnConnect);
        btnVoltaje = (Button) findViewById(R.id.btnVoltaje);
        btnConfiguracion = (Button) findViewById(R.id.btnConfiguracion);
        btnDiagnostico = (Button) findViewById(R.id.btnDiagnostico);
        btnAjuste = (Button) findViewById(R.id.btnAjuste);
        progressBar = (ProgressBar) findViewById(R.id.progressBar1);
        progressBarHide();



        btnConnect.setOnClickListener(this);
        btnVoltaje.setOnClickListener(this);
        btnConfiguracion.setOnClickListener(this);
        btnDiagnostico.setOnClickListener(this);
        btnAjuste.setOnClickListener(this);

        // Agrega el fragmento Voltaje en MainActivity
        conFrag = new ConfigurationFragment();
        diagFrag = new DiagnosticoFragment();
        voltFrag = new VoltajeFragment();
        ajusFrag = new AjusteFragment();

        FragmentTransaction transaction = getFragmentManager().beginTransaction();
        transaction.add(R.id.fragment_container, voltFrag);
        transaction.commit();
        DiagnosticoBool = false;
        ConfiguracionBool = false;
        System.out.println("Ya terminó OnCreate");

    }

    @Override
    public void onClick(View view) {

        switch (view.getId()){
            case R.id.btnConnect:
                if(!connected) {
                    if (BTinit()) {

                        BTconnect();
                        btnVoltaje.setBackgroundColor(Color.LTGRAY);
                        /*
                        try{
                            askGPS();
                        }catch(IOException e){

                        }
                        */

                    }
                }else{
                    try
                    {
                        desconectarBluetooth();
                    }
                    catch (IOException ex) { }
                }
                break;


            case R.id.btnVoltaje:
                if (connected){
                    try {
                        eraseColorFromButtons();
                        btnVoltaje.setBackgroundColor(Color.LTGRAY);
                        FragmentTransaction transaction = getFragmentManager().beginTransaction();
                        transaction.replace(R.id.fragment_container, voltFrag);
                        transaction.addToBackStack(null);
                        transaction.commit();
                    } catch (Exception e) {
                    }
                }else{
                    showToast("Bluetooth desconectado");
                }
                break;

            case R.id.btnConfiguracion:
                if (connected){
                    try {
                        eraseColorFromButtons();
                        btnConfiguracion.setBackgroundColor(Color.LTGRAY);
                        FragmentTransaction transaction2 = getFragmentManager().beginTransaction();
                        transaction2.replace(R.id.fragment_container, conFrag);
                        transaction2.addToBackStack(null);
                        transaction2.commit();
                    } catch (Exception e) {
                    }
                }else{
                    showToast("Bluetooth desconectado");
                }
                break;

            case R.id.btnDiagnostico:
                if (connected){
                    try {
                        eraseColorFromButtons();
                        btnDiagnostico.setBackgroundColor(Color.LTGRAY);
                        FragmentTransaction transaction3 = getFragmentManager().beginTransaction();
                        transaction3.replace(R.id.fragment_container, diagFrag);
                        transaction3.addToBackStack(null);
                        transaction3.commit();
                    } catch (Exception e) {
                    }
                }else{
                    showToast("Bluetooth desconectado");
                }
                break;

            case R.id.btnAjuste:
                if (connected){
                    showDialog("Ingrese la contraseña", "");
                }else{
                    showToast("Bluetooth desconectado");
                }
                break;

        }
    }

    public void showToast(final String toast)
    {
        runOnUiThread(new Runnable() {
            public void run()
            {
                Toast.makeText(MainActivity.this, toast, Toast.LENGTH_SHORT).show();
            }
        });
    }

    //Identifica el device BT
    public boolean BTinit()
    {
        boolean found = false;
        BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if(bluetoothAdapter == null) //Checks if the device supports bluetooth
        {
            Toast.makeText(getApplicationContext(), "Este dispositivo no soporta bluetooth", Toast.LENGTH_SHORT).show();
        }
        if(!bluetoothAdapter.isEnabled()) //Checks if bluetooth is enabled. If not, the program will ask permission from the user to enable it
        {
            Intent enableAdapter = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableAdapter,0);
            try
            {
                Thread.sleep(1000);
            }
            catch(InterruptedException e)
            {
                e.printStackTrace();
            }
        }
        Set<BluetoothDevice> bondedDevices = bluetoothAdapter.getBondedDevices();
        if(bondedDevices.isEmpty()) //Checks for paired bluetooth devices
        {
            Toast.makeText(getApplicationContext(), "Favor de conectar un dispositivo", Toast.LENGTH_SHORT).show();
        }
        else
        {
            for(BluetoothDevice iterator : bondedDevices)
            {

                //Suponiendo que solo haya un bondedDevice
                device = iterator;
                found = true;
                //Toast.makeText(getApplicationContext(), "Conectado a: " + device.getName(), Toast.LENGTH_SHORT).show();
            }
        }
        return found;
    }
    //Conexión al device BT
    public boolean BTconnect()
    {
        try
        {
            progressBarVisible();
            conectar();

        }
        catch(IOException e)
        {
            Toast.makeText(getApplicationContext(), "Conexión no exitosa", Toast.LENGTH_SHORT).show();
            e.printStackTrace();
            connected = false;
        }

        return connected;
    }

    public void conectar() throws IOException{
        socket = device.createRfcommSocketToServiceRecord(PORT_UUID); //Crea un socket para manejar la conexión
        socket.connect();
        socketConectado = true;
        Log.d("Socket ", String.valueOf(socket.isConnected()));
        Toast.makeText(getApplicationContext(), "Conexión exitosa", Toast.LENGTH_SHORT).show();
        connected = true;
        tvConect.setText("Conectado a " + device.getName());
        progressBarHide();
        btnConnect.setText("Desconectar módulo Bluetooth");
        outputStream = socket.getOutputStream();
        inputStream = socket.getInputStream();
        beginListenForData();

        //waitMs(5000);
        //closeSocket();

    }

    public void desconectarBluetooth() throws IOException{
        //Desconectar bluetooth
        if(socketConectado){
            progressBarVisible();
            System.out.println("Socket Conectado");
            outputStream.close();
            outputStream = null;
            inputStream.close();
            inputStream = null;
            socket.close();
            socket = null;
        }
        resetFields();
        connected = false;
        progressBarHide();
        tvConect.setText("");
        btnConnect.setText("Conectar a módulo Bluetooth");
        device = null;
        stopThread = true;
        socketConectado = false;




    }

    public void resetFields(){
        if (DiagnosticoBool && ConfiguracionBool){
            resetConfiguration();
            resetDiagnostico();
            resetVoltaje();

        }else if(DiagnosticoBool){
            resetVoltaje();
            resetDiagnostico();

        }else if(ConfiguracionBool){
            resetVoltaje();
            resetConfiguration();
        }else{
            resetVoltaje();
        }
         tvConect.setText("");
        eraseColorFromButtons();
    }

    public void resetVoltaje(){
        voltFrag.phase1RadioGroup.clearCheck();
        voltFrag.phase2RadioGroup.clearCheck();
        voltFrag.phase3RadioGroup.clearCheck();
        //voltFrag.switchVoltajeAutomatico.setChecked(false);
        voltFrag.switchLocalRemoto.setChecked(false);
        //voltFrag.tvVoltajeAutomatico.setText("");
        voltFrag.tvLocalRemoto.setText("");
        voltFrag.tvVoltaje.setText("");
        voltFrag.tvSenal.setText("0");
        voltFrag.tvPaquetes.setText("0");
        voltFrag.tvRSSI.setText("0");
        voltFrag.tvBloqueo.setText("");
    }

    public void resetConfiguration(){
        conFrag.destinationAddressTextInput.setText("");
        conFrag.maximumVoltageTextInput.setText("");
        conFrag.minimumVoltageTextInput.setText("");
        conFrag.sourceAddressTextInput.setText("");
        //conFrag.voltageControlCheckbox.setChecked(false);

    }

    public void resetDiagnostico(){
        diagFrag.etNetID.setText("");
        diagFrag.etNodeID.setText("");
        diagFrag.etPotencia.setText("");
        diagFrag.hideSpinners();
        diagFrag.tvUno.setVisibility(View.GONE);
        diagFrag.tvDos.setVisibility(View.GONE);
        /*
        diagFrag.tvRuidoRemoto.setText("");
        diagFrag.tvRuidoLocal.setText("");
        diagFrag.tvRSSIRemoto.setText("");
        diagFrag.tvRSSILocal.setText("");
        */
    }

    public void initItems(){
        controlPassword = "OK";
        connected = false;
        RadOn = true;
        //manOn = true;
        diagnostico = true;
        tvConect = (TextView)findViewById(R.id.tvConnect);
    }

    void beginListenForData() {
        stopThread = false;
        thread = new Thread(new Runnable() {
            public void run() {
                while(!Thread.currentThread().isInterrupted() && !stopThread) {
                    try {
                        //waitMs(1000);
                        final int byteCount = inputStream.available();
                        if(byteCount > 0) {
                            byte[] packetBytes = new byte[byteCount];
                            inputStream.read(packetBytes);
                            s = new String(packetBytes);
                            System.out.println("Linea: " + s);

                            handler.post(new Runnable() {
                                @Override
                                public void run() {
                                    //Status
                                    if(s.contains("s,") && s.contains("&")){
                                        if(s.length() >= 37 ){
                                            a = s.substring(s.indexOf("s,"), s.length() - 1);
                                            System.out.println("A: " + a);
                                            readMessage(a);
                                        }
                                    }
                                    //Config
                                    if(s.contains("g") && s.contains("&") ){
                                        //System.out.println("Index of : " + s.indexOf("g"));
                                        System.out.println("Sí la mandé  !");
                                        b = s.substring(s.indexOf("g"), s.indexOf("g") + 31);
                                        readMessage(b);

                                    }
                                    //RSSI
                                    if(s.contains("RSSI") && s.contains("L/R") ){
                                        System.out.println("Leí RSSI");
                                        try
                                        {
                                            readRSSI(s);

                                        }
                                        catch (IOException ex) { }
                                    }
                                    //GPS
                                    if(s.contains("S,")){
                                        System.out.println("Leí GPS");
                                        if(s.contains("&")){
                                            System.out.println("Sí contiene &");
                                            readGPS(s);
                                        }else{
                                            System.out.println("No contiene &");
                                            try
                                            {
                                                askGPS();
                                            }
                                            catch (IOException ex) { }

                                        }

                                    }
                                    //Password broadcast
                                    if(control.equals("Pass")){
                                        System.out.println("Control matches Pass");
                                        readPass(s);
                                        changeStatus();
                                    }

                                    //Atributos individuales
                                    if(control.matches("Config")){
                                        System.out.println("Config");
                                        System.out.println("Este es el atributo: " + atributo);
                                        switch (atributo){
                                            case "Power":
                                                if(s.length() >= 6 && s.contains("ANT2")){
                                                    readPower(s);
                                                }else{
                                                    //System.out.println("No es lo que quiero de Power" + s.length());
                                                    //System.out.println(s);
                                                }
                                                break;
                                            case "NetID":
                                                if(s.length() >= 7 && s.contains("ANT1")){
                                                    readNetID(s);
                                                }else{
                                                    //System.out.println("No es lo que quiero de NetID " + s.length());
                                                    //System.out.println(s);
                                                }
                                                break;
                                            case "NodeID":
                                                if(s.length() >= 7 && s.contains("ID")){
                                                    readNodeID(s);
                                                }else{
                                                    //System.out.println("No es lo que quiero de NodeID " + s.length());
                                                    //System.out.println(s);
                                                }
                                                break;
                                            case "S10":
                                                if(s.length() >= 7 && s.contains("COORD")){
                                                    readS10(s);
                                                }else{
                                                    //System.out.println("No es lo que quiero de S10 " + s.length());
                                                    //System.out.println(s);
                                                }
                                                break;
                                            case "S11":
                                                if(s.length() >= 7 && s.contains("NODO")){
                                                    readS11(s);
                                                }else{
                                                    //System.out.println("No es lo que quiero de S11 " + s.length());
                                                    //System.out.println(s);
                                                }
                                                break;
                                            case "S12":
                                                if(s.length() >= 7 && s.contains("NODO")){
                                                    readS12(s);
                                                }else{
                                                    //System.out.println("No es lo que quiero de S12 " + s.length());
                                                    //System.out.println(s);
                                                }
                                                break;
                                            case "S13":
                                                if(s.length() >= 7 && s.contains("COORD")){
                                                    readS13(s);
                                                }else{
                                                    //System.out.println("No es lo que quiero de S13 " + s.length());
                                                    //System.out.println(s);
                                                }
                                                break;
                                        }
                                    }



                                }
                            });
                        }
                    }
                    catch (IOException ex) {
                        stopThread = true;
                    }
                }
                System.out.println("Stop thread es true");
            }
        });
        thread.start();
    }

    public static void readMessage(String frase)  {
        System.out.println("Estoy en el readMessage: ");
        System.out.println(frase);
        tokens = frase.split(",");
        System.out.println("Tokens: " + Arrays.toString(tokens));
        if (frase.contains("s") && tokens.length >= 8) {
            //System.out.println("Contains Status : ");
            float currentVoltage;
            int phase1State, phase2State, phase3State, paquetes, rssi, senal;
            int phase1Transition, phase2Transition, phase3Transition, flagManOn;
            int currentVoltageControl, bloqueoControl;
            currentVoltage = Float.parseFloat(tokens[1]);
            voltFrag.tvVoltaje.setText(Float.toString(currentVoltage) + " V");
            System.out.println("Se cambió el voltaje a: " + Float.toString(currentVoltage));
            phase1State = Integer.parseInt(tokens[2]);
            updateLabels(1, phase1State);
            if (phase1State == 0){
                voltFrag.phase1OpenButton.setChecked(true);
            } else{
                voltFrag.phase1CloseButton.setChecked(true);
            }
            flagManOn = Integer.parseInt(tokens[3]);
            if(flagManOn == 1){
                //System.out.println("El BT dice que el ManOn es True");
                //manOn = true;
                //voltFrag.btnManOn.setText(R.string.ManOff);
                //enableButtons();
                voltFrag.switchLocalRemoto.setChecked(true);
                voltFrag.tvLocalRemoto.setText("Local");


            }else{
                //System.out.println("El BT dice que el ManOn es False");
                //manOn = false;
                //voltFrag.btnManOn.setText(R.string.ManOn);
                //disableButtons();
                voltFrag.switchLocalRemoto.setChecked(false);
                voltFrag.tvLocalRemoto.setText("Remoto");

            }

            phase2State = Integer.parseInt(tokens[4]);
            updateLabels(2, phase2State);

            if (phase2State == 0)
                voltFrag.phase2OpenButton.setChecked(true);
            else
                voltFrag.phase2CloseButton.setChecked(true);

            //phase2Transition = Integer.parseInt(tokens[5]);

            phase3State = Integer.parseInt(tokens[6]);
            updateLabels(3, phase3State);

            if (phase3State == 0)
                voltFrag.phase3OpenButton.setChecked(true);
            else
                voltFrag. phase3CloseButton.setChecked(true);

            //phase3Transition = Integer.parseInt(tokens[7]);

            /*
            currentVoltageControl = Integer.parseInt(tokens[8]);

            if (currentVoltageControl == 0) {
                voltFrag.switchVoltajeAutomatico.setChecked(false);
                voltFrag.tvVoltajeAutomatico.setText("Manual");

            }
            else {
                voltFrag.switchVoltajeAutomatico.setChecked(true);
                voltFrag.tvVoltajeAutomatico.setText("Control por Voltaje");
            }
            */

            bloqueoControl = Integer.parseInt(tokens[9]);
            System.out.println("Esto es lo que tiene el bloqueo: " + bloqueoControl);
            //Modificar el label del bloqueo y control de los botones
            if(bloqueoControl == 1){
                voltFrag.tvBloqueo.setText("Bloqueado");
                //disableButtons();
                if(controlBloqueado == 0){
                    voltFrag.createTimer();
                    controlBloqueado = 1;
                }


            }else{
                voltFrag.tvBloqueo.setText("");
                //enableButtons();
                controlBloqueado = 0;
            }
            paquetes = Integer.parseInt(tokens[10]);
            voltFrag.tvPaquetes.setText(Integer.toString(paquetes));
            rssi = Integer.parseInt(tokens[11]);
            voltFrag.tvRSSI.setText(Integer.toString(rssi));
            senal = Integer.parseInt(tokens[12]);
            voltFrag.tvSenal.setText(Integer.toString(senal));



            waitAndEraseLabels();


        } else if (frase.contains("g") && tokens.length >= 6) {

            System.out.println("RECIBÍ CONFIG");
            System.out.println("Tokens: " + Arrays.toString(tokens));
            int currentSourceAddress, currentDestinationAddress;
            int currentVoltageControl;
            float currentMinimumVoltage, currentMaximumVoltage, currentNivelAC;

            currentSourceAddress = Integer.parseInt(tokens[1]);
            conFrag.sourceAddressTextInput.setText(String.format(Locale.ENGLISH, "%05d", currentSourceAddress));

            currentDestinationAddress = Integer.parseInt(tokens[2]);
            conFrag. destinationAddressTextInput.setText(String.format(Locale.ENGLISH, "%05d", currentDestinationAddress));

            /*
            currentVoltageControl = Integer.parseInt(tokens[3]);
            if (currentVoltageControl == 0) {
                conFrag.voltageControlCheckbox.setChecked(false);
                voltFrag.switchVoltajeAutomatico.setChecked(false);
            }
            else {
                conFrag.voltageControlCheckbox.setChecked(true);
                voltFrag.switchVoltajeAutomatico.setChecked(true);

            }
            */

            currentMinimumVoltage = Float.parseFloat(tokens[3]);
            conFrag.minimumVoltageTextInput.setText(String.format(Locale.ENGLISH, "%1.1f", currentMinimumVoltage));

            currentMaximumVoltage = Float.parseFloat(tokens[4]);
            conFrag.maximumVoltageTextInput.setText(String.format(Locale.ENGLISH, "%1.1f", currentMaximumVoltage));

            currentNivelAC = Float.parseFloat(tokens[5]);
            conFrag.nivelACInput.setText(String.format(Locale.ENGLISH, "%1.1f", currentNivelAC));
        }
    }

    public void readRSSI(String line) throws IOException{
        System.out.println("Estoy en el readRssi");
        System.out.println("Esta es la linea: " + line);
        //Para evitar leer un string equivocado
        if(line.contains("[") && line.lastIndexOf("[") > 5 && line.length() > 7){
            String linea = line.substring(line.indexOf("RSSI:"), line.lastIndexOf("["));
            System.out.println("Esta es la linea buena: " + linea);

            String filtered = linea.replaceAll("[^0-9,/]","");
            System.out.println("Esta es filtered: " + filtered);
            tokensRSSI = filtered.split("/");
            System.out.println("Estos son los tokens del rssi: ");
            System.out.println(Arrays.toString(tokensRSSI));

            System.out.println("Este es el size del tokensRSSI: " + tokensRSSI.length);

            if(tokensRSSI.length == 4){
                diagFrag.tvRSSILocal.setText(tokensRSSI[0]);
                diagFrag.tvRSSIRemoto.setText(tokensRSSI[1]);
                diagFrag.tvRuidoLocal.setText(tokensRSSI[2]);
                diagFrag.tvRuidoRemoto.setText(tokensRSSI[3]);
            }

        }



    }

    public static void updateLabels(int pos, int state){
        switch (pos){
            case 1:
                if(state == 1){
                    //System.out.println("State 1");
                    voltFrag.phase1TransitionTextView.setText(R.string.opening_label);
                }else {
                    //System.out.println("State 0");
                    voltFrag.phase1TransitionTextView.setText(R.string.closing_label);
                }
                break;

            case 2:
                if(state == 1){
                    voltFrag.phase2TransitionTextView.setText(R.string.opening_label);

                }else {
                    voltFrag.phase2TransitionTextView.setText(R.string.closing_label);

                }
                break;
            case 3:
                if(state == 1){
                    voltFrag.phase3TransitionTextView.setText(R.string.opening_label);

                }else {
                    voltFrag.phase3TransitionTextView.setText(R.string.closing_label);

                }
                break;
        }
    }
    public static void waitAndEraseLabels(){
        try{
            //waitMs(1000);
            voltFrag.phase1TransitionTextView.setText("");
            voltFrag.phase2TransitionTextView.setText("");
            voltFrag.phase3TransitionTextView.setText("");


        }catch (Exception e) {
            System.out.println(e);
        }
    }

    public static void waitMs(int ms) throws IOException{
        try {
            // thread to sleep for ms milliseconds
            //SystemClock.sleep(ms);
            Thread.sleep(ms);
        } catch (Exception e) {
            System.out.println(e);
        }
    }

    public static void disableButtons(){
        voltFrag.closeButton.setEnabled(false);
        voltFrag.openButton.setEnabled(false);
        //dos switches
        //voltFrag.switchLocalRemoto.setClickable(false);
        //voltFrag.switchVoltajeAutomatico.setClickable(false);

    }

    public static void enableButtons(){
        //System.out.println("Estoy en enable buttons");
        voltFrag.closeButton.setEnabled(true);
        voltFrag.openButton.setEnabled(true);
        //voltFrag.closeButton.setClickable(true);
        //voltFrag.openButton.setClickable(true);
        //voltFrag.switchVoltajeAutomatico.setClickable(false);

    }

    void sendManOn() throws IOException
    {
        System.out.println("Estoy en el ManOn");
        //manOn = true;
        String msg = "$ManOn&";
        outputStream.write(msg.getBytes());
        //Wait para evitar que se cambie solo
        waitMs(2000);

    }

    void sendManOff() throws IOException
    {
        System.out.println("Estoy en el ManOff");
        //manOn = false;
        String msg = "$ManOff&";
        outputStream.write(msg.getBytes());
        waitMs(2000);
    }

    void sendStop() throws IOException{
        System.out.println("Estoy en sendStop");
        String msg = "$Parar&";
        outputStream.write(msg.getBytes());
        outputStream.write(msg.getBytes());
        outputStream.write(msg.getBytes());
        outputStream.write(msg.getBytes());

    }

    void sendOpen() throws IOException
    {
        System.out.println("Estoy en el Open");
        String msg = "$Abrir&";
        outputStream.write(msg.getBytes());
    }
    void sendClose() throws IOException
    {
        System.out.println("Estoy en el Close");
        String msg = "$Cerrar&";
        outputStream.write(msg.getBytes());
    }

    void sendLocation(String location) throws IOException
    {
        System.out.println("Estoy en el sendLocation");
        String msg = location;
        outputStream.write(msg.getBytes());
    }

    void askGPS() throws IOException
    {
        if(connected){
            control = "GPS";
            System.out.println("Estoy en el askGPS");
            String msg = "$GPS?,&";
            outputStream.write(msg.getBytes());
        }else{
            System.out.println("Desconectado y se tratará de conectar otra vez");
            BTconnect();
        }

    }

    void sendPassword(String pass) throws IOException
    {
        System.out.println("El control = Pass");
        control = "Pass";
        System.out.println("Estoy en el sendPassword");
        String msg = "$PASS=" + pass + ",& ";
        System.out.println("Este es el pass que mando " + msg);
        outputStream.write(msg.getBytes());
    }




    public void editSourceAddress(View view)
    {
        System.out.println("Estoy en el editSourceAddress");

        if(connected)
        {
            Bundle arguments = new Bundle();
            arguments.putInt("field", SOURCE_ADDRESS);
            arguments.putString("inputText", getString(R.string.sourceAddress_label));
            arguments.putString("inputHint", getString(R.string.sourceAddress_hint));
            arguments.putInt("inputType", InputType.TYPE_CLASS_NUMBER);
            arguments.putInt("minValue", 1);
            arguments.putInt("maxValue", 65535);

            DialogFragment dialog;

            dialog = GenericInputDialog.newInstance(arguments);
            dialog.show(getFragmentManager(), "generic_input");
        }
        else
        {
            Toast.makeText(getApplicationContext(), getString(R.string.disconnected_message), Toast.LENGTH_SHORT).show();
        }
    }

    public void editDestinationAddress(View view)
    {
        System.out.println("Estoy en el editDestinationAddress");

        if(connected)
        {
            Bundle arguments = new Bundle();
            arguments.putInt("field", DESTINATION_ADDRESS);
            arguments.putString("inputText", getString(R.string.destinationAddress_label));
            arguments.putString("inputHint", getString(R.string.destinationAddress_hint));
            arguments.putInt("inputType", InputType.TYPE_CLASS_NUMBER);
            arguments.putInt("minValue", 1);
            arguments.putInt("maxValue", 65535);

            DialogFragment dialog;

            dialog = GenericInputDialog.newInstance(arguments);
            dialog.show(getFragmentManager(), "generic_input");
        }
        else
        {
            Toast.makeText(getApplicationContext(), getString(R.string.disconnected_message), Toast.LENGTH_SHORT).show();
        }
    }

    public void toggleVoltageControl(View view)
    {
        if(!connected)
        {
            Toast.makeText(getApplicationContext(), getString(R.string.disconnected_message), Toast.LENGTH_SHORT).show();
        }
    }

    public void editMinimumVoltage(View view)
    {
        if(connected)
        {
            Bundle arguments = new Bundle();
            arguments.putInt("field", MINIMUM_VOLTAGE);
            arguments.putString("inputText", getString(R.string.minimumVoltage_label));
            //arguments.putString("inputHint", getString(R.string.destinationAddress_hint));
            arguments.putInt("inputType", InputType.TYPE_CLASS_NUMBER);
            arguments.putInt("minValue", 0);
            arguments.putInt("maxValue", 1000);

            DialogFragment dialog;

            dialog = GenericInputDialog.newInstance(arguments);
            dialog.show(getFragmentManager(), "generic_input");
        }
        else
        {
            Toast.makeText(getApplicationContext(), getString(R.string.disconnected_message), Toast.LENGTH_SHORT).show();
        }
    }

    public void editMaximumVoltage(View view)
    {
        if(connected)
        {
            Bundle arguments = new Bundle();
            arguments.putInt("field", MAXIMUM_VOLTAGE);
            arguments.putString("inputText", getString(R.string.maximumVoltage_label));
            //arguments.putString("inputHint", getString(R.string.destinationAddress_hint));
            arguments.putInt("inputType", InputType.TYPE_CLASS_NUMBER);
            arguments.putInt("minValue", 0);
            arguments.putInt("maxValue", 1000);

            DialogFragment dialog;

            dialog = GenericInputDialog.newInstance(arguments);
            dialog.show(getFragmentManager(), "generic_input");
        }
        else
        {
            Toast.makeText(getApplicationContext(), getString(R.string.disconnected_message), Toast.LENGTH_SHORT).show();
        }
    }

    public void editNivelAC(View view)
    {
        if(connected)
        {
            Bundle arguments = new Bundle();
            arguments.putInt("field", AC_VOLTAGE);
            arguments.putString("inputText", "Nivel AC:");
            arguments.putInt("inputType", InputType.TYPE_CLASS_NUMBER);
            arguments.putInt("minValue", 0);
            arguments.putInt("maxValue", 1000);

            DialogFragment dialog;

            dialog = GenericInputDialog.newInstance(arguments);
            dialog.show(getFragmentManager(), "generic_input");
        }
        else
        {
            Toast.makeText(getApplicationContext(), getString(R.string.disconnected_message), Toast.LENGTH_SHORT).show();
        }
    }
    public void programConfiguration()
    {
        int newSourceAddress, newDestinationAddress;
        float newMinimumVoltage, newMaximumVoltage, newNivelAC;
        String stringToSend;
        if(connected)
        {
            try {
                if(!conFrag.sourceAddressTextInput.getText().toString().matches("") && !conFrag.destinationAddressTextInput.getText().toString().matches("") && !conFrag.minimumVoltageTextInput.getText().toString().matches("") && !conFrag.maximumVoltageTextInput.getText().toString().matches(""))
                {
                    // not null not empty
                    newSourceAddress = Integer.parseInt(conFrag.sourceAddressTextInput.getText().toString());
                    newDestinationAddress = Integer.parseInt(conFrag.destinationAddressTextInput.getText().toString());
                    //newVoltageControl = (conFrag.voltageControlCheckbox.isChecked() ? 1 : 0);
                    newMinimumVoltage = Float.parseFloat(conFrag.minimumVoltageTextInput.getText().toString());
                    newMaximumVoltage = Float.parseFloat(conFrag.maximumVoltageTextInput.getText().toString());
                    newNivelAC = Float.parseFloat(conFrag.nivelACInput.getText().toString());


                    stringToSend = String.format(Locale.ENGLISH, "$Config,%04d,%04d,%1.1f,%1.1f,%1.1f,&",
                            newSourceAddress, newDestinationAddress, newMinimumVoltage, newMaximumVoltage, newNivelAC);
                    if(socket.isConnected()){
                        System.out.println("Este es el string que mando: " + stringToSend);
                        outputStream.write(stringToSend.getBytes());
                        //waitMs(10);
                        //Guardar configuración
                        saveValues();
                        //showToast("Configuración Enviada");
                    }else{
                        showToast("Conexión Perdida");

                    }

                }else {
                    //null or empty
                    showToast("Favor de llenar todos los datos");
                }
            } catch (Exception e)
            {
                Toast.makeText(getApplicationContext(), getString(R.string.invalidConfig_message), Toast.LENGTH_SHORT).show();
            }

        }
        else
        {
            Toast.makeText(getApplicationContext(), getString(R.string.disconnected_message), Toast.LENGTH_SHORT).show();
        }
    }


    public void updateConfigField(int field, float value)
    {
        switch(field)
        {
            case SOURCE_ADDRESS:
                conFrag.sourceAddressTextInput.setText(String.format(Locale.ENGLISH,"%05d", (int)value));
                break;
            case DESTINATION_ADDRESS:
                conFrag.destinationAddressTextInput.setText(String.format(Locale.ENGLISH,"%05d", (int)value));
                break;
            case VOLTAGE_CONTROL_CHECKBOX:
                if((int)value == 0)
                    conFrag.voltageControlCheckbox.setChecked(false);
                else
                    conFrag.voltageControlCheckbox.setChecked(true);
                break;
            case MINIMUM_VOLTAGE:
                conFrag.minimumVoltageTextInput.setText(String.format(Locale.ENGLISH,"%1.1f", value));
                break;
            case MAXIMUM_VOLTAGE:
                conFrag.maximumVoltageTextInput.setText(String.format(Locale.ENGLISH,"%1.1f", value));
                break;
            case AC_VOLTAGE:
                conFrag.nivelACInput.setText(String.format(Locale.ENGLISH,"%1.1f", value));
                break;
        }
    }
    void sendRadOnR() throws IOException {
        Runnable r = new Runnable() {
            @Override
            public void run() {
                try {
                    System.out.println("Estoy en el RadOnR");
                    //Para evitar que siga mandando la cadena y poder entrar al radio
                    String msg1 = "$RadOn,&";
                    outputStream.write(msg1.getBytes());
                } catch (IOException ex) {
                }
            }
        };
        Handler h = new Handler();
        h.postDelayed(r, 20);
    }


    void sendRssiR() throws IOException {
        Runnable r = new Runnable() {
            @Override
            public void run() {
                try {
                    System.out.println("Estoy en el SendRssiR");
                    //Para evitar que siga mandando la cadena y poder entrar al radio
                    String msg1 = "AT&T=RSSI\r";
                    outputStream.write(msg1.getBytes());
                } catch (IOException ex) {
                }
            }
        };
        Handler h = new Handler();
        h.postDelayed(r, 600);
    }

    void sendCommand() throws IOException {
        Runnable r = new Runnable() {
            @Override
            public void run() {
                try {
                    System.out.println("Estoy en sendCommand");
                    String msg = "+++";
                    outputStream.write(msg.getBytes());
                } catch (IOException ex) {
                }
            }
        };
        Handler h = new Handler();
        h.postDelayed(r, 50);
    }

    public void sendRadOn() throws IOException{
        System.out.println("Estoy en sendRadOn");
        String msg = "$RadOn,&";
        outputStream.write(msg.getBytes());
    }

    void sendRadOff() throws IOException {
        Runnable r = new Runnable() {
            @Override
            public void run() {
                try {
                    System.out.println("Estoy en el RadOff");
                    String msg1 = "$RadOff,&";
                    outputStream.write(msg1.getBytes());
                    outputStream.write(msg1.getBytes());
                } catch (IOException ex) {
                }
            }
        };
        Handler h = new Handler();
        h.postDelayed(r, 1000);
    }

    void sendNetID() throws IOException
    {
        Runnable r = new Runnable() {
            @Override
            public void run(){
                try
                {
                    //Control para saber qué leer
                    control = "Config";
                    //Lo que lee es sobre el netid
                    atributo = "NetID";
                    System.out.println("Estoy en Antena1");
                    String msg = "ATS14?\r";
                    outputStream.write(msg.getBytes());
                    //outputStream.write(msg.getBytes());
                    //outputStream.write(msg.getBytes());

                }
                catch (IOException ex) { }
            }
        };
        Handler h = new Handler();
        h.postDelayed(r, 1500);
    }

    void sendPower() throws IOException
    {
        Runnable r = new Runnable() {
            @Override
            public void run(){
                try
                {
                    atributo = "Power";
                    System.out.println("Estoy en Antena2");
                    String msg = "ATS15?\r";
                    outputStream.write(msg.getBytes());
                    //outputStream.write(msg.getBytes());
                }
                catch (IOException ex) { }
            }
        };
        Handler h = new Handler();
        h.postDelayed(r, 2000);
    }

    void sendNodeID() throws IOException
    {
        Runnable r = new Runnable() {
            @Override
            public void run(){
                try
                {
                    atributo = "NodeID";
                    System.out.println("Estoy en sendNodeID");
                    String msg = "ATS2?\r";
                    outputStream.write(msg.getBytes());
                    //outputStream.write(msg.getBytes());

                }
                catch (IOException ex) { }

            }
        };
        Handler h = new Handler();
        h.postDelayed(r, 3000);
    }
    void sendS10() throws IOException
    {
        Runnable r = new Runnable() {
            @Override
            public void run(){
                try
                {
                    atributo = "S10";
                    System.out.println("Estoy en sendS10");
                    String msg = "ATS10?\r";
                    outputStream.write(msg.getBytes());
                    //outputStream.write(msg.getBytes());

                }
                catch (IOException ex) { }

            }
        };
        Handler h = new Handler();
        h.postDelayed(r, 3500);
    }
    void sendS11() throws IOException
    {
        Runnable r = new Runnable() {
            @Override
            public void run(){
                try
                {
                    atributo = "S11";
                    System.out.println("Estoy en sendS11");
                    String msg = "ATS11?\r";
                    outputStream.write(msg.getBytes());
                    //outputStream.write(msg.getBytes());

                }
                catch (IOException ex) { }

            }
        };
        Handler h = new Handler();
        h.postDelayed(r, 4000);
    }
    void sendS12() throws IOException
    {
        Runnable r = new Runnable() {
            @Override
            public void run(){
                try
                {
                    atributo = "S12";
                    System.out.println("Estoy en sendS12");
                    String msg = "ATS12?\r";
                    outputStream.write(msg.getBytes());
                    //outputStream.write(msg.getBytes());

                }
                catch (IOException ex) { }

            }
        };
        Handler h = new Handler();
        h.postDelayed(r, 4500);
    }
    void sendS13() throws IOException
    {
        Runnable r = new Runnable() {
            @Override
            public void run(){
                try
                {
                    atributo = "S13";
                    System.out.println("Estoy en sendS13");
                    String msg = "ATS13?\r";
                    outputStream.write(msg.getBytes());
                    //outputStream.write(msg.getBytes());

                }
                catch (IOException ex) { }

            }
        };
        Handler h = new Handler();
        h.postDelayed(r, 5000);
    }

    void sendATZ() throws IOException
    {
        Runnable r = new Runnable() {
            @Override
            public void run(){
                try
                {
                    System.out.println("Estoy en sendATZ");
                    String msg = "ATZ\r";
                    outputStream.write(msg.getBytes());
                    outputStream.write(msg.getBytes());

                    //Send radoff para que siga mandando el string de status
                    sendRadOff();

                }
                catch (IOException ex) { }

            }
        };
        Handler h = new Handler();
        h.postDelayed(r, 1);
    }

    void sendATO() throws IOException
    {
        Runnable r = new Runnable() {
            @Override
            public void run(){
                try
                {
                    System.out.println("Estoy en sendATZ");
                    String msg = "ATZ\r";
                    outputStream.write(msg.getBytes());
                    outputStream.write(msg.getBytes());

                    //Send radoff para que siga mandando el string de status
                    sendRadOff();

                }
                catch (IOException ex) { }

            }
        };
        Handler h = new Handler();
        h.postDelayed(r, 10000);
    }

    void readS10(final String line){

        Runnable r = new Runnable() {
            @Override
            public void run(){
                //Siguente atributo a leer
                atributo = "S11";
                System.out.println("Esta es la linea que lee S10: " + line );
                if(line.length() > 5){
                    s10 = line.substring(line.lastIndexOf("COORD=") + 6, line.lastIndexOf("COORD=") + 7);

                    System.out.println("Esto tiene S10: " + s10);
                    diagFrag.spinner10.setSelection(Integer.valueOf(s10));
                }
            }
        };
        Handler h = new Handler();
        h.postDelayed(r, 80);
    }
    void readS11(final String line){

        Runnable r = new Runnable() {
            @Override
            public void run(){
                //Siguente atributo a leer
                atributo = "S12";
                System.out.println("Esta es la linea que lee S11: " + line );
                if(line.length() > 5){
                    s11 = line.substring(line.lastIndexOf("NODO=") + 5, line.lastIndexOf("NODO=") + 6);

                    System.out.println("Esto tiene s11: " + s11);
                    diagFrag.spinner11.setSelection(Integer.valueOf(s11));

                }
            }
        };
        Handler h = new Handler();
        h.postDelayed(r, 80);
    }void readS12(final String line){

        Runnable r = new Runnable() {
            @Override
            public void run(){
                //Siguente atributo a leer
                atributo = "S13";
                System.out.println("Esta es la linea que lee S12: " + line );
                if(line.length() > 5){
                    s12 = line.substring(line.lastIndexOf("NODO=") + 5, line.lastIndexOf("NODO=") + 6);

                    System.out.println("Esto tiene S12: " + s12);
                    diagFrag.spinner12.setSelection(Integer.valueOf(s12));
                }
            }
        };
        Handler h = new Handler();
        h.postDelayed(r, 80);
    }
    void readS13(final String line){

        Runnable r = new Runnable() {
            @Override
            public void run(){

                System.out.println("Esta es la linea que lee S13: " + line );
                if(line.length() > 5){
                    s13 = line.substring(line.lastIndexOf("COORD=") + 6, line.lastIndexOf("COORD=") + 7);

                    System.out.println("Esto tiene s13: " + s13);
                    diagFrag.spinner13.setSelection(Integer.valueOf(s13));

                }
                try
                {
                    //Ultimo atributo leído
                    sendATZ();
                    changeStatus();
                }
                catch (IOException ex) { }
            }
        };
        Handler h = new Handler();
        h.postDelayed(r, 80);
    }
    void readPower(final String line){
        Runnable r = new Runnable() {
            @Override
            public void run(){
                //Siguente atributo a leer
                atributo = "NodeID";
                System.out.println("Esta es la linea que lee Power: " + line );
                if(line.length() > 5){
                    String aux = line.substring(line.length() / 2, line.length());
                    System.out.println("Esto tiene aux: " + aux);
                    potenciaValue = aux.substring(aux.lastIndexOf("ANT2=") + 5, aux.lastIndexOf("\r"));
                    System.out.println("Esto tiene potenciaValue: " + potenciaValue);
                }
            }
        };
        Handler h = new Handler();
        h.postDelayed(r, 60);
    }

    void readNetID(final String line){

        Runnable r = new Runnable() {
            @Override
            public void run(){
                //Siguente atributo a leer
                atributo = "Power";
                System.out.println("Esta es la linea que lee NetID: " + line );
                if(line.length() > 5){
                    netIDValue = line.substring(line.lastIndexOf("ANT1=") + 5, line.length());

                    System.out.println("Esto tiene netIDvalue: " + netIDValue);
                }
            }
        };
        Handler h = new Handler();
        h.postDelayed(r, 80);
    }

    void readNodeID(final String line){
        Runnable r = new Runnable() {
            @Override
            public void run(){
                atributo = "S10";

                System.out.println("NodeID: " + line);
                System.out.println("Esta es la linea que lee NodeID: " + line );
                if(line.length() > 5 && line.contains("ID")){
                    nodeIDvalue = line.substring(line.lastIndexOf("\r") - 4, line.lastIndexOf("\r"));
                    System.out.println("Esto tiene nodeIDvalue: " + nodeIDvalue);
                    //Ya que se leyeron los datos, se acomodan en los edit texts al mismo tiempo
                    diagFrag.etNetID.setText(netIDValue);
                    diagFrag.etPotencia.setText(potenciaValue);
                    diagFrag.etNodeID.setText(nodeIDvalue);
                    System.out.println("Esto tiene etNodeID " + diagFrag.etNodeID.getText().toString());
                }

            }
        };
        Handler h = new Handler();
        h.postDelayed(r, 70);
    }

    void readGPS(final String line){
        System.out.println("Estoy en el readGPS: " + line );

        Runnable r = new Runnable() {
            @Override
            public void run(){

                System.out.println("Esta es la linea que lee GPS: " + line );
                //Checar si es las coordenadas default
                if(line.contains("25.6984") || line.contains("-100.3176")){

                    voltFrag.getLocation();
                    System.out.println("Mandé la nueva ubicación del GPS: ");

                }else{
                    System.out.println("Ya tiene una ubicación guardada diferente a la default");

                }

            }
        };
        Handler h = new Handler();
        h.postDelayed(r, 80);
    }

    void readPass(final String line){

        Runnable r = new Runnable() {
            @Override
            public void run(){

                System.out.println("Esta es la linea que lee Pass: " + line );
                if(line.contains("OK")){
                    //Password Correcto
                    controlPassword = "OK";
                }else{
                    //El password es incorrecto
                    //////controlPassword = "ERROR";
                }

            }
        };
        Handler h = new Handler();
        h.postDelayed(r, 500);
    }

    void changeStatus(){
        Runnable r = new Runnable() {
            @Override
            public void run() {
                control = "Status";
                System.out.println("Status changed");
            }
        };
        Handler h = new Handler();
        h.postDelayed(r, 400);
    }

    public void sendOffset() throws IOException{
        String msg = "$AjOff&";
        System.out.println("Estoy en sendOffset: " + msg);
        outputStream.write(msg.getBytes());
    }

    public void sendVoltaje(String voltaje) throws IOException{
        String msg = "$AjGan," + voltaje + ",&";
        System.out.println("Estoy en sendVoltaje: " + msg);
        outputStream.write(msg.getBytes());
    }

    public void configuraRadio(String netID, String nodeID, String potencia, String s10, String s11, String s12, String s13) throws IOException{
        System.out.println("Estoy en configura radio y estos son los valores: " + netID + " " + nodeID + " " + potencia + " " + s10 + " " + s11 + " " + s12 + " " + s13);
        try {
            if(Integer.parseInt(nodeID.trim()) == 0){
                System.out.println("NodeID igual a 0");
                destination = "65535";
            }else{
                System.out.println("NodeID diferente a 0");
                destination = "0";
            }
            sendRadOn();
            sendCommand();
            writeNodeID(nodeID);
            writeNodeID(nodeID);
            writePower(potencia);
            writeNetID(netID);
            //writeDestination(destination);
            //Valores de las antenas
            write10(s10);
            write11(s11);
            write12(s12);
            write13(s13);


            saveValues();
        } catch (IOException ex) {
        }
    }
    public void write13(final String s13) throws IOException{
        Runnable r = new Runnable() {
            @Override
            public void run(){

                try {
                    System.out.println("Estoy en el writeS13");
                    String msg1 = "ATS13=" + s13 + "\r";
                    System.out.println(msg1);
                    outputStream.write(msg1.getBytes());

                } catch (IOException ex) {
                }

            }
        };

        Handler h = new Handler();
        h.postDelayed(r, 1600);

    }
    public void write12(final String s12) throws IOException{
        Runnable r = new Runnable() {
            @Override
            public void run(){

                try {
                    System.out.println("Estoy en el writeS12");
                    String msg1 = "ATS12=" + s12 + "\r";
                    System.out.println(msg1);
                    outputStream.write(msg1.getBytes());

                } catch (IOException ex) {
                }

            }
        };

        Handler h = new Handler();
        h.postDelayed(r, 1500);

    }

    public void write11(final String s11) throws IOException{
        Runnable r = new Runnable() {
            @Override
            public void run(){

                try {
                    System.out.println("Estoy en el writeS11");
                    String msg1 = "ATS11=" + s11 + "\r";
                    System.out.println(msg1);
                    outputStream.write(msg1.getBytes());

                } catch (IOException ex) {
                }

            }
        };

        Handler h = new Handler();
        h.postDelayed(r, 1400);

    }

    public void write10(final String s10) throws IOException{
        Runnable r = new Runnable() {
            @Override
            public void run(){

                try {
                    System.out.println("Estoy en el writeS10");
                    String msg1 = "ATS10=" + s10 + "\r";
                    System.out.println(msg1);
                    outputStream.write(msg1.getBytes());

                } catch (IOException ex) {
                }

            }
        };

        Handler h = new Handler();
        h.postDelayed(r, 1300);

    }

    public void writeNetID(final String netID) throws IOException{
        Runnable r = new Runnable() {
            @Override
            public void run(){

                try {
                    System.out.println("Estoy en el WriteAntena1");
                    String msg1 = "ATS14=" + netID + "\r";
                    System.out.println(msg1);
                    outputStream.write(msg1.getBytes());

                } catch (IOException ex) {
                }

            }
        };

        Handler h = new Handler();
        h.postDelayed(r, 1200);

    }

    public void writeNodeID(final String nodeID) throws IOException{
        Runnable r = new Runnable() {
            @Override
            public void run(){

                try {
                    System.out.println("Estoy en el WriteNodeID");
                    String msg1 = "ATS2=" + nodeID + "\r";
                    System.out.println(msg1);
                    outputStream.write(msg1.getBytes());


                } catch (IOException ex) {
                }

            }
        };

        Handler h = new Handler();
        h.postDelayed(r, 1000);

    }

    public void writePower(final String power) throws IOException{
        Runnable r = new Runnable() {
            @Override
            public void run(){

                try {
                    System.out.println("Estoy en el WritePower");
                    String msg1 = "ATS15=" + power + "\r";
                    System.out.println(msg1);
                    outputStream.write(msg1.getBytes());
                    outputStream.write(msg1.getBytes());


                } catch (IOException ex) {
                }

            }
        };

        Handler h = new Handler();
        h.postDelayed(r, 1100);

    }

    public void writeDestination(final String destination) throws IOException{
        Runnable r = new Runnable() {
            @Override
            public void run(){

                try {
                    System.out.println("Estoy en el WriteDestination");
                    String msg1 = "ATS16=" + destination + "\r";
                    System.out.println(msg1);
                    outputStream.write(msg1.getBytes());

                } catch (IOException ex) {
                }

            }
        };
        Handler h = new Handler();
        h.postDelayed(r, 900);

    }

    public void saveValues() throws IOException{
        Runnable r = new Runnable() {
            @Override
            public void run(){

                try {
                    System.out.println("Estoy en el SaveValues");
                    String msg1 = "AT&W\r";
                    outputStream.write(msg1.getBytes());
                    sendRadOff();
                    showToast("¡ Configuración Guardada !");

                } catch (IOException ex) {
                }

            }
        };

        Handler h = new Handler();
        h.postDelayed(r, 1700);

    }

    @Override
    protected void onPause() {
        super.onPause();
        System.out.println("Estoy en onPause");
        /*
        if(connected){
            try{
                desconectarBluetooth();
            }catch (IOException e){
                System.out.println("Error: " + e);
            }
        }
        */
    }

    public void progressBarVisible(){
        System.out.println("PB Visible");
        progressBar.setVisibility(View.VISIBLE);
    }

    public void progressBarHide(){
        System.out.println("PB Gone");
        progressBar.setVisibility(View.GONE);
    }
    //Le quita el color a todos los botones cuando se desconecta
    public void eraseColorFromButtons(){
        btnVoltaje.setBackgroundResource(android.R.drawable.btn_default);
        btnConfiguracion.setBackgroundResource(android.R.drawable.btn_default);
        btnDiagnostico.setBackgroundResource(android.R.drawable.btn_default);
        btnAjuste.setBackgroundResource(android.R.drawable.btn_default);

    }

    //Input Dialog para ingresar el password del Fragmento de Ajuste
    public void showDialog(final String title, final String message){
        AlertDialog.Builder alert = new AlertDialog.Builder(this);
        final EditText edittext = new EditText(getApplicationContext());
        edittext.setInputType(InputType.TYPE_CLASS_NUMBER);
        edittext.setTextColor(getResources().getColor(R.color.black));
        edittext.setRawInputType(Configuration.KEYBOARD_12KEY);
        alert.setMessage(message);
        alert.setTitle(title);
        alert.setView(edittext);

        alert.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                String pass = edittext.getText().toString();
                if(pass.equals("1234")){
                    Toast.makeText(getApplicationContext(), "Correcto", Toast.LENGTH_SHORT).show();
                    //Presentar el fragmento
                    eraseColorFromButtons();
                    btnAjuste.setBackgroundColor(Color.LTGRAY);
                    FragmentTransaction transaction3 = getFragmentManager().beginTransaction();
                    transaction3.replace(R.id.fragment_container, ajusFrag);
                    transaction3.addToBackStack(null);
                    transaction3.commit();

                }else{
                    showDialog(title, "Incorrecto");
                }
            }
        });

        alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                // Cerrar el input dialog
                dialog.dismiss();
            }
        });

        alert.show();
    }

    //Input Dialog para ingresar el password para permitir el cambio a remoto
    public void showPasswordDialog(final String title, final String message){
        AlertDialog.Builder alert = new AlertDialog.Builder(this);
        final EditText edittext = new EditText(getApplicationContext());
        edittext.setInputType(InputType.TYPE_CLASS_NUMBER);
        edittext.setTextColor(getResources().getColor(R.color.black));
        edittext.setRawInputType(Configuration.KEYBOARD_12KEY);
        alert.setMessage(message);
        alert.setTitle(title);
        alert.setView(edittext);

        alert.setPositiveButton("Ingresar", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                String pass = edittext.getText().toString();
                //Pasar el pass con este comando = $PASS=12345,& regresa OK si es correcto o error si incorrecto
                try{
                    sendPassword(pass);
                }catch(IOException e){

                }
                if(controlPassword.matches("OK")){
                    Toast.makeText(getApplicationContext(), "Correcto", Toast.LENGTH_SHORT).show();
                    //eraseColorFromButtons();
                    try{
                        System.out.println("Estoy adentro del try del OK");
                        sendManOn();
                        voltFrag.tvLocalRemoto.setText("Local");
                        waitMs(1000);

                    }catch(IOException e){
                    }
                }else{
                    showDialog(title, "Password Inválido");
                    //controlPassword = "ERROR";
                    try{
                        sendManOff();
                        voltFrag.tvLocalRemoto.setText("Remoto");
                        waitMs(1000);
                    }catch(IOException e){
                    }
                }
            }
        });

        alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                // Cerrar el input dialog
                dialog.dismiss();
                //Regresate a Remoto
                try{
                    sendManOff();
                    voltFrag.tvLocalRemoto.setText("Remoto");
                    waitMs(1000);
                }catch(IOException e){

                }
            }
        });

        alert.show();
    }
}
