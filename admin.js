const API_ADMIN = 'api/livres-admin';
const API_LIVRES = 'api/livres';

let modal;
let livreEnCours = null;

document.addEventListener('DOMContentLoaded', async () => {
    modal = new bootstrap.Modal(document.getElementById('livreModal'));
    await verifierSession();
    await chargerLivres();
});

async function verifierSession() {
    try {
        const r = await fetch('api/auth/me');
        if (!r.ok) {
            window.location.href = 'login.html';
            return;
        }
        const user = await r.json();
        document.getElementById('userName').textContent = '👤 ' + (user.nom || user.login);
    } catch (e) {
        window.location.href = 'login.html';
    }
}

async function chargerLivres() {
    try {
        const r = await fetch(API_LIVRES);
        const livres = await r.json();
        const tbody = document.getElementById('livresTable');
        tbody.innerHTML = '';

        if (livres.length === 0) {
            tbody.innerHTML = '<tr><td colspan="6" class="text-center text-muted">Aucun livre</td></tr>';
            return;
        }

        livres.forEach(l => {
            tbody.innerHTML += `
                <tr>
                    <td>${l.id}</td>
                    <td>${echapper(l.titre)}</td>
                    <td>${echapper(l.auteur)}</td>
                    <td>${echapper(l.domaine || '')}</td>
                    <td>${l.annee || ''}</td>
                    <td class="text-end">
                        <button class="btn btn-sm btn-outline-primary" onclick='ouvrirFormModif(${JSON.stringify(l)})'>
                            <i class="bi bi-pencil"></i>
                        </button>
                        <button class="btn btn-sm btn-outline-danger" onclick="supprimerLivre(${l.id}, '${echapper(l.titre)}')">
                            <i class="bi bi-trash"></i>
                        </button>
                    </td>
                </tr>
            `;
        });
    } catch (e) {
        afficherMessage('Erreur chargement : ' + e.message, 'danger');
    }
}

function ouvrirFormAjout() {
    livreEnCours = null;
    document.getElementById('modalTitle').textContent = 'Ajouter un livre';
    document.getElementById('livreForm').reset();
    document.getElementById('livreId').value = '';
    document.getElementById('previewImage').innerHTML = '';
    modal.show();
}

function ouvrirFormModif(livre) {
    livreEnCours = livre;
    document.getElementById('modalTitle').textContent = 'Modifier un livre';
    document.getElementById('livreId').value = livre.id;
    document.getElementById('titre').value = livre.titre || '';
    document.getElementById('auteur').value = livre.auteur || '';
    document.getElementById('domaine').value = livre.domaine || '';
    document.getElementById('annee').value = livre.annee || '';
    document.getElementById('isbn').value = livre.isbn || '';
    document.getElementById('editeur').value = livre.editeur || '';
    document.getElementById('imageUrl').value = livre.imageUrl || '';
    document.getElementById('description').value = livre.description || '';
    document.getElementById('fichierUrl').value = livre.fichierUrl || '';

    // Aperçu image
    const preview = document.getElementById('previewImage');
    if (livre.imageUrl) {
        preview.innerHTML = `<img src="${livre.imageUrl}" 
                                   style="max-width: 150px; max-height: 200px; border-radius: 8px; box-shadow: 0 2px 8px rgba(0,0,0,0.15);"
                                   onerror="this.src='https://via.placeholder.com/150x200?text=Erreur'">`;
    } else {
        preview.innerHTML = '';
    }

    modal.show();
}

