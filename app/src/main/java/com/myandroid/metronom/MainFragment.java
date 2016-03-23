package com.myandroid.metronom;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.LocalBroadcastManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.ToggleButton;

/**
 * Main Fragment of the program
 */
public class MainFragment extends Fragment {

    private static final String LOG_TAG = MainFragment.class.getSimpleName();
    private static final String APP_PREFERENCES = "MainPreferences";
    private static final String APP_PREFERENCES_STATE_START_BUTTON = "buttonStart";
    private static final String APP_PREFERENCES_STATE_VIBRATE_BUTTON = "buttonVibration";
    private static final String APP_PREFERENCES_STATE_FLASH_BUTTON = "buttonFlash";
    private static final String APP_PREFERENCES_STATE_SOUND_BUTTON = "buttonSound";
    private static final String APP_PREFERENCES_STATE_PROGRESS = "seekbarProgress";
    private static final String APP_PREFERENCES_STATE_INDICATOR = "indicator";
    private static final int ADD_VALUE_TO_PROGRESS = 1; //value that adding to progress for one click

    private ToggleButton buttonStart;
    private ToggleButton buttonVibration;
    private ToggleButton buttonFlash;
    private ToggleButton buttonSound;
    private EditText editTextBPM;
    private SeekBar seekBar;
    private Button buttonLess;
    private Button buttonMore;
    private ToggleButton indicator;

    private SharedPreferences sharedpreferences;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        sharedpreferences = getContext().getSharedPreferences(APP_PREFERENCES, Context.MODE_PRIVATE);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_main, container, false);

        initViews(view);
        initSharedPreferences();

        /*start or stop service listener*/
        buttonStart.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    startService();
                } else {
                    stopService();
                }
            }
        });

        addOnParametersClickListeners();
        return view;
    }

    /**
     * Init views of the fragment
     *
     * @param view Root view
     */
    private void initViews(View view) {
        buttonStart = (ToggleButton) view.findViewById(R.id.button_start);
        buttonVibration = (ToggleButton) view.findViewById(R.id.button_vibrate);
        buttonFlash = (ToggleButton) view.findViewById(R.id.button_flash);
        buttonSound = (ToggleButton) view.findViewById(R.id.button_sound);
        editTextBPM = (EditText) view.findViewById(R.id.editText_BPM);
        seekBar = (SeekBar) view.findViewById(R.id.seekBar);
        buttonLess = (Button) view.findViewById(R.id.button_less);
        buttonMore = (Button) view.findViewById(R.id.button_more);
        indicator = (ToggleButton) view.findViewById(R.id.indicator);
    }

    /**
     * Restore initiate states from sharedPreferences
     */
    private void initSharedPreferences() {
        buttonStart.setChecked(
                sharedpreferences.getBoolean(APP_PREFERENCES_STATE_START_BUTTON, false));
        buttonVibration.setChecked(
                sharedpreferences.getBoolean(APP_PREFERENCES_STATE_VIBRATE_BUTTON, true));
        buttonFlash.setChecked(
                sharedpreferences.getBoolean(APP_PREFERENCES_STATE_FLASH_BUTTON, true));
        buttonSound.setChecked(
                sharedpreferences.getBoolean(APP_PREFERENCES_STATE_SOUND_BUTTON, true));
        seekBar.setProgress(
                sharedpreferences.getInt(APP_PREFERENCES_STATE_PROGRESS, 100));
        indicator.setChecked(
                sharedpreferences.getBoolean(APP_PREFERENCES_STATE_INDICATOR, false));
        editTextBPM.setText(seekBar.getProgress() + ""); //set current progress to editText
    }

    /**
     * Listeners for changing states of views
     */
    private void addOnParametersClickListeners() {
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                editTextBPM.setText(seekBar.getProgress() + "");
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });

        editTextBPM.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.length() != 0)
                    seekBar.setProgress(Integer.valueOf(editTextBPM.getText().toString()));
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        buttonLess.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                seekBar.setProgress(seekBar.getProgress() - ADD_VALUE_TO_PROGRESS);
            }
        });

        buttonMore.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                seekBar.setProgress(seekBar.getProgress() + ADD_VALUE_TO_PROGRESS);
            }
        });
    }

    /**
     * Start service that makes all work
     */
    private void startService() {
        final boolean[] parameters = {buttonVibration.isChecked(),
                buttonFlash.isChecked(), buttonSound.isChecked()};

        getActivity().startService(new Intent(MetronomService.METRONOM_SERVICE)
                .putExtra("parameters", parameters)
                .putExtra("frequency", seekBar.getProgress()));

        Log.v(LOG_TAG, "service started");
    }

    /**
     * Stop service that runs in background
     */
    private void stopService() {
        getActivity().stopService(new Intent(MetronomService.METRONOM_SERVICE));
        Log.v(LOG_TAG, "service stopped");

        indicator.setChecked(false);
    }

    @Override
    public void onResume() {
        super.onResume();
        LocalBroadcastManager.getInstance(getContext()).registerReceiver(messageFromService,
                new IntentFilter("updateIndicatorOnUI"));
    }

    @Override
    public void onPause() {
        super.onPause();
        LocalBroadcastManager.getInstance(getContext()).unregisterReceiver(messageFromService);
    }

    /**
     * BroadcastReceiver that receives messages from service
     */
    private BroadcastReceiver messageFromService = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (indicator.isChecked()) {
                indicator.setChecked(false);
            } else {
                indicator.setChecked(true);
            }
            Log.v(LOG_TAG, "messageFromService received");
        }
    };

    @Override
    public void onStop() {
        super.onStop();
        SharedPreferences.Editor editor = sharedpreferences.edit();
        editor.putBoolean(APP_PREFERENCES_STATE_VIBRATE_BUTTON, buttonVibration.isChecked());
        editor.putBoolean(APP_PREFERENCES_STATE_FLASH_BUTTON, buttonFlash.isChecked());
        editor.putBoolean(APP_PREFERENCES_STATE_SOUND_BUTTON, buttonSound.isChecked());
        editor.putInt(APP_PREFERENCES_STATE_PROGRESS, seekBar.getProgress());
        editor.putBoolean(APP_PREFERENCES_STATE_START_BUTTON, buttonStart.isChecked());
        editor.apply();
    }
}
