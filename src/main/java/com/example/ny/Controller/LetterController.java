package com.example.ny.Controller;

import com.example.ny.Model.Letter;
import com.example.ny.Model.MemoryPhoto;
import com.example.ny.Model.Song;
import com.example.ny.Respository.LetterRepository;
import com.example.ny.Respository.MemoryPhotoRepository;
import com.example.ny.Respository.SongRepository;
import com.example.ny.Service.CloudinaryService;
import com.example.ny.Service.EmailService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Locale;
import java.util.Set;

@Controller
public class LetterController {
    @Autowired
    private EmailService emailService;
    private static final Set<String> ALLOWED_IMG = Set.of("jpg", "jpeg", "png", "gif", "webp");
    private static final Set<String> ALLOWED_AUDIO = Set.of("mp3", "m4a", "wav", "ogg");

    private final LetterRepository letterRepo;
    private final MemoryPhotoRepository photoRepo;
    private final SongRepository songRepo;
    private final CloudinaryService cloudinaryService;
    private final String adminPassword;

    public LetterController(LetterRepository letterRepo,
                            MemoryPhotoRepository photoRepo,
                            SongRepository songRepo,
                            CloudinaryService cloudinaryService,
                            @Value("${app.admin-password:}") String adminPassword) {
        this.letterRepo = letterRepo;
        this.photoRepo = photoRepo;
        this.songRepo = songRepo;
        this.cloudinaryService = cloudinaryService;
        this.adminPassword = adminPassword;
    }

    private boolean isAdmin(HttpSession s) {
        return Boolean.TRUE.equals(s.getAttribute("isAdmin"));
    }

    private boolean canRead(HttpSession s) {
        return Boolean.TRUE.equals(s.getAttribute("isLoggedIn")) || isAdmin(s);
    }
    private static String extOf(String name) {
        if (name == null || !name.contains(".")) return "";
        return name.substring(name.lastIndexOf('.') + 1).toLowerCase(Locale.ROOT);
    }

    // ================= TRANG QUẢN TRỊ CỦA ANH =================

    @GetMapping("/anh-viet-thu")
    public String adminPage(HttpSession session, Model model) {
        // Chưa đăng nhập đúng mật khẩu admin -> đuổi thẳng về trang /login
        if (!isAdmin(session)) {
            return "redirect:/login";
        }
        model.addAttribute("isAdmin", true);
        model.addAttribute("letters", letterRepo.findAllByOrderByCreatedAtDesc());
        model.addAttribute("photos", photoRepo.findAllByOrderByCreatedAtDesc());
        model.addAttribute("songs", songRepo.findAllByOrderByCreatedAtDesc());
        return "admin-letters";
    }

    @PostMapping("/anh-viet-thu/login")
    public String adminLogin(@RequestParam("password") String password, HttpSession session) {
        if (adminPassword != null && !adminPassword.trim().isEmpty() && adminPassword.trim().equals(password)) {
            session.setAttribute("isAdmin", true);
            session.setAttribute("isLoggedIn", true);
            return "redirect:/anh-viet-thu";
        }
        return "redirect:/login?adminErr=1";
    }

    @GetMapping("/anh-viet-thu/logout")
    public String adminLogout(HttpSession session) {
        session.invalidate();
        return "redirect:/login";
    }

    // --- 1. QUẢN LÝ THƯ ---
    @PostMapping("/anh-viet-thu/dang")
    public String postLetter(@RequestParam("title") String title,
                             @RequestParam("content") String content,
                             @RequestParam(value = "image", required = false) MultipartFile image,
                             HttpSession session) throws IOException {
        if (!isAdmin(session)) return "redirect:/anh-viet-thu";

        title = title.trim();
        content = content.trim();
        if (title.isEmpty() || content.isEmpty() || title.length() > 255 || content.length() > 20000) {
            return "redirect:/anh-viet-thu?err=letter";
        }

        Letter letter = new Letter();
        letter.setTitle(title);
        letter.setContent(content);

        if (image != null && !image.isEmpty()) {
            String ext = extOf(image.getOriginalFilename());
            if (!ALLOWED_IMG.contains(ext)) return "redirect:/anh-viet-thu?err=img";
            String secureUrl = cloudinaryService.uploadFile(image, false);
            letter.setImageFile(secureUrl);
        }

        letterRepo.save(letter);
        emailService.sendNewLetterNotification(letter.getTitle(), letter.getContent());
        return "redirect:/anh-viet-thu?tab=letters";
    }

    @PostMapping("/anh-viet-thu/xoa/{id}")
    public String deleteLetter(@PathVariable Long id, HttpSession session) {
        if (!isAdmin(session)) return "redirect:/anh-viet-thu";
        letterRepo.deleteById(id);
        return "redirect:/anh-viet-thu?tab=letters";
    }

