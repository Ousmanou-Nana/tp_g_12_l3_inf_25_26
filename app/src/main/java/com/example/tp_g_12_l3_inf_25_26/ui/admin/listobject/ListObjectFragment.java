

// ============================================================================
// ListObjectFragment.java
// ============================================================================
// Fragment qui gère l'interface utilisateur pour la liste des objets trouvés
// C'est le pendant de ListDeclarationFragment mais pour les objets trouvés
// Permet aux admins de:
// - Visualiser tous les objets trouvés ou filtrer par statut
// - Voir les détails d'un objet trouvé
// - Mettre à jour le statut (en vérification, récupéré)
// - Associer un objet trouvé à une déclaration d'objet perdu (matching)
// - Supprimer un objet trouvé
// ============================================================================

package com.example.tp_g_12_l3_inf_25_26.ui.admin.listobject;

import androidx.lifecycle.ViewModelProvider;
import android.app.AlertDialog;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;
import com.example.tp_g_12_l3_inf_25_26.R;
import com.example.tp_g_12_l3_inf_25_26.models.Declaration;
import com.example.tp_g_12_l3_inf_25_26.models.Objet;
import com.example.tp_g_12_l3_inf_25_26.ui.admin.matching.MatchingDialog;
import com.example.tp_g_12_l3_inf_25_26.utils.TableAdapter;
import com.example.tp_g_12_l3_inf_25_26.utils.TableRow;
import java.util.List;

public class ListObjectFragment extends Fragment {

    // ===== VIEWMODEL ET ADAPTER =====
    private ListObjectViewModel mViewModel;

    /**
     * Adapter pour afficher les objets trouvés dans un tableau (RecyclerView)
     * Gère l'affichage de chaque ligne et le clic sur une ligne
     */
    private TableAdapter<TableRow> adapter;

    // ===== BOUTONS DE FILTRAGE =====
    private Button btnAll;              // Afficher tous les objets
    private Button btnPending;          // Filtrer "En attente"
    private Button btnVerification;     // Filtrer "En cours de vérification"
    private Button btnRecovered;        // Filtrer "Récupéré"
    private Button btnRefresh;          // Rafraîchir la liste

    /**
     * Méthode factory pour créer une instance du fragment
     */
    public static ListObjectFragment newInstance() {
        return new ListObjectFragment();
    }

