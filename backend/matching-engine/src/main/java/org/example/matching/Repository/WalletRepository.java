package org.example.matching.repository;

import org.example.matching.entity.WalletEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface WalletRepository extends JpaRepository<WalletEntity, String> {

    // Atomic: debit available, credit reserved — only succeeds if funds exist
    @Modifying
    @Query("UPDATE WalletEntity w SET w.availableCash = w.availableCash - :amount, w.reservedCash = w.reservedCash + :amount WHERE w.userId = :userId AND w.availableCash >= :amount")
    int atomicReserveCash(@Param("userId") String userId, @Param("amount") long amount);

    @Modifying
    @Query("UPDATE WalletEntity w SET w.availableCash = w.availableCash + :amount WHERE w.userId = :userId")
    void addAvailableCash(@Param("userId") String userId, @Param("amount") long amount);

    @Modifying
    @Query("UPDATE WalletEntity w SET w.reservedCash = w.reservedCash - :amount WHERE w.userId = :userId")
    void decrementReservedCash(@Param("userId") String userId, @Param("amount") long amount);

    @Modifying
    @Query("UPDATE WalletEntity w SET w.reservedCash = w.reservedCash - :amount, w.availableCash = w.availableCash + :amount WHERE w.userId = :userId")
    void releaseReservedCash(@Param("userId") String userId, @Param("amount") long amount);
}
