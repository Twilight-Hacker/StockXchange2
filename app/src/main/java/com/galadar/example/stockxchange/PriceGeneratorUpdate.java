package com.galadar.example.stockxchange;

import android.app.IntentService;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.content.LocalBroadcastManager;

import java.util.Random;

/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 * <p/>
 *
 * helper methods.
 */
public class PriceGeneratorUpdate extends IntentService {

    final int PRICE_UPDATE = 1;
    final int SHARE_TRANSACTION = 2;
    final int SHORT_TRANSACTION = 3;
    final int SHARE_REMAINING = 4;
    final int DOUBLE_SHARES = 5;
    final int HALF_SHARES = 6;

    boolean dayOpen;
    Message message;
    Finance f = MainActivity.getFinance();
    Handler handler = new Handler(getMainLooper());


    /**
     * Starts this service to perform action Foo with the given parameters. If
     * the service is already performing a task this action will be queued.
     *
     * @see IntentService
     */
    // TODO: Customize helper method
    private void callforTransactions() {
        if(!dayOpen) return;
        int temp;
        Random random = new Random();

        for(int i=0;i<f.getNumComp();i++) {
            if (f.getCompCurrValue(i) <= 0){ //If Company bankrupt, sell all shares.
                if(f.getShareCurrPrince(i)>100){
                    Intent intent = new Intent("SharesTransaction");
                    Bundle data = new Bundle();
                    data.putInt("SID", i);
                    data.putInt("amount", f.getTotalShares(i)/5);
                    data.putInt("atPrice", f.getShareCurrPrince(i));
                    data.putBoolean("ByPlayer", false);
                    intent.putExtras(data);
                    LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
                }
            } else {
                temp = getSharesAmount(random, f.getAvg(i), f.getCap(i), f.getTotalShares(i));
                if (temp != 0) {
                    Intent intent = new Intent("SharesTransaction");
                    Bundle data = new Bundle();
                    data.putInt("SID", i);
                    data.putInt("amount", temp);
                    data.putInt("atPrice", f.getShareCurrPrince(i));
                    data.putBoolean("ByPlayer", false);
                    intent.putExtras(data);
                    LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
                }
            }
        }
    }

    public int getSharesAmount(Random random, double Avg, double Cap, int total) {
        double determinant = getDeterminant(Avg, Cap);

        determinant += random.nextDouble() * 2 - 1;
        double am = Math.min(Math.abs(random.nextGaussian()), 3) * determinant * 100;
        if (Math.abs(am) <= 20) am = Math.signum(determinant) * random.nextInt(50) + 20;
        if (Math.abs(am) > 0.1 * total) am = Math.signum(am) * 0.1 * total;
        return (int) Math.round(am);

    }

    public double getDeterminant(double Avg, double Cap){
        return Avg/Cap - 1;
    }

    private BroadcastReceiver SharesTransactionedRec = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Bundle data = intent.getExtras();
            int SID = data.getInt("SID");
            int amount = data.getInt("amount");
            int oldPrice = data.getInt("atPrice");
            boolean byPlayer = data.getBoolean("ByPlayer");

            if(byPlayer){
                message = new Message();
                message.arg1 = SID;
                message.arg2 = amount;
                message.what = SHARE_TRANSACTION;
                handler.sendMessage(message);
                //handler for this will take care of the remaining shares AND player money
            } else {
                message = new Message();
                message.arg1 = SID;
                message.arg2 = amount;
                message.what = SHARE_REMAINING;
                handler.sendMessage(message);
                //obviously, this handler will, also
            }

            double cap = (2*f.getCap(SID) + f.getAvg(SID))/3;

            int newPrice;

            double dnewPrice = getNewPrice(cap, (double)f.getRemShares(SID)/f.getTotalShares(SID)) + Math.random()*35;
            dnewPrice = ((double)oldPrice+dnewPrice)/2;

/*
            TODO relegate control for this in MainActivity handler on PRICE_UPDATE message received to change remaining and total shares and then fire specific SID change request, ignoring the previous message
            if(dnewPrice>35000){
                message = new Message();
                message.arg1=SID;
                message.what=DOUBLE_SHARES;
                handler.sendMessage(message);
                //don't forget to launch news
                cap = (2*f.getCap(SID) + f.getAvg(SID))/3;
                dnewPrice = getNewPrice(cap, (double)f.getRemShares(SID)/f.getTotalShares(SID)) + Math.random()*35;
            } else if(dnewPrice<1500){
                message = new Message();
                message.arg1=SID;
                message.what=HALF_SHARES;
                handler.sendMessage(message);
                //don't forget to launch news
                cap = (2*f.getCap(SID) + f.getAvg(SID))/3;
                dnewPrice = getNewPrice(cap, (double)f.getRemShares(SID)/f.getTotalShares(SID)) + Math.random()*35;
            }
*/

