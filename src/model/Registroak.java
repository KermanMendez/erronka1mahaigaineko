package model;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class Registroak {

    private final String fitxategia = "eskaerak.dat";
    private final byte kodea = 0x5A;

    private byte[] xorBytes(byte[] data) {
        byte[] result = new byte[data.length];
        for (int i = 0; i < data.length; i++) {
            result[i] = (byte) (data[i] ^ kodea);
        }
        return result;
    }

    public void eskaeraGorde(PojoRegistratu s) throws IOException {
        List<PojoRegistratu> lista = eskaerakKargatu();
        lista.add(s);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(baos);
        oos.writeObject(lista);
        oos.close();
        byte[] bytesSerializados = xorBytes(baos.toByteArray());
        try (FileOutputStream fos = new FileOutputStream(fitxategia)) {
            fos.write(bytesSerializados);
        }
    }

    @SuppressWarnings("unchecked")
    public List<PojoRegistratu> eskaerakKargatu() {
        File f = new File(fitxategia);
        if (!f.exists() || f.length() == 0)
            return new ArrayList<>();
        try {
            byte[] fileBytes = Files.readAllBytes(Paths.get(fitxategia));
            byte[] descifratuak = xorBytes(fileBytes);
            ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(descifratuak));
            return (List<PojoRegistratu>) ois.readObject();
        } catch (Exception e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    public void ezabatuEskaerak(List<PojoRegistratu> lista) {
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ObjectOutputStream oos = new ObjectOutputStream(baos);
            oos.writeObject(lista);
            oos.close();
            byte[] byteSerializatuak = xorBytes(baos.toByteArray());
            try (FileOutputStream fos = new FileOutputStream(fitxategia)) {
                fos.write(byteSerializatuak);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
