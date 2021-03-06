
package com.jahanwalsh.meerkat;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Typeface;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.app.Activity;
import android.content.Intent;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.animation.Transformation;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;
import com.spotify.sdk.android.authentication.AuthenticationClient;
import com.spotify.sdk.android.authentication.AuthenticationRequest;
import com.spotify.sdk.android.authentication.AuthenticationResponse;
import com.spotify.sdk.android.player.Config;
import com.spotify.sdk.android.player.ConnectionStateCallback;
import com.spotify.sdk.android.player.Connectivity;
import com.spotify.sdk.android.player.Error;
import com.spotify.sdk.android.player.Metadata;
import com.spotify.sdk.android.player.PlaybackBitrate;
import com.spotify.sdk.android.player.PlaybackState;
import com.spotify.sdk.android.player.Player;
import com.spotify.sdk.android.player.PlayerEvent;
import com.spotify.sdk.android.player.Spotify;
import com.spotify.sdk.android.player.SpotifyPlayer;
import com.squareup.picasso.Picasso;

public class MainActivity extends Activity implements SpotifyPlayer.NotificationCallback, ConnectionStateCallback {

    private static final String CLIENT_ID = "939c554123ba49ee9b14bb76bc59012a";
    private static final String REDIRECT_URI = "http://meerkatmusic.com/callback";
    private static final String TEST_SONG_URI = "spotify:track:2PpruBYCo4H7WOBJ7Q2EwM";
    private static final String TEST_PLAYLIST_URI = "spotify:user:1216740004:playlist:6wzgIxl33B2pHsKNqakxP9";
    private static final String TEST_ALBUM_URI = "spotify:album:12DmuRtZNTx84ELHKD3VGL";
    private static final String TEST_QUEUE_SONG_URI = "spotify:track:5EEOjaJyWvfMglmEwf9bG3";
    private static final String TEST_PROFILE_URI = "spotify:user:1216740004";



    //CONSTANTS

    private Player mPlayer;
    private PlaybackState mCurrentPlaybackState;
    private BroadcastReceiver mNetworkStateReceiver;
    private TextView mStatusText;

    private TextView mMetadataText;

    private EditText mSeekEditText;
    private ScrollView mStatusTextScrollView;
    private Metadata mMetadata;
    private static final int REQUEST_CODE = 1991;
    private Button mPlayButton;
    private Button mAlbumButton;
    private Button mPlaylistButton;
    private Button mPauseButton;
    private Button mSeekButton;
    private Button mSkipNextButton;
    private Button mSkipPrevButton;
    private Button mQueueButton;
    private Button mShuffleButton;
    private Button mRepeatButton;
    private Button mProfileButton;


    private static final int[] REQUIRES_INITIALIZED_STATE = {
            R.id.play_track_button,
            R.id.play_album_button,
            R.id.play_playlist_button,
            R.id.pause_button,
            R.id.seek_button,
            R.id.seek_edittext,
            R.id.toggle_profile,
    };

    private static final int[] REQUIRES_PLAYING_STATE = {
            R.id.skip_next_button,
            R.id.skip_prev_button,
            R.id.queue_song_button,
            R.id.toggle_shuffle_button,
            R.id.toggle_repeat_button,

    };
    public static final String TAG = "SpotifySdk";

    private final Player.OperationCallback mOperationCallback = new Player.OperationCallback() {
        @Override
        public void onSuccess(){
        logStatus("OK!");
        }

        @Override
        public void onError(Error error) {
            logStatus("ERROR:" + error);
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        AuthenticationRequest.Builder builder = new AuthenticationRequest.Builder(CLIENT_ID,
                AuthenticationResponse.Type.TOKEN,
                REDIRECT_URI);
        builder.setScopes(new String[]{"user-read-private", "streaming"});
        AuthenticationRequest request = builder.build();

        AuthenticationClient.openLoginActivity(this, REQUEST_CODE, request);

        mStatusText = (TextView) findViewById(R.id.status_text);
        mMetadataText = (TextView) findViewById(R.id.metadata);
        mSeekEditText = (EditText) findViewById(R.id.seek_edittext);
        mStatusTextScrollView = (ScrollView) findViewById(R.id.status_text_container);

        mPlayButton = (Button) findViewById( R.id.play_track_button);
        mAlbumButton = (Button) findViewById( R.id.play_album_button);
        mPlaylistButton = (Button) findViewById( R.id.play_playlist_button);
        mPauseButton = (Button) findViewById(R.id.pause_button);
        mSeekButton = (Button) findViewById( R.id.seek_button);
        mSkipNextButton = (Button) findViewById(  R.id.skip_next_button);
        mSkipPrevButton = (Button) findViewById( R.id.skip_prev_button);
        mQueueButton = (Button) findViewById( R.id.queue_song_button);
        mShuffleButton = (Button) findViewById( R.id.toggle_shuffle_button);
        mRepeatButton = (Button) findViewById( R.id.toggle_repeat_button);
        mProfileButton = (Button) findViewById(R.id.toggle_profile);


//FONTS
        Typeface bread = Typeface.createFromAsset(getAssets(), "fonts/bread.ttf");
        mStatusText.setTypeface(bread);
        mMetadataText.setTypeface(bread);
        mSeekEditText.setTypeface(bread);
        mPlayButton.setTypeface(bread);
        mAlbumButton.setTypeface(bread);
        mPlaylistButton.setTypeface(bread);
        mPauseButton.setTypeface(bread);
        mSeekButton.setTypeface(bread);
        mSkipNextButton.setTypeface(bread);
        mSkipPrevButton.setTypeface(bread);
        mQueueButton.setTypeface(bread);
        mShuffleButton.setTypeface(bread);
        mRepeatButton.setTypeface(bread);
        mProfileButton.setTypeface(bread);



        updateView();
        logStatus("Ready");
    }

    @Override
    protected void onResume() {
        super.onResume();


        mNetworkStateReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (mPlayer != null) {
                    Connectivity connectivity = getNetworkConnectivity(getBaseContext());
                    logStatus("Network state changed: " + connectivity.toString());
                    mPlayer.setConnectivityStatus(mOperationCallback, connectivity);
                }
            }
        };

