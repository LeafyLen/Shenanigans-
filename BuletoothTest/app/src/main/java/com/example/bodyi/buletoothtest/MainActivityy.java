package votea.koro.com.locvo;

import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public class MainActivity extends AppCompatActivity {

    ListView joinView;

    Handler bHandler;
    int REQUEST_ENABLE_BT = 1;
    //Set<BluetoothDevice> pairedDevices;
    String TAGG = "myApp";
    int READ = 0;
    String name = ""; // username
    UUID my_uuid;
    BluetoothAdapter bAdapter;
    ActionBar mActionBar;
    AlertDialog dialog;

    String MY_UUID;

    private BluetoothAdapter BA;
    private Set<BluetoothDevice> pairedDevices;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //initialize();

        setUpBluetooth();


        // Remove the action bar
        mActionBar = getSupportActionBar();
        mActionBar.setDisplayShowCustomEnabled(true);
        mActionBar.setTitle("");
        TextView v = (TextView)getLayoutInflater().inflate(R.layout.texthead, null);
        format(v);
        mActionBar.setCustomView(v);
        mActionBar.hide();

        showUI();

        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        registerReceiver(mReceiver, filter);

        //TelephonyManager tManager = (TelephonyManager;)getSystemService(Context.TELEPHONY_SERVICE);
        MY_UUID = UUID.randomUUID().toString();
    }

    public void setUpBluetooth() {
        BA = BluetoothAdapter.getDefaultAdapter();

        bton();

        Intent discoverableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
        discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300);
        startActivity(discoverableIntent);
    }

    public void bton(){
        if (!BA.isEnabled()) {
            Intent turnOn = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(turnOn, 0);
            Toast.makeText(getApplicationContext(), "Turned on", Toast.LENGTH_LONG).show();
        } else {
            Toast.makeText(getApplicationContext(), "Already on", Toast.LENGTH_LONG).show();
        }
    }

    public void btoff(){
        BA.disable();
        Toast.makeText(getApplicationContext(), "Turned off" ,Toast.LENGTH_LONG).show();
    }

    public  void btvisible(){
        /*Intent getVisible = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
        startActivityForResult(getVisible, 0);*/
        
    }

    List<String> listtt;
    ArrayAdapter adaptertt;
    List<BluetoothDevice> listttt;

    public void list(){
        pairedDevices = BA.getBondedDevices();

        listtt = new ArrayList<String>();
        listttt = new ArrayList<BluetoothDevice>();

        Toast.makeText(getApplicationContext(), "Showing Paired Devices",Toast.LENGTH_SHORT).show();

        adaptertt = new  ArrayAdapter(this,android.R.layout.simple_list_item_1, listtt);

        joinView.setAdapter(adaptertt);
    }

    public void showJoin() {
        setContentView(R.layout.join_layout);

        joinView = (ListView)findViewById(R.id.listview_join);

        list();

        Set<BluetoothDevice> pairedDevices = BA.getBondedDevices();

        if (pairedDevices.size() > 0) {
            Log.d("myApp", "Pairing devices...");
            for (BluetoothDevice device : pairedDevices) {
                String deviceName = device.getName();
                String deviceHardwareAddress = device.getAddress();
                Log.d("myApp", "Device: " + deviceName);
                adaptertt.add(deviceName);
                adaptertt.notifyDataSetChanged();
                listttt.add(device);
            }
        }

        joinView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                new ConnectThread(listttt.get(i)).start();
            }
        });

        IntentFilter filter = new IntentFilter();

        filter.addAction(BluetoothDevice.ACTION_FOUND);
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED);
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);

        registerReceiver(mReceiver, filter);
    }


    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            if (BluetoothAdapter.ACTION_DISCOVERY_STARTED.equals(action)) {
                //discovery starts, we can show progress dialog or perform other tasks
            } else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
                //discovery finishes, dismis progress dialog
            } else if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                //bluetooth device found
                BluetoothDevice device = (BluetoothDevice) intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);

                Log.d(TAG, "Found device " + device.getName());
            }
        }
    };

    private class ConnectThread extends Thread {
        private final BluetoothSocket mmSocket;
        private final BluetoothDevice mmDevice;

        public ConnectThread(BluetoothDevice device) {
            // Use a temporary object that is later assigned to mmSocket
            // because mmSocket is final.
            BluetoothSocket tmp = null;
            mmDevice = device;

            try {
                // Get a BluetoothSocket to connect with the given BluetoothDevice.
                // MY_UUID is the app's UUID string, also used in the server code.
                tmp = device.createRfcommSocketToServiceRecord(UUID.fromString(MY_UUID));
            } catch (IOException e) {
                Log.e(TAG, "Socket's create() method failed", e);
            }
            mmSocket = tmp;
        }

        public void run() {
            // Cancel discovery because it otherwise slows down the connection.
            BA.cancelDiscovery();

            try {
                // Connect to the remote device through the socket. This call blocks
                // until it succeeds or throws an exception.
                mmSocket.connect();
            } catch (IOException connectException) {
                // Unable to connect; close the socket and return.
                try {
                    mmSocket.close();
                } catch (IOException closeException) {
                    Log.e(TAG, "Could not close the client socket", closeException);
                }
                return;
            }

            // The connection attempt succeeded. Perform work associated with
            // the connection in a separate thread.
            manageMyConnectedSocket(mmSocket);
        }

        // Closes the client socket and causes the thread to finish.
        public void cancel() {
            try {
                mmSocket.close();
            } catch (IOException e) {
                Log.e(TAG, "Could not close the client socket", e);
            }
        }
    }

    public void manageMyConnectedSocket(BluetoothSocket socket) {
        new ConnectedThread(socket).start();
    }

    private class AcceptThread extends Thread {
        private final BluetoothServerSocket mmServerSocket;

        public AcceptThread() {
            BluetoothServerSocket tmp = null;
            try {
                tmp = BA.listenUsingRfcommWithServiceRecord(android.os.Build.MODEL + ":D", UUID.fromString(MY_UUID));
            } catch (IOException e) {
                Log.e(TAG, "Socket's listen() method failed", e);
            }
            mmServerSocket = tmp;
        }

        public void run() {
            BluetoothSocket socket = null;
            // Keep listening until exception occurs or a socket is returned.
            while (true) {
                try {
                    socket = mmServerSocket.accept();
                } catch (IOException e) {
                    Log.e(TAG, "Socket's accept() method failed", e);
                    break;
                }

                if (socket != null) {
                    // A connection was accepted. Perform work associated with
                    // the connection in a separate thread.
                    manageMyConnectedSocket(socket);
                    try {
                        mmServerSocket.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    break;
                }
            }
        }

        // Closes the connect socket and causes the thread to finish.
        public void cancel() {
            try {
                mmServerSocket.close();
            } catch (IOException e) {
                Log.e(TAG, "Could not close the connect socket", e);
            }
        }
    }

/*
    // Initialize bluetooth
    public void initialize() {
        pairedDevices = new HashSet<BluetoothDevice>();
        my_uuid = new UUID(4234234, 32423423);
        setUpHandler();
    }

    // Set up the bluetooth devices
    public void setUpBluetooth() {
        bAdapter = BluetoothAdapter.getDefaultAdapter();
        if (bAdapter == null) {
            // Device does not support Bluetooth
        } else if (!bAdapter.isEnabled()) { // Request permissions
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        }
        // Find paired devices
        try {
            Set<BluetoothDevice> devices = bAdapter.getBondedDevices(); // Check for paired devices
        } catch (Exception e) {
            Log.e(TAG, "setUpBluetooth() error getting bonded devices >> " + e.toString());
        }
    }*/

    // Shows the UI
    public void showUI() {
        setContentView(R.layout.activity_main);

        // Formats the layouts/textviews
        RelativeLayout join = (RelativeLayout)findViewById(R.id.joinLayout);
        join.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //showJoinLayout();
                showJoin();
            }
        });
        format((TextView)findViewById(R.id.joinAVote));
        RelativeLayout start = (RelativeLayout)findViewById(R.id.startLayout);
        start.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showCreateDialog();
            }
        });
        format((TextView) findViewById(R.id.startAVote));
    }

    // Discovers bluetooth devices
    /*public void discoverDevices() {
        if (bAdapter.isDiscovering()){
            bAdapter.cancelDiscovery();
        }
        pairedDevices.clear();
        bAdapter.startDiscovery();
        mReceiver = new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                if (BluetoothDevice.ACTION_FOUND.equals(action)) { // Found device
                    BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                    pairedDevices.add(device);
                }
            }
        };
        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        registerReceiver(mReceiver, filter);

        new ServerThread().start();
    }

    /*public void makeDiscoverable(int time // time in seconds. Ex: 300 = 5 minutes
                                            ) {
        Intent discoverableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
        discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, time);
        startActivity(discoverableIntent);
    }*/

    // Parent method that's called whenever the app is closed
    @Override
    public void onDestroy() {
        super.onDestroy();
        /*if (mReceiver != null) {
            unregisterReceiver(mReceiver);
        }*/
        btoff();
        unregisterReceiver(mReceiver);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch(id) {
            case R.id.action_settings:
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    /*// Started from discoverDevices()
    private class ServerThread extends Thread {
        private final BluetoothServerSocket serverSocket;
        private boolean socketFinding = true;

        public ServerThread() {
            BluetoothServerSocket temp = null;
            try { // MY_UUID is the app's UUID string, also used by the client code
                temp = bAdapter.listenUsingRfcommWithServiceRecord(name, my_uuid);
            } catch (IOException e) { Log.v(TAG, "Err using Rfcomm >> " + e.toString()); }
            serverSocket = temp;
        }

        public void run() {
            socketFinding = true;
            BluetoothSocket socket = null;
            while (socketFinding) {
                try {
                    socket = serverSocket.accept();
                } catch (IOException e) {
                    close();
                }
                if (socket != null) {
                    manageConnectedSocket(socket);
                }
            }
        }

        public void close() {
            socketFinding = false;
            try {
                serverSocket.close();
            } catch (IOException e) {
                Log.v(TAG, "Server sockety closing >> " + e.toString());
            }
        }
    }

    // Should be started on button press
    private class ClientThread extends Thread {
        private final BluetoothSocket socket;
        private final BluetoothDevice device;

        public ClientThread(BluetoothDevice device) {
            BluetoothSocket temp = null;
            this.device = device;
            try {
                temp = device.createRfcommSocketToServiceRecord(my_uuid);
            } catch (IOException e) { }
            socket = temp;
        }

        public void run() {
            if (bAdapter.isDiscovering()) {
                bAdapter.cancelDiscovery();
            }
            try {
                socket.connect();
            } catch (IOException connectException) {
                try {
                    socket.close();
                } catch (IOException closeException) {
                    Toast.makeText(MainActivity.this, "Failed to connect.", Toast.LENGTH_SHORT).show();
                    Log.v(TAG, "Error connecting as client >> " + closeException.toString());
                }
                return;
            }
            //manageConnectedSocketAsClient(socket);
        }

        public void cancel() {
            try {
                socket.close();
            } catch (IOException e) {
                Log.v(TAG, "Error on closing client socket >> " + e.toString());
            }
        }
    }

    // Thread that manages a given connection
    private class ConnectedThread extends Thread {
        private final BluetoothSocket socket;
        private final InputStream inStream;
        private final OutputStream outStream;

        public ConnectedThread(BluetoothSocket socket) {
            this.socket = socket;
            InputStream tempIn = null;
            OutputStream tempOut = null;
            try {
                tempIn = socket.getInputStream();
                tempOut = socket.getOutputStream();
            } catch (IOException e) { }
            inStream = tempIn;
            outStream = tempOut;
        }

        public void run() {
            byte[] buffer = new byte[1024];
            int bytes;
            while (true) {
                try {
                    bytes = inStream.read(buffer);
                    bHandler.obtainMessage(READ, bytes, -1, buffer);
                } catch (IOException e) {
                    break;
                }
            }
        }

        // Call this as a client to send data
        public void write(byte[] bytes) {
            try {
                outStream.write(bytes);
            } catch (IOException e) {
                Log.v(TAG, "Out-stream writing failed >> " + e.toString());
            }
        }

        public void cancel() {
            try {
                socket.close();
            } catch (IOException e) {
                Log.v(TAG, "Socket closing failed >> " + e.toString());
            }
        }
    }

    // Sets up the handler for a thread
    public void setUpHandler() {
        bHandler = new Handler();
    }


    */// Shows the "Create new Vote" dialogue box

    public void startServerThread() {
        new AcceptThread().start();
    }

    public void showCreateDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = getLayoutInflater();
        //discover
        //dialog.cancel
        View view = inflater.inflate(R.layout.dialog, null);
        final TextView create = (TextView) view.findViewById(R.id.createbutton);
        create.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                blopAnimate(create);
                setUpBluetooth();
                btvisible();
                startServerThread();
            }
        });
        format(create);
        TextView cancel = (TextView) view.findViewById(R.id.cancelbutton);
        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.cancel();
            }
        });
        format(cancel);
        format((EditText) view.findViewById(R.id.votename));
        format((EditText) view.findViewById(R.id.password));
        format((EditText) view.findViewById(R.id.votedesc));
        builder.setView(view);
        dialog = builder.create();
        dialog.show();
    }
