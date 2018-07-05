package com.example.ricardotrevino.bancodecapacitores;

import android.app.Fragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import java.io.IOException;

/**
 * Created by ricardotrevino on 12/17/17.
 */

public class DiagnosticoFragment extends Fragment implements View.OnClickListener {

    TextView tvNetID, tvNodeID, tvPotencia, tvRSSILocal, tvRSSIRemoto, tvRuidoLocal, tvRuidoRemoto, tvTipo;
    Button btnRadio, btnDiagnostico;

    EditText etNetID, etNodeID, etPotencia;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.diagnostico_fragment,
                container, false);
        etNetID = (EditText) view.findViewById(R.id.etNetID);
        etNodeID = (EditText) view.findViewById(R.id.etNodeID);
        etPotencia = (EditText) view.findViewById(R.id.etPotencia);

        etNodeID.addTextChangedListener(new TextWatcher() {

            public void afterTextChanged(Editable s) {

                // you can call or do what you want with your EditText here
                System.out.println("TEXTO CAMBIO");
                String nodo = etNodeID.getText().toString();
                if(nodo.length() == 4){
                    String nodo1 = nodo.substring(0, (nodo.length()/2));
                    String nodo2 = nodo.substring((nodo.length()/2));
                    System.out.println("Esto es nodo 1: " + nodo1);
                    System.out.println("Esto es nodo 2: " + nodo2);

                }

            }

            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            public void onTextChanged(CharSequence s, int start, int before, int count) {}
        });
        /*
        tvRSSILocal = (TextView) view.findViewById(R.id.tvRSSILocal);
        tvRSSIRemoto = (TextView) view.findViewById(R.id.tvRSSIRemoto);
        tvRuidoLocal = (TextView) view.findViewById(R.id.tvRuidoLocal);
        tvRuidoRemoto = (TextView) view.findViewById(R.id.tvRuidoRemoto);
        tvRSSILocal.setBackgroundResource(R.drawable.bordercolor);
        tvRSSIRemoto.setBackgroundResource(R.drawable.bordercolor);
        tvRuidoLocal.setBackgroundResource(R.drawable.bordercolor);
        tvRuidoRemoto.setBackgroundResource(R.drawable.bordercolor);
        */
        //btnDiagnostico = (Button) view.findViewById(R.id.botonDiagnostico);
        btnRadio = (Button) view.findViewById(R.id.btnRadio);
        btnRadio.setOnClickListener(this);
        //btnDiagnostico.setOnClickListener(this);
        ((MainActivity)getActivity()).DiagnosticoBool = true;
        tvTipo = (TextView) view.findViewById(R.id.tvTipo);
        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        System.out.println("OnActivityCreated Diagnostico");
        //Borrar los valores para que se vean que son nuevos
        resetValues();

        try{
            ((MainActivity)getActivity()).sendRadOn();
            ((MainActivity)getActivity()).sendCommand();
            ((MainActivity)getActivity()).sendNetID();
            ((MainActivity)getActivity()).sendPower();
            ((MainActivity)getActivity()).sendNodeID();

        }catch (IOException e){
            System.out.println("Error: " + e);
        }

    }

    public void resetValues(){
        etNodeID.setText("");
        etNetID.setText("");
        etPotencia.setText("");
    }


    @Override
    public void onClick(View view) {

        switch (view.getId()){
            case R.id.btnRadio:
                try{
                    //Configurar el radio
                    System.out.println("Botón Radio");
                    if(etNetID.getText() != null && etNodeID.getText() != null && etPotencia.getText() != null){
                        ((MainActivity)getActivity()).configuraRadio(etNetID.getText().toString().trim(), etNodeID.getText().toString().trim(), etPotencia.getText().toString().trim());
                    }else{
                        System.out.println("Campos incompletos");
                        ((MainActivity)getActivity()).showToast("Favor de llenar todos los campos");
                    }
                }catch (Exception e){
                    System.out.println("Error: " + e);

                }
                break;

                /*
            case R.id.botonDiagnostico:
                try{
                    System.out.println("Botón Diagnóstico");
                    ((MainActivity)getActivity()).outputStream.write("ATO\r".getBytes());
                    ((MainActivity)getActivity()).sendRadOnR();
                    ((MainActivity)getActivity()).sendCommandR();
                    ((MainActivity)getActivity()).sendRssiR();
                    ((MainActivity)getActivity()).sendATO();

                }catch (Exception e){
                    System.out.println("Error: " + e);
                }
                break;
                */
        }
    }
}
