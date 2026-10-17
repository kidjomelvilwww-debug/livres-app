const API_SEARCH = 'api/search';
const API_LIVRES = 'api/livres';

let tousLesLivres = [];

document.addEventListener('DOMContentLoaded', () => {
    chargerLivres();
    brancherEvenements();
});

async function chargerLivres() {
    try {
        const response = await fetch(API_LIVRES);
        tousLesLivres = await response.json();
        afficherLivres(tousLesLivres);
    } catch (error) {
        console.error('Erreur :', error);
        document.getElementById('resultatsCount').innerHTML =
            '<span class="text-danger">Erreur de chargement</span>';
    }
}

function afficherLivres(livres) {
    const grid = document.getElementById('livresGrid');
    const noResults = document.getElementById('noResults');
    const count = document.getElementById('resultatsCount');

    grid.innerHTML = '';

    if (livres.length === 0) {
        noResults.classList.remove('d-none');
        count.innerHTML = '<i class="bi bi-info-circle"></i> Aucun résultat';
        return;
    }

    noResults.classList.add('d-none');
    count.innerHTML = `<i class="bi bi-check-circle text-success"></i> 
                       ${livres.length} livre${livres.length > 1 ? 's' : ''} trouvé${livres.length > 1 ? 's' : ''}`;

    livres.forEach(livre => {
        grid.innerHTML += creerCarte(livre);
    });
}

function creerCarte(livre) {
    const imageUrl = livre.imageUrl ||
        'https://via.placeholder.com/300x400?text=' + encodeURIComponent(livre.titre || 'Livre');

    const annee = livre.annee || '—';
    const domaine = livre.domaine || 'Non classé';
    const auteur = livre.auteur || 'Auteur inconnu';
    const isLocal = livre.source === 'LOCAL';

    const titreJs = (livre.titre || '').replace(/'/g, "\\'").replace(/"/g, '\\"');

    // Lien Amazon (recherche par titre + auteur)
    const amazonUrl = livre.buyLink ||
        ('https://www.amazon.fr/s?k=' + encodeURIComponent((livre.titre || '') + ' ' + (livre.auteur || '')));

    const badgeSource = isLocal
        ? '<span class="badge bg-success badge-domaine mb-2 align-self-start"><i class="bi bi-database"></i> Ma bibliothèque</span>'
        : '<span class="badge bg-info text-dark badge-domaine mb-2 align-self-start"><i class="bi bi-globe"></i> Open Library</span>';

    const btns = [];

    // 1. Détails (local → livre.html, externe → Open Library)
    btns.push(`
        <button class="btn btn-outline-primary btn-sm" 
                onclick="voirDetails('${livre.source}', ${livre.localId || 0}, '${titreJs}', '${livre.fichierUrl || ''}')">
            <i class="bi bi-eye"></i> Détails
        </button>
    `);

    // 2. Télécharger (local) / Voir (externe)
    if (livre.fichierUrl) {
        btns.push(`
            <a href="${livre.fichierUrl}" target="_blank" class="btn btn-primary btn-sm">
                <i class="bi bi-download"></i> ${isLocal ? 'Télécharger' : 'Voir'}
            </a>
        `);
    } else {
        btns.push(`
            <button class="btn btn-primary btn-sm" disabled title="Pas de fichier disponible">
                <i class="bi bi-download"></i> ${isLocal ? 'Télécharger' : 'Voir'}
            </button>
        `);
    }

    // 3. Amazon
    btns.push(`
        <a href="${amazonUrl}" target="_blank" class="btn btn-success btn-sm">
            <i class="bi bi-cart"></i> Amazon
        </a>
    `);

    // 4. Commander
    btns.push(`
        <button class="btn btn-warning btn-sm" onclick="commander('${titreJs}', '${livre.source}')">
            <i class="bi bi-bag-check"></i> Commander
        </button>
    `);

    return `
        <div class="col-6 col-md-4 col-lg-3">
            <div class="card livre-card">
                <img src="${imageUrl}" class="card-img-top" 
                     alt="${echapper(livre.titre)}"
                     onerror="this.src='https://via.placeholder.com/300x400?text=Livre'">
                <div class="card-body d-flex flex-column">
                    ${badgeSource}
                    <span class="badge bg-primary badge-domaine mb-2 align-self-start">
                        ${echapper(domaine)}
                    </span>
                    <h5 class="card-title">${echapper(livre.titre)}</h5>
                    <p class="card-subtitle mb-2">
                        <i class="bi bi-person"></i> ${echapper(auteur)}
                    </p>
                    <p class="card-text small text-muted mb-3">
                        <i class="bi bi-calendar"></i> ${annee}
                    </p>
                    <div class="d-grid gap-1 mt-auto">${btns.join('')}</div>
                </div>
            </div>
        </div>
    `;
}

