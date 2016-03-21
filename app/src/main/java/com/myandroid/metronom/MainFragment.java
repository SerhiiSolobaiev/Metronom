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

public class MainFragment extends Fragment {

    private static final String LOG_TAG = MainFragment.class.getSimpleName();
    private static final String APP_PREFERENCES = "MainPreferences";
    private static final String APP_PREFERENCES_STATE_START_BUTTON = "buttonStart";

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
    private boolean isIndicator; //for showing changes of indicator

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        sharedpreferences = getContext().getSharedPreferences(APP_PREFERENCES, Context.MODE_PRIVATE);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_main, container, false);

        buttonStart = (ToggleButton) view.findViewById(R.id.button_start);
        buttonVibration = (ToggleButton) view.findViewById(R.id.button_vibrate);
        buttonFlash = (ToggleButton) view.findViewById(R.id.button_flash);
        buttonSound = (ToggleButton) view.findViewById(R.id.button_sound);
        editTextBPM = (EditText) view.findViewById(R.id.editText_BPM);
        seekBar = (SeekBar) view.findViewById(R.id.seekBar);
        buttonLess = (Button) view.findViewById(R.id.button_less);
        buttonMore = (Button) view.findViewById(R.id.button_more);
        indicator = (ToggleButton) view.findViewById(R.id.indicator);

        editTextBPM.setText(seekBar.getProgress() + "");
        buttonStart.setChecked(
                sharedpreferences.getBoolean(APP_PREFERENCES_STATE_START_BUTTON, true));
        buttonStart.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                SharedPreferences.Editor editor = sharedpreferences.edit();
                if (isChecked) {
                    startService();
                    editor.putBoolean(APP_PREFERENCES_STATE_START_BUTTON, true);
                } else {
                    stopService();
                    editor.putBoolean(APP_PREFERENCES_STATE_START_BUTTON, false);
                }
                editor.apply();
            }
        });

        addOnParametersClickListeners();
        return view;
    }

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
                if (editTextBPM.getText().toString().length() != 0)
                    seekBar.setProgress(Integer.valueOf(editTextBPM.getText().toString()));
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        buttonLess.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                seekBar.setProgress(seekBar.getProgress() - 1);
            }
        });

        buttonMore.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                seekBar.setProgress(seekBar.getProgress() + 1);
            }
        });
    }

    private void startService() {
        final boolean[] parameters = {buttonVibration.isChecked(),
                buttonFlash.isChecked(), buttonSound.isChecked()};
        LocalBroadcastManager.getInstance(getContext()).registerReceiver(messageFromService,
                new IntentFilter("updateIndicatorOnUI"));

        getActivity().startService(new Intent(MetronomService.METRONOM_SERVICE)
                .putExtra("parameters", parameters)
                .putExtra("frequency", seekBar.getProgress()));
        isIndicator = indicator.isChecked();
        Log.v(LOG_TAG, "service started");
    }

    private void stopService() {
        getActivity().stopService(new Intent(MetronomService.METRONOM_SERVICE));
        LocalBroadcastManager.getInstance(getContext()).unregisterReceiver(messageFromService);
        Log.v(LOG_TAG, "service stopped");

        indicator.setChecked(false);
    }

    private BroadcastReceiver messageFromService = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (!isIndicator) {
                if (indicator.isChecked()) {
                    indicator.setChecked(false);
                } else {
                    indicator.setChecked(true);
                }
            }
            Log.v(LOG_TAG, "messageFromService received");
        }
    };
}
