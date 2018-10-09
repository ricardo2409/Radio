package com.example.ricardotrevino.bancodecapacitores;

import android.Manifest;
import android.app.Fragment;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;

import static android.Manifest.permission.ACCESS_FINE_LOCATION;

/**
 * Created by sergiotrevino on 2/9/18.
 */

public class AjusteFragment extends Fragment implements View.OnClickListener{
    View view;
    Button btnComando, btnVoltaje, btnLocation, btnCalibracion;
    EditText etComando, etVoltaje, etCalibracion;
    private FusedLocationProviderClient client;
    String latitude, longitud, latitudeUno, latitudeDos, longitudUno, longitudDos;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.ajuste_fragment,
                container, false);

        btnComando = (Button) view.findViewById(R.id.btnComando);
        btnVoltaje = (Button) view.findViewById(R.id.btnVoltaje);
        btnLocation = (Button) view.findViewById(R.id.btnLocation);
        btnCalibracion = (Button) view.findViewById(R.id.btnCalibracion);


        etVoltaje = (EditText) view.findViewById(R.id.etVoltaje);

        btnComando.setOnClickListener(this);
        btnVoltaje.setOnClickListener(this);
        btnLocation.setOnClickListener(this);
        btnCalibracion.setOnClickListener(this);


        return view;
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



    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.btnComando:
                try{
                    //Manda comando para ajustar el offset
                    ((MainActivity)getActivity()).sendOffset();
                    ((MainActivity)getActivity()).showToast("Offset Configurado");

                }catch (Exception e){
                    System.out.println("Error: " + e);
                }
                break;
            case R.id.btnVoltaje:
                try{
                    if(etVoltaje.getText() != null){
                        ((MainActivity)getActivity()).sendVoltaje(etVoltaje.getText().toString());
                    }else{
                        ((MainActivity)getActivity()).showToast("Voltaje Vacío");
                        ((MainActivity)getActivity()).showToast("Voltaje Configurado");

                    }
                }catch (Exception e){
                    System.out.println("Error: " + e);

                }
                break;
            case R.id.btnLocation:
                try{
                    //Manda comando para pedir coordenadas
                    getLocation();

                }catch (Exception e){
                    System.out.println("Error: " + e);
                }
                break;
            case R.id.btnCalibracion:
                try{
                    if(etCalibracion.getText() != null){
                        ((MainActivity)getActivity()).sendCalibracion(etCalibracion.getText().toString());
                    }else{
                        ((MainActivity)getActivity()).showToast("Voltaje Vacío");
                        ((MainActivity)getActivity()).showToast("Voltaje Configurado");

                    }
                }catch (Exception e){
                    System.out.println("Error: " + e);

                }
                break;
        }

    }


}

