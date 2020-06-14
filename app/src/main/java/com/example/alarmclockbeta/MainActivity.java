package com.example.alarmclockbeta;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.DialogFragment;

import android.view.View;

import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.TextView;
import android.widget.TimePicker;
import android.text.format.DateFormat;

import org.w3c.dom.Text;

import java.util.Calendar;

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        this.context = this;

        alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
        updateText = (TextView) findViewById(R.id.textview_first);
        intent = new Intent(this.context, AlarmReceiver.class);

        FloatingActionButton fab = findViewById(R.id.NewAlarm);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                c = Calendar.getInstance();
                int hour = c.get(Calendar.HOUR_OF_DAY);
                int minutes = c.get(Calendar.MINUTE);
                // time picker dialog
                picker = new TimePickerDialog(MainActivity.this,
                        new TimePickerDialog.OnTimeSetListener() {
                            @Override
                            public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                                c.set(Calendar.HOUR_OF_DAY, hourOfDay);
                                c.set(Calendar.MINUTE, minute);
                                boolean isAM = true;

                                if(!DateFormat.is24HourFormat(MainActivity.this))
                                    if (hourOfDay == 0) {
                                        hourOfDay = 12;
                                    }
                                if (hourOfDay > 12){
                                    hourOfDay -= 12;
                                    isAM = false;
                                }
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

                                pendingIntent = PendingIntent.getBroadcast(MainActivity.this, 0,
                                        intent, PendingIntent.FLAG_UPDATE_CURRENT);

                                alarmManager.set(AlarmManager.RTC_WAKEUP, c.getTimeInMillis(), pendingIntent);
                            }
                        }, hour, minutes, false);
                picker.show();

            }
        });

        Button endAlarm = (Button) findViewById(R.id.end_alarm);
        endAlarm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                alarmManager.cancel(pendingIntent);
                set_text("Alarm Off. Set Time");
                intent.putExtra("extra", "off");
                sendBroadcast(intent);
            }
        });
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