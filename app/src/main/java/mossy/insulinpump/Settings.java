package mossy.insulinpump;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Switch;

public class Settings extends AppCompatActivity {
    Switch vibrate_on_alert_switch;
    Switch vibrate_on_notification_switch;


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





}
