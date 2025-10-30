package io.github.android.gui;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

/**
 * {@code MyPagerAdapter} est un {@link RecyclerView.Adapter} minimaliste qui permet
 * d'afficher une série de layouts XML comme pages via un {@code RecyclerView}.
 * <p>
 * Chaque élément du pager est identifié par son identifiant de ressource de layout
 * (par exemple {@code R.layout.page_1}). L'adapter utilise cet identifiant comme
 * {@linkplain #getItemViewType(int) viewType} afin que {@link LayoutInflater} puisse
 * gonfler directement le layout correspondant.
 * </p>
 *
 * <h3>Principales caractéristiques</h3>
 * <ul>
 *   <li>Supporte un tableau d'identifiants de layout {@code int[] layouts}.</li>
 *   <li>Utilise {@link LayoutInflater#from(Context)} fourni via le constructeur.</li>
 *   <li>Conception simple sans logique supplémentaire dans {@link #onBindViewHolder} —
 *       les layouts sont supposés être statiques ou auto-suffisants.</li>
 * </ul>
 *
 * <h3>Exemple d'utilisation</h3>
 * <pre>
 * <code>
 * int[] pages = new int[] { R.layout.page_one, R.layout.page_two, R.layout.page_three };
 * MyPagerAdapter adapter = new MyPagerAdapter(context, pages);
 * recyclerView.setAdapter(adapter);
 * </code>
 * </pre>
 *
 * @author
 * @since 1.0
 */
public class MyPagerAdapter extends RecyclerView.Adapter<MyPagerAdapter.ViewHolder> {

    /**
     * Tableau des ressources de layout à afficher, chaque valeur doit être un identifiant
     * de ressource de type layout (ex. {@code R.layout.my_page}).
     *
     * <p><b>Remarque :</b> Les identifiants sont retournés par {@link #getItemViewType(int)}.</p>
     */
    private final int[] layouts;

    /**
     * {@link LayoutInflater} utilisé pour gonfler les layouts.
     */
    private final LayoutInflater inflater;

    /**
     * Crée une instance de {@code MyPagerAdapter}.
     *
     * @param context le {@link Context} utilisé pour obtenir le {@link LayoutInflater}.
     * @param layouts tableau d'identifiants de layout (ne doit pas être {@code null}).
     *                Chaque élément doit référencer un fichier XML de layout.
     * @throws NullPointerException si {@code context} ou {@code layouts} est {@code null}.
     *
     * <p>Exemple :</p>
     * <pre>
     * <code>
     * int[] pages = { R.layout.page1, R.layout.page2 };
     * new MyPagerAdapter(context, pages);
     * </code>
     * </pre>
     */
    public MyPagerAdapter(Context context, int[] layouts) {
        if (context == null) {
            throw new NullPointerException("context must not be null");
        }
        if (layouts == null) {
            throw new NullPointerException("layouts must not be null");
        }
        this.layouts = layouts;
        this.inflater = LayoutInflater.from(context);
    }

    /**
     * Crée et retourne un {@link ViewHolder} en gonflant le layout correspondant au
     * {@code viewType}. Ici {@code viewType} est attendu être l'identifiant du layout.
     *
     * @param parent   le parent {@link ViewGroup} dans lequel la nouvelle vue sera attachée.
     * @param viewType l'identifiant du layout (ex. {@code R.layout.my_page}).
     * @return un nouveau {@link ViewHolder} contenant la vue gonflée.
     */
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Inflate the layout for this page
        View view = inflater.inflate(viewType, parent, false);
        return new ViewHolder(view);
    }

    /**
     * Lie les données à la vue. Dans cette implémentation minimale, il n'y a pas
     * d'opération de liaison car les layouts sont supposés être autonomes.
     *
     * <p>Si vous devez initialiser des éléments (boutons, texte dynamique, etc.),
     * faites-le ici en utilisant {@code holder.itemView.findViewById(...)}.</p>
     *
     * @param holder   le {@link ViewHolder} contenant la vue à mettre à jour.
     * @param position la position de l'élément dans l'adapter.
     */
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        // Rien à faire ici, le layout est déjà affiché
        // Exemple : TextView tv = holder.itemView.findViewById(R.id.title);
        // tv.setText(...);
    }

    /**
     * Retourne le nombre de pages (layouts) gérées par l'adapter.
     *
     * @return le nombre total de layouts.
     */
    @Override
    public int getItemCount() {
        return layouts.length;
    }

    /**
     * Retourne le type de vue pour la position donnée. Cette implémentation
     * retourne l'identifiant du layout correspondant (par ex. {@code R.layout.page}).
     *
     * <p>Important : {@link #onCreateViewHolder} attend que {@code viewType} soit
     * un identifiant de layout valide.</p>
     *
     * @param position la position de l'élément.
     * @return l'identifiant du layout à gonfler pour cette position.
     */
    @Override
    public int getItemViewType(int position) {
        // Retourne le layout correspondant à la position
        return layouts[position];
    }

    /**
     * {@code ViewHolder} simple qui encapsule la vue de la page.
     *
     * <p>Si vous avez besoin d'accéder souvent à des sous-vues, créez ici des
     * champs pour les référencer et initialisez-les dans le constructeur afin
     * d'éviter des appels répétés à {@code findViewById}.</p>
     */
    static class ViewHolder extends RecyclerView.ViewHolder {
        /**
         * Crée un {@link ViewHolder} pour la vue donnée.
         *
         * @param itemView la vue racine du layout de la page.
         */
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
        }
    }
}
