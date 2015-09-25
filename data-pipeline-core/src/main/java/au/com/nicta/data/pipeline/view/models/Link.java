package au.com.nicta.data.pipeline.view.models;

/**
 * Created by tiantian on 24/09/15.
 */
public class Link {

    private Integer source;
    private Integer target;
    private Integer value;

    public Link() {

    }

    public Link(Integer source, Integer target) {
        this.source = source;
        this.target = target;
        this.value = (int) Math.round(Math.random() * 3);
    }

    public Link(Integer source, Integer target, Integer value) {
        this.source = source;
        this.target = target;
        this.value = value;
    }

    public Integer getSource() {
        return source;
    }

    public void setSource(Integer source) {
        this.source = source;
    }

    public Integer getTarget() {
        return target;
    }

    public void setTarget(Integer target) {
        this.target = target;
    }

    public Integer getValue() {
        return value;
    }

    public void setValue(Integer value) {
        this.value = value;
    }
}
