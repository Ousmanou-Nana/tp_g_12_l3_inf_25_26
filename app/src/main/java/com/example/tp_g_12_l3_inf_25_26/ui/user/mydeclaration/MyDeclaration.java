package com.example.tp_g_12_l3_inf_25_26.ui.user.mydeclaration;

import android.app.AlertDialog;
import android.content.pm.ActivityInfo;
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
import com.example.tp_g_12_l3_inf_25_26.ui.user.lostlist.UserLostList;
import com.example.tp_g_12_l3_inf_25_26.ui.user.declareobjectfrom.UserDeclareObjectFrom;
import com.example.tp_g_12_l3_inf_25_26.utils.TableAdapter;
import com.example.tp_g_12_l3_inf_25_26.utils.TableRow;

/**
 * Fragment affichant les déclarations personnelles de l'utilisateur connecté.
 /*
 * Ce fragment permet à l'utilisateur de consulter et gérer ses propres déclarations
 * d'objets perdus. Il offre des fonctionnalités de filtrage, suppression et navigation.
 /*
 * Fonctionnalités principales:
 * - Affichage des déclarations de l'utilisateur dans un RecyclerView
 * - Filtrage par statut (Tous, En attente, En vérification, Récupéré)
 * - Suppression de déclarations avec confirmation
 * - Navigation vers création de nouvelle déclaration
 * - Navigation vers liste complète des objets perdus
 * - Rafraîchissement manuel et automatique des données
 * - Codes couleur visuels selon le statut
 /*
 * Architecture:
 * - Pattern MVVM avec MyDeclarationViewModel
 * - RecyclerView avec TableAdapter personnalisé
 * - LiveData pour mises à jour réactives
 * - AlertDialog pour confirmations de suppression

 * Navigation:
 * - Vers UserDeclareObjectFrom (nouvelle déclaration)
 * - Vers UserLostList (liste complète des objets perdus)
 * - Avec support du bouton retour (addToBackStack)
 /*
 * Filtres disponibles:
 * - Tous : Toutes les déclarations de l'utilisateur (par défaut)
 * - En attente : Déclarations en attente de traitement
 * - En vérification : Déclarations en cours de vérification
 * - Récupéré : Objets déjà récupérés
 *
 * @author Votre équipe
 * @version 1.0
 */
public class MyDeclaration extends Fragment {

    // ==================== ATTRIBUTS ====================

    /**
     * ViewModel gérant la logique métier et les données des déclarations.
     * Responsable du chargement, filtrage et suppression des déclarations.
     */
    private MyDeclarationViewModel viewModel;

    /**
     * Adaptateur personnalisé pour afficher les déclarations en format tableau.
     * Gère l'affichage et les interactions avec chaque ligne.
     * Support du clic pour suppression.
     */
    private TableAdapter<TableRow> adapter;

    // ========== BOUTONS DE FILTRAGE ==========

    /**
     * Bouton pour afficher toutes les déclarations (aucun filtre).
     * C'est le filtre par défaut au démarrage.
     */
    private Button btnAll;

    /**
     * Bouton pour filtrer les déclarations "En attente"
     */
    private Button btnPending;

    /**
     * Bouton pour filtrer les déclarations "En cours de vérification"
     */
    private Button btnVerification;

    /**
     * Bouton pour filtrer les déclarations "Récupéré"
     */
    private Button btnRecovered;

    /**
     * Bouton pour rafraîchir manuellement les données
     */
    private Button btnRefresh;

    // ==================== MÉTHODES DE CYCLE DE VIE ====================

    /**
     * Factory method pour créer une nouvelle instance du fragment.
     *
     * @return Nouvelle instance de MyDeclaration
     */
    public static MyDeclaration newInstance() {
        return new MyDeclaration();
    }

