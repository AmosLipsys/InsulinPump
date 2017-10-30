package mossy.insulinpump;

import android.content.ComponentName;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.os.Vibrator;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import static android.provider.Settings.System.DEFAULT_ALARM_ALERT_URI;

public class Home extends AppCompatActivity {

    private static final String TAG=Home.class.getSimpleName();
    private Context mContext;
    private Intent serviceIntent;

    public static final int GET_GLUCOSE_LEVEL_FLAG =0, DELIVER_INSULIN_FLAG = 1;
    private boolean mIsBound;
    private int glucose_level;
    private int max_allowed_glucose_level = 700;

    Messenger randomNumberRequestMessenger, randomNumberReceiveMessenger;

    private TextView status_text_view;
    private Button button_blood_glucose_levels;
    private Button button_inject_insulin;
    private GraphView graphView;
    private ListView listView;

    private boolean want_insulin_log = true;


    private final Handler mHandler = new Handler();
    private Runnable mTimer2;
    private LineGraphSeries<DataPoint> blood_glucose_serires_data;
    private double graph2LastXValue = 5d;

    private InsulinLogDAOHelper insulin_log_DAO;
    private GlucoseLogDAOHelper glucose_log_DAO;
    SQLiteDatabase database_insulin;
    SQLiteDatabase database_glucose;
    ContentValues contentValues;
    int data_point_1, data_point_2, data_point_3, volume;

    private SharedPreferences global_preferences;
    boolean manual_mode_enabled;
    private boolean vibrate_on_alert,vibrate_on_notification;

    int min_single_dose, max_single_dose, max_daily_dose, dose_total;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        serviceIntent=new Intent(getApplicationContext(),InsulinPumpService.class);
        startService(serviceIntent);
        mContext=getApplicationContext();


        graphView = (GraphView) findViewById(R.id.graph);
        status_text_view = (TextView) findViewById(R.id.status_text_view);
        button_inject_insulin =(Button)findViewById(R.id.insulin_butt);
        button_blood_glucose_levels =(Button)findViewById(R.id.blood_glucose);
        listView = (ListView) findViewById(R.id.list_view);

        global_preferences = getSharedPreferences("global_preferences", MODE_PRIVATE);
        manual_mode_enabled = global_preferences.getBoolean("manual_mode_enabled", false);
        min_single_dose = global_preferences.getInt("min_single_dose", 1);
        max_single_dose = global_preferences.getInt("max_single_dose", 3);
        max_daily_dose = global_preferences.getInt("max_daily_dose", 25);
        dose_total = global_preferences.getInt("dose_total", 25);
        vibrate_on_alert = global_preferences.getBoolean("vibrate_on_alert", true);
        vibrate_on_notification = global_preferences.getBoolean("vibrate_on_notification", true);
        volume = global_preferences.getInt("volume", 0);



        //graph
        blood_glucose_serires_data = new LineGraphSeries<>();
        graphView.addSeries(blood_glucose_serires_data);
        graphView.getViewport().setXAxisBoundsManual(true);
        graphView.getViewport().setMinX(0);
        graphView.getViewport().setMaxX(7);
        graphView.getViewport().setMaxY(800);
        graphView.getViewport().setMinY(300);
        graphView.getViewport().setYAxisBoundsManual(true);
        graphView.getViewport().setXAxisBoundsManual(true);

        //database
        insulin_log_DAO = new InsulinLogDAOHelper(this);
        glucose_log_DAO = new GlucoseLogDAOHelper(this);

