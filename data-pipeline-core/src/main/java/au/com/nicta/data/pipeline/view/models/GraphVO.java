package au.com.nicta.data.pipeline.view.models;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by tiantian on 24/09/15.
 */
public class GraphVO {

    private List nodes = new ArrayList();
    private List links = new ArrayList();


    public List getNodes() {
        return nodes;
    }

    public void setNodes(List nodes) {
        this.nodes = nodes;
    }

    public List getLinks() {
        return links;
    }

    public void setLinks(List links) {
        this.links = links;
    }

    public  GraphVO addNodes(Object node){
        nodes.add(node);
        return this;
    }

    public GraphVO addLink(Object link){
        links.add(link);
        return this;
    }
}
