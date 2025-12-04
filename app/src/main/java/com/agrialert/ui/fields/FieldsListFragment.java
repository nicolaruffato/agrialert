package com.agrialert.ui.fields;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.agrialert.R;
import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class FieldsListFragment extends Fragment {

    private RecyclerView rvFields;
    private MaterialButton btnFields;
    private MaterialButton btnFieldGroups;
    private MaterialButton btnAddField;

    public FieldsListFragment() {
        // Costruttore vuoto richiesto
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_fields_list, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view,
                              @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Collego le view dal layout
        rvFields = view.findViewById(R.id.rvFields);
        btnFields = view.findViewById(R.id.btnFields);
        btnFieldGroups = view.findViewById(R.id.btnFieldGroups);
        btnAddField = view.findViewById(R.id.btnAddField);

        // Siamo sulla tab "Campi"
        btnFields.setChecked(true);

        // Setup RecyclerView
        rvFields.setLayoutManager(new LinearLayoutManager(requireContext()));
        FieldsAdapter adapter = new FieldsAdapter();
        rvFields.setAdapter(adapter);

        // Dati finti di esempio (poi arriveranno dal backend )
        List<FieldUiModel> sampleFields = createSampleFields();
        adapter.submitList(sampleFields);

        // Per ora questi pulsanti non fanno nulla (li gestiremo dopo)
        btnFieldGroups.setOnClickListener(v -> {
            // TODO: mostrare i gruppi invece dei campi
        });

        btnAddField.setOnClickListener(v -> {
            // TODO: aprire schermata "Nuovo campo"
        });
    }

    /**
     * Crea una lista di campi di esempio, con:
     * - indirizzo, coltura, gruppo
     * - icona principale in base alla coltura
     * - icone alert (lista, max 6 usate dall'adapter)
     */
    private List<FieldUiModel> createSampleFields() {
        List<FieldUiModel> list = new ArrayList<>();

        // Liste di icone alert di esempio (riuso sempre ic_alert, poi saranno da mettere quelle degli alert)
        List<Integer> alerts1 = Arrays.asList(
                R.drawable.ic_alert,
                R.drawable.ic_alert,
                R.drawable.ic_alert
        );

        List<Integer> alerts2 = Arrays.asList(
                R.drawable.ic_alert,
                R.drawable.ic_alert
        );

        List<Integer> alerts3 = Arrays.asList(
                R.drawable.ic_alert
        );

        // Campo 1 – Ortaggi
        list.add(new FieldUiModel(
                1,
                "Via Verdirdi, 15 - Mestre (VE)",
                "Ortaggi",
                "Gruppo: Prova",
                getIconForCrop("Ortaggi"),
                alerts1
        ));

        // Campo 2 – Cereali
        list.add(new FieldUiModel(
                2,
                "Via Giallo, 15 - Mestre (VE)",
                "Cereali",
                "Gruppo: Zona A",
                getIconForCrop("Cereali"),
                alerts2
        ));

        // Campo 3 – Frutteti
        list.add(new FieldUiModel(
                3,
                "Via Blu, 15 - Rovigo (RO)",
                "Frutteti",
                "Gruppo: Zona B",
                getIconForCrop("Frutteti"),
                alerts3
        ));

        // Aggiungi altri campi se vuoi…

        return list;
    }

    /**
     * Restituisce l'icona giusta in base alla coltura.
     * Qui usiamo le  foto messe in res/drawable come PNG:
     *  - ic_ortaggi.png
     *  - ic_cereali.png
     *  - ic_frutteti.png
     *  - ic_leguminose.png
     *  - ic_oleaginose.png
     *  - ic_aromatiche.png
     *
     * Se il nome della coltura non corrisponde, torna una icona di default.
     */
    private int getIconForCrop(String cropType) {
        if (cropType == null) {
            return R.drawable.ic_fields;  // icona generica
        }

        String c = cropType.toLowerCase();

        if (c.contains("ortaggi"))        return R.drawable.ic_ortaggi;
        if (c.contains("leguminose"))     return R.drawable.ic_leguminose;
        if (c.contains("cereali"))        return R.drawable.ic_cereali;
        if (c.contains("frutt"))          return R.drawable.ic_frutteti;
        if (c.contains("oleaginose"))     return R.drawable.ic_oleaginose;
        if (c.contains("aromatiche"))     return R.drawable.ic_aromatiche;

        return R.drawable.ic_fields;      // default se non trova niente
    }
}

