package com.smartclassroom.service;

import com.smartclassroom.entity.*;
import com.smartclassroom.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * ClassroomService - manages classroom CRUD, enrollment, and schedules.
 */
@Service
@Transactional
public class ClassroomService {

    @Autowired private ClassroomRepository classroomRepository;
    @Autowired private EnrollmentRepository enrollmentRepository;
    @Autowired private ScheduleRepository scheduleRepository;
    @Autowired private UserRepository userRepository;

    // ---- Classroom CRUD ----

    public Classroom createClassroom(Classroom classroom) {
        if (classroomRepository.existsByClassCode(classroom.getClassCode())) {
            throw new RuntimeException("Class code already exists: " + classroom.getClassCode());
        }
        return classroomRepository.save(classroom);
    }

    public Optional<Classroom> findById(Long id) {
        return classroomRepository.findById(id);
    }

    public List<Classroom> findAll() {
        return classroomRepository.findAll();
    }

    /** Classrooms taught by a specific faculty member */
    public List<Classroom> findByFaculty(Long facultyId) {
        return classroomRepository.findByFacultyId(facultyId);
    }

    /** Classrooms a student is enrolled in */
    public List<Classroom> findByStudent(Long studentId) {
        return classroomRepository.findByStudentId(studentId);
    }

    public Classroom update(Classroom classroom) {
        return classroomRepository.save(classroom);
    }

    public void delete(Long id) {
        classroomRepository.deleteById(id);
    }

    // ---- Enrollment ----

    public Enrollment enrollStudent(Long studentId, Long classroomId) {
        if (enrollmentRepository.existsByStudentIdAndClassroomId(studentId, classroomId)) {
            throw new RuntimeException("Student already enrolled in this class.");
        }
        User student   = userRepository.findById(studentId)
                .orElseThrow(() -> new RuntimeException("Student not found"));
        Classroom cls  = classroomRepository.findById(classroomId)
                .orElseThrow(() -> new RuntimeException("Classroom not found"));

        Enrollment enrollment = Enrollment.builder()
                .student(student).classroom(cls).build();
        return enrollmentRepository.save(enrollment);
    }

    public void unenrollStudent(Long studentId, Long classroomId) {
        enrollmentRepository.deleteByStudentIdAndClassroomId(studentId, classroomId);
    }

    public List<Enrollment> getEnrollmentsByClassroom(Long classroomId) {
        return enrollmentRepository.findByClassroomId(classroomId);
    }

    public long countStudentsInClassroom(Long classroomId) {
        return enrollmentRepository.countStudentsInClassroom(classroomId);
    }

    // ---- Schedule ----

    public Schedule addSchedule(Schedule schedule) {
        return scheduleRepository.save(schedule);
    }

    public List<Schedule> getSchedulesByClassroom(Long classroomId) {
        return scheduleRepository.findByClassroomId(classroomId);
    }

    public void deleteSchedule(Long scheduleId) {
        scheduleRepository.deleteById(scheduleId);
    }

    public long countAll() {
        return classroomRepository.count();
    }
}