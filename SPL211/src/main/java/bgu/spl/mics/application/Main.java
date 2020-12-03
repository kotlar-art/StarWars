package bgu.spl.mics.application;
import bgu.spl.mics.Input;
import bgu.spl.mics.JsonInputReader;
import bgu.spl.mics.application.passiveObjects.Attack;

/** This is the Main class of the application. You should parse the input file,
 * create the different components of the application, and run the system.
 * In the end, you should output a JSON.
 */
public class Main {
	public static void main(String[] args) {
		JsonInputReader reader = new JsonInputReader();
		Input input = reader.getInputFromJson();//need to add the path in the argument. just not sure what it is - need to ask people

		int ewoksSupplied = input.getEwoks();
		long LandoDuration = input.getLando();
		long R2D2Duration = input.getR2D2();
		Attack[] attacks = input.getAttacks();
	}
}
