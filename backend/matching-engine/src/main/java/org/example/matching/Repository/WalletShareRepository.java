package org.example.matching.repository;

import org.example.matching.entity.WalletShareEntity;
import org.example.matching.entity.WalletShareId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface WalletShareRepository extends JpaRepository<WalletShareEntity, WalletShareId> {

    List<WalletShareEntity> findByUserId(String userId);

    // Atomic: debit available, credit reserved — only succeeds if shares exist
    @Modifying
    @Query("UPDATE WalletShareEntity ws SET ws.availableQty = ws.availableQty - :qty, ws.reservedQty = ws.reservedQty + :qty WHERE ws.userId = :userId AND ws.instrument = :instrument AND ws.availableQty >= :qty")
    int atomicReserveShares(@Param("userId") String userId, @Param("instrument") String instrument, @Param("qty") long qty);

    @Modifying
    @Query("UPDATE WalletShareEntity ws SET ws.reservedQty = ws.reservedQty - :qty WHERE ws.userId = :userId AND ws.instrument = :instrument")
    void decrementReservedQty(@Param("userId") String userId, @Param("instrument") String instrument, @Param("qty") long qty);

    @Modifying
    @Query("UPDATE WalletShareEntity ws SET ws.reservedQty = ws.reservedQty - :qty, ws.availableQty = ws.availableQty + :qty WHERE ws.userId = :userId AND ws.instrument = :instrument")
    void releaseReservedShares(@Param("userId") String userId, @Param("instrument") String instrument, @Param("qty") long qty);

    // Upsert available shares (PostgreSQL ON CONFLICT)
    @Modifying
    @Query(value = "INSERT INTO wallet_shares (user_id, instrument, available_qty, reserved_qty) VALUES (:userId, :instrument, :qty, 0) ON CONFLICT (user_id, instrument) DO UPDATE SET available_qty = wallet_shares.available_qty + :qty", nativeQuery = true)
    void upsertAddAvailableQty(@Param("userId") String userId, @Param("instrument") String instrument, @Param("qty") long qty);

    // Add to available (for settlement credits)
    @Modifying
    @Query("UPDATE WalletShareEntity ws SET ws.availableQty = ws.availableQty + :qty WHERE ws.userId = :userId AND ws.instrument = :instrument")
    void addAvailableQty(@Param("userId") String userId, @Param("instrument") String instrument, @Param("qty") long qty);

    // Zero out both available and reserved (used at event settlement)
    @Modifying
    @Query("UPDATE WalletShareEntity ws SET ws.availableQty = 0, ws.reservedQty = 0 WHERE ws.userId = :userId AND ws.instrument = :instrument")
    void zeroOut(@Param("userId") String userId, @Param("instrument") String instrument);

    // Find all users holding shares of an instrument (for settlement payout)
    @Query("SELECT ws FROM WalletShareEntity ws WHERE ws.instrument = :instrument AND (ws.availableQty > 0 OR ws.reservedQty > 0)")
    List<WalletShareEntity> findHolders(@Param("instrument") String instrument);
}
