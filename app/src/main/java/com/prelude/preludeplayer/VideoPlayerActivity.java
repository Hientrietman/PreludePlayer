package com.prelude.preludeplayer;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.app.PictureInPictureParams;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.media.MediaMetadataRetriever;
import android.media.audiofx.AudioEffect;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.util.Rational;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.developer.filepicker.controller.DialogSelectionListener;
import com.developer.filepicker.model.DialogConfigs;
import com.developer.filepicker.model.DialogProperties;
import com.developer.filepicker.view.FilePickerDialog;
import com.google.android.exoplayer2.C;
import com.google.android.exoplayer2.ExoPlaybackException;
import com.google.android.exoplayer2.Format;
import com.google.android.exoplayer2.PlaybackParameters;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.source.ConcatenatingMediaSource;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.MergingMediaSource;
import com.google.android.exoplayer2.source.ProgressiveMediaSource;
import com.google.android.exoplayer2.source.SingleSampleMediaSource;
import com.google.android.exoplayer2.ui.AspectRatioFrameLayout;
import com.google.android.exoplayer2.ui.PlayerView;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.android.exoplayer2.util.MimeTypes;
import com.google.android.exoplayer2.util.Util;

import java.io.File;
import java.util.ArrayList;

public class  VideoPlayerActivity extends AppCompatActivity implements View.OnClickListener {

    ArrayList<MediaFiles> mVideoFiles = new ArrayList<>();
    PlayerView playerView;
    SimpleExoPlayer player;
    int position;
    String videoTitle;
    TextView title;
    private ControlsMode controlsMode;

    public enum ControlsMode {
        LOCK, FULLSCREEN;
    }

    ImageView videoBack, lock, unlock, scaling,videoList;
    VideoFilesAdapter videoFilesAdapter;
    RelativeLayout root;
    ConcatenatingMediaSource concatenatingMediaSource;
    ImageView nextButton, previousButton;
    //horizontal recyclerview variables
    ArrayList<IconModel> iconModelsArrayList = new ArrayList<>();
    PlaybackIconAdapter playbackIconAdapter;
    RecyclerView recyclerViewIcons;
    boolean expand = false;
    View nightMode;
    boolean dark = false;
    boolean mute = false;
    PlaybackParameters parameters;
    float speed;
    DialogProperties dialogProperties;
    FilePickerDialog filePickerDialog;
    Uri uriSubtile;
    PictureInPictureParams.Builder pictureInPicture;
    boolean isCrossChecked;


    //horizontal recyclerview variables
    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setFullScreen();
        setContentView(R.layout.activity_video_player);
        getSupportActionBar().hide();
        playerView = findViewById(R.id.exoplayer_view);
        position = getIntent().getIntExtra("position", 1);
        videoTitle = getIntent().getStringExtra("video_title");
        mVideoFiles = getIntent().getExtras().getParcelableArrayList("videoArrayList");
        //screenOrientation();

        nextButton = findViewById(R.id.exo_next);
        previousButton = findViewById(R.id.exo_prev);
        title = findViewById(R.id.video_title);
        videoBack = findViewById(R.id.video_back);
        lock = findViewById(R.id.lock);
        unlock = findViewById(R.id.unlock);
        scaling = findViewById(R.id.scaling);
        root = findViewById(R.id.root_layout);
        nightMode = findViewById(R.id.night_mode);
        videoList =findViewById(R.id.video_list);
        recyclerViewIcons = findViewById(R.id.recyclerview_icon);

        title.setText(videoTitle);
        nextButton.setOnClickListener(this);
        previousButton.setOnClickListener(this);
        videoBack.setOnClickListener(this);
        lock.setOnClickListener(this);
        unlock.setOnClickListener(this);
        videoList.setOnClickListener(this);
        scaling.setOnClickListener(firstListener);