async function enregistrerLivre() {
    const data = {
        titre: document.getElementById('titre').value,
        auteur: document.getElementById('auteur').value,
        domaine: document.getElementById('domaine').value,
        annee: document.getElementById('annee').value,
        isbn: document.getElementById('isbn').value,
        editeur: document.getElementById('editeur').value,
        imageUrl: document.getElementById('imageUrl').value,
        description: document.getElementById('description').value,
        fichierUrl: document.getElementById('fichierUrl').value
    };

    if (!data.titre || !data.auteur) {
        alert('Titre et auteur obligatoires');
        return;
    }

    const id = document.getElementById('livreId').value;
    const url = id ? `${API_ADMIN}/${id}` : API_ADMIN;
    const method = id ? 'PUT' : 'POST';

    try {
        const r = await fetch(url, {
            method,
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(data)
        });

        if (r.status === 401) {
            window.location.href = 'login.html';
            return;
        }

        if (!r.ok) {
            const err = await r.json();
            throw new Error(err.error || 'Erreur');
        }

        modal.hide();
        afficherMessage(id ? '✅ Livre modifié' : '✅ Livre ajouté', 'success');
        await chargerLivres();
    } catch (e) {
        afficherMessage('Erreur : ' + e.message, 'danger');
    }
}

async function supprimerLivre(id, titre) {
    if (!confirm(`Supprimer "${titre}" ?`)) return;

    try {
        const r = await fetch(`${API_ADMIN}/${id}`, { method: 'DELETE' });

        if (r.status === 401) {
            window.location.href = 'login.html';
            return;
        }

        if (!r.ok) throw new Error('Erreur suppression');

        afficherMessage('🗑️ Livre supprimé', 'success');
        await chargerLivres();
    } catch (e) {
        afficherMessage('Erreur : ' + e.message, 'danger');
    }
}

async function deconnecter() {
    await fetch('api/auth/logout', { method: 'POST' });
    window.location.href = 'index.html';
}

function afficherMessage(texte, type) {
    const div = document.getElementById('message');
    div.textContent = texte;
    div.className = 'alert alert-' + type;
    setTimeout(() => div.classList.add('d-none'), 4000);
}

function echapper(str) {
    if (str === null || str === undefined) return '';
    return String(str).replace(/&/g, '&amp;').replace(/</g, '&lt;').replace(/>/g, '&gt;')
        .replace(/"/g, '&quot;').replace(/'/g, '&#039;');
}

// ======================================================
// UPLOAD DE FICHIERS
// ======================================================

function ouvrirUploadImage() {
    document.getElementById('fileImage').click();
}

function ouvrirUploadFichier() {
    document.getElementById('fileFichier').click();
}

async function uploadFichier(champCible, input) {
    if (!input.files || input.files.length === 0) return;

    const file = input.files[0];

    if (file.size > 10 * 1024 * 1024) {
        alert('❌ Fichier trop volumineux (max 10 MB)');
        input.value = '';
        return;
    }

    console.log('Upload de :', file.name, '(' + (file.size / 1024).toFixed(1) + ' Ko)');

    const btn = input.previousElementSibling.querySelector('button');
    const originalHtml = btn.innerHTML;
    btn.innerHTML = '<span class="spinner-border spinner-border-sm"></span>';
    btn.disabled = true;

    try {
        const formData = new FormData();
        formData.append('file', file);

        const r = await fetch('api/upload', {
            method: 'POST',
            body: formData
        });

        if (!r.ok) {
            const err = await r.json();
            throw new Error(err.error || 'Erreur upload');
        }

        const data = await r.json();
        document.getElementById(champCible).value = data.url;
        console.log('✅ Fichier uploadé :', data.url);

        if (champCible === 'imageUrl') {
            const preview = document.getElementById('previewImage');
            preview.innerHTML = `<img src="${data.url}" 
                                       style="max-width: 150px; max-height: 200px; border-radius: 8px; box-shadow: 0 2px 8px rgba(0,0,0,0.15); margin-top: 8px;"
                                       onerror="this.src='https://via.placeholder.com/150x200?text=Erreur'">`;
        }

    } catch (e) {
        alert('❌ Erreur upload : ' + e.message);
        console.error(e);
    } finally {
        btn.innerHTML = originalHtml;
        btn.disabled = false;
        input.value = '';
    }
}