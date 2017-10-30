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
import android.widget.Button;
import android.widget.EditText;
import android.widget.NumberPicker;
import android.widget.Switch;

import com.jjoe64.graphview.GraphView;

public class ManualMode extends AppCompatActivity {


    // Initialise buttons and edits from layout
    private Button manual_inject_button;
    private EditText single_dosage_picker;
    private Switch manual_mode_swith;

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
        manual_mode_swith = (Switch) findViewById(R.id.manual_mode_switch);

    // Set initialize the inject button when manual mode is switched on

        manual_mode_swith.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                manual_inject_button.setEnabled(true);

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

}
