package com.broderickwestrope.whiteboard.student_records;

import static android.app.Activity.RESULT_CANCELED;
import static android.app.Activity.RESULT_OK;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.broderickwestrope.whiteboard.R;
import com.broderickwestrope.whiteboard.student_records.Models.RecordModel;
import com.broderickwestrope.whiteboard.student_records.Utils.RecordDBManager;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

// This provides the interface on the lower-portion of the screen to create and edit the contents of records
public class RecordEditor extends BottomSheetDialogFragment {

    // Store the type of dialogue for easy access outside of the class
    public static final String TAG = "ActionBottomDialog";

    // Our database manager for the records (using SQLite)
    private RecordDBManager db;

    // The Bitmap containing the students image
    private byte[] studentImage;

    // Holds the instance of the activity
    private Activity activity;

    // Views within our fragment:
    private TextView changeTitleTxt; // The title either reading "Edit Record" or "New Record"
    private EditText editRecord_StudentID; // The field for the record student ID input
    private EditText editRecord_Name; // The field for the record name input
    private Spinner editRecord_Gender; // The field for the record gender input
    private EditText editRecord_Course; // The field for the record course input
    private EditText editRecord_Age; // The field for the record age input
    private EditText editRecord_Address; // The field for the record address input
    private ImageView editRecord_studentImage; // The image of the student (also acts as a button for selecting a new image)
    private Button saveRecordBtn; // The button to save the changes to the record

    // Class constructor
    public RecordEditor(Activity activity) {
        this.activity = activity;
    }

