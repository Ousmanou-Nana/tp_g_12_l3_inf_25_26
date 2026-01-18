
// ============================================================================
// ListDeclarationFragment.java
// ============================================================================
// Fragment qui gère l'interface utilisateur pour la liste des déclarations
// Permet aux admins de:
// - Visualiser toutes les déclarations ou filtrer par statut
// - Voir les détails d'une déclaration
// - Valider, rejeter, marquer comme récupéré ou supprimer une déclaration
// - Associer une déclaration à un objet trouvé (matching)
// ============================================================================

package com.example.tp_g_12_l3_inf_25_26.ui.admin.listdeclaration;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.tp_g_12_l3_inf_25_26.R;
import com.example.tp_g_12_l3_inf_25_26.models.Declaration;
import com.example.tp_g_12_l3_inf_25_26.models.Objet;
import com.example.tp_g_12_l3_inf_25_26.ui.admin.matching.MatchingDialog;
import com.example.tp_g_12_l3_inf_25_26.utils.TableAdapter;
import com.example.tp_g_12_l3_inf_25_26.utils.TableRow;
import java.util.List;

public class ListDeclarationFragment extends Fragment {

    // ===== VIEWMODEL ET ADAPTER =====
    private ListDeclarationViewModel viewModel;

    /**
     * Adapter pour afficher les déclarations dans un tableau (RecyclerView)
     * Gère l'affichage de chaque ligne et le clic sur une ligne
     */
    private TableAdapter<TableRow> adapter;

    // ===== BOUTONS DE FILTRAGE =====
    private Button btnAll;          // Afficher toutes les déclarations
    private Button btnPending;      // Filtrer "En attente"
    private Button btnValidated;    // Filtrer "En cours de vérification"
    private Button btnRecovered;    // Filtrer "Récupéré"
    private Button btnRefresh;      // Rafraîchir la liste

    /**
     * Méthode factory pour créer une instance du fragment
     */
    public static ListDeclarationFragment newInstance() {
        return new ListDeclarationFragment();
    }

