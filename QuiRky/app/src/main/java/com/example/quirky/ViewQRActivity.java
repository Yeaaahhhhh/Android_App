package com.example.quirky;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import java.util.ArrayList;
import java.util.List;

/**
 * Activity to view a QRCode. Holds two fragments: one fragment to see the QRCode location, & start up the comments activity
 * Another fragment to see all other players who have scanned the QRCode
 * */
public class ViewQRActivity extends AppCompatActivity implements ViewQRFragmentListener {

    private final String TAG = "ViewQRActivity says";
    ImageView image;
    TextView scoreText;
    Fragment buttonsFrag, playersFrag;
    Bitmap photo;

    DatabaseController dc;
    ArrayList<String> players;

    Intent i;
    QRCode qr;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_qr);

        dc = new DatabaseController();

        i = getIntent();
        qr = i.getParcelableExtra("qr");
        if(qr == null)
            ExitWithError();

        image = findViewById(R.id.imageView2);
        scoreText = findViewById(R.id.text_showScore);

        buttonsFrag = new ViewQRButtonsFragment();
        playersFrag = new ViewQRScannersFragment();

        scoreText.setText(String.valueOf(qr.getScore()));

        photo = BitmapFactory.decodeResource(getResources(), R.drawable.temp);
        image.setImageBitmap(photo);

        players = qr.getScanners();
        changeFragment(buttonsFrag);
    }

    /**
     * Changes the fragment in the FrameLayout
     * @param frag The fragment to place in the layout
     */
    @Override
    public void changeFragment(Fragment frag) {
        FragmentManager fm = getSupportFragmentManager();
        FragmentTransaction ft = fm.beginTransaction();
        ft.replace(R.id.view_qr_frame, frag);
        ft.commit();
    }

    /**
     * Returns the list of players to the calling fragment
     * @return All other players who have scanned the same QRCode
     */
    @Override
    public ArrayList<String> getPlayers() {
        Log.d(TAG, "players is:\t" + players);
        return players;
    }

    /**
     * Opens the Comment Activity. This will be called when the user clicks the Comments button in the fragment
     */
    @Override
    public void commentsButton() {
        Intent intent = new Intent(this, ViewCommentsActivity.class);
        intent.putExtra("qr", qr);
        startActivity(intent);
    }

    /**
     * Deletes the QRCode from the list of codes the user has scanned. Exits the activity.
     */
    @Override
    public void deleteButton() {
        MemoryController mc = new MemoryController(this);
        Profile p = mc.read();

        if(p.removeScanned(qr.getId())) {
            mc.write(p);
            dc.writeProfile(p);

            qr.removeScanner(p.getUname());
            dc.writeQRCode(qr);

            Toast.makeText(this, "Removed from your scanned codes!", Toast.LENGTH_SHORT).show();

            Intent i = new Intent(this, StartingPageActivity.class);
            startActivity(i);
        } else {
            Toast.makeText(this, "You did not have that code anyways!", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void viewProfile(String username) {
        ListeningList<Profile> readResult = new ListeningList<>();
        readResult.setOnAddListener(new OnAddListener<Profile>() {
            @Override
            public void onAdd(ListeningList<Profile> listeningList) {
                Profile p = listeningList.get(0);
                Intent intent = new Intent(getApplicationContext(), ProfileViewerActivity.class);
                intent.putExtra("profile", p);
                startActivity(intent);
            }
        });

        dc.readProfile(username, readResult);
    }

    /**
     * Method called when data is passed to this activity incorrectly, or when there is an issue reading the data from FireStore.
     * Makes a toast and then finishes the activity.
     */
    private void ExitWithError() {
        Toast.makeText(this, "QRCode was passed incorrectly, or not found in FireStore!", Toast.LENGTH_SHORT).show();
        finish();
    }
}