package com.example.ricardotrevino.bancodecapacitores;

import android.Manifest;
import android.app.Fragment;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Criteria;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
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

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;

import org.w3c.dom.Text;

import java.util.Arrays;
import java.util.List;

import static android.Manifest.permission.ACCESS_FINE_LOCATION;

/**
 * Created by ricardotrevino on 12/17/17.
 */

public class VoltajeFragment extends Fragment implements View.OnClickListener {

    TextView tvBloqueo, tvVoltaje, tvVoltajeControl, phase1TransitionTextView, phase2TransitionTextView, phase3TransitionTextView, tvLocalRemoto, tvVoltajeAutomatico;
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

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.voltaje_fragment, null);
        requestPermission();
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
        //tvVoltajeAutomatico = (TextView) view.findViewById(R.id.tvVoltajeAutomatico);


        switchLocalRemoto = (Switch) view.findViewById(R.id.switchLocalRemoto);
        //switchVoltajeAutomatico = (Switch) view.findViewById(R.id.switchVoltajeAutomatico);


        switchLocalRemoto.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    if (((MainActivity) getActivity()).connected) {
                        try {
                            System.out.println("Switch es True");
                            ((MainActivity) getActivity()).showPasswordDialog("Ingrese la Contraseña", "Cambio a Remoto");

                        } catch (Exception e) {
                            System.out.println("Error: " + e);
                        }
                    } else {
                        ((MainActivity) getActivity()).showToast("Bluetooth desconectado");
                    }

                } else {
                    if (((MainActivity) getActivity()).connected) {
                        try {
                            System.out.println("Switch es False");
                            ((MainActivity) getActivity()).sendManOff();
                            tvLocalRemoto.setText("Remoto");
                            ((MainActivity) getActivity()).waitMs(1000);
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
        if (((MainActivity) getActivity()).connected) {
            try {
                client = LocationServices.getFusedLocationProviderClient(getContext());
                if (ActivityCompat.checkSelfPermission(getActivity(), ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    System.out.println("Permiso no otorgado");

                    return;
                }else{
                    client.getLastLocation().addOnSuccessListener(getActivity(), new OnSuccessListener<Location>() {
                        @Override
                        public void onSuccess(Location location) {

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
}