    /**
     * Crée la vue du fragment en inflatant le layout XML
     */
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_list_object, container, false);
    }

    /**
     * Appelé après la création de la vue
     * Configure tous les composants de l'interface
     */
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // ===== INITIALISATION DU VIEWMODEL =====
        mViewModel = new ViewModelProvider(this,
                ViewModelProvider.AndroidViewModelFactory.getInstance(requireActivity().getApplication()))
                .get(ListObjectViewModel.class);

        // ===== CONFIGURATION DE L'INTERFACE =====
        initViews(view);            // Récupère les références aux boutons
        setupRecyclerView(view);    // Configure le tableau RecyclerView
        setupButtons();             // Configure les écouteurs de clics sur les boutons
        observeViewModel();         // Configure les observateurs de LiveData
    }

    // ===== INITIALISATION DES VUES =====

    /**
     * Récupère les références à tous les boutons de l'interface
     */
    private void initViews(View view) {
        btnAll = view.findViewById(R.id.btnAll);
        btnPending = view.findViewById(R.id.btnPending);
        btnVerification = view.findViewById(R.id.btnVerification);
        btnRecovered = view.findViewById(R.id.btnRecovered);
        btnRefresh = view.findViewById(R.id.btnRefresh);
    }

    // ===== CONFIGURATION DU RECYCLERVIEW (TABLEAU) =====

    /**
     * Configure le RecyclerView qui affiche la liste des objets trouvés
     * Utilise un TableAdapter personnalisé pour l'affichage en tableau
     */
    private void setupRecyclerView(View view) {
        RecyclerView recycler = view.findViewById(R.id.table);

        // Utilise un LayoutManager linéaire (affichage vertical)
        recycler.setLayoutManager(new LinearLayoutManager(requireContext()));

        // ===== CRÉATION DE L'ADAPTER =====
        adapter = new TableAdapter<>(
                requireContext(),
                mViewModel.getColumns(),     // Définitions des colonnes du tableau
                null,                        // Données (seront mises à jour via observer)
                row -> showActionDialog(row) // Action au clic sur une ligne
        );

        // Associe l'adapter au RecyclerView
        recycler.setAdapter(adapter);
    }

    // ===== CONFIGURATION DES BOUTONS DE FILTRAGE =====

    /**
     * Configure les écouteurs de clics pour tous les boutons de filtrage
     * Chaque bouton charge un sous-ensemble différent d'objets trouvés
     *
     * Note: Vérifie que chaque bouton n'est pas null avant de configurer
     * (protection contre les erreurs si un bouton est manquant dans le layout)
     */
    private void setupButtons() {

        // ===== BOUTON "TOUS" =====
        // Affiche tous les objets trouvés sans filtre
        if (btnAll != null) {
            btnAll.setOnClickListener(v -> {
                mViewModel.loadObjets();
                Toast.makeText(requireContext(),
                        "Affichage de tous les objets",
                        Toast.LENGTH_SHORT).show();
            });
        }

        // ===== BOUTON "EN ATTENTE" =====
        // Filtre uniquement les objets qui attendent un traitement
        if (btnPending != null) {
            btnPending.setOnClickListener(v -> {
                mViewModel.loadObjetsByStatut("En attente");
                Toast.makeText(requireContext(),
                        "Affichage des objets en attente",
                        Toast.LENGTH_SHORT).show();
            });
        }

        // ===== BOUTON "EN VÉRIFICATION" =====
        // Filtre les objets en cours de traitement
        if (btnVerification != null) {
            btnVerification.setOnClickListener(v -> {
                mViewModel.loadObjetsByStatut("En cours de vérification");
                Toast.makeText(requireContext(),
                        "Affichage des objets en cours de vérification",
                        Toast.LENGTH_SHORT).show();
            });
        }

        // ===== BOUTON "RÉCUPÉRÉS" =====
        // Filtre les objets qui ont été rendus à leurs propriétaires
        if (btnRecovered != null) {
            btnRecovered.setOnClickListener(v -> {
                mViewModel.loadObjetsByStatut("Récupéré");
                Toast.makeText(requireContext(),
                        "Affichage des objets récupérés",
                        Toast.LENGTH_SHORT).show();
            });
        }

        // ===== BOUTON "RAFRAÎCHIR" =====
        // Recharge tous les objets (utile pour voir les mises à jour)
        if (btnRefresh != null) {
            btnRefresh.setOnClickListener(v -> {
                mViewModel.loadObjets();
                Toast.makeText(requireContext(),
                        "Liste actualisée",
                        Toast.LENGTH_SHORT).show();
            });
        }
    }

    // ===== OBSERVATION DU VIEWMODEL =====

    /**
     * Configure les observateurs pour réagir aux changements dans le ViewModel
     */
    private void observeViewModel() {

        // ===== OBSERVER LES LIGNES DU TABLEAU =====
        // S'exécute chaque fois que les données du tableau changent
        mViewModel.getRowsLiveData().observe(getViewLifecycleOwner(), rows -> {
            if (rows != null) {
                // Met à jour l'affichage du tableau avec les nouvelles données
                adapter.updateData(rows);
            }
        });

        // ===== OBSERVER LES RÉSULTATS D'ACTIONS =====
        // S'exécute après mise à jour statut, suppression, etc.
        mViewModel.getActionResult().observe(getViewLifecycleOwner(), result -> {
            if (result != null) {
                // Affiche un message de confirmation ou d'erreur
                Toast.makeText(requireContext(), result, Toast.LENGTH_LONG).show();
            }
        });
    }

    // ===== DIALOGUE D'ACTIONS SUR UN OBJET =====

    /**
     * Affiche un dialogue avec les détails d'un objet trouvé et les actions possibles
     *
     * Actions disponibles:
     * - Mettre en vérification (avec choix de déclaration correspondante)
     * - Marquer comme récupéré
     * - Supprimer
     *
     * Note: Pas d'option "Rejeter" pour les objets trouvés
     * (si un objet trouvé est invalide, on le supprime directement)
     *
     * @param row Ligne du tableau cliquée
     */
    private void showActionDialog(TableRow row) {
        // ===== RÉCUPÉRATION DE L'ID DE L'OBJET =====
        List<String> data = row.getData();
        if (data == null || data.isEmpty()) return;

        int objetId;
        try {
            // Premier élément = ID de l'objet
            objetId = Integer.parseInt(data.get(0));
        } catch (NumberFormatException e) {
            Toast.makeText(requireContext(),
                    "Erreur: ID invalide",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        // ===== RÉCUPÉRATION DE L'OBJET COMPLET =====
        Objet objet = mViewModel.getObjetById(objetId);
        if (objet == null) {
            Toast.makeText(requireContext(),
                    "Objet introuvable",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        // ===== CONSTRUCTION DU MESSAGE DE DÉTAILS =====
        String message = buildDetailMessage(objet);

        // ===== AFFICHAGE DU DIALOGUE AVEC ACTIONS =====
        new AlertDialog.Builder(requireContext())
                .setTitle("Détails de l'objet")
                .setMessage(message)

                // ===== BOUTON "METTRE EN VÉRIFICATION" =====
                // Ouvre un dialogue pour associer à une déclaration
                .setPositiveButton("Mettre en vérification", (dialog, which) -> {
                    showMatchingDeclarationsDialog(objet);
                })

                // ===== BOUTON "MARQUER RÉCUPÉRÉ" =====
                // Change le statut à "Récupéré"
                .setNeutralButton("Marquer récupéré", (dialog, which) -> {
                    mViewModel.updateObjetStatut(objetId, "Récupéré");
                })

                // ===== BOUTON "SUPPRIMER" =====
                // Supprime définitivement après confirmation
                .setNegativeButton("Supprimer", (dialog, which) -> {
                    confirmDelete(objetId);
                })
                .show();
    }

    // ===== DIALOGUE DE MATCHING (CORRESPONDANCE) =====

    /**
     * Affiche un dialogue permettant d'associer un objet trouvé à une déclaration
     * C'est l'inverse de showMatchingObjectsDialog dans ListDeclarationFragment
     *
     * Processus:
     * 1. Charge les déclarations du même type
     * 2. Si aucune déclaration: propose juste de changer le statut
     * 3. Si déclarations trouvées: affiche une liste pour choisir la correspondance
     * 4. Crée le lien (matching) entre objet et déclaration choisie
     *
     * @param objet Objet trouvé à traiter
     */
    private void showMatchingDeclarationsDialog(Objet objet) {
        // ===== CHARGEMENT DES DÉCLARATIONS CORRESPONDANTES POTENTIELLES =====
        // Charge les déclarations du même type (ex: toutes les déclarations de clés perdues)
        mViewModel.loadPotentialMatchingDeclarations(
                objet.getIdType(),
                objet.getIdObjet()
        );

        // ===== OBSERVATION DES RÉSULTATS =====
        mViewModel.getPotentialMatchesLiveData().observe(getViewLifecycleOwner(), declarations -> {

            // ===== CAS 1: AUCUNE DÉCLARATION CORRESPONDANTE =====
            if (declarations == null || declarations.isEmpty()) {
                // Propose simplement de changer le statut sans lier à une déclaration
                new AlertDialog.Builder(requireContext())
                        .setTitle("Aucune déclaration correspondante")
                        .setMessage("Aucune déclaration de même type. Voulez-vous quand même mettre en vérification ?")
                        .setPositiveButton("Oui", (d, w) -> {
                            mViewModel.updateObjetStatut(objet.getIdObjet(), "En cours de vérification");
                        })
                        .setNegativeButton("Non", null)
                        .show();
                return;
            }

            // ===== CAS 2: DÉCLARATIONS TROUVÉES =====
            // Affiche un dialogue personnalisé avec la liste des déclarations
            MatchingDialog.showMatchingDeclarationsForObjet(
                    requireContext(),
                    objet,
                    declarations,
                    new MatchingDialog.OnMatchSelectedListener() {

                        /**
                         * Appelé quand l'admin sélectionne une déclaration correspondante
                         * Crée le lien entre l'objet trouvé et la déclaration
                         */
                        @Override
                        public void onDeclarationSelected(Declaration declaration) {
                            // Crée la correspondance dans la BDD
                            mViewModel.createMatching(
                                    declaration.getIdDeclaration(),
                                    objet.getIdObjet()
                            );

                            // Change le statut à "En cours de vérification"
                            mViewModel.updateObjetStatut(objet.getIdObjet(), "En cours de vérification");

                            // Confirme à l'admin
                            Toast.makeText(requireContext(),
                                    "Objet lié à la déclaration N°" + declaration.getIdDeclaration(),
                                    Toast.LENGTH_LONG).show();
                        }

                        @Override
                        public void onObjetSelected(Objet o) {
                            // Non utilisé dans ce contexte
                            // (utilisé quand on fait le matching depuis les déclarations)
                        }

                        /**
                         * Appelé si l'admin choisit "Aucune correspondance"
                         * Change juste le statut sans créer de lien
                         */
                        @Override
                        public void onNoMatch() {
                            mViewModel.updateObjetStatut(objet.getIdObjet(), "En cours de vérification");
                        }
                    }
            );
        });
    }

    // ===== CONSTRUCTION DU MESSAGE DE DÉTAILS =====

    /**
     * Construit un message formaté avec tous les détails d'un objet trouvé
     *
     * @param objet Objet à afficher
     * @return Message formaté multi-lignes
     */
    private String buildDetailMessage(Objet objet) {
        StringBuilder message = new StringBuilder();

        // Informations principales
        message.append("N° : ").append(objet.getIdObjet()).append("\n\n");

        // Informations sur le déclarant (qui a trouvé l'objet)
        message.append("Déclarant : ").append(objet.getNomDeclarant()).append("\n");
        message.append("Téléphone : ").append(objet.getTelephone()).append("\n");

        // Informations sur l'objet
        message.append("Type : ").append(objet.getNomType()).append("\n");
        message.append("Statut : ").append(objet.getStatut()).append("\n");
        message.append("Date : ").append(objet.getDateDeclaration()).append("\n\n");

        // Description détaillée
        message.append("Description :\n").append(objet.getDescription()).append("\n\n");

        // Nombre d'images
        message.append("Nombre d'images : ").append(objet.getCheminImages().size());

        return message.toString();
    }

    // ===== DIALOGUE DE CONFIRMATION =====

    /**
     * Demande confirmation avant de supprimer définitivement un objet trouvé
     * ATTENTION: Action irréversible
     *
     * @param objetId ID de l'objet à supprimer
     */
    private void confirmDelete(int objetId) {
        new AlertDialog.Builder(requireContext())
                .setTitle("Confirmation")
                .setMessage("Êtes-vous sûr de vouloir supprimer définitivement cet objet ? Cette action est irréversible.")
                .setPositiveButton("Supprimer", (dialog, which) -> {
                    mViewModel.deleteObjet(objetId);
                })
                .setNegativeButton("Annuler", null)
                .show();
    }
}