        // Set code for buttons
        button_blood_glucose_levels.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                want_insulin_log = false;
                populate_list_view();
            }
        });
        button_inject_insulin.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                want_insulin_log = true;
                populate_list_view();
            }
        });

        // Service is simulating the person
        serviceIntent=new Intent();
        serviceIntent.setComponent(new ComponentName("mossy.insulinpump","mossy.insulinpump.InsulinPumpService"));
        bindService(serviceIntent, randomNumberServiceConnection, BIND_AUTO_CREATE);




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
                startActivity(new Intent(Home.this,Home.class));
                break;
            case R.id.action_dosage:
                startActivity(new Intent(Home.this,Dosages.class));
                break;
            case R.id.action_manual_mode:
                startActivity(new Intent(Home.this,ManualMode.class));
                break;
            case R.id.action_settings:
                startActivity(new Intent(Home.this,Settings.class));
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onResume() {
        super.onResume();
        // Creates list on start up
        populate_list_view();

        //Graph
        mTimer2 = new Runnable() {
            @Override
            public void run() {
                fetch_glucose_level();
                mHandler.postDelayed(this, 3000);
            }
        };
        mHandler.postDelayed(mTimer2, 1000);

        // Populate Graph
        Cursor cursor;
        try {
            cursor = glucose_log_DAO.getReadableDatabase().rawQuery("select * from glucose_log", null);

            cursor.moveToLast();
            while(cursor.moveToNext()){
                graph2LastXValue += 1d;
                blood_glucose_serires_data.appendData(new DataPoint(graph2LastXValue, cursor.getInt(2)), true, 10);
            }
            cursor.close();


        }catch (Exception e){

        }



    }

    @Override
    public void onPause() {
        mHandler.removeCallbacks(mTimer2);
        super.onPause();
    }


    // What happens when the message is returned
    private class RecieveMessageHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            glucose_level =0;
            switch (msg.what) {
                // When we get the glucose levels
                case GET_GLUCOSE_LEVEL_FLAG:

                    glucose_level =msg.arg1;
                    status_text_view.setText("Glucose Level: "+ glucose_level);

                    Log.i(TAG,"New Data Point Taken:" + glucose_level );

                    //Data base
                    database_glucose = glucose_log_DAO.getWritableDatabase();

                    //Graph
                    graph2LastXValue += 1d;
                    blood_glucose_serires_data.appendData(new DataPoint(graph2LastXValue, glucose_level), true, 10);
                    data_point_1 = global_preferences.getInt("data_point_1", 0);
                    data_point_2 = global_preferences.getInt("data_point_2", 0);
                    data_point_3 = global_preferences.getInt("data_point_3", 0);

                    contentValues = new ContentValues();
                    contentValues.put("time", System.currentTimeMillis());
                    contentValues.put("amount", glucose_level);
                    database_glucose.insert("glucose_log", null, contentValues);
                    database_glucose.close();

                    data_point_3 = data_point_2;
                    data_point_2 = data_point_1;
                    data_point_1 = glucose_level;

                    global_preferences.edit()
                            .putInt("data_point_1", data_point_1)
                            .putInt("data_point_2", data_point_2)
                            .putInt("data_point_3", data_point_3)
                            .apply();

                            //*** Automatic insulin mode ***\\
                            // - Manual Mode Disabled
                            // - Glucose level is above max range
                            // - Last 3 Data points in a row
                            if(!manual_mode_enabled && glucose_level > max_allowed_glucose_level && data_point_1 > data_point_2 && data_point_2 > data_point_3) {
                                deliver_insulin();
                            }

                    String warning="";

                    if(manual_mode_enabled){
                        warning += "\nManual Mode Enabled";
                    }
                    if(glucose_level > max_allowed_glucose_level){
                        warning += "\nGlucose Levels Too High ";
                    }
                    String status_message="";
                    if(warning.isEmpty()){
                        status_message = "Status: Nothing to Report";
                        status_text_view.setBackgroundColor(Color.WHITE);
                    }
                    else {
                        if(vibrate_on_notification){
                            Vibrator vibrator = (Vibrator)getSystemService(Context.VIBRATOR_SERVICE);
                            vibrator.vibrate(100);
                        }
                        status_message = "Status: Warning!" + warning;
                        status_text_view.setBackgroundColor(Color.RED);
                    }
                    status_text_view.setText(status_message);

                    break;
                // Message saying insulin is delivered
                case DELIVER_INSULIN_FLAG:

                    Log.i(TAG,"Took Insulin");
                    make_toast(String.format(Locale.ENGLISH,"Warning: \nInsulin Successfully Administered\nDose: %d",2));
                    if(vibrate_on_alert){
                        Vibrator vibrator = (Vibrator)getSystemService(Context.VIBRATOR_SERVICE);
                        vibrator.vibrate(100);
                    }

                    if(volume!=0){
                        Uri notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
                        Ringtone r = RingtoneManager.getRingtone(getApplicationContext(), notification);
                        r.play();
                    }

                    // Database
                    contentValues = new ContentValues();
                    int dose;
                    if(max_daily_dose > max_single_dose + dose_total){
                        dose = max_single_dose;
                    }
                    else if(max_daily_dose > min_single_dose + dose_total){
                        dose = min_single_dose;
                    }
                    else {
                        dose = 0;
                    }

                    database_insulin = insulin_log_DAO.getWritableDatabase();
                    contentValues.put("time", System.currentTimeMillis());
                    contentValues.put("amount", dose);
                    database_insulin.insert("insulin_log", null, contentValues);

                    database_insulin = insulin_log_DAO.getReadableDatabase();
                    String yesterday = Long.toString(System.currentTimeMillis()-86400000);
                    Cursor c = database_insulin.rawQuery("SELECT SUM(amount) FROM insulin_log WHERE time > " + yesterday, null);
                    c.moveToFirst();
                    dose_total = c.getInt(0);

                    // Updates both list views
                    populate_list_view();
                    break;
                default:
                    break;
            }
            super.handleMessage(msg);
        }
    }

    ServiceConnection randomNumberServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            randomNumberRequestMessenger=null;
            randomNumberReceiveMessenger=null;
            mIsBound = false;
        }
        @Override
        public void onServiceConnected(ComponentName arg0, IBinder binder) {
            randomNumberRequestMessenger=new Messenger(binder);
            randomNumberReceiveMessenger=new Messenger(new RecieveMessageHandler());
            mIsBound=true;
        }
    };


    // Send messages to Service
    private void fetch_glucose_level(){

        if (mIsBound) {
            Message requestMessage=Message.obtain(null, GET_GLUCOSE_LEVEL_FLAG);
            requestMessage.replyTo=randomNumberReceiveMessenger;
            try {
                randomNumberRequestMessenger.send(requestMessage);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }else{
            Toast.makeText(mContext,"Service Unbound, can't get random number",Toast.LENGTH_SHORT).show();
        }
    }
    private void deliver_insulin(){
        if (mIsBound) {
            Message requestMessage=Message.obtain(null, DELIVER_INSULIN_FLAG);
            requestMessage.replyTo=randomNumberReceiveMessenger;
            try {
                randomNumberRequestMessenger.send(requestMessage);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }else{
            Toast.makeText(mContext,"Service Unbound, can't get glucose ",Toast.LENGTH_SHORT).show();
        }
    }



    @Override
    protected void onDestroy() {
        super.onDestroy();
        randomNumberServiceConnection=null;
    }

    void populate_list_view(){
        String format_text;
        Cursor cursor;
        // Display insulin or glucose logs
        try {
        if(want_insulin_log){
            format_text = "Insulin Dose: %s\nDate: %s";
            cursor= insulin_log_DAO.getReadableDatabase().rawQuery("select * from insulin_log", null);
        }
        else {
            format_text = "Glucose Level: %s\nDate: %s";
            cursor = glucose_log_DAO.getReadableDatabase().rawQuery("select * from glucose_log", null);
        }



            SimpleDateFormat sdf = new SimpleDateFormat("EEE, d MMM yyyy HH:mm:ss", Locale.ENGLISH);
            String[] array = new String[cursor.getCount()-1];
            String string;
            int i = 0;
            cursor.moveToLast();
            while(cursor.moveToPrevious()){
                string = String.format(format_text, cursor.getInt(2) , sdf.format(new Date(cursor.getLong(1))));
                if(!string.isEmpty()){
                    array[i] = string;
                    i++;
                }
            }
            cursor.close();
            ListAdapter listAdapter = new ArrayAdapter<String>(listView.getContext(), android.R.layout.simple_list_item_1, array);
            listView.setAdapter(listAdapter);
        }
        catch (Exception e){
            System.out.print(e);
            String[] array = {"No logs availiable"};
            ListAdapter listAdapter = new ArrayAdapter<String>(listView.getContext(), android.R.layout.simple_list_item_1, array );
            listView.setAdapter(listAdapter);
        }


    }

    private void make_toast(CharSequence message){
        LayoutInflater inflater = getLayoutInflater();
        View layout = inflater.inflate(R.layout.custom_toast,
                (ViewGroup) findViewById(R.id.custom_toast_layout_id));
        TextView text = (TextView) layout.findViewById(R.id.text);
        text.setText(message);
        // Toast...
        Toast toast = new Toast(getApplicationContext());
        toast.setGravity(Gravity.BOTTOM, 0,60);
        toast.setDuration(Toast.LENGTH_LONG);
        toast.setView(layout);
        toast.show();

//        Context context = getApplicationContext();
//        Toast toast = Toast.makeText(context, message, Toast.LENGTH_LONG);
//        TextView v = (TextView) toast.getView().findViewById(android.R.id.message);
//        v.setTextColor(Color.RED);
//
//        toast.show();
    }

}
