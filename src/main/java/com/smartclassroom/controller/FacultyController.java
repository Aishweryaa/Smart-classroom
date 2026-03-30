package com.smartclassroom.controller;

import com.smartclassroom.entity.*;
import com.smartclassroom.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.*;

@Controller
@RequestMapping("/faculty")
public class FacultyController {

    @Autowired private UserService         userService;
    @Autowired private ClassroomService    classroomService;
    @Autowired private AttendanceService   attendanceService;
    @Autowired private AnnouncementService announcementService;

    private User getCurrentUser(UserDetails p) {
        return userService.findByUsername(p.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    // =================== DASHBOARD ===================

    @GetMapping("/dashboard")
    public String dashboard(@AuthenticationPrincipal UserDetails principal, Model model) {
        User faculty = getCurrentUser(principal);
        List<Classroom> classes = classroomService.findByFaculty(faculty.getId());
        model.addAttribute("faculty",   faculty);
        model.addAttribute("classes",   classes);
        model.addAttribute("globalAnn", announcementService.getGlobalAnnouncements());
        return "faculty/dashboard";
    }

    // =================== CLASSES ===================

    @GetMapping("/classes")
    public String classes(@AuthenticationPrincipal UserDetails principal, Model model) {
        User faculty = getCurrentUser(principal);
        model.addAttribute("faculty", faculty);
        model.addAttribute("classes", classroomService.findByFaculty(faculty.getId()));
        return "faculty/classes";
    }

    @GetMapping("/classes/{id}")
    public String classDetail(@PathVariable Long id,
                              @AuthenticationPrincipal UserDetails principal,
                              Model model) {
        User faculty  = getCurrentUser(principal);
        Classroom cls = classroomService.findById(id)
                .orElseThrow(() -> new RuntimeException("Class not found"));

        List<Enrollment>   enrollments   = classroomService.getEnrollmentsByClassroom(id);
        List<Announcement> announcements = announcementService.getByClassroom(id);
        List<Schedule>     schedules     = classroomService.getSchedulesByClassroom(id);

        model.addAttribute("faculty",       faculty);
        model.addAttribute("classroom",     cls);
        model.addAttribute("enrollments",   enrollments);
        model.addAttribute("announcements", announcements);
        model.addAttribute("schedules",     schedules);
        return "faculty/class-detail";
    }

    // =================== SCHEDULE ===================

    @PostMapping("/schedule/add")
    public String addSchedule(@RequestParam Long   classroomId,
                              @RequestParam String dayOfWeek,
                              @RequestParam String startTime,
                              @RequestParam String endTime,
                              @RequestParam(required = false) String roomNumber,
                              RedirectAttributes ra) {
        try {
            Classroom cls = classroomService.findById(classroomId)
                    .orElseThrow(() -> new RuntimeException("Classroom not found"));

            Schedule schedule = Schedule.builder()
                    .classroom(cls)
                    .dayOfWeek(Schedule.DayOfWeek.valueOf(dayOfWeek))
                    .startTime(LocalTime.parse(startTime))
                    .endTime(LocalTime.parse(endTime))
                    .roomNumber(roomNumber)
                    .build();

            classroomService.addSchedule(schedule);
            ra.addFlashAttribute("successMsg",
                    "Schedule added: " + dayOfWeek + " " + startTime + " - " + endTime);
        } catch (Exception e) {
            ra.addFlashAttribute("errorMsg", "Error adding schedule: " + e.getMessage());
        }
        return "redirect:/faculty/classes/" + classroomId;
    }

    @PostMapping("/schedule/delete/{id}")
    public String deleteSchedule(@PathVariable Long id,
                                 @RequestParam Long classroomId,
                                 RedirectAttributes ra) {
        try {
            classroomService.deleteSchedule(id);
            ra.addFlashAttribute("successMsg", "Schedule removed.");
        } catch (Exception e) {
            ra.addFlashAttribute("errorMsg", "Error: " + e.getMessage());
        }
        return "redirect:/faculty/classes/" + classroomId;
    }

    // =================== ATTENDANCE ===================

    @GetMapping("/attendance/{classId}")
    public String attendancePage(@PathVariable Long classId,
                                 @RequestParam(required = false)
                                 @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
                                 @AuthenticationPrincipal UserDetails principal,
                                 Model model) {
        User faculty  = getCurrentUser(principal);
        Classroom cls = classroomService.findById(classId)
                .orElseThrow(() -> new RuntimeException("Class not found"));

        if (date == null) date = LocalDate.now();

        List<Enrollment> enrollments = classroomService.getEnrollmentsByClassroom(classId);
        List<Attendance> existing    = attendanceService.getAttendanceByClassAndDate(classId, date);

        Map<Long, Attendance.AttendanceStatus> currentStatus = new HashMap<>();
        existing.forEach(a -> currentStatus.put(a.getStudent().getId(), a.getStatus()));

        model.addAttribute("faculty",       faculty);
        model.addAttribute("classroom",     cls);
        model.addAttribute("enrollments",   enrollments);
        model.addAttribute("currentStatus", currentStatus);
        model.addAttribute("date",          date);
        model.addAttribute("statuses",      Attendance.AttendanceStatus.values());
        return "faculty/attendance";
    }

    @PostMapping("/attendance/{classId}")
    public String markAttendance(@PathVariable Long classId,
                                 @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
                                     LocalDate date,
                                 @RequestParam Map<String, String> params,
                                 @AuthenticationPrincipal UserDetails principal,
                                 RedirectAttributes ra) {
        User faculty = getCurrentUser(principal);

        Map<Long, Attendance.AttendanceStatus> statusMap = new HashMap<>();
        params.forEach((key, val) -> {
            if (key.startsWith("status_")) {
                Long studentId = Long.parseLong(key.replace("status_", ""));
                statusMap.put(studentId, Attendance.AttendanceStatus.valueOf(val));
            }
        });

        attendanceService.markBulkAttendance(classId, date, statusMap, faculty.getId());
        ra.addFlashAttribute("successMsg", "Attendance saved for " + date);
        return "redirect:/faculty/attendance/" + classId + "?date=" + date;
    }

    // =================== ANNOUNCEMENTS ===================

    @PostMapping("/announcements/post")
    public String postAnnouncement(@RequestParam String title,
                                   @RequestParam String content,
                                   @RequestParam(required = false) Long classroomId,
                                   @AuthenticationPrincipal UserDetails principal,
                                   RedirectAttributes ra) {
        User faculty = getCurrentUser(principal);
        if (classroomId != null) {
            announcementService.postClassAnnouncement(title, content, faculty.getId(), classroomId);
            ra.addFlashAttribute("successMsg", "Announcement posted to class.");
            return "redirect:/faculty/classes/" + classroomId;
        } else {
            announcementService.postGlobalAnnouncement(title, content, faculty.getId());
            ra.addFlashAttribute("successMsg", "Global announcement posted.");
            return "redirect:/faculty/dashboard";
        }
    }
}
