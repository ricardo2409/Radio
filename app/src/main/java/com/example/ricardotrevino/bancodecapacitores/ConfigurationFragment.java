package com.example.ricardotrevino.bancodecapacitores;

import android.app.Fragment;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.TextView;

import org.w3c.dom.Text;

import java.io.IOException;

/**
 * Created by ricardotrevino on 12/17/17.
 */

public class ConfigurationFragment extends Fragment implements View.OnClickListener {
    Button programButton, solicitarButton;
    TextView sourceAddressTextInput, destinationAddressTextInput, minimumVoltageTextInput, maximumVoltageTextInput;
    CheckBox voltageControlCheckbox;
    View view;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.configuration_fragment, container, false);
        initItems();
        ((MainActivity)getActivity()).ConfiguracionBool = true;
        return view;
    }


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        System.out.println("OnAttach");
    }

    @Override
    public void onAttachFragment(Fragment childFragment) {
        super.onAttachFragment(childFragment);
        System.out.println("OnAttachFragment");
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        System.out.println("OnCreate");
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        System.out.println("OnActivityCreated");
        resetItems();
        sendStop();
    }

    @Override
    public void onViewStateRestored(Bundle savedInstanceState) {
        super.onViewStateRestored(savedInstanceState);
        System.out.println("OnViewStatedRestored");
    }

    @Override
    public void onStart() {
        super.onStart();
        //System.out.println("OnStart");
        //sendStop();

    }

    public void sendStop(){
        try{
            ((MainActivity)getActivity()).sendStop();
        }catch (IOException e){
            System.out.println("Error: " + e);
        }
    }

    public void initItems(){
        sourceAddressTextInput = (TextView) view.findViewById(R.id.sourceAddressTextInput);
        destinationAddressTextInput = (TextView) view.findViewById(R.id.destinationAddressTextInput);
        voltageControlCheckbox = (CheckBox) view.findViewById(R.id.voltageControlCheckbox);
        minimumVoltageTextInput = (TextView) view.findViewById(R.id.minimumVoltageTextInput);
        maximumVoltageTextInput = (TextView) view.findViewById(R.id.maximumVoltageTextInput);


        sourceAddressTextInput.setBackgroundResource(R.drawable.bordercolor);
        destinationAddressTextInput.setBackgroundResource(R.drawable.bordercolor);
        minimumVoltageTextInput.setBackgroundResource(R.drawable.bordercolor);
        maximumVoltageTextInput.setBackgroundResource(R.drawable.bordercolor);

        solicitarButton = (Button) view.findViewById(R.id.solicitarButton);
        solicitarButton.setOnClickListener(this);

        programButton = (Button) view.findViewById(R.id.programButton);
        programButton.setOnClickListener(this);

    }

    public void resetItems(){
        voltageControlCheckbox.setChecked(false);
    }


    @Override
    public void onClick(View view) {

        switch (view.getId()){
            case R.id.solicitarButton:
                System.out.println("Estoy en el solicitarButton");
                try{
                    ((MainActivity)getActivity()).sendStop();
                }catch (IOException e){
                    System.out.println("Error: " + e);
                }

                break;

            case R.id.programButton:
                System.out.println("Estoy en el programConfiguration");
                ((MainActivity)getActivity()).programConfiguration();
                ((MainActivity)getActivity()).showToast("¡ Configuración enviada !");



                break;

        }
    }
}
