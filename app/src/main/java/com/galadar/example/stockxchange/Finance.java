package com.galadar.example.stockxchange;

import com.galadar.example.stockxchange.Company.Sectors;

import java.util.HashSet;
import java.util.Random;

/**
 * Created by Galadar on 29/9/2015.
 * Finance Object
 *
 * This is a wrapper object that holds all financial data for the game. This is the "RAM memory" on which the game runs.
 *
 * There are 3 constructors. One for Starting a new Quick game, one for Loading a game from DB and one for starting a new full game
 *
 * Check each variable declaration for what it does, as well as each function to find out where it is used
 *
 * For more information, see MainActivity
 *
 * Companies, shares, Scams, Shorts and all related tables share the same index in their respective arrays.
 * This is denoted as sid or cid.
 */

class Finance {

    private double[][] outlooks;            //This holds the economy and sector outlooks
    private long size1, size2;              //The two sizes of the economy (see getter functions)
    private HashSet<String> CompaniesNames; //Hash set for making sure no two companies have the same name
    private HashSet<String> Scams;          //Hash set for making sure a share is not part of multiple scams
    private int[][] ScamResolution;         //Array for storing scam type and days left to execute scam for every share
    private String[] Names;                 //Array with all company/share names
    private int[][] Shares;                 //Array holding share data for quick access
    private long[][] Companies;             //Array holding Company data for quick access
    private int[][] Short;                  //Array holding data on short sales
    private int[][] ShareDayLimits;         //For storing shares data (OPEN-HIGH-LOW-CLOSE) for the day. Update during day as
    // price changes, then record.
    private Company company;                //General object for temporary company creation
    private Share share;                    //General object for temporary share creation
    private int numComp;                    //variable for the current number of companies/shares


    //CONSTRUCTORS

    //Quick game
    Finance(int size) { //Quick Game Constructor
        numComp = size*10;
        Companies = new long[numComp][8];
        Shares = new int[numComp][5];
        Names = new String[numComp];
        Short = new int[numComp][2];
        ShareDayLimits = new int[numComp][4];
        CompaniesNames = new HashSet<>();
        Scams = new HashSet<>();
        ScamResolution = new int[numComp][2];
        for(int i=0;i<numComp;i++){
            String name = randomName();
            boolean go = CompaniesNames.add(name);
            if(go) {
                company = new Company(name);
                share = new Share(name, i, company.shareStart(), company.getTotalShares());
                Names[i]=name;
                Companies[i][0] = company.getTotalValue();
                Companies[i][1] = company.getSectorInt();
                Companies[i][2] = company.getRevenue();
                Companies[i][3] = company.get10000Outlook();
                Companies[i][4] = company.getLastRevenue();
                Companies[i][5] = company.getInvestment();
                Companies[i][6] =(int)Math.round(company.getMarketShare()*1000);
                Companies[i][7] = company.getCurrentValue();
                Shares[i][0] = share.getCurrentSharePrice();
                Shares[i][1] = 0; //Amount Owned
                Shares[i][2] = company.getTotalShares();
                Shares[i][3] = share.getPrevDayClose();
                Shares[i][4] = Math.round(share.getTotalShares() / 2);
                ShareDayLimits[i][0] = -1;
                ShareDayLimits[i][1] = -1;
                ShareDayLimits[i][2] = -1;
                ShareDayLimits[i][3] = -1;
                Short[i][0] = 0; //Amount to Settle
                Short[i][1] = -1; //Remaining days
                ScamResolution[i][0]=0;
                ScamResolution[i][1]=-1;
            } else {
                i--;
            }
        }

        outlooks = new double[Sectors.values().length+1][2];

        outlooks[0][0]=0;
        outlooks[0][1]=0;

        for(int i=1;i<outlooks.length;i++){
            outlooks[i][0] = Math.random()*2-1;
            outlooks[i][1]=0;
        }

        Random random = new Random();

        int newScams = random.nextInt(10)+5;
        int sid;
        int type;
        int totalDays;
        for (int i = 0; i < newScams; i++) {
            do{
                sid=random.nextInt(getNumComp()-1);
            } while(!addScam(sid));

            double e = random.nextDouble(); //5 is current total Number of Categories, from 1 to 5, see MainActivity Scam Resolution Function for details.

            if(e<0.1) type = 1;                     //Ponzi Scheme
            else if (e<=0.3) type = 2;              //Pump&Dump
            else if (e<=0.5) type = 3;              //Short&Distort
            else if (e<0.6) type = 4;               //Empty Room
            else type =5;                           //Lawbreaker Scandal

            totalDays=random.nextInt(30)+25;
            addScamData(sid, type, totalDays);
        }

        size1 = calcEconSize1();
        size2 = calcEconSize2();

    }