    /**
     * Crée la vue du fragment en inflatant le layout XML
     */
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_list_declaration, container, false);
    }

    /**
     * Appelé après la création de la vue
     * Configure tous les composants de l'interface
     */
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // ===== INITIALISATION DU VIEWMODEL =====
        viewModel = new ViewModelProvider(this,
                ViewModelProvider.AndroidViewModelFactory.getInstance(requireActivity().getApplication()))
                .get(ListDeclarationViewModel.class);

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
        btnValidated = view.findViewById(R.id.btnValidated);
        btnRecovered = view.findViewById(R.id.btnRecovered);
        btnRefresh = view.findViewById(R.id.btnRefresh);
    }

    // ===== CONFIGURATION DU RECYCLERVIEW (TABLEAU) =====

    /**
     * Configure le RecyclerView qui affiche la liste des déclarations
     * Utilise un TableAdapter personnalisé pour l'affichage en tableau
     */
    private void setupRecyclerView(View view) {
        RecyclerView recycler = view.findViewById(R.id.table);

        // Utilise un LayoutManager linéaire (affichage vertical)
        recycler.setLayoutManager(new LinearLayoutManager(requireContext()));

        // ===== CRÉATION DE L'ADAPTER =====
        adapter = new TableAdapter<>(
                requireContext(),
                viewModel.getColumns(),     // Définitions des colonnes du tableau
                null,                       // Données (seront mises à jour via observer)
                row -> showActionDialog(row) // Action au clic sur une ligne
        );

        // Associe l'adapter au RecyclerView
        recycler.setAdapter(adapter);
    }

    // ===== CONFIGURATION DES BOUTONS DE FILTRAGE =====

    /**
     * Configure les écouteurs de clics pour tous les boutons de filtrage
     * Chaque bouton charge un sous-ensemble différent de déclarations
     */
    private void setupButtons() {

        // ===== BOUTON "TOUTES" =====
        // Affiche toutes les déclarations sans filtre
        btnAll.setOnClickListener(v -> {
            viewModel.loadDeclarations();
            Toast.makeText(requireContext(),
                    "Affichage de toutes les déclarations",
                    Toast.LENGTH_SHORT).show();
        });

        // ===== BOUTON "EN ATTENTE" =====
        // Filtre uniquement les déclarations qui attendent un traitement
        btnPending.setOnClickListener(v -> {
            viewModel.loadDeclarationsByStatut("En attente");
            Toast.makeText(requireContext(),
                    "Affichage des déclarations en attente",
                    Toast.LENGTH_SHORT).show();
        });

        // ===== BOUTON "EN VÉRIFICATION" =====
        // Filtre les déclarations en cours de traitement
        btnValidated.setOnClickListener(v -> {
            viewModel.loadDeclarationsByStatut("En cours de vérification");
            Toast.makeText(requireContext(),
                    "Affichage des déclarations en cours de vérification",
                    Toast.LENGTH_SHORT).show();
        });

        // ===== BOUTON "RÉCUPÉRÉS" =====
        // Filtre les objets qui ont été récupérés par leurs propriétaires
        btnRecovered.setOnClickListener(v -> {
            viewModel.loadDeclarationsByStatut("Récupéré");
            Toast.makeText(requireContext(),
                    "Affichage des objets récupérés",
                    Toast.LENGTH_SHORT).show();
        });

        // ===== BOUTON "RAFRAÎCHIR" =====
        // Recharge toutes les déclarations (utile pour voir les mises à jour)
        btnRefresh.setOnClickListener(v -> {
            viewModel.loadDeclarations();
            Toast.makeText(requireContext(),
                    "Liste actualisée",
                    Toast.LENGTH_SHORT).show();
        });
    }

    // ===== OBSERVATION DU VIEWMODEL =====

    /**
     * Configure les observateurs pour réagir aux changements dans le ViewModel
     */
    private void observeViewModel() {

        // ===== OBSERVER LES LIGNES DU TABLEAU =====
        // S'exécute chaque fois que les données du tableau changent
        viewModel.getRowsLiveData().observe(getViewLifecycleOwner(), rows -> {
            if (rows != null) {
                // Met à jour l'affichage du tableau avec les nouvelles données
                adapter.updateData(rows);
            }
        });

        // ===== OBSERVER LES RÉSULTATS D'ACTIONS =====
        // S'exécute après validation, rejet, suppression, etc.
        viewModel.getActionResult().observe(getViewLifecycleOwner(), result -> {
            if (result != null) {
                // Affiche un message de confirmation ou d'erreur
                Toast.makeText(requireContext(), result, Toast.LENGTH_LONG).show();
            }
        });
    }

    // ===== DIALOGUE D'ACTIONS SUR UNE DÉCLARATION =====

    /**
     * Affiche un dialogue avec les détails d'une déclaration et les actions possibles
     *
     * Actions disponibles:
     * - Mettre en vérification (avec choix d'objet correspondant)
     * - Marquer comme récupéré
     * - Rejeter
     * - Supprimer
     *
     * @param row Ligne du tableau cliquée
     */
    private void showActionDialog(TableRow row) {
        // ===== RÉCUPÉRATION DE L'ID DE LA DÉCLARATION =====
        List<String> data = row.getData();
        if (data == null || data.isEmpty()) return;

        int declarationId;
        try {
            // Premier élément = ID de la déclaration
            declarationId = Integer.parseInt(data.get(0));
        } catch (NumberFormatException e) {
            Toast.makeText(requireContext(),
                    "Erreur: ID invalide",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        // ===== RÉCUPÉRATION DE L'OBJET DECLARATION COMPLET =====
        Declaration declaration = viewModel.getDeclarationById(declarationId);
        if (declaration == null) {
            Toast.makeText(requireContext(),
                    "Déclaration introuvable",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        // ===== CONSTRUCTION DU MESSAGE DE DÉTAILS =====
        String message = buildDetailMessage(declaration);

        // ===== AFFICHAGE DU DIALOGUE AVEC ACTIONS =====
        new AlertDialog.Builder(requireContext())
                .setTitle("Détails de la déclaration")
                .setMessage(message)

                // ===== BOUTON "METTRE EN VÉRIFICATION" =====
                // Ouvre un dialogue pour associer à un objet trouvé
                .setPositiveButton("Mettre en vérification", (dialog, which) -> {
                    showMatchingObjectsDialog(declaration);
                })

                // ===== BOUTON "MARQUER RÉCUPÉRÉ" =====
                // Change le statut à "Récupéré"
                .setNeutralButton("Marquer récupéré", (dialog, which) -> {
                    viewModel.markAsRecovered(declarationId);
                })

                // ===== BOUTON "REJETER" =====
                // Change le statut à "Rejeté" après confirmation
                .setNegativeButton("Rejeter", (dialog, which) -> {
                    confirmReject(declarationId);
                })

                // ===== BOUTON "SUPPRIMER" =====
                // Supprime définitivement après confirmation
                .setNeutralButton("Supprimer", (dialog, which) -> {
                    confirmDelete(declarationId);
                })
                .show();
    }

    // ===== DIALOGUE DE MATCHING (CORRESPONDANCE) =====

    /**
     * Affiche un dialogue permettant d'associer une déclaration à un objet trouvé
     *
     * Processus:
     * 1. Charge les objets trouvés du même type
     * 2. Si aucun objet: propose juste de changer le statut
     * 3. Si objets trouvés: affiche une liste pour choisir la correspondance
     * 4. Crée le lien (matching) entre déclaration et objet choisi
     *
     * @param declaration Déclaration à traiter
     */
    private void showMatchingObjectsDialog(Declaration declaration) {
        // ===== CHARGEMENT DES OBJETS CORRESPONDANTS POTENTIELS =====
        // Charge les objets du même type (ex: toutes les clés trouvées)
        viewModel.loadPotentialMatchingObjets(
                declaration.getIdType(),
                declaration.getIdDeclaration()
        );

        // ===== OBSERVATION DES RÉSULTATS =====
        viewModel.getPotentialMatchesLiveData().observe(getViewLifecycleOwner(), objets -> {

            // ===== CAS 1: AUCUN OBJET CORRESPONDANT =====
            if (objets == null || objets.isEmpty()) {
                // Propose simplement de changer le statut sans lier à un objet
                new AlertDialog.Builder(requireContext())
                        .setTitle("Aucun objet correspondant")
                        .setMessage("Aucun objet trouvé de même type. Voulez-vous quand même mettre en vérification ?")
                        .setPositiveButton("Oui", (d, w) -> {
                            viewModel.validateDeclaration(declaration.getIdDeclaration());
                        })
                        .setNegativeButton("Non", null)
                        .show();
                return;
            }

            // ===== CAS 2: OBJETS TROUVÉS =====
            // Affiche un dialogue personnalisé avec la liste des objets
            MatchingDialog.showMatchingObjectsForDeclaration(
                    requireContext(),
                    declaration,
                    objets,
                    new MatchingDialog.OnMatchSelectedListener() {

                        @Override
                        public void onDeclarationSelected(Declaration d) {
                            // Non utilisé dans ce contexte
                            // (utilisé quand on fait le matching depuis les objets trouvés)
                        }

                        /**
                         * Appelé quand l'admin sélectionne un objet correspondant
                         * Crée le lien entre la déclaration et l'objet
                         */
                        @Override
                        public void onObjetSelected(Objet objet) {
                            // Crée la correspondance dans la BDD
                            viewModel.createMatching(
                                    declaration.getIdDeclaration(),
                                    objet.getIdObjet()
                            );

                            // Change le statut à "En cours de vérification"
                            viewModel.validateDeclaration(declaration.getIdDeclaration());

                            // Confirme à l'admin
                            Toast.makeText(requireContext(),
                                    "Déclaration liée à l'objet N°" + objet.getIdObjet(),
                                    Toast.LENGTH_LONG).show();
                        }

                        /**
                         * Appelé si l'admin choisit "Aucune correspondance"
                         * Change juste le statut sans créer de lien
                         */
                        @Override
                        public void onNoMatch() {
                            viewModel.validateDeclaration(declaration.getIdDeclaration());
                        }
                    }
            );
        });
    }

    // ===== CONSTRUCTION DU MESSAGE DE DÉTAILS =====

    /**
     * Construit un message formaté avec tous les détails d'une déclaration
     *
     * @param declaration Déclaration à afficher
     * @return Message formaté multi-lignes
     */
    private String buildDetailMessage(Declaration declaration) {
        StringBuilder message = new StringBuilder();

        // Informations principales
        message.append("N° : ").append(declaration.getIdDeclaration()).append("\n\n");

        // Informations sur le déclarant
        message.append("Déclarant : ").append(declaration.getUserName()).append("\n");
        message.append("Téléphone : ").append(declaration.getUserPhone()).append("\n");
        message.append("Matricule : ").append(declaration.getUserMatricule()).append("\n");

        // Informations sur l'objet
        message.append("Type : ").append(declaration.getNomType()).append("\n");
        message.append("Statut : ").append(declaration.getStatut()).append("\n");
        message.append("Date : ").append(declaration.getDateDeclaration()).append("\n\n");

        // Description détaillée
        message.append("Description :\n").append(declaration.getDescription()).append("\n\n");

        // Nombre d'images
        message.append("Nombre d'images : ").append(declaration.getCheminImages().size());

        return message.toString();
    }

    // ===== DIALOGUES DE CONFIRMATION =====

    /**
     * Demande confirmation avant de rejeter une déclaration
     *
     * @param declarationId ID de la déclaration à rejeter
     */
    private void confirmReject(int declarationId) {
        new AlertDialog.Builder(requireContext())
                .setTitle("Confirmation")
                .setMessage("Êtes-vous sûr de vouloir rejeter cette déclaration ?")
                .setPositiveButton("Oui", (dialog, which) -> {
                    viewModel.rejectDeclaration(declarationId);
                })
                .setNegativeButton("Non", null)
                .show();
    }

    /**
     * Demande confirmation avant de supprimer définitivement une déclaration
     * ATTENTION: Action irréversible
     *
     * @param declarationId ID de la déclaration à supprimer
     */
    private void confirmDelete(int declarationId) {
        new AlertDialog.Builder(requireContext())
                .setTitle("Confirmation")
                .setMessage("Êtes-vous sûr de vouloir supprimer définitivement cette déclaration ? Cette action est irréversible.")
                .setPositiveButton("Supprimer", (dialog, which) -> {
                    viewModel.deleteDeclaration(declarationId);
                })
                .setNegativeButton("Annuler", null)
                .show();
    }
}