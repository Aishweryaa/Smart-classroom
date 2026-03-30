package com.smartclassroom.controller;

import com.smartclassroom.entity.*;
import com.smartclassroom.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.*;

@Controller
@RequestMapping("/student")
public class StudentController {

    @Autowired private UserService         userService;
    @Autowired private ClassroomService    classroomService;
    @Autowired private AttendanceService   attendanceService;
    @Autowired private AnnouncementService announcementService;
    @Autowired private StudyTaskService    studyTaskService;

    private User getCurrentUser(UserDetails principal) {
        return userService.findByUsername(principal.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    // =================== DASHBOARD ===================

    @GetMapping("/dashboard")
    public String dashboard(@AuthenticationPrincipal UserDetails principal, Model model) {
        User student = getCurrentUser(principal);
        List<Classroom> classes = classroomService.findByStudent(student.getId());

        model.addAttribute("student",       student);
        model.addAttribute("classes",       classes);
        model.addAttribute("announcements", announcementService.getAnnouncementsForStudent(student.getId()));
        model.addAttribute("overdueTasks",  studyTaskService.getOverdueTasks(student.getId()));
        model.addAttribute("pendingCount",  studyTaskService.countPending(student.getId()));
        model.addAttribute("completedPct",  studyTaskService.getCompletionPercentage(student.getId()));
        return "student/dashboard";
    }

    // =================== CLASSES ===================

    @GetMapping("/classes")
    public String classes(@AuthenticationPrincipal UserDetails principal, Model model) {
        User student = getCurrentUser(principal);
        List<Classroom> classes = classroomService.findByStudent(student.getId());

        // Build schedules map: classroomId -> List<Schedule>
        Map<Long, List<Schedule>> schedulesByClass = new HashMap<>();
        for (Classroom cls : classes) {
            schedulesByClass.put(cls.getId(),
                classroomService.getSchedulesByClassroom(cls.getId()));
        }

        model.addAttribute("student",         student);
        model.addAttribute("classes",         classes);
        model.addAttribute("schedulesByClass", schedulesByClass);
        return "student/classes";
    }

    // =================== ATTENDANCE ===================

    @GetMapping("/attendance")
    public String attendance(@AuthenticationPrincipal UserDetails principal,
                             @RequestParam(required = false) Long classId,
                             Model model) {
        User student = getCurrentUser(principal);
        List<Classroom> classes = classroomService.findByStudent(student.getId());
        model.addAttribute("student", student);
        model.addAttribute("classes", classes);

        if (classId != null) {
            List<Attendance> records =
                    attendanceService.getStudentAttendance(student.getId(), classId);
            double percentage =
                    attendanceService.getAttendancePercentage(student.getId(), classId);
            model.addAttribute("records",    records);
            model.addAttribute("percentage", percentage);
            model.addAttribute("selectedId", classId);
        }
        return "student/attendance";
    }

    // =================== STUDY PLANNER ===================

    @GetMapping("/planner")
    public String planner(@AuthenticationPrincipal UserDetails principal, Model model) {
        User student = getCurrentUser(principal);
        model.addAttribute("student",         student);
        model.addAttribute("scheduledTasks",  studyTaskService.getScheduledTasks(student.getId()));
        model.addAttribute("completedTasks",  studyTaskService.getCompletedTasks(student.getId()));
        model.addAttribute("pendingCount",    studyTaskService.countPending(student.getId()));
        model.addAttribute("inProgressCount", studyTaskService.countInProgress(student.getId()));
        model.addAttribute("completedCount",  studyTaskService.countCompleted(student.getId()));
        model.addAttribute("completedPct",    studyTaskService.getCompletionPercentage(student.getId()));
        model.addAttribute("newTask",         new StudyTask());
        return "student/planner";
    }

    @PostMapping("/planner/add")
    public String addTask(@AuthenticationPrincipal UserDetails principal,
                          @ModelAttribute("newTask") StudyTask task,
                          RedirectAttributes ra) {
        User student = getCurrentUser(principal);
        studyTaskService.createTask(task, student.getId());
        ra.addFlashAttribute("successMsg", "Task added successfully!");
        return "redirect:/student/planner";
    }

    @PostMapping("/planner/update/{id}")
    public String updateTask(@PathVariable Long id,
                             @ModelAttribute StudyTask updatedTask,
                             @AuthenticationPrincipal UserDetails principal,
                             RedirectAttributes ra) {
        StudyTask existing = studyTaskService.findById(id)
                .orElseThrow(() -> new RuntimeException("Task not found"));
        User student = getCurrentUser(principal);
        if (!existing.getStudent().getId().equals(student.getId())) {
            ra.addFlashAttribute("errorMsg", "Unauthorized.");
            return "redirect:/student/planner";
        }
        existing.setTitle(updatedTask.getTitle());
        existing.setDescription(updatedTask.getDescription());
        existing.setSubject(updatedTask.getSubject());
        existing.setPriority(updatedTask.getPriority());
        existing.setStatus(updatedTask.getStatus());
        existing.setDueDate(updatedTask.getDueDate());
        existing.setEstimatedHours(updatedTask.getEstimatedHours());
        studyTaskService.updateTask(existing);
        ra.addFlashAttribute("successMsg", "Task updated.");
        return "redirect:/student/planner";
    }

    @PostMapping("/planner/status/{id}")
    public String updateStatus(@PathVariable Long id,
                               @RequestParam StudyTask.TaskStatus status,
                               RedirectAttributes ra) {
        studyTaskService.updateStatus(id, status);
        ra.addFlashAttribute("successMsg", "Status updated.");
        return "redirect:/student/planner";
    }

    @PostMapping("/planner/delete/{id}")
    public String deleteTask(@PathVariable Long id,
                             @AuthenticationPrincipal UserDetails principal,
                             RedirectAttributes ra) {
        StudyTask task = studyTaskService.findById(id)
                .orElseThrow(() -> new RuntimeException("Task not found"));
        User student = getCurrentUser(principal);
        if (!task.getStudent().getId().equals(student.getId())) {
            ra.addFlashAttribute("errorMsg", "Unauthorized.");
            return "redirect:/student/planner";
        }
        studyTaskService.deleteTask(id);
        ra.addFlashAttribute("successMsg", "Task deleted.");
        return "redirect:/student/planner";
    }
}
