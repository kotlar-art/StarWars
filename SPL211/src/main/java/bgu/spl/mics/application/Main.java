package bgu.spl.mics.application;
import bgu.spl.mics.Input;
import bgu.spl.mics.JsonInputReader;
import bgu.spl.mics.MicroService;
import bgu.spl.mics.application.passiveObjects.Attack;
import bgu.spl.mics.application.passiveObjects.Diary;
import bgu.spl.mics.application.passiveObjects.Ewoks;
import bgu.spl.mics.application.services.*;

import java.io.IOException;

/** This is the Main class of the application. You should parse the input file,
 * create the different components of the application, and run the system.
 * In the end, you should output a JSON.
 */
public class Main {
	public static void main(String[] args) throws IOException {
		JsonInputReader reader = new JsonInputReader();
		Input input = reader.getInputFromJson("./src/input.json.txt");//need to add the path in the argument. just not sure what it is - need to ask people
		int ewoksSupplied = input.getEwoks();
		long LandoDuration = input.getLando();
		long R2D2Duration = input.getR2D2();
		Attack[] attacks = input.getAttacks();
		Ewoks ewoks = Ewoks.getInstance();
		ewoks.setEwoks(ewoksSupplied);

		Thread Leia = new Thread(new LeiaMicroservice(attacks));
		Thread HanSolo = new Thread(new HanSoloMicroservice(ewoks));
		Thread C3PO = new Thread(new C3POMicroservice(ewoks));
		Thread R2D2 = new Thread(new R2D2Microservice(R2D2Duration));
		Thread Lando = new Thread(new LandoMicroservice(LandoDuration));
		System.out.println(System.currentTimeMillis());
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
		System.out.println(diary.toString());
	}
}
