package com.jc.jnotes;

/**
 * 
 * Main class which doesn't extend javafx specific class.
 * This is needed for running the uber jar created by maven shade plugin.
 * 
 * @author Joy C
 *
 */
public class JNotesMain {

    public static void main(String[] args) {
        JNotesApplication.main(args);
    }

}
