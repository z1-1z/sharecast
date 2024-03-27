package com.intelligent.share.tool;

import android.content.Context;
import android.text.TextUtils;

import com.intelligent.share.base.ShareApp;

import java.util.ArrayList;
import java.util.List;

/**
 * @author xxx
 * @date 2020/6/4
 */

public class HistoryWordMgr {
    private Context mContext;
    public final static String TYPE_ULL = "url";
    private static final int MAX_HISTORY_WORDS = 10;
    private static final String SEPARATE_LINE = ",";
    private static HistoryWordMgr sInstance;

    private HistoryWordMgr(Context context) {
        mContext = context;
    }

    public synchronized static HistoryWordMgr getInstance(Context context) {
        if (sInstance == null) {
            sInstance = new HistoryWordMgr(context.getApplicationContext());
        }
        return sInstance;
    }

    public List<String> getHistoryWords(String type) {
        return generateWords(ShareApp.get(type, ""));
    }

    private List<String> generateWords(String wordsStr) {
        List<String> historyWordList = new ArrayList<>();
        if (!TextUtils.isEmpty(wordsStr)) {
            String[] words = wordsStr.split(SEPARATE_LINE);
            for (String word : words) {
                if (!TextUtils.isEmpty(word)) {
                    historyWordList.add(word);
                }
            }
        }
        return historyWordList;
    }

    public void saveHistoryWords(String type, List<String> historyWordList) {
        StringBuilder newHistoryWords = new StringBuilder();
        if (historyWordList.size() > 0) {
            for (int i = 0; i < historyWordList.size(); i++) {
                newHistoryWords.append(historyWordList.get(i)).append(SEPARATE_LINE);
                if (i == MAX_HISTORY_WORDS - 1) {
                    break;
                }
            }
            newHistoryWords.deleteCharAt(newHistoryWords.length() - 1);
        }
        ShareApp.set(type, newHistoryWords.toString());
    }

    public void addHistoryWord(String type, String inputWord) {
        List<String> historyWords = getHistoryWords(type);
        if (historyWords.contains(inputWord)) {
            historyWords.remove(inputWord);
        }
        historyWords.add(0, inputWord);
        if (historyWords.size() > MAX_HISTORY_WORDS) {
            historyWords.remove(MAX_HISTORY_WORDS);
        }
        saveHistoryWords(type, historyWords);
    }

    public void removeHistoryWord(String type, String word) {
        List<String> historyWords = getHistoryWords(type);
        if (historyWords.contains(word)) {
            historyWords.remove(word);
        }
        saveHistoryWords(type, historyWords);
    }

    public void clearHistory(String type) {
        ShareApp.set(type, "");
    }

}
