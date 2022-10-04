package me.ag2s.lyraandroidtest;


import static android.view.ViewGroup.LayoutParams.MATCH_PARENT;
import static android.view.ViewGroup.LayoutParams.WRAP_CONTENT;

import android.app.Activity;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainActivity extends Activity {
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private ScrollView scrollView;
    private TextView textView;
    private Button encodeBtn, decodeBtn;
    private static final String[] BitrateS = {"3200", "6000", "9200"};
    private String Bitrate = "3200";


    private void checkFile(){
        File sample=new File(getExternalCacheDir(),"test.wav");
        if (!sample.exists()){
            IoUtils.copyAsset(this,"test.wav",sample.getAbsolutePath());
        }

        File decodeExeFile = new File(getCacheDir(), "lyra-decoder");
        File encodeExeFile= new File(getCacheDir(), "lyra-decoder");
        File modelFiles = new File(getCacheDir(), "model_coeffs");

        if(!(decodeExeFile.exists()&&encodeExeFile.exists()&&modelFiles.exists())){
            IoUtils.copyAssets(this,"lyra",getCacheDir().getAbsolutePath());
        }




    }


    private View buildView() {
        LinearLayout rootView = new LinearLayout(this);
        rootView.setOrientation(LinearLayout.VERTICAL);
        rootView.setFitsSystemWindows(true);
        LinearLayout nav = new LinearLayout(this);
        nav.setOrientation(LinearLayout.HORIZONTAL);

        rootView.addView(nav);
        encodeBtn = new Button(this);
        encodeBtn.setText("Encode");
        decodeBtn = new Button(this);
        decodeBtn.setText("Decode");

        Spinner spinner = new Spinner(this);

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, BitrateS);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                Bitrate = BitrateS[position];
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        spinner.setVisibility(View.VISIBLE);


        nav.addView(encodeBtn);
        nav.addView(decodeBtn);

        nav.addView(spinner);


        scrollView = new ScrollView(this);
        LinearLayout.LayoutParams scrollParams = new LinearLayout.LayoutParams(MATCH_PARENT, MATCH_PARENT, 1);
        rootView.addView(scrollView, scrollParams);

        textView = new TextView(this);
        ScrollView.LayoutParams textParams = new ScrollView.LayoutParams(WRAP_CONTENT, WRAP_CONTENT);
        int dp8 = dp2px(8);
        textView.setPadding(dp8, dp8, dp8, dp8);
        textView.setTypeface(Typeface.create(Typeface.MONOSPACE, Typeface.NORMAL));
        textView.setTextIsSelectable(true);
        scrollView.addView(textView, textParams);

        return rootView;
    }

    private void append(String string) {
        scrollView.post(() -> {
            textView.append(string);
            textView.append("\n");
            scrollView.fullScroll(ScrollView.FOCUS_DOWN);
        });
    }

    private void startProcess(String... command) throws Exception {
        Process process = new ProcessBuilder(command).redirectErrorStream(true).start();
        InputStreamReader reader = new InputStreamReader(process.getInputStream());
        try (BufferedReader br = new BufferedReader(reader)) {
            String line = br.readLine();
            while (line != null) {
                append(line);
                line = br.readLine();
            }
        }
        append("[ exit " + process.waitFor() + " ]");
    }


    private void decodeTest() {
        if (Build.SUPPORTED_64_BIT_ABIS.length == 0) return;

        String path = new File(getCacheDir(), "lyra-decoder").getAbsolutePath();
        String model = new File(getCacheDir(), "model_coeffs").getAbsolutePath();
        File in = new File(getExternalCacheDir(), "test.lyra");
        File ot = new File(getExternalCacheDir(), "test_" + Bitrate + "_encode.lyra");
        checkFile();
        IoUtils.copyFile(in, ot);

        try {
            append("[ exec " + path + " ]");
            startProcess("linker64", path,
                    "--model_path", model,
                    "--encoded_path", getExternalCacheDir().getAbsolutePath() + "/test.lyra",
                    "--output_dir", getExternalCacheDir().getAbsolutePath(),
                    "--sample_rate_hz=48000",
                    "--output_suffix", "_" + Bitrate + "_decoded",
                    "--bitrate=" + Bitrate);
        } catch (Exception e) {
            append(Log.getStackTraceString(e));
        }
    }

    private void encodeTest() {
        if (Build.SUPPORTED_64_BIT_ABIS.length == 0) return;
        String path = new File(getCacheDir(), "lyra-encoder").getAbsolutePath();
        String model = new File(getCacheDir(), "model_coeffs").getAbsolutePath();
        checkFile();
        try {
            append("[ exec " + path + " ]");
            startProcess("linker64", path,
                    "--model_path", model,
                    "--input_path", getExternalCacheDir().getAbsolutePath() + "/test.wav",
                    "--output_dir", getExternalCacheDir().getAbsolutePath(),
                    "--bitrate=" + Bitrate);
        } catch (Exception e) {
            append(Log.getStackTraceString(e));
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(buildView());
        checkFile();
        encodeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                executor.submit(MainActivity.this::encodeTest);
            }
        });
        decodeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                executor.submit(MainActivity.this::decodeTest);
            }
        });

    }

    @SuppressWarnings("SameParameterValue")
    private int dp2px(float dp) {
        float density = getResources().getDisplayMetrics().density;
        return (int) (dp * density + 0.5f);
    }

}