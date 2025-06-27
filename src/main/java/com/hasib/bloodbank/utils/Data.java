package com.hasib.bloodbank.utils;

import java.io.Serializable;

public class Data implements Serializable, Cloneable {

    public String message;

    public Object clone() throws CloneNotSupportedException {
        return super.clone();
    }
}
