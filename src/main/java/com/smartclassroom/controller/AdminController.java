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

import java.util.List;

@Controller
@RequestMapping("/admin")
public class AdminController {

    @Autowired private UserService         userService;
    @Autowired private ClassroomService    classroomService;
    @Autowired private AnnouncementService announcementService;
    @Autowired private AttendanceService   attendanceService;

    private User getCurrentUser(UserDetails p) {
        return userService.findByUsername(p.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    // =================== DASHBOARD =================== 

    @GetMapping("/dashboard")
    public String dashboard(@AuthenticationPrincipal UserDetails principal, Model model) {
        model.addAttribute("admin",         getCurrentUser(principal));
        model.addAttribute("totalUsers",    userService.countAll());
        model.addAttribute("totalStudents", userService.countStudents());
        model.addAttribute("totalFaculty",  userService.countFaculty());
        model.addAttribute("totalClasses",  classroomService.countAll());
        model.addAttribute("recentAnn",     announcementService.getAll());
        model.addAttribute("recentUsers",   userService.findAll());
        return "admin/dashboard";
    }

    // =================== USERS ===================

    @GetMapping("/users")
    public String manageUsers(Model model) {
        model.addAttribute("users", userService.findAll());
        return "admin/users";
    }

    @PostMapping("/users/toggle/{id}")
    public String toggleUser(@PathVariable Long id, RedirectAttributes ra) {
        User u = userService.toggleUserStatus(id);
        ra.addFlashAttribute("successMsg",
                "User " + u.getUsername() + " is now " + (u.isEnabled() ? "ENABLED" : "DISABLED"));
        return "redirect:/admin/users";
    }

    @PostMapping("/users/delete/{id}")
    public String deleteUser(@PathVariable Long id, RedirectAttributes ra) {
        userService.deleteUser(id);
        ra.addFlashAttribute("successMsg", "User deleted.");
        return "redirect:/admin/users";
    }

    @GetMapping("/users/new")
    public String newUserForm(Model model) {
        model.addAttribute("user",  new User());
        model.addAttribute("roles", User.Role.values());
        return "admin/new-user";
    }

    @PostMapping("/users/new")
    public String createUser(@ModelAttribute User user, RedirectAttributes ra) {
        try {
            userService.register(user);
            ra.addFlashAttribute("successMsg", "User created: " + user.getUsername());
        } catch (RuntimeException e) {
            ra.addFlashAttribute("errorMsg", e.getMessage());
        }
        return "redirect:/admin/users";
    }

    // =================== CLASSROOMS ===================

    @GetMapping("/classrooms")
    public String manageClassrooms(Model model) {
        model.addAttribute("classrooms", classroomService.findAll());
        model.addAttribute("faculty",    userService.findAllFaculty());
        model.addAttribute("newClass",   new Classroom());
        return "admin/classrooms";
    }

    @PostMapping("/classrooms/add")
    public String addClass(@ModelAttribute Classroom classroom,
                           @RequestParam Long facultyId,
                           RedirectAttributes ra) {
        try {
            User faculty = userService.findById(facultyId)
                    .orElseThrow(() -> new RuntimeException("Faculty not found"));
            classroom.setFaculty(faculty);
            classroomService.createClassroom(classroom);
            ra.addFlashAttribute("successMsg", "Classroom '" + classroom.getClassName() + "' created.");
        } catch (RuntimeException e) {
            ra.addFlashAttribute("errorMsg", e.getMessage());
        }
        return "redirect:/admin/classrooms";
    }

    @PostMapping("/classrooms/delete/{id}")
    public String deleteClass(@PathVariable Long id, RedirectAttributes ra) {
        classroomService.delete(id);
        ra.addFlashAttribute("successMsg", "Classroom deleted.");
        return "redirect:/admin/classrooms";
    }

    /**
     * Dedicated page to manage students for ONE classroom.
     * Avoids all complex map/lazy-loading issues in the main classrooms page.
     */
    @GetMapping("/classrooms/manage/{classroomId}")
    public String manageStudents(@PathVariable Long classroomId, Model model) {
        Classroom cls = classroomService.findById(classroomId)
                .orElseThrow(() -> new RuntimeException("Classroom not found"));

        // Get enrolled students as plain User objects
        List<Enrollment> enrollments = classroomService.getEnrollmentsByClassroom(classroomId);
        List<User> enrolledStudents = enrollments.stream()
                .map(Enrollment::getStudent)
                .toList();

        model.addAttribute("classroomId",     classroomId);
        model.addAttribute("className",       cls.getClassName());
        model.addAttribute("classCode",       cls.getClassCode());
        model.addAttribute("enrolledStudents",enrolledStudents);
        model.addAttribute("allStudents",     userService.findAllStudents());
        return "admin/manage-students";
    }

    @PostMapping("/classrooms/enroll")
    public String enrollStudent(@RequestParam Long studentId,
                                @RequestParam Long classroomId,
                                RedirectAttributes ra) {
        try {
            classroomService.enrollStudent(studentId, classroomId);
            User student  = userService.findById(studentId)
                    .orElseThrow(() -> new RuntimeException("Student not found"));
            Classroom cls = classroomService.findById(classroomId)
                    .orElseThrow(() -> new RuntimeException("Classroom not found"));
            ra.addFlashAttribute("successMsg",
                    student.getFullName() + " enrolled in " + cls.getClassName() + " successfully!");
        } catch (RuntimeException e) {
            ra.addFlashAttribute("errorMsg", e.getMessage());
        }
        return "redirect:/admin/classrooms/manage/" + classroomId;
    }

    @PostMapping("/classrooms/unenroll")
    public String unenrollStudent(@RequestParam Long studentId,
                                  @RequestParam Long classroomId,
                                  RedirectAttributes ra) {
        try {
            classroomService.unenrollStudent(studentId, classroomId);
            ra.addFlashAttribute("successMsg", "Student removed from classroom.");
        } catch (RuntimeException e) {
            ra.addFlashAttribute("errorMsg", e.getMessage());
        }
        return "redirect:/admin/classrooms/manage/" + classroomId;
    }

    // =================== ANNOUNCEMENTS ===================

    @PostMapping("/announcements/post")
    public String postGlobalAnn(@RequestParam String title,
                                @RequestParam String content,
                                @AuthenticationPrincipal UserDetails principal,
                                RedirectAttributes ra) {
        User admin = getCurrentUser(principal);
        announcementService.postGlobalAnnouncement(title, content, admin.getId());
        ra.addFlashAttribute("successMsg", "Global announcement posted.");
        return "redirect:/admin/dashboard";
    }

    @PostMapping("/announcements/delete/{id}")
    public String deleteAnn(@PathVariable Long id, RedirectAttributes ra) {
        announcementService.delete(id);
        ra.addFlashAttribute("successMsg", "Announcement deleted.");
        return "redirect:/admin/dashboard";
    }

    // =================== REPORTS ===================

    @GetMapping("/reports")
    public String reports(Model model) {
        model.addAttribute("classrooms", classroomService.findAll());
        return "admin/reports";
    }

    @GetMapping("/reports/attendance/{classId}")
    public String attendanceReport(@PathVariable Long classId, Model model) {
        Classroom cls = classroomService.findById(classId)
                .orElseThrow(() -> new RuntimeException("Class not found"));
        List<Enrollment> enrollments = classroomService.getEnrollmentsByClassroom(classId);

        var report = enrollments.stream().map(e -> {
            Long sid = e.getStudent().getId();
            return new Object[]{
                e.getStudent(),
                attendanceService.getAttendancePercentage(sid, classId)
            };
        }).toList();

        model.addAttribute("classroom",   cls);
        model.addAttribute("report",      report);
        model.addAttribute("enrollments", enrollments);
        return "admin/attendance-report";
    }
}
