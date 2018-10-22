package com.example.ricardotrevino.bancodecapacitores;

import android.Manifest;
import android.app.Fragment;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Address;
import android.location.Criteria;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static android.Manifest.permission.ACCESS_FINE_LOCATION;

/**
 * Created by ricardotrevino on 12/17/17.
 */

public class VoltajeFragment extends Fragment implements View.OnClickListener {

    TextView tvBloqueo, tvVoltaje, tvVoltajeControl, phase1TransitionTextView, phase2TransitionTextView, phase3TransitionTextView, tvLocalRemoto, tvVoltajeAutomatico, tvPaquetes, tvRSSI, tvSenal, tvTimer, tvBateria, tvTemperatura;
    Button openButton, closeButton;
    Switch switchLocalRemoto, switchVoltajeAutomatico;
    static RadioButton phase1OpenButton, phase1CloseButton, phase2OpenButton, phase2CloseButton, phase3OpenButton, phase3CloseButton;
    RadioGroup phase1RadioGroup, phase2RadioGroup, phase3RadioGroup;

    Geocoder geocoder;
    String bestProvider;
    List<Address> user = null;
    double lat;
    double lng;

    private FusedLocationProviderClient client;
    String latitude, longitud, latitudeUno, latitudeDos, longitudUno, longitudDos;
    static CountDownTimer mCountDownTimer;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.voltaje_fragment, null);
        requestPermission();
        //barChart = (BarChart) view.findViewById(R.id.graph);
        tvVoltaje = (TextView) view.findViewById(R.id.voltageTextView);
        tvBloqueo = (TextView) view.findViewById(R.id.tvBloqueo);
        phase1OpenButton = (RadioButton) view.findViewById(R.id.phase1OpenButton);
        phase1CloseButton = (RadioButton) view.findViewById(R.id.phase1ClosedButton);
        phase2OpenButton = (RadioButton) view.findViewById(R.id.phase2OpenButton);
        phase2CloseButton = (RadioButton) view.findViewById(R.id.phase2ClosedButton);
        phase3OpenButton = (RadioButton) view.findViewById(R.id.phase3OpenButton);
        phase3CloseButton = (RadioButton) view.findViewById(R.id.phase3ClosedButton);
        phase1RadioGroup = (RadioGroup) view.findViewById(R.id.phase1RadioGroup);
        phase2RadioGroup = (RadioGroup) view.findViewById(R.id.phase2RadioGroup);
        phase3RadioGroup = (RadioGroup) view.findViewById(R.id.phase3RadioGroup);
        //btnManOn = (Button) view.findViewById(R.id.ManOn);
        //tvVoltajeControl = (TextView) view.findViewById(R.id.voltageControlTextView);
        openButton = (Button) view.findViewById(R.id.openButton);
        openButton.setOnClickListener(this);
        closeButton = (Button) view.findViewById(R.id.closeButton);
        closeButton.setOnClickListener(this);
        //stopButton = (Button) view.findViewById(R.id.stopButton);

        phase1TransitionTextView = (TextView) view.findViewById(R.id.phase1TransitionTextView);
        phase2TransitionTextView = (TextView) view.findViewById(R.id.phase2TransitionTextView);
        phase3TransitionTextView = (TextView) view.findViewById(R.id.phase3TransitionTextView);

        tvLocalRemoto = (TextView) view.findViewById(R.id.tvLocalRemoto);
        tvPaquetes = (TextView) view.findViewById(R.id.tvPaquetes);
        tvRSSI = (TextView) view.findViewById(R.id.tvRSSI);
        tvSenal = (TextView) view.findViewById(R.id.tvSenal);
        tvTimer = (TextView) view.findViewById(R.id.tvTimer);
        tvBateria = (TextView) view.findViewById(R.id.tvBateria);
        tvTemperatura = (TextView) view.findViewById(R.id.tvTemperatura);


        //tvVoltajeAutomatico = (TextView) view.findViewById(R.id.tvVoltajeAutomatico);


        switchLocalRemoto = (Switch) view.findViewById(R.id.switchLocalRemoto);
        //switchVoltajeAutomatico = (Switch) view.findViewById(R.id.switchVoltajeAutomatico);


        switchLocalRemoto.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    //Cambio a Local
                    if (((MainActivity) getActivity()).connected) {
                        try {
                            System.out.println("Switch es True");
                            ((MainActivity) getActivity()).showPasswordDialog("Ingrese la Contraseña", "Cambio a Local");

                        } catch (Exception e) {
                            System.out.println("Error: " + e);
                        }
                    } else {
                        ((MainActivity) getActivity()).showToast("Bluetooth desconectado");
                    }

                } else {
                    //Cambio a Remoto
                    if (((MainActivity) getActivity()).connected) {
                        try {
                            System.out.println("Switch es False");
                            ((MainActivity) getActivity()).sendManOff();
                            tvLocalRemoto.setText("Remoto");
                            ((MainActivity) getActivity()).waitMs(1000);
                            //Reiniciar el timer
                            //IF blocked
                            if(MainActivity.bloqueoControl == 1){
                                System.out.println("Resetea el timer cuando cambia a remoto");
                                createTimer();
                            }
                        } catch (Exception e) {
                            System.out.println("Error: " + e);
                        }
                    } else {
                        ((MainActivity) getActivity()).showToast("Bluetooth desconectado");
                    }

                }
            }
        });

        //getLocation(); //Pide las coordenadas solo una vez

        /*
        ArrayList<BarEntry> barEntries = new ArrayList<>();
        barEntries.add(new BarEntry(2, 100));
        barEntries.add(new BarEntry(1,40));
        barEntries.add(new BarEntry(0,20));
        BarDataSet barDataSet = new BarDataSet(barEntries, "Datos");

        ArrayList<String> Tipos = new ArrayList<>();
        Tipos.add("Paquetes %");
        Tipos.add("Ruido");
        Tipos.add("Señal");


        BarData barData = new BarData(barDataSet);
        barChart.setData(barData);
        barChart.setTouchEnabled(false);
        barChart.setDragEnabled(false);
        barChart.setDrawGridBackground(true);
        barChart.setFitBars(true);
        */
        return view;
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.openButton:
                if (((MainActivity) getActivity()).connected) {
                    try {
                        System.out.println("Botón Abrir");
                        ((MainActivity) getActivity()).sendOpen();
                    } catch (Exception e) {
                        System.out.println("Error: " + e);
                    }
                } else {
                    ((MainActivity) getActivity()).showToast("Bluetooth desconectado");
                }

                break;

            case R.id.closeButton:
                if (((MainActivity) getActivity()).connected) {
                    try {
                        System.out.println("Botón Cerrar");
                        ((MainActivity) getActivity()).sendClose();
                    } catch (Exception e) {
                        System.out.println("Error: " + e);
                    }
                } else {
                    ((MainActivity) getActivity()).showToast("Bluetooth desconectado");
                }
                break;
        }

    }

    public void getLocation() {
        System.out.println("Estoy en el GetLocation");
        if (((MainActivity) getActivity()).connected) {
            try {
                System.out.println("Voy a pedir la ubicación en GetLocation");
                client = LocationServices.getFusedLocationProviderClient(getContext());
                if (ActivityCompat.checkSelfPermission(getActivity(), ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    System.out.println("Permiso no otorgado");

                    return;
                }else{
                    client.getLastLocation().addOnSuccessListener(getActivity(), new OnSuccessListener<Location>() {
                        @Override
                        public void onSuccess(Location location) {
                            System.out.println("Permiso Otorgado");
                            try {
                                latitude = Double.toString(location.getLatitude());
                                longitud = Double.toString(location.getLongitude());
                                System.out.println("Esta es la latitude: " + latitude);
                                System.out.println("Esta es la longitud: " + longitud);
                                latitudeUno = latitude.substring(0, latitude.indexOf("."));
                                latitudeDos = latitude.substring(latitude.indexOf(".") + 1, latitude.length());
                                longitudUno = longitud.substring(0, longitud.indexOf("."));
                                longitudDos = longitud.substring(longitud.indexOf(".") + 1, longitud.length());
                                System.out.println("Estos son los numeros separados por el punto: " + latitudeUno + " " + latitudeDos + " " + longitudUno + " " + longitudDos + " " );
                                //$GPS=,-10014,0980,2608,8791,&
                                String coordenasAMandar = "$GPS=," + latitudeUno + "," + latitudeDos + "," + longitudUno + "," + longitudDos + ",&";
                                System.out.println("Esta es la cadena a mandar: " + coordenasAMandar);
                                System.out.println("Send Location");
                                ((MainActivity) getActivity()).sendLocation(coordenasAMandar);
                                ((MainActivity) getActivity()).showToast("Ubicación Registrada");
                                //$GPS=,025,698455,-100,317649,&
                            } catch (Exception e) {
                                System.out.println("Error al enviar location: " + e);
                            }

                        }

                    });
                }
            } catch (Exception e) {
                System.out.println("Error: " + e);
            }

        } else {
            ((MainActivity) getActivity()).showToast("Bluetooth desconectado");
        }

    }

    public void requestPermission(){
        ActivityCompat.requestPermissions(getActivity(), new String[]{ACCESS_FINE_LOCATION},1);
    }

    public void createTimer(){
        System.out.println("Estoy en el createTimer");
        mCountDownTimer = new CountDownTimer(360000, 1000) {
            //Llega a 6 minutos
            long milis = 0;
            long aux = 0;
            public void onTick(long millisUntilFinished) {
                //tvTimer.setText("Segundos Transcurridos: " + millisUntilFinished / 1000);
                milis = 360000 - millisUntilFinished;
                if(milis > 300000){
                    tvTimer.setTextColor(Color.RED);
                }else{
                    tvTimer.setTextColor(Color.BLACK);
                }
                //System.out.println("Estos son los milis que han transcurrido: " + milis);
                tvTimer.setText(String.format("%d min, %d seg",
                        TimeUnit.MILLISECONDS.toMinutes(milis),
                        TimeUnit.MILLISECONDS.toSeconds(milis) -
                                TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(milis))
                ));

                //DE REMOTO A LOCAL SE TIENE QUE REINICIAR EL TIMER
            }

            public void onFinish() {
                //CAMBIAR ESTA LEYENDA EN CASO DE QUE SE PASE DE TIEMPO
                tvTimer.setText("");
            }

        }.start();

    }
}
