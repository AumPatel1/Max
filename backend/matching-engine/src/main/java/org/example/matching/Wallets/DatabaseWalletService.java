package org.example.matching.Wallets;

import lombok.RequiredArgsConstructor;
import org.example.matching.Repository.OrderJpaRepository;
import org.example.matching.Repository.ReservationRepository;
import org.example.matching.Repository.WalletRepository;
import org.example.matching.Repository.WalletShareRepository;
import org.example.matching.entity.*;
import org.example.matching.model.Order;
import org.example.matching.model.Reservation;
import org.example.matching.model.Trade;
import org.example.matching.model.Wallet;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@Service
@RequiredArgsConstructor
public class DatabaseWalletService implements WalletService {

    private final WalletRepository walletRepo;
    private final WalletShareRepository shareRepo;
    private final OrderJpaRepository orderRepo;
    private final ReservationRepository reservationRepo;

    // ── Ensure wallet row exists ─────────────────────────────────────────────

    @Transactional
    private void ensureWallet(String userId) {
        if (!walletRepo.existsById(userId)) {
            walletRepo.save(WalletEntity.builder()
                    .userId(userId)
                    .availableCash(0L)
                    .reservedCash(0L)
                    .build());
        }
    }

    // ── WalletService interface ──────────────────────────────────────────────

    /**
     * Public wallet creation — called once during user registration.
     * Delegates to ensureWallet() which is idempotent (safe to call if wallet already exists).
     * The @Transactional here is load-bearing (unlike the private ensureWallet method
     * where Spring AOP cannot proxy private methods — the annotation there is effectively ignored).
     */
    @Override
    @Transactional
    public void createWallet(String userId) {
        ensureWallet(userId);
    }

    @Override
    @Transactional
    public boolean reserveForOrder(Order order) {
        String userId = order.getUserId();
        String instrument = order.getInstrument();
        ensureWallet(userId);

        if ("BUY".equals(order.getSide().name())) {
            long required = order.getPrice() * (long) order.getQuantity();
            int rows = walletRepo.atomicReserveCash(userId, required);
            if (rows == 0) return false;

            reservationRepo.save(ReservationEntity.builder()
                    .orderId(order.getId())
                    .userId(userId)
                    .instrument(instrument)
                    .isBuy(true)
                    .priceAtReserve(order.getPrice())
                    .remainingQty(order.getQuantity())
                    .reservedCash(required)
                    .reservedShares(0L)
                    .build());
        } else {
            int rows = shareRepo.atomicReserveShares(userId, instrument, order.getQuantity());
            if (rows == 0) return false;

            reservationRepo.save(ReservationEntity.builder()
                    .orderId(order.getId())
                    .userId(userId)
                    .instrument(instrument)
                    .isBuy(false)
                    .priceAtReserve(order.getPrice())
                    .remainingQty(order.getQuantity())
                    .reservedCash(0L)
                    .reservedShares(order.getQuantity())
                    .build());
        }
        return true;
    }

    @Override
    @Transactional
    public void releaseReservation(String orderId) {
        ReservationEntity res = reservationRepo.findById(orderId).orElse(null);
        if (res == null) return;

        if (res.isBuy()) {
            walletRepo.releaseReservedCash(res.getUserId(), res.getReservedCash());
        } else {
            shareRepo.releaseReservedShares(res.getUserId(), res.getInstrument(), res.getReservedShares());
        }
        reservationRepo.deleteById(orderId);
    }

    @Override
    @Transactional
    public void settleTrade(Trade trade) {
        OrderEntity buyOrder  = orderRepo.findById(trade.getBuyOrderId()).orElse(null);
        OrderEntity sellOrder = orderRepo.findById(trade.getSellOrderId()).orElse(null);
        if (buyOrder == null || sellOrder == null) return;

        String instrument = buyOrder.getInstrument();
        int    qty        = (int) trade.getQuantity();
        long   tradeValue = trade.getPrice() * qty;

        // ── Buy side ───────────────────────────────────────────────────────
        ReservationEntity buyRes = reservationRepo.findById(trade.getBuyOrderId()).orElse(null);
        if (buyRes != null) {
            long cashDebited = (long) qty * buyRes.getPriceAtReserve();
            long refund      = cashDebited - tradeValue;

            walletRepo.decrementReservedCash(buyOrder.getUserId(), cashDebited);
            shareRepo.upsertAddAvailableQty(buyOrder.getUserId(), instrument, qty);
            if (refund > 0) {
                walletRepo.addAvailableCash(buyOrder.getUserId(), refund);
            }

            buyRes.setRemainingQty(buyRes.getRemainingQty() - qty);
            buyRes.setReservedCash(buyRes.getReservedCash() - cashDebited);
            if (buyRes.getRemainingQty() <= 0) {
                reservationRepo.deleteById(trade.getBuyOrderId());
            } else {
                reservationRepo.save(buyRes);
            }
        }

        // ── Sell side ──────────────────────────────────────────────────────
        ReservationEntity sellRes = reservationRepo.findById(trade.getSellOrderId()).orElse(null);
        if (sellRes != null) {
            shareRepo.decrementReservedQty(sellOrder.getUserId(), instrument, qty);
            walletRepo.addAvailableCash(sellOrder.getUserId(), tradeValue);

            sellRes.setRemainingQty(sellRes.getRemainingQty() - qty);
            sellRes.setReservedShares(sellRes.getReservedShares() - qty);
            if (sellRes.getRemainingQty() <= 0) {
                reservationRepo.deleteById(trade.getSellOrderId());
            } else {
                reservationRepo.save(sellRes);
            }
        }
    }

    @Override
    @Transactional
    public void creditUserCash(String userId, long cash) {
        ensureWallet(userId);
        walletRepo.addAvailableCash(userId, cash);
    }

    @Override
    @Transactional
    public void creditUserShares(String userId, String instrument, long shares) {
        ensureWallet(userId);
        shareRepo.upsertAddAvailableQty(userId, instrument, shares);
    }

    @Override
    @Transactional
    public void creditUserShares(String userId, long shares) {
        creditUserShares(userId, "MARKET", shares);
    }

    @Override
    @Transactional
    public void zeroOutShares(String userId, String instrument) {
        shareRepo.zeroOut(userId, instrument);
    }

    @Override
    public Wallet getWallet(String userId) {
        WalletEntity entity = walletRepo.findById(userId).orElse(null);
        if (entity == null) return null;
        return buildWallet(entity, shareRepo.findByUserId(userId));
    }

    @Override
    public Collection<Wallet> getAllWallets() {
        List<Wallet> result = new ArrayList<>();
        for (WalletEntity entity : walletRepo.findAll()) {
            result.add(buildWallet(entity, shareRepo.findByUserId(entity.getUserId())));
        }
        return result;
    }

    // ── Helpers ──────────────────────────────────────────────────────────────

    private Wallet buildWallet(WalletEntity entity, List<WalletShareEntity> shares) {
        Wallet w = new Wallet(entity.getUserId());
        w.addAvailableCash(entity.getAvailableCash());
        w.initReservedCash(entity.getReservedCash());
        for (WalletShareEntity s : shares) {
            if (s.getAvailableQty() > 0) w.addAvailableShares(s.getInstrument(), s.getAvailableQty());
            if (s.getReservedQty()  > 0) w.initReservedShares(s.getInstrument(), s.getReservedQty());
        }
        return w;
    }
}
