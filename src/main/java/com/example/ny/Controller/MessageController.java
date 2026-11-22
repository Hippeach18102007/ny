package com.example.ny.Controller;

import com.example.ny.Model.GameMessage;
import com.example.ny.Service.EmailService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Controller

public class MessageController {
    @Autowired
    private com.example.ny.Respository.LetterRepository letterRepo;

    // --- CẤU HÌNH THÔNG TIN CƠ BẢN ---
    private final String tenChi = "Nguyễn Thùy Dương";
    private final String tenEm = "Anh Duc";
    private final String myEmail = "ducdath04243@fpt.edu.vn";

    // 🔥 MẬT KHẨU ĐỂ VÀO TRANG
    private final String PASSWORD = "27092010";

    @Autowired
    private EmailService emailService;

    // --- 1. LOGIC ĐĂNG NHẬP / TRANG CHỦ ---

    @GetMapping("/")
    public String index(HttpSession session) {
        if (session.getAttribute("isLoggedIn") != null) {
            return "redirect:/gui-chi";
        }
        return "redirect:/login";
    }

    @GetMapping("/login")
    public String showLoginPage() {
        return "login";
    }

    @PostMapping("/login")
    public String handleLogin(@RequestParam("password") String password, HttpSession session, Model model) {
        if (PASSWORD.equals(password)) {
            session.setAttribute("isLoggedIn", true);
            return "redirect:/gui-chi";
        } else {
            model.addAttribute("error", "Sai mật khẩu rồi! Gợi ý: Ngày sinh nhật hoặc kỷ niệm 🎂");
            return "login";
        }
    }

    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/login";
    }

    // --- 2. TRANG THƯ ---



    // --- 2. TRANG THƯ ---
    @GetMapping("/gui-chi")
    public String showLetter(Model model, HttpSession session) {
        if (session.getAttribute("isLoggedIn") == null) {
            return "redirect:/login";
        }

        var letters = letterRepo.findAllByOrderByCreatedAtDesc();
        // Nếu đã có thư trong database thì lấy bức mới nhất, chưa có thì hiện câu chào ngắn
        String noiDungHienThi = letters.isEmpty() ? "Chào bạn nhỏ, chúc cậu một ngày thật vui vẻ ❤️" : letters.get(0).getContent();
        String tieuDeHienThi = letters.isEmpty() ? "Every moment with you is a blessing" : letters.get(0).getTitle();

        model.addAttribute("tenNguoiNhan", tenChi);
        model.addAttribute("tenNguoiGui", tenEm);
        model.addAttribute("loiNhanYeuThuong", noiDungHienThi);
        model.addAttribute("tieuDeThu", tieuDeHienThi);
        return "letter";
    }

    @PostMapping("/gui-chi")
    public ResponseEntity<String> handleReply(
            @RequestParam("reply_message") String replyMessage,
            @RequestParam(value = "imageFile", required = false) MultipartFile imageFile) {

        try {
            String subject = "💌 Thư mới từ " + tenChi + "!";
            StringBuilder body = new StringBuilder();
            body.append("Thư mới từ ").append(tenChi).append("!\n");
            body.append("--------------------------------\n");
            body.append("Nội dung:\n").append(replyMessage).append("\n");

            if (imageFile != null && !imageFile.isEmpty()) {
                body.append("--------------------------------\n");
                body.append("Lưu ý: Chị ấy có gửi kèm một bức ảnh!\n");
                emailService.sendEmailWithAttachment(myEmail, subject, body.toString(), imageFile);
            } else {
                emailService.sendSimpleEmail(myEmail, subject, body.toString());
            }

            return ResponseEntity.ok("Lời nhắn của chị đã được gửi đi thành công!");
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body("Đã có lỗi xảy ra.");
        }
    }

    @GetMapping("/ghep-hinh")
    public String showPuzzlePage() { return "ghep-hinh"; }

    @GetMapping("/ky-niem")
    public String showAlbumPage() { return "ky-niem"; }

    @GetMapping("/nghe-nhac")
    public String showMusicPage() { return "nhac"; }

    @GetMapping("/dem-ngay")
    public String showCountdownPage() { return "dem-ngay"; }

    static class PrizeDto {
        private String prize;
        public String getPrize() { return prize; }
        public void setPrize(String prize) { this.prize = prize; }
    }

    @GetMapping("/vong-quay")
    public String showWheelPage() { return "vong-quay"; }

    @PostMapping("/vong-quay/thong-bao")
    @ResponseBody
    public ResponseEntity<String> notifyPrize(@RequestBody PrizeDto prizeDto) {
        try {
            emailService.sendSimpleEmail(myEmail, "Trúng thưởng!", "Bạn gái trúng: " + prizeDto.getPrize());
            return ResponseEntity.ok("Thông báo đã được gửi.");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Lỗi.");
        }
    }

    @GetMapping("/mon-an") public String showFoodPickerPage() { return "mon-an"; }
    @GetMapping("/bai-hoc") public String showLessonsPage() { return "bai-hoc"; }
    @GetMapping("/tro-choi-nho") public String showMemoryGamePage() { return "tro-choi-nho"; }
    @GetMapping("/trac-nghiem") public String showQuizPage() { return "trac-nghiem"; }
    @GetMapping("/truth-or-dare") public String showTruthOrDarePage() { return "truth-or-dare"; }
    @GetMapping("/thoi-tiet") public String showWeatherPage() { return "thoi-tiet"; }
    @GetMapping("/open-when") public String showOpenWhenPage() { return "open-when"; }
    @GetMapping("/timeline") public String showTimelinePage() { return "timeline"; }
    @GetMapping("/love-map") public String showMapPage() { return "love-map"; }
    @GetMapping("/safe") public String showSafePage() { return "safe"; }
    @GetMapping("/store") public String showStorePage() { return "store"; }

    @PostMapping("/api/buy-item")
    @ResponseBody
    public ResponseEntity<String> buyItem(@RequestParam("itemName") String itemName, @RequestParam("price") int price) {
        try {
            String subject = "🚨 Đơn hàng mới từ cửa hàng!";
            String body = "Vật phẩm: " + itemName + "\nGiá: " + price + " Xu";
            emailService.sendSimpleEmail(myEmail, subject, body);
            return ResponseEntity.ok("Mua thành công!");
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Lỗi hệ thống");
        }
    }

    @GetMapping("/challenge")
    public String showChallengePage() { return "challenge"; }

    @PostMapping("/api/complete-challenge")
    @ResponseBody
    public ResponseEntity<String> completeChallenge(@RequestParam("day") int day, @RequestParam("msg") String msg) {
        LocalDate startDate = LocalDate.of(2025, 12, 1);
        LocalDate today = LocalDate.now();
        LocalDate unlockDate = startDate.plusDays(day - 1);

        if (today.isBefore(unlockDate)) {
            return ResponseEntity.badRequest().body("Chưa đến ngày này đâu bé ơi! Đừng ăn gian nha 😘");
        }
        try {
            String subject = "🎖️ Báo cáo thử thách ngày " + day;
            emailService.sendSimpleEmail(myEmail, subject, msg);
            return ResponseEntity.ok("Giỏi lắm!");
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Lỗi kết nối");
        }
    }

    private final Map<Integer, String> GIFTS = new HashMap<>() {{
        put(1, "Voucher 150k ở Thành Đô");
        put(2, "Voucher 200k ở Winmart");
        put(3, "Voucher 200k ở Ốc sên");
    }};

    @GetMapping("/christmas") public String showChristmasPage() { return "lixi"; }

    @PostMapping("/api/open-gift")
    @ResponseBody
    public ResponseEntity<String> openGift(@RequestParam("boxId") int boxId) {
        String giftName = GIFTS.getOrDefault(boxId, "Một nụ hôn nồng cháy");
        try {
            emailService.sendSimpleEmail(myEmail, "🎄 Quà Giáng Sinh", "Vợ chọn hộp " + boxId + " - Quà: " + giftName);
            return ResponseEntity.ok(giftName);
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Lỗi kết nối");
        }
    }

    @GetMapping("/game-kho") public String showHardGamePage() { return "game-kho"; }
    @GetMapping("/game-2048") public String show2048Game() { return "game-2048"; }
    @GetMapping("/game-piano") public String showPianoGame() { return "game-piano"; }
    @GetMapping("/game-snake") public String showSnakeGame() { return "game-snake"; }
    @GetMapping("/garden") public String showGardenPage() { return "garden"; }

    @PostMapping("/api/water-plant")
    @ResponseBody
    public ResponseEntity<String> waterPlant(@RequestParam("dayCount") int dayCount) {
        try {
            emailService.sendSimpleEmail(myEmail, "🌱 Vườn cây", "Đã tưới nước! Cấp độ: " + dayCount);
            return ResponseEntity.ok("Đã tưới nước thành công!");
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Lỗi kết nối");
        }
    }

    @GetMapping("/pharmacy") public String showPharmacyPage() { return "pharmacy"; }

    @PostMapping("/api/ke-don")
    @ResponseBody
    public ResponseEntity<String> prescribeMedicine(@RequestParam("symptom") String symptom) {
        try {
            emailService.sendSimpleEmail(myEmail, "🚑 Bệnh án", "Triệu chứng: " + symptom);
            return ResponseEntity.ok("Đã kê đơn!");
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Lỗi hệ thống");
        }
    }

    @GetMapping("/heart-game") public String showHeartGamePage() { return "heart-game"; }

    @PostMapping("/api/submit-score")
    @ResponseBody
    public ResponseEntity<String> submitScore(@RequestParam("score") int score) {
        try {
            if (score > 5) {
                emailService.sendSimpleEmail(myEmail, "🎮 Điểm cao Game Bắt Tim", "Điểm số đạt được: " + score);
            }
            return ResponseEntity.ok("Đã lưu điểm!");
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Lỗi mạng");
        }
    }

    @GetMapping("/catch-game") public String showCatchGamePage() { return "catch-game"; }
    @GetMapping("/ticket") public String showTicketPage() { return "ticket"; }
    @GetMapping("/tarot") public String showTarotPage() { return "tarot"; }
    @GetMapping("/kitchen") public String showKitchenPage() { return "kitchen"; }

    @PostMapping("/api/cook-bento")
    @ResponseBody
    public ResponseEntity<String> cookBento(
            @RequestParam String dishList,
            @RequestParam String message
    ) {
        String body = "🍱 Cơm vợ nấu đến rồi!\nThực đơn: " + dishList + "\nLời nhắn: \"" + message + "\"";
        emailService.sendSimpleEmail(myEmail, "🍱 Tin nhắn nấu ăn", body);
        return ResponseEntity.ok("Đã gửi thành công!");
    }

    @GetMapping("/cinema") public String showCinemaPage() { return "cinema"; }

    @PostMapping("/api/invite-movie")
    @ResponseBody
    public ResponseEntity<String> inviteMovie(@RequestParam("movieName") String movieName) {
        try {
            String body = "🍿 Phim: " + movieName + "\nLời nhắn: Phim này hay quá, xem cùng anh nhé!";
            emailService.sendSimpleEmail(myEmail, "🎬 Lời mời xem phim", body);
            return ResponseEntity.ok("Đã gửi lời mời xem phim!");
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Lỗi hệ thống");
        }
    }

    @GetMapping("/star") public String showStarPage() { return "star"; }

    @PostMapping("/api/fold-star")
    @ResponseBody
    public ResponseEntity<String> foldStar(@RequestParam("wish") String wish, @RequestParam("count") int count) {
        try {
            String body = "🔢 Ngôi sao thứ: " + count + "\nĐiều ước: " + wish;
            emailService.sendSimpleEmail(myEmail, "🌟 Ngôi sao hy vọng", body);
            return ResponseEntity.ok("Điều ước đã được gửi tới vũ trụ!");
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Lỗi kết nối");
        }
    }

    private String[] board = new String[9];
    private String turn = "X";

    @GetMapping("/game-online") public String showGamePage() { return "game-online"; }

    @MessageMapping("/move")
    @SendTo("/topic/game")
    public GameMessage processMove(GameMessage message) {
        if (message.getType().equals("RESET")) {
            board = new String[9];
            turn = "X";
            return new GameMessage("RESET", -1, "", "Ván mới bắt đầu!");
        }

        if (board[message.getIndex()] == null) {
            board[message.getIndex()] = message.getPlayer();
            turn = message.getPlayer().equals("X") ? "O" : "X";

            if (checkWin(message.getPlayer())) {
                String winner = message.getPlayer().equals("X") ? "Anh Đức" : "Vợ Yêu";
                emailService.sendSimpleEmail(myEmail, "🎮 Kết quả Cờ Caro", winner + " đã chiến thắng!");
                return new GameMessage("WIN", message.getIndex(), message.getPlayer(), "Chiến thắng!");
            }
            return message;
        }
        return null;
    }

    private boolean checkWin(String p) {
        int[][] wins = {{0, 1, 2}, {3, 4, 5}, {6, 7, 8}, {0, 3, 6}, {1, 4, 7}, {2, 5, 8}, {0, 4, 8}, {2, 4, 6}};
        for (int[] w : wins) {
            if (p.equals(board[w[0]]) && p.equals(board[w[1]]) && p.equals(board[w[2]])) return true;
        }
        return false;
    }

    @GetMapping("/adventure") public String showAdventurePage() { return "adventure"; }

    public static class MoveData {
        public String role;
        public int x;
        public int y;
        public String action;
    }

    @MessageMapping("/adventure/move")
    @SendTo("/topic/adventure")
    public MoveData syncMove(MoveData data) {
        if ("WIN".equals(data.action)) {
            emailService.sendSimpleEmail(myEmail, "🏆 Game Lửa & Nước", "Hai bạn đã phá đảo thành công!");
        }
        return data;
    }

    @Controller
    public class ShooterController {

        private static int hpBoy = 100;
        private static int hpGirl = 100;

        @GetMapping("/shooter") public String showShooterPage() { return "shooter"; }

        public static class ShooterData {
            public String role;
            public String action;
            public double y;
            public double bulletY;
            public int hpBoy;
            public int hpGirl;
        }

        @MessageMapping("/shooter/action")
        @SendTo("/topic/shooter")
        public ShooterData handleAction(ShooterData data) {
            ShooterData response = new ShooterData();
            response.role = data.role;
            response.action = data.action;
            response.y = data.y;
            response.bulletY = data.bulletY;

            switch (data.action) {
                case "RESET":
                    hpBoy = 100;
                    hpGirl = 100;
                    response.action = "SYNC_HP";
                    break;
                case "HIT":
                    if ("BOY".equals(data.role)) {
                        hpBoy = Math.max(0, hpBoy - 10);
                    } else {
                        hpGirl = Math.max(0, hpGirl - 10);
                    }
                    response.action = "SYNC_HP";
                    if (hpBoy <= 0 || hpGirl <= 0) {
                        String winner = (hpBoy <= 0) ? "Vợ Yêu" : "Anh Đức";
                        // Có thể gọi emailService ở đây nếu cần, nhưng lưu ý trong inner class cần bean EmailService hoặcAutowired
                    }
                    break;
            }
            response.hpBoy = hpBoy;
            response.hpGirl = hpGirl;
            return response;
        }
    }

    private final List<String> REWARDS = Arrays.asList(
            "💰 Ting ting 100k",
            "💄 1 Thỏi son",
            "👗 1 Cái váy mới",
            "🧋 1 Ly Trà Sữa",
            "🍗 1 Chầu Gà Rán",
            "🍕 1 Cái Pizza",
            "🎬 1 Vé xem phim"
    );

    @GetMapping("/gacha") public String showGachaPage() { return "gacha"; }

    @PostMapping("/api/gacha-pull")
    @ResponseBody
    public ResponseEntity<String> pullGacha() {
        Random rand = new Random();
        String reward = REWARDS.get(rand.nextInt(REWARDS.size()));

        String body = "Chúc mừng Vợ Yêu đã quay trúng:\n\n🎉 " + reward + " 🎉\n\nAnh Đức nhớ thực hiện ngay nhé!";
        emailService.sendSimpleEmail(myEmail, "🎰 Kết quả Gacha", body);

        return ResponseEntity.ok(reward);
    }

    @GetMapping("/cycle") public String showCalendarPage() { return "cycle"; }

    public static class CycleEvent {
        public String startDate;
        public String ovulationDate;
        public CycleEvent(String startDate, String ovulationDate) {
            this.startDate = startDate;
            this.ovulationDate = ovulationDate;
        }
    }

    @GetMapping("/api/cycle-data")
    @ResponseBody
    public ResponseEntity<List<CycleEvent>> getCycleData(@RequestParam String startDate) {
        List<CycleEvent> events = new ArrayList<>();
        LocalDate currentPeriod = LocalDate.parse(startDate);
        for (int i = 0; i < 15; i++) {
            LocalDate ovulation = currentPeriod.plusDays(14);
            events.add(new CycleEvent(currentPeriod.toString(), ovulation.toString()));
            currentPeriod = currentPeriod.plusDays(28);
        }
        return ResponseEntity.ok(events);
    }

    @GetMapping("/lixi") public String showLiXiPage() { return "lixi"; }

    static class Prize {
        String name;
        int value;
        double weight;
        public Prize(String name, int value, double weight) {
            this.name = name;
            this.value = value;
            this.weight = weight;
        }
    }

    private final List<Prize> prizes = new ArrayList<>(Arrays.asList(
            new Prize("10.000 VNĐ", 10000, 10.0),
            new Prize("20.000 VNĐ", 20000, 20.0),
            new Prize("50.000 VNĐ", 50000, 40.0),
            new Prize("100.000 VNĐ", 100000, 20.0),
            new Prize("200.000 VNĐ", 200000, 7.0),
            new Prize("500.000 VNĐ", 500000, 3.0)
    ));

    @PostMapping("/api/boc-lixi")
    @ResponseBody
    public ResponseEntity<String> getLuckyMoney() {
        double totalWeight = 0.0;
        for (Prize p : prizes) totalWeight += p.weight;

        double random = new Random().nextDouble() * totalWeight;
        Prize selectedPrize = null;

        for (Prize p : prizes) {
            random -= p.weight;
            if (random <= 0.0) {
                selectedPrize = p;
                break;
            }
        }
        if (selectedPrize == null) selectedPrize = prizes.get(0);

        String time = LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss dd/MM/yyyy"));
        String body = String.format("Người chơi: Chị Yêu\nVừa rút được: %s\nThời gian: %s", selectedPrize.name, time);

        emailService.sendSimpleEmail(myEmail, "🚨 Thông báo bốc lì xì", body);

        return ResponseEntity.ok(selectedPrize.name);
    }
}