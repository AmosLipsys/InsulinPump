package mossy.insulinpump;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

public class Dosages extends AppCompatActivity {
    private SharedPreferences global_preferences;
    EditText min_single_dose_text_view;
    EditText max_single_dose_text_view;
    EditText max_daily_dose_text_view;
    Button confirm_butt;
    Button reset_button;
    int min_single_dose;
    int  max_single_dose;
    int max_daily_dose;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dosages);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        // Load Shared Preferences
        global_preferences = getSharedPreferences("global_preferences", MODE_PRIVATE);
        min_single_dose =global_preferences.getInt("min_single_dose", 2);
        max_single_dose = global_preferences.getInt("max_single_dose", 5);
        max_daily_dose = global_preferences.getInt("max_daily_dose", 25);

        min_single_dose_text_view = (EditText) findViewById(R.id.minimum_single_dosage_picker);
        max_single_dose_text_view = (EditText) findViewById(R.id.maximum_single_dosage_picker);
        max_daily_dose_text_view = (EditText) findViewById(R.id.maximum_daily_dosage_picker);
        confirm_butt = (Button) findViewById(R.id.confirm_changes_button);
        reset_button = (Button) findViewById(R.id.reset_button);
        update_edit_text_fields();

        reset_button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                min_single_dose = 2;
                max_single_dose = 5;
                max_daily_dose = 25;
                update_edit_text_fields();
            }
        });

        confirm_butt.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                min_single_dose = Integer.valueOf(min_single_dose_text_view.getText().toString());
                max_single_dose = Integer.valueOf(max_single_dose_text_view.getText().toString());;
                max_daily_dose = Integer.valueOf(max_single_dose_text_view.getText().toString());;
                update_edit_text_fields();
            }
        });

        min_single_dose_text_view.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean b) {
                min_single_dose = Integer.valueOf(min_single_dose_text_view.getText().toString());
                validate_values();
                update_edit_text_fields();
            }
        });
        max_single_dose_text_view.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean b) {
                max_single_dose = Integer.valueOf(max_single_dose_text_view.getText().toString());
                validate_values();
                update_edit_text_fields();
            }
        });
        max_daily_dose_text_view.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean b) {
                max_daily_dose = Integer.valueOf(max_daily_dose_text_view.getText().toString());
                validate_values();
                update_edit_text_fields();
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
                startActivity(new Intent(Dosages.this,Home.class));
                break;
            case R.id.action_dosage:
                startActivity(new Intent(Dosages.this,Dosages.class));
                break;
            case R.id.action_manual_mode:
                startActivity(new Intent(Dosages.this,ManualMode.class));
                break;
            case R.id.action_settings:
                startActivity(new Intent(Dosages.this,Settings.class));
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void validate_values(){
        if(min_single_dose>max_single_dose){
            max_single_dose = min_single_dose;
        }
        if(max_daily_dose<max_single_dose){
            max_daily_dose = max_single_dose;
        }
    }

    private void update_edit_text_fields(){
        min_single_dose_text_view.setText(Integer.toString(min_single_dose));
        max_single_dose_text_view.setText(Integer.toString(max_single_dose));
        max_daily_dose_text_view.setText(Integer.toString(max_daily_dose));
    }

    @Override
    public void onPause(){
        super.onPause();
        global_preferences = getSharedPreferences("global_preferences", MODE_PRIVATE);
        global_preferences.edit()
                .putInt("min_single_dose", min_single_dose)
                .putInt("max_single_dose", max_single_dose)
                .putInt("max_daily_dose", max_daily_dose)
                .apply();
    }


}
