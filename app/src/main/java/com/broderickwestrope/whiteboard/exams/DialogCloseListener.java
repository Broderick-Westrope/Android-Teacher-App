package com.broderickwestrope.whiteboard.exams;

import android.content.DialogInterface;

public interface DialogCloseListener {
    //Performed every time a dialogue is closed. We use this to refresh when the exam editor cancels or saves
    void handleDialogClose(DialogInterface dialog);
}
