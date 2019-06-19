package com.bruinmon;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class BattleActivity extends AppCompatActivity {

    final int MAX_HP = 10;

    private Handler handler = new Handler();

    private final BattleActivity activity = this;
    private BluetoothServerSocket mmServerSocket;
    private BluetoothSocket mmSocket;
    private int mmSequence = 0;
    private AtomicInteger mmSequenceACK = new AtomicInteger(0);
    private int mmPeerSequence = 0;
    private OutputStream mmOutStream;

    /** States for the game to move through **/
    private enum GameState {
        WAITING_ON_OPPONENT,
        PLAYER_MOVE_CHOOSE,
        OPPONENT_MOVE_CHOOSE,
        PLAYER_MOVE_SHOW,
        OPPONENT_MOVE_SHOW,
        RESOLVE_COMBAT,
        PLAYER_WON,
        OPPONENT_WON,
        TIED
    }

    /** Types of messages sent over the network **/
    private enum NetMsgType {
        MESSAGE_ACK,
        CHOOSE_BRUINMON,
        CHOOSE_MOVE
    }

    /** Game state variables **/
    volatile GameState game_state;
    boolean is_ai_battle;
    Bruinmon player_bruinmon;
    int player_hp;
    Move player_move;
    int player_move_num;
    Bruinmon opponent_bruinmon;
    int opponent_hp;
    Move opponent_move;

    /** Updates the opponent's bruinmon in response to a message sent over Bluetooth **/
    private class NetUpdateBruinmon implements Runnable {
        int sequence;
        Bruinmon bruinmon;

        NetUpdateBruinmon(int sequence, Bruinmon bruinmon) {
            this.sequence = sequence;
            this.bruinmon = bruinmon;
        }

        public void run() {
            // Update the opponent's Bruinmon, but only if we're expecting it
            if (sequence > mmPeerSequence) {
                if (game_state != GameState.WAITING_ON_OPPONENT) {
                    return;
                }
                mmPeerSequence = sequence;
                opponent_bruinmon = bruinmon;
                ((TextView)activity.findViewById(R.id.opponent_bruin_name)).setText(opponent_bruinmon.getName());
                ((ImageView)activity.findViewById(R.id.opponent_bruin_image)).setImageResource(opponent_bruinmon.getImage());
            }

            // Let the sender know we processed this message
            try {
                mmOutStream.write((sequence + "," + NetMsgType.MESSAGE_ACK.name()).getBytes());
            } catch (IOException e) {}
        }
    };

    /** Updates the opponent's move in response to a message sent over Bluetooth **/
    private class NetUseMove implements Runnable {
        int sequence;
        int move;

        NetUseMove(int sequence, int move) {
            this.sequence = sequence;
            this.move = move;
        }

        public void run() {
            // Process the opponent's move choice, but only if we're expecting it
            if (sequence > mmPeerSequence) {
                if (game_state != GameState.OPPONENT_MOVE_CHOOSE) {
                    return;
                }
                mmPeerSequence = sequence;
                switch (move) {
                    case 1:
                        opponent_move = opponent_bruinmon.getMove1();
                        break;
                    case 2:
                        opponent_move = opponent_bruinmon.getMove2();
                        break;
                    case 3:
                        opponent_move = opponent_bruinmon.getMove3();
                        break;
                    case 4:
                        opponent_move = opponent_bruinmon.getMove4();
                        break;
                }
            }

            // Let the sender know we processed this message
            try {
                mmOutStream.write((sequence + "," + NetMsgType.MESSAGE_ACK.name()).getBytes());
            } catch (IOException e) {}
        }
    };

    /** Accept thread ran by host when awaiting challenge **/
    private volatile boolean isAcceptThreadRunning = false;
    private class AcceptThread extends Thread {
        public void run() {
            int counter = 0;
            while (isAcceptThreadRunning) {
                counter = counter + 1;
                if (counter >= 40) {
                    counter = 0;
                    activity.runOnUiThread(new Runnable() {
                        public void run() {
                            Toast.makeText(activity, "Waiting for challenger", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
                try {
                    mmSocket = mmServerSocket.accept(100);
                } catch (IOException e) {
                    mmSocket = null;
                }
                if (mmSocket != null) {
                    // A connection was accepted, start network thread
                    isNetworkThreadRunning = true;
                    networkThread.start();
                    try {
                        mmServerSocket.close();
                    } catch (IOException e) {}
                    break;
                }
            }
            isAcceptThreadRunning = false;
        }
    }
    final Thread acceptThread = new Thread(new AcceptThread());

    /** Network thread ran by both sides in order to receive updates from peer **/
    private volatile boolean isNetworkThreadRunning = false;
    private class NetworkThread extends Thread {
        public void run() {
            // Finish connecting if we need to
            if (!mmSocket.isConnected()) {
                try {
                    mmSocket.connect();
                } catch (IOException e) {
                    activity.runOnUiThread(new Runnable() {
                        public void run() {
                            Toast.makeText(activity, "Failed to connect", Toast.LENGTH_LONG).show();
                        }
                    });
                    isNetworkThreadRunning = false;
                    return;
                }
            }

            // Get the input scanner so we can read messages from the peer
            InputStream mmInStream;
            try {
                mmInStream = mmSocket.getInputStream();
            } catch (IOException e) {
                activity.runOnUiThread(new Runnable() {
                    public void run() {
                        Toast.makeText(activity, "Failed to create input stream", Toast.LENGTH_LONG).show();
                    }
                });
                isNetworkThreadRunning = false;
                return;
            }

            // Get the output writer so we can write messages to the peer
            try {
                mmOutStream = mmSocket.getOutputStream();
            } catch (IOException e) {
                activity.runOnUiThread(new Runnable() {
                    public void run() {
                        Toast.makeText(activity, "Failed to create output stream", Toast.LENGTH_LONG).show();
                    }
                });
                isNetworkThreadRunning = false;
                return;
            }

            // Continually process messages in this thread now
            byte[] buffer = new byte[1024];
            int numBytes = 0; // bytes returned from read()
            Pattern r = Pattern.compile("^(\\d+),([^,]+),?(\\d*)");
            while (isNetworkThreadRunning) {
                // Get any message sent to us
                try {
                    numBytes = mmInStream.read(buffer);
                } catch (IOException e) {
                    final IOException e2 = e;
                    activity.runOnUiThread(new Runnable() {
                        public void run() {
                            Toast.makeText(activity, "Disconnected", Toast.LENGTH_LONG).show();
                        }
                    });
                    isNetworkThreadRunning = false;
                    return;
                }

                // Make sure it matches the expected format
                String message = new String(buffer, 0, numBytes);
                Matcher m = r.matcher(message);
                if (!m.find()) {
                    continue;
                }

                // Parse and handle it
                int sequence = Integer.parseInt(m.group(1));
                NetMsgType messageType = NetMsgType.valueOf(m.group(2));
                Runnable action = null;
                switch (messageType) {
                    case MESSAGE_ACK: {
                        if (sequence > mmSequenceACK.get()) {
                            mmSequenceACK.set(sequence);
                        }
                        continue;
                    }
                    case CHOOSE_BRUINMON: {
                        action = new NetUpdateBruinmon(sequence, Bruinmon.getAll().get(Integer.parseInt(m.group(3))));
                        break;
                    }
                    case CHOOSE_MOVE: {
                        action = new NetUseMove(sequence, Integer.parseInt(m.group(3)));
                        break;
                    }
                }
                handler.post(action);
            }
        }
    }
    final Thread networkThread = new Thread(new NetworkThread());

    /** Game state update function ran periodically **/
    private Runnable gameUpdate = new Runnable() {
        @Override
        public void run() {
        switch (game_state) {
            case WAITING_ON_OPPONENT: {
                if (mmOutStream != null && mmSequence > mmSequenceACK.get()) {
                    try {
                        mmOutStream.write((mmSequence + "," + NetMsgType.CHOOSE_BRUINMON.name() + "," + player_bruinmon.getId()).getBytes());
                    } catch (IOException e) {}
                    handler.postDelayed(gameUpdate, 100);
                    break;
                }
                if (opponent_bruinmon != null) {
                    game_state = GameState.PLAYER_MOVE_CHOOSE;
                    handler.post(gameUpdate);
                } else {
                    handler.postDelayed(gameUpdate, 100);
                }
                break;
            }
            case PLAYER_MOVE_CHOOSE: {
                mmSequence = mmSequence + 1;
                showMoveButtons();
                break;
            }
            case OPPONENT_MOVE_CHOOSE: {
                if (is_ai_battle) {
                    Random rand = new Random();
                    int moveProbability = rand.nextInt(8);
                    if (moveProbability < 3) {
                        // AI has weight 3 for using move 1
                        opponent_move = opponent_bruinmon.getMove1();
                    } else if (moveProbability < 5) {
                        // AI has weight 2 for using move 2
                        opponent_move = opponent_bruinmon.getMove2();
                    } else if (moveProbability < 7) {
                        // AI has weight 2 for using move 3
                        opponent_move = opponent_bruinmon.getMove3();
                    } else {
                        // AI has weight 1 for using move 4
                        opponent_move = opponent_bruinmon.getMove4();
                    }
                    game_state = GameState.PLAYER_MOVE_SHOW;
                    handler.post(gameUpdate);
                } else {
                    ((TextView)findViewById(R.id.battle_description)).setText("Waiting for opponent to make a move...");
                    if (mmOutStream != null && mmSequence > mmSequenceACK.get()) {
                        try {
                            mmOutStream.write((mmSequence + "," + NetMsgType.CHOOSE_MOVE.name() + "," + player_move_num).getBytes());
                        } catch (IOException e) {}
                        handler.postDelayed(gameUpdate, 100);
                        break;
                    }
                    if (opponent_move != null) {
                        game_state = GameState.PLAYER_MOVE_SHOW;
                        handler.post(gameUpdate);
                    } else {
                        handler.postDelayed(gameUpdate, 100);
                    }
                }
                break;
            }
            case PLAYER_MOVE_SHOW: {
                ((TextView)findViewById(R.id.battle_description)).setText("Your " + player_bruinmon.getName() + " used " + player_move.getName());
                game_state = GameState.OPPONENT_MOVE_SHOW;
                handler.postDelayed(gameUpdate, 2500);
                break;
            }
            case OPPONENT_MOVE_SHOW: {
                ((TextView)findViewById(R.id.battle_description)).setText("Opponent's " + opponent_bruinmon.getName() + " used " + opponent_move.getName());
                game_state = GameState.RESOLVE_COMBAT;
                handler.postDelayed(gameUpdate, 2500);
                break;
            }
            case RESOLVE_COMBAT: {
                String resolutionText = "";

                // Resolve how much damage the player is doing to the opponent
                int player_damage = 2;
                if (player_move.getType() == player_bruinmon.getType()) {
                    player_damage = player_damage + 1;
                }
                if (player_move.getType() == Bruinmon.Type.ROCK && opponent_move.getType() == Bruinmon.Type.PAPER) {
                    resolutionText = resolutionText + "You got countered! ";
                    player_damage = 0;
                } else if (player_move.getType() == Bruinmon.Type.PAPER && opponent_move.getType() == Bruinmon.Type.SCISSORS) {
                    resolutionText = resolutionText + "You got countered! ";
                    player_damage = 0;
                } else if (player_move.getType() == Bruinmon.Type.SCISSORS && opponent_move.getType() == Bruinmon.Type.ROCK) {
                    resolutionText = resolutionText + "You got countered! ";
                    player_damage = 0;
                } else if (player_move.getType() == Bruinmon.Type.NONE) {
                    player_damage = player_damage - 1;
                }

                // Resolve how much damage the opponent is doing to the player
                int opponent_damage = 2;
                if (opponent_move.getType() == opponent_bruinmon.getType()) {
                    opponent_damage = opponent_damage + 1;
                }
                if (opponent_move.getType() == Bruinmon.Type.ROCK && player_move.getType() == Bruinmon.Type.PAPER) {
                    resolutionText = resolutionText + "Opponent got countered! ";
                    opponent_damage = 0;
                } else if (opponent_move.getType() == Bruinmon.Type.PAPER && player_move.getType() == Bruinmon.Type.SCISSORS) {
                    resolutionText = resolutionText + "Opponent got countered! ";
                    opponent_damage = 0;
                } else if (opponent_move.getType() == Bruinmon.Type.SCISSORS && player_move.getType() == Bruinmon.Type.ROCK) {
                    resolutionText = resolutionText + "Opponent got countered! ";
                    opponent_damage = 0;
                } else if (opponent_move.getType() == Bruinmon.Type.NONE) {
                    opponent_damage = opponent_damage - 1;
                }

                // Null out the player_move and opponent_move so we don't re-use on accident
                player_move = null;
                player_move_num = 0;
                opponent_move = null;

                // Do the actual damage to the player and opponent
                player_hp = player_hp - opponent_damage;
                opponent_hp = opponent_hp - player_damage;
                resolutionText = resolutionText + "Opponent does " + opponent_damage + " damage, while you do " + player_damage + " damage";
                ((TextView)findViewById(R.id.battle_description)).setText(resolutionText);
                updateVisuals();

                // Move to the correct next game state
                if (player_hp < 1 && opponent_hp < 1) {
                    game_state = GameState.TIED;
                } else if (player_hp < 1) {
                    game_state = GameState.OPPONENT_WON;
                } else if (opponent_hp < 1) {
                    game_state = GameState.PLAYER_WON;
                } else {
                    game_state = GameState.PLAYER_MOVE_CHOOSE;
                }
                handler.postDelayed(gameUpdate, 5000);
                break;
            }
            case PLAYER_WON:
                ((TextView)findViewById(R.id.battle_description)).setText("You won the battle!");
                break;
            case OPPONENT_WON:
                ((TextView)findViewById(R.id.battle_description)).setText("You lost the battle!");
                break;
            case TIED:
                ((TextView)findViewById(R.id.battle_description)).setText("You tied the battle!");
                break;
        }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_battle);

        Intent intent = getIntent();

        // Reset the game state
        game_state = GameState.PLAYER_MOVE_CHOOSE;
        is_ai_battle = intent.getBooleanExtra("is_ai_battle", false);
        player_bruinmon = (Bruinmon)intent.getSerializableExtra("player_bruinmon");
        player_hp = MAX_HP;
        ((TextView)findViewById(R.id.player_bruin_name)).setText(player_bruinmon.getName());
        ((ImageView)findViewById(R.id.player_bruin_image)).setImageResource(player_bruinmon.getImage());
        ((ProgressBar)findViewById(R.id.player_bruin_hp_bar)).setMax(MAX_HP);
        opponent_hp = MAX_HP;
        ((ProgressBar)findViewById(R.id.opponent_bruin_hp_bar)).setMax(MAX_HP);

        // If we're fighting AI, we know what Bruinmon we're fighting right away, but otherwise we must wait
        if (is_ai_battle) {
            opponent_bruinmon = Bruinmon.getAll().get(0);
            ((TextView)findViewById(R.id.opponent_bruin_name)).setText(opponent_bruinmon.getName());
            ((ImageView)findViewById(R.id.opponent_bruin_image)).setImageResource(opponent_bruinmon.getImage());
        } else {
            opponent_bruinmon = null;
            ((TextView)findViewById(R.id.opponent_bruin_name)).setText("???");
            ((ImageView)findViewById(R.id.opponent_bruin_image)).setImageResource(R.mipmap.question_mark);
            ((TextView)findViewById(R.id.battle_description)).setText("Waiting for challenger to load...");
            findViewById(R.id.button_move1).setVisibility(View.GONE);
            findViewById(R.id.button_move2).setVisibility(View.GONE);
            findViewById(R.id.button_move3).setVisibility(View.GONE);
            findViewById(R.id.button_move4).setVisibility(View.GONE);
            game_state = GameState.WAITING_ON_OPPONENT;
            mmSequence = mmSequence + 1;
            handler.postDelayed(gameUpdate, 100);
        }

        // Update progress bars and HP text
        updateVisuals();

        // Make the UI buttons have the move names
        ((Button)findViewById(R.id.button_move1)).setText(player_bruinmon.getMove1().getName());
        ((Button)findViewById(R.id.button_move2)).setText(player_bruinmon.getMove2().getName());
        ((Button)findViewById(R.id.button_move3)).setText(player_bruinmon.getMove3().getName());
        ((Button)findViewById(R.id.button_move4)).setText(player_bruinmon.getMove4().getName());

        // If we're hosting, we need to wait for a challenger
        if (intent.getBooleanExtra("is_hosting", false)) {
            // MY_UUID is the app's UUID string, also used by the client code
            try {
                mmServerSocket = BluetoothAdapter.getDefaultAdapter().listenUsingRfcommWithServiceRecord("Bruinmon", UUID.fromString("12c5a344-d9af-11e7-9296-cec278b6b50a"));
            } catch (IOException e) {
                Toast.makeText(this, "Failed to listen", Toast.LENGTH_LONG).show();
            }

            // Keep listening until a challenger appears
            isAcceptThreadRunning = true;
            acceptThread.start();
        } else if (!is_ai_battle) {
            BluetoothDevice device = getIntent().getParcelableExtra("opponent_device");

            // Get a BluetoothSocket to connect with the given BluetoothDevice
            try {
                mmSocket = device.createRfcommSocketToServiceRecord(UUID.fromString("12c5a344-d9af-11e7-9296-cec278b6b50a"));
            } catch (IOException e) {
                Toast.makeText(this, "Failed to create socket", Toast.LENGTH_LONG).show();
            }

            // Immediately start the network thread now that we have a socket
            isNetworkThreadRunning = true;
            networkThread.start();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        // Close out any sockets first (before shutting threads down)
        if (mmServerSocket != null) {
            try {
                mmServerSocket.close();
            } catch (IOException e) {}
        }
        if (mmSocket != null) {
            try {
                mmSocket.close();
            } catch (IOException e) {}
        }

        // Kill the background threads (in this order)
        try {
            isAcceptThreadRunning = false;
            acceptThread.join();
            isNetworkThreadRunning = false;
            networkThread.join();
        } catch (InterruptedException e) {}

        // Reset state for next time
        mmServerSocket = null;
        mmSocket = null;
        mmSequence = 0;
        mmSequenceACK.set(0);
        mmPeerSequence = 0;
        mmOutStream = null;
    }

    /** Hides the move buttons **/
    private void hideMoveButtons() {
        game_state = GameState.OPPONENT_MOVE_CHOOSE;
        handler.post(gameUpdate);
        findViewById(R.id.button_move1).setVisibility(View.GONE);
        findViewById(R.id.button_move2).setVisibility(View.GONE);
        findViewById(R.id.button_move3).setVisibility(View.GONE);
        findViewById(R.id.button_move4).setVisibility(View.GONE);
    }

    /** Shows the move buttons **/
    private void showMoveButtons() {
        ((TextView)findViewById(R.id.battle_description)).setText("Choose which move to use");
        findViewById(R.id.button_move1).setVisibility(View.VISIBLE);
        findViewById(R.id.button_move2).setVisibility(View.VISIBLE);
        findViewById(R.id.button_move3).setVisibility(View.VISIBLE);
        findViewById(R.id.button_move4).setVisibility(View.VISIBLE);
    }

    /** Updates the visuals on the UI such as the HP bar **/
    private void updateVisuals() {
        ((TextView)findViewById(R.id.player_bruin_hp_text)).setText(player_hp + "/" + MAX_HP + " HP");
        ((ProgressBar)findViewById(R.id.player_bruin_hp_bar)).setProgress(player_hp);
        ((TextView)findViewById(R.id.opponent_bruin_hp_text)).setText(opponent_hp + "/" + MAX_HP + " HP");
        ((ProgressBar)findViewById(R.id.opponent_bruin_hp_bar)).setProgress(opponent_hp);
    }

    /** Called when the user touches the first use move button **/
    public void useMove1(View view) {
        player_move = player_bruinmon.getMove1();
        player_move_num = 1;
        hideMoveButtons();
    }

    /** Called when the user touches the second use move button **/
    public void useMove2(View view) {
        player_move = player_bruinmon.getMove2();
        player_move_num = 2;
        hideMoveButtons();
    }

    /** Called when the user touches the third use move button **/
    public void useMove3(View view) {
        player_move = player_bruinmon.getMove3();
        player_move_num = 3;
        hideMoveButtons();
    }

    /** Called when the user touches the fourth use move button **/
    public void useMove4(View view) {
        player_move = player_bruinmon.getMove4();
        player_move_num = 4;
        hideMoveButtons();
    }
}
