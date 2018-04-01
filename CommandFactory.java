
public class CommandFactory {
	
	/**
	 * metoda ce creeaza o instanta a clasei dorite
	 * @param command numele comenzii careia trebuie sa se creeze o instanta
	 * @return instanta a clasei dorite
	 */
	public Command getCommand(String command) {
		switch (command) {
		case "adduser":
			return new CommandExecuter.Adduser();
		case "deluser":
			return new CommandExecuter.Deluser();
		case"chuser":
			return new CommandExecuter.Chuser();
		case "cd":
			return new CommandExecuter.Cd();
		case "mkdir":
			return new CommandExecuter.Mkdir();
		case "ls":
			return new CommandExecuter.Ls();
		case "chmod":
			return new CommandExecuter.Chmod();
		case "touch":
			return new CommandExecuter.Touch();
		case "rm":
			return new CommandExecuter.Rm();
		case "rmdir":
			return new CommandExecuter.Rmdir();
		case "writetofile":
			return new CommandExecuter.WriteToFile();
		case "cat":
			return new CommandExecuter.Cat();
		}
		return null;
	}
}
