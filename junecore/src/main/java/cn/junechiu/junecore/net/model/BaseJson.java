package cn.junechiu.junecore.net.model;

import java.io.Serializable;

public class BaseJson<T> implements Serializable {

    public T data;

    public int code;

    public String message;
}
