document.querySelectorAll('.edit-btn').forEach(button => {
    button.addEventListener('click', function () {
        openEditModal(
            this.dataset.id,
            this.dataset.name,
            this.dataset.value,
            this.dataset.quantity
        );
    });
});

// Edit Modal
function openEditModal(id, name, value, qty) {
    document.getElementById('editId').value = id;
    document.getElementById('editName').value = name;
    document.getElementById('editValue').value = value;
    document.getElementById('editQty').value = qty;

    const form = document.getElementById('editAssetForm');
    form.action = `/assets/${id}/edit`;

    const modal = document.getElementById('editModal');
    modal.classList.remove('hidden');
    modal.classList.add('flex');
}

function closeModal() {
    const modal = document.getElementById('editModal');
    modal.classList.add('hidden');
    modal.classList.remove('flex');
}

window.addEventListener('click', function (event) {
    const modal = document.getElementById('editModal');
    if (event.target === modal) {
        closeModal();
    }
});