package com.agrialert.data_manager;

import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.rule.ServiceTestRule;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeoutException;

import static org.junit.Assert.*;

import io.reactivex.rxjava3.core.Flowable;
import kotlin.Pair;


public class DataManagerTest {

    @Rule
    public final ServiceTestRule serviceRule = new ServiceTestRule();

    @Test
    public void TC_RF_01_01() throws TimeoutException {
        Intent serviceIntent = new Intent(ApplicationProvider.getApplicationContext(), DataManager.class);
        IBinder binder = serviceRule.bindService(serviceIntent);
        DataManager dataManager = ((DataManager.LocalBinder) binder).getService();
        assertNotNull(dataManager);

    }

    @Test
    public void TC_RF_01_02() throws TimeoutException {
        Intent serviceIntent = new Intent(ApplicationProvider.getApplicationContext(), DataManager.class);
        IBinder binder = serviceRule.bindService(serviceIntent);
        DataManager dataManager = ((DataManager.LocalBinder) binder).getService();
        assertNotNull(dataManager);

        String validAddress = "V. Torino, 155, 30170 Mestre, Venezia VE";
        Double validLat = 45.47804942667723d;
        Double validLon = 12.254535275612122d;
        String validGroupName = "valid_group_" + System.currentTimeMillis(); // Nome unico per isolamento

        dataManager.insertGroup(new FieldsGroup(validGroupName, "description")).blockingAwait();

        dataManager.insertField(new Field(validAddress, validLat, validLon, "Default", CropType.CEREALS)).blockingAwait();
        dataManager.insertField(new Field(validAddress, validLat, validLon, validGroupName, CropType.CEREALS)).blockingAwait();

        dataManager.insertField(new Field(null, null, null, "Default", CropType.CEREALS)).blockingAwait();

        List<GroupWithFields> groups = dataManager.getAllGroups().blockingFirst();
        boolean foundValidInDefault = false;
        boolean foundInvalidInDefault = false;
        boolean foundValidInGroup = false;

        for (GroupWithFields group : groups) {
            String name = group.getGroup().getName();
            List<Field> fields = group.getFields();

            if (name.equals("Default")) {
                for (Field f : fields) {
                    if (validAddress.equals(f.getAddress())) foundValidInDefault = true;
                    if (f.getAddress() == null) foundInvalidInDefault = true;
                }
            } else if (name.equals(validGroupName)) {
                for (Field f : fields) {
                    if (validAddress.equals(f.getAddress())) foundValidInGroup = true;
                }
            }
        }

        assertTrue(foundValidInDefault);
        assertTrue(foundInvalidInDefault);
        assertTrue(foundValidInGroup);
    }

    @Test
    public void TC_RF_01_03() throws TimeoutException {
        Intent serviceIntent = new Intent(ApplicationProvider.getApplicationContext(), DataManager.class);
        IBinder binder = serviceRule.bindService(serviceIntent);
        DataManager dataManager = ((DataManager.LocalBinder) binder).getService();
        assertNotNull(dataManager);

        String address = "V. Torino, 155, 30170 Mestre, Venezia VE";
        String group = "Default";

        Field field = new Field(address, 45.0, 12.0, group, CropType.CEREALS);
        dataManager.insertField(field).blockingAwait();

        List<GroupWithFields> groups = dataManager.getAllGroups().blockingFirst();
        Field savedField = null;
        for (GroupWithFields g : groups) {
            if (g.getGroup().getName().equals(group)) {
                for (Field f : g.getFields()) {
                    if (f.getAddress().equals(address)) {
                        savedField = f;
                        break;
                    }
                }
            }
        }
        assertNotNull(savedField);

        savedField.setCropType(CropType.OILSEEDS); // Da CEREALS a OILSEEDS
        dataManager.updateField(savedField).blockingAwait();


        Field updated1 = dataManager.getAllGroups().blockingFirst().get(0).getFields().stream()
                .filter(f -> f.getAddress().equals(address)).findFirst().get();
        Assert.assertEquals(CropType.OILSEEDS, updated1.getCropType());

        updated1.setCropType(CropType.NONE);
        dataManager.updateField(updated1).blockingAwait();

        updated1.setCropType(CropType.VEGETABLES);
        dataManager.updateField(updated1).blockingAwait();

        Field updated2 = dataManager.getAllGroups().blockingFirst().get(0).getFields().stream()
                .filter(f -> f.getAddress().equals(address)).findFirst().get();
        Assert.assertEquals(CropType.VEGETABLES, updated2.getCropType());

        updated2.setCropType(CropType.NONE); // "nessuna coltivazione"
        dataManager.updateField(updated2).blockingAwait();

        Field finalField = dataManager.getAllGroups().blockingFirst().get(0).getFields().stream()
                .filter(f -> f.getAddress().equals(address)).findFirst().get();
        Assert.assertEquals(CropType.NONE, finalField.getCropType());

    }