    /**
     * Appelé lors de la création du fragment.
     /*
     * Initialise le ViewModel qui survivra aux changements de configuration.
     * Le ViewModel charge automatiquement les déclarations de l'utilisateur
     * connecté via SharedPreferences.
     *
     * @param savedInstanceState État sauvegardé précédent
     */
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getActivity() != null) {
            getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        }

        // Initialisation du ViewModel avec AndroidViewModelFactory
        // pour passer le contexte Application
        viewModel = new ViewModelProvider(this,
                ViewModelProvider.AndroidViewModelFactory.getInstance(requireActivity().getApplication()))
                .get(MyDeclarationViewModel.class);
    }

    /**
     * Crée et retourne la hiérarchie de vues du fragment.
     *
     * @param inflater Objet pour gonfler les vues
     * @param container Vue parent
     * @param savedInstanceState État sauvegardé
     * @return Vue racine du fragment
     */
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState
    ) {
        return inflater.inflate(R.layout.fragment_my_declaration, container, false);
    }

    /**
     * Appelé immédiatement après onCreateView().
     /*
     * Configure tous les composants de l'interface utilisateur:
     * 1. Initialise les références aux vues
     * 2. Configure le RecyclerView avec son adapter
     * 3. Configure les boutons d'action (nouvelle déclaration, liste)
     * 4. Configure les boutons de filtre
     * 5. Configure les observateurs LiveData
     *
     * @param view Vue retournée par onCreateView()
     * @param savedInstanceState État sauvegardé
     */
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Séquence d'initialisation complète
        initViews(view);              // Liaison des vues
        setupRecyclerView(view);      // Configuration du RecyclerView
        setupActionButtons(view);     // Configuration des boutons d'action
        setupFilterButtons();         // Configuration des filtres
        observeViewModel();           // Observation des LiveData
    }

    /**
     * Appelé lorsque le fragment redevient visible et interactif.
     /*
     * Rafraîchit automatiquement les données car:
     * - L'utilisateur peut revenir après avoir créé une déclaration
     * - Le statut d'une déclaration peut avoir été modifié par un admin
     * - D'autres changements peuvent avoir eu lieu
     /*
     * Le rafraîchissement garantit que l'utilisateur voit toujours
     * ses données les plus récentes.
     */
    @Override
    public void onResume() {
        super.onResume();

        // Rechargement automatique des données avec le filtre actuel
        viewModel.refresh();
    }
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (getActivity() != null) {
            getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
        }
    }

    // ==================== INITIALISATION DES VUES ====================

    /**
     * Initialise toutes les références aux vues de l'interface.
     /*
     * Lie les variables Java aux éléments XML via findViewById.
     * Seuls les boutons de filtrage sont initialisés ici.
     * Les boutons d'action sont initialisés dans setupActionButtons().
     *
     * @param view Vue racine contenant tous les composants
     */
    private void initViews(View view) {
        // Boutons de filtrage par statut
        btnAll = view.findViewById(R.id.btnAll);
        btnPending = view.findViewById(R.id.btnPending);
        btnVerification = view.findViewById(R.id.btnVerification);
        btnRecovered = view.findViewById(R.id.btnRecovered);

        // Bouton de rafraîchissement
        btnRefresh = view.findViewById(R.id.btnRefresh);
    }

    // ==================== CONFIGURATION DU RECYCLERVIEW ====================

    /**
     * Configure le RecyclerView pour afficher la liste des déclarations.
     /*
     * Configuration:
     * - LayoutManager: LinearLayoutManager vertical (liste défilante)
     * - Adapter: TableAdapter avec callback pour suppression
     * - Données initiales: Liste vide (sera remplie par l'observateur)
     /*
     * CALLBACK DE SUPPRESSION:
     * L'adapter appelle showDeleteDialog() quand l'utilisateur clique
     * sur une ligne du tableau. Cela permet de confirmer avant suppression.
     /*
     * SÉPARATION DES RESPONSABILITÉS:
     * - RecyclerView: Affichage et défilement
     * - Adapter: Gestion des données et binding
     * - Fragment: Gestion des interactions (suppression, navigation)
     * - ViewModel: Logique métier et accès aux données
     *
     * @param view Vue racine contenant le RecyclerView
     */
    private void setupRecyclerView(View view) {
        // Récupération de la référence au RecyclerView
        RecyclerView recycler = view.findViewById(R.id.recyclerMyDeclarations);

        // Configuration du LayoutManager pour affichage vertical
        recycler.setLayoutManager(new LinearLayoutManager(requireContext()));

        // Initialisation de l'adapter avec configuration complète
        adapter = new TableAdapter<>(
                requireContext(),              // Contexte
                viewModel.getColumns(),        // Définition des colonnes
                viewModel.getRows(),           // Données initiales (liste vide)
                this::showDeleteDialog         // Callback pour clic sur ligne
        );

        // Attachement de l'adapter au RecyclerView
        recycler.setAdapter(adapter);
    }

    // ==================== CONFIGURATION DES BOUTONS D'ACTION ====================

    /**
     * Configure les boutons d'action principaux.
     *
     * BOUTONS:
     * 1. Nouvelle déclaration: Ouvre le formulaire de déclaration
     * 2. Liste des objets perdus: Ouvre la liste complète des objets
     *
     * Ces boutons permettent la navigation entre les différentes
     * sections de l'application.
     *
     * @param view Vue racine contenant les boutons
     */
    private void setupActionButtons(View view) {
        Button btnNewDeclaration = view.findViewById(R.id.buttonNewDeclaration);
        Button btnLostList = view.findViewById(R.id.buttonLostList);

        // Bouton "Nouvelle déclaration"
        btnNewDeclaration.setOnClickListener(v -> openNewDeclaration());

        // Bouton "Liste des objets perdus"
        btnLostList.setOnClickListener(v -> openLostList());
    }

    // ==================== CONFIGURATION DES FILTRES ====================

    /**
     * Configure les écouteurs de clics pour tous les boutons de filtrage.
     *
     * BOUTONS DE FILTRAGE:
     * Chaque bouton de filtre:
     * 1. Applique le filtre correspondant via le ViewModel
     * 2. Met à jour l'interface pour montrer le bouton actif
     *
     * FILTRE PAR DÉFAUT:
     * "Tous" est sélectionné par défaut contrairement à UserLostList
     * qui utilise "En attente" car ici l'utilisateur veut probablement
     * voir toutes ses déclarations, pas seulement celles en attente.
     *
     * BOUTON DE RAFRAÎCHISSEMENT:
     * Permet de recharger manuellement les données sans changer de filtre.
     * Utile pour vérifier les changements de statut.
     */
    private void setupFilterButtons() {

        // ========== BOUTON "TOUS" ==========
        /**
         * Affiche toutes les déclarations de l'utilisateur.
         * Passe null au ViewModel pour désactiver le filtrage.
         */
        btnAll.setOnClickListener(v -> {
            viewModel.filterByStatus(null); // null = toutes les déclarations
            highlightSelectedFilter(btnAll);
        });

        // ========== BOUTON "EN ATTENTE" ==========
        /**
         * Filtre les déclarations avec le statut "En attente".
         * Utile pour voir ce qui nécessite encore une action.
         */
        btnPending.setOnClickListener(v -> {
            viewModel.filterByStatus("En attente");
            highlightSelectedFilter(btnPending);
        });

        // ========== BOUTON "EN VÉRIFICATION" ==========
        /**
         * Filtre les déclarations en cours de vérification.
         * Permet de suivre les déclarations en cours de traitement.
         */
        btnVerification.setOnClickListener(v -> {
            viewModel.filterByStatus("En cours de vérification");
            highlightSelectedFilter(btnVerification);
        });

        // ========== BOUTON "RÉCUPÉRÉ" ==========
        /**
         * Filtre les objets déjà récupérés.
         * Permet de consulter l'historique des objets retrouvés.
         */
        btnRecovered.setOnClickListener(v -> {
            viewModel.filterByStatus("Récupéré");
            highlightSelectedFilter(btnRecovered);
        });

        // ========== BOUTON "RAFRAÎCHIR" ==========
        /**
         * Recharge les données sans changer le filtre actuel.
         * Utile pour vérifier les mises à jour de statut.
         */
        btnRefresh.setOnClickListener(v -> viewModel.refresh());

        // ========== SÉLECTION PAR DÉFAUT ==========
        // Active le filtre "Tous" au démarrage
        // L'utilisateur voit toutes ses déclarations par défaut
        highlightSelectedFilter(btnAll);
    }

    /**
     * Met en évidence le bouton de filtre sélectionné.
     *
     * Stratégie visuelle:
     * - Tous les boutons sont réactivés (enabled = true)
     * - Le bouton sélectionné est désactivé (enabled = false)
     *
     * Avantages:
     * - Indication visuelle claire du filtre actif
     * - Empêche de cliquer plusieurs fois sur le même filtre
     * - Utilise les styles Android par défaut (pas de code supplémentaire)
     *
     * L'apparence change automatiquement car Android applique
     * différents styles aux boutons enabled/disabled.
     *
     * @param selectedButton Le bouton qui vient d'être sélectionné
     */
    private void highlightSelectedFilter(Button selectedButton) {
        // Réactivation de tous les boutons
        btnAll.setEnabled(true);
        btnPending.setEnabled(true);
        btnVerification.setEnabled(true);
        btnRecovered.setEnabled(true);

        // Désactivation du bouton sélectionné pour indication visuelle
        selectedButton.setEnabled(false);
    }

    // ==================== OBSERVATION DES DONNÉES ====================

    /**
     * Configure les observateurs LiveData pour réagir aux changements de données.
     *
     * OBSERVATEUR 1 - DÉCLARATIONS:
     * - Se déclenche lors du chargement initial
     * - Se déclenche lors du changement de filtre
     * - Se déclenche après suppression réussie
     * - Met à jour le RecyclerView automatiquement
     *
     * OBSERVATEUR 2 - RÉSULTAT DE SUPPRESSION:
     * - Se déclenche après tentative de suppression
     * - Affiche un message Toast (succès ou erreur)
     * - Permet de donner un feedback immédiat à l'utilisateur
     *
     * AVANTAGES DE LIVEDATA:
     * - Mises à jour automatiques de l'interface
     * - Respect du cycle de vie du fragment
     * - Pas de fuites mémoire
     * - Code déclaratif et maintenable
     */
    private void observeViewModel() {

        // ========== OBSERVATEUR 1: DÉCLARATIONS ==========
        /**
         * Observer la liste des déclarations pour mettre à jour l'interface.
         *
         * Déclencheurs:
         * - Chargement initial dans le constructeur du ViewModel
         * - Changement de filtre via filterByStatus()
         * - Rafraîchissement via refresh()
         * - Suppression réussie (rechargement automatique)
         */
        viewModel.getDeclarationsLiveData().observe(getViewLifecycleOwner(), declarations -> {
            if (declarations != null) {
                // Mise à jour des données de l'adaptateur
                // L'adapter notifie automatiquement le RecyclerView
                adapter.updateData(declarations);
            }
        });

        // ========== OBSERVATEUR 2: RÉSULTAT DE SUPPRESSION ==========
        /**
         * Observer le résultat de suppression pour feedback utilisateur.
         *
         * Déclencheurs:
         * - Après appel à deleteDeclaration() dans le ViewModel
         *
         * Actions:
         * - Affiche un Toast avec le message de résultat
         * - Le rechargement des données est géré automatiquement
         *   par le ViewModel en cas de succès
         */
        viewModel.getDeleteResultLiveData().observe(getViewLifecycleOwner(), result -> {
            if (result != null) {
                // Affichage du message (succès ou erreur)
                Toast.makeText(requireContext(), result.getMessage(), Toast.LENGTH_SHORT).show();

                // Note: Le rechargement des données est déjà géré
                // par le ViewModel en cas de succès
            }
        });
    }

    // ==================== GESTION DE LA SUPPRESSION ====================

    /**
     * Affiche une boîte de dialogue pour confirmer la suppression d'une déclaration.
     *
     * Cette méthode est appelée par le TableAdapter quand l'utilisateur
     * clique sur une ligne du tableau.
     *
     * PROCESSUS:
     * 1. Extraction des informations de la ligne (ID, type, description)
     * 2. Affichage d'un AlertDialog avec les détails
     * 3. Si confirmation: suppression via le ViewModel
     * 4. Si annulation: fermeture du dialogue
     *
     * SÉCURITÉ:
     * - Confirmation obligatoire avant suppression
     * - Gestion des erreurs de parsing de l'ID
     * - Affichage des détails pour éviter les suppressions accidentelles
     *
     * EXPÉRIENCE UTILISATEUR:
     * - L'utilisateur voit exactement ce qu'il va supprimer
     * - Double confirmation (clic + dialogue) évite les erreurs
     * - Messages d'erreur clairs en cas de problème
     *
     * @param row Ligne du tableau cliquée, contenant les données de la déclaration
     */
    private void showDeleteDialog(TableRow row) {
        // ========== EXTRACTION DES DONNÉES ==========

        // Structure de row.cells():
        // Index 0: ID de la déclaration
        // Index 1: Type de l'objet
        // Index 2: Description
        // Index 3: Date
        // Index 4: Statut
        // Index 5: Code couleur
        String declarationId = row.cells().get(0);
        String type = row.cells().get(1);
        String description = row.cells().get(2);

        // ========== AFFICHAGE DU DIALOGUE DE CONFIRMATION ==========

        new AlertDialog.Builder(requireContext())
                .setTitle("Supprimer la déclaration")

                // Message avec détails de la déclaration
                .setMessage("Voulez-vous supprimer cette déclaration ?\n\n" +
                        "Type: " + type + "\n" +
                        "Description: " + description)

                // Bouton de confirmation
                .setPositiveButton("Oui", (dialog, which) -> {
                    try {
                        // Conversion de l'ID en entier
                        int id = Integer.parseInt(declarationId);

                        // Appel au ViewModel pour suppression
                        // Le ViewModel gère:
                        // - La suppression des images associées
                        // - La suppression de la déclaration
                        // - Le rechargement automatique
                        // - La notification du résultat
                        viewModel.deleteDeclaration(id);

                    } catch (NumberFormatException e) {
                        // Gestion des erreurs de parsing
                        // Ne devrait normalement jamais arriver
                        Toast.makeText(requireContext(),
                                "Erreur: ID invalide",
                                Toast.LENGTH_SHORT).show();
                    }
                })

                // Bouton d'annulation (null = fermeture simple du dialogue)
                .setNegativeButton("Non", null)

                // Affichage du dialogue
                .show();
    }

    // ==================== NAVIGATION ====================

    /**
     * Ouvre le fragment pour déclarer un nouvel objet perdu.
     *
     * Navigation:
     * - Remplace le fragment actuel par UserDeclareObjectFrom
     * - Ajoute à la pile de retour (addToBackStack)
     * - L'utilisateur peut revenir avec le bouton retour
     *
     * Flux utilisateur:
     * 1. L'utilisateur clique sur "Nouvelle déclaration"
     * 2. Il est redirigé vers le formulaire de déclaration
     * 3. Après soumission, il peut revenir ici
     * 4. Les données sont rafraîchies automatiquement (onResume)
     */
    private void openNewDeclaration() {
        getParentFragmentManager()
                .beginTransaction()
                .replace(R.id.main_user_container, UserDeclareObjectFrom.newInstance())
                .addToBackStack(null)  // Permet le retour avec bouton back
                .commit();
    }

    /**
     * Ouvre le fragment affichant la liste complète des objets perdus.
     *
     * Navigation:
     * - Remplace le fragment actuel par UserLostList
     * - Ajoute à la pile de retour (addToBackStack)
     * - L'utilisateur peut revenir avec le bouton retour
     *
     * Cas d'usage:
     * - L'utilisateur veut consulter tous les objets perdus
     * - Il cherche un objet qu'il a peut-être perdu
     * - Il veut comparer avec ses propres déclarations
     *
     * Flux utilisateur:
     * 1. L'utilisateur clique sur "Liste des objets perdus"
     * 2. Il est redirigé vers la liste complète
     * 3. Il peut revenir à ses déclarations avec le bouton retour
     */
    private void openLostList() {
        getParentFragmentManager()
                .beginTransaction()
                .replace(R.id.main_user_container, UserLostList.newInstance())
                .addToBackStack(null)  // Permet le retour avec bouton back
                .commit();
    }


}