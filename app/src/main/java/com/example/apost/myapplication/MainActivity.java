package com.example.apost.myapplication;

import android.Manifest;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.media.AudioManager;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.SystemClock;
import android.provider.ContactsContract;
import android.provider.Settings;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.telephony.SmsManager;
import android.text.InputFilter;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;



import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static android.bluetooth.BluetoothAdapter.STATE_CONNECTED;
import static android.bluetooth.BluetoothGatt.GATT_SUCCESS;


//per bluetooth
public class MainActivity extends AppCompatActivity {
//VERSIONE FINALE
    //per bluetooth
    private final static int REQUEST_ENABLE_BT = 1;

    public UUID uuid = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

    BluetoothAdapter mBluetoothAdapter = null;
    BluetoothSocket mmSocket = null;
    BluetoothDevice mmDevice = null;
    OutputStream outStream;
    //SMS
    private static final int SEND_SMS_CODE = 23;


    ImageView i;
    String MAC;
    Intent intent;
    String phoneNumber;
    String number;

    public Boolean pesoce=false;

    Boolean connessione = false;

    TextView txtPeso;
    Ringtone r;

    ToggleButton tgb;

    private BluetoothGatt GattServer = null;

    private BluetoothGattService Servizio = null;
    private Boolean Connesso = false;
    private BluetoothGattCharacteristic Caratteristica = null;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ColorDrawable cd = new ColorDrawable(0xFFFF6666);
        getSupportActionBar().setBackgroundDrawable(cd);
        getSupportActionBar().setTitle("iSeat Application");
        // CONNESSIONE OFF


        pesoce=false;


        IntentFilter filter = new IntentFilter();
        filter.addAction(BluetoothDevice.ACTION_ACL_CONNECTED);
        filter.addAction(BluetoothDevice.ACTION_ACL_DISCONNECTED);
        filter.addAction(BluetoothDevice.ACTION_ACL_DISCONNECT_REQUESTED);
        registerReceiver(mAclConnectReceiver, filter);

