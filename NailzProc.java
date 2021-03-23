/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.app.nailz;

/**
 *
 * @author Kathilee Ledgister
 *         Shanee Barnes 
 *         Jordan Wilson 
 *         Raman Lewis
 */
public class NailzProc {

    private final long id;
    private final String content;

    public NailzProc(long id, String content) {
        this.id = id;
        this.content = content;
    }

    public long getId() {
        return id;
    }

    public String getContent() {
        return content;
    }
}
