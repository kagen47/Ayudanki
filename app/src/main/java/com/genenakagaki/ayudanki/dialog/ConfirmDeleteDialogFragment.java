package com.genenakagaki.ayudanki.dialog;

import android.app.Dialog;
import android.content.DialogInterface;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.util.Log;

import com.genenakagaki.ayudanki.BuildConfig;
import com.genenakagaki.ayudanki.MainActivity;
import com.genenakagaki.ayudanki.R;
import com.genenakagaki.ayudanki.data.CardDb;
import com.genenakagaki.ayudanki.data.QuizDb;
import com.genenakagaki.ayudanki.data.QuizSetDb;
import com.genenakagaki.ayudanki.exception.PreferenceNotFound;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static com.genenakagaki.ayudanki.data.QuizContract.*;
import static java.util.Arrays.asList;

/**
 * Created by gene on 3/5/17.
 */

public class ConfirmDeleteDialogFragment extends DialogFragment {

    private static final String TAG = ConfirmDeleteDialogFragment.class.getSimpleName();
    private static final boolean D = BuildConfig.APP_DEBUG;

    public static final int TYPE_QUIZ_SET = 0;
    public static final int TYPE_QUIZ = 1;
    public static final int TYPE_CARD = 2;

    private static final String ARG_DELETE_TYPE = "arg_delete_type";
    private static final String ARG_ROW_ID = "arg_row_id";
    private static final String ARG_CARD_IDS = "arg_card_ids";

    private int mDeleteType;
    private long mRowId;

    private long[] mCardIds;

    public static ConfirmDeleteDialogFragment newInstance(List<Long> cardIdList) {
        long[] cardIds = new long[cardIdList.size()];
        for (int i = 0; i < cardIds.length; i++) {
            cardIds[i] = cardIdList.get(i);
        }

        Bundle args = new Bundle();
        args.putInt(ARG_DELETE_TYPE, TYPE_CARD);
        args.putLongArray(ARG_CARD_IDS, cardIds);

        ConfirmDeleteDialogFragment fragment = new ConfirmDeleteDialogFragment();
        fragment.setArguments(args);
        return fragment;
    }

    public static ConfirmDeleteDialogFragment newInstance(int deleteType, long rowId) {
        ConfirmDeleteDialogFragment fragment = new ConfirmDeleteDialogFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_DELETE_TYPE, deleteType);
        args.putLong(ARG_ROW_ID, rowId);
        fragment.setArguments(args);
        return fragment;
    }

    public ConfirmDeleteDialogFragment() {}

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Bundle args = getArguments();
        if (args != null) {
            mDeleteType = args.getInt(ARG_DELETE_TYPE);

            if (mDeleteType == TYPE_CARD) {
                mRowId = 0;
                mCardIds = args.getLongArray(ARG_CARD_IDS);
            } else {
                mRowId = args.getLong(ARG_ROW_ID);
            }
        }
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        String title = "";
        String message = "";

        if (mDeleteType == TYPE_QUIZ_SET) {
            String quizSetName = "";
            try {
                quizSetName = QuizSetDb.getCurrentName(getActivity());
            } catch (PreferenceNotFound preferenceNotFound) {
                if (D) Log.d(TAG, preferenceNotFound.getMessage());
            }

            title = getString(R.string.dialog_delete_quiz_set_title, quizSetName);
            message = getString(R.string.dialog_delete_quiz_set_message);
        } else if (mDeleteType == TYPE_QUIZ) {
            String quizName = "";
            try {
                quizName = QuizDb.getCurrentName(getActivity());
            } catch (PreferenceNotFound preferenceNotFound) {
                if (D) Log.d(TAG, preferenceNotFound.getMessage());
            }

            title = getString(R.string.dialog_delete_quiz_title, quizName);
            message = getString(R.string.dialog_delete_quiz_message);
        } else if (mDeleteType == TYPE_CARD) {
            title = getString(R.string.dialog_delete_card_title);
            message = getString(R.string.dialog_delete_card_message);
        }


        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(title)
                .setMessage(message)
                .setPositiveButton(R.string.delete, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        switch (mDeleteType) {
                            case TYPE_QUIZ_SET:
                                deleteQuizSet();
                                break;
                            case TYPE_QUIZ:
                                deleteQuiz();
                                break;
                            case TYPE_CARD:
                                deleteCards();
                                break;

                        }

                    }
                })
                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                });

        final AlertDialog alertDialog = builder.create();
        alertDialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialog) {
                alertDialog.getButton(AlertDialog.BUTTON_POSITIVE)
                        .setTextColor(ContextCompat.getColor(getActivity(), R.color.error));
                alertDialog.getButton(AlertDialog.BUTTON_NEGATIVE)
                        .setTextColor(ContextCompat.getColor(getActivity(), R.color.colorPrimary));
            }
        });

        return alertDialog;
    }

    public void deleteQuizSet() {
        try {
            QuizSetDb.deleteCurrent(getActivity());
        } catch (PreferenceNotFound preferenceNotFound) {
            preferenceNotFound.printStackTrace();
        }

        MainActivity mainActivity = (MainActivity) getActivity();
        try {
            mainActivity.setTitle(QuizSetDb.getCurrentName(mainActivity));
        } catch (PreferenceNotFound preferenceNotFound) {
            if (D) Log.d(TAG, preferenceNotFound.getMessage());
        }
        mainActivity.restartLoaders();
    }

    public void deleteQuiz() {
        QuizDb.delete(getActivity(), mRowId);

        getActivity().onBackPressed();
    }

    public void deleteCards() {
        List<Long> cardIds = new ArrayList<>();
        for (int i = 0; i < mCardIds.length; i++) {
            cardIds.add(mCardIds[i]);
        }

        CardDb.delete(getActivity(), cardIds);
    }
}
