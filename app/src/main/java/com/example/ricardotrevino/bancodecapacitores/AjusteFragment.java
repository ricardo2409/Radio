package com.example.ricardotrevino.bancodecapacitores;

import android.app.Fragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

/**
 * Created by sergiotrevino on 2/9/18.
 */

public class AjusteFragment extends Fragment implements View.OnClickListener{
    View view;
    Button btnComando, btnVoltaje;
    EditText etComando, etVoltaje;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.ajuste_fragment,
                container, false);

        btnComando = (Button) view.findViewById(R.id.btnComando);
        btnVoltaje = (Button) view.findViewById(R.id.btnVoltaje);
        etVoltaje = (EditText) view.findViewById(R.id.etVoltaje);

        btnComando.setOnClickListener(this);
        btnVoltaje.setOnClickListener(this);
        return view;
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
        }

    }


}

