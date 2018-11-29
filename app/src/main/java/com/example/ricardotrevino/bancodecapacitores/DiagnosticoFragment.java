package com.example.ricardotrevino.bancodecapacitores;

import android.app.Fragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import java.io.IOException;

/**
 * Created by ricardotrevino on 12/17/17.
 */

public class DiagnosticoFragment extends Fragment implements View.OnClickListener, AdapterView.OnItemSelectedListener {

    TextView tvNetID, tvNodeID, tvPotencia, tvRSSILocal, tvRSSIRemoto, tvRuidoLocal, tvRuidoRemoto, tvTipo, tvUno, tvDos;
    Button btnRadio, btnDiagnostico;

    EditText etNetID, etNodeID, etPotencia;
    //Spinners
    Spinner spinner12;
    Spinner spinner13;
    Spinner spinner10;
    Spinner spinner11;
    ArrayAdapter<String> adapter12;
    ArrayAdapter<String> adapter13;
    ArrayAdapter<String> adapter10;
    ArrayAdapter<String> adapter11;
    String nodo1, nodo2;
    String s10, s11, s12, s13;
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.diagnostico_fragment,
                container, false);
        tvTipo = (TextView) view.findViewById(R.id.tvTipo);
        tvUno = (TextView) view.findViewById(R.id.tvUno);
        tvDos = (TextView) view.findViewById(R.id.tvDos);
        etNetID = (EditText) view.findViewById(R.id.etNetID);
        etNodeID = (EditText) view.findViewById(R.id.etNodeID);
        //etPotencia = (EditText) view.findViewById(R.id.etPotencia);
        spinner10 = (Spinner) view.findViewById(R.id.spinner10);
        spinner11 = (Spinner) view.findViewById(R.id.spinner11);
        spinner12 = (Spinner) view.findViewById(R.id.spinner12);
        spinner13 = (Spinner) view.findViewById(R.id.spinner13);
        resetValues();
        //Llena de valores los spinners
        fillSpinners();
        hideSpinners();
        s10 = "0";
        s11 = "0";
        s12 = "0";
        s13 = "0";


        etNodeID.addTextChangedListener(new TextWatcher() {

            public void afterTextChanged(Editable s) {
                // you can call or do what you want with your EditText here
                System.out.println("TEXTO CAMBIO");
                String nodo = etNodeID.getText().toString();
                if(nodo.length() == 4){
                    nodo1 = nodo.substring(0, (nodo.length()/2));
                    nodo2 = nodo.substring((nodo.length()/2));
                    System.out.println("Esto es nodo 1: " + nodo1);
                    System.out.println("Esto es nodo 2: " + nodo2);

                    if(nodo1.equals("00") && nodo2.equals("00")){
                        System.out.println("Coordinador");
                        tvTipo.setText("Coordinador");
                        //Cambiar el nombre del textview y mostrar spinner correcto
                        tvUno.setText("Antena Coordinador");
                        tvDos.setText("");
                        spinner13.setVisibility(View.VISIBLE);
                        spinner10.setVisibility(View.GONE);
                        spinner11.setVisibility(View.GONE);
                        spinner12.setVisibility(View.GONE);

                    }else if(!nodo1.equals("00") && nodo2.equals("00")){
                        System.out.println("Repetidor");
                        tvTipo.setText("Repetidor");
                        //Cambiar el nombre del edittext
                        tvUno.setText("Antena Coordinador");
                        tvDos.setText("Antena Nodo");
                        spinner13.setVisibility(View.GONE);
                        spinner10.setVisibility(View.VISIBLE);//Coordinador
                        spinner11.setVisibility(View.VISIBLE);//Nodo
                        spinner12.setVisibility(View.GONE);
                    }else if(!nodo1.equals("00") && !nodo2.equals("00")){
                        System.out.println("Nodo");
                        tvTipo.setText("Nodo");
                        tvDos.setText("");
                        //Cambiar el nombre del edittext
                        tvUno.setText("Antena Nodo");
                        spinner13.setVisibility(View.GONE);
                        spinner10.setVisibility(View.GONE);
                        spinner11.setVisibility(View.GONE);
                        spinner12.setVisibility(View.VISIBLE);
                    }else  if(nodo1.equals("00") && !nodo2.equals("00")){
                        System.out.println("Nodo");
                        tvTipo.setText("Nodo");
                        tvDos.setText("");
                        //Cambiar el nombre del edittext
                        tvUno.setText("Antena Nodo");
                        spinner13.setVisibility(View.GONE);
                        spinner10.setVisibility(View.GONE);
                        spinner11.setVisibility(View.GONE);
                        spinner12.setVisibility(View.VISIBLE);
                    }
                }else{
                    tvTipo.setText("");
                    tvUno.setText("");
                    tvDos.setText("");
                    hideSpinners();
                }

            }

            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            public void onTextChanged(CharSequence s, int start, int before, int count) {}
        });

        btnRadio = (Button) view.findViewById(R.id.btnRadio);
        btnRadio.setOnClickListener(this);
        //btnDiagnostico.setOnClickListener(this);
        ((MainActivity)getActivity()).DiagnosticoBool = true;


        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        System.out.println("OnActivityCreated Diagnostico");
        //Borrar los valores para que se vean que son nuevos
        resetValues();
        if(((MainActivity) getActivity()).boolPassword == true)
        {
            try{
                //Manda los comandos para obtener los valores
                ((MainActivity)getActivity()).sendRadOn();
                ((MainActivity)getActivity()).sendCommand();
                ((MainActivity)getActivity()).sendNetID();
                ((MainActivity)getActivity()).sendNodeID();
                ((MainActivity)getActivity()).sendS10();
                ((MainActivity)getActivity()).sendS11();
                ((MainActivity)getActivity()).sendS12();
                ((MainActivity)getActivity()).sendS13();

            }catch (IOException e){
                System.out.println("Error: " + e);
            }
        }else{
            ((MainActivity) getActivity()).showPasswordDialog("Ingrese la contraseña", "");
        }

    }

    public void resetValues(){
        etNodeID.setText("");
        etNetID.setText("");
    }

    public void fillSpinners(){
        String[] items10 = new String[2];
        String[] items11 = new String[2];
        String[] items12 = new String[2];
        String[] items13 = new String[2];
        items10[0] = "0";
        items10[1] = "1";
        items11[0] = "0";
        items11[1] = "1";
        items12[0] = "0";
        items12[1] = "1";
        items13[0] = "0";
        items13[1] = "1";

        adapter10 = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_list_item_1, items10);
        spinner10.setAdapter(adapter10);
        spinner10.setOnItemSelectedListener(this);
        adapter11 = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_list_item_1, items11);
        spinner11.setAdapter(adapter11);
        spinner11.setOnItemSelectedListener(this);
        adapter12 = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_list_item_1, items12);
        spinner12.setAdapter(adapter12);
        spinner12.setOnItemSelectedListener(this);
        adapter13 = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_list_item_1, items13);
        spinner13.setAdapter(adapter13);
        spinner13.setOnItemSelectedListener(this);
    }

    public void hideSpinners(){
        spinner10.setVisibility(View.GONE);
        spinner11.setVisibility(View.GONE);
        spinner12.setVisibility(View.GONE);
        spinner13.setVisibility(View.GONE);

    }


    @Override
    public void onClick(View view) {

        switch (view.getId()){
            case R.id.btnRadio:
                try{
                    //Configurar el radio
                    System.out.println("Botón Radio");
                    System.out.println("CLICK");
                    System.out.println("Nodo 1: " + nodo1);
                    System.out.println("Nodo 2:" + nodo2);

                    if(Integer.parseInt(nodo1) < 31 && Integer.parseInt(nodo2) < 31){
                        if(etNetID.getText() != null && etNodeID.getText() != null ){
                            System.out.println("Estos son los valores de las antenas que mando: " + s10 + " " + s11 + " " + s12 + " " + s13);

                            ((MainActivity)getActivity()).configuraRadio(etNetID.getText().toString().trim(), etNodeID.getText().toString().trim(), s10, s11, s12, s13);
                            //Despues de esto se manda el save values (AT&W y radoff)
                        }else{
                            System.out.println("Campos incompletos");
                            ((MainActivity)getActivity()).showToast("Campos Incompletos");
                        }
                    }else{
                        ((MainActivity)getActivity()).showToast("Números mayores a 30");

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

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        switch (parent.getId()) {
            case R.id.spinner10:
                s10 = spinner10.getSelectedItem().toString();
                System.out.println("s10: " + s10);
                break;
            case R.id.spinner11:
                s11 = spinner11.getSelectedItem().toString();
                System.out.println("s11: " + s11);
                break;
            case R.id.spinner12:
                s12 = spinner12.getSelectedItem().toString();
                System.out.println("s12: " + s12);
                break;
            case R.id.spinner13:
                s13 = spinner13.getSelectedItem().toString();
                System.out.println("s13: " + s13);
                break;
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }
}