function brancherEvenements() {
    document.getElementById('searchBtn').addEventListener('click', rechercher);
    document.getElementById('resetBtn').addEventListener('click', resetFiltres);

    document.getElementById('searchInput').addEventListener('keypress', (e) => {
        if (e.key === 'Enter') rechercher();
    });
}

async function rechercher() {
    const q = document.getElementById('searchInput').value.trim();
    const domaine = document.getElementById('filtreDomaine').value;
    const anneeMin = document.getElementById('filtreAnneeMin').value;
    const anneeMax = document.getElementById('filtreAnneeMax').value;

    document.getElementById('resultatsCount').innerHTML =
        '<i class="bi bi-hourglass-split"></i> Recherche en cours...';

    const params = new URLSearchParams();
    if (q) params.append('q', q);
    if (domaine) params.append('domaine', domaine);
    if (anneeMin) params.append('anneeMin', anneeMin);
    if (anneeMax) params.append('anneeMax', anneeMax);

    try {
        const response = await fetch(API_SEARCH + '?' + params.toString());
        const resultats = await response.json();
        afficherLivres(resultats);
    } catch (error) {
        console.error('Erreur recherche:', error);
        document.getElementById('resultatsCount').innerHTML =
            '<span class="text-danger">Erreur de recherche</span>';
    }
}

function resetFiltres() {
    document.getElementById('searchInput').value = '';
    document.getElementById('filtreDomaine').value = '';
    document.getElementById('filtreAnneeMin').value = '';
    document.getElementById('filtreAnneeMax').value = '';
    afficherLivres(tousLesLivres);
}

// ---------- DÉTAILS ----------
function voirDetails(source, id, titre, fichierUrl) {
    if (source === 'LOCAL') {
        // Livre local → fiche détaillée dans l'app
        window.location.href = `livre.html?id=${id}`;
    } else {
        // Livre externe → ouvre la fiche Open Library (ou recherche)
        if (fichierUrl && fichierUrl.startsWith('http')) {
            window.open(fichierUrl, '_blank');
        } else {
            window.open('https://openlibrary.org/search?q=' + encodeURIComponent(titre), '_blank');
        }
    }
}

// ---------- COMMANDER ----------
async function commander(titre, source) {
    const email = prompt('Votre email pour commander "' + titre + '" :');
    if (!email || email.trim() === '') return;

    try {
        const r = await fetch('api/commande', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ email: email.trim(), titre: titre, source: source })
        });

        if (r.ok) {
            alert('✅ Commande enregistrée !\nUn email de confirmation sera envoyé à ' + email);
        } else {
            const data = await r.json();
            alert('❌ Erreur : ' + (data.error || 'Erreur inconnue'));
        }
    } catch (e) {
        alert('❌ Erreur réseau : ' + e.message);
    }
}

function echapper(str) {
    if (str === null || str === undefined) return '';
    return String(str)
        .replace(/&/g, '&amp;')
        .replace(/</g, '&lt;')
        .replace(/>/g, '&gt;')
        .replace(/"/g, '&quot;')
        .replace(/'/g, '&#039;');
}