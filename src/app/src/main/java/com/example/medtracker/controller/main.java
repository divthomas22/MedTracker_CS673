package com.example.medtracker.controller;


import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.room.Room;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;


import com.example.medtracker.MedTracker;
import com.example.medtracker.R;
import com.example.medtracker.database.MedTrackerDatabase;
import com.example.medtracker.database.MedTrackerDatabaseSingleton;
import com.example.medtracker.database.MedicationDao;
import com.example.medtracker.database.entities.Medication;

import org.w3c.dom.Text;

import java.util.List;


public class main extends AppCompatActivity{

    Button addMed;
    ImageButton setting_button;
    ActivityResultLauncher<Intent> addMedLauncher;
    Button defaultDelete;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.home);

        //press add medication button
        addMed = (Button) findViewById(R.id.addMedButton);
        addMed.setOnClickListener(view -> openMed());
        addMedLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                new ActivityResultCallback<ActivityResult>() {
                    @Override
                    public void onActivityResult(ActivityResult result) {
                        if (result.getResultCode() == 1) {
                            //generate the medication

                            LinearLayout ll = (LinearLayout) findViewById(R.id.medLayout);
                            View card = getLayoutInflater().inflate(R.layout.med_card, null);
                            Button deleteMed = (Button) card.findViewById(R.id.deleteButton);
                            deleteMed.setOnClickListener(view -> openDelete(deleteMed));
                            ll.addView(card);

                            TextView medInfo = (TextView)card.findViewById(R.id.medInfo);
                            medInfo.setText(result.getData().getStringExtra("name"));

                            Toast.makeText(getApplicationContext(), "Add Medicine Successfully!", Toast.LENGTH_SHORT).show();
                        }
                    }
                });

        //press account image button
        setting_button = (ImageButton) findViewById(R.id.settingImage);
        setting_button.setOnClickListener(view -> openSetting());

        // Load medications from database or add this intent to a stack
        MedTrackerDatabase db = MedTrackerDatabaseSingleton.getInstance(MedTracker.getAppContext());
        MedicationDao medDao = db.medicationDao();

        List<Medication> medList = medDao.getAllMeds();
        for (Medication medication:medList) {
            LinearLayout ll = (LinearLayout) findViewById(R.id.medLayout);
            View card = getLayoutInflater().inflate(R.layout.med_card, null);
            Button deleteMed = (Button) card.findViewById(R.id.deleteButton);
            deleteMed.setOnClickListener(view -> openDelete(deleteMed));
            ll.addView(card);

            TextView medInfo = (TextView)card.findViewById(R.id.medInfo);
            medInfo.setText(medication.medName);

        }
    }

    public void openMed(){
        Intent intent = new Intent(this,addMed.class);
        addMedLauncher.launch(intent);
    }

    public void openSetting(){
        Intent intent = new Intent(this,setting.class);
        startActivity(intent);
        finish();
        overridePendingTransition(0, 0);
    }

    public void openDelete(Button delButton){
        AlertDialog.Builder deleteAlert = new AlertDialog.Builder(main.this);
        deleteAlert.setMessage("Are you sure you want to delete this medication?");
        deleteAlert.setTitle("Delete Medication!");
        deleteAlert.setCancelable(true);
        deleteAlert.setPositiveButton("Yes", (dialog, which) -> {
            ((ViewGroup) delButton.getParent().getParent()).removeAllViews();

            // Remove record from database
            MedTrackerDatabase db = MedTrackerDatabaseSingleton.getInstance(MedTracker.getAppContext());
            MedicationDao medDao = db.medicationDao();

            CardView cardView = (CardView) delButton.getParent();
            TextView textView = (TextView)cardView.findViewById(R.id.medInfo);
            Log.d("test", textView.getText().toString());

            List<Medication> medicationList = medDao.searchMedsByName(textView.getText().toString());
            for (Medication medication : medicationList) {
                medDao.deleteMed(medication);
            }
            dialog.cancel();
        });
        deleteAlert.setNegativeButton("No", (dialog, which) -> {
            dialog.cancel();
        });
        AlertDialog deleteDialog = deleteAlert.create();
        deleteDialog.show();
    }
}
