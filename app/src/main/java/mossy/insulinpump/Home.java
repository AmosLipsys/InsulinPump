package mossy.insulinpump;

import android.content.ComponentName;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

import java.lang.reflect.Array;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Random;

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
    private LineGraphSeries<DataPoint> mSeries2;
    private double graph2LastXValue = 5d;

    private InsulinLogDAOHelper insulin_log_DAO;
    private GlucoseLogDAOHelper glucose_log_DAO;
    SQLiteDatabase database_insulin;
    SQLiteDatabase database_glucose;

    ContentValues contentValues;


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


        //graph
        mSeries2 = new LineGraphSeries<>();
        graphView.addSeries(mSeries2);
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
                    mSeries2.appendData(new DataPoint(graph2LastXValue, glucose_level), true, 10);
                    contentValues = new ContentValues();
                    contentValues.put("time", System.currentTimeMillis());
                    contentValues.put("amount", glucose_level);
                    database_glucose.insert("glucose_log", null, contentValues);

                    // Automatic insulin mode
                    if(glucose_level > max_allowed_glucose_level) {
                        deliver_insulin();
                    }
                    break;
                // Message saying insulin is delivered
                case DELIVER_INSULIN_FLAG:

                    Log.i(TAG,"Took Insulin");
                    // Database
                    database_insulin = insulin_log_DAO.getWritableDatabase();
                    contentValues = new ContentValues();
                    contentValues.put("time", System.currentTimeMillis());
                    contentValues.put("amount", 2);
                    database_insulin.insert("insulin_log", null, contentValues);

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
}