    //Load game
    Finance(MemoryDB DBHandler){ //Loaded Game Constructor
        String name;
        int CurrentDay = MainActivity.getClock().totalDays();
        numComp = DBHandler.getMaxSID();
        size1 = DBHandler.getEconomySize1();
        size2 = DBHandler.getEconomySize2();
        Companies = new long[numComp][8];
        Shares = new int[numComp][5];
        Names = new String[numComp];
        Short = new int[numComp][2];
        ShareDayLimits = new int[numComp][4];
        CompaniesNames = new HashSet<>();
        Scams = new HashSet<>();
        for(int i=0; i<DBHandler.getMaxSID();i++){
            name = DBHandler.getDBShareName(i);
            boolean ok = CompaniesNames.add(name);
            while(!ok){
                String newName = randomName();
                ok = CompaniesNames.add(newName);
                if(ok) {
                    DBHandler.setDBShareName(i, newName);
                    DBHandler.setDBCompName(name, newName);
                    name = newName;
                }
            }
            Companies[i][0] = DBHandler.getCompTotalValue(name);
            Companies[i][1] = Company.getSectorInt(DBHandler.getCompanySector(name));
            Companies[i][2] = DBHandler.getCompRevenue(name);
            Companies[i][3] = DBHandler.get10000CompOutlook(name);
            Companies[i][4] = DBHandler.getLastRevenue(name);
            Companies[i][5] = DBHandler.getInvestment(name);
            Companies[i][6] = Math.round(DBHandler.getCompMarketShare(name) * 1000);
            Companies[i][7] = DBHandler.getCompCurrValue(name);
            ShareDayLimits[i][0] = -1;
            ShareDayLimits[i][1] = -1;
            ShareDayLimits[i][2] = -1;
            ShareDayLimits[i][3] = -1;
            Shares[i][0] = DBHandler.getDBCurrPrice(i);
            Shares[i][1] = DBHandler.getOwnedShare(i);
            Shares[i][2] = DBHandler.getTotalShares(i);
            Shares[i][3] = DBHandler.getDBLastClose(i);
            Shares[i][4] = DBHandler.getRemShares(i);
            Short[i][0] = DBHandler.getShortAmount(i); //Amount of Share i to settle
            int days = DBHandler.getShortDays(i)-CurrentDay;
            if(days>0) {
                Short[i][1] = days; //Remaining days until settle
            } else {
                Short[i][1] = -1;
            }
            Names[i]=name;
        }

        outlooks = new double[Sectors.values().length+1][2];

        outlooks[0][0]=DBHandler.getEconomyOutlook();
        outlooks[0][1]=0;

        for (int i = 1; i < outlooks.length ; i++) {
            outlooks[i][0] = DBHandler.getOutlook(Sectors.values()[i-1].toString());
            outlooks[i][1] = 0;
        }

        ScamResolution = new int[numComp][2];
        for (int i = 0; i < ScamResolution.length; i++) {
            if(DBHandler.isScam(i)){
                Scams.add(Names[i]);                //Add name to Hash set
                ScamResolution[i][0] = DBHandler.getScamType(i);
                ScamResolution[i][1] = DBHandler.getScamResolutionDay(i)-CurrentDay;
            } else {
                ScamResolution[i][0] = 0;   //Scam type/category 1-5, 0 for no scam
                ScamResolution[i][1] = -1;  //remaining days, -1 for no scams
            }
            if(this.ScamResolution[i][0]==2) {       //Alter Scam Share Outlook for execution of Pump&Dump Scam
                if (ScamResolution[i][1] < 6) {
                    Companies[ScamResolution[i][0]][3] += 5 * (5 - ScamResolution[i][1]);
                }
            }
            if(ScamResolution[i][0]==3) {       //Alter Scam Share Outlook for execution of Short&Distort Scam
                if (ScamResolution[i][1] < 4) {
                    Companies[ScamResolution[i][0]][3] -= 5 * (3 - ScamResolution[i][1]);
                }
            }
        }

    }

