package gc.carbon.test;

import org.w3c.dom.Node;
import org.custommonkey.xmlunit.DifferenceListener;
import org.custommonkey.xmlunit.Difference;

public class UIDDifferenceListener implements DifferenceListener {

    public UIDDifferenceListener() {
    }


    public int differenceFound(Difference difference) {

        org.w3c.dom.Node node=difference.getControlNodeDetail().getNode();

        if (node.getNodeName().equals("uid")){
            return gc.carbon.test.UIDDifferenceListener.RETURN_IGNORE_DIFFERENCE_NODES_SIMILAR;
        }

        if (node.getNodeType()== Node.TEXT_NODE &&
            node.getParentNode().getNodeName().equals("Name") &&
            node.getParentNode().getParentNode().getNodeName().equals("ProfileItem")){
                return gc.carbon.test.UIDDifferenceListener.RETURN_IGNORE_DIFFERENCE_NODES_SIMILAR;
        }

        //System.out.println(node.getNodeName() + " => " + difference.getDescription() + " => " + node.getNodeValue());

        return gc.carbon.test.UIDDifferenceListener.RETURN_ACCEPT_DIFFERENCE;
    }

    public void skippedComparison(org.w3c.dom.Node node,org.w3c.dom.Node node1) {
    }
}