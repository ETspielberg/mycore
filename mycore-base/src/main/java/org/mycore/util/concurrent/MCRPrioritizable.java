package org.mycore.util.concurrent;

/**
 * Objects can implement this interface if they are capable of being prioritized.
 * 
 * @author Matthias Eichner
 */
public interface MCRPrioritizable extends Comparable<MCRPrioritizable> {

    /**
     * Returns the priority.
     */
    public Integer getPriority();

    @Override
    default int compareTo(MCRPrioritizable o) {
        return o.getPriority().compareTo(o.getPriority());
    }

}