    //New game
    Finance(MemoryDB DBHandler, int size) { //New Game Constructor
        numComp = size*10;
        Companies = new long[numComp][8];
        Shares = new int[numComp][5];
        Names = new String[numComp];
        Short = new int[numComp][2];
        ShareDayLimits = new int[numComp][4];
        Scams = new HashSet<>();
        CompaniesNames = new HashSet<>();
        ScamResolution = new int[numComp][2];
        for(int i=0;i<numComp;i++){
            String name = randomName();
            boolean go = CompaniesNames.add(name);
            if(go) {
                company = new Company(name);
                share = new Share(name, i, company.shareStart(), company.getTotalShares());
                DBHandler.addCompany(company, i);
                DBHandler.addShare(share);
                Names[i]=name;
                Companies[i][0] = company.getTotalValue();
                Companies[i][1] = company.getSectorInt();
                Companies[i][2] = company.getRevenue();
                Companies[i][3] = company.get10000Outlook();
                Companies[i][4] = company.getLastRevenue();
                Companies[i][5] = company.getInvestment();
                Companies[i][6] = (int)Math.round(company.getMarketShare()*1000);
                Companies[i][7] = company.getCurrentValue();
                ShareDayLimits[i][0] = -1;
                ShareDayLimits[i][1] = -1;
                ShareDayLimits[i][2] = -1;
                ShareDayLimits[i][3] = -1;
                Shares[i][0] = share.getCurrentSharePrice();
                Shares[i][1] = 0; //Amount Owned
                Shares[i][2] = company.getTotalShares();
                Shares[i][3] = share.getPrevDayClose();
                Shares[i][4] = Math.round(share.getTotalShares() / 2);
                Short[i][0] = 0; //Amount to Settle
                Short[i][1] = -1; //Remaining days
                ScamResolution[i][0]=0;
                ScamResolution[i][1]=-1;
            } else {
                i--;
            }
        }

        outlooks = new double[Sectors.values().length+1][2];

        outlooks[0][0]=DBHandler.getEconomyOutlook();
        outlooks[0][1]=0;

        for(int i=1;i<outlooks.length;i++){
            outlooks[i][0] = Math.random()*0.5;
            DBHandler.setOutlook(Sectors.values()[i-1].toString(), outlooks[i][0]);
            outlooks[i][1]=0;
        }

        size1 = calcEconSize1();
        size2 = calcEconSize2();
        DBHandler.setEconomySize1(size1);
        DBHandler.setEconomySize2(size2);

    }



    //OTHER

    //For (re)calculating the economy size 1: The sum of the product (for each share) of the number of
    //shares times their current price
    long calcEconSize1(){ //Econ Size of shares
        long size=0;
        long am;
        for (int i = 0; i < Companies.length; i++) {
            am=getTotalShares(i)*getShareCurrPrince(i);
            size += am/1000;
        }
        return size;
    }

