package com.angavai.bidding;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.*;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.*;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.config.annotation.*;
import javax.annotation.PostConstruct;
import javax.persistence.*;
import java.io.*;
import java.nio.file.*;
import java.util.*;
@SpringBootApplication
public class BiddingPlatformApplication {
    public static void main(String[] args) {
        SpringApplication.run(BiddingPlatformApplication.class, args);
        System.out.println("Backend Server Started Successfully!");
        System.out.println("========================================");
        System.out.println("Server running at: http://localhost:8080");
        System.out.println("========================================\n");
        openBrowser("file:///" + System.getProperty("user.dir").replace("\\", "/") + "/../index.html");
    }
    private static void openBrowser(String url) {
        try {
            String os = System.getProperty("os.name").toLowerCase();
            if (os.contains("win")) {
                Runtime.getRuntime().exec("rundll32 url.dll,FileProtocolHandler " + url);
            } else if (os.contains("mac")) {
                Runtime.getRuntime().exec("open " + url);
            } else {
                Runtime.getRuntime().exec("xdg-open " + url);
            }
            System.out.println("Opening browser: " + url);
        } catch (Exception e) {
            System.out.println("Could not open browser automatically: " + e.getMessage());
        }
    }
    @Configuration
    public static class CorsConfig {
        @Bean
        public WebMvcConfigurer corsConfigurer() {
            return new WebMvcConfigurer() {
                @Override
                public void addCorsMappings(CorsRegistry registry) {
                    registry.addMapping("/api/**")
                            .allowedOrigins("*")
                            .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                            .allowedHeaders("*")
                            .allowCredentials(false)
                            .maxAge(3600);
                }
            };
        }
    }
    @Entity
    @Table(name = "users")
    public static class User {
        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        private Long id;
        private String email;
        private String password;
        private String role;
        private String name;
        public User() {}
        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }
        public String getPassword() { return password; }
        public void setPassword(String password) { this.password = password; }
        public String getRole() { return role; }
        public void setRole(String role) { this.role = role; }
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
    }
    @Entity
    @Table(name = "auctions")
    public static class Auction {
        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        private Long id;
        private String productName;
        @Column(length = 2000)
        private String description;
        private Double biddingAmount;
        private Integer stock;
        private String startDate;
        private String endDate;
        private String orderTimeLimit;
        private Double interestRate;
        @Column(length = 100000)
        private String image;
        @Column(length = 5000)
        private String terms;
        private String sellerEmail;
        private String status;
        public Auction() {}
        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
        public String getProductName() { return productName; }
        public void setProductName(String productName) { this.productName = productName; }
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
        public Double getBiddingAmount() { return biddingAmount; }
        public void setBiddingAmount(Double biddingAmount) { this.biddingAmount = biddingAmount; }
        public Integer getStock() { return stock; }
        public void setStock(Integer stock) { this.stock = stock; }
        public String getStartDate() { return startDate; }
        public void setStartDate(String startDate) { this.startDate = startDate; }
        public String getEndDate() { return endDate; }
        public void setEndDate(String endDate) { this.endDate = endDate; }
        public String getOrderTimeLimit() { return orderTimeLimit; }
        public void setOrderTimeLimit(String orderTimeLimit) { this.orderTimeLimit = orderTimeLimit; }
        public Double getInterestRate() { return interestRate; }
        public void setInterestRate(Double interestRate) { this.interestRate = interestRate; }
        public String getImage() { return image; }
        public void setImage(String image) { this.image = image; }
        public String getTerms() { return terms; }
        public void setTerms(String terms) { this.terms = terms; }
        public String getSellerEmail() { return sellerEmail; }
        public void setSellerEmail(String sellerEmail) { this.sellerEmail = sellerEmail; }
        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
    }
    @Repository
    public interface UserRepository extends org.springframework.data.jpa.repository.JpaRepository<User, Long> {
        User findByEmail(String email);
    }
    @Repository
    public interface AuctionRepository extends org.springframework.data.jpa.repository.JpaRepository<Auction, Long> {
        List<Auction> findBySellerEmail(String sellerEmail);
    }
    @Service
    public static class JsonFileService {
        @Value("${app.data.directory:data}")
        private String dataDirectory;
        private final ObjectMapper objectMapper = new ObjectMapper();
        @PostConstruct
        public void init() throws IOException {
            Files.createDirectories(Paths.get(dataDirectory));
            initializeFile("users.json", new ArrayList<>());
            initializeFile("auctions.json", new ArrayList<>());
            initializeFile("bids.json", new ArrayList<>());
            initializeFile("orders.json", new HashMap<String, Object>() {{
                put("pending", new ArrayList<>());
                put("accepted", new ArrayList<>());
            }});
            initializeFile("cart.json", new HashMap<>());
            initializeFile("returns.json", new ArrayList<>());
            initializeFile("wallets.json", new HashMap<>());
            System.out.println("JSON storage initialized: " + dataDirectory);
        }
        private void initializeFile(String filename, Object defaultValue) throws IOException {
            File file = new File(dataDirectory, filename);
            if (!file.exists()) {
                objectMapper.writerWithDefaultPrettyPrinter().writeValue(file, defaultValue);
            }
        }
        public List<Map<String, Object>> readList(String filename) throws IOException {
            return objectMapper.readValue(new File(dataDirectory, filename), List.class);
        }
        public void writeList(String filename, List<?> data) throws IOException {
            objectMapper.writerWithDefaultPrettyPrinter().writeValue(new File(dataDirectory, filename), data);
        }
        public Map<String, Object> readMap(String filename) throws IOException {
            return objectMapper.readValue(new File(dataDirectory, filename), Map.class);
        }
        public void writeMap(String filename, Map<String, Object> data) throws IOException {
            objectMapper.writerWithDefaultPrettyPrinter().writeValue(new File(dataDirectory, filename), data);
        }
    }
    @Service
    public static class StorageService {
        @Value("${app.storage.type:json}")
        private String storageType;
        @Autowired(required = false)
        private UserRepository userRepository;
        @Autowired(required = false)
        private AuctionRepository auctionRepository;
        @Autowired
        private JsonFileService jsonFileService;
        @PostConstruct
        public void init() {
            System.out.println("Storage type: " + storageType.toUpperCase());
        }
        public boolean isMySQL() {
            return "mysql".equalsIgnoreCase(storageType);
        }
        public List<Map<String, Object>> getAllUsers() throws IOException {
            if (isMySQL()) {
                List<Map<String, Object>> users = new ArrayList<>();
                userRepository.findAll().forEach(u -> {
                    Map<String, Object> map = new HashMap<>();
                    map.put("email", u.getEmail());
                    map.put("password", u.getPassword());
                    map.put("role", u.getRole());
                    map.put("name", u.getName());
                    users.add(map);
                });
                return users;
            }
            return jsonFileService.readList("users.json");
        }
        public void saveUser(Map<String, Object> userData) throws IOException {
            if (isMySQL()) {
                User user = new User();
                user.setEmail((String) userData.get("email"));
                user.setPassword((String) userData.get("password"));
                user.setRole((String) userData.get("role"));
                user.setName((String) userData.get("name"));
                userRepository.save(user);
            } else {
                List<Map<String, Object>> users = jsonFileService.readList("users.json");
                users.add(userData);
                jsonFileService.writeList("users.json", users);
            }
        }
        public List<Map<String, Object>> getAllAuctions() throws IOException {
            if (isMySQL()) {
                List<Map<String, Object>> auctions = new ArrayList<>();
                auctionRepository.findAll().forEach(a -> auctions.add(auctionToMap(a)));
                return auctions;
            }
            return jsonFileService.readList("auctions.json");
        }
        public void saveAuction(Map<String, Object> auctionData) throws IOException {
            if (isMySQL()) {
                Auction auction = mapToAuction(auctionData);
                auctionRepository.save(auction);
            } else {
                List<Map<String, Object>> auctions = jsonFileService.readList("auctions.json");
                auctions.add(auctionData);
                jsonFileService.writeList("auctions.json", auctions);
            }
        }
        public void updateAuctions(List<Map<String, Object>> auctionsData) throws IOException {
            if (isMySQL()) {
                auctionRepository.deleteAll();
                auctionsData.forEach(data -> auctionRepository.save(mapToAuction(data)));
            } else {
                jsonFileService.writeList("auctions.json", auctionsData);
            }
        }
        private Map<String, Object> auctionToMap(Auction a) {
            Map<String, Object> map = new HashMap<>();
            map.put("id", a.getId());
            map.put("productName", a.getProductName());
            map.put("description", a.getDescription());
            map.put("biddingAmount", a.getBiddingAmount());
            map.put("stock", a.getStock());
            map.put("startDate", a.getStartDate());
            map.put("endDate", a.getEndDate());
            map.put("orderTimeLimit", a.getOrderTimeLimit());
            map.put("interestRate", a.getInterestRate());
            map.put("image", a.getImage());
            map.put("terms", a.getTerms());
            map.put("sellerEmail", a.getSellerEmail());
            map.put("status", a.getStatus());
            return map;
        }
        private Auction mapToAuction(Map<String, Object> data) {
            Auction a = new Auction();
            if (data.get("id") != null) a.setId(((Number) data.get("id")).longValue());
            a.setProductName((String) data.get("productName"));
            a.setDescription((String) data.get("description"));
            a.setBiddingAmount(data.get("biddingAmount") != null ? ((Number) data.get("biddingAmount")).doubleValue() : null);
            a.setStock(data.get("stock") != null ? ((Number) data.get("stock")).intValue() : null);
            a.setStartDate((String) data.get("startDate"));
            a.setEndDate((String) data.get("endDate"));
            a.setOrderTimeLimit((String) data.get("orderTimeLimit"));
            a.setInterestRate(data.get("interestRate") != null ? ((Number) data.get("interestRate")).doubleValue() : null);
            a.setImage((String) data.get("image"));
            a.setTerms((String) data.get("terms"));
            a.setSellerEmail((String) data.get("sellerEmail"));
            a.setStatus((String) data.get("status"));
            return a;
        }
        public Map<String, Object> getWallets() throws IOException {
            return jsonFileService.readMap("wallets.json");
        }
        public void updateWallets(Map<String, Object> wallets) throws IOException {
            jsonFileService.writeMap("wallets.json", wallets);
        }
        public Map<String, Object> getOrders() throws IOException {
            return jsonFileService.readMap("orders.json");
        }
        public void updateOrders(Map<String, Object> orders) throws IOException {
            jsonFileService.writeMap("orders.json", orders);
        }
        public Map<String, Object> getCart() throws IOException {
            return jsonFileService.readMap("cart.json");
        }
        public void updateCart(Map<String, Object> cart) throws IOException {
            jsonFileService.writeMap("cart.json", cart);
        }
        public List<Map<String, Object>> getReturns() throws IOException {
            return jsonFileService.readList("returns.json");
        }
        public void updateReturns(List<Map<String, Object>> returns) throws IOException {
            jsonFileService.writeList("returns.json", returns);
        }
    }
    @RestController
    @RequestMapping("/api/users")
    public static class UserController {
        @Autowired
        private StorageService storageService;
        @PostMapping("/register")
        public ResponseEntity<?> register(@RequestBody Map<String, Object> user) {
            try {
                List<Map<String, Object>> users = storageService.getAllUsers();
                for (Map<String, Object> u : users) {
                    if (user.get("email").equals(u.get("email"))) {
                        return ResponseEntity.status(400).body(Map.of("error", "Email already registered"));
                    }
                }
                storageService.saveUser(user);
                return ResponseEntity.ok(Map.of("message", "User registered successfully"));
            } catch (Exception e) {
                return ResponseEntity.status(500).body("Error: " + e.getMessage());
            }
        }
        @PostMapping("/login")
        public ResponseEntity<?> login(@RequestBody Map<String, Object> credentials) {
            try {
                String email = (String) credentials.get("email");
                String password = (String) credentials.get("password");
                List<Map<String, Object>> users = storageService.getAllUsers();
                for (Map<String, Object> u : users) {
                    if (email.equals(u.get("email")) && password.equals(u.get("password"))) {
                        return ResponseEntity.ok(u);
                    }
                }
                return ResponseEntity.status(401).body(Map.of("error", "Invalid credentials"));
            } catch (Exception e) {
                return ResponseEntity.status(500).body("Error: " + e.getMessage());
            }
        }
        @GetMapping("/wallet/{email}")
        public ResponseEntity<?> getWallet(@PathVariable String email) {
            try {
                Map<String, Object> wallets = storageService.getWallets();
                Object balance = wallets.getOrDefault(email, 0);
                return ResponseEntity.ok(((Number) balance).doubleValue());
            } catch (Exception e) {
                return ResponseEntity.status(500).body("Error: " + e.getMessage());
            }
        }
        @PostMapping("/wallet")
        public ResponseEntity<?> addFunds(@RequestBody Map<String, Object> body) {
            try {
                String email = (String) body.get("email");
                double amount = ((Number) body.get("amount")).doubleValue();
                Map<String, Object> wallets = storageService.getWallets();
                double current = wallets.containsKey(email) ? ((Number) wallets.get(email)).doubleValue() : 0;
                wallets.put(email, current + amount);
                storageService.updateWallets(wallets);
                return ResponseEntity.ok(Map.of("balance", current + amount));
            } catch (Exception e) {
                return ResponseEntity.status(500).body("Error: " + e.getMessage());
            }
        }
        @PostMapping("/wallet/debit")
        public ResponseEntity<?> debitWallet(@RequestBody Map<String, Object> body) {
            try {
                String email = (String) body.get("email");
                double amount = ((Number) body.get("amount")).doubleValue();
                Map<String, Object> wallets = storageService.getWallets();
                double current = wallets.containsKey(email) ? ((Number) wallets.get(email)).doubleValue() : 0;
                if (current < amount) return ResponseEntity.status(400).body(Map.of("error", "Insufficient balance"));
                wallets.put(email, current - amount);
                storageService.updateWallets(wallets);
                return ResponseEntity.ok(Map.of("balance", current - amount));
            } catch (Exception e) {
                return ResponseEntity.status(500).body("Error: " + e.getMessage());
            }
        }
        @PostMapping("/wallet/credit")
        public ResponseEntity<?> creditWallet(@RequestBody Map<String, Object> body) {
            try {
                String email = (String) body.get("email");
                double amount = ((Number) body.get("amount")).doubleValue();
                Map<String, Object> wallets = storageService.getWallets();
                double current = wallets.containsKey(email) ? ((Number) wallets.get(email)).doubleValue() : 0;
                wallets.put(email, current + amount);
                storageService.updateWallets(wallets);
                return ResponseEntity.ok(Map.of("balance", current + amount));
            } catch (Exception e) {
                return ResponseEntity.status(500).body("Error: " + e.getMessage());
            }
        }
    }
    @RestController
    @RequestMapping("/api/auctions")
    public static class AuctionController {
        @Autowired
        private StorageService storageService;
        @Autowired
        private JsonFileService jsonFileService;
        @GetMapping
        public ResponseEntity<?> getAuctions() {
            try {
                List<Map<String, Object>> all = storageService.getAllAuctions();
                List<Map<String, Object>> active = new ArrayList<>();
                for (Map<String, Object> a : all) {
                    if (Boolean.TRUE.equals(a.get("isActive"))) active.add(a);
                }
                return ResponseEntity.ok(active);
            } catch (Exception e) {
                return ResponseEntity.status(500).body("Error: " + e.getMessage());
            }
        }
        @GetMapping("/all")
        public ResponseEntity<?> getAllAuctions() {
            try {
                return ResponseEntity.ok(storageService.getAllAuctions());
            } catch (Exception e) {
                return ResponseEntity.status(500).body("Error: " + e.getMessage());
            }
        }
        @GetMapping("/seller/{email}")
        public ResponseEntity<?> getBySellerEmail(@PathVariable String email) {
            try {
                List<Map<String, Object>> all = storageService.getAllAuctions();
                List<Map<String, Object>> result = new ArrayList<>();
                for (Map<String, Object> a : all) {
                    if (email.equals(a.get("sellerId"))) result.add(a);
                }
                return ResponseEntity.ok(result);
            } catch (Exception e) {
                return ResponseEntity.status(500).body("Error: " + e.getMessage());
            }
        }
        @GetMapping("/category/{category}")
        public ResponseEntity<?> getByCategory(@PathVariable String category) {
            try {
                List<Map<String, Object>> all = storageService.getAllAuctions();
                List<Map<String, Object>> result = new ArrayList<>();
                for (Map<String, Object> a : all) {
                    if (category.equals(a.get("category"))) result.add(a);
                }
                return ResponseEntity.ok(result);
            } catch (Exception e) {
                return ResponseEntity.status(500).body("Error: " + e.getMessage());
            }
        }
        @GetMapping("/{id}")
        public ResponseEntity<?> getAuction(@PathVariable int id) {
            try {
                List<Map<String, Object>> all = storageService.getAllAuctions();
                for (Map<String, Object> a : all) {
                    if (id == ((Number) a.get("id")).intValue()) return ResponseEntity.ok(a);
                }
                return ResponseEntity.status(404).body("Not found");
            } catch (Exception e) {
                return ResponseEntity.status(500).body("Error: " + e.getMessage());
            }
        }
        @PostMapping
        public ResponseEntity<?> createAuction(@RequestBody Map<String, Object> auction) {
            try {
                List<Map<String, Object>> auctions = storageService.getAllAuctions();
                int maxId = 0;
                for (Map<String, Object> a : auctions) {
                    int aid = ((Number) a.get("id")).intValue();
                    if (aid > maxId) maxId = aid;
                }
                auction.put("id", maxId + 1);
                auctions.add(auction);
                storageService.updateAuctions(auctions);
                return ResponseEntity.ok(auction);
            } catch (Exception e) {
                return ResponseEntity.status(500).body("Error: " + e.getMessage());
            }
        }
        @PutMapping("/{id}")
        public ResponseEntity<?> updateAuction(@PathVariable int id, @RequestBody Map<String, Object> updated) {
            try {
                List<Map<String, Object>> auctions = storageService.getAllAuctions();
                for (int i = 0; i < auctions.size(); i++) {
                    if (id == ((Number) auctions.get(i).get("id")).intValue()) {
                        updated.put("id", id);
                        auctions.set(i, updated);
                        storageService.updateAuctions(auctions);
                        return ResponseEntity.ok(updated);
                    }
                }
                return ResponseEntity.status(404).body("Not found");
            } catch (Exception e) {
                return ResponseEntity.status(500).body("Error: " + e.getMessage());
            }
        }
        @DeleteMapping("/{id}")
        public ResponseEntity<?> deleteAuction(@PathVariable int id) {
            try {
                List<Map<String, Object>> auctions = storageService.getAllAuctions();
                auctions.removeIf(a -> id == ((Number) a.get("id")).intValue());
                storageService.updateAuctions(auctions);
                return ResponseEntity.ok(Map.of("message", "Deleted"));
            } catch (Exception e) {
                return ResponseEntity.status(500).body("Error: " + e.getMessage());
            }
        }
        @PutMapping("/{id}/stop")
        public ResponseEntity<?> stopAuction(@PathVariable int id) {
            try {
                List<Map<String, Object>> auctions = storageService.getAllAuctions();
                Map<String, Object> auction = null;
                int idx = -1;
                for (int i = 0; i < auctions.size(); i++) {
                    if (id == ((Number) auctions.get(i).get("id")).intValue()) { auction = auctions.get(i); idx = i; break; }
                }
                if (auction == null) return ResponseEntity.status(404).body("Not found");
                List<Map<String, Object>> bids = jsonFileService.readList("bids.json");
                Map<String, Object> topBid = null;
                double topAmount = -1;
                for (Map<String, Object> b : bids) {
                    if (b.get("auctionId") != null && id == ((Number) b.get("auctionId")).intValue()) {
                        double amt = ((Number) b.get("amount")).doubleValue();
                        if (amt > topAmount) { topAmount = amt; topBid = b; }
                    }
                }
                auction.put("isActive", false);
                if (topBid != null) {
                    String winnerEmail = (String) topBid.get("bidderEmail");
                    String winnerName = (String) topBid.get("name");
                    auction.put("winnerId", winnerEmail);
                    auction.put("winnerName", winnerName);
                    auction.put("winnerBid", topAmount);
                }
                auctions.set(idx, auction);
                storageService.updateAuctions(auctions);
                return ResponseEntity.ok(auction);
            } catch (Exception e) {
                return ResponseEntity.status(500).body("Error: " + e.getMessage());
            }
        }
    }
    @RestController
    @RequestMapping("/api/orders")
    public static class OrderController {
        @Autowired
        private StorageService storageService;
        @GetMapping
        public ResponseEntity<?> getOrders() {
            try {
                return ResponseEntity.ok(storageService.getOrders());
            } catch (Exception e) {
                return ResponseEntity.status(500).body("Error: " + e.getMessage());
            }
        }
        @PostMapping
        public ResponseEntity<?> createOrder(@RequestBody Map<String, Object> order) {
            try {
                Map<String, Object> orders = storageService.getOrders();
                List<Map<String, Object>> pending = (List<Map<String, Object>>) orders.getOrDefault("pending", new ArrayList<>());
                int maxId = 0;
                for (Map<String, Object> o : pending) {
                    if (o.get("id") != null) { int oid = ((Number) o.get("id")).intValue(); if (oid > maxId) maxId = oid; }
                }
                List<Map<String, Object>> accepted = (List<Map<String, Object>>) orders.getOrDefault("accepted", new ArrayList<>());
                for (Map<String, Object> o : accepted) {
                    if (o.get("id") != null) { int oid = ((Number) o.get("id")).intValue(); if (oid > maxId) maxId = oid; }
                }
                order.put("id", maxId + 1);
                order.put("status", "pending");
                pending.add(order);
                orders.put("pending", pending);
                storageService.updateOrders(orders);
                return ResponseEntity.ok(order);
            } catch (Exception e) {
                return ResponseEntity.status(500).body("Error: " + e.getMessage());
            }
        }
        @GetMapping("/pending/{sellerEmail}")
        public ResponseEntity<?> getPendingOrders(@PathVariable String sellerEmail) {
            try {
                Map<String, Object> orders = storageService.getOrders();
                List<Map<String, Object>> pending = (List<Map<String, Object>>) orders.getOrDefault("pending", new ArrayList<>());
                List<Map<String, Object>> result = new ArrayList<>();
                for (Map<String, Object> o : pending) {
                    if (sellerEmail.equals(o.get("sellerId"))) result.add(o);
                }
                return ResponseEntity.ok(result);
            } catch (Exception e) {
                return ResponseEntity.status(500).body("Error: " + e.getMessage());
            }
        }
        @GetMapping("/accepted/{sellerEmail}")
        public ResponseEntity<?> getAcceptedOrders(@PathVariable String sellerEmail) {
            try {
                Map<String, Object> orders = storageService.getOrders();
                List<Map<String, Object>> accepted = (List<Map<String, Object>>) orders.getOrDefault("accepted", new ArrayList<>());
                List<Map<String, Object>> result = new ArrayList<>();
                for (Map<String, Object> o : accepted) {
                    if (sellerEmail.equals(o.get("sellerId"))) result.add(o);
                }
                return ResponseEntity.ok(result);
            } catch (Exception e) {
                return ResponseEntity.status(500).body("Error: " + e.getMessage());
            }
        }
        @PutMapping("/{id}/accept")
        public ResponseEntity<?> acceptOrder(@PathVariable int id) {
            try {
                Map<String, Object> orders = storageService.getOrders();
                List<Map<String, Object>> pending = (List<Map<String, Object>>) orders.getOrDefault("pending", new ArrayList<>());
                List<Map<String, Object>> accepted = (List<Map<String, Object>>) orders.getOrDefault("accepted", new ArrayList<>());
                Map<String, Object> found = null;
                for (Map<String, Object> o : pending) {
                    if (o.get("id") != null && id == ((Number) o.get("id")).intValue()) { found = o; break; }
                }
                if (found != null) {
                    pending.remove(found);
                    found.put("status", "accepted");
                    accepted.add(found);
                    orders.put("pending", pending);
                    orders.put("accepted", accepted);
                    storageService.updateOrders(orders);
                }
                return ResponseEntity.ok(Map.of("message", "Order accepted"));
            } catch (Exception e) {
                return ResponseEntity.status(500).body("Error: " + e.getMessage());
            }
        }
        @GetMapping("/buyer/{buyerEmail}")
        public ResponseEntity<?> getBuyerOrders(@PathVariable String buyerEmail) {
            try {
                Map<String, Object> orders = storageService.getOrders();
                List<Map<String, Object>> all = new ArrayList<>();
                all.addAll((List<Map<String, Object>>) orders.getOrDefault("pending", new ArrayList<>()));
                all.addAll((List<Map<String, Object>>) orders.getOrDefault("accepted", new ArrayList<>()));
                List<Map<String, Object>> result = new ArrayList<>();
                for (Map<String, Object> o : all) {
                    if (buyerEmail.equals(o.get("buyerEmail"))) result.add(o);
                }
                return ResponseEntity.ok(result);
            } catch (Exception e) {
                return ResponseEntity.status(500).body("Error: " + e.getMessage());
            }
        }
        @DeleteMapping("/{id}")
        public ResponseEntity<?> deleteOrder(@PathVariable int id) {
            try {
                Map<String, Object> orders = storageService.getOrders();
                List<Map<String, Object>> pending = (List<Map<String, Object>>) orders.getOrDefault("pending", new ArrayList<>());
                pending.removeIf(o -> o.get("id") != null && id == ((Number) o.get("id")).intValue());
                orders.put("pending", pending);
                storageService.updateOrders(orders);
                return ResponseEntity.ok(Map.of("message", "Order deleted"));
            } catch (Exception e) {
                return ResponseEntity.status(500).body("Error: " + e.getMessage());
            }
        }
    }
    @RestController
    @RequestMapping("/api/bids")
    public static class BidController {
        @Autowired
        private StorageService storageService;
        @Autowired
        private JsonFileService jsonFileService;

        @GetMapping("/auction/{auctionId}")
        public ResponseEntity<?> getBidsByAuction(@PathVariable int auctionId) {
            try {
                List<Map<String, Object>> bids = jsonFileService.readList("bids.json");
                List<Map<String, Object>> result = new ArrayList<>();
                for (Map<String, Object> b : bids) {
                    if (b.get("auctionId") != null && auctionId == ((Number) b.get("auctionId")).intValue()) result.add(b);
                }
                return ResponseEntity.ok(result);
            } catch (Exception e) {
                return ResponseEntity.status(500).body("Error: " + e.getMessage());
            }
        }
        @PostMapping
        public ResponseEntity<?> createBid(@RequestBody Map<String, Object> bid) {
            try {
                List<Map<String, Object>> bids = jsonFileService.readList("bids.json");
                bids.add(bid);
                jsonFileService.writeList("bids.json", bids);
                return ResponseEntity.ok(bid);
            } catch (Exception e) {
                return ResponseEntity.status(500).body("Error: " + e.getMessage());
            }
        }
    }
    @RestController
    @RequestMapping("/api/cart")
    public static class CartController {
        @Autowired
        private StorageService storageService;
        @GetMapping("/{email}")
        public ResponseEntity<?> getCart(@PathVariable String email) {
            try {
                Map<String, Object> allCarts = storageService.getCart();
                Object userCart = allCarts.getOrDefault(email, new ArrayList<>());
                return ResponseEntity.ok(userCart);
            } catch (Exception e) {
                return ResponseEntity.status(500).body("Error: " + e.getMessage());
            }
        }
        @PostMapping("/{email}")
        public ResponseEntity<?> addToCart(@PathVariable String email, @RequestBody Map<String, Object> item) {
            try {
                Map<String, Object> allCarts = storageService.getCart();
                List<Map<String, Object>> userCart = (List<Map<String, Object>>) allCarts.getOrDefault(email, new ArrayList<>());
                // avoid duplicates by productId
                userCart.removeIf(i -> i.get("productId") != null && i.get("productId").equals(item.get("productId")));
                userCart.add(item);
                allCarts.put(email, userCart);
                storageService.updateCart(allCarts);
                return ResponseEntity.ok(userCart);
            } catch (Exception e) {
                return ResponseEntity.status(500).body("Error: " + e.getMessage());
            }
        }
        @DeleteMapping("/{email}/{productId}")
        public ResponseEntity<?> removeFromCart(@PathVariable String email, @PathVariable int productId) {
            try {
                Map<String, Object> allCarts = storageService.getCart();
                List<Map<String, Object>> userCart = (List<Map<String, Object>>) allCarts.getOrDefault(email, new ArrayList<>());
                userCart.removeIf(i -> i.get("productId") != null && productId == ((Number) i.get("productId")).intValue());
                allCarts.put(email, userCart);
                storageService.updateCart(allCarts);
                return ResponseEntity.ok(userCart);
            } catch (Exception e) {
                return ResponseEntity.status(500).body("Error: " + e.getMessage());
            }
        }
    }
    @RestController
    @RequestMapping("/api/returns")
    public static class ReturnsController {
        @Autowired
        private StorageService storageService;

        @GetMapping
        public ResponseEntity<?> getReturns() {
            try {
                return ResponseEntity.ok(storageService.getReturns());
            } catch (Exception e) {
                return ResponseEntity.status(500).body("Error: " + e.getMessage());
            }
        }
        @PostMapping
        public ResponseEntity<?> updateReturns(@RequestBody List<Map<String, Object>> returns) {
            try {
                storageService.updateReturns(returns);
                return ResponseEntity.ok(Map.of("message", "Returns updated successfully"));
            } catch (Exception e) {
                return ResponseEntity.status(500).body("Error: " + e.getMessage());
            }
        }
    }
}