package com.example.alarmclockbeta;

import android.Manifest;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;

import clarifai2.api.ClarifaiBuilder;
import clarifai2.api.ClarifaiClient;
import clarifai2.dto.input.ClarifaiInput;
import clarifai2.dto.model.output.ClarifaiOutput;
import clarifai2.dto.prediction.Concept;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.FileProvider;
import androidx.fragment.app.DialogFragment;

import android.os.Environment;
import android.provider.MediaStore;
import android.view.View;

import android.view.Menu;
import android.view.MenuItem;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;
import android.text.format.DateFormat;

import org.w3c.dom.Text;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

import static android.os.Environment.getExternalStoragePublicDirectory;

public class MainActivity extends AppCompatActivity implements TimePickerDialog.OnTimeSetListener {

    private int hour;
    private int minutes;
    AlarmManager alarmManager;
    TimePicker timePicker;
    TextView updateText;
    Context context;
    PendingIntent pendingIntent;
    Calendar c;
    Intent intent;
    TimePickerDialog picker;
    String pathtofile;
    ImageView imageview;
    String defaultObject;
    Spinner dropdown;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if(Build.VERSION.SDK_INT>=23){
            requestPermissions(new String[]{Manifest.permission.CAMERA,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE},2);
        }

        context = this;
        alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
        updateText = (TextView) findViewById(R.id.textview_first);
        intent = new Intent(this.context, AlarmReceiver.class);

        dropdown = (Spinner) findViewById(R.id.spinnerObjects);
        String[] items = new String[]{"Bathroom","Computer","Keyboard","Bed"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, items);
        dropdown.setAdapter(adapter);
        defaultObject = "computer";

        FloatingActionButton fab = findViewById(R.id.NewAlarm);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                c = Calendar.getInstance();
                int hour = c.get(Calendar.HOUR_OF_DAY);
                final int minutes = c.get(Calendar.MINUTE);
                // time picker dialog
                picker = new TimePickerDialog(MainActivity.this,
                        new TimePickerDialog.OnTimeSetListener() {
                            @Override
                            public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                                Calendar cur_cal = new GregorianCalendar();
                                cur_cal.setTimeInMillis(System.currentTimeMillis());
                                if (cur_cal.get(Calendar.HOUR_OF_DAY) > hourOfDay)
                                    c.set(Calendar.DAY_OF_YEAR, Calendar.DAY_OF_YEAR+1);
                                else if (cur_cal.get(Calendar.HOUR_OF_DAY) == hourOfDay && cur_cal.get(Calendar.MINUTE)> minute)
                                    c.set(Calendar.DAY_OF_YEAR, Calendar.DAY_OF_YEAR+1);

                                c.set(Calendar.HOUR_OF_DAY, hourOfDay);
                                c.set(Calendar.MINUTE, minute);
                                c.set(Calendar.SECOND, 0);
                                c.set(Calendar.MILLISECOND, 0);
                                boolean isAM = true;

                                if(!DateFormat.is24HourFormat(MainActivity.this))
                                    if (hourOfDay == 0) {
                                        hourOfDay = 12;
                                    }
                                if (hourOfDay > 12){
                                    hourOfDay -= 12;
                                    isAM = false;
                                }
                                if (hourOfDay == 12) isAM = false;
                                if(minute<10){
                                    if (isAM) {
                                        updateText.setText("Alarm Set To " + hourOfDay + ":0" + minute + " AM");
                                    } else {
                                        updateText.setText("Alarm Set To " + hourOfDay + ":0" + minute + " PM");
                                    }
                                } else {
                                    if (isAM) {
                                        updateText.setText("Alarm Set To " + hourOfDay + ":" + minute + " AM");
                                    } else {
                                        updateText.setText("Alarm Set To " + hourOfDay + ":" + minute + " PM");
                                    }
                                }

                                intent.putExtra("extra", "on");

                                pendingIntent = PendingIntent.getBroadcast(
                                        MainActivity.this, 0,
                                        intent, PendingIntent.FLAG_UPDATE_CURRENT);

                                alarmManager.set(AlarmManager.RTC_WAKEUP, c.getTime().getTime(),
                                        pendingIntent);
                            }
                        }, hour, minutes, false);
                picker.show();

            }
        });

        Button endAlarm = findViewById(R.id.end_alarm);
        endAlarm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                   Intent camera = new Intent();
                   camera.setAction("android.media.action.IMAGE_CAPTURE");
                   if(camera.resolveActivity(getPackageManager()) != null) {
                       File photofile = null;
                       File image = null;
                       String name = new SimpleDateFormat("yyyMMdd_HHmmss")
                               .format(new Date());
                       File storageDir =
                               getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
                       try {
                           image = File.createTempFile(name, ".jpg", storageDir);
                       } catch (Exception e) {
                           e.printStackTrace();
                       }
                       photofile = image;
                       if (photofile != null) {
                           pathtofile = photofile.getAbsolutePath();
                           Uri photoURI = FileProvider.getUriForFile(MainActivity.this,
                                   "com.example.alarmclockbeta.fileprovier", photofile);
                           camera.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                           startActivityForResult(camera, 1);
                       }
                   }
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data){
        super.onActivityResult(requestCode,resultCode,data);
        if(resultCode == RESULT_OK){
            if(requestCode == 1){
                set_text("Processing...");
            }
        }
    }

//    AFTER RETURNING FROM THE CAMERA, GO SUBMIT PIC TO API IN CLARIFAITASK
    @Override
    public void onResume() {
        super.onResume();
        if (pathtofile != null) {
            defaultObject = String.valueOf(dropdown.getSelectedItem());
            new ClarifaiTask(pathtofile, updateText, alarmManager, pendingIntent, intent, MainActivity.this,defaultObject)
                    .execute(new File(pathtofile));
        }
    }

//  IF THE PICTURE MATCHES WHAT YOU WANT IT TO MATCH, TURN OFF ALARM
    protected void onRestart(boolean yeet) {
        alarmManager.cancel(pendingIntent);
        intent.putExtra("extra", "off");
        sendBroadcast(intent);
    }

    private void set_text(String txt){
        updateText.setText(txt);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {}
}
