package com.smartclassroom.repository;
import com.smartclassroom.entity.Classroom;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ClassroomRepository extends JpaRepository<Classroom, Long> {
    List<Classroom> findByFacultyId(Long facultyId);
    @Query("SELECT e.classroom FROM Enrollment e WHERE e.student.id = :studentId")
    List<Classroom> findByStudentId(@Param("studentId") Long studentId);
    boolean existsByClassCode(String classCode);
}
