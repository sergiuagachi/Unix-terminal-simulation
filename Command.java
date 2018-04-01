
public interface Command {
	/**
	 * metoda principala ce executa comanda
	 * @param c tine detalii despre ierarhia de fisiere si clasele ce executa comenzile
	 */
	public void executeCommand(CommandExecuter c);
}