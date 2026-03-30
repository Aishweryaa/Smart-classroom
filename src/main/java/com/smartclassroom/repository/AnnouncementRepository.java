package com.smartclassroom.repository;
import com.smartclassroom.entity.Announcement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface AnnouncementRepository extends JpaRepository<Announcement, Long> {
    List<Announcement> findByClassroomIsNullOrderByCreatedAtDesc();
    List<Announcement> findByClassroomIdOrderByCreatedAtDesc(Long classroomId);
    List<Announcement> findAllByOrderByCreatedAtDesc();
}