        IntentFilter filter = new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);
        registerReceiver(mNetworkStateReceiver, filter);

        if (mPlayer != null) {
            mPlayer.addNotificationCallback(MainActivity.this);
            mPlayer.addConnectionStateCallback(MainActivity.this);
        }
    }

    private Connectivity getNetworkConnectivity(Context context) {
        ConnectivityManager connectivityManager;
        connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = connectivityManager.getActiveNetworkInfo();
        if (activeNetwork != null && activeNetwork.isConnected()) {
            return Connectivity.fromNetworkType(activeNetwork.getType());
        } else {
            return Connectivity.OFFLINE;
        }
    }

    private void openLoginWindow() {
        final AuthenticationRequest request = new AuthenticationRequest.Builder(CLIENT_ID, AuthenticationResponse.Type.TOKEN, REDIRECT_URI)
                .setScopes(new String[]{"user-read-private", "playlist-read", "playlist-read-private", "streaming"})
                .build();

        AuthenticationClient.openLoginActivity(this, REQUEST_CODE, request);
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);

        if (requestCode == REQUEST_CODE) {
            AuthenticationResponse response = AuthenticationClient.getResponse(resultCode, intent);
            if (response.getType() == AuthenticationResponse.Type.TOKEN) {
                Config playerConfig = new Config(this, response.getAccessToken(), CLIENT_ID);
                Spotify.getPlayer(playerConfig, this, new SpotifyPlayer.InitializationObserver() {
                    @Override
                    public void onInitialized(SpotifyPlayer spotifyPlayer) {
                        mPlayer = spotifyPlayer;
                        mPlayer.addConnectionStateCallback(MainActivity.this);
                        mPlayer.addNotificationCallback(MainActivity.this);
                    }

                    @Override
                    public void onError(Throwable throwable) {
                        Log.e("MainActivity", "Could not initialize player: " + throwable.getMessage());
                    }
                });
            }
        }
    }

    private void onAuthenticationComplete(AuthenticationResponse authResponse) {
        // Once we have obtained an authorization token, we can proceed with creating a Player.
        Log.d("Got authtoken", "Got");
        if (mPlayer == null) {
            Config playerConfig = new Config(getApplicationContext(), authResponse.getAccessToken(), CLIENT_ID);

            mPlayer = Spotify.getPlayer(playerConfig, this, new SpotifyPlayer.InitializationObserver() {
                @Override
                public void onInitialized(SpotifyPlayer player) {
                    Log.d("-- Player initialized ", "playerStart");
                    mPlayer.setConnectivityStatus(mOperationCallback, getNetworkConnectivity(MainActivity.this));
                    mPlayer.addNotificationCallback(MainActivity.this);
                    mPlayer.addConnectionStateCallback(MainActivity.this);
                    // Trigger UI refresh
                    updateView();
                }

                @Override
                public void onError(Throwable error) {
                    Log.d("Error in initialization", error.getMessage());
                }
            });
        } else {
            mPlayer.login(authResponse.getAccessToken());
        }
    }


    private void updateView() {
        boolean loggedIn = isLoggedIn();

        Button loginButton = (Button) findViewById(R.id.login_button);
        loginButton.setText(loggedIn ? R.string.logout_button_label : R.string.login_button_label);

        for (int id : REQUIRES_INITIALIZED_STATE) {
            findViewById(id).setEnabled(loggedIn);
        }

        boolean playing = loggedIn && mCurrentPlaybackState != null && mCurrentPlaybackState.isPlaying;
        for (int id : REQUIRES_PLAYING_STATE) {
            findViewById(id).setEnabled(playing);
        }

        if (mMetadata != null) {
            findViewById(R.id.skip_next_button).setEnabled(mMetadata.nextTrack != null);
            findViewById(R.id.skip_prev_button).setEnabled(mMetadata.prevTrack != null);
            findViewById(R.id.pause_button).setEnabled(mMetadata.currentTrack != null);
        }

        final ImageView coverArtView = (ImageView) findViewById(R.id.cover_art);
        if (mMetadata != null && mMetadata.currentTrack != null) {
            mStatusText.setText(mMetadata.contextName + "\n" + "\n" + "Track: " + mMetadata.currentTrack.name + " \n "  + "\n" + "Artist: " + mMetadata.currentTrack.artistName);

            Picasso.with(this)
                    .load(mMetadata.currentTrack.albumCoverWebUrl)
                    .transform(new MyTransform() {
                    })
                    .into(coverArtView);
        } else {

            mMetadataText.setText("");
            mStatusText.setText("<nothing is playing>");
            coverArtView.setBackground(null);
        }

    }

    private boolean isLoggedIn() {

        return mPlayer != null;
    }

    public void onLoginButtonClicked(View view) {
        if (!isLoggedIn()) {
            logStatus("Logging in");
            openLoginWindow();
        } else {
            mPlayer.logout();
        }
    }

    public void onPlayButtonClicked(View view) {

        String uri;
        switch (view.getId()) {
            case R.id.play_track_button:
                uri = TEST_SONG_URI;
                break;
            case R.id.play_playlist_button:
                uri = TEST_PLAYLIST_URI;
                break;
            case R.id.play_album_button:
                uri = TEST_ALBUM_URI;
                break;
            case R.id.toggle_profile:
                uri = TEST_PROFILE_URI;
                break;
            default:
                throw new IllegalArgumentException("View ID does not have an associated URI to play");
        }

        logStatus("Starting playback for " + uri);
        mPlayer.playUri(mOperationCallback, uri, 0, 0);
    }

    public void onPauseButtonClicked(View view) {
        if (mCurrentPlaybackState != null && mCurrentPlaybackState.isPlaying) {
            mPlayer.pause(mOperationCallback);
        } else {
            mPlayer.resume(mOperationCallback);
        }
    }

    public void onSkipToPreviousButtonClicked(View view) {
        mPlayer.skipToPrevious(mOperationCallback);
    }

    public void onSkipToNextButtonClicked(View view) {
        mPlayer.skipToNext(mOperationCallback);
    }

    public void onQueueSongButtonClicked(View view) {
        mPlayer.queue(mOperationCallback, TEST_QUEUE_SONG_URI);
        Toast toast = Toast.makeText(this, R.string.song_queued_toast, Toast.LENGTH_SHORT);
        toast.show();
    }

    public void onToggleProfileButtonClicked(View view){
        mPlayer.playUri(mOperationCallback,"spotify:user:1216740004",0,0 );

    }

    public void onToggleShuffleButtonClicked(View view) {
        mPlayer.setShuffle(mOperationCallback, !mCurrentPlaybackState.isShuffling);
    }

    public void onToggleRepeatButtonClicked(View view) {
        mPlayer.setRepeat(mOperationCallback, !mCurrentPlaybackState.isRepeating);
    }

    public void onSeekButtonClicked(View view) {
        final Integer seek = Integer.valueOf(mSeekEditText.getText().toString());
        mPlayer.seekToPosition(mOperationCallback, seek);
    }



