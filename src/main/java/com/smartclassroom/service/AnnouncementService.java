package com.smartclassroom.service;

import com.smartclassroom.entity.*;
import com.smartclassroom.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@Transactional
public class AnnouncementService {

    @Autowired private AnnouncementRepository announcementRepository;
    @Autowired private UserRepository         userRepository;
    @Autowired private ClassroomRepository    classroomRepository;
    @Autowired private EnrollmentRepository   enrollmentRepository;

    public Announcement postClassAnnouncement(String title, String content,
                                              Long postedById, Long classroomId) {
        User poster   = userRepository.findById(postedById)
                .orElseThrow(() -> new RuntimeException("User not found"));
        Classroom cls = classroomRepository.findById(classroomId)
                .orElseThrow(() -> new RuntimeException("Classroom not found"));

        Announcement ann = Announcement.builder()
                .title(title).content(content)
                .postedBy(poster).classroom(cls)
                .createdAt(LocalDateTime.now())
                .build();
        return announcementRepository.save(ann);
    }

    public Announcement postGlobalAnnouncement(String title, String content, Long postedById) {
        User poster = userRepository.findById(postedById)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Announcement ann = Announcement.builder()
                .title(title).content(content)
                .postedBy(poster).classroom(null)
                .createdAt(LocalDateTime.now())
                .build();
        return announcementRepository.save(ann);
    }

    public List<Announcement> getAnnouncementsForStudent(Long studentId) {
        List<Announcement> all = new ArrayList<>(
                announcementRepository.findByClassroomIsNullOrderByCreatedAtDesc());

        enrollmentRepository.findByStudentId(studentId).forEach(e ->
            all.addAll(announcementRepository
                    .findByClassroomIdOrderByCreatedAtDesc(e.getClassroom().getId())));

        // Null-safe sort: treat null createdAt as epoch (sort to end)
        all.sort((a, b) -> {
            LocalDateTime ta = a.getCreatedAt() != null ? a.getCreatedAt() : LocalDateTime.MIN;
            LocalDateTime tb = b.getCreatedAt() != null ? b.getCreatedAt() : LocalDateTime.MIN;
            return tb.compareTo(ta);
        });
        return all;
    }

    public List<Announcement> getByClassroom(Long classroomId) {
        return announcementRepository.findByClassroomIdOrderByCreatedAtDesc(classroomId);
    }

    public List<Announcement> getGlobalAnnouncements() {
        return announcementRepository.findByClassroomIsNullOrderByCreatedAtDesc();
    }

    public List<Announcement> getAll() {
        return announcementRepository.findAllByOrderByCreatedAtDesc();
    }

    public void delete(Long id) {
        announcementRepository.deleteById(id);
    }
}