        // BACKGROUND
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this);
        builder
                .setContentTitle("Title")
                .setContentText("content")
                .setSmallIcon(R.mipmap.ic_launcher)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC);


        Window window = getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            window.setStatusBarColor(Color.rgb(140,3,0));
            window.setNavigationBarColor(Color.rgb(140,3,0));
        }

        txtPeso = findViewById(R.id.txtViewPeso);
        // SMS PERMISSION
        requestSmsSendPermission();

        // DO NOT DISTURB PERMISSION

        final NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        intent = new Intent(Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS);
        startActivity(intent);
        // FINE DO NOT DISTURB

        i = (ImageView)findViewById(R.id.imageViewCry);
        i.setVisibility(View.VISIBLE);
        i.setImageDrawable(ContextCompat.getDrawable(MainActivity.this, R.drawable.baby));

        tgb = (ToggleButton) findViewById(R.id.tgb);

        //evento: tap sul togglebutton per la connessione del bluetooth
        tgb.setOnClickListener(new View.OnClickListener(){
            public void onClick(View view){
                if(tgb.isChecked()) {//controlla che sia attivo il toggle button
                    mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
                    //}
                    if (mBluetoothAdapter == null){ //controlla se il devices è supportato
                        // IL BLUETOOTH NON E' SUPPORTATO
                        Toast.makeText(MainActivity.this, "BlueTooth non supportato", Toast.LENGTH_LONG).show();
                        tgb.setChecked(false);
                        i.setImageDrawable(ContextCompat.getDrawable(MainActivity.this, R.drawable.babycry));
                    }
                    else{
                        if (!mBluetoothAdapter.isEnabled())//controlla che sia abilitato il devices
                        {
                            //  NON E' ABILITATO IL BLUETOOTH
                            tgb.setChecked(false);
                            i.setImageDrawable(ContextCompat.getDrawable(MainActivity.this, R.drawable.babycry));
                            connessione = false;
                            //Chiedo il permesso di abilitare il bluetooth se disattivato
                            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
                        }
                        else{
                            //  IL BLUETOOTH E' ABILITATO
                            mmDevice=mBluetoothAdapter.getRemoteDevice("00:21:13:04:B2:F3");
                            try{
                                mmSocket=mmDevice.createRfcommSocketToServiceRecord(uuid);
                            }
                            catch (IOException e){
                                tgb.setChecked(false);
                                i.setImageDrawable(ContextCompat.getDrawable(MainActivity.this, R.drawable.babycry));

                                connessione = false;
                            }
                            try{
                                // CONNETTE IL DISPOSITIVO TRAMITE IL SOCKET mmSocket
                                connessione = true;
                                mmSocket.connect();
                                mmSocket.getInputStream();
                                outStream = mmSocket.getOutputStream();
                                Toast.makeText(MainActivity.this,  mBluetoothAdapter.getName()+"",  Toast.LENGTH_SHORT).show();//bluetooth è connesso
                                // Qui la connessione è stata effettuata
                                if(mmSocket == null) Log.e("Socket", "Obj NULL");
                                ConnectedThread ct= new ConnectedThread(mmSocket);
                                ct.start();
                                i.setVisibility(View.VISIBLE);
                                i.setImageDrawable(ContextCompat.getDrawable(MainActivity.this, R.drawable.baby));
                            }
                            catch (IOException closeException){
                                tgb.setChecked(false);
                                i.setImageDrawable(ContextCompat.getDrawable(MainActivity.this, R.drawable.babycry));
                                try{
                                    //TENTA DI CHIUDERE IL SOCKET
                                    mmSocket.close();
                                    connessione = false;
                                }
                                catch (IOException ceXC){
                                }
                                Toast.makeText(MainActivity.this, "connessione non riuscita",  Toast.LENGTH_SHORT).show();
                            }}
                        //CHIUDE l'else di isEnabled
                    }  //CHIUDE l'else di mBluetoothAdapter == null
                }  // CHIUDE if (tgb.isChecked())
                else{
                    try{
                        //TENTA DI CHIUDERE IL SOCKET
                        tgb.setChecked(false);
                        i.setImageDrawable(ContextCompat.getDrawable(MainActivity.this, R.drawable.babycry));
                        outStream.close();
                        mmSocket.close();
                        connessione = false;
                    }
                    catch (IOException ceXC){}
                }
            } // CHIUDE public void OnClick(View view)
        });//chiude il tgb.listener
    }

    private BroadcastReceiver mAclConnectReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);

            if (BluetoothDevice.ACTION_ACL_CONNECTED.equals(intent.getAction())) {
                Log.i("AccessoryController", "ACL Connect Device: "+device.getName());
            }
            if (BluetoothDevice.ACTION_ACL_DISCONNECTED.equals(intent.getAction())
                    || BluetoothDevice.ACTION_ACL_DISCONNECT_REQUESTED.equals(intent.getAction())) {
                Log.i("AccessoryController", "ACL Disconnect Device: "+device.getName());
                closeConnection();
            }
        }
    };
    private void closeConnection() {
        try {
            Toast.makeText(this, pesoce+"", Toast.LENGTH_SHORT).show();
            if(pesoce==false) {
                outStream.close();
                mmSocket.close();
                connessione = false;
                tgb.setChecked(false);
                i.setImageDrawable(ContextCompat.getDrawable(MainActivity.this, R.drawable.baby));
            }
            else if(pesoce==true)
            {
                outStream.close();
                mmSocket.close();
                connessione = false;
                Toast.makeText(MainActivity.this, "Connessione caduta",  Toast.LENGTH_SHORT).show();
                tgb.setChecked(false);
                eventoSMS();
                allarmeTelefono();
                i.setImageDrawable(ContextCompat.getDrawable(MainActivity.this, R.drawable.babycry));
            }

        } catch (Exception e) { }
    }

    BluetoothGattCallback Callback = new BluetoothGattCallback() {
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            Log.e("Conn_res","status: " + status + " -- newState: " + newState);
            GattServer = gatt;
            String ToastText="";
            if(status == GATT_SUCCESS){
                if(newState == STATE_CONNECTED){
                    ToastText="Connesso";
                    Connesso = true;
                } else {
                    ToastText="Disconnesso";
                    Connesso = false;
                }
            } else {
                ToastText="Connessione non riuscita";
                Connesso = false;
            }

            final String finalToastText = ToastText;
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(getBaseContext(), finalToastText,Toast.LENGTH_LONG).show();
                }
            });
            if(Connesso) {
                GattServer.discoverServices();
            }
            super.onConnectionStateChange(gatt, status, newState);
        }
        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            super.onServicesDiscovered(gatt, status);
            GattServer = gatt;

            Servizio = GattServer.getServices().get(3);
            Caratteristica = Servizio.getCharacteristics().get(0);

        }

        @Override
        public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            // Log.e("letto:","-->" + characteristic.getStringValue(0));
            super.onCharacteristicRead(gatt, characteristic, status);
        }

        @Override
        public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            Log.e("scritto:","-->" + characteristic.getStringValue(0));
            super.onCharacteristicWrite(gatt, characteristic, status);
        }

        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
            Log.e("letto:","-->" + characteristic.getStringValue(0));
            //  Toast.makeText(getBaseContext(), characteristic.getStringValue(0), Toast.LENGTH_SHORT).show();
            super.onCharacteristicChanged(gatt, characteristic);
        }
    };
    public void btnRTelefoni_click(View v)
    {
        registroTelefoni();
    }

    private void registroTelefoni() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType(ContactsContract.CommonDataKinds.Phone.CONTENT_TYPE);
        startActivityForResult(intent, 1);
    }
