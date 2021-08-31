package com.broderickwestrope.whiteboard.todolist;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;

import com.broderickwestrope.whiteboard.R;
import com.broderickwestrope.whiteboard.todolist.Adapters.ToDoAdapter;

// THis is a touch helper, allowing us to have swipe actions for our tasks (such as edit and delete)
public class TaskTouchHelper extends ItemTouchHelper.SimpleCallback {
    private ToDoAdapter adapter; // The adapter for the recycler view

    // The class constructor
    public TaskTouchHelper(ToDoAdapter adapter) {
        // Set the two swipe-types we want to use (left and right
        super(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT);
        this.adapter = adapter; //Set the local adapter to the given adapter (this will be the adapter made by the TodoActivity)
    }

    // We do not want to use "moves" so we always return false (we use swipes)
    @Override
    public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
        return false;
    }

    // Whenever an element is "swiped" this function will run
    @Override
    public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
        // Get the index of the task that was swiped
        final int index = viewHolder.getAbsoluteAdapterPosition();

        // If the task was swiped left
        if (direction == ItemTouchHelper.LEFT) {
            // Create an alert builder for our warning message
            AlertDialog.Builder builder = new AlertDialog.Builder(adapter.getContext());
            builder.setTitle("Delete Task"); // Set the title of the alert
            builder.setMessage("Are you sure you want to delete this task?"); // Set the contents of the alert

            // Set the positive button
            builder.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    //Continue to delete the item
                    adapter.deleteItem(index);
                }
            });

            //Set the negative button
            builder.setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    // Cancel the deletion and reset the swiped task to the center
                    adapter.notifyItemChanged(index);
                }
            });
            AlertDialog dialog = builder.create();
            dialog.show();
        } else { // Else, if the task was swiped right
            // Call the function for editing the task
            adapter.editItem(index);
        }
    }

    // Used to handle all of the graphics for the swipes
    @Override
    public void onChildDraw(@NonNull Canvas c, @NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, float dX, float dY, int actionState, boolean isCurrentlyActive) {
        super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);

        Drawable icon; // The icon for the action (ie. bin for delete)
        ColorDrawable background; // The colored background of the swipe

        View itemView = viewHolder.itemView; // The view of the task (this allows us to match its dimensions)
        int backgroundCornerOffset = 20;

        // Depending on the value along the X axis, we set a different background color
        if (dX > 0) {
            // When X is greater-than 0 it means we have swiped to the right and are trying to edit the task
            // For this we use the edit symbol and a turquoise color
            icon = ContextCompat.getDrawable(adapter.getContext(), R.drawable.ic_edit);
            background = new ColorDrawable(ContextCompat.getColor(adapter.getContext(), R.color.turquoise_blue));
        } else {
            // When X is less-than 0 it means we have swiped to the left and are trying to delete the task
            // For this we use the bin symbol and a red color
            icon = ContextCompat.getDrawable(adapter.getContext(), R.drawable.ic_delete);
            background = new ColorDrawable(Color.RED);
        }

        assert icon != null; // Ensure the icon has been set (we must have set it one of the above if-statements). This is a simple bug check to minimise crashes

        // Set the dimensions and margins of the icon, for correct positioning
        int iconMargin = (itemView.getHeight() - icon.getIntrinsicHeight()) / 2;
        int iconTop = itemView.getTop() + (itemView.getHeight() - icon.getIntrinsicHeight()) / 2;
        int iconBottom = iconTop + icon.getIntrinsicHeight();

        // Depending on the value along the X axis, we position the icon differently within the background
        if (dX > 0) { // Swiping to the right
            int iconLeft = itemView.getLeft() + iconMargin;
            int iconRight = itemView.getLeft() + iconMargin + icon.getIntrinsicWidth();
            icon.setBounds(iconLeft, iconTop, iconRight, iconBottom);

            background.setBounds(itemView.getLeft(), itemView.getTop(),
                    itemView.getLeft() + ((int) dX) + backgroundCornerOffset, itemView.getBottom());
        } else if (dX < 0) { // Swiping to the left
            int iconLeft = itemView.getRight() - iconMargin - icon.getIntrinsicWidth();
            int iconRight = itemView.getRight() - iconMargin;
            icon.setBounds(iconLeft, iconTop, iconRight, iconBottom);

            background.setBounds(itemView.getRight() + ((int) dX) - backgroundCornerOffset,
                    itemView.getTop(), itemView.getRight(), itemView.getBottom());
        } else { // When the view is "un-swiped"
            background.setBounds(0, 0, 0, 0);
        }

        // Draw the background and icon elements
        background.draw(c);
        icon.draw(c);
    }
}