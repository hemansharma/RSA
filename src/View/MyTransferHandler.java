/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package View;

import Controler.ControlerClass;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.File;
import java.io.IOException;
import java.util.List;
import javax.swing.JTree;
import javax.swing.TransferHandler;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;

/**
 *
 * @author francois
 */
public class MyTransferHandler extends TransferHandler 
{
    
    private JTree tree;
    private ControlerClass controler;
    
    public MyTransferHandler(JTree tree, ControlerClass c)
    {
        this.tree = tree;
        controler =c;
    }
    
    public boolean canImport(TransferHandler.TransferSupport info)
    {
        
        if (!info.isDataFlavorSupported(DataFlavor.javaFileListFlavor))
            return false;
        else
            return true;
    }

    public boolean importData(TransferHandler.TransferSupport support)
    {
        
        if(!canImport(support))
            return false;
        
        Transferable data = support.getTransferable();
        List<File> list = null;
        
        try 
        {
            list =  (List<File>) data.getTransferData(DataFlavor.javaFileListFlavor);
        } catch (UnsupportedFlavorException e)
        {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        
        JTree.DropLocation dl = (JTree.DropLocation) support.getDropLocation();
        TreePath path = dl.getPath();
        Object[] object = path.getPath();
        String fichier_cible = System.getProperty("user.dir");
        for(int i=1; i<object.length; i++)
            fichier_cible += '/'+ object[i].toString();
        
        for(File file : list) 
        {
            controler.crypter(file.getAbsolutePath(), fichier_cible);
            
            if(new File(fichier_cible).isDirectory()) 
            {
                // On ajoute un nouveau noeud dans l'arbre
                DefaultMutableTreeNode nouveau = new DefaultMutableTreeNode (file.getName()+".cryp");
                DefaultMutableTreeNode parent = (DefaultMutableTreeNode) path.getLastPathComponent();
                DefaultTreeModel model = (DefaultTreeModel) this.tree.getModel();

                int index = dl.getChildIndex();
                index = (index == -1) ? model.getChildCount(path.getLastPathComponent()) : index ;

                model.insertNodeInto(nouveau, parent, index);
                tree.makeVisible(path.pathByAddingChild(nouveau));
                tree.scrollPathToVisible(path);
            }
            
        }
        
        return true;
        
    }
    
    
}
