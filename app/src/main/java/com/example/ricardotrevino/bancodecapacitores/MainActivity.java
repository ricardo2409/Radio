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

    static String s;

    static String a;
    static String b;

    static Thread thread;
    static Handler handler = new Handler();
    static String control = "Status";



    static VoltajeFragment voltFrag;
    static ConfigurationFragment conFrag;
    static DiagnosticoFragment diagFrag;
    static AjusteFragment ajusFrag;

    Boolean DiagnosticoBool, ConfiguracionBool;
    ProgressBar progressBar;



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
                    }
                }else{
                    try
                    {
                        desconectarBluetooth();
                    }
                    catch (IOException ex) { }
                }
                break;

                /*
            case R.id.btnVoltaje:
                if (connected){
                    try {
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
                */
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
    }

    public void resetVoltaje(){
        voltFrag.phase1RadioGroup.clearCheck();
        voltFrag.phase2RadioGroup.clearCheck();
        voltFrag.phase3RadioGroup.clearCheck();
        voltFrag.switchVoltajeAutomatico.setChecked(false);
        voltFrag.switchLocalRemoto.setChecked(false);
        voltFrag.tvVoltajeAutomatico.setText("");
        voltFrag.tvLocalRemoto.setText("");
        voltFrag.tvVoltaje.setText("");
    }

    public void resetConfiguration(){
        conFrag.destinationAddressTextInput.setText("");
        conFrag.maximumVoltageTextInput.setText("");
        conFrag.minimumVoltageTextInput.setText("");
        conFrag.sourceAddressTextInput.setText("");
        conFrag.voltageControlCheckbox.setChecked(false);

    }

    public void resetDiagnostico(){
        diagFrag.etNetID.setText("");
        diagFrag.etNodeID.setText("");
        diagFrag.etPotencia.setText("");
        diagFrag.tvRuidoRemoto.setText("");
        diagFrag.tvRuidoLocal.setText("");
        diagFrag.tvRSSIRemoto.setText("");
        diagFrag.tvRSSILocal.setText("");
    }

    public void initItems(){
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
                            //try{ thread.sleep(100); }catch(InterruptedException e){ }
                            byte[] packetBytes = new byte[byteCount];
                            inputStream.read(packetBytes);
                            s = new String(packetBytes);
                            System.out.println("Linea: " + s);

                            handler.post(new Runnable() {
                                @Override
                                public void run() {
                                    //System.out.println("Estoy en el run");

                                    if(s.contains("s,") && s.contains("&")){
                                        if(s.length() >= 26 && s.length() <= 34){
                                            //System.out.println("S: " + s);
                                            a = s.substring(s.indexOf("s,"), s.length() - 1);
                                            System.out.println("A: " + a);
                                            //s = s.substring(s.indexOf('&'), s.length() - 1);
                                            //System.out.println("S: " + s);
                                            readMessage(a);
                                        }

                                    }
                                    if(s.contains("g") && s.contains("&") ){
                                        //System.out.println("Index of : " + s.indexOf("g"));
                                        System.out.println("Sí la mandé  !");
                                        b = s.substring(s.indexOf("g"), s.indexOf("g") + 29);
                                        readMessage(b);

                                    }

                                    if(s.contains("RSSI") ){
                                        System.out.println("Leí RSSI");
                                        try
                                        {
                                            readRSSI(s);

                                        }
                                        catch (IOException ex) { }
                                    }

                                }
                            });

                            //waitMs(1000);


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
            int phase1State, phase2State, phase3State;
            int phase1Transition, phase2Transition, phase3Transition, flagManOn;
            int currentVoltageControl;
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
                enableButtons();
                voltFrag.switchLocalRemoto.setChecked(true);
                voltFrag.tvLocalRemoto.setText("Local");


            }else{
                //System.out.println("El BT dice que el ManOn es False");
                //manOn = false;
                //voltFrag.btnManOn.setText(R.string.ManOn);
                disableButtons();
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

            currentVoltageControl = Integer.parseInt(tokens[8]);

            if (currentVoltageControl == 0) {
                //Cambiar el estado del Switch !!!!1
                voltFrag.switchVoltajeAutomatico.setChecked(false);
                voltFrag.tvVoltajeAutomatico.setText("Control por Voltaje");

            }
            else {
                //Cambiar el estado del Switch !!!!1
                voltFrag.switchVoltajeAutomatico.setChecked(true);
                voltFrag.tvVoltajeAutomatico.setText("Automático");
            }


            waitAndEraseLabels();


        } else if (frase.contains("g") && tokens.length >= 6) {

            System.out.println("RECIBÍ CONFIG");
            System.out.println("Tokens: " + Arrays.toString(tokens));
            int currentSourceAddress, currentDestinationAddress;
            int currentVoltageControl;
            float currentMinimumVoltage, currentMaximumVoltage;

            currentSourceAddress = Integer.parseInt(tokens[1]);
            conFrag.sourceAddressTextInput.setText(String.format(Locale.ENGLISH, "%05d", currentSourceAddress));

            currentDestinationAddress = Integer.parseInt(tokens[2]);
            conFrag. destinationAddressTextInput.setText(String.format(Locale.ENGLISH, "%05d", currentDestinationAddress));

            currentVoltageControl = Integer.parseInt(tokens[3]);
            if (currentVoltageControl == 0) {
                conFrag.voltageControlCheckbox.setChecked(false);
                voltFrag.switchVoltajeAutomatico.setChecked(false);
            }
            else {
                conFrag.voltageControlCheckbox.setChecked(true);
                voltFrag.switchVoltajeAutomatico.setChecked(true);

            }

            currentMinimumVoltage = Float.parseFloat(tokens[4]);
            conFrag.minimumVoltageTextInput.setText(String.format(Locale.ENGLISH, "%1.1f", currentMinimumVoltage));

            currentMaximumVoltage = Float.parseFloat(tokens[5]);
            conFrag.maximumVoltageTextInput.setText(String.format(Locale.ENGLISH, "%1.1f", currentMaximumVoltage));
        }
    }

    public void readRSSI(String line) throws IOException{
        System.out.println("Estoy en el readRssi");
        System.out.println("Esta es la linea: " + line);
        String filtered = line.replaceAll("[^0-9,/]","");
        System.out.println("Esta es filtered: " + filtered);
        String numeros = filtered.substring(0, filtered.length() - 6);
        System.out.println("Esta es numeros: " + numeros);
        tokensRSSI = numeros.split("/");
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
        voltFrag.switchVoltajeAutomatico.setClickable(false);

    }

    public static void enableButtons(){
        voltFrag.closeButton.setEnabled(true);
        voltFrag.openButton.setEnabled(true);
        voltFrag.switchVoltajeAutomatico.setClickable(false);

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
    public void programConfiguration()
    {
        int newSourceAddress, newDestinationAddress, newVoltageControl;
        float newMinimumVoltage, newMaximumVoltage;
        String stringToSend;
        if(connected)
        {
            try {
                if(!conFrag.sourceAddressTextInput.getText().toString().matches("") && !conFrag.destinationAddressTextInput.getText().toString().matches("") && !conFrag.minimumVoltageTextInput.getText().toString().matches("") && !conFrag.maximumVoltageTextInput.getText().toString().matches(""))
                {
                    // not null not empty
                    newSourceAddress = Integer.parseInt(conFrag.sourceAddressTextInput.getText().toString());
                    newDestinationAddress = Integer.parseInt(conFrag.destinationAddressTextInput.getText().toString());
                    newVoltageControl = (conFrag.voltageControlCheckbox.isChecked() ? 1 : 0);
                    newMinimumVoltage = Float.parseFloat(conFrag.minimumVoltageTextInput.getText().toString());
                    newMaximumVoltage = Float.parseFloat(conFrag.maximumVoltageTextInput.getText().toString());

                    stringToSend = String.format(Locale.ENGLISH, "$Config,%05d,%05d,%01d,%1.1f,%1.1f,&",
                            newSourceAddress, newDestinationAddress,
                            newVoltageControl, newMinimumVoltage, newMaximumVoltage);
                    if(socket.isConnected()){
                        outputStream.write(stringToSend.getBytes());
                        //waitMs(10);
                        //Guardar configuración
                        outputStream.write("AT&W".getBytes());
                        showToast("Configuración Enviada");
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
        }
    }

    public void sendCommand() throws IOException{
        System.out.println("Estoy en sendCommand");
        String msg = "+++";
        outputStream.write(msg.getBytes());
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
                    //Para evitar que siga mandando la cadena y poder entrar al radio
                    String msg1 = "$RadOff,&";
                    outputStream.write(msg1.getBytes());
                    changeStatus();
                } catch (IOException ex) {
                }
            }
        };
        Handler h = new Handler();
        h.postDelayed(r, 1000);
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

    public void sendNETID() throws IOException{
        System.out.println("Estoy en sendNetID");
        String msg = "ATS3?\r";
        outputStream.write(msg.getBytes());
    }

    public void sendNodeID() throws IOException{
        System.out.println("Estoy en sendNodeID");
        String msg = "ATS15?\r";
        outputStream.write(msg.getBytes());
    }

    public void sendPotencia() throws IOException{
        System.out.println("Estoy en sendPotencia");
        String msg = "ATS4?\r";
        outputStream.write(msg.getBytes());
    }

    public void sendOffset() throws IOException{
        System.out.println("Estoy en sendOffset");
        String msg = "$AjOff&";
        outputStream.write(msg.getBytes());
    }

    public void sendVoltaje(String voltaje) throws IOException{
        System.out.println("Estoy en sendVoltaje");
        String msg = "$AjGan," + voltaje + ",&";
        outputStream.write(msg.getBytes());
    }

    public void sendRssi() throws IOException{
        System.out.println("Estoy en sendRSSI");
        String comando = "+++";
        outputStream.write(comando.getBytes());
        //waitMs(10);
        String msg = "AT&T=RSSI\r";
        outputStream.write(msg.getBytes());
    }


    public void configuraRadio(String netID, String nodeID, String potencia) throws IOException{
        String msg = "ATS3=" + netID;
        outputStream.write(msg.getBytes());
        //waitMs(100);
        msg = "ATS15=" + nodeID;
        outputStream.write(msg.getBytes());
        //waitMs(100);
        msg = "ATS4=" + potencia;
        outputStream.write(msg.getBytes());
        //waitMs(100);
    }

    @Override
    protected void onPause() {
        super.onPause();
        System.out.println("Estoy en onPause");
        if(connected){
            try{
                desconectarBluetooth();
            }catch (IOException e){
                System.out.println("Error: " + e);
            }
        }
    }
    public void progressBarVisible(){
        System.out.println("PB Visible");
        progressBar.setVisibility(View.VISIBLE);
    }

    public void progressBarHide(){
        System.out.println("PB Gone");
        progressBar.setVisibility(View.GONE);
    }

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
                // what ever you want to do with No option.
                dialog.dismiss();
            }
        });

        alert.show();

    }






}
