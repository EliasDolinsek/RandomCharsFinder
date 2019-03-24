package com.dolinsek.elias.randomcharsfinder.ui;

import com.dolinsek.elias.randomcharsfinder.core.RandomCharFinder;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.text.Text;

import java.math.BigInteger;
import java.net.URL;
import java.util.Calendar;
import java.util.ResourceBundle;

public class MainController implements Initializable {

    @FXML
    private TextField fldWordsToFind, fldApprovedChars;

    @FXML
    private Text txtStartTime, txtRunningTime, txtStopTime, txtLoopsPerSecond, txtTotalLoops, txtScore;

    @FXML
    private CheckBox cbxIncludeWhitespace;

    @FXML
    private Button btnStart, btnStop, btnReset;

    @FXML
    private Spinner spnThreads, spnUpdateTime;

    private long lastUpdateTimestamp;
    private RandomCharFinder randomCharFinder;
    private RandomCharFinder.RandomCharFinderListener randomCharFinderListener;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        setupListener();
        btnStart.setOnAction(event -> {
            lastUpdateTimestamp = 0;
            randomCharFinder = new RandomCharFinder(getApprovedChars(), getWordsToFind(), randomCharFinderListener);
            randomCharFinder.reset();
            randomCharFinder.start(getThreads());
        });

        btnStop.setOnAction(event -> randomCharFinder.stop());

        btnReset.setOnAction(event -> {
            randomCharFinder.reset();
        });
    }

    private void setupListener(){
        randomCharFinderListener = new RandomCharFinder.RandomCharFinderListener() {
            @Override
            public void onStart() {
                txtStartTime.setText(getDateStampAsReadableString(randomCharFinder.getStartTime()));
                btnStart.setDisable(true);
                btnReset.setDisable(true);
                btnStop.setDisable(false);
            }

            @Override
            public void onStop(BigInteger loops, long runningTime) {
                txtStopTime.setText(getDateStampAsReadableString(randomCharFinder.getStopTime()));
                btnStart.setDisable(false);
                btnStop.setDisable(true);
                btnReset.setDisable(false);
            }

            @Override
            public void onCharGenerated(String c, BigInteger loops) {
                displayUpdateIfNecessary(loops);
            }

            @Override
            public void onNewBestScore(int bestScore) {
                txtScore.setText(String.valueOf(bestScore));
            }

            @Override
            public void onReset() {
                txtStopTime.setText("unknown");
                txtRunningTime.setText("unknown");
                txtStopTime.setText("unknown");
                txtLoopsPerSecond.setText("unknown");
                txtTotalLoops.setText("unknown");
                txtScore.setText("unknown");

                btnStart.setDisable(false);
                btnStop.setDisable(true);
                btnReset.setDisable(true);
            }
        };
    }

    private String getApprovedChars(){
        return fldApprovedChars.getText().trim() + (cbxIncludeWhitespace.isSelected() ? " "  : "");
    }

    private String getWordsToFind(){
        return  fldWordsToFind.getText();
    }

    private int getThreads(){
        return 4;
    }

    private void displayUpdateIfNecessary(BigInteger loops){
        if (System.currentTimeMillis() - lastUpdateTimestamp >= getUpdateTime()){
           txtRunningTime.setText(getTimeAsReadableString(randomCharFinder.getRunningTime()));
           txtTotalLoops.setText(loops.toString());
           txtLoopsPerSecond.setText(loops.divide(BigInteger.valueOf(randomCharFinder.getRunningTime() + 1)).toString());
           lastUpdateTimestamp = System.currentTimeMillis();
        }
    }

    private long getUpdateTime(){
        return 1000;
    }

    private String getDateStampAsReadableString(long timeStamp){
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(timeStamp);

        return calendar.get(Calendar.DAY_OF_MONTH) + "." + calendar.get(Calendar.MONTH) + "." + calendar.get(Calendar.YEAR) + " " + calendar.get(Calendar.HOUR) + ":" + calendar.get(Calendar.MINUTE) + ":" + calendar.get(Calendar.SECOND) + "." + calendar.get(Calendar.MILLISECOND);
    }

    private String getTimeAsReadableString(long timeInMillis){
        int seconds = (int) (timeInMillis / 1000) % 60 ;
        int minutes = (int) ((timeInMillis / (1000*60)) % 60);
        int hours   = (int) ((timeInMillis / (1000*60*60)) % 24);

        return hours + ":" + minutes + ":" + seconds + ".";
    }
}
