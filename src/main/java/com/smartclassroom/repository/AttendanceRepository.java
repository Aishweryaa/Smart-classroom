package com.smartclassroom.repository;
import com.smartclassroom.entity.Attendance;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface AttendanceRepository extends JpaRepository<Attendance, Long> {
    List<Attendance> findByClassroomIdAndDate(Long classroomId, LocalDate date);
    List<Attendance> findByStudentIdAndClassroomId(Long studentId, Long classroomId);
    @Query("SELECT COUNT(a) FROM Attendance a WHERE a.student.id = :sid AND a.classroom.id = :cid AND a.status = 'PRESENT'")
    long countPresent(@Param("sid") Long studentId, @Param("cid") Long classroomId);
    @Query("SELECT COUNT(DISTINCT a.date) FROM Attendance a WHERE a.classroom.id = :cid")
    long countTotalSessions(@Param("cid") Long classroomId);
    Optional<Attendance> findByStudentIdAndClassroomIdAndDate(Long studentId, Long classroomId, LocalDate date);
}
