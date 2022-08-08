package com.itheima.utils;


/**
 * @author v-rr
 */
public class UStack<T> {
    NodeP<T> top = null;
    int size = 0;

    public boolean empty() {
        return size == 0;
    }

    public Integer size() {
        return size;
    }

    class NodeP<T> {
        T val;
        NodeP<T> next;

        public NodeP(T val) {
            this.val = val;
        }

        public NodeP(T val, NodeP<T> next) {
            this.val = val;
            this.next = next;
        }

        public NodeP() {

        }

    }

    public UStack() {

    }

    @Deprecated
    public void clear(T val) {
        this.top = null;
        size = 0;
    }

    public void push(T val) {
        NodeP<T> nodeP = new NodeP<>(val);
        nodeP.next = top;
        top = nodeP;
        size++;
    }

    public T pop() {
        NodeP<T> top = this.top;
        if (top == null) {
            return null;
        }
        this.top = this.top.next;
        size--;
        return top.val;
    }

    public T top() {
        return this.top.val;
    }


}