/*
    // Shows the "Join a vote" layout
    public void Layout() {
        setContentView(R.layout.client_layout);
        format((TextView) (findViewById(R.id.header1)));
        ImageView back = (ImageView)findViewById(R.id.backbutton);
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //showUI();
                new ClientThread().start();
            }
        });
        ListView list = (ListView)findViewById(R.id.deviceList);
        ClientAdapter adapter = new ClientAdapter(this, setToList(pairedDevices));
        list.setAdapter(adapter);
        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Toast.makeText(MainActivity.this, "Clicked!", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // Manages the connected thread
    public void manageConnectedSocket(BluetoothSocket socket) {
        ConnectedThread thread = new ConnectedThread(socket);
        thread.run();
    }*/

    // FOrmats a given textview with the font
    public void format(TextView text) {
        Typeface myTypeface = Typeface.createFromAsset(getAssets(), "fonts/Fonarto.ttf");
        text.setTypeface(myTypeface);
    }

    /*// Shows a list of bluetooth devices within range
    public List<BluetoothDevice> setToList(Set<BluetoothDevice> set) {
        List<BluetoothDevice> b = new ArrayList<BluetoothDevice>();
        for (BluetoothDevice bb :set) {
            b.add(bb);
        }
        return b;
    }

    // Conversts a list to a list???
    public List<String> listToList(List<BluetoothDevice> set) {
        List<String> b = new ArrayList<String>();
        for (BluetoothDevice bb : set) {
            b.add(bb.getAddress());
        }
        return b;
    }

    // Client adapter
    public class ClientAdapter extends ArrayAdapter<BluetoothDevice> {
        public ClientAdapter(Context context, List<BluetoothDevice> users) {
            super(context, 0, users);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            BluetoothDevice server = getItem(position);
            if (convertView == null) {
                convertView = LayoutInflater.from(getContext()).inflate(R.layout.list_item_, parent, false);
            }
            TextView text = (TextView)convertView.findViewById(R.id.name_vote);
            text.setText(server.getName());
            format(text);
            return convertView;
        }
    }*/

    // Shows a stupid animation
    public void blopAnimate(View v) {
        Animation animation = AnimationUtils.loadAnimation(this, R.anim.blop);
        v.startAnimation(animation);
    }

    //public class MyBluetoothService {
        private static final String TAG = "MY_APP_DEBUG_TAG";
        private Handler mHandler; // handler that gets info from Bluetooth service

        public static final int MESSAGE_READ = 0;
        public static final int MESSAGE_WRITE = 1;
        public static final int MESSAGE_TOAST = 2;

        public class ConnectedThread extends Thread {
            private final BluetoothSocket mmSocket;
            private final InputStream mmInStream;
            private final OutputStream mmOutStream;
            private byte[] mmBuffer; // mmBuffer store for the stream

            public ConnectedThread(BluetoothSocket socket) {
                mmSocket = socket;
                InputStream tmpIn = null;
                OutputStream tmpOut = null;

                // Get the input and output streams; using temp objects because
                // member streams are final.
                try {
                    tmpIn = socket.getInputStream();
                } catch (IOException e) {
                    Log.e(TAG, "Error occurred when creating input stream", e);
                }
                try {
                    tmpOut = socket.getOutputStream();
                } catch (IOException e) {
                    Log.e(TAG, "Error occurred when creating output stream", e);
                }

                mmInStream = tmpIn;
                mmOutStream = tmpOut;

                Button bb = (Button)findViewById(R.id.buttontt);
                bb.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        write("YO WASSUP".getBytes());
                    }
                });
            }

            public void run() {
                mmBuffer = new byte[1024];
                int numBytes; // bytes returned from read()

                // Keep listening to the InputStream until an exception occurs.
                while (true) {
                    try {
                        // Read from the InputStream.
                        numBytes = mmInStream.read(mmBuffer);
                        // Send the obtained bytes to the UI activity.
                        Message readMsg = mHandler.obtainMessage(
                                MESSAGE_READ, numBytes, -1,
                                mmBuffer);
                        readMsg.sendToTarget();
                    } catch (IOException e) {
                        Log.d(TAG, "Input stream was disconnected", e);
                        break;
                    }
                }
            }

            // Call this from the main activity to send data to the remote device.
            public void write(byte[] bytes) {
                try {
                    mmOutStream.write(bytes);

                    // Share the sent message with the UI activity.
                    Message writtenMsg = mHandler.obtainMessage(
                            MESSAGE_WRITE, -1, -1, mmBuffer);
                    writtenMsg.sendToTarget();
                } catch (IOException e) {
                    Log.e(TAG, "Error occurred when sending data", e);

                    // Send a failure message back to the activity.
                    Message writeErrorMsg =
                            mHandler.obtainMessage(MESSAGE_TOAST);
                    Bundle bundle = new Bundle();
                    bundle.putString("toast",
                            "Couldn't send data to the other device");
                    writeErrorMsg.setData(bundle);
                    mHandler.sendMessage(writeErrorMsg);
                }
            }

            // Call this method from the main activity to shut down the connection.
            public void cancel() {
                try {
                    mmSocket.close();
                } catch (IOException e) {
                    Log.e(TAG, "Could not close the connect socket", e);
                }
            }
        }
    //}
}
