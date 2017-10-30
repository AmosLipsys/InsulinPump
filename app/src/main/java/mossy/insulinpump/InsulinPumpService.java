package mossy.insulinpump;

import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.util.Log;
import android.widget.Toast;
import java.util.Random;


/**
 * Created by Amos on 29-Oct-17.
 */

public class InsulinPumpService extends Service {
    private static final String TAG=InsulinPumpService.class.getSimpleName();

    private int blood_glucose_level = 500;
    private boolean mIsRandomGeneratorOn;

    private int min_increase =-2;
    private int max_increase =10;
    private int min_blood_glucose =250;
    private int max_blood_glucose =790;

    private int[] insulin = {0,0,0,0,0};

    public static final int GET_GLUCOSE_LEVEL_FLAG =0, DELIVER_INSULIN_FLAG = 1;

    private class RandomNumberRequestHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            Message  message;
            switch (msg.what){
                case GET_GLUCOSE_LEVEL_FLAG:
                    message=Message.obtain(null, GET_GLUCOSE_LEVEL_FLAG);

                    message.arg1=getRandomNumber();
                    try{
                        msg.replyTo.send(message);
                    }catch (RemoteException e){
                        Log.i(TAG,""+e.getMessage());
                    }
                    break;
                case DELIVER_INSULIN_FLAG :
                    message=Message.obtain(null, DELIVER_INSULIN_FLAG);
                    deliver_insulin();
                    try{
                        msg.replyTo.send(message);
                    }catch (RemoteException e){
                        Log.i(TAG,""+e.getMessage());
                    }
            }
            super.handleMessage(msg);
        }
    }

    private void deliver_insulin() {
        insulin[0] += 10;
        insulin[1] += 20;
        insulin[2] += 30;
        insulin[3] += 20;
        insulin[4] += 10;
    }

    private Messenger randomNumberMessenger=new Messenger(new RandomNumberRequestHandler());


    @Override
    public IBinder onBind(Intent intent) {
        return randomNumberMessenger.getBinder();
    }

    @Override
    public void onRebind(Intent intent) {
        super.onRebind(intent);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        stopRandomNumberGenerator();
    }



    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        mIsRandomGeneratorOn =true;
        new Thread(new Runnable() {
            @Override
            public void run() {
                start_glucose_levels_generator();
            }
        }).start();
        return START_REDELIVER_INTENT;
    }

    private void start_glucose_levels_generator(){
        while (mIsRandomGeneratorOn){
            try{
                Thread.sleep(1000);
                if(mIsRandomGeneratorOn){
                    blood_glucose_level = blood_glucose_level + (new Random().nextInt(max_increase)+ min_increase)- insulin[0];
                    if(blood_glucose_level > max_blood_glucose){
                        blood_glucose_level = max_blood_glucose;
                    }
                    else if(blood_glucose_level < min_blood_glucose){
                        blood_glucose_level = min_blood_glucose;
                    }
                    insulin[0] = insulin[1];
                    insulin[1] = insulin[2];
                    insulin[2] = insulin[3];
                    insulin[3] = insulin[4];
                    insulin[4] = 0;

                    Log.i(TAG,"Random Number: "+ blood_glucose_level+ " Insulin Levels: " + insulin[0] + " " + insulin[1] + " " + insulin[2]+ " " + insulin[3]+ " " + insulin[4] );

                }
            }catch (InterruptedException e){
                Log.i(TAG,"Thread Interrupted");
            }

        }
    }

    private void stopRandomNumberGenerator(){
        mIsRandomGeneratorOn =false;
        Toast.makeText(getApplicationContext(),"Service Stopped",Toast.LENGTH_SHORT).show();
    }

    @Override
    public boolean onUnbind(Intent intent) {

        return super.onUnbind(intent);
    }

    public int getRandomNumber(){
        return blood_glucose_level;
    }


}
