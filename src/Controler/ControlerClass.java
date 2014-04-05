
package Controler;

import java.awt.Desktop;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import securebox.RSA;
import securebox.Stream;

public class ControlerClass
{

    // tableau de bytes COMPLETEMENT ALEATOIRE pour valider l'identiter de l'utilisateur
    private byte arrayTest[] = {-20,46,58,39,-118,62,47,15,-70,66,118,26,-107,120,-123,
                                104,-5,-84,44,34,19,-70,60,17,104,89,96,74,123,-12,
                                -105,95,79,120,-2,-14,-86,75,-121,85,-76,-114,-92,126,-53,
                                -107,-2,110,83,-45,-106,2,-102,22,-14,-116,-10,-82,98,80,
                                121,62,-125,127,-50,90,-44,-13,51,-66,61,-26,-112,-14,57,
                                -16,76,59,100,96,-114,126,16,110,59,84,-84,63,51,-78,
                                -82,-102,-18,-12,66,97,-124,124,97};
    
    private int nbrePro = 4;
    private String DossierDeLancement;
    private String C;
    private String N;
    private byte temoin[];
    private Stream ParametreDefaut;
    
    public ControlerClass()
    {        
        DossierDeLancement = System.getProperty("user.dir");
        ParametreDefaut = new Stream(DossierDeLancement+"/ParametreDefaut");
        
        // On recupere les parametre de l'utilisateur, il y a
        // - la clef publique comprennant le nombre C et N
        // - l'image du string test crypte avec le mot de passe officiel
        byte trame[] = ParametreDefaut.read();
        int index = 0;
        //On extrait le taille du nombre C
        int C_length = trame[index];
        index++;
        // On extrait le nombre C
        byte tampon[] = new byte[C_length];
        for(int i=0; i<tampon.length; i++) {
            tampon[i] = trame[index];
            index++;
        }
        C = new String(tampon);
        
        // On extrait la taille du nombre N
        int N_length = trame[index];
        index++;
        // On extrait le nombre N
        tampon = new byte[N_length];
        for(int i=0; i<tampon.length; i++) {
            tampon[i] = trame[index];
            index++;
        }
        N = new String(tampon);
        
        System.out.println(C+'-'+N);
        
        // On recupere le tableau de byte temoin
        temoin = new byte[trame.length- index];
        for(int i=0; i<temoin.length; i++) {
            temoin[i] = trame[index];
            index++;
        }
        
        
    }

    public void crypter(String entre, String sortie)
    {
        File file = new File(sortie);
        
        // Si le fichie sortant est un dossier
        if(file.isDirectory())
        {
            File e = new File(entre);
            // On ajoute cree un fichier de même nom que le fichier
            // entrant plus l'extension .cryp
            sortie = sortie+"/"+e.getName()+".cryp";
        }
        // On instancie l'objet RSA
        RSA rsa = new RSA(C, N);
        // On ouvre les flux de donnees
        Stream Input = new Stream(entre);
        Stream Output = new Stream(sortie);
        // On fixe le nombre de coeurs
        rsa.setNbrePro(nbrePro);
        // On crypte les donnees
        Output.write(rsa.crypted(Input.read()));
    }

    public boolean decrypter(String entre, String sortie, String MDP, boolean ouvrir)
    {
        // On verifie l'identite de l'utilisateur
        if(ValiderMDP(MDP))
        {
            File file = new File(sortie);
            // Si le fichier sortant est un dossier
            if(file.isDirectory())
            {
                File f = new File(entre);
                String nameFile = f.getName();
                String extensionFile = nameFile.substring(nameFile.length() - 5);
                // On verifie que le fichier entrant n'a pas l'extension .cryp
                // Si oui on la retire
                if(extensionFile.equals(".cryp"))
                    sortie = sortie+"/"+nameFile.substring(0, nameFile.length() - 5);
                else
                    sortie = sortie+"/"+nameFile;
            }
            
            // On instancie l'objet RSA
            RSA rsa = new RSA(C, N, MDP);
            // On ouvre les flux
            Stream Output = new Stream(sortie);
            Stream Input = new Stream(entre);
            // On fixe le nombre de coeurs
            rsa.setNbrePro(nbrePro);
            // on decrypte le fichier
            Output.write(rsa.decrypted(Input.read()));
            
            // Si on demande d'ouvrir le fichier
            if(ouvrir) {
                try {
                    Desktop desktop = Desktop.getDesktop();
                    desktop.open(new File(sortie));
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
            
            return true;
            
        } else
            return false;
    }

    public boolean nouveauMDP(String AMDP, String NMDP1, String NMDP2, int NC)
    {
        // On verifie que l'ancien mot de passe est le bon
        // et que les deux nouveaux mot de passe sont identiques
        if(ValiderMDP(AMDP) && NMDP1.equals(NMDP2))
        {
            
            RSA rsa = new RSA("12345","12345");
            rsa.setNbrePro(1);
            // On demande a l'objet RSA de construire des 
            // nouvelles clefs avec le nouveau mot de passe
            String strParametreDefaut[] = rsa.creatClefs(NMDP1, NC);
            C = strParametreDefaut[0];
            N = strParametreDefaut[1];
            byte arrayC[] = C.getBytes();
            byte arrayN[] = N.getBytes();
            temoin = rsa.crypted(arrayTest);
            
            System.out.println(C+"-"+N);
            
            // On additionne les trois tableaux comme suit
            // taille_arrayC + arrayC + taille_arrayN + arrayN + test
            List<Byte> list = new ArrayList<Byte>(arrayC.length+arrayN.length+temoin.length+2);
            Collections.addAll(list, (byte) arrayC.length);
            for(byte b : arrayC)
                Collections.addAll(list, b);
            Collections.addAll(list, (byte) arrayN.length);
            for(byte b: arrayN)
                Collections.addAll(list, b);
            for(byte b: temoin)
                Collections.addAll(list, b);
            
            
            // On enregistre les nouveaux parametres
            byte result[] = new byte[list.size()];
            for(int i=0; i<result.length; i++)
                result[i] = list.get(i);
            ParametreDefaut.write(result);
            
            return true;
            
        } else
            return false;
    }

    public boolean ValiderMDP(String MDP)
    {
        RSA rsa = new RSA(C, N, MDP);
        rsa.setNbrePro(1);
        // Pour valider le mot de passe, on decrypte l'image
        // du tableau test avec le mot de passe donne
        byte array[] = rsa.decrypted(temoin);
        // Si le resultat est identique au tableau test
        // alors le mot de passe donne est le meme qui a
        // servi pour faire les clefs, c'est donc le bon
        return java.util.Arrays.equals(array, arrayTest);
    }

    public void setNbrePro(int i)
    {
        nbrePro = i;
    }

}