    //For retrieving the stored size 1 (without recalculating)
    long getEconSize1(){ //Econ Size of shares
        return this.size1;
    }

    //For (re)calculating the economy size 2 (reported size): The sum of total values of all companies
    long calcEconSize2(){ //Econ Size of Companies
        long size=0;
        for (int i = 0; i < Companies.length; i++) {
            size+=getCompTotalValue(i)/1000;
        }
        return size;
    }

    //For retrieving the stored size 2 (reported size) (without recalculating
    long getEconSize2(){ //Econ Size of Companies
        return this.size2;
    }

    //Get the full economy size (sum of size 1 and size 2)
    long getFullEconSize() {
        return getEconSize1()+getEconSize2();
    }

    //Get the sum of all shares in the economy
    int getSumShares(){
        int sum=0;
        for (int i = 0; i <getNumComp() ; i++) {
            sum += Shares[i][2];
        }
        return sum;
    }

    //Get the total number of sectors
    int getNumOfOutlooks(){
        return outlooks.length;
    }

    //Function handling the day end tasks, updating candle data and company current values
    void DayCloseShares(){
        for(int i=0;i<Shares.length;i++){
            Shares[i][3]=Shares[i][0];
            ShareDayLimits[i][3] = Shares[i][0];
            if(Short[i][1]>=0) {
                Short[i][1]--;
            }
            if(ScamResolution[i][1]>=0){
                ScamResolution[i][1]--;
            }
            updCompCurrValue(i, getCompRevenue(i));
            ResetCompRevenue(i);
        }
    }

    //Function handling the day start tasks, updating candle data
    void DayOpenShares() {
        for (int i = 0; i < Shares.length; i++) {
            ShareDayLimits[i][0] = Shares[i][0];
            if(ShareDayLimits[i][1]==-1){
                ShareDayLimits[i][1] = Shares[i][0];
                ShareDayLimits[i][2] = Shares[i][0];
                ShareDayLimits[i][3] = Shares[i][0];
            }
        }
    }

    //Get a random 4 Letter sequence to serve as a name for a company/share
    static String randomName() {

        final String alphabet = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
        final int N = alphabet.length();
        Random r = new Random();

        String value="";
        for (int i = 0; i < 4; i++) {
            value = value + (alphabet.charAt(r.nextInt(N)));
        }
        return value;
    }

    //Try to add a name in the names list to see if available for use
    boolean addCompanyName(String name){
        return CompaniesNames.add(name);
    }

    //Get the number of companies in this economy
    int getNumComp() {
        return numComp;
    }

    //Get the economy size 1 (the hidden size) for a sector
    long getSectorValue(int Sec) {
        long size=0;
        for (int i = 0; i < Companies.length; i++) {
            if(getCompSectorInt(i)==Sec){
                size+=getCompCurrValue(i);
            }
        }
        return size;
    }

    //Get the economy size 2 (the reported size) for a sector
    long getSecEconSize(int sectorI) {
        long size =0;
        for (int i = 0; i < Companies.length; i++) {
            if(Companies[i][1]==sectorI){
                size += getCompCurrValue(i);
            }
        }
        return size;
    }

    //Get the player's total shares net worth (will add bonds here later)
    long NetWorth(){
        long value = 0;
        for (int i = 0; i < Shares.length; i++) {
            value += getShareCurrPrince(i)*getSharesOwned(i);
        }
        return value;
    }

    //Get a random id for a (non-bankrupt) company
    int getRandomActiveSID() {
        int sid;
        Random random = new Random();
        do{
            sid=random.nextInt(getNumComp());
        } while(isScam(sid) | (getCompCurrValue(sid)<=0));
        return sid;
    }

    //Clean up for term end to reset company names so that they match their cid/sid numbers
    void resetAllNames(){
        Object[] NamesObj = CompaniesNames.toArray();
        Names = new String[CompaniesNames.size()];
        for (int i = 0; i < CompaniesNames.size(); i++) {
            Names[i]=NamesObj[i].toString();
        }
        numComp=Names.length;
    }