    @Test
    public void TC_RF_01_04() throws TimeoutException {
        Intent serviceIntent = new Intent(ApplicationProvider.getApplicationContext(), DataManager.class);
        IBinder binder = serviceRule.bindService(serviceIntent);
        DataManager dataManager = ((DataManager.LocalBinder) binder).getService();
        assertNotNull(dataManager);

        String address = "V. Torino, 155, 30170 Mestre, Venezia VE";
        String group = "Default";
        dataManager.insertField(new Field(address, 45.0, 12.0, group, CropType.CEREALS)).blockingAwait();

        // Id dal campo creato
        Field savedField = dataManager.getAllGroups().blockingFirst().get(0).getFields().stream()
                .filter(f -> f.getAddress().equals(address)).findFirst().get();
        int fieldId = savedField.getId();

        // --- CRITERIO 1: A un campo con nessun alert, associo almeno un alert ---
        dataManager.addAlertToField(fieldId, 1, new Threshold(31d)).blockingAwait();

        // Verifica
        ActivatedAlerts alerts = dataManager.getActivatedAlertsFromField(fieldId).blockingFirst();
        assertEquals(1, alerts.getAlerts().size());

        // --- CRITERIO 2: A un campo con almeno un alert, associo un nuovo alert ---
        dataManager.addAlertToField(fieldId, 2, new Threshold(31d)).blockingAwait();

        // Verifica
        alerts = dataManager.getActivatedAlertsFromField(fieldId).blockingFirst();
        assertEquals(2, alerts.getAlerts().size());

        // --- CRITERIO 3: A un campo con almeno un alert, rimuovo almeno un alert ---
        dataManager.deleteAlertToField(2, fieldId).blockingAwait();

        // Verifica
        alerts = dataManager.getActivatedAlertsFromField(fieldId).blockingFirst();
        assertEquals(1, alerts.getAlerts().size());

        // --- CRITERIO 4: A un campo con almeno un alert, rimuovo tutti gli alert ---
        dataManager.deleteAlertsToField(Arrays.asList(new Pair<>(1, fieldId))).blockingAwait();

        // Verifica
        alerts = dataManager.getActivatedAlertsFromField(fieldId).blockingFirst();
        assertTrue(alerts.getAlerts().isEmpty());
    }

    @Test
    public void TC_RF_01_05() throws TimeoutException {
        Intent serviceIntent = new Intent(ApplicationProvider.getApplicationContext(), DataManager.class);
        IBinder binder = serviceRule.bindService(serviceIntent);
        DataManager dataManager = ((DataManager.LocalBinder) binder).getService();
        assertNotNull(dataManager);

        String addressToDelete = "V. Torino, 155, 30170 Mestre, Venezia VE";
        String addressToKeep = "V. Torino, 156, 30170 Mestre, Venezia VE";
        String groupName = "Default";

        dataManager.insertField(new Field(addressToDelete, 45.1, 12.1, groupName, CropType.CEREALS)).blockingAwait();
        dataManager.insertField(new Field(addressToKeep, 45.2, 12.2, groupName, CropType.CEREALS)).blockingAwait();

        // Recuperiamo gli oggetti salvati per avere gli ID corretti
        GroupWithFields group = dataManager.getGroupByName(groupName).blockingFirst();
        Field fieldToDelete = null;
        Field fieldToKeep = null;

        for (Field f : group.getFields()) {
            if (f.getAddress().equals(addressToDelete)) fieldToDelete = f;
            if (f.getAddress().equals(addressToKeep)) fieldToKeep = f;
        }

        assertNotNull("Il campo da eliminare deve esistere", fieldToDelete);
        assertNotNull("Il campo da mantenere deve esistere", fieldToKeep);

        // --- CRITERIO DI INPUT: Campo da eliminare ---
        dataManager.deleteField(fieldToDelete).blockingAwait();

        // --- VERIFICA ASSERZIONI DI OUTPUT ---
        GroupWithFields groupsAfterDelete = dataManager.getGroupByName(groupName).blockingFirst();
        List<Field> remainingFields = groupsAfterDelete.getFields();

        // 1. Il campo viene eliminato permanentemente e non appare più nella lista
        Field finalFieldToDelete = fieldToDelete;
        boolean stillExists = remainingFields.stream()
                .anyMatch(f -> f.getId() == finalFieldToDelete.getId());
        assertFalse("Il campo eliminato non deve apparire nella lista", stillExists);

        // 2. Il campo non viene eliminato e continua ad apparire nella lista
        Field finalFieldToKeep = fieldToKeep;
        boolean stillKept = remainingFields.stream()
                .anyMatch(f -> f.getId() == finalFieldToKeep.getId());
        assertTrue("Il campo non selezionato per l'eliminazione deve ancora esistere", stillKept);
    }







}
