package com.smartclassroom.repository;
import com.smartclassroom.entity.Enrollment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface EnrollmentRepository extends JpaRepository<Enrollment, Long> {
    List<Enrollment> findByClassroomId(Long classroomId);
    List<Enrollment> findByStudentId(Long studentId);
    boolean existsByStudentIdAndClassroomId(Long studentId, Long classroomId);
    void deleteByStudentIdAndClassroomId(Long studentId, Long classroomId);
    @Query("SELECT COUNT(e) FROM Enrollment e WHERE e.classroom.id = :classroomId")
    long countStudentsInClassroom(@Param("classroomId") Long classroomId);
}
