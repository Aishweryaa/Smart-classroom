// Smart Classroom Management System — main.js

function toggleModal(id, show) {
    const el = document.getElementById(id);
    if (!el) return;
    if (show === undefined) {
        el.style.display = el.style.display === 'none' ? 'flex' : 'none';
    } else {
        el.style.display = show ? 'flex' : 'none';
    }
}

document.addEventListener('click', function(e) {
    if (e.target.classList.contains('modal-overlay')) e.target.style.display = 'none';
});
document.addEventListener('keydown', function(e) {
    if (e.key === 'Escape') document.querySelectorAll('.modal-overlay').forEach(m => m.style.display = 'none');
});

function filterTable(inputId, tableId) {
    const filter = document.getElementById(inputId).value.toLowerCase();
    const table  = document.getElementById(tableId);
    if (!table) return;
    table.querySelectorAll('tbody tr').forEach(row => {
        row.style.display = row.textContent.toLowerCase().includes(filter) ? '' : 'none';
    });
}

function openEditModal(id, title, description, subject, priority, status, dueDate, hours) {
    const form = document.getElementById('editTaskForm');
    if (!form) return;
    form.action = '/student/planner/update/' + id;
    const setVal = (id, v) => { const el = document.getElementById(id); if(el) el.value = (!v || v==='null') ? '' : v; };
    const setSel = (id, v) => { const el = document.getElementById(id); if(el) Array.from(el.options).forEach(o => o.selected = o.value===v); };
    setVal('editTitle', title); setVal('editDescription', description);
    setVal('editSubject', subject); setVal('editDueDate', dueDate); setVal('editHours', hours);
    setSel('editPriority', priority); setSel('editStatus', status);
    toggleModal('editTaskModal', true);
}

document.addEventListener('DOMContentLoaded', function() {
    document.querySelectorAll('.alert').forEach(alert => {
        setTimeout(() => {
            alert.style.transition = 'opacity 0.5s ease';
            alert.style.opacity = '0';
            setTimeout(() => alert.remove(), 500);
        }, 4000);
    });
});