    //Get the company's name
    String getName(int id){
        return Names[id];
    }

    //add revenue for all companies
    void revenue(MainActivity.EconomyState ec) {
        double upper, lower;

        switch (ec){
            case Boom:
                upper = 7.5;
                lower = -2.5;
                break;
            case Accel:             //Increased chance of profit/ reduced chance of loss
                upper = 6.0;
                lower = -3.0;
                break;
            case Normal:            //Nearly equal economic chance of profit and loss
                upper = 5.0;
                lower = -5.0;
                break;
            case Recess:            //Decreased chance of loss/ reduced chance of profit
                upper = 3.0;
                lower = -6.0;
                break;
            default: //Assume depression (to avoid might not have been initialized error)
                upper = 2.5;
                lower = -7.5;
                break;
        }

        long marketSize= Math.round(RangedRandom(100, 500));
        for (int i = 0; i < getNumComp(); i++) { //Add revenue
            if(getCompCurrValue(i)<=0)continue; //Exclude bankrupt companies
            upper += 2*getCompOutlook(i)+getSectorOutlook(getCompSectorInt(i)+1);
            lower += 2*getCompOutlook(i)+getSectorOutlook(getCompSectorInt(i) + 1);
            UpdateCompRevenue(i, Math.round(marketSize*getMarketShare(i)*(RangedRandom(upper, lower)) ));

        }
    }

    //Get a random number between upper and lower
    private double RangedRandom(double Upper, double Lower){
        double diff = Upper-Lower;
        return Math.random()*diff + Lower;
    }

    //CAP and AVG are the final determinants for the share price. Please see Main Activity for details.
    double getCap(int sid){
        return (double)getCompCurrValue(sid)*100/getTotalShares(sid);
    }

    //CAP and AVG are the final determinants for the share price. Please see Main Activity for details.
    double getAvg(int sid){
        return (double)getCompTotalValue(sid)*100/getTotalShares(sid);
    }



    //SHORT SALES

    //For shorting a share, by id, number of shares shorted and days until the sale must be settled
    void ShortShare(int sid, int amount, int days) {
        Short[sid][0] = amount;
        Short[sid][1] = days;
    }

    //Return if the player has sold short this share
    boolean isShorted(int SID){
        return Short[SID][0]!=0;
    }

    //How many shares were shorted
    int getPosShortAmount(int SID){ return Math.abs(Short[SID][0]); }

    //How many days remain until the short sale must be settled
    int getShortRemainingDays(int SID){ return Short[SID][1]; }

    //Called to negate short after it is settled
    void clearShort(int SID){
        Short[SID][0]=0;
        Short[SID][1]=-1;
    }



    //COMPANIES

    //Get the public total value of the company
    long getCompTotalValue(int id){
        return Companies[id][0];
    }

    //Set the public total value of the company
    void setCompTotalValue(int id, long newV){
        Companies[id][0] = newV;
    }

    //Get Sector ID the company belongs to
    int getCompSectorInt(int i) {
        return (int)Companies[i][1];
    }

    //Get the current company revenue (hidden)
    long getCompRevenue(int id){
        return Companies[id][2];
    }

    //Update the current company revenue (hidden) (add amount)
    void UpdateCompRevenue(int id, long amount){
        Companies[id][2] += amount;
    }

    //Set the revenue back to 0 (at day end) to start accumulating again
    void ResetCompRevenue(int id){
        Companies[id][2] = 0;
    }

    //Get the (hidden) current value of the company
    long getCompCurrValue(int id){
        return Companies[id][7];
    }

    //Get the Sector the company belongs to (as string)
    String getCompSector(int id){
        return Sectors.values()[(int)Companies[id][1]].toString();
    }

