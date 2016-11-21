package com.cyberiontiger.scarnesdice;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Vibrator;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageView;
import android.widget.RadioGroup;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.github.fafaldo.fabtoolbar.widget.FABToolbarLayout;

import java.util.Random;

public class IntroActivity extends AppCompatActivity {

    //Difficulty level (number of turns computer takes)
    private int compturns = 5;

    //Global UI References
    private FABToolbarLayout layout;
    private View roll, hold, reset;
    private View game_content;
    private View intro_content;
    private Boolean exit = false;
    private int LongAnimationDuration,ShortAnimationDuration;
    final Handler handler = new Handler();

    private boolean rollHoldEnabled = true;
    private boolean THE_END = false;

    //Global Game Variables
    private Random rand;
    private TextView total_scores,turn_scores,comp_scores;
    private RadioGroup radio;
    private ImageView dice_image,user_status,comp_status;
    private ScrollView scrollView;
    private int turns = 0;
    private int rolled;
    private int user_total;
    private int user_turn;
    private int comp_total;
    private int comp_turn;
    private boolean multiplayer;

    private Vibrator vibe;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_intro);

        //For button vibrations
        vibe = ( Vibrator ) getSystemService( VIBRATOR_SERVICE );

        //UI Implementation
        scrollView = (ScrollView) findViewById(R.id.scroll_view);
        game_content = findViewById(R.id.include_game);
        intro_content = findViewById(R.id.include_intro);
        radio = (RadioGroup) findViewById(R.id.radio);

        layout = (FABToolbarLayout) findViewById(R.id.fabtoolbar);
        View fab = findViewById(R.id.fabtoolbar_fab);

        scrollView.setSmoothScrollingEnabled(true);
        scrollView.setScrollbarFadingEnabled(true);
        game_content.setVisibility(View.GONE);

        //Initializing Animation Durations
        ShortAnimationDuration = getResources().getInteger(
                android.R.integer.config_shortAnimTime);
        LongAnimationDuration = getResources().getInteger(
                android.R.integer.config_longAnimTime);

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(radio.getCheckedRadioButtonId() == R.id.one){
                    multiplayer = false;
                }
                else if(radio.getCheckedRadioButtonId() == R.id.two)
                {
                    multiplayer = true;
                }
                layout.show();
                crossFade();
            }
        });

        //############Game implementation################
        //#################STARTS HERE###################

        roll = findViewById(R.id.roll);
        hold = findViewById(R.id.hold);
        reset = findViewById(R.id.reset);

        dice_image = (ImageView) findViewById(R.id.imageView);
        user_status = (ImageView) findViewById(R.id.user_status);
        comp_status = (ImageView) findViewById(R.id.comp_status);
        total_scores = (TextView) findViewById(R.id.total_scores);
        comp_scores = (TextView) findViewById(R.id.comp_scores);
        turn_scores = (TextView) findViewById(R.id.turn_scores);

        //Initializing by resetting
        reset();

        roll.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(rollHoldEnabled) {
                    user_status.setImageDrawable(getResources().getDrawable(R.drawable.ic_action_start));
                    int temp = rollDice();
                    diceImageChanger(temp);
                    if (temp != 1) {
                        vibe.vibrate(15);
                        user_turn += temp;
                        updateScores();
                    }
                    else
                    {
                        vibe.vibrate(25);
                        user_turn = 0;
                        Toast.makeText(IntroActivity.this, "You rolled a 1!", Toast.LENGTH_SHORT).show();
                        user_status.setImageDrawable(getResources().getDrawable(R.drawable.ic_action_stop));
                        updateScores();
                        computerTurn();
                    }
                }
                else
                {
                    if(THE_END) {
                        Toast.makeText(IntroActivity.this, "Please reset to continue", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });

        hold.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (rollHoldEnabled) {
                    if(user_turn == 0)
                    {
                        Toast.makeText(IntroActivity.this, "Please Roll before Holding", Toast.LENGTH_SHORT).show();
                    }
                    else {
                        vibe.vibrate(20);
                        user_total += user_turn;
                        user_turn = 0;
                        diceImageChanger(1);
                        updateScores();
                        user_status.setImageDrawable(getResources().getDrawable(R.drawable.ic_action_stop));
                        checkWin();
                        if(!THE_END) {
                        computerTurn();
                        }
                    }
                }
                else
                {
                    if(THE_END) {
                        Toast.makeText(IntroActivity.this, "Please reset to continue", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });

        reset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                reset();
                vibe.vibrate(20);
                Toast.makeText(IntroActivity.this, "Reset", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void computerTurn() {
        rollHoldEnabled = false;
        turn_scores.setText("Computer is Rolling");
        comp_status.setImageDrawable(getResources().getDrawable(R.drawable.ic_action_start));
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                callComputerTurn();
            }
        }, 2000);
    }

    private void callComputerTurn(){
        while(turns<compturns) {
            turns++;
            rolled = rollDice();
            if(rolled != 1)
            {
                comp_turn+=rolled;
            }
            else
            {
                Toast.makeText(this, "Computer rolled a 1!", Toast.LENGTH_SHORT).show();
                diceImageChanger(1);
                comp_turn = 0;
                break;
            }
        }
        turns=0;
        comp_total+=comp_turn;
        updateScores();
        turn_scores.setText("Turn Score : "+comp_turn+" (Comp)");
        comp_status.setImageDrawable(getResources().getDrawable(R.drawable.ic_action_stop));
        comp_turn=0;
        rollHoldEnabled = true;
        checkWin();
    }

    private void checkWin(){
        if(user_total>=100)
        {
            Toast.makeText(this, "You Win!", Toast.LENGTH_SHORT).show();
            vibe.vibrate(200);
            rollHoldEnabled = false;
            THE_END = true;
            comp_status.setImageDrawable(getResources().getDrawable(R.drawable.ic_action_start));
            user_status.setImageDrawable(getResources().getDrawable(R.drawable.ic_action_start));
        }
        if(comp_total>=100)
        {
            Toast.makeText(this, "Computer Wins", Toast.LENGTH_SHORT).show();
            vibe.vibrate(200);
            rollHoldEnabled = false;
            THE_END = true;
            comp_status.setImageDrawable(getResources().getDrawable(R.drawable.ic_action_start));
            user_status.setImageDrawable(getResources().getDrawable(R.drawable.ic_action_start));
        }
    }

    //Random Number generator function
    private int rollDice() {
        rand = new Random();
        return (rand.nextInt(6) + 1);
    }

    private void updateScores(){
        total_scores.setText("Player : "+user_total);
        comp_scores.setText("Comp : "+comp_total);
        turn_scores.setText("Turn Score : "+user_turn);
    }

    private void diceImageChanger(int i) {
        switch(i)
        {
            case 1:
                dice_image.setImageDrawable(getResources().getDrawable(R.drawable.dice1));
                break;
            case 2:
                dice_image.setImageDrawable(getResources().getDrawable(R.drawable.dice2));
                break;
            case 3:
                dice_image.setImageDrawable(getResources().getDrawable(R.drawable.dice3));
                break;
            case 4:
                dice_image.setImageDrawable(getResources().getDrawable(R.drawable.dice4));
                break;
            case 5:
                dice_image.setImageDrawable(getResources().getDrawable(R.drawable.dice5));
                break;
            case 6:
                dice_image.setImageDrawable(getResources().getDrawable(R.drawable.dice6));
                break;
        }
    }

    private void reset() {
        user_total = 0;
        user_turn = 0;
        comp_total = 0;
        comp_turn = 0;
        total_scores.setText("Player : 0");
        comp_scores.setText("Comp : 0");
        turn_scores.setText("Press \"Play\" to start");
        dice_image.setImageDrawable(getResources().getDrawable(R.drawable.dice1));
        comp_status.setImageDrawable(getResources().getDrawable(R.drawable.ic_action_stop));
        user_status.setImageDrawable(getResources().getDrawable(R.drawable.ic_action_stop));
        turns = 0;
        rollHoldEnabled = true;
        THE_END = false;
    }



    @Override
    public void onBackPressed() {
        if (exit) {
            finish(); // finish activity
        } else {
            Toast.makeText(this, "Press Back again to Exit.",
                    Toast.LENGTH_SHORT).show();
            exit = true;
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    exit = false;
                }
            }, 3000);

        }
    }

    private void crossFade() {

        game_content.setAlpha(0f);
        game_content.setVisibility(View.VISIBLE);
        // Animate the content view to 100% opacity, and clear any animation
        // listener set on the view.
        game_content.animate()
                .alpha(1f)
                .setDuration(LongAnimationDuration)
                .setListener(null);

        // Animate the loading view to 0% opacity. After the animation ends,
        // set its visibility to GONE as an optimization step (it won't
        // participate in layout passes, etc.)
        intro_content.animate()
                .alpha(0f)
                .setDuration(ShortAnimationDuration)
                .setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        intro_content.setVisibility(View.GONE);
                    }
                });
    }

}