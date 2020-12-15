package bgu.spl.mics.application;
import bgu.spl.mics.Input;
import bgu.spl.mics.JsonInputReader;
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
	public static void main(String[] args) throws IOException {
		JsonInputReader reader = new JsonInputReader();
		Input input = reader.getInputFromJson("./src/input.json.txt");
		int ewoksSupplied = input.getEwoks();
		long LandoDuration = input.getLando();
		long R2D2Duration = input.getR2D2();
		Attack[] attacks = input.getAttacks();
		Ewoks ewoks = Ewoks.getInstance();
		ewoks.setEwoks(ewoksSupplied);
		System.out.println(System.currentTimeMillis());

		Thread Leia = new Thread(new LeiaMicroservice(attacks));
		Thread HanSolo = new Thread(new HanSoloMicroservice());
		Thread C3PO = new Thread(new C3POMicroservice());
		Thread R2D2 = new Thread(new R2D2Microservice(R2D2Duration));
		Thread Lando = new Thread(new LandoMicroservice(LandoDuration));
		Leia.start();
		HanSolo.start();
		C3PO.start();
		R2D2.start();
		Lando.start();

		try {
			Leia.join();
			HanSolo.join();
			C3PO.join();
			R2D2.join();
			Lando.join();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		Diary diary = Diary.getInstance();
		System.out.println(diary.R2D2Terminate);
		Gson gson = new GsonBuilder().setPrettyPrinting().create();
		FileWriter writer = new FileWriter("./src/output.json.txt");
		gson.toJson(diary,writer);
		writer.flush();
		writer.close();
	}
}
