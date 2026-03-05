package org.example;

import org.example.matching.Replay;
import org.example.matching.Wallets.InMemoryWalletService;
import org.example.matching.Wallets.RiskManager;
import org.example.matching.journal.EventJournal;
import org.example.matching.matching.MatchingEngine;
import org.example.matching.model.Order;
import org.example.matching.model.OrderBook;
import org.example.matching.model.OrderSide;
import org.example.matching.model.Trade;
import org.example.matching.orderbook.InMemoryOrderRepository;
import org.example.matching.orderbook.OrderOrchestrator;
import org.example.matching.orderbook.OrderRepository;
import org.example.matching.validation.OrderValidator;
import org.example.matching.util.IdGenerator;

import java.util.List;
import java.util.UUID;

public class Main {
    public static void main(String[] args) {
        System.out.println("=== PART A: Engine-only smoke test ===");
        engineSmokeTest();

        System.out.println("\n\n=== PART B: Full orchestrator E2E test ===");
        orchestratorE2ETest();

        System.out.println("\n\n=== PART C: Replay test ===");
        replayTest();
    }

    private static void engineSmokeTest(){
        System.out.println("Testing MatchingEngine directly...");
        MatchingEngine engine = new MatchingEngine();
        
        Order s1 = new Order(IdGenerator.newId(),"seller1",100L,10,System.currentTimeMillis(),OrderSide.SELL, "AAPL");
        engine.placeOrder(s1);
        System.out.println("After SELL (10 @100):");
        System.out.println(engine.dumpBook());

        Order b1 = new Order(IdGenerator.newId(), "buyer1", 105L, 4, System.currentTimeMillis(), OrderSide.BUY, "AAPL");
        List<Trade> t1 = engine.placeOrder(b1);
        System.out.println("Trade executed: " + t1.size() + " trade(s)");
        System.out.println(engine.dumpBook());

        Order b2 = new Order(IdGenerator.newId(), "buyer2", 100L, 6, System.currentTimeMillis(), OrderSide.BUY, "AAPL");
        List<Trade> t2 = engine.placeOrder(b2);
        System.out.println("Trade executed: " + t2.size() + " trade(s)");
        System.out.println("Final order book:\n" + engine.dumpBook());
    }


        private static void orchestratorE2ETest(){
        System.out.println("Testing full orchestrator with wallet integration...");
        OrderRepository orderRepo = new InMemoryOrderRepository();
        InMemoryWalletService walletService = new InMemoryWalletService(orderRepo);
        RiskManager riskManager = new RiskManager(walletService, orderRepo);
        EventJournal journal = new EventJournal();
        MatchingEngine engine = new MatchingEngine();
        
        // Create orchestrator with full constructor
        OrderOrchestrator orchestrator = new OrderOrchestrator(engine, journal, riskManager, orderRepo, walletService);
        
        // Top up wallets
        walletService.creditUserShares("seller1", 10);
        walletService.creditUserCash("buyer1", 10000);

        System.out.println("Initial state - Buyer cash: " + walletService.getWallet("buyer1").getAvailableCash() + 
                          ", Seller shares: " + walletService.getWallet("seller1").getAvailableShares("MARKET"));

        // create orders
        Order sell = new Order(UUID.randomUUID().toString(), "seller1", 100L, 10, System.currentTimeMillis(), OrderSide.SELL, "TSLA");
        Order buy1 = new Order(UUID.randomUUID().toString(), "buyer1", 105L, 4, System.currentTimeMillis(), OrderSide.BUY, "TSLA");
        Order buy2 = new Order(UUID.randomUUID().toString(), "buyer1", 100L, 6, System.currentTimeMillis(), OrderSide.BUY, "TSLA");
        
        System.out.println("\nSubmitting orders...");
        orchestrator.submitOrder(sell);
        orchestrator.submitOrder(buy1);
        orchestrator.submitOrder(buy2);

        // final state
        System.out.println("\nFinal order book:");
        System.out.println(engine.dumpBook());

        System.out.println("\nFinal wallet balances:");
        System.out.println("Buyer - Cash: " + walletService.getWallet("buyer1").getAvailableCash() + 
                          ", Shares: " + walletService.getWallet("buyer1").getAvailableShares("MARKET"));
        System.out.println("Seller - Cash: " + walletService.getWallet("seller1").getAvailableCash() + 
                          ", Shares: " + walletService.getWallet("seller1").getAvailableShares("MARKET"));

        // Show only recent journal entries (last 10)
        System.out.println("\nRecent journal entries:");
        List<String> lines = journal.readAllLines();
        int start = Math.max(0, lines.size() - 10);
        for (int i = start; i < lines.size(); i++) {
            System.out.println(lines.get(i));
        }
    }
    
    private static void replayTest(){
        System.out.println("Testing journal replay functionality...");
        MatchingEngine engine2 = new MatchingEngine();
        Replay r = new Replay(engine2);
        r.replayJournal();
        System.out.println("Reconstructed order book:");
        System.out.println(engine2.dumpBook());
    }
}