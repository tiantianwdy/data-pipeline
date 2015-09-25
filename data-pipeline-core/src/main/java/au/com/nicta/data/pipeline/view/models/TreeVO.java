package au.com.nicta.data.pipeline.view.models;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by tiantian on 24/09/15.
 */
public class TreeVO {

    private String name ;
    private List<TreeVO> children = null;

    public TreeVO() {
        this("", new ArrayList<TreeVO>());
    }

    public TreeVO(String name) {
        this(name, new ArrayList<TreeVO>());
    }

    public TreeVO(String name, List<TreeVO> children) {
        this.name = name;
        this.children = children;
    }


    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<TreeVO> getChildren() {
        return children;
    }

    public void setChildren(List<TreeVO> children) {
        this.children = children;
    }

    public TreeVO addChild(TreeVO child) {
        children.add(child);
        return this;
    }
}
