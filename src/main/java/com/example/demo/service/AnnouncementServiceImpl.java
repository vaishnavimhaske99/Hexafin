
package com.example.demo.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.demo.dto.AnnouncementRequestDTO;
import com.example.demo.dto.AnnouncementResponseDTO;
import com.example.demo.entity.Announcement;
import com.example.demo.entity.AnnouncementRecipient;
import com.example.demo.entity.User;
import com.example.demo.mapper.AnnouncementMapper;
import com.example.demo.repository.AnnouncementRecipientRepository;
import com.example.demo.repository.AnnouncementRepository;
import com.example.demo.repository.UserRepository;
import com.example.demo.service.AnnouncementService;

@Service
public class AnnouncementServiceImpl implements AnnouncementService {

    private final AnnouncementRepository announcementRepository;
    private final AnnouncementRecipientRepository recipientRepository;
    private final UserRepository userRepository;
    private final AnnouncementMapper announcementMapper;

    public AnnouncementServiceImpl(
            AnnouncementRepository announcementRepository,
            AnnouncementRecipientRepository recipientRepository,
            UserRepository userRepository,
            AnnouncementMapper announcementMapper) {

        this.announcementRepository = announcementRepository;
        this.recipientRepository = recipientRepository;
        this.userRepository = userRepository;
        this.announcementMapper = announcementMapper;
    }

    @Override
    @Transactional
    public AnnouncementResponseDTO createAnnouncement(
            AnnouncementRequestDTO request,
            Long adminId) {

        User admin = userRepository.findById(adminId)
                .orElseThrow(() -> new RuntimeException("Admin not found"));

        Announcement announcement = new Announcement();
        announcement.setTitle(request.getTitle());
        announcement.setMessage(request.getMessage());
        announcement.setCreatedBy(admin);

        Announcement savedAnnouncement =
                announcementRepository.save(announcement);

        List<User> users = userRepository.findAll();

        for (User user : users) {

            // Sender ला स्वतःचा broadcast recipient म्हणून जोडत नाही.
            if (user.getId().equals(adminId)) {
                continue;
            }

            if (!"ACTIVE".equalsIgnoreCase(user.getStatus())) {
                continue;
            }

            AnnouncementRecipient recipient =
                    new AnnouncementRecipient();

            recipient.setAnnouncement(savedAnnouncement);
            recipient.setUser(user);
            recipient.setRead(false);

            recipientRepository.save(recipient);
        }

        return announcementMapper.toResponseDTO(
                savedAnnouncement, null);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AnnouncementResponseDTO> getMyAnnouncements(Long userId) {

        List<AnnouncementResponseDTO> announcements = new java.util.ArrayList<>();

        // User ला मिळालेल्या announcements
        recipientRepository
                .findByUser_IdOrderByAnnouncement_CreatedAtDesc(userId)
                .forEach(recipient -> announcements.add(
                        announcementMapper.toResponseDTO(
                                recipient.getAnnouncement(), recipient)));

        // User ने स्वतः publish केलेल्या announcements
        announcementRepository
                .findByCreatedBy_IdOrderByCreatedAtDesc(userId)
                .forEach(announcement -> announcements.add(
                        announcementMapper.toResponseDTO(announcement, null)));

        return announcements;
    }

    @Override
    @Transactional
    public void markAsRead(Long announcementId, Long userId) {

        AnnouncementRecipient recipient = recipientRepository
                .findByAnnouncement_IdAndUser_Id(announcementId, userId)
                .orElseThrow(() ->
                        new RuntimeException("Announcement not found"));

        if (!recipient.isRead()) {
            recipient.setRead(true);
            recipient.setReadAt(java.time.LocalDateTime.now());
            recipientRepository.save(recipient);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public long getUnreadCount(Long userId) {

        return recipientRepository
                .countByUser_IdAndIsReadFalse(userId);
    }
}
