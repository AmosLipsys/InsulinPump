package mossy.insulinpump;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Locale;

public class ManualMode extends AppCompatActivity {


    // Initialise buttons and edits from layout
    private Button manual_inject_button, okay_butt, back_butt;
    private TextView pop_up_warning;
    private ImageView warning_icon;
    private EditText single_dosage_picker;
    private Switch manual_mode_switch;
    private SharedPreferences global_preferences;
    private int single_dose_amount;

    private InsulinLogDAOHelper insulin_log_DAO;
    ContentValues contentValues;
    SQLiteDatabase database_insulin;

    boolean manual_mode_enabled;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manual_mode);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

      // Initialise call buttons and values
        manual_inject_button = (Button) findViewById(R.id.manual_inject_button);
        single_dosage_picker = (EditText) findViewById(R.id.single_dosage_picker);
        manual_mode_switch = (Switch) findViewById(R.id.manual_mode_switch);
        okay_butt = (Button) findViewById(R.id.ok_butt);
        back_butt = (Button) findViewById(R.id.back_butt);
        warning_icon = (ImageView) findViewById(R.id.warning_image);
        pop_up_warning = (TextView) findViewById(R.id.pop_up_warning);
        global_preferences = getSharedPreferences("global_preferences", MODE_PRIVATE);
        manual_mode_enabled = global_preferences.getBoolean("manual_mode_enabled", false);
        single_dose_amount = global_preferences.getInt("single_dosage_amount", 1);

        manual_mode_switch.setChecked(manual_mode_enabled);
        single_dosage_picker.setText(String.format(Locale.ENGLISH,"%d", single_dose_amount));

        insulin_log_DAO = new InsulinLogDAOHelper(this);



    // Set initialize the inject button when manual mode is switched on

        manual_mode_switch.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if(manual_mode_switch.isChecked()){
                    manual_inject_button.setEnabled(true);
                }
                else {
                    manual_inject_button.setEnabled(false);
                    toggle_warning(false);
                }
            }
        });
        manual_inject_button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                toggle_warning(true);
            }
        });
        back_butt.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                toggle_warning(false);
            }
        });
        okay_butt.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                make_toast(String.format(Locale.ENGLISH,"Warning: \nInsulin Successfully Administered\nDose: %d",single_dose_amount));
                database_insulin = insulin_log_DAO.getWritableDatabase();
                contentValues = new ContentValues();
                contentValues.put("time", System.currentTimeMillis());
                contentValues.put("amount", single_dose_amount);
                database_insulin.insert("insulin_log", null, contentValues);
                toggle_warning(false);
            }
        });
        single_dosage_picker.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View view, int i, KeyEvent keyEvent) {
                try{
                    single_dose_amount = Integer.valueOf(single_dosage_picker.getText().toString());
                }
                catch (Exception e){
                    single_dose_amount = 1;
                }


                return false;
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
                startActivity(new Intent(ManualMode.this,Home.class));
                break;
            case R.id.action_dosage:
                startActivity(new Intent(ManualMode.this,Dosages.class));
                break;
            case R.id.action_manual_mode:
                startActivity(new Intent(ManualMode.this,ManualMode.class));
                break;
            case R.id.action_settings:
                startActivity(new Intent(ManualMode.this,Settings.class));
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    void toggle_warning(boolean enabled){
        int visibility;
        if(enabled){
            visibility = View.VISIBLE;
        }
        else {
            visibility = View.INVISIBLE;
        }
        pop_up_warning.setVisibility(visibility);
        warning_icon.setVisibility(visibility);
        okay_butt.setVisibility(visibility);
        back_butt.setVisibility(visibility);
        pop_up_warning.setEnabled(enabled);
        warning_icon.setEnabled(enabled);
        okay_butt.setEnabled(enabled);
        back_butt.setEnabled(enabled);
    }

    private void make_toast(CharSequence message){
        Context context = getApplicationContext();
        Toast toast = Toast.makeText(context, message, Toast.LENGTH_LONG);
        TextView v = (TextView) toast.getView().findViewById(android.R.id.message);
        v.setTextColor(android.R.color.holo_red_light);
        toast.show();
    }



}