    //Get the company outlook (that is used to determine future revenues)
    //The outlook in calculations cannot exceed +-1, but must be stored as is, so that it can be increased/decreased normally
    double getCompOutlook(int id){
        if(Math.abs(Companies[id][3])>10000) return Math.signum(Companies[id][3]);
        else return (double)Companies[id][3]/10000;
    }

    //Set the company outlook to a new value
    void setCompOutlook(int id, double newO){
        Companies[id][3] = (int)Math.round(newO*10000);
    }

    //Get the reported (last term's) revenue
    long getLastRevenue(int id) { return (int)Companies[id][4]; }

    //Set the reported (last term's) revenue
    void setLastRevenue(int id, long revenue) { Companies[id][4] = revenue; }

    //Get the investment for this company
    int getInvestment(int id) {
        return (int)Companies[id][5];
    }

    //Set the investment for this company
    void setInvestment(int id, int newInv){
        Companies[id][5]=newInv;
    }

    //Get the company's market share (affects revenue with sector outlook)
    double getMarketShare(int sid){
        return (double)Companies[sid][6]/1000;
    }

    //set the (hidden) current value of the company
    void setCompCurrValue(int id, long newV){
        Companies[id][7] = newV;
    }

    //Update the (hidden) current value of the company (add amount rev)
    void updCompCurrValue(int id, long rev){
        Companies[id][7] += rev;
    }

    //Bankrupt a company (set extremely negative total value and outlook, share price drops really fast)
    void Bankrupt(int i) { //you lose all shares in the remaining company
        setCompCurrValue(i, -10000);
        setCompOutlook(i, -10);
    }

    //remove a bankrupt company's name from the hash set
    void removeCompanyName(String name) {
        CompaniesNames.remove(name);
    }

    //Calculate an amount to be given as dividend
    int GetDividend(int i, long revenue) {
        int dividend = Math.max(getShareCurrPrince(i)/100, 100); //min dividend of $1, max at 1% of share price
        Random random = new Random();
        while (dividend*getTotalShares(i)>revenue*50){ //no more than half of revenue to dividend
            dividend-=random.nextInt(100);
        }
        return dividend;
    }



    //SHARES

    //Get the current price of the share
    int getShareCurrPrince(int id){
        return Shares[id][0];
    }

    //Set the current price of the share and alter the candle data if needed
    void setShareCurrPrice(int id, int price){
        Shares[id][0] = price;
        if(ShareDayLimits[id][0]==-1){
            ShareDayLimits[id][0]=price;
            ShareDayLimits[id][1]=price;
            ShareDayLimits[id][2]=price;
            ShareDayLimits[id][3]=price;
        } else {
            if(price>ShareDayLimits[id][1])ShareDayLimits[id][1]=price;
            else if(price<ShareDayLimits[id][2])ShareDayLimits[id][2]=price;
        }
    }

    //Get how many shares the player owns
    int getSharesOwned(int id){
        return Shares[id][1];
    }

    //Change the amount of shares the player owns
    void TransactShares(int id, int amount){
        Shares[id][1] += amount;
    }

    //Get the total number of shares outstanding the company has
    int getTotalShares(int id){
        return Shares[id][2];
    }

    //Get the price the share closed the previous day
    int getLastClose(int id){
        return Shares[id][3];
    }

    //Get the amount of shares remaining available (possible multi player use?)
    int getRemShares(int id){
        return Shares[id][4];
    }

    //Update the amount of shares remaining available (add amount) (possible multi player use?)
    void alterRemShares(int id, int amount){
        int newAm = Shares[id][4] - amount;
        Shares[id][4] = newAm;
    }

    //For when a company splits (doubles) its shares outstanding. Used to prevent the shares for reaching a very high price that
    //will make the unavailable to smaller investors
    void doubleShares(int sid) {
        Shares[sid][2] *= 2;
        Shares[sid][1] *= 2;
    }

