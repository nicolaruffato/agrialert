package com.agrialert.ui.alerts;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.agrialert.R;

public class AlertsListFragment extends Fragment {

    public AlertsListFragment() {
        // Costruttore vuoto richiesto
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {

        // Per ora niente binding, solo inflate “normale”
        return inflater.inflate(R.layout.fragment_alerts_list, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view,
                              @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // qui dopo ci metteremo la logica della lista alert
    }
}