    // Add our custom style to the on create method
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(STYLE_NORMAL, R.style.EditRecordStyle);
    }

    // Set soft input mode as this allows us to move the items up when the keyboard opens up (when editing the record details)
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.edit_record, container, false);
        Objects.requireNonNull(getDialog()).getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Collect the references to the views
        changeTitleTxt = requireView().findViewById(R.id.editRecord_Title);
        editRecord_StudentID = requireView().findViewById(R.id.editRecord_StudentID);
        editRecord_Name = requireView().findViewById(R.id.editRecord_Name);
        editRecord_Gender = requireView().findViewById(R.id.editRecord_Gender);
        editRecord_Course = requireView().findViewById(R.id.editRecord_Course);
        editRecord_Age = requireView().findViewById(R.id.editRecord_Age);
        editRecord_Address = requireView().findViewById(R.id.editRecord_Address);
        editRecord_studentImage = requireView().findViewById(R.id.editRecord_Image);
        saveRecordBtn = requireView().findViewById(R.id.saveRecordBtn);

        // Used to differentiate between when we are trying to create a new record or update an existing record
        boolean isUpdate = false;

        // The bundle lets us get any data passed to this fragment
        final Bundle bundle = getArguments();
        if (bundle != null) // If our bundle isn't empty (ie. we were passed data)
        {
            // If we were passed data then that means the user has selected a record from the list to edit
            isUpdate = true; // This means we are updating an existing record
            int id = bundle.getInt("id"); // Get the record text
            String name = bundle.getString("name"); // Get the record text
            String gender = bundle.getString("gender"); // Get the record text
            String course = bundle.getString("course"); // Get the record text
            int age = bundle.getInt("age"); // Get the record text
            String address = bundle.getString("address"); // Get the record text
            studentImage = bundle.getByteArray("image");


            changeTitleTxt.setText("Edit Record"); // Display that the user is editing an existing record
            editRecord_StudentID.setText(String.valueOf(id)); // Display the existing ID in the input field
            editRecord_Name.setText(name); // Display the existing name in the input field
            editRecord_Gender.setSelection((gender.equals("Male")) ? 0 : ((gender.equals("Female")) ? 1 : 2)); // Display the existing selection of gender
            editRecord_Course.setText(course); // Display the existing course in the input field
            editRecord_Age.setText(String.valueOf(age)); // Display the existing age in the input field
            editRecord_Address.setText(address); // Display the existing address in the input field
            if (studentImage != null)
                editRecord_studentImage.setImageBitmap(byteArrayToBitmap(studentImage));
        } else { // Else, if weren't passed any data, then we are creating a new record
            changeTitleTxt.setText("New Record"); // Display that the user is creating a new record
        }

        db = new RecordDBManager(getActivity()); // Create a new database handler
        db.openDatabase(); // Open the database for use

        // Listen for clicks on the save button
        boolean finalIsUpdate = isUpdate; // Copy of our update value so we can use it safely within the onClick method
        saveRecordBtn.setOnClickListener(v -> {
            if (!canSaveRecord(editRecord_StudentID, editRecord_Name, editRecord_Course, editRecord_Age, editRecord_Address))
                return;

            // Get the corresponding values from each of the input views
            int studentID = Integer.parseInt(editRecord_StudentID.getText().toString()); // Get the int of the student id
            String name = editRecord_Name.getText().toString(); // Get the student id
            String gender = editRecord_Gender.getSelectedItem().toString(); // Get the gender
            String course = editRecord_Course.getText().toString(); // Get the course
            int age = Integer.parseInt(editRecord_Age.getText().toString()); // Get the age
            String address = editRecord_Address.getText().toString(); // Get the address

            if (finalIsUpdate && bundle.getInt("id") == studentID) { // If we are updating an existing record and the student ID hasn't been changed
                db.updateRecord(bundle.getInt("id"), name, gender, course, age, address); // Update the elements of the record
            } else { // Else, if we are adding a new record
                RecordModel record = new RecordModel(); // Create a new record
                // Set the values of the new record
                record.setId(studentID);
                record.setName(name);
                record.setGender(gender);
                record.setCourse(course);
                record.setAge(age);
                record.setAddress(address);
                record.setImage(studentImage);

                db.insertRecord(record); // Insert the record to the database

                // If we reach here and we are updating, it means that the user changed the student ID, so we made a new entry and we now have to delete the old entry
                if (finalIsUpdate) {
                    db.deleteRecord(bundle.getInt("id"));
                }
            }
            dismiss(); // Dismiss the record editor fragment (this)
        });

        // When the user clicks the image we want to let them pick a new image if we have the permissions
        editRecord_studentImage.setOnClickListener(v -> {
            if (checkImagePermissions(activity)) {
                selectImage(getContext());
            }
        });
    }

    // Checks if we have the required permissions for using the gallery and camera, and if not then it attempts to get them
    public boolean checkImagePermissions(final Activity context) {
        // Get the permission value of writing to external storage
        int permission_externalStorageWrite = ContextCompat.checkSelfPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        // Get the permission value of using the camera
        int permission_camera = ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA);

        // Depending on what permissions we already have, construct a list of permissions we need to get
        List<String> permissionsList = new ArrayList<>();
        if (permission_camera != PackageManager.PERMISSION_GRANTED) { // Camera Permission
            permissionsList.add(Manifest.permission.CAMERA);
        }
        if (permission_externalStorageWrite != PackageManager.PERMISSION_GRANTED) { // Write to External Storage Permission (for saving photos)
            permissionsList.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
        }

        // If we don't yet have all the permissions we need
        if (!permissionsList.isEmpty()) {
            // Request the permissions and then let the user try again to access photos
            ActivityCompat.requestPermissions(context, permissionsList.toArray(new String[permissionsList.size()]), 101);
            return false;
        }
        return true;
    }


    // Used to either take an image with the camera or select an image from the devices gallery
    private void selectImage(Context context) {
        //Create an array for the menu options
        final CharSequence[] optionsMenu = {"Take Photo", "Choose from Gallery", "Cancel"};
        // Create an alert dialogue to house these menu options
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        // Put the options in the alter dialogue and set their corresponding actions for onClick
        builder.setItems(optionsMenu, (dialogInterface, i) -> {
            if (optionsMenu[i].equals("Take Photo")) { //When the user chooses to take a photo with the camera
                // Open the camera
                Intent takePicture = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                startActivityForResult(takePicture, 0);
            } else if (optionsMenu[i].equals("Choose from Gallery")) { // When the user chooses to use an existing photo from their device
                // Open the device gallery
                Intent pickPhoto = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(pickPhoto, 1);
            } else if (optionsMenu[i].equals("Cancel")) { // When the user chooses to cancel the selection
                dialogInterface.dismiss();
            }
        });
        builder.show(); // Display the alert dialogue containing the menu options
    }

    // Called when the we request a permission from the user
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode == 101) { // 101 is our code for selecting several image permissions
            // If we have not been granted access to the users camera
            if (ContextCompat.checkSelfPermission(activity, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                // Make and display a toast saying that we need access to the camera
                Toast.makeText(getContext(), "Whiteboard Requires Access to Camara.", Toast.LENGTH_SHORT).show();
            }
            // If we have not been granted access to write to the external storage (for saving taken images)
            else if (ContextCompat.checkSelfPermission(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                // Make and display a toast saying that we need access to write to the external storage
                Toast.makeText(getContext(), "Whiteboard Requires Access to Your Storage.", Toast.LENGTH_SHORT).show();
            } else {
                // If we have not been denied any of the required permissions, we can instead continue to select an image
                selectImage(activity);
            }
        }
    }

    // Called when we get the result of an activity (but obviously only those activities that give results, ie. image selection)
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode != RESULT_CANCELED) { // If the result wasn't a cancelled operation
            switch (requestCode) {
                // If the request code was 0 (got the photo from the camera)
                case 0:
                    // If the result is success and there was some data returned
                    if (resultCode == RESULT_OK && data != null) {
                        Bitmap bitmap = (Bitmap) data.getExtras().get("data");
                        editRecord_studentImage.setImageBitmap(bitmap);
                        studentImage = bitmapToByteArray(bitmap);
                    }
                    break;
                // If the request code was 1 (got the photo from the gallery)
                case 1:
                    // If the result is success and there was some data returned
                    if (resultCode == RESULT_OK && data != null) {
                        Uri selectedImage = data.getData();
                        String[] filePathColumn = {MediaStore.Images.Media.DATA};
                        if (selectedImage != null) {
                            Cursor cursor = requireContext().getContentResolver().query(selectedImage, filePathColumn, null, null, null);
                            if (cursor != null) {
                                cursor.moveToFirst();

                                int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
                                String picturePath = cursor.getString(columnIndex);
                                Bitmap bitmap = BitmapFactory.decodeFile(picturePath);
                                editRecord_studentImage.setImageBitmap(bitmap);
                                studentImage = bitmapToByteArray(bitmap);
                                cursor.close();
                            }
                        }

                    }
                    break;
            }
        }
    }

    private Bitmap byteArrayToBitmap(byte[] data) {
        return BitmapFactory.decodeByteArray(data, 0, data.length);
    }

    private byte[] bitmapToByteArray(Bitmap data) {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        data.compress(Bitmap.CompressFormat.PNG, 0, outputStream);
        return outputStream.toByteArray();
    }

    public boolean canSaveRecord(TextView id, TextView name, TextView course, TextView age, TextView address) {
        if (id.getText().toString().isEmpty() || name.getText().toString().isEmpty() || course.getText().toString().isEmpty() || age.getText().toString().isEmpty() || address.getText().toString().isEmpty())
            return false;
        else
            return true;
    }

    // Allows us to refresh/update the recyclerview of our records each time the panel is dismissed (when the record is saved)
    @Override
    public void onDismiss(@NonNull DialogInterface dialog) {
        Activity activity = getActivity(); // Get a reference to the current activity
        if (activity instanceof DialogCloseListener) { // If this activity is an instance of our custom listener
            ((DialogCloseListener) activity).handleDialogClose(dialog); // Handle the close
        }
    }
}
