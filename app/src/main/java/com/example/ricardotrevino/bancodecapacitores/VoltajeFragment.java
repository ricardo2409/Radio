package com.example.ricardotrevino.bancodecapacitores;

import android.app.Fragment;
import android.content.Context;
import android.location.Address;
import android.location.Criteria;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.annotation.Nullable;
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

import org.w3c.dom.Text;

import java.util.List;

/**
 * Created by ricardotrevino on 12/17/17.
 */

public class VoltajeFragment extends Fragment implements View.OnClickListener {

    TextView tvBloqueo, tvVoltaje, tvVoltajeControl, phase1TransitionTextView, phase2TransitionTextView, phase3TransitionTextView, tvLocalRemoto, tvVoltajeAutomatico;
    Button openButton, closeButton;
    Switch switchLocalRemoto, switchVoltajeAutomatico;
    static RadioButton phase1OpenButton, phase1CloseButton, phase2OpenButton, phase2CloseButton, phase3OpenButton, phase3CloseButton;
    RadioGroup phase1RadioGroup, phase2RadioGroup,phase3RadioGroup;

    Geocoder geocoder;
    String bestProvider;
    List<Address> user = null;
    double lat;
    double lng;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.voltaje_fragment, null);

        tvVoltaje = (TextView) view.findViewById(R.id.voltageTextView);
        tvBloqueo = (TextView)view.findViewById(R.id.tvBloqueo);
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
                if(isChecked){
                    if(((MainActivity)getActivity()).connected){
                        try{
                            System.out.println("Switch es True");
                            ((MainActivity)getActivity()).sendManOn();
                            tvLocalRemoto.setText("Local");
                            ((MainActivity)getActivity()).waitMs(1000);

                        }catch (Exception e){
                            System.out.println("Error: " + e);
                        }
                    }else{
                        ((MainActivity)getActivity()).showToast("Bluetooth desconectado");
                    }

                }else{
                    if(((MainActivity)getActivity()).connected){
                        try{
                            System.out.println("Switch es False");
                            ((MainActivity)getActivity()).sendManOff();
                            tvLocalRemoto.setText("Remoto");
                            ((MainActivity)getActivity()).waitMs(1000);
                        }catch (Exception e){
                            System.out.println("Error: " + e);
                        }
                    }else{
                        ((MainActivity)getActivity()).showToast("Bluetooth desconectado");
                    }

                }
            }
        });

        getLocation(); //Pide las coordenadas solo una vez
        return view;
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.openButton:
                if(((MainActivity)getActivity()).connected){
                    try{
                        System.out.println("Botón Abrir");
                        ((MainActivity)getActivity()).sendOpen();
                    }catch (Exception e){
                        System.out.println("Error: " + e);
                    }
                }else{
                    ((MainActivity)getActivity()).showToast("Bluetooth desconectado");
                }

                break;

            case R.id.closeButton:
                if(((MainActivity)getActivity()).connected){
                    try{
                        System.out.println("Botón Cerrar");
                        ((MainActivity)getActivity()).sendClose();
                    }catch (Exception e){
                        System.out.println("Error: " + e);
                    }
                }else{
                    ((MainActivity)getActivity()).showToast("Bluetooth desconectado");
                }
                break;
        }

    }

    public void getLocation(){
        LocationManager lm = (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);

        Criteria criteria = new Criteria();
        bestProvider = lm.getBestProvider(criteria, false);

        try {
            Location location = lm.getLastKnownLocation(bestProvider);
            if (location == null){
                ((MainActivity)getActivity()).showToast("No se encontró la ubicación");
            }else{
                geocoder = new Geocoder(getActivity());
                try {
                    user = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
                    lat=(double)user.get(0).getLatitude();
                    lng=(double)user.get(0).getLongitude();
                    System.out.println(" DDD lat: " +lat+",  longitude: "+lng);

                }catch (Exception e) {
                    e.printStackTrace();
                }
            }
        } catch (SecurityException e) {
            ((MainActivity)getActivity()).showToast("No se encontró la ubicación");
        }

    }
}
