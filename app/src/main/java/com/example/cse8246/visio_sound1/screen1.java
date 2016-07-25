package com.example.cse8246.visio_sound1;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.musicg.fingerprint.FingerprintSimilarity;
import com.musicg.wave.Wave;

public class screen1 extends AppCompatActivity {

    private String[] arraySpinner;
    final Button recordbutton = (Button) findViewById(R.id.Record);
    final Button stopButton = (Button) findViewById(R.id.Stop);
    final Button playStudent = (Button) findViewById(R.id.PlayTrain);
    final Button playBase = (Button) findViewById(R.id.PlayBase);
    final Button match = (Button) findViewById(R.id.Match);
    final Spinner dropDown = (Spinner) findViewById(R.id.spinner);
    final TextView text = (TextView) findViewById(R.id.textView);
    VisioAudioRecorder recorder = new VisioAudioRecorder();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_screen1);
        this.arraySpinner = new String[] {
                "hello", "prey", "pray", "bye"
        };
        Spinner s = (Spinner) findViewById(R.id.spinner);
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_spinner_item, arraySpinner);
        s.setAdapter(adapter);

        recordbutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                recorder.Record(true);
                playStudent.setEnabled(false);
                stopButton.setVisibility(View.VISIBLE);
                recordbutton.setVisibility(View.INVISIBLE);
                text.setVisibility(View.INVISIBLE);
            }
        });

        stopButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view)
            {
                recorder.Stop();
                playStudent.setEnabled(true);
                recordbutton.setVisibility(View.VISIBLE);
                playStudent.setVisibility(View.VISIBLE);
            }
        });

        playStudent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                recorder.PlayLearn();
            }
        });

        playBase.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (dropDown.getSelectedItem() != null) {
                    String selectedItem = dropDown.getSelectedItem().toString();
                    text.setVisibility(View.INVISIBLE);
                    recorder.Play(selectedItem);
                    Toast.makeText(getApplicationContext(), "Playing audio", Toast.LENGTH_LONG).show();
                } else
                    Toast.makeText(getApplicationContext(), "No Audio selected", Toast.LENGTH_LONG).show();

            }
        });

        match.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String learnaudio = recorder.path + '/' + recorder.AUDIO_RECORDER_FOLDER + '/'+'/'+ recorder.AUDIO_Learner_FOLDER + '/'+ recorder.getLearnFileNameForModify();
                String file = null;
                if (dropDown.getSelectedItem() != null) {
                    file = dropDown.getSelectedItem().toString();
                }
                String baseaudio = recorder.path+'/'+ recorder.AUDIO_RECORDER_FOLDER +'/'+file;
                Wave waveA = new Wave(learnaudio);
                Wave waveB = new Wave(baseaudio);
                FingerprintSimilarity similarity;

                similarity = waveA.getFingerprintSimilarity(waveB);
                text.setText(similarity.getSimilarity() * 100 + "%");
                //t.setText(Double.toString(Math.round(similarity*100))+'%');
                text.setVisibility(View.VISIBLE);

            }
        });





    }
}
