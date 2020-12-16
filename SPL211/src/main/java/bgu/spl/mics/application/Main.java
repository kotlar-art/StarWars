package bgu.spl.mics.application;
import bgu.spl.mics.Input;
import bgu.spl.mics.JsonInputReader;
import bgu.spl.mics.MessageBusImpl;
import bgu.spl.mics.MicroService;
import bgu.spl.mics.application.passiveObjects.Attack;
import bgu.spl.mics.application.passiveObjects.Diary;
import bgu.spl.mics.application.passiveObjects.Ewoks;
import bgu.spl.mics.application.services.*;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.FileWriter;
import java.io.IOException;

/** This is the Main class of the application. You should parse the input file,
 * create the different components of the application, and run the system.
 * In the end, you should output a JSON.
 */
public class Main {
    public static void main(String[] args) {
        Input input = new Input();
        try {
            input = JsonInputReader.getInputFromJson("./input.json");
        } catch (IOException e) {
        }

        int ewoksSupplied = input.getEwoks();
        long LandoDuration = input.getLando();
        long R2D2Duration = input.getR2D2();
        Attack[] attacks = input.getAttacks();
        Ewoks ewoks = Ewoks.getInstance();
        ewoks.setEwoks(ewoksSupplied);

        Thread Leia = new Thread(new LeiaMicroservice(attacks));
        Thread Han = new Thread(new HanSoloMicroservice());
        Thread C3PO = new Thread(new C3POMicroservice());
        Thread R2D2 = new Thread(new R2D2Microservice(R2D2Duration));
        Thread Lando = new Thread(new LandoMicroservice(LandoDuration));
        Leia.start();
        Han.start();
        C3PO.start();
        R2D2.start();
        Lando.start();
        try {
            Leia.join();
            Han.join();
            C3PO.join();
            R2D2.join();
            Lando.join();
        } catch (InterruptedException e) {
        }
        try {
            Diary diary = Diary.getInstance();
            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            FileWriter writer = new FileWriter("Output.json");
            gson.toJson(diary, writer);
            writer.flush();
            writer.close();
        } catch (IOException e) {
        }
        MessageBusImpl.reset();

    }
}
