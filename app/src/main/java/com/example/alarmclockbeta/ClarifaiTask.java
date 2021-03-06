package com.example.alarmclockbeta;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.os.AsyncTask;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.io.File;
import java.util.List;

import clarifai2.api.ClarifaiBuilder;
import clarifai2.api.ClarifaiClient;
import clarifai2.dto.input.ClarifaiInput;
import clarifai2.dto.model.output.ClarifaiOutput;
import clarifai2.dto.prediction.Concept;

public class ClarifaiTask extends AsyncTask<File, Integer, Boolean> {

    TextView updateText;
    AlarmManager alarmManager;
    PendingIntent pendingIntent;
    Intent intent;
    MainActivity mainActivity;
    String object;

    public ClarifaiTask(TextView u, AlarmManager a, PendingIntent pi, Intent i, MainActivity m,
                        String o) {
        updateText = u;
        alarmManager = a;
        pendingIntent = pi;
        intent = i;
        mainActivity= m;
        object = o;
    }

    protected Boolean doInBackground(File... images) {
        // Connect to Clarifai using your API token
        ClarifaiClient client = new ClarifaiBuilder("463ce488c814494da85e3968102bf90c")
                .buildSync();
        List<ClarifaiOutput<Concept>> predictionResults = null;
        // For each photo we pass, send it off to Clarifai
        for (File image : images) {
            predictionResults = client.getDefaultModels().generalModel().predict()
                    .withInputs(ClarifaiInput.forImage(image)).executeSync().get();
            // Check if Clarifai thinks the photo contains the object we are looking for
            for (ClarifaiOutput<Concept> result : predictionResults)
                for (Concept datum : result.data())
                    if (datum.name().contains(object.toLowerCase()))
                        return true;
        }
        return false;
    }

    @Override
    protected void onPostExecute(Boolean aBoolean) {
        super.onPostExecute(aBoolean);
        if (aBoolean){
            updateText.setText("Alarm Off. Set Time (+)");
            mainActivity.onRestart(true);
        } else {
            updateText.setText("Try Again! No " + object + " found");
        }
    }

}
