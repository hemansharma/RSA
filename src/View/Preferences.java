package View;

import Controler.ControlerClass;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.*;

public class Preferences
{
    
    private JPasswordField AncienMDP;
    private JPasswordField NouveauMDP1;
    private JPasswordField NouveauMDP2;
    private JTextArea JTAavert;
    private JSlider NiveauCryptage;
    private JButton Changer;
    private JPanel container;
    private JPanel PanelHaut;
    private JPanel PanelBas;
    private JPanel PanelAMDP;
    private JPanel PanelNMDP1;
    private JPanel PanelNMDP2;
    private JPanel PanelNC;
    private ControlerClass controler;
    private String avertissement;

    public Preferences(ControlerClass c)
    {
        // On nstancie les composants
        AncienMDP = new JPasswordField();
        NouveauMDP1 = new JPasswordField();
        NouveauMDP2 = new JPasswordField();
        JTAavert = new JTextArea();
        NiveauCryptage = new JSlider();
        Changer = new JButton("Changer");
        container = new JPanel();
        PanelHaut = new JPanel();
        PanelBas = new JPanel();
        PanelAMDP = new JPanel();
        PanelNMDP1 = new JPanel();
        PanelNMDP2 = new JPanel();
        PanelNC = new JPanel();
        avertissement = "Le logiciel utilise le cryptage RSA pour le chiffrement de vos données. "
                + "Il n'est donc pas nécessaire de connaitre le mot de passe pour crypter. "
                + "\n Le fichier ParametreDefaut est très important ne le perdez, et vieillez à ce qu'il "
                + "soit toutjours dans le même dossier que l'application. "
                + "\n Le changement de mot de passe est irréverssible est ne permerttera plus de décrypter "
                + "les données cryptées avec l'ancien mot de passe. "
                + "Pour plus d'information merci de consulter le bog 2frogblog.wordpress.com ou de consulter "
                + "github.com/FrancoisJ/Source_RSA pour voir les sources et un tutoriel sur ce logiciel.";
        controler = c;
        
        // On configure la JPanel principal
        container.setLayout(new GridLayout(2, 1));
        container.add(PanelHaut);
        container.add(PanelBas);
        
        // On construit le JPanel du haut (changement du mot de passe)
        PanelHaut.setLayout(new GridLayout(5, 1));
        PanelHaut.add(PanelAMDP);
        PanelHaut.add(PanelNMDP1);
        PanelHaut.add(PanelNMDP2);
        PanelHaut.add(PanelNC);
        PanelHaut.add(Changer);
        PanelHaut.setBorder(BorderFactory.createTitledBorder("Changer de mot de passe"));
        
        // On cnostruit le champ de text de l'ancient MDP
        PanelAMDP.add(new JLabel("Ancien Mot de passe"));
        PanelAMDP.add(AncienMDP);
        AncienMDP.setColumns(10);
        
        // On cnostruit le champ de text du nouveau MDP 1
        PanelNMDP1.add(new JLabel("Nouveau mot de passe"));
        PanelNMDP1.add(NouveauMDP1);
        NouveauMDP1.setColumns(10);
         
        // On cnostruit le champ de text du nouveau MDP 2
        PanelNMDP2.add(new JLabel("Confirmez mot de passe"));
        PanelNMDP2.add(NouveauMDP2);
        NouveauMDP2.setColumns(10);
        
        // On construit le JSlider qui permet de choisir le degres du cryptage
        PanelNC.add(new JLabel("Choissiez le niveau de cryptage"));
        PanelNC.add(NiveauCryptage);
        
        NiveauCryptage.setMaximum(120);
        NiveauCryptage.setMinimum(30);
        NiveauCryptage.setMajorTickSpacing(10);
        NiveauCryptage.setSnapToTicks(true);
        NiveauCryptage.setValue(80);
        
        // On met sur écoute le bouton
        Changer.addActionListener(new ChangerListener());
        
        // On construit le JPanel du bas (le texte)
        PanelBas.add(JTAavert);
        PanelBas.setBorder(BorderFactory.createTitledBorder("Informations sur le logiciel"));
        JTAavert.setText(avertissement);
        JTAavert.setEditable(false);
        JTAavert.setLineWrap(true);
        JTAavert.setWrapStyleWord(true);
        JTAavert.setColumns(30);
        JTAavert.setBackground(PanelBas.getBackground());
    }

    public JPanel getJPanel()
    {
        return container;
    }

    

    private class ChangerListener implements ActionListener
    {

        public void actionPerformed(ActionEvent e)
        {
            boolean b = controler.nouveauMDP(new String(AncienMDP.getPassword()) ,new String(NouveauMDP1.getPassword()), new String(NouveauMDP2.getPassword()), NiveauCryptage.getValue());
            if(b)
            {
                JOptionPane jop = new JOptionPane();
                JOptionPane _tmp = jop;
                JOptionPane.showConfirmDialog(null, "Le Changement a bien \351t\351 pris en compte", "Preferences", -1);
            } else
            {
                JOptionPane jop = new JOptionPane();
                JOptionPane _tmp1 = jop;
                JOptionPane.showConfirmDialog(null, "Votre ancien mot de passe est incorrect.\nOu vos nouveaux mots de passe ne sont pas identiques", "Preferences", -1);
            }
        }

    }






}
