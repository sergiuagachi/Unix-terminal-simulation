import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;

public class Test {
	public static void main(String[] args) {

		CommandFactory commandFactory = new CommandFactory();
		CommandExecuter job = new CommandExecuter();
		Command command;

		try {
			
//			FileInputStream fstream = new FileInputStream(args[0]);
			FileInputStream fstream = new FileInputStream("in9.txt");
			BufferedReader br = new BufferedReader(new InputStreamReader(fstream));

			String strLine;

			// se citeste din fisier linie cu linie
			while ((strLine = br.readLine()) != null) {

				// se obtine numele comenzii in comarguments[0]
				// ex: mkdir
				String[] comarguments;
				comarguments = strLine.split(" ");

				// se creeaza o instanta a clasei respective
				command = commandFactory.getCommand(comarguments[0]);

				// se seteaza comanda dorita si se aplica
				job.setCommand(strLine);
				command.executeCommand(job);

			}

			// se inchide bufferul din care se citeste
			br.close();

		} catch (Exception ex) {
			System.out.println("Error: " + ex);
		}

		// se foloseste comanda cd pentru a ajunge in root
		command = commandFactory.getCommand("cd");
		job.setCommand("cd /");
		command.executeCommand(job);

		// dupa ce s-a ajuns in root, se afiseaza intreaga ierarhie de fisiere
		CommandExecuter.Tree com = new CommandExecuter.Tree();
		com.executeCommand(job, 1);
	}
}
