package trees;

import java.util.*;
import java.io.*;
import javax.swing.*;
import javax.swing.filechooser.*;

public class FamilyTree {

    private static class TreeNode {
        private String name;
        private TreeNode parent;
        private ArrayList<TreeNode> children;

        TreeNode(String name) {
            this.name = name;
            children = new ArrayList<>();
        }

        String getName() {
            return name;
        }

        void addChild(TreeNode childNode) {
            children.add(childNode);
            childNode.parent = this;
        }

        // Searches subtree at this node for a node
        // with the given name. Returns the node, or null if not found.
        TreeNode getNodeWithName(String targetName) {
            // Does this node have the target name?
            if (name.equals(targetName))
                return this;
                    
            // No, recurse. Check all children of this node.
            for (TreeNode child: children)
            {
                TreeNode found = child.getNodeWithName(targetName);
                if (found != null)
                    return found;
            }
            
            // Not found anywhere.
            return null;
        }

        // Returns a list of ancestors of this TreeNode, starting with this node’s
        // parent and
        // ending with the root. Order is from recent to ancient.
        ArrayList<TreeNode> collectAncestorsToList() {
            ArrayList<TreeNode> ancestors = new ArrayList<>();

            TreeNode current = parent;
            while (current != null) {
                ancestors.add(current);
                current = current.parent;
            }

            return ancestors;
        }

        public String toString() {
            return toStringWithIndent("");
        }

        private String toStringWithIndent(String indent) {
            String s = indent + name + "\n";
            indent += "  ";
            for (TreeNode childNode : children)
                s += childNode.toStringWithIndent(indent);
            return s;
        }
    }

    private TreeNode root;

    //
    // Displays a file browser so that user can select the family tree file.
    //
    public FamilyTree() throws IOException, TreeException {
        // User chooses input file.
        FileNameExtensionFilter filter =
            new FileNameExtensionFilter("Family tree text files", "txt");
        File dirf = new File("data");
        if (!dirf.exists()) dirf = new File(".");

        JFileChooser chooser = new JFileChooser(dirf);
        chooser.setFileFilter(filter);
        if (chooser.showOpenDialog(null) != JFileChooser.APPROVE_OPTION)
            System.exit(1);

        File treeFile = chooser.getSelectedFile();

        // Read the input file.
        FileReader fr = new FileReader(treeFile);
        BufferedReader br = new BufferedReader(fr);

        String line;
        while ((line = br.readLine()) != null)
            addLine(line);

        br.close();
        fr.close();
    }

    private void addLine(String line) throws TreeException {
        // Extract parent and children.
        int colonIndex = line.indexOf(':');

        if (colonIndex < 0)
            throw new TreeException("Invalid line: missing colon.");

        String parent = line.substring(0, colonIndex).trim();
        String childrenString = line.substring(colonIndex + 1).trim();
        String[] childrenArray = childrenString.split(",");

        // Find or create the parent node.
        TreeNode parentNode;

        if (root == null) {
            parentNode = root = new TreeNode(parent);
        } else {
            parentNode = root.getNodeWithName(parent);

            if (parentNode == null)
                throw new TreeException("Parent not found: " + parent);
        }

        // Add child nodes.
        for (String child : childrenArray) {
            TreeNode childNode = new TreeNode(child.trim());
            parentNode.addChild(childNode);
        }
    }

    TreeNode getMostRecentCommonAncestor(String name1, String name2)
            throws TreeException {

        // Get nodes for input names.
        TreeNode node1 = root.getNodeWithName(name1);

        if (node1 == null)
            throw new TreeException("Person not found: " + name1);

        TreeNode node2 = root.getNodeWithName(name2);

        if (node2 == null)
            throw new TreeException("Person not found: " + name2);

        // Get ancestors of both nodes.
        ArrayList<TreeNode> ancestorsOf1 = node1.collectAncestorsToList();
        ArrayList<TreeNode> ancestorsOf2 = node2.collectAncestorsToList();

        // Find the first shared ancestor.
        for (TreeNode n1 : ancestorsOf1) {
            if (ancestorsOf2.contains(n1))
                return n1;
        }

        // No common ancestor.
        return null;
    }

    public String toString() {
        return "Family Tree:\n\n" + root;
    }

    public static void main(String[] args) {
        try {
            FamilyTree tree = new FamilyTree();
            System.out.println("Tree:\n" + tree + "\n**************\n");
            TreeNode ancestor = tree.getMostRecentCommonAncestor("Bilbo", "Frodo");
            System.out.println("Most recent common ancestor of Bilbo and Frodo is " + ancestor.getName());
        } catch (IOException x) {
            System.out.println("IO trouble: " + x.getMessage());
        } catch (TreeException x) {
            System.out.println("Input file trouble: " + x.getMessage());
        }
    }
}
