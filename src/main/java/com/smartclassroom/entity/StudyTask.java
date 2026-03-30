package com.smartclassroom.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * StudyTask - a to-do item in a student's study planner.
 *
 * Priority Queue logic (Greedy approach):
 *   Tasks are sorted by a composite score:
 *     score = priorityWeight(HIGH=3, MEDIUM=2, LOW=1)
 *             - days_until_due (fewer days = more urgent)
 *   This greedy heuristic ensures HIGH priority + nearest deadline tasks
 *   appear at the top of the planner.
 */
@Entity
@Table(name = "study_tasks")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class StudyTask {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Owner of this task */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id", nullable = false)
    private User student;

    @Column(nullable = false, length = 200)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(length = 100)
    private String subject;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Priority priority = Priority.MEDIUM;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TaskStatus status = TaskStatus.PENDING;

    @Column(name = "due_date")
    private LocalDate dueDate;

    @Column(name = "estimated_hours")
    private Integer estimatedHours = 1;

    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "updated_at")
    private LocalDateTime updatedAt = LocalDateTime.now();

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    // ---- Enums ----
    public enum Priority { HIGH, MEDIUM, LOW }
    public enum TaskStatus { PENDING, IN_PROGRESS, COMPLETED }

    /**
     * Greedy Priority Score for Priority Queue ordering.
     * Higher score = should be done sooner.
     * Formula: priorityWeight * 10 - daysUntilDue
     */
    public int getPriorityScore() {
        int weight = switch (this.priority) {
            case HIGH   -> 30;
            case MEDIUM -> 20;
            case LOW    -> 10;
        };
        if (this.dueDate != null) {
            long daysLeft = java.time.temporal.ChronoUnit.DAYS.between(LocalDate.now(), this.dueDate);
            // Negative days (overdue) increase urgency significantly
            weight -= (int) Math.max(daysLeft, -10);
        }
        return weight;
    }
}