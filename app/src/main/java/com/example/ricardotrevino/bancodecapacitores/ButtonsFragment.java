package com.example.ricardotrevino.bancodecapacitores;

import android.app.Fragment;
import android.app.FragmentTransaction;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

/**
 * Created by ricardotrevino on 12/17/17.
 */

public class ButtonsFragment extends Fragment implements View.OnClickListener {

    Button prueba;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.buttons_fragment, null);

        prueba = (Button) view.findViewById(R.id.btnConnect);
        //prueba.setOnClickListener(this);

        return view;
    }



    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.btnConnect:
                ConfigurationFragment conFrag = new ConfigurationFragment();
                FragmentTransaction transaction = getFragmentManager().beginTransaction();

                //Transaction.replace(el container a modificar, el fragmento que pone)
                transaction.replace(R.id.fragment_container, conFrag);
                transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
                transaction.addToBackStack(null);
                transaction.commit();
                break;
        }

    }
}
