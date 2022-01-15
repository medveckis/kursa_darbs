package org.maks.model;

import java.util.Objects;

public class DecisionNode {
    private Integer value;
    private boolean flag;

    public DecisionNode() {
    }

    public DecisionNode(Integer value, boolean flag) {
        this.value = value;
        this.flag = flag;
    }

    public Integer getValue() {
        return value;
    }

    public void setValue(Integer value) {
        this.value = value;
    }

    public boolean getFlag() {
        return flag;
    }

    public void setFlag(boolean flag) {
        this.flag = flag;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        DecisionNode that = (DecisionNode) o;

        if (flag != that.flag) return false;
        return Objects.equals(value, that.value);
    }

    @Override
    public int hashCode() {
        int result = value != null ? value.hashCode() : 0;
        result = 31 * result + (flag ? 1 : 0);
        return result;
    }
}
