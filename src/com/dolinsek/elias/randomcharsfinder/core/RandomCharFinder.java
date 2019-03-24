package com.dolinsek.elias.randomcharsfinder.core;

import java.math.BigInteger;
import java.util.Random;

public class RandomCharFinder implements Runnable {

    private RandomCharFinderListener listener;

    private String approvedChars;
    private String searchedChars;

    private int bestScore = 0;
    private BigInteger loops = BigInteger.ZERO;

    private boolean running;

    private long startTime, stopTime;

    private final Object runningLock = new Object();

    public RandomCharFinder(String approvedChars, String searchedChars, RandomCharFinderListener listener) {
        this.approvedChars = approvedChars;
        this.searchedChars = searchedChars;
        this.listener = listener;
    }

    public void start(int threads) {
        startTime = System.currentTimeMillis();
        running = true;

        for (int i = 0; i < threads; i++) {
            new Thread(this).start();
        }

        listener.onStart();
    }

    public void stop() {
        running = false;
        stopTime = System.currentTimeMillis();
        listener.onStop(loops, stopTime - startTime);
    }

    public void reset(){
        startTime = 0;
        stopTime = 0;
        bestScore = 0;
        loops = BigInteger.ZERO;
        listener.onReset();
    }

    @Override
    public void run() {
        Random random = new Random();
        StringBuilder stringBuilder = new StringBuilder();
        String currentStringRow;

        while (true) {
            synchronized (runningLock){
                if(!running){
                    return;
                } else {
                    loops = loops.add(BigInteger.ONE);
                    appendRandomString(stringBuilder, random);
                    currentStringRow = stringBuilder.toString();
                    listener.onCharGenerated(currentStringRow, loops);
                }

                updateScoreIfNecessary(currentStringRow);
                if (searchedChars.equals(currentStringRow)) {
                    stopTime = System.currentTimeMillis();
                    stop();
                }

                if (currentStringRow.length() == searchedChars.length()) {
                    stringBuilder.deleteCharAt(0);
                }
            }
        }
    }

    private void updateScoreIfNecessary(String generatedString){
        int score = getScore(generatedString);
        if(score > bestScore){
            bestScore = score;
            listener.onNewBestScore(bestScore);
        }
    }

    private int getScore(String generatedString){
        for(int i = 0; i<generatedString.length(); i++){
            if(generatedString.charAt(i) != searchedChars.charAt(i)){
                return i;
            }
        }

        return generatedString.length();
    }

    private void appendRandomString(StringBuilder stringBuilder, Random random){
        stringBuilder.append(approvedChars.charAt(random.nextInt(approvedChars.length())));
    }

    public void setApprovedChars(String approvedChars) {
        this.approvedChars = approvedChars;
    }

    public void setSearchedChars(String searchedChars) {
        this.searchedChars = searchedChars;
    }

    public RandomCharFinderListener getListener() {
        return listener;
    }

    public String getApprovedChars() {
        return approvedChars;
    }

    public String getSearchedChars() {
        return searchedChars;
    }

    public int getBestScore() {
        return bestScore;
    }

    public BigInteger getLoops() {
        return loops;
    }

    public boolean isRunning() {
        return running;
    }

    public long getStartTime() {
        return startTime;
    }

    public long getStopTime() {
        return stopTime;
    }

    public long getRunningTime() {
        return System.currentTimeMillis() - startTime;
    }

    public static interface RandomCharFinderListener {

        void onStart();

        void onStop(BigInteger loops, long runningTime);

        void onCharGenerated(String c, BigInteger loops);

        void onNewBestScore(int bestScore);

        void onReset();
    }
}