    // --- 2. QUẢN LÝ ẢNH KỶ NIỆM ---
    @PostMapping("/anh-viet-thu/anh/them")
    public String addPhoto(@RequestParam("image") MultipartFile image,
                           @RequestParam(value = "caption", defaultValue = "") String caption,
                           @RequestParam(value = "memoryDate", defaultValue = "") String memoryDate,
                           HttpSession session) throws IOException {
        if (!isAdmin(session)) return "redirect:/anh-viet-thu";
        if (image == null || image.isEmpty()) return "redirect:/anh-viet-thu?tab=photos&err=empty";

        String ext = extOf(image.getOriginalFilename());
        if (!ALLOWED_IMG.contains(ext)) return "redirect:/anh-viet-thu?tab=photos&err=ext";

        String secureUrl = cloudinaryService.uploadFile(image, false);

        MemoryPhoto photo = new MemoryPhoto();
        photo.setImageFile(secureUrl);
        photo.setCaption(caption.trim());
        photo.setMemoryDate(memoryDate.trim());
        photoRepo.save(photo);

        return "redirect:/anh-viet-thu?tab=photos";
    }

    @PostMapping("/anh-viet-thu/anh/xoa/{id}")
    public String deletePhoto(@PathVariable Long id, HttpSession session) {
        if (!isAdmin(session)) return "redirect:/anh-viet-thu";
        photoRepo.deleteById(id);
        return "redirect:/anh-viet-thu?tab=photos";
    }

    // --- 3. QUẢN LÝ NHẠC ---
    @PostMapping("/anh-viet-thu/nhac/them")
    public String addSong(@RequestParam("title") String title,
                          @RequestParam(value = "artist", defaultValue = "") String artist,
                          @RequestParam(value = "link", defaultValue = "") String link,
                          @RequestParam(value = "audioFile", required = false) MultipartFile audioFile,
                          HttpSession session) throws IOException {
        if (!isAdmin(session)) return "redirect:/anh-viet-thu";
        if (title.isBlank()) return "redirect:/anh-viet-thu?tab=songs&err=title";

        Song song = new Song();
        song.setTitle(title.trim());
        song.setArtist(artist.trim());

        if (audioFile != null && !audioFile.isEmpty()) {
            String ext = extOf(audioFile.getOriginalFilename());
            if (!ALLOWED_AUDIO.contains(ext)) return "redirect:/anh-viet-thu?tab=songs&err=ext";
            String secureUrl = cloudinaryService.uploadFile(audioFile, true);
            song.setAudioUrl(secureUrl);
            song.setLocalFile(true);
        } else if (!link.isBlank()) {
            song.setAudioUrl(link.trim());
            song.setLocalFile(false);
        } else {
            return "redirect:/anh-viet-thu?tab=songs&err=source";
        }

        songRepo.save(song);
        return "redirect:/anh-viet-thu?tab=songs";
    }

    @PostMapping("/anh-viet-thu/nhac/xoa/{id}")
    public String deleteSong(@PathVariable Long id, HttpSession session) {
        if (!isAdmin(session)) return "redirect:/anh-viet-thu";
        songRepo.deleteById(id);
        return "redirect:/anh-viet-thu?tab=songs";
    }

    // ================= API CHO TRANG CỦA CHỊ =================

    public record LetterDto(Long id, String title, String content, String imageUrl, String createdAt) { }
    public record PhotoDto(Long id, String imageUrl, String caption, String memoryDate) { }
    public record SongDto(Long id, String title, String artist, String url) { }

    @GetMapping("/api/letters")
    @ResponseBody
    public ResponseEntity<List<LetterDto>> listLetters(HttpSession session) {
        if (!canRead(session)) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        return ResponseEntity.ok(letterRepo.findAllByOrderByCreatedAtDesc().stream()
                .map(l -> new LetterDto(l.getId(), l.getTitle(), l.getContent(), l.getImageFile(), l.getCreatedAtText()))
                .toList());
    }

    @GetMapping("/api/photos")
    @ResponseBody
    public ResponseEntity<List<PhotoDto>> listPhotos(HttpSession session) {
        if (!canRead(session)) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        return ResponseEntity.ok(photoRepo.findAllByOrderByCreatedAtDesc().stream()
                .map(p -> new PhotoDto(p.getId(), p.getImageFile(), p.getCaption(), p.getMemoryDate()))
                .toList());
    }

    @GetMapping("/api/songs")
    @ResponseBody
    public ResponseEntity<List<SongDto>> listSongs(HttpSession session) {
        if (!canRead(session)) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        return ResponseEntity.ok(songRepo.findAllByOrderByCreatedAtDesc().stream()
                .map(s -> new SongDto(s.getId(), s.getTitle(), s.getArtist(), s.getAudioUrl()))
                .toList());
    }

}