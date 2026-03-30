package com.smartclassroom.service;

import com.smartclassroom.entity.*;
import com.smartclassroom.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.*;

/**
 * AttendanceService - marks attendance, retrieves records, calculates percentages.
 */
@Service
@Transactional
public class AttendanceService {

    @Autowired private AttendanceRepository attendanceRepository;
    @Autowired private UserRepository userRepository;
    @Autowired private ClassroomRepository classroomRepository;
    @Autowired private EnrollmentRepository enrollmentRepository;

    /**
     * Mark or update attendance for a student in a class on a given date.
     * Uses upsert logic: update if record exists, create if not.
     */
    public Attendance markAttendance(Long studentId, Long classroomId,
                                     LocalDate date, Attendance.AttendanceStatus status,
                                     Long markedByFacultyId) {
        // Check for existing record (unique constraint: student + class + date)
        Optional<Attendance> existing = attendanceRepository
                .findByStudentIdAndClassroomIdAndDate(studentId, classroomId, date);

        Attendance record;
        if (existing.isPresent()) {
            record = existing.get();
            record.setStatus(status);
        } else {
            User student   = userRepository.findById(studentId)
                    .orElseThrow(() -> new RuntimeException("Student not found"));
            Classroom cls  = classroomRepository.findById(classroomId)
                    .orElseThrow(() -> new RuntimeException("Classroom not found"));
            User faculty   = userRepository.findById(markedByFacultyId)
                    .orElseThrow(() -> new RuntimeException("Faculty not found"));

            record = Attendance.builder()
                    .student(student).classroom(cls)
                    .date(date).status(status).markedBy(faculty)
                    .build();
        }
        return attendanceRepository.save(record);
    }

    /** Bulk mark attendance for an entire class on a date */
    public void markBulkAttendance(Long classroomId, LocalDate date,
                                   Map<Long, Attendance.AttendanceStatus> studentStatusMap,
                                   Long facultyId) {
        studentStatusMap.forEach((studentId, status) ->
            markAttendance(studentId, classroomId, date, status, facultyId));
    }

    /** Get all attendance records for a class on a specific date */
    public List<Attendance> getAttendanceByClassAndDate(Long classroomId, LocalDate date) {
        return attendanceRepository.findByClassroomIdAndDate(classroomId, date);
    }

    /** Get a student's full attendance history in a classroom */
    public List<Attendance> getStudentAttendance(Long studentId, Long classroomId) {
        return attendanceRepository.findByStudentIdAndClassroomId(studentId, classroomId);
    }

    /**
     * Calculate attendance percentage for a student in a classroom.
     * Returns 0 if no sessions have been held yet.
     */
    public double getAttendancePercentage(Long studentId, Long classroomId) {
        long totalSessions = attendanceRepository.countTotalSessions(classroomId);
        if (totalSessions == 0) return 0.0;
        long present = attendanceRepository.countPresent(studentId, classroomId);
        return Math.round((present * 100.0 / totalSessions) * 10.0) / 10.0;
    }

    /**
     * Build an attendance summary map for all students in a classroom.
     * Returns: { studentId -> percentage }
     */
    public Map<Long, Double> getClassAttendanceSummary(Long classroomId) {
        List<Enrollment> enrollments = enrollmentRepository.findByClassroomId(classroomId);
        Map<Long, Double> summary = new LinkedHashMap<>();
        for (Enrollment e : enrollments) {
            Long sid = e.getStudent().getId();
            summary.put(sid, getAttendancePercentage(sid, classroomId));
        }
        return summary;
    }
}