            newPrice = (int)Math.round(dnewPrice);

            if(newPrice<=50) {
                newPrice=50+(int)Math.round(Math.random() * 30);   //No price less than $0.50
            }

            Message message = new Message();
            message.arg1=SID;
            message.arg2=newPrice;
            message.what = PRICE_UPDATE;
            handler.sendMessage(message);

        }
    };

    private double getNewPrice(double cap, double x){
        //TODO coordinate this to get data from MainActivity Finance
        x = 0.5-x;                          //To center at 0, reversing sign
        return 0.5 * cap + 1.5 * cap / (1 + Math.exp(x));
    }

    private BroadcastReceiver SharesShortTransactionedRec = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Bundle data = intent.getExtras();
            int SID = data.getInt("SID");
            int amount = data.getInt("amount");
            int oldPrice = data.getInt("atPrice");
            int days = data.getInt("Days");

            f.ShortShare(SID, amount, days);
            p.alterMoney(amount * oldPrice);
            if(fullGame)DBHandler.ShortShare(SID, amount, time.totalDays(days), p.getMoney());

            amount = Math.round(amount/2);
            f.alterRemShares(SID, amount);
            int remaining = f.getRemShares(SID);
            if(fullGame)DBHandler.setRemShares(SID, remaining);

            UpdateCommandsUI();

            int newPrice;
            double cap = (2*f.getCap(SID) + f.getAvg(SID))/3;
            double dnewPrice = getNewPrice(cap, (double)f.getRemShares(SID)/f.getTotalShares(SID)) + Math.random()*35;


            if(dnewPrice>65000){
                f.doubleShares(SID);
                cap = (2*f.getCap(SID) + f.getAvg(SID))/3;
                if(fullGame)DBHandler.setCompTotalShares(SID, f.getTotalShares(SID));
                editNews(25, getString(R.string.NewsDoubleShares, f.getName(SID)), getString(R.string.NewsDoubleSharesBody, f.getName(SID)));
                dnewPrice = getNewPrice(cap, (double)f.getRemShares(SID)/f.getTotalShares(SID)) + Math.random()*35;
            } else if(dnewPrice<1800){
                f.halfShares(SID);
                cap = (2*f.getCap(SID) + f.getAvg(SID))/3;
                if(fullGame)DBHandler.setCompTotalShares(SID, f.getTotalShares(SID));
                editNews(25, getString(R.string.NewsHalfShares, f.getName(SID)), getString(R.string.NewsHalfSharesBody, f.getName(SID)));
                dnewPrice = getNewPrice(cap, (double)f.getRemShares(SID)/f.getTotalShares(SID)) + Math.random()*35;
            }

            newPrice = (int)Math.round(dnewPrice);

            if(newPrice<=50) {
                newPrice=50+(int)Math.round(Math.random() * 30);   //No price less than $0.50
            }


            f.setShareCurrPrice(SID, newPrice);
            if(fullGame)DBHandler.setDBCurrPrice(SID, newPrice);

            Intent intent1 = new Intent("SpecificPriceChange");
            Bundle data1 = new Bundle();
            data1.putInt("SID", SID);
            data1.putInt("newPrice", newPrice);
            data1.putInt("oldPrice", oldPrice);
            data1.putBoolean("PlayerOwner", f.getSharesOwned(SID)>0);
            intent1.putExtras(data1);
            LocalBroadcastManager.getInstance(MainActivity.this).sendBroadcast(intent1);
        }
    };


    /**
     * Starts this service to perform action Baz with the given parameters. If
     * the service is already performing a task this action will be queued.
     *
     * @see IntentService
     */

    public PriceGeneratorUpdate() {
        super("PriceGeneratorUpdate");
    }

    @Override
    protected void onHandleIntent(Intent intent) {

    }


}
