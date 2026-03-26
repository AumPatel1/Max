package org.example.matching.model;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

public class Wallet {
    private final String UserID;
    private final AtomicLong availablecash = new AtomicLong(0);
    private final AtomicLong reservedcash = new AtomicLong(0);

    // Fixed: Standardized on AtomicLong for all maps
    private final ConcurrentHashMap<String, AtomicLong> availableShares = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, AtomicLong> reservedShares = new ConcurrentHashMap<>();

    public Wallet(String userID) {
        this.UserID = userID;
    }

    public String getUserId() {
        return UserID;
    }

    public long getAvailableCash() {
        return availablecash.get();
    }

    public long getReservedCash() {
        return reservedcash.get();
    }

    public void addAvailableCash(long amount) {
        availablecash.addAndGet(amount);
    }

    public boolean tryReserveCash(long amount) {
        while (true) {
            long avail = availablecash.get();
            //if avail less than the amount needed
            if (avail < amount) return false;
            if (availablecash.compareAndSet(avail, avail - amount)) {
                reservedcash.addAndGet(amount);
                return true;
                //else avail = avail - amount and reserve cash .add amount
            }
        }
    }

    public void releaseReserveCash(long amount) {
        //in release reserve cash we just subtract the amount from the reservedcash and add it back to the available cash.
        reservedcash.addAndGet(-amount);
        availablecash.addAndGet(amount);
    }

//remove the money from the reserves
    public void debitReservedCash(long amount) {
        reservedcash.addAndGet(-amount);

    }

    // Fixed: Returns along to match AtomicLong, and handles the Map lookup safely
    public long getAvailableShares(String instrument) {
        AtomicLong val = availableShares.get(instrument);
        return (val == null) ? 0L : val.get();
    }

    public long getReservedShares(String instrument) {
        AtomicLong val = reservedShares.get(instrument);
        return (val == null) ? 0L : val.get();
    }

    // Methods to get all shares for API responses
    public ConcurrentHashMap<String, AtomicLong> getAvailableShares() {
        return availableShares;
    }

    public ConcurrentHashMap<String, AtomicLong> getReservedShares() {
        return reservedShares;
    }

    // get the quantity of the shares the available
    //get the qty of reserved of shares
    //pass the shares and the qty to reserve it
    public boolean tryReserveShares(String instrument, long qty) {
        // Only look up existing entry — don't create a ghost zero entry if shares don't exist
        AtomicLong avail = availableShares.get(instrument);
        if (avail == null) return false;
        AtomicLong reserved = null;
        while (true) {
            long currentAvail = avail.get();
            if (currentAvail < qty) return false;
            if (avail.compareAndSet(currentAvail, currentAvail - qty)) {
                // Create reserved entry only when actually reserving (avoids ghost 0 entries)
                if (reserved == null) {
                    reserved = reservedShares.computeIfAbsent(instrument, k -> new AtomicLong(0));
                }
                reserved.addAndGet(qty);
                return true;
            }
        }
    }
    // see if the instrument exists
    //if not create it
    //
//atomiclong avail = availableshares.computeifabsent(instrument ,
    //atomiclong resrved = resrvedshares.computeIfabsent(instrument,k-> new AtomicLong(0);
    public void releaseReservedShares(String instrument, long qty) {
        AtomicLong reserved = reservedShares.get(instrument);
        if (reserved != null) {
            if (reserved.addAndGet(-qty) <= 0) reservedShares.remove(instrument);
        }

        AtomicLong avail = availableShares.get(instrument);
        if (avail != null) {
            avail.addAndGet(qty);
        }
    }

    public void debitReservedShares(String instrument, long qty) {
        AtomicLong reserved = reservedShares.get(instrument);
        if (reserved != null) {
            if (reserved.addAndGet(-qty) <= 0) reservedShares.remove(instrument);
        }
    }

    /** Used by DatabaseWalletService to hydrate reserved cash from DB without going through CAS. */
    public void initReservedCash(long amount) {
        reservedcash.set(amount);
    }

    /** Used by DatabaseWalletService to hydrate reserved shares from DB without going through CAS. */
    public void initReservedShares(String instrument, long qty) {
        reservedShares.computeIfAbsent(instrument, k -> new java.util.concurrent.atomic.AtomicLong(0)).set(qty);
    }

    //add so available shares . add the shares and in if not create and add
    public void addAvailableShares(String instrument, long qty) {
        // Fixed: Used AtomicLong in the lambda
        availableShares.computeIfAbsent(instrument, k -> new AtomicLong(0))
                .addAndGet(qty);
    }
}