    //For when a company halves its shares outstanding. Used to prevent the shares for reaching a very low price that
    //will make the unattractive to larger investors
    void halfShares(int sid) {
        Shares[sid][2] = (int)Math.floor(Shares[sid][2]/2)+1;
        if(Shares[sid][1]>0) {
            Shares[sid][1] = (int) Math.floor(Shares[sid][1] / 2) + 1;
        }
    }

    //Get the candlestick data of the share (Open, High, Low, Close)
    int[] getShareCandleData(int sid){
        int[] data = {ShareDayLimits[sid][0],ShareDayLimits[sid][1],ShareDayLimits[sid][2],ShareDayLimits[sid][3]};
        return data;
    }



    //SCAMS

    //Check if there is an ongoing scam with this share
    boolean isScam(int i) {
        return Scams.contains(getName(i));
    }

    //Get the id of the type of the scam (0 if no scam)
    int getScamType(int ScamTableIndex){
        return ScamResolution[ScamTableIndex][0];
    }

    //Get the days remaining until the scam is executed (-1 if no scam)
    int getScamRemDays(int ScamTableIndex){
        return ScamResolution[ScamTableIndex][1];
    }

    //Get the number of the currently ongoing scams
    int getScamsNo(){
        return Scams.size();
    }

    //Add a scam to the list, check if successful to proceed with adding scam data
    boolean addScam(int sid){
        return Scams.add(getName(sid));
    }

    //Add scam data (type and days until execution) by id
    void addScamData(int sid, int type, int totalDays) {
        ScamResolution[sid][0]=type;
        ScamResolution[sid][1]=totalDays;
    }

    //Remove an executed scam (to make share available again for new scams)
    void removeExecutedScam(int i){
        Scams.remove(getName(i));
        ScamResolution[i][0]=0;
        ScamResolution[i][1]=-1;
    }

    //Remove a called scam (without any effects)
    void removeCalledScam(int i){
        Scams.remove(getName(i));
        ScamResolution[i][0]=0;
        ScamResolution[i][1]=-1;
    }



    //SECTORS


    //Get the outlook of the sector. Adds the base outlook and the event outlook together. Base outlook is capped at +-1.
    double getSectorOutlook(int i){
        if(Math.abs(outlooks[i][0])<=1)return outlooks[i][0]+outlooks[i][1];
        else return Math.signum(outlooks[i][0])+outlooks[i][1];
    }

    //Get the base outlook (the one unaffected by events) of the sector
    double getBaseSectorOutlook(int i){
        return outlooks[i][0];
    }

    //Reset the sector base outlook
    void setSectorOutlook(int index, double newO){
        outlooks[index][0]=newO;
    }

    //Update the sector base outlook
    void alterSectorOutlook(int index, double newO){
        outlooks[index][0]+=newO;
    }

    //Update the sector outlook by adding newO
    void setSectorEventOutlook(int index, double newO){
        outlooks[index][1]+=newO;
    }

    //Get sector index for use in outlooks. 0 is omitted because it is used for the economy outlook
    int getSectorOutlookIndex(String Sector){ //For Finance Tables
        int index = -1;
        switch (Sector){
            case "Constr":
                index=1;
                break;
            case "Transp":
                index=2;
                break;
            case "Oil":
                index=3;
                break;
            case "Tech":
                index=4;
                break;
            case "Food":
                index=5;
                break;
            case "Telecom":
                index=6;
                break;
            case "Defence":
                index=7;
                break;
            case "Entert":
                index=8;
                break;
            case "Educ":
                index=9;
                break;
            case "Tourism":
                index=10;
                break;
        }
        return index;
    }

    //Get the number of companies in a sector
    int getSecCompNum(int Sec) { //WARNING: Does not count bankrupt companies, Use only in term Update adding company
        int count=0;
        for (int i = 0; i < Companies.length; i++) {
            if(getCompSectorInt(i)==Sec & getCompCurrValue(i)>0){
                count++;
            }
        }
        return count;
    }

}
