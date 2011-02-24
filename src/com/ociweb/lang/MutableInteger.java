package com.ociweb.lang;

/**
 * A mutable integer, as opposed to java.lang.Integer which is immutable.
 * @author R. Mark Volkmann, Object Computing, Inc.
 */
public class MutableInteger {

    public int value;

    /**
     * Creates a MutableInteger with a value of zero.
     */
    public MutableInteger() {}

    /**
     * Creates a MutableInteger with a given value.
     * @param value the value
     */
    public MutableInteger(int value) { this.value = value; }
}