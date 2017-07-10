package com.galadar.example.stockxchange;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

/*
This is the activity used for selling shares. The player decides the amount to sell by using 6 buttons, +1, +10, +100 and -1, -10, -100.
Two extra buttons set the amount to max and 0 respectively.

The amount cannot be less than 0 (obviously, shares are bought elsewhere), and more than they own.

Short selling is only available on higher levels. If available, and if the player does not own shares, a numeric textbox is added for
the player to input the number of days until the sale is settled

Buttons that should not be used become inactive. When done, the player clicks the Sell button which sends an intent broadcast (locally)
to a receiver in Main that handles the transaction, and finishes the activity to return to Main.

The activity also updates prices automatically when a new price is generated from main.
 */

public class SellActivity extends AppCompatActivity {

    static int price;           //the current share price
    static int amount;          //The amount of the shares to buy
    static int total;           //the total price (amount * price)
    static int SID;             //the share id the system uses to differentiate the share
    static long money;          //the amount of cash the player has
    static int owned;           //the amount of shares of this company the player owns
    static int level;           //the level of the player (higher levels allow short selling)
    static int assets;          //the full assets the player has
    static String Sname;        //The name of the share
    static Daytime time;        //Object for storing time data (term, day, hour, minutes)
    static boolean playSound;   //Play sound? Mostly used in menu for ticking the option
    String zerodigit;           //Temp variable for printing the extra 0 if required ($5.0 -> $5.00)


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sell);


        Intent intent = getIntent();
        Bundle data = intent.getExtras();
        SID = data.getInt("SID");
        time = MainActivity.getClock();
        playSound = data.getBoolean("playSound");
        Sname = data.getString("Sname");

        price = data.getInt("Sprice");
        owned = data.getInt("owned");
        money = data.getLong("Pmoney");
        level = data.getInt("level");
        assets = data.getInt("assets");
        amount = 0;

        String title = getString(R.string.title_activity_sell) + " " + Sname + " " + getString(R.string.shares);
        this.setTitle(title);

        TextView topBarPlayer = (TextView)findViewById(R.id.PlayerDataInfo);
        TextView topBarDaytime = (TextView)findViewById(R.id.DaytimeInfo);
        UpdateTopBar(topBarPlayer, topBarDaytime);

        final EditText Days = (EditText)findViewById(R.id.SettleDaysEditText);

        if(owned==0){
            TextView view1 = (TextView)findViewById(R.id.ShortSettleLbl);
            view1.setVisibility(TextView.VISIBLE);
            TextView view2 = (TextView)findViewById(R.id.ShortSellWarning);
            view2.setVisibility(TextView.VISIBLE);
            Days.setVisibility(EditText.VISIBLE);
            Days.setText(Integer.toString(1));
            Days.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) { }

                @Override
                public void afterTextChanged(Editable s) {
                    if(Days.getText().toString().length() > 0) {
                        if (Integer.parseInt(Days.getText().toString()) <= 0)
                            Days.setText(Integer.toString(1));
                    }
                }
            });
        }

        TextView ShareName = (TextView)findViewById(R.id.ShareNameDt);
        ShareName.setText(Sname);
        TextView SharePrice = (TextView)findViewById(R.id.ShareCurrPriDt);
        if(price%10==0)zerodigit="0";
        else zerodigit = "";
        SharePrice.setText("$"+Double.toString(((double)price)/100)+zerodigit);
        TextView SharesOwned = (TextView)findViewById(R.id.SharesOwnedDt);
        SharesOwned.setText(Integer.toString(owned));

        final TextView ShareAmount = (TextView)findViewById(R.id.ShareAmountDt);
        final TextView Cost = (TextView)findViewById(R.id.TotalValueDt);
        ShareAmount.setText(Integer.toString(amount));
        total = amount*price;
        if(total%10==0)zerodigit="0";
        else zerodigit = "";
        Cost.setText("$"+Double.toString(((double)total)/100)+zerodigit);

        final Button maxButton = (Button)findViewById(R.id.MaxSharesButton);
        final Button plusOne = (Button)findViewById(R.id.AddSharesButton);
        final Button plus10 = (Button)findViewById(R.id.Add10SharesButton);
        final Button plus100 = (Button)findViewById(R.id.Add100SharesButton);
        final Button minusOne = (Button)findViewById(R.id.RemSharesButton);
        final Button minus10 = (Button)findViewById(R.id.Rem10SharesButton);
        final Button minus100 = (Button)findViewById(R.id.Rem100SharesButton);
        final Button resetAll = (Button)findViewById(R.id.ZeroSharesButton);

        if(owned==0){
            maxButton.setEnabled(false);
        } else {
            maxButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    amount = owned;
                    ShareAmount.setText(Integer.toString(amount));
                    total = amount * price;
                    if(total%10==0)zerodigit="0";
                    else zerodigit = "";
                    Cost.setText("$"+Double.toString(((double)total)/100)+zerodigit);
                    }
                }
            );
        }

        plusOne.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(owned==0){
                    amount++;
                    ShareAmount.setText(Integer.toString(amount));
                    total = amount * price;
                    if(total%10==0)zerodigit="0";
                    else zerodigit = "";
                    Cost.setText("$"+Double.toString(((double)total)/100)+zerodigit);
                } else if(amount<owned) {
                    amount++;
                    ShareAmount.setText(Integer.toString(amount));
                    total = amount * price;
                    if(total%10==0)zerodigit="0";
                    else zerodigit = "";
                    Cost.setText("$"+Double.toString(((double)total)/100)+zerodigit);
                } else {
                    Toast.makeText(getApplicationContext(), "You cannot sell more shares than those you have", Toast.LENGTH_SHORT).show();
                }
            }
        });

        plus10.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(owned==0){
                    amount+=10;
                    ShareAmount.setText(Integer.toString(amount));
                    total = amount * price;
                    if(total%10==0)zerodigit="0";
                    else zerodigit = "";
                    Cost.setText("$"+Double.toString(((double)total)/100)+zerodigit);
                } else if(amount+10<=owned) {
                    amount+=10;
                    ShareAmount.setText(Integer.toString(amount));
                    total = amount * price;
                    if(total%10==0)zerodigit="0";
                    else zerodigit = "";
                    Cost.setText("$"+Double.toString(((double)total)/100)+zerodigit);
                } else {
                    Toast.makeText(getApplicationContext(), "You cannot sell more shares than those you have", Toast.LENGTH_SHORT).show();
                    amount = owned;
                    ShareAmount.setText(Integer.toString(amount));
                    total = amount * price;
                    if(total%10==0)zerodigit="0";
                    else zerodigit = "";
                    Cost.setText("$"+Double.toString(((double)total)/100)+zerodigit);
                }
            }
        });

        plus100.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(owned==0){
                    amount+=100;
                    ShareAmount.setText(Integer.toString(amount));
                    total = amount * price;
                    if(total%10==0)zerodigit="0";
                    else zerodigit = "";
                    Cost.setText("$"+Double.toString(((double)total)/100)+zerodigit);
                } else if(amount+100<=owned) {
                    amount+=100;
                    ShareAmount.setText(Integer.toString(amount));
                    total = amount * price;
                    if(total%10==0)zerodigit="0";
                    else zerodigit = "";
                    Cost.setText("$"+Double.toString(((double)total)/100)+zerodigit);
                } else {
                    Toast.makeText(getApplicationContext(), "You cannot sell more shares than those you have", Toast.LENGTH_SHORT).show();
                    amount = owned;
                    ShareAmount.setText(Integer.toString(amount));
                    total = amount * price;
                    if(total%10==0)zerodigit="0";
                    else zerodigit = "";
                    Cost.setText("$"+Double.toString(((double)total)/100)+zerodigit);
                }
            }
        });

        minusOne.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(owned==0){
                    amount--;
                    ShareAmount.setText(Integer.toString(amount));
                    total = amount * price;
                    if(total%10==0)zerodigit="0";
                    else zerodigit = "";
                    Cost.setText("$"+Double.toString(((double)total)/100)+zerodigit);
                } else if(amount>0) {
                    amount--;
                    ShareAmount.setText(Integer.toString(amount));
                    total = amount * price;
                    if(total%10==0)zerodigit="0";
                    else zerodigit = "";
                    Cost.setText("$"+Double.toString(((double)total)/100)+zerodigit);
                } else {
                    Toast.makeText(getApplicationContext(),"You cannot sell less than 0 shares", Toast.LENGTH_SHORT).show();
                }
            }
        });

        minus10.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(owned==0){
                    amount-=10;
                    ShareAmount.setText(Integer.toString(amount));
                    total = amount * price;
                    if(total%10==0)zerodigit="0";
                    else zerodigit = "";
                    Cost.setText("$"+Double.toString(((double)total)/100)+zerodigit);
                } else if(amount-10>=0) {
                    amount-=10;
                    ShareAmount.setText(Integer.toString(amount));
                    total = amount * price;
                    if(total%10==0)zerodigit="0";
                    else zerodigit = "";
                    Cost.setText("$"+Double.toString(((double)total)/100)+zerodigit);
                } else {
                    Toast.makeText(getApplicationContext(),"You cannot sell less than 0 shares", Toast.LENGTH_SHORT).show();
                    amount=0;
                    ShareAmount.setText(Integer.toString(amount));
                    total = amount * price;
                    if(total%10==0)zerodigit="0";
                    else zerodigit = "";
                    Cost.setText("$"+Double.toString(((double)total)/100)+zerodigit);
                }
            }
        });

        minus100.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(owned==0){
                    amount-=100;
                    ShareAmount.setText(Integer.toString(amount));
                    total = amount * price;
                    if(total%10==0)zerodigit="0";
                    else zerodigit = "";
                    Cost.setText("$"+Double.toString(((double)total)/100)+zerodigit);
                } else if(amount-100>=0) {
                    amount-=100;
                    ShareAmount.setText(Integer.toString(amount));
                    total = amount * price;
                    if(total%10==0)zerodigit="0";
                    else zerodigit = "";
                    Cost.setText("$"+Double.toString(((double)total)/100)+zerodigit);
                } else {
                    Toast.makeText(getApplicationContext(),"You cannot sell less than 0 shares", Toast.LENGTH_SHORT).show();
                    amount=0;
                    ShareAmount.setText(Integer.toString(amount));
                    total = amount * price;
                    if(total%10==0)zerodigit="0";
                    else zerodigit = "";
                    Cost.setText("$"+Double.toString(((double)total)/100)+zerodigit);
                }
            }
        });

        resetAll.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                amount = 0;
                ShareAmount.setText(Integer.toString(amount));
                total = amount*price;
                if(total%10==0)zerodigit="0";
                else zerodigit = "";
                Cost.setText("$"+Double.toString(((double)total)/100)+zerodigit);
            }
        });


        final Button Execute = (Button)findViewById(R.id.SellButton);
        final Button Cancel = (Button)findViewById(R.id.CancelButton);

        Execute.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(owned==0){
                    Intent SharesSold = new Intent("SharesShortTransaction"); //To add to short sales, and them broadcast to update prices
                    Bundle Sdata = new Bundle();
                    Sdata.putInt("SID", SID);
                    Sdata.putInt("amount", (0 - amount));
                    Sdata.putInt("atPrice", price);
                    Sdata.putBoolean("ByPlayer", true);
                    Sdata.putInt("Days", Integer.parseInt( Days.getText().toString()));
                    SharesSold.putExtras(Sdata);
                    LocalBroadcastManager.getInstance(SellActivity.this).sendBroadcast(SharesSold);
                    SellActivity.this.finish();
                } else {
                    Intent SharesSold = new Intent("SharesTransaction"); //To update prices
                    Bundle Sdata = new Bundle();
                    Sdata.putInt("SID", SID);
                    Sdata.putInt("amount", (0 - amount));
                    Sdata.putInt("atPrice", price);
                    Sdata.putBoolean("ByPlayer", true);
                    SharesSold.putExtras(Sdata);
                    LocalBroadcastManager.getInstance(SellActivity.this).sendBroadcast(SharesSold);
                    SellActivity.this.finish();
                }
            }
        });

        Cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SellActivity.this.finish();
            }
        });

        LocalBroadcastManager.getInstance(this).registerReceiver(PricesUpdateMessageRec, new IntentFilter("SpecificPriceChange"));
        LocalBroadcastManager.getInstance(this).registerReceiver(new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                Execute.setEnabled(false);
                Execute.setTextColor(0xff000000);
            }
        }, new IntentFilter("DayEnded"));
        LocalBroadcastManager.getInstance(this).registerReceiver(new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                Execute.setEnabled(true);
                Execute.setTextColor(0xffffffff);
            }
        }, new IntentFilter("DayStarted"));

        LocalBroadcastManager.getInstance(this).registerReceiver(new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, final Intent intent) {
                UpdateTimeView(time);
            }
        }, new IntentFilter("TimeForwarded"));

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_nonmain, menu);
        menu.findItem(R.id.menu_sound).setChecked(playSound);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        switch (id){
            case R.id.menu_sound:
                playSound = !playSound;
                LocalBroadcastManager.getInstance(SellActivity.this).sendBroadcast(new Intent("SoundAltered").putExtra("sound", playSound));
                item.setChecked(playSound);
                break;
            case R.id.menu_backMain:
                SellActivity.this.finish();
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    public void UpdateTopBar(TextView player, TextView daytime){
        String TBPlayer = "Lvl "+level+": $"+Double.toString((double)money/100)+" ("+assets+") ";
        player.setText(TBPlayer);
        daytime.setText(time.DTtoString());
    }

    private BroadcastReceiver PricesUpdateMessageRec = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if(intent.getExtras().getInt("SID")==SID) {
                Button Execute = (Button) findViewById(R.id.SellButton);
                Execute.setEnabled(false);
                final TextView Cost = (TextView) findViewById(R.id.TotalValueDt);
                TextView SharePrice = (TextView) findViewById(R.id.ShareCurrPriDt);
                price = intent.getExtras().getInt("newPrice");
                if(price%10==0)zerodigit="0";
                else zerodigit = "";
                SharePrice.setText("$"+Double.toString(((double) price) / 100)+zerodigit);
                total = amount * price;
                if(total%10==0)zerodigit="0";
                else zerodigit = "";
                Cost.setText("$"+Double.toString(((double)total)/100)+zerodigit);
                Execute.setEnabled(true);
            }
        }
    };

    public void UpdateTimeView(Daytime time){
        TextView DT = (TextView)findViewById(R.id.DaytimeInfo);
        DT.setText(time.DTtoString());
    }

}