// gestione numero telefono
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (data != null) {
            Uri uri = data.getData();

            if (uri != null) {
                Cursor c = null;
                try {
                    c = getContentResolver().query(uri, new String[]{
                                    ContactsContract.CommonDataKinds.Phone.NUMBER,
                                    ContactsContract.CommonDataKinds.Phone.TYPE },
                            null, null, null);

                    if (c != null && c.moveToFirst()) {
                        number = c.getString(0);
                        int type = c.getInt(1);
                        showSelectedNumber(type, number);
                    }
                } finally {
                    if (c != null) {
                        c.close();
                    }
                }
            }
        }

        phoneNumber=number;
    }
    public void showSelectedNumber(int type, String number) {
        Toast.makeText(this, number, Toast.LENGTH_LONG).show();
    }

    public void btnSms_click(View v) {

        if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.SEND_SMS)
                        == PackageManager.PERMISSION_GRANTED
                ) {
            eventoSMS(); }
        else {
            // richiedi permission
            requestSmsSendPermission();
        }
    }

    private void eventoSMS() {
       if (phoneNumber == null || phoneNumber == "") {
            Toast.makeText(MainActivity.this, "Prima bisogna registrare un numero d'emergenza",
                    Toast.LENGTH_LONG).show();
        } else {
            String messaggio = "Hai dimenticato il bambino in macchina ";
            SmsManager sms = SmsManager.getDefault();
            sms.sendTextMessage(phoneNumber, null, messaggio, null, null);

            Toast.makeText(MainActivity.this, "Messaggio d'emergenza inviato",
                    Toast.LENGTH_LONG).show();

        }


    }

    private void requestSmsSendPermission() {
        ActivityCompat.requestPermissions(this, new String[] { Manifest.permission.SEND_SMS },
                SEND_SMS_CODE);
    }




    private static final int NOTIFICATION_ID = 1;
    private static final String NOTIFICATION_CHANNEL_ID = "my_notification_channel";
    private static final String YES_ACTION = "com.tinbytes.simplenotificationapp.YES_ACTION";

    private Intent getNotificationIntent() {
        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        return intent;
    }
    NotificationCompat.Builder builder;
    @TargetApi(Build.VERSION_CODES.KITKAT)
    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    public void btnMessaggioPeso_click(View v)
    {
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        if ( (true == notificationManager.isNotificationPolicyAccessGranted())
                ) {
            allarmeTelefono(); }
        else {
            intent = new Intent(Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS);
            startActivity(intent);
        }
    }

    private void allarmeTelefono() {
        Intent yesIntent = getNotificationIntent();
        yesIntent.setAction(YES_ACTION);

        NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel notificationChannel = new NotificationChannel(NOTIFICATION_CHANNEL_ID, "My Notifications", NotificationManager.IMPORTANCE_DEFAULT);

            notificationChannel.setDescription("Channel description");
            notificationChannel.enableLights(true);
            notificationChannel.setLightColor(Color.RED);
            notificationChannel.setVibrationPattern(new long[]{0, 1000, 500, 1000});
            notificationChannel.enableVibration(true);
            notificationManager.createNotificationChannel(notificationChannel);
        }
        AudioManager mgr = (AudioManager)getSystemService(Context.AUDIO_SERVICE);
        mgr.setRingerMode(AudioManager.RINGER_MODE_NORMAL);

        Uri notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE);
        r = RingtoneManager.getRingtone(getApplicationContext(), notification);
        r.play();
        int valuess = 15;//range(0-15)
        mgr.setStreamVolume(AudioManager.STREAM_RING, valuess, 0);
        //Gestione Allarme
        builder = null;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.KITKAT) {
            builder = new NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)
                    .setVibrate(new long[]{0, 100, 100, 100, 100, 100})
                    .setContentIntent(PendingIntent.getActivity(this, 0, getNotificationIntent(), PendingIntent.FLAG_UPDATE_CURRENT))
                    .setSmallIcon(R.drawable.baby2)
                    .setLargeIcon(BitmapFactory.decodeResource(getApplicationContext().getResources(),R.drawable.babycry))
                    .setContentTitle("iSeat")
                    .setContentText("Hai dimenticato il bambino in auto!")
                    .setOngoing(true)
                    .addAction(new NotificationCompat.Action(
                            R.mipmap.ic_launcher,
                            "DISATTIVA",
                            PendingIntent.getActivity(this, 0, yesIntent, PendingIntent.FLAG_UPDATE_CURRENT)));
        }
        notificationManager.notify(NOTIFICATION_ID, builder.build());
    }

    @Override
    protected void onNewIntent(Intent intent) {
        processIntentAction(intent);
        super.onNewIntent(intent);
    }

    private void processIntentAction(Intent intent) {
        if (intent.getAction() != null) {
            switch (intent.getAction()) {
                case YES_ACTION:
                    Toast.makeText(this, "Allarme disattivato", Toast.LENGTH_SHORT).show();
                    r.stop();
                    NotificationManager notificationManager = (NotificationManager) getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);
                    notificationManager.cancelAll();

                    break;
            }
        }}
    private class ConnectedThread extends Thread {
        private final BluetoothSocket mmSocket;
        private final InputStream mmInStream;
        private final OutputStream mmOutStream;


        public ConnectedThread(BluetoothSocket socket) {

            this.mmSocket = socket;
            InputStream tmpIn = null;
            OutputStream tmpOut = null;

            // Prende gli input e gli output streams
            try {
                tmpIn = socket.getInputStream();
                tmpOut = socket.getOutputStream();
            } catch (IOException e) {
            }

            mmInStream = tmpIn;
            mmOutStream = tmpOut;
        }

        public void run() {
            final String str2 = "0";
            byte[] buffer = new byte[1024];
            int bytes; // bytes ritornati dal read()
            // Continua ad ascoltare InputStream
            while (true) {
                try {
                    // Qua entra
                    // Legge da InputStream
                    bytes = mmInStream.available();
                    if (bytes != 0) {
                        buffer = new byte[8];
                        SystemClock.sleep(1000); //pause and wait for rest of data. Adjust this depending on your sending speed.
                        bytes = mmInStream.available(); // how many bytes are ready to be read?
                        bytes = mmInStream.read(buffer, 0, bytes); // record how many bytes we actually read
                        final String str = new String(buffer);

                        Pattern p = Pattern.compile("[a-z]+|\\d+");
                        Matcher m = p.matcher(str);
                        ArrayList<String> allMatches = new ArrayList<>();
                        while (m.find()) {
                            allMatches.add(m.group());
                        }
                        final int a = Integer.parseInt(allMatches.get(0));

                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                if (a > 1000)
                                    pesoce = true;
                                else
                                    pesoce = false;
                                txtPeso.setText("Il peso sul seggiolino è: " + a+" g");

                            }
                        });
                    }
                } catch (IOException e) {
                    e.printStackTrace();

                    break;
            }
                if (BluetoothDevice.ACTION_ACL_DISCONNECTED.equals(intent.getAction())
                        || BluetoothDevice.ACTION_ACL_DISCONNECT_REQUESTED.equals(intent.getAction())) {

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            tgb.setChecked(false);
                            i.setImageDrawable(ContextCompat.getDrawable(MainActivity.this, R.drawable.babycry));
                        }
                        
                    });
                    closeConnection();
                }
            }

        }


    }

    @Override
    protected void onPause() {
        super.onPause();
    }
    @SuppressLint("MissingSuperCall")
    @Override
    public void onBackPressed()
    {

        // super.onBackPressed(); // Comment this super call to avoid calling finish() or fragmentmanager's backstack pop operation.
    }


}





