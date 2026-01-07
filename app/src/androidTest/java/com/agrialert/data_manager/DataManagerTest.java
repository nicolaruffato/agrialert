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

import java.security.SecureRandom;
import java.sql.Time;
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


    private String randomString(int len) {
        if (len < 0) throw new IllegalArgumentException();
        SecureRandom random = new SecureRandom();
        StringBuilder sb = new StringBuilder(len);
        for (int i = 0; i < len; i++) {
            sb.append(random.nextInt(37, 127));
        }
        return sb.toString();
    }

    // TODO: finire questo
    @Test
    public void TC_RF_01_01() throws TimeoutException {
        Intent serviceIntent = new Intent(ApplicationProvider.getApplicationContext(), DataManager.class);
        IBinder binder = serviceRule.bindService(serviceIntent);
        DataManager dataManager = ((DataManager.LocalBinder) binder).getService();
        assertNotNull(dataManager);

        var all_groups = dataManager.getAllGroups().blockingFirst();
        for(var group : all_groups) {
            assertTrue(group.getFields().isEmpty());
        }

        String groupName = "test";

        dataManager.insertGroup(new FieldsGroup(groupName, "")).blockingAwait();
        dataManager.insertField(new Field("test_addr", 0d, 0d, groupName, CropType.CEREALS)).blockingAwait();
        dataManager.insertField(new Field("test_addr", 0d, 0d, "Default", CropType.CEREALS)).blockingAwait();

        all_groups = dataManager.getAllGroups().blockingFirst();
        int n_groups = 0;
        for(var group : all_groups) {
            n_groups += group.getFields().size();
        }
        assertEquals(2, n_groups);
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
        dataManager.deleteAlertsToField(List.of(new Pair<>(1, fieldId))).blockingAwait();

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

        assertNotNull(fieldToDelete);
        assertNotNull(fieldToKeep);

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

    @Test
    public void TC_RF_04_01() throws TimeoutException {
        Intent serviceIntent = new Intent(ApplicationProvider.getApplicationContext(), DataManager.class);
        IBinder binder = serviceRule.bindService(serviceIntent);
        DataManager dataManager = ((DataManager.LocalBinder) binder).getService();
        assertNotNull(dataManager);

        String randomString = randomString(new SecureRandom().nextInt(0, 100));
        //String maxLenString = new String(new char[Integer.MAX_VALUE]);

        dataManager.insertGroup(new FieldsGroup("", "")).blockingAwait();
        dataManager.insertGroup(new FieldsGroup(randomString, "")).blockingAwait();
        //dataManager.insertGroup(new FieldsGroup(maxLenString, "")).blockingAwait();
        dataManager.insertGroup(new FieldsGroup("aa", "")).blockingAwait();
        boolean check = false;
        try {
            dataManager.insertGroup(new FieldsGroup("aa", "")).blockingAwait();
        } catch (RuntimeException e) {
            check = true;
        }
        var emptyStringGroup = dataManager.getGroupByName("").blockingFirst();
        var randomStringGroup = dataManager.getGroupByName(randomString).blockingFirst();
        //var maxLenStringGroup = dataManager.getGroupByName(maxLenString).blockingFirst();
        var aaGroup = dataManager.getGroupByName("aa").blockingFirst();
        assertTrue(emptyStringGroup.getGroup().getName().equals(""));
        assertTrue(randomStringGroup.getGroup().getName().equals(randomString));
        //assertTrue(maxLenStringGroup.getGroup().getName().equals(maxLenString));
        assertTrue(aaGroup.getGroup().getName().equals("aa"));
        assertTrue(check);
    }


    @Test
    public void TC_RF_04_02() throws TimeoutException {
        Intent serviceIntent = new Intent(ApplicationProvider.getApplicationContext(), DataManager.class);
        IBinder binder = serviceRule.bindService(serviceIntent);
        DataManager dataManager = ((DataManager.LocalBinder) binder).getService();
        assertNotNull(dataManager);

        String emptyGroupName = "emptyGroupName";
        String nonEmptyGroupName = "nonEmptyGroupName";

        dataManager.insertGroup(new FieldsGroup(emptyGroupName, "")).blockingAwait();
        dataManager.insertGroup(new FieldsGroup(nonEmptyGroupName, "")).blockingAwait();
        dataManager.insertField(new Field("test_addr", 0d, 0d, nonEmptyGroupName, CropType.CEREALS)).blockingAwait();

        // verifica inserimento
        var emptyGroup = dataManager.getGroupByName(emptyGroupName).blockingFirst();
        var nonEmptyGroup = dataManager.getGroupByName(nonEmptyGroupName).blockingFirst();

        assertTrue(emptyGroup.getGroup().getName().equals(emptyGroupName) && emptyGroup.getFields().isEmpty());
        assertTrue(nonEmptyGroup.getGroup().getName().equals(nonEmptyGroupName)
        && nonEmptyGroup.getFields().size() == 1);


        // eliminazione gruppo
        dataManager.deleteGroup(emptyGroup.getGroup()).blockingAwait();
        dataManager.deleteGroup(nonEmptyGroup.getGroup()).blockingAwait();

        // sistemare bug qui + controllare gruppo di default
        emptyGroup = dataManager.getGroupByName(emptyGroupName).blockingFirst();
        nonEmptyGroup = dataManager.getGroupByName(nonEmptyGroupName).blockingFirst();
        assertNull(emptyGroup);
        assertNull(nonEmptyGroup);
    }

    @Test
    public void TC_RF_04_03() throws TimeoutException {
        Intent serviceIntent = new Intent(ApplicationProvider.getApplicationContext(), DataManager.class);
        IBinder binder = serviceRule.bindService(serviceIntent);
        DataManager dataManager = ((DataManager.LocalBinder) binder).getService();
        assertNotNull(dataManager);

        String group1 = "group1";
        String group2 = "group2";
        Field testField = new Field("test_addr", 0d, 0d, group1, CropType.CEREALS);

        dataManager.insertGroup(new FieldsGroup(group1, "")).blockingAwait();
        dataManager.insertGroup(new FieldsGroup(group2, "")).blockingAwait();
        dataManager.insertField(testField).blockingAwait();

        // caso 1 -> da gruppo utente a gruppo utente
        testField = dataManager.getGroupByName(group1).blockingFirst().getFields().get(0);
        testField.setGroupName(group2);
        dataManager.updateField(testField).blockingAwait();
        testField = dataManager.getGroupByName(group2).blockingFirst().getFields().get(0);
        assertTrue(testField.getGroupName().equals(group2));

        // caso 2 -> assegnamento allo stesso gruppo
        testField.setGroupName(testField.getGroupName());
        dataManager.updateField(testField).blockingAwait();
        testField = dataManager.getGroupByName(group2).blockingFirst().getFields().get(0);
        assertTrue(testField.getGroupName().equals(group2));

        // caso 3 -> da gruppo utente a gruppo di default
        testField.setGroupName("Default");
        dataManager.updateField(testField).blockingAwait();
        testField = dataManager.getGroupByName("Default").blockingFirst().getFields().get(0);
        assertTrue(testField.getGroupName().equals("Default"));

        // caso 4 -> da gruppo di default a gruppo utente
        testField.setGroupName(group1);
        dataManager.updateField(testField).blockingAwait();
        testField = dataManager.getGroupByName(group1).blockingFirst().getFields().get(0);
        assertTrue(testField.getGroupName().equals(group1));
    }






}
