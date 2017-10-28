package mossy.insulinpump;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Switch;

public class Settings extends AppCompatActivity {
    Switch vibrate_on_alert_switch;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        vibrate_on_alert_switch = (Switch) findViewById(R.id.vibrate_on_alert_switch);
        vibrate_on_alert_switch.setOnClickListener(new View.OnClickListener(){

        @Override
        public void onClick(View view){

            vibrate_on_alert_switch.setTextOn("On");
            vibrate_on_alert_switch.setTextOff("Off");

        }
    });
    }





}
