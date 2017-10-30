package mossy.insulinpump;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.CompoundButton;
import android.widget.SeekBar;
import android.widget.Switch;

public class Settings extends AppCompatActivity {
    Switch vibrate_on_alert_switch;
    Switch vibrate_on_notification_switch;
    SeekBar volume_bar;
    private SharedPreferences global_preferences;
    private boolean vibrate_on_alert,vibrate_on_notification;
    int volume;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        global_preferences = getSharedPreferences("global_preferences", MODE_PRIVATE);
        vibrate_on_alert = global_preferences.getBoolean("vibrate_on_alert", true);
        volume = global_preferences.getInt("volume", 0);
        vibrate_on_notification = global_preferences.getBoolean("vibrate_on_notification", true);
        vibrate_on_alert_switch = (Switch) findViewById(R.id.vibrate_on_alert_switch);
        vibrate_on_notification_switch = (Switch) findViewById(R.id.vibrate_on_notification_switch);
        volume_bar = (SeekBar) findViewById(R.id.volume_seek_bar);


        vibrate_on_alert_switch.setChecked(vibrate_on_alert);
        vibrate_on_notification_switch.setChecked(vibrate_on_notification);


        volume_bar.setProgress(volume);

        vibrate_on_alert_switch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                vibrate_on_alert = isChecked;
            }
        });
        vibrate_on_notification_switch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                vibrate_on_alert = isChecked;
            }
        });

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
        switch (id){
            case R.id.action_home:
                startActivity(new Intent(Settings.this,Home.class));
                break;
            case R.id.action_dosage:
                startActivity(new Intent(Settings.this,Dosages.class));
                break;
            case R.id.action_manual_mode:
                startActivity(new Intent(Settings.this,ManualMode.class));
                break;
            case R.id.action_settings:
                startActivity(new Intent(Settings.this,Settings.class));
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onPause(){
        super.onPause();
        global_preferences = getSharedPreferences("global_preferences", MODE_PRIVATE);
        volume = volume_bar.getProgress();
        global_preferences.edit()
                .putBoolean("vibrate_on_alert", vibrate_on_alert)
                .putBoolean("vibrate_on_notification", vibrate_on_notification)
                .putInt("volume", volume)
                .apply();
    }





}
