package View;

import Controler.ControlerClass;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.File;
import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.tree.DefaultMutableTreeNode;


public class FenetrePrincipale extends JFrame
{
    private JSlider NbrePro;
    private JTabbedPane onglets;
    private JPanel container;
    private JPanel Centre;
    private JPanel Nord;
    private JPasswordField pw;
    private ControlerClass controler;
    private Preferences preferences;
    private JTree tree;
    
    private String titre = "Secure Box";
    
    public FenetrePrincipale(ControlerClass c)
    {
        // On instancie les composants
        NbrePro = new JSlider();
        onglets = new JTabbedPane();
        container = new JPanel();
        Centre = new JPanel();
        Nord = new JPanel();
        pw = new JPasswordField();
        tree = new JTree(listRoot(System.getProperty("user.dir")));
        
        // On cree le JPanel preference
        controler = c;
        preferences = new Preferences(controler);
        
        //On configure la fenetre
        setSize(450, 600);
        setTitle(titre);
        setDefaultCloseOperation(3);
        setLocationRelativeTo(null);
        getContentPane().add(onglets);
        setVisible(true);
        
        // On configure les onglets
        onglets.add("Crypter/Decrypter", container);
        onglets.add("Pr\351f\351rences", preferences.getJPanel());
        
        // On construit la JPanel principal
        container.setLayout(new BorderLayout());
        container.add(Nord, "North");
        container.add(Centre, "Center");
        // On construit le JPanel central (l'arbre)
        JScrollPane jsp = new JScrollPane(tree);
        jsp.setBorder(BorderFactory.createTitledBorder("Votre Nuage"));
        
        // On active le drop sur l'arbre
        tree.setTransferHandler(new MyTransferHandler(tree, controler));
        // On met sur ecoute l'arbre pour reperer quand on clique dessus
        tree.addMouseListener(new MouseListener() {

            // Si on clique sur un element de l'abre
            public void mouseClicked(MouseEvent me) {
                try {
                    // On recupere le chemin du fichier selectionne
                    Object[] object = tree.getSelectionPath().getPath();
                    String fichier_cible = System.getProperty("user.dir");
                    for(int i=1; i<object.length; i++)
                        fichier_cible += '/'+ object[i].toString();

                    // Si il s'agit bien d'un fichier, on le decrypte et on l'ouvre
                    if( (new File(fichier_cible).isFile()) ) {
                        
                        // On recupere le mot de passe
                        JOptionPane.showConfirmDialog(null, pw, "Mot de passe", 2);
                        boolean rep = controler.decrypter(fichier_cible, System.getProperty("user.home")+"/Documents", new String(pw.getPassword()), true);
                        if(!rep)
                            JOptionPane.showConfirmDialog(null, "Mauvais mot de passe", titre,-1);
                    }
                    
                } catch(NullPointerException e)
                {
                    e.printStackTrace();
                }
            }
            

            @Override
            public void mousePressed(MouseEvent me) {
                // Rien faire
            }

            @Override
            public void mouseReleased(MouseEvent me) {
                // Rien faire
            }

            @Override
            public void mouseEntered(MouseEvent me) {
                // Rien faire
            }

            @Override
            public void mouseExited(MouseEvent me) {
                // Rien faire
            }
            
        });
        
        Centre.setLayout(new GridLayout(1, 2));
        Centre.add(jsp);
        
        // On construit le JPanel du haut (le choix des coeurs)
        Nord.add(new JLabel("Nombre de Coeurs utilis\351s"));
        Nord.add(NbrePro);
        
        // On configure le JSlider pour choisir les coeurs
        NbrePro.setMaximum(4);
        NbrePro.setMinimum(1);
        NbrePro.setMajorTickSpacing(1);
        NbrePro.setSnapToTicks(true);
        NbrePro.setPaintLabels(true);
        // On met sur ecoute le JSlider
        NbrePro.addChangeListener(new ChangeListener() {

            @Override
            public void stateChanged(ChangeEvent ce) 
            {
                controler.setNbrePro(NbrePro.getValue());
            }
            
        });
        
    }
    
    // Permet de lister les dossiers et fichiers dans l'arbre
    private DefaultMutableTreeNode listRoot(String path)
    {
        File file = new File(path);
        DefaultMutableTreeNode root = new DefaultMutableTreeNode(file.getName());
        if(file.listFiles() != null) 
        {
            for(File f : file.listFiles()) {
                if(f.isFile() && !f.isHidden())
                    root.add(new DefaultMutableTreeNode(f.getName()));
                else if(f.isDirectory() && !f.isHidden())
                    root.add(listRoot(f.getAbsolutePath()));
            }
        }
        
        return root;
        
    }
  
}
