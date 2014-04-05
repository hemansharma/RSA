/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package securebox;

import Controler.ControlerClass;
import View.FenetrePrincipale;

/**
 *
 * @author francois
 */
public class SecureBox {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        // TODO code application logic here
        
        ControlerClass c = new ControlerClass();
        new FenetrePrincipale (c);
    }
}
