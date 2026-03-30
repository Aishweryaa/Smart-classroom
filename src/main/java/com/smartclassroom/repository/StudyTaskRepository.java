package com.smartclassroom.repository;
import com.smartclassroom.entity.StudyTask;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.time.LocalDate;
import java.util.List;

@Repository
public interface StudyTaskRepository extends JpaRepository<StudyTask, Long> {
    List<StudyTask> findByStudentIdOrderByDueDateAsc(Long studentId);
    List<StudyTask> findByStudentIdAndStatus(Long studentId, StudyTask.TaskStatus status);
    List<StudyTask> findByStudentIdAndPriority(Long studentId, StudyTask.Priority priority);
    @Query("SELECT COUNT(t) FROM StudyTask t WHERE t.student.id = :sid AND t.status = :status")
    long countByStudentIdAndStatus(@Param("sid") Long studentId, @Param("status") StudyTask.TaskStatus status);
    @Query("SELECT t FROM StudyTask t WHERE t.student.id = :sid AND t.dueDate <= :today AND t.status != 'COMPLETED'")
    List<StudyTask> findOverdueTasks(@Param("sid") Long studentId, @Param("today") LocalDate today);
}