        dialogProperties = new DialogProperties();
        filePickerDialog = new FilePickerDialog(VideoPlayerActivity.this);
        filePickerDialog.setTitle("Select a Subtitle File");
        filePickerDialog.setPositiveBtnName("OK");
        filePickerDialog.setPositiveBtnName("Cancel");

        if (Build.VERSION.SDK_INT>=Build.VERSION_CODES.O){
            pictureInPicture = new PictureInPictureParams.Builder();
        }

        iconModelsArrayList.add(new IconModel(R.drawable.ic_right, ""));
        iconModelsArrayList.add(new IconModel(R.drawable.ic_night_mode, "Night"));
        iconModelsArrayList.add(new IconModel(R.drawable.ic_pip_mode, "Popup"));
        iconModelsArrayList.add(new IconModel(R.drawable.ic_equalizer, "Equalizer"));
        iconModelsArrayList.add(new IconModel(R.drawable.ic_rotate, "Rotate"));
        playbackIconAdapter = new PlaybackIconAdapter(iconModelsArrayList, this);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this,
                RecyclerView.HORIZONTAL, true);
        recyclerViewIcons.setLayoutManager(layoutManager);
        recyclerViewIcons.setAdapter(playbackIconAdapter);
        playbackIconAdapter.notifyDataSetChanged();
        playbackIconAdapter.setOnItemClickListener(new PlaybackIconAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(int position) {
                if (position == 0) {
                    if (expand) {
                        iconModelsArrayList.clear();
                        iconModelsArrayList.add(new IconModel(R.drawable.ic_right, ""));
                        iconModelsArrayList.add(new IconModel(R.drawable.ic_night_mode, "Night"));
                        iconModelsArrayList.add(new IconModel(R.drawable.ic_pip_mode, "Popup"));
                        iconModelsArrayList.add(new IconModel(R.drawable.ic_equalizer, "Equalizer"));
                        iconModelsArrayList.add(new IconModel(R.drawable.ic_rotate, "Rotate"));
                        playbackIconAdapter.notifyDataSetChanged();
                        expand = false;
                    } else {
                        if (iconModelsArrayList.size() == 5) {
                            iconModelsArrayList.add(new IconModel(R.drawable.ic_volume_off, "Mute"));
                            iconModelsArrayList.add(new IconModel(R.drawable.ic_volume, "Volume"));
                            iconModelsArrayList.add(new IconModel(R.drawable.ic_brightness, "Brightness"));
                            iconModelsArrayList.add(new IconModel(R.drawable.ic_speed, "Speed"));
                            iconModelsArrayList.add(new IconModel(R.drawable.ic_subtitle, "Subtitle"));
                        }
                        iconModelsArrayList.set(position, new IconModel(R.drawable.ic_left, ""));
                        playbackIconAdapter.notifyDataSetChanged();
                        expand = true;
                    }
                }
                if (position == 1) {
                    // night mode
                    if (dark) {
                        nightMode.setVisibility(View.GONE);
                        iconModelsArrayList.set(position, new IconModel(R.drawable.ic_night_mode, "Night"));
                        playbackIconAdapter.notifyDataSetChanged();
                        dark = false;
                    } else {
                        nightMode.setVisibility(View.VISIBLE);
                        iconModelsArrayList.set(position, new IconModel(R.drawable.ic_night_mode, "Day"));
                        playbackIconAdapter.notifyDataSetChanged();
                        dark = true;
                    }
                }
                if (position == 2) {
                    //popup
                    if (Build.VERSION.SDK_INT>=Build.VERSION_CODES.O){
                        Rational aspectRatio = new Rational(16,9 );
                        pictureInPicture.setAspectRatio(aspectRatio);
                        enterPictureInPictureMode(pictureInPicture.build());
                    }else {
                        Log.wtf("not oreo","yes");
                    }
                    //

                }
                if (position == 3) {
                    //equalizer
                    Intent intent = new Intent(AudioEffect.ACTION_DISPLAY_AUDIO_EFFECT_CONTROL_PANEL);
                    if((intent.resolveActivity(getPackageManager())!=null)){
                        startActivityForResult(intent,123);
                    }else{
                        Toast.makeText(VideoPlayerActivity.this, "No Equalizer Found", Toast.LENGTH_SHORT).show();
                    }
                    playbackIconAdapter.notifyDataSetChanged();
                    //
                    Toast.makeText(VideoPlayerActivity.this,"fourth", Toast.LENGTH_SHORT).show();

                }
                if(position ==4 ){
                    //rotate
                    if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT){
                        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
                        playbackIconAdapter.notifyDataSetChanged();
                    }else if(getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE){
                        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
                        playbackIconAdapter.notifyDataSetChanged();
                    }



                }
                if(position == 5){
                    //mute
                    if (mute){
                        player.setVolume(100);
                        iconModelsArrayList.set(position, new IconModel(R.drawable.ic_volume_off, "Mute"));
                        playbackIconAdapter.notifyDataSetChanged();
                        mute = false;
                    }
                    else{
                        player.setVolume(0);
                        iconModelsArrayList.set(position, new IconModel(R.drawable.ic_volume, "unMute"));
                        playbackIconAdapter.notifyDataSetChanged();
                        mute = true;
                    }


                }
                if(position == 6){
                    //volume
                    VolumeDialog volumeDialog = new VolumeDialog();
                    volumeDialog.show(getSupportFragmentManager(),"dialog");
                    playbackIconAdapter.notifyDataSetChanged();


                }
                if (position == 7){
                    //brightness
                    BrightnessDialog brightnessDialog = new BrightnessDialog();
                    brightnessDialog.show(getSupportFragmentManager(),"dialog");
                    playbackIconAdapter.notifyDataSetChanged();


                }
                if(position == 8){
                    //speed
                    AlertDialog.Builder alerDialog = new AlertDialog.Builder(VideoPlayerActivity.this);
                    alerDialog.setTitle("Select Playback Speed").setPositiveButton("OK",null);
                    String[] items ={"0,5x", "1x Normal", "1.25x", "1.5x", "2x"};
                    int checkedItem = -1;
                    alerDialog.setSingleChoiceItems(items, checkedItem, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            switch (which){
                                case 0:
                                    speed = 0.5f;
                                    parameters = new PlaybackParameters(speed);
                                    player.setPlaybackParameters(parameters);
                                    break;
                                case 1:
                                    speed = 1f;
                                    parameters = new PlaybackParameters(speed);
                                    player.setPlaybackParameters(parameters);
                                    break;
                                case 2:
                                    speed = 1.25f;
                                    parameters = new PlaybackParameters(speed);
                                    player.setPlaybackParameters(parameters);
                                    break;
                                case 3:
                                    speed = 1.5f;
                                    parameters = new PlaybackParameters(speed);
                                    player.setPlaybackParameters(parameters);
                                    break;
                                case 4:
                                    speed = 2f;
                                    parameters = new PlaybackParameters(speed);
                                    player.setPlaybackParameters(parameters);
                                    break;
                                default:
                                    break;
                            }
                        }
                    });
                    AlertDialog alert = alerDialog.create();
                    alert.show();

                }
                if(position == 9){
                    //subtitle
                    dialogProperties.selection_mode = DialogConfigs.SINGLE_MODE;
                    dialogProperties.extensions = new String[]{".srt"};
                    dialogProperties.root = new File("/storage/emulated/0");
                    filePickerDialog.setProperties(dialogProperties);
                    filePickerDialog.show();
                    filePickerDialog.setDialogSelectionListener(new DialogSelectionListener() {
                        @Override
                        public void onSelectedFilePaths(String[] strings) {
                            for (String path: strings){
                                File file = new File(path);
                                uriSubtile = Uri.parse(file.getAbsolutePath().toString());
                            }
                            playVideoSubtitle(uriSubtile);
                        }

                        private void playVideoSubtitle(Uri uriSubtile) {
                        }
                    });
                }

            }
        });
        playVideo();
    }

    private void playVideo() {
        String path = mVideoFiles.get(position).getPath();
        Uri uri = Uri.parse(path);
        player = new SimpleExoPlayer.Builder(this).build();
        DefaultDataSourceFactory dataSourceFactory = new DefaultDataSourceFactory(
                this, Util.getUserAgent(this, "app"));
        concatenatingMediaSource = new ConcatenatingMediaSource();
        for (int i = 0; i < mVideoFiles.size(); i++) {
            new File(String.valueOf(mVideoFiles.get(i)));
            MediaSource mediaSource = new ProgressiveMediaSource.Factory(dataSourceFactory)
                    .createMediaSource(Uri.parse(String.valueOf(uri)));
            concatenatingMediaSource.addMediaSource(mediaSource);
        }
        playerView.setPlayer(player);
        playerView.setKeepScreenOn(true);
        player.setPlaybackParameters(parameters);
        player.prepare(concatenatingMediaSource);
        player.seekTo(position, C.TIME_UNSET);
        playError();
    }
    private void playVideoSubtitle(Uri subtitle) {
        long oldPosition = player.getCurrentPosition();
        player.stop();

        String path = mVideoFiles.get(position).getPath();
        Uri uri = Uri.parse(path);
        player = new SimpleExoPlayer.Builder(this).build();
        DefaultDataSourceFactory dataSourceFactory = new DefaultDataSourceFactory(
                this, Util.getUserAgent(this, "app"));
        concatenatingMediaSource = new ConcatenatingMediaSource();
        for (int i = 0; i < mVideoFiles.size(); i++) {
            new File(String.valueOf(mVideoFiles.get(i)));
            MediaSource mediaSource = new ProgressiveMediaSource.Factory(dataSourceFactory)
                    .createMediaSource(Uri.parse(String.valueOf(uri)));
            Format textFormat = Format.createTextSampleFormat(null, MimeTypes.APPLICATION_SUBRIP,Format.NO_VALUE,"app");
            MediaSource subtitleSource = new SingleSampleMediaSource.Factory(dataSourceFactory).setTreatLoadErrorsAsEndOfStream(true)
                    .createMediaSource(Uri.parse(String.valueOf(subtitle)),textFormat,C.TIME_UNSET);
            MergingMediaSource mergingMediaSource = new MergingMediaSource(mediaSource,subtitleSource);
            concatenatingMediaSource.addMediaSource(mergingMediaSource);
        }
        playerView.setPlayer(player);
        playerView.setKeepScreenOn(true);
        player.setPlaybackParameters(parameters);
        player.prepare(concatenatingMediaSource);
        player.seekTo(position, oldPosition);
        playError();
    }

    private  void screenOrientation(){
        try{
            MediaMetadataRetriever retriever = new MediaMetadataRetriever();
            Bitmap bitmap;
            String path = mVideoFiles.get(position).getPath();
            Uri uri = Uri.parse(path);
            retriever.setDataSource(this,uri);
            bitmap = retriever.getFrameAtTime();

            int videoWidth = bitmap.getWidth();
            int videoHeight = bitmap.getHeight();
            if (videoWidth > videoHeight) {
                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
            } else {
                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
            }

        }catch (Exception e){
            Log.e("MediaMetaDataRetriever", "screenOrientation: ");
        }


    }

    private void playError() {
        player.addListener(new Player.EventListener() {
            @Override
            public void onPlayerError(ExoPlaybackException error) {
                Toast.makeText(VideoPlayerActivity.this, "Video Playing Error", Toast.LENGTH_SHORT).show();
            }
        });
        player.setPlayWhenReady(true);
    }


    @Override
    public void onBackPressed() {
        super.onBackPressed();
        if (player.isPlaying()) {
            player.stop();
        }
    }


    @Override
    protected void onPause() {
        super.onPause();
        player.setPlayWhenReady(false);
        player.getPlaybackState();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            if (isInPictureInPictureMode()){
                player.setPlayWhenReady(true);
            }else {
                player.setPlayWhenReady(false);
                player.getPlaybackState();
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        player.setPlayWhenReady(true);
        player.getPlaybackState();

    }

    @Override
    protected void onRestart() {
        super.onRestart();
        player.setPlayWhenReady(true);
        player.getPlaybackState();
    }

    private void setFullScreen() {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
    }


    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.video_back) {
            if (player != null) {
                player.release();
            }
            finish();
        }
        else if (id == R.id.video_list) {
            PlaylistDialog playlistDialog = new PlaylistDialog(mVideoFiles,videoFilesAdapter);
            playlistDialog.show(getSupportFragmentManager(),playlistDialog.getTag());
        }

        else if (id == R.id.lock) {
            controlsMode = controlsMode.FULLSCREEN;
            root.setVisibility(View.VISIBLE);
            lock.setVisibility(View.INVISIBLE);
            Toast.makeText(this, "unlock", Toast.LENGTH_SHORT).show();
        } else if (id == R.id.unlock) {
            controlsMode = controlsMode.LOCK;
            root.setVisibility(View.INVISIBLE);
            lock.setVisibility(View.VISIBLE);
            Toast.makeText(this, "Locked", Toast.LENGTH_SHORT).show();
        } else if (id == R.id.exo_next) {
            try {
                player.stop();
                position++;
                playVideo();
                // title.setText(mVideoFiles.get(position).getDisplayName());
            } catch (Exception e) {
                Toast.makeText(this, "no Next Video", Toast.LENGTH_SHORT).show();
                finish();
            }
        } else if (id == R.id.exo_prev) {
            try {
                player.stop();
                position--;
                playVideo();
                // title.setText(mVideoFiles.get(position).getDisplayName());
            } catch (Exception e) {
                Toast.makeText(this, "no Previus Video", Toast.LENGTH_SHORT).show();
                finish();
            }
        }
    }

    View.OnClickListener firstListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            playerView.setResizeMode(AspectRatioFrameLayout.RESIZE_MODE_FILL);
            player.setVideoScalingMode(C.VIDEO_SCALING_MODE_DEFAULT);
            scaling.setImageResource(R.drawable.fullscreen);
            Toast.makeText(VideoPlayerActivity.this, "Full Screen", Toast.LENGTH_SHORT).show();
            scaling.setOnClickListener(secondListener);
        }
    };
    View.OnClickListener secondListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            playerView.setResizeMode(AspectRatioFrameLayout.RESIZE_MODE_ZOOM);
            player.setVideoScalingMode(C.VIDEO_SCALING_MODE_DEFAULT);
            scaling.setImageResource(R.drawable.zoom);
            Toast.makeText(VideoPlayerActivity.this, "Zoom", Toast.LENGTH_SHORT).show();
            scaling.setOnClickListener(thirdListener);
        }
    };
    View.OnClickListener thirdListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            playerView.setResizeMode(AspectRatioFrameLayout.RESIZE_MODE_FIT);
            player.setVideoScalingMode(C.VIDEO_SCALING_MODE_DEFAULT);
            scaling.setImageResource(R.drawable.fit);
            Toast.makeText(VideoPlayerActivity.this, "Fit", Toast.LENGTH_SHORT).show();
            scaling.setOnClickListener(firstListener);
        }
    };

    @Override
    public void onPictureInPictureModeChanged(boolean isInPictureInPictureMode) {
        super.onPictureInPictureModeChanged(isInPictureInPictureMode);
        isCrossChecked = isInPictureInPictureMode;
        if (isInPictureInPictureMode){
            playerView.hideController();
        }else {
            playerView.showController();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (isCrossChecked){
            player.release();
            finish();
        }
    }
}