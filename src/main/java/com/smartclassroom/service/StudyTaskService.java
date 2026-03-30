package com.smartclassroom.service;

import com.smartclassroom.entity.StudyTask;
import com.smartclassroom.entity.StudyTask.Priority;
import com.smartclassroom.entity.StudyTask.TaskStatus;
import com.smartclassroom.entity.User;
import com.smartclassroom.repository.StudyTaskRepository;
import com.smartclassroom.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.*;

/**
 * StudyTaskService - manages study tasks with Priority Queue scheduling.
 *
 * ===== ALGORITHM: Priority-Based Scheduling (Greedy Approach) =====
 *
 * Data Structure: Java PriorityQueue<StudyTask> (min-heap by default,
 *                 reversed for max-heap using Comparator.reverseOrder)
 *
 * Greedy Heuristic:
 *   A greedy algorithm makes the locally optimal choice at each step.
 *   Here, the "best" task to do next is the one with:
 *     → Highest priority weight (HIGH > MEDIUM > LOW)
 *     → Nearest deadline (fewer days remaining = more urgent)
 *
 *   Score = priorityWeight - daysUntilDue
 *     HIGH   = 30 base points
 *     MEDIUM = 20 base points
 *     LOW    = 10 base points
 *     Subtract days remaining (overdue tasks get bonus urgency)
 *
 *   The PriorityQueue orders tasks by this score (descending),
 *   so the most critical task is always at the head.
 * ================================================================
 */
@Service
@Transactional
public class StudyTaskService {

    @Autowired private StudyTaskRepository taskRepository;
    @Autowired private UserRepository userRepository;

    // ---- CRUD Operations ----

    public StudyTask createTask(StudyTask task, Long studentId) {
        User student = userRepository.findById(studentId)
                .orElseThrow(() -> new RuntimeException("Student not found: " + studentId));
        task.setStudent(student);
        return taskRepository.save(task);
    }

    public Optional<StudyTask> findById(Long id) {
        return taskRepository.findById(id);
    }

    public StudyTask updateTask(StudyTask task) {
        return taskRepository.save(task);
    }

    public void deleteTask(Long taskId) {
        taskRepository.deleteById(taskId);
    }

    /** Update only the status of a task (quick toggle) */
    public StudyTask updateStatus(Long taskId, TaskStatus newStatus) {
        StudyTask task = taskRepository.findById(taskId)
                .orElseThrow(() -> new RuntimeException("Task not found: " + taskId));
        task.setStatus(newStatus);
        return taskRepository.save(task);
    }

    // ---- Priority Queue Scheduling ----

    /**
     * Returns all PENDING and IN_PROGRESS tasks for a student,
     * ordered by priority score using a PriorityQueue (Greedy approach).
     *
     * Time Complexity:  O(n log n)  — inserting n tasks into a heap
     * Space Complexity: O(n)        — heap stores all tasks
     */
    public List<StudyTask> getScheduledTasks(Long studentId) {
        List<StudyTask> rawTasks = taskRepository.findByStudentIdOrderByDueDateAsc(studentId);

        // Build a max-heap: task with HIGHEST score is polled first
        // Comparator: compare by priorityScore descending
        PriorityQueue<StudyTask> pq = new PriorityQueue<>(
            Comparator.comparingInt(StudyTask::getPriorityScore).reversed()
        );

        // Add only non-completed tasks to the queue
        for (StudyTask t : rawTasks) {
            if (t.getStatus() != TaskStatus.COMPLETED) {
                pq.offer(t);   // O(log n) per insertion
            }
        }

        // Drain the queue in priority order
        List<StudyTask> ordered = new ArrayList<>();
        while (!pq.isEmpty()) {
            ordered.add(pq.poll());   // O(log n) per poll
        }
        return ordered;
    }

    /** All tasks (including completed) for history view */
    public List<StudyTask> getAllTasks(Long studentId) {
        return taskRepository.findByStudentIdOrderByDueDateAsc(studentId);
    }

    /** Completed tasks only */
    public List<StudyTask> getCompletedTasks(Long studentId) {
        return taskRepository.findByStudentIdAndStatus(studentId, TaskStatus.COMPLETED);
    }

    /** Tasks overdue or due today */
    public List<StudyTask> getOverdueTasks(Long studentId) {
        return taskRepository.findOverdueTasks(studentId, LocalDate.now());
    }

    // ---- Statistics ----

    public long countPending(Long studentId) {
        return taskRepository.countByStudentIdAndStatus(studentId, TaskStatus.PENDING);
    }

    public long countInProgress(Long studentId) {
        return taskRepository.countByStudentIdAndStatus(studentId, TaskStatus.IN_PROGRESS);
    }

    public long countCompleted(Long studentId) {
        return taskRepository.countByStudentIdAndStatus(studentId, TaskStatus.COMPLETED);
    }

    /**
     * Completion percentage = completed / total * 100
     */
    public double getCompletionPercentage(Long studentId) {
        long total     = taskRepository.findByStudentIdOrderByDueDateAsc(studentId).size();
        if (total == 0) return 0.0;
        long completed = countCompleted(studentId);
        return Math.round((completed * 100.0 / total) * 10.0) / 10.0;
    }
}