//CALLBACK METHODS






    @Override
    public void onLoggedIn() {
        logStatus("Login complete");

        mPlayer.playUri(null, "spotify:track:1ETZhP9orTkDclKEyt0xqm", 0, 0);
    }

    @Override
    public void onLoggedOut() {

        logStatus("Logout complete");
    }

    @Override
    public void onLoginFailed(Error error) {

        logStatus("Login error "+ error);
    }

    @Override
    public void onTemporaryError() {

        logStatus("Temporary error occurred");
    }

    @Override
    public void onConnectionMessage(String message) {
        logStatus("Incoming connection message: " + message);
    }


    private void logStatus(String status) {
        if (!TextUtils.isEmpty(mStatusText.getText())) {
            mStatusText.append("\n");
        }
//        mStatusText.append(">>>" + status);
        mStatusTextScrollView.post(new Runnable() {
            @Override
            public void run() {
                // Scroll to the bottom
                mStatusTextScrollView.fullScroll(View.FOCUS_DOWN);
            }
        });
    }

    //destruction

    @Override
    protected void onDestroy() {
        Spotify.destroyPlayer(this);
        super.onDestroy();
    }

    @Override
    public void onPlaybackEvent(PlayerEvent playerEvent) {
        mCurrentPlaybackState = mPlayer.getPlaybackState();
        mMetadata = mPlayer.getMetadata();
        logStatus("Event: " + playerEvent);

        Log.i(TAG, "Player state: " + mCurrentPlaybackState);
        Log.i(TAG, "Metadata: " + mMetadata);
        updateView();
    }

    @Override
    public void onPlaybackError(Error error) {
        logStatus("Playback error received: " + error.name());
        switch (error) {
            default:
                break;
        }
    }
}