package com.practica.genericas;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;

public class Lector {
    File archivo = null;
    FileReader fr = null;
    BufferedReader br = null;

    public Lector(File archivo, FileReader fr, BufferedReader br) {
        this.archivo = archivo;
        this.fr = fr;
        this.br = br;
    }
    public Lector(){
        
    }

    public File getArchivo() {
        return archivo;
    }

    public void setArchivo(File archivo) {
        this.archivo = archivo;
    }

    public FileReader getFr() {
        return fr;
    }

    public void setFr(FileReader fr) {
        this.fr = fr;
    }

    public BufferedReader getBr() {
        return br;
    }

    public void setBr(BufferedReader br) {
        this.br = br;
    }
}
