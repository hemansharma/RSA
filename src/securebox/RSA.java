package securebox;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Random;
import java.util.Vector;

public class RSA
{
    private BigInteger P;
    private BigInteger Q;
    private BigInteger M;
    private BigInteger N;
    private BigInteger U;
    private BigInteger C;
    private int nbrePro = 4;
    private int DegresChiffrage;
    private int compression = 30;
    
    

    // Constructeur pour le cryptage seul
    public RSA(String strC, String strN)
    {
        C = new BigInteger(strC);
        N = new BigInteger(strN);
    }
    
    // Constructeur pour le cryptage et le decryptage
    public RSA(String strC, String strN, String mdp) 
    {
        
        C = new BigInteger(strC);
        N = new BigInteger(strN);
        Q =  BigInteger.ONE;

	// On transforme le mot de passe en nombre par le hachage SHA-256
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            Q = new BigInteger(md.digest(mdp.getBytes()));
            // Si Q est negatif, on prend l'opppose
            if (Q.compareTo(BigInteger.ZERO) == -1)
                Q = Q.multiply(new BigInteger("-1"));
            Q = reachPremier(Q);
            
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }

	// A l'aide de Q, N et C, on retrouve P, M et U
        P = N.divide(Q);
        M = P.subtract(BigInteger.ONE).multiply(Q.subtract(BigInteger.ONE));
        U = reachU(C, M);
    }

    public String[] creatClefs(String mdp, int DC)
    {
        DegresChiffrage = DC;
        P = new BigInteger(DegresChiffrage, new Random());
        P = reachPremier(P);
        
        //on transforme le mdp en nombre par le hachage SHA-256
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            Q = new BigInteger(md.digest(mdp.getBytes()));
            // Si Q est negatif, om prend l'oppose
            if (Q.compareTo(BigInteger.ZERO) == -1)
                Q = Q.multiply(new BigInteger("-1"));
            Q = reachPremier(Q);
            
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        
        // N = P*Q
        N = P.multiply(Q);
        // M = (P-1)(Q-1)
        M = P.subtract(BigInteger.ONE).multiply(Q.subtract(BigInteger.ONE));
        
        // On tire C au sort tant qu'il n'est pas premier avec M
        do
            C = new BigInteger(DegresChiffrage / 2, new Random());
        while(PGCD(C, M).compareTo(BigInteger.ONE) != 0);
        // On trouve U
        U = reachU(C, M);
        
        // On ajoute C et N dans le tableau que l'on va retourner
        String strClefPublic[] = {
            String.valueOf(C), String.valueOf(N)
        };
        return strClefPublic;
    }
    
    // Simple alogorithme de PGCD
    private BigInteger PGCD(BigInteger a, BigInteger b)
    {
        for(BigInteger r = a.mod(b); r != BigInteger.ZERO; r = a.mod(b))
        {
            a = b;
            b = r;
        }

        return b;
    }

    // Algorithme pour trouver U selon la relation M*U + C*V = 1
    private BigInteger reachU(BigInteger a, BigInteger b)
    {
        BigInteger r = a;
        BigInteger rr = b;
        BigInteger u = BigInteger.ONE;
        BigInteger v = BigInteger.ZERO;
        BigInteger uu = BigInteger.ZERO;
        BigInteger q;
        BigInteger vs;
        for(BigInteger vv = BigInteger.ONE; rr.compareTo(BigInteger.ZERO) != 0; vv = vs.subtract(q.multiply(vv)))
        {
            q = r.divide(rr);
            BigInteger rs = r;
            BigInteger us = u;
            vs = v;
            r = rr;
            u = uu;
            v = vv;
            rr = rs.subtract(q.multiply(rr));
            uu = us.subtract(q.multiply(uu));
        }

        for(; u.compareTo(BigInteger.ONE) != 1; u = u.add(M));
        return u;
    }

    // algorithme de primalite probable avec le petit theoreme de Fermat
    private BigInteger reachPremier(BigInteger bi)
    {
        int premier[] = {
            2, 3, 5, 7, 11, 13, 17, 23, 29, 31, 
            37, 41
        };
        BigInteger produit;
        do
        {
            bi = bi.add(BigInteger.ONE);
            produit = BigInteger.ONE;

            for(int i = 0; i < premier.length; i++)
            {
                int nbrePremier = premier[i];
                // produit = produit*( a^(p-1) % p)
                produit = produit.multiply(BigInteger.valueOf(nbrePremier).modPow(bi.subtract(BigInteger.ONE), bi));
            }

        } while(produit.compareTo(BigInteger.ONE) != 0);
        return bi;
    }
    
    // Méthode qui calcule en multicoeurs chaque nombre du tableau
    private Vector<BigInteger> startCalcul(Vector<BigInteger> vect, BigInteger exp, BigInteger mod) {
        
        Vector<BigInteger> tab = vect;
        
        // creation de la liste des processus selon le nombre souhaite
        ProcessusCalculs pc[] = new ProcessusCalculs[nbrePro];
        // determination du nombre de calculs par processus
        int nbreDeCalculsParPro = tab.size() / nbrePro;
        // Pour chaque processus
        for(int IndexPro = 0; IndexPro < pc.length; IndexPro++)
        {
            // On instancie le String à retourner
            // Il s'agit d'un StringBuffer pour plus de rapidité lors du l'ajout de caractère
            BigInteger partieTab[] = {BigInteger.ZERO};
            
            // Si c'est le dernier processus, il prend la part standard + toute la fin du tableau
            if(IndexPro == pc.length-1)
                 partieTab = tab.subList(IndexPro*nbreDeCalculsParPro, tab.size()).toArray(partieTab);
            // Sinon, il prend une part standard
            else
                partieTab = tab.subList(IndexPro*nbreDeCalculsParPro, (IndexPro+1)*nbreDeCalculsParPro).toArray(partieTab);
            
            // On assigne la tableau au processus et on le lance
            pc[IndexPro] = new ProcessusCalculs(partieTab, exp, mod);
            pc[IndexPro].start();
        }
        
        boolean CalculsTermines = false;
        // tant que tous les processus n'ont pas termine, on attend
        while(!CalculsTermines) 
        {
            CalculsTermines = true;
            for(ProcessusCalculs p : pc) {
                if(p.getState().compareTo(Thread.State.TERMINATED) != 0)
                    CalculsTermines &= false;
            }
        }
        
        // On nettoie le tableau
        tab.clear();
        
        // On va chercher les processus
        for(ProcessusCalculs p : pc) {
            tab.addAll(Arrays.asList(p.getTab()));
        }
        
        return tab;
        
    }

    
    public byte[] crypted(byte[] array)
    {
        
        // On compresse en fonction de la longeur de N
        compression = N.toByteArray().length -2;
        Vector<BigInteger> tab = new Vector<BigInteger>();
        
        int index = 0;
        // On va regrouper le tableau array en paquet de compression bytes
        while (index+compression < array.length) {
            
            //Les bytes nuls en debut de paquet posent probleme
            // On met chaque byte dans une case differente
            while(array[index] == 0)
            {
                tab.add(BigInteger.ZERO);
                index++;
            }
            
            // On instancie le paquet de byte
            byte[] partieArray = new byte[compression];
            // On fait correspondre les bytes du paquet et les bytes du tableau array
            for(int i = 0; i< partieArray.length; i++)
                partieArray[i] = array[index+i];
            
            tab.add(new BigInteger(1,partieArray));
            index += compression;
        }
        
        // On recupere les bytes de fin de tableau qui ne pouvais former un paquet complet
        byte[] partieArray = new byte[array.length - index];
        for(int i = 0; i<partieArray.length; i++)
            partieArray[i] = array[index+i];
        tab.add(new BigInteger(1,partieArray));
        
        
        // On crypte tous les nombres du tableau
        tab = startCalcul(tab, C, N);
        
        int bytesParNombre = N.toByteArray().length;
        Vector<Byte> tabByte = new Vector<Byte>();
        
        // Pour chaque nombre
        for(BigInteger bi : tab) {
            // on remplit artificiellement sa taille pour que tous les nombres
            // ont la meme taille
            for(int i=0; i< bytesParNombre - bi.toByteArray().length; i++)
                tabByte.add(new Byte("0"));

            for(byte b : bi.toByteArray())
                tabByte.add(b);
            
            
        }
        byte[] returnTab = new byte[tabByte.size()];
        for(int i=0; i<returnTab.length; i++) {
            returnTab[i] = tabByte.elementAt(i);
            
        }
        
        return returnTab;
        
    }
    
    public byte[] decrypted(byte[] array)
    {
        
        int bytesParNombre = N.toByteArray().length;
        Vector<BigInteger> tab = new Vector<BigInteger>();
        
        // On lit le tableau par paquet
        for(int index =0; index<array.length; index+= bytesParNombre) {
            
            // On isole chaque paquet dans un sous tableau
            byte[] partieArray = new byte[bytesParNombre];
            for(int i=0; i<partieArray.length; i++)
                partieArray[i] = array[index+i];
           
            
            // On ajoute le nombre cree avec ce sous tableau au vector
            tab.add(new BigInteger(1, partieArray));
            
        }
        
        // On decrypte chaque nombre du vector
        tab = startCalcul(tab, U, N);
        
        Vector<Byte> tabByte = new Vector<Byte>();
        
        // On enumere chaque nombre
        for(BigInteger bi : tab)
        {
            
            // On recupere les nombres nuls que l'on a traite separement
            if(bi.compareTo(BigInteger.ZERO) == 0)
                tabByte.add(new Byte("0"));

            // Sinon on ajoute chaque byte du numbre dans tabByte
            else 
            {
                byte bi_to_array[] = bi.toByteArray();
                for(int i=0; i<bi_to_array.length; i++) 
                {
                    byte b = bi_to_array[i];
                    // Lorsque l'on instancie une nombre avec new BigInteger(1, byte [])
                    // les BigIntegers negatifs sont transformes en BigInteger positifs
                    // pour cela un byte 0 est ajoute automatiquement au debut du byte[]
                    // Ce "if" est la pour ne pas prendre  en compte ce byte 0
                    if(b!=0 || i>0)
                        tabByte.add(b);
                }
            }
        }
        
        byte returnByte[] = new byte[tabByte.size()];
        for(int i=0; i<returnByte.length; i++)
            returnByte[i] = tabByte.elementAt(i);
        
        return returnByte;
        
    }

    public void setNbrePro(int i)
    {
        nbrePro = i;
    }

    // thread qui cacul pour tout son tableau X--> X^e %m
    private class ProcessusCalculs extends Thread
    {

        private BigInteger tab[];
        private BigInteger exposant;
        private BigInteger modulo;
        
        public ProcessusCalculs( BigInteger T[], BigInteger e, BigInteger m)
        {

            tab = T;
            exposant = e;
            modulo = m;
            
        }
        
        public void run()
        {
            for(int i = 0; i<tab.length; i++) 
            {
                if(tab[i] != null)
                    tab[i] = tab[i].modPow(exposant, modulo);
            }

        }

        public BigInteger[] getTab()
        {
            return tab;
        }
        
    }

}
