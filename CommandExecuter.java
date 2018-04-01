
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class CommandExecuter {

	// retine comanda de executat
	public String command;

	// se tin minte directorul si utilizatorul curent
	private Directory currentDirectory;
	private User currentUser;

	// se tine minte root-ul si utilizatorii
	private Directory root;
	private List<User> users = new ArrayList<>();

	// campul arata daca comanda ce ruleaza a fi rulata are este primita de bash
	// sau din interiorul altei comenzi
	private boolean cd = true;

	/**
	 * setter pentru comanda
	 * @param command stringul ce reprezinta comanda ce va fi executata
	 */
	public void setCommand(String command) {
		this.command = command;
	}
	
	/**
	 * aduce directorul curent la versiunea originala in cazul in care acesta a fost modificat 
	 * @param bkp backup al directorului original
	 */
	public void restoreDir(Directory bkp) {
		this.currentDirectory = bkp;
	}

	/**
	 * aduce comanda la versiunea sa originala in cazul in care a fost modificata
	 * @param originalCommand backup al comenzii original
	 */
	public void restoreCom(String originalCommand) {
		this.command = originalCommand;
	}

	/**
	 * aduce comanda si directorul curent la versiunea lor originala
	 * @param bkp backup al directorului original
	 * @param originalCommand backup al comenzii original
	 */
	public void restore(Directory bkp, String originalCommand) {
		restoreDir(bkp);
		restoreCom(originalCommand);
	}

	/**
	 * verifica daca userul curent are drept de citire asupra entitatii primite ca parametru
	 * @param e entitatea careia ii interogam drepturile pentru userul curent
	 * @return true daca exista drepturi, false daca nu
	 */
	public boolean hasRightToRead(Entity e) {
		if ((this.currentUser.name.equals(e.owner) && e.ownerPermission.charAt(0) == 'r')
				|| (!this.currentUser.name.equals(e.owner) && e.otherPermission.charAt(0) == 'r'))
			return true;
		return false;
	}

	/**
	 * verifica daca userul curent are drept de scriere asupra entitatii primite ca parametru
	 * @param e entitatea careia ii interogam drepturile pentru userul curent
	 * @return true daca exista drepturi, false daca nu
	 */
	public boolean hasRightToWrite(Entity e) {
		if ((this.currentUser.name.equals(e.owner) && e.ownerPermission.charAt(1) == 'w')
				|| (!this.currentUser.name.equals(e.owner) && e.otherPermission.charAt(1) == 'w'))
			return true;
		return false;
	}

	/**
	 * verifica daca userul curent are drept de executie asupra entitatii primite ca parametru
	 * @param e entitatea careia ii interogam drepturile pentru userul curent
	 * @return true daca exista drepturi, false daca nu
	 */
	public boolean hasRightToExecute(Entity e) {
		if ((this.currentUser.name.equals(e.owner) && e.ownerPermission.charAt(2) == 'x')
				|| (!this.currentUser.name.equals(e.owner) && e.otherPermission.charAt(2) == 'x'))
			return true;
		return false;
	}

	/**
	 * metoda printeaza eroare corespunzatoare lui errNo
	 * @param c tine structura arborescenta de entitati si numele comenzii curente 
	 * @param errNo numarul aferent erorii
	 */
	private static void printError(CommandExecuter c, int errNo) {
		switch (errNo) {
		case -1:
			System.out.format("-1: %s: Is a directory\n", c.command);
			break;
		case -2:
			System.out.format("-2: %s: No such directory\n", c.command);
			break;
		case -3:
			System.out.format("-3: %s: Not a directory\n", c.command);
			break;
		case -4:
			System.out.format("-4: %s: No rights to read\n", c.command);
			break;
		case -5:
			System.out.format("-5: %s: No rights to write\n", c.command);
			break;
		case -6:
			System.out.format("-6: %s: No rights to execute\n", c.command);
			break;
		case -7:
			System.out.format("-7: %s: File already exists\n", c.command);
			break;
		case -8:
			System.out.format("-8: %s: User does not exist\n", c.command);
			break;
		case -9:
			System.out.format("-9: %s: User already exists\n", c.command);
			break;
		case -10:
			System.out.format("-10: %s: No rights to change user status\n", c.command);
			break;
		case -11:
			System.out.format("-11: %s: No such file\n", c.command);
			break;
		case -12:
			System.out.format("-12: %s: No such file or directory\n", c.command);
			break;
		case -13:
			System.out.format("-13: %s: Cannot delete parent or current directory\n", c.command);
			break;
		case -14:
			System.out.format("-14: %s: Non empty directory\n", c.command);
			break;

		}
	}

	public static class Adduser implements Command {
		public void executeCommand(CommandExecuter c) {
			if (c.command.startsWith("adduser")) {
				// doar root poate adauga useri
				if (c.currentUser.name.equals("root")) {

					Directory bkp = c.currentDirectory;
					String originalCommand = c.command;

					// creez un nou user cu numele dorit si un director ca home al sau
					User user = new User(c.command.substring(8));
					Directory dir = new Directory(c.command.substring(8));

					// se afiseaza eroare daca userul exista
					if (c.users.indexOf(user) != -1) {
						c.restore(bkp, originalCommand);
						printError(c, -9);
						return;
					}

					// adaugam efectiv userul in lista de useri
					c.users.add(user);
					
					// se seteaza directorul pe / si se creeaza un nou folder in acelasi loc
					c.currentDirectory = c.root;
					c.setCommand("mkdir" + " " + c.command.substring(8));
					new CommandExecuter.Mkdir().executeCommand(c);
					
					// atribuim noului user directorul home si il facem pe acesta owner asupra lui
					user.homeDir = (Directory) c.root.sub.get(c.root.sub.indexOf(dir));
					user.homeDir.owner = user.name;
					user.homeDir.parent = c.root;

					c.restoreDir(bkp);
				} else
					printError(c, -10);
			}
		}
	}

	public static class Deluser implements Command {

		// metoda ce schimba ownerul tuturor fisierelor sterse ale unui user cu
		// primul user adaugat
		public void changeOwner(CommandExecuter c, String newOwner, String prevOwner) {
			for (Entity element : c.currentDirectory.sub) {
				if (element.owner.equals(prevOwner))
					element.owner = newOwner;
				if (element instanceof Directory) {
					c.currentDirectory = (Directory) element;
					changeOwner(c, newOwner, prevOwner);
				}
			}
		} 

		public void executeCommand(CommandExecuter c) {
			if (c.command.startsWith("deluser")) {
				
				Directory bkp = c.currentDirectory;
				String originalCommand = c.command;

				// doar root are dreptul sa stearga un user
				if (c.currentUser.name.equals("root")) {
					
					// se gaseste userul ce trebuie sters si directorul sau home
					User user = new User(c.command.substring(8));
					Directory dir = (Directory) c.root.sub
							.get(c.root.sub.indexOf(new Directory(user.name)));

					// noul owner va fi primul user adaugat
					String newOwner = c.users.get(1).name;
					dir.owner = newOwner;

					try {
						c.users.remove(c.users.indexOf(user));
					} catch (Exception e) {
						
						// se verifica daca userul ce trebuie sters exista
						if (e.getMessage().equals("-1")) {
							c.restore(bkp, originalCommand);
							printError(c, -8);
							return;
						}
					}

					// daca userul a fost sters, trebuie sa schimbam permisiunile fisierelor sale
					c.command = "cd /" + user.name;
					new CommandExecuter.Cd().executeCommand(c);
					changeOwner(c, newOwner, user.name);

				} else
					printError(c, -10);

				c.restore(bkp, originalCommand);
			}
		}
	}

	public static class Chuser implements Command {
		public void executeCommand(CommandExecuter c) {
			if (c.command.startsWith("chuser")) {
				
				// caz special in caz ca se doreste schimbarea la root
				if (c.command.substring(7).equals("root")) {
					c.currentUser = c.users.get(c.users.indexOf(new User("root")));
					c.currentDirectory = c.root;
				} else {
					try {
						// se incearca schimbarea userului
						c.currentUser = c.users
								.get(c.users.indexOf(new User(c.command.substring(7))));
					} catch (Exception e) {
						// se verifica daca userul ce de schimbat exista
						if (e.getMessage().equals("-1")) {
							printError(c, -8);
							return;
						}
					}
					// daca s-a reusit schimbarea userului, directorul curent
					// trebuie sa fie home-ul userului proaspat schimbat
					c.currentDirectory = (Directory) c.root.sub
							.get(c.root.sub.indexOf(new Directory(c.command.substring(7))));
				}
			}
		}
	}

	public static class Cd implements Command {
		public void executeCommand(CommandExecuter c) {
			if (c.command.startsWith("cd")) {
				
				Directory bkp = c.currentDirectory;
				
				// se obtine calea pana la directorul de schimbat
				String[] path = c.command.substring(3).split("/");
				int i = 0;
				Directory dir;

				// se verifica daca calea este absoluta sau relativa
				if (c.command.substring(3).startsWith("/"))
					dir = c.root;
				else
					dir = c.currentDirectory;

				// se parcurge pe rand calea spre directorul dorit, director cu director
				while (i <= (path.length - 1)) {

					// daca in path se gaseste .. => se merge un director inapoi
					if (path[i].equals(".."))
						
						// cazurile . si .. nu se aplica pentru /
						if (!dir.name.equals("/")) {
							dir = dir.parent;
							i++;
							continue;
						} else {
							i++;
							continue;
						}
					// daca in path se gaseste . => se ramane in directorul curent
					if (path[i].equals(".")) {
						i++;
						continue;
					}

					if (!path[i].equals(""))
						// atata timp cat nu am ajuns deja in directorul dorit
						if (!(dir.name.equals(path[path.length - 1]))) {

							// se incearca trecerea la urmatorul director din path
							try {
								dir = (Directory) dir.sub.get(dir.sub.indexOf(new Entity(path[i])));

							} catch (Exception e) {

								// daca directorul dorit nu exista, se afiseaza eroare
								if (e.getMessage().equals("-1"))
									if (c.cd == true) {
										printError(c, -2);
										c.restoreDir(bkp);
										return;
									} else {
										c.command = "outOfBounds";
										throw e;
									}

								// daca in cale s-a gasit un fisier in loc de un director
								// afisam eroare
								if (e.getMessage().equals("File cannot be cast to Directory"))
									if (c.cd == true) {
										printError(c, -3);
										c.restoreDir(bkp);
										return;
									} else if (dir.sub.get(
											dir.sub.indexOf(new Entity(path[i]))) instanceof File) {
										c.command = "file";
										throw e;
									}
							}
							// se verifica permisiunea de execute pe directorul curent
							// daca nu exista permisiune, se afiseaza eroare
							if (!c.currentUser.name.equals("root"))
								if (!c.hasRightToExecute(dir))
									if (c.cd == true) {
										printError(c, -6);
										c.restoreDir(bkp);
										return;
									} else {
										c.restoreDir(bkp);
										c.command = "permission";
										return;
									}
						}
						// daca calea primita nu duce la directorul dorit, se seteaza un
						// "flag" ce specifica acest fapt
						else {
							c.restoreDir(bkp);
							c.command = "outOfBounds";
							return;
						}

					i++;
				}
				c.currentDirectory = dir;
			}
		}
	}

	public static class Mkdir implements Command {
		public void executeCommand(CommandExecuter c) {
			if (c.command.startsWith("mkdir")) {
				
				Directory bkp = c.currentDirectory;
				String originalCommand = c.command;
				
				// se pregateste directorul de creeat
				Directory dir = new Directory(c.command.substring(6));

				// caz special de luat in calcul in cazul lui "mkdir /"
				if (dir.name.equals("/")) {
					
					// daca exista deja un root se afiseaza eroare
					if (!(c.root == null)) {
						printError(c, -1);
						return;
					}
					
					// se fac modificarile specifice root-ului
					// ex: se seteaza permisiunile rwxr-x, se creeaza si atribuie userul "root"
					dir.setPermission("75");
					c.currentDirectory = dir;
					c.root = dir;
					c.currentUser = new User("root");
					dir.owner = c.currentUser.name;
					c.users.add(c.currentUser);
					c.currentUser.homeDir = dir;

				} else {

					// se pregateste path-ul de schimbat pentru a ajunge in folderul
					// in care creem folderul
					String[] path = c.command.substring(6).split("/");
					
					dir.name = path[path.length - 1];

					if (path.length > 1) {
						String pathTo = c.command.substring(6);

						if (pathTo.endsWith("/"))
							pathTo = pathTo.substring(0, pathTo.length() - 1);

						c.command = "cd" + " " + pathTo.substring(0,
								pathTo.length() - path[path.length - 1].length());
						try {
							c.cd = false;
							new CommandExecuter.Cd().executeCommand(c);
							c.cd = true;
							
							// daca nu exista permisiuni anumite permisiuni se arunca o exceptie 
							if (c.command.equals("permission"))
								throw new Exception();
								
						} catch (Exception e) {
							
							// daca nu exista permisiuni de scriere se afiseaza se afiseaza eroare
							if (c.command.equals("permission")) {
								c.restore(bkp, originalCommand);
								printError(c, -6);
								return;
							}

							// daca exista un fisier in calea unde trebuie creeat directorul
							// se afiseaza eroare
							if (c.command.equals("file")) {
								c.restore(bkp, originalCommand);
								printError(c, -3);
								return;
							}

							c.restore(bkp, originalCommand);
							
							// daca pe calea unde trebuie creeat, 
							// la un moment dat un director nu exista
							printError(c, -2);
							return;
						}
					}

					if (!c.command.equals("")) {

						c.restoreCom(originalCommand);
						dir.owner = c.currentUser.name;
						dir.parent = c.currentDirectory;

						// daca directorul exista deja se afiseaza eroare
						if (c.currentDirectory.sub.indexOf(dir) != -1) {
							printError(c, -1);
							c.restoreDir(bkp);
							return;
						}

						// daca exista deja un fisier cu numele directorului de creeat 
						// se afiseaza eroare
						if (c.currentDirectory.sub.indexOf(new Entity(dir.name)) != -1) {
							printError(c, -3);
							c.restoreDir(bkp);
							return;
						}
						
						// daca nu exista permisiuni de scriere in folderul unde trebuie
						// creeat directorul, se afiseaza eroare
						if (!c.currentUser.name.equals("root"))
							if (!c.hasRightToWrite(c.currentDirectory)) {
								printError(c, -5);
								c.restoreDir(bkp);
								return;
							}
						
						// daca nu apare nicio problema, se adauga directorul
						c.currentDirectory.sub.add(dir);
						c.restoreDir(bkp);
					}
				}
			}
		}
	}

	public static class Ls implements Command {
		public void executeCommand(CommandExecuter c) {
			if (c.command.startsWith("ls")) {

				Directory bkp = c.currentDirectory;
				String originalCommand = c.command;

				// daca nu sunt in directorul ce trebuie printat, incerc sa ajung la el
				if (!c.command.equals("ls .")) {
					String pathTo = c.command.substring(3);
					c.command = "cd" + " " + pathTo;

					try {
						c.cd = false;
						new CommandExecuter.Cd().executeCommand(c);
						c.cd = true;

					} catch (Exception e) {
						if (c.command.equals("outOfBounds")) {
							c.cd = true;
							c.restoreCom(originalCommand);
							printError(c, -12);
							return;
						}
					}
				}

				// daca nu exista permisiuni de executie pe unul din directoarele catre 
				// calea catre directorul de afisat, se afiseaza eroare
				if (c.command.equals("permission")) {
					c.restore(bkp, originalCommand);
					printError(c, -6);
					return;
				}

				// daca nu exista permisiuni pe unul din directoarele din path-ul spre
				// directorul ce trebuie afisat, se afiseaza eroare
				if (!c.currentUser.name.equals("root"))
					if (!c.hasRightToRead(c.currentDirectory)) {
						c.restore(bkp, originalCommand);
						printError(c, -4);
						return;
					}
				
				// se parcurge fiecare entitate al directorului dorit si se afiseaza
				// tipul + permisiuni + owner-ul sau
				for (Entity element : c.currentDirectory.sub)
					if (element instanceof File)
						System.out.println(element.name + " " + "f" + element.ownerPermission
								+ element.otherPermission + " " + element.owner);
					else
						System.out.println(element.name + " " + "d" + element.ownerPermission
								+ element.otherPermission + " " + element.owner);

				c.restoreDir(bkp);
			}
		}
	}

	public static class Chmod implements Command {
		public void executeCommand(CommandExecuter c) {
			if (c.command.startsWith("chmod")) {
				
				Directory bkp = c.currentDirectory;
				String originalCommand = c.command;

				// se pregateste path-ul catre care se ajunge la entitatea
				// careia trebuie sa ii schimbam permisiunile
				String[] path = c.command.substring(9).split("/");
				String pathToChange = c.command.substring(9);

				// din path-ul primit se taie ultimul director si se foloseste
				// comanda cd pentru a ajunge in foledrul respectiv
				if (pathToChange.endsWith("/"))
					pathToChange = pathToChange.substring(0, pathToChange.length() - 1);

				pathToChange = pathToChange.substring(0,
						pathToChange.length() - path[path.length - 1].length());

				c.command = "cd " + pathToChange;
				
				try {
					c.cd = false;
					new CommandExecuter.Cd().executeCommand(c);
					c.cd = true;
				} catch (Exception e) {
					
					// daca in path nu exista un director, se afiseaza eroare
					if (c.command.equals("outOfBounds")) {
						c.restore(bkp, originalCommand);
						printError(c, -2);
						return;
					}
				}

				c.restoreCom(originalCommand);

				// se gaseste entitatea careia trebuie sa ii schimbam permisiunile
				String entName = path[path.length - 1];
				Entity ent = new Entity(entName);

				try {
					ent = c.currentDirectory.sub.get(c.currentDirectory.sub.indexOf(ent));

					if (!c.currentUser.name.equals("root"))
						if (c.hasRightToWrite(ent))
							ent.setPermission(c.command.substring(6, 8));
					
						// daca nu exista permisiuni de write in pe entitatea
						// careia vrem sa ii schimbam permisiunea, se afiseaza eroare
						else {
							printError(c, -5);
							c.restoreDir(bkp);
							return;
						}

					ent.setPermission(c.command.substring(6, 8));
					c.restoreDir(bkp);

				} catch (Exception e) {
					
					// daca nu exista entitatea se afiseaza eroare
					if (e.getMessage().equals("-1")) {
						printError(c, -12);
						c.restoreDir(bkp);
						return;
					}
				}

			}
		}
	}

	public static class Touch implements Command {
		public void executeCommand(CommandExecuter c) {
			if (c.command.startsWith("touch")) {

				Directory bkp = c.currentDirectory;
				String originalCommand = c.command;

				// se pregateste path-ul catre directorul in care creem fisierul
				File file = new File(c.command.substring(6));
				String[] path = c.command.substring(6).split("/");

				file.name = path[path.length - 1];

				// daca directorul curent nu este cel dorit, incercam sa ajungem la el
				if (path.length > 1) {
					
					String pathTo = c.command.substring(6);
					c.command = "cd" + " " + pathTo.substring(0,
							pathTo.length() - path[path.length - 1].length() - 1);
					
					try {
						new CommandExecuter.Cd().executeCommand(c);
						
					} catch (Exception e) {
						
						// daca nu exista drept de execute pe unul din directoarele
						// din path-ul spre directorul dorit, se afiseaza eroare
						if (c.command.equals("permission")) {
							c.restore(bkp, originalCommand);
							printError(c, -6);
							return;
						}
					}
				}

				// se verifica daca exista drepturi de scriere in folderul in care creem fisierul
				if (!c.currentUser.name.equals("root")) {
					if (!c.hasRightToWrite(c.currentDirectory)) {
						printError(c, -5);
						c.restoreDir(bkp);
						return;
					}
				}
				
				file.owner = c.currentUser.name;
				file.parent = c.currentDirectory;

				// daca fisierul exista deja se afiseaza eroare
				if (c.currentDirectory.sub.indexOf(file) != -1) {
					printError(c, -7);
					return;
				}

				// daca exista deja un director cu acelasi nume ca fierul ce dorim sa il adaugam
				// se afiseaza eroare
				if (c.currentDirectory.sub.indexOf(new Entity(file.name)) != -1) {
					printError(c, -1);
					return;
				}

				// daca nu exista nicio problema, se adauga fisierul in lista directorului curent
				c.currentDirectory.sub.add(file);
				c.restoreDir(bkp);
			}
		}
	}

	public static class Rm implements Command {

		/**
		 * metoda verifica daca directorului "d" este parintele entitatii "e"
		 * @param c CommandExecuter-ul ce retine ierarhia de fisiere
		 * @param e entitatea pentru care se verifica
		 * @param d folderul ce se verifica daca este parintele lui "e"
		 * @return true daca este "d" este parintele/stramosul lui "e" 
		 */
		public boolean checkIfParent(CommandExecuter c, Entity e, Directory d) {
			if (!e.name.equals("/") && !c.currentDirectory.name.equals("/")) {
				e = c.currentDirectory.parent;
				while (true) {

					if (e.name.equals(d.name))
						return true;

					if (e.name.equals("/"))
						break;

					e = e.parent;
				}
			}
			return false;

		}

		public void executeCommand(CommandExecuter c) {

			// nu se poate sterge / deoarece este ori parintele oricui
			// ori folderul curent
			if (c.command.equals("rm -r /")) {
				printError(c, -13);
				return;
			}

			// rm simplu
			if (c.command.startsWith("rm") && !c.command.substring(3, 4).equals("-")) {
				
				Directory bkp = c.currentDirectory;
				String originalCommand = c.command;

				// se pregateste path-ul catre directoul din care vom sterge
				String pathTo = c.command.substring(3);
				File toDel = new File(pathTo.split("/")[pathTo.split("/").length - 1]);
				pathTo = c.command.substring(3).substring(0, pathTo.length() - toDel.name.length());

				// se incearca ajungerea catre directorul dorit
				c.command = "cd" + " " + pathTo;
				try {
					c.cd = false;
					new CommandExecuter.Cd().executeCommand(c);
					c.cd = true;
					
				} catch (Exception e) {

					c.cd = true;
					
					// daca pe calea spre directorul dorit am intalnit un fisier se afiseaza eroare
					if (c.command.equals("file")) {
						c.restore(bkp, originalCommand);
						printError(c, -3);
						return;
					}

					// daca pe calea spre directorul dorit am intalnit un director ce nu exista
					// se afiseaza eroare
					c.restore(bkp, originalCommand);
					if (e.getMessage().equals("-1")) {
						printError(c, -2);
						return;
					}
				}

				c.restoreCom(originalCommand);

				// se verifica daca exista drepturi de scriere in directorul curent
				if (!c.hasRightToWrite(c.currentDirectory)) {
					printError(c, -5);
					c.restoreDir(bkp);
					return;
				}
				
				// se incearca stergerea fisierului
				try {
					c.currentDirectory.sub.remove(c.currentDirectory.sub.indexOf(toDel));

				} catch (Exception e) {

					//  se verifica daca ceea ce dorim sa stergem este un fisier,
					// si nu un director
					Directory maybeDir = new Directory(toDel.name);
					
					// in caz ca este director, afisam eroare
					if (c.currentDirectory.sub.indexOf(maybeDir) != -1) {
						printError(c, -1);
						c.restoreDir(bkp);
						return;
					}
					
					// daca fisierul nu exista, se afiseaza eroare
					if (e.getMessage().equals("-1")) {
						printError(c, -11);
						c.restoreDir(bkp);
						return;
					}
				}

				c.restoreDir(bkp);
			}

			// rm -r
			if (c.command.startsWith("rm -r")) {
				
				Directory bkp = c.currentDirectory;
				String originalCommand = c.command;

				// se pregateste path-ul catre directoul din care vom sterge
				String pathTo = c.command.substring(6);
				Entity toDel = new Entity(pathTo.split("/")[pathTo.split("/").length - 1]);
				pathTo = c.command.substring(6).substring(0, pathTo.length() - toDel.name.length());

				// se incearca ajungerea catre directorul dorit
				c.command = "cd" + " " + pathTo;
				try {
					c.cd = false;
					new CommandExecuter.Cd().executeCommand(c);
					c.cd = true;

				} catch (Exception e) {

					c.cd = true;

					// daca pe calea spre directorul dorit am intalnit un fisier se afiseaza eroare
					if (c.command.equals("file")) {
						c.restore(bkp, originalCommand);
						printError(c, -3);
						return;
					}
					
					// daca pe calea spre directorul dorit am intalnit un director ce nu exista
					// se afiseaza eroare
					c.restore(bkp, originalCommand);
					if (e.getMessage().equals("-1")) {
						printError(c, -2);
						return;
					}
				}

				c.restoreCom(originalCommand);

				// se verifica daca entitatea ce se doreste a fi stearsa este un parinte/stramos
				// al directorului curent
				if (checkIfParent(c, bkp, c.currentDirectory) || c.currentDirectory.equals(bkp))
					if (toDel instanceof Directory 
							|| toDel.name.equals(".")
							|| toDel.name.equals("..")) {
						
						// daca este parinte sau directorul curent, se afiseaza eroare
						printError(c, -13);
						c.restoreDir(bkp);
						return;
					}

				// daca nu exista drepturi de write in folderul curent se afiseaza eroare
				if (!c.hasRightToWrite(c.currentDirectory)) {
					printError(c, -5);
					c.restoreDir(bkp);
					return;
				}

				// se incearca gasirea directorul radacina a ierarhiei de sters
				try {
					toDel = c.currentDirectory.sub.get(c.currentDirectory.sub.indexOf(toDel));
					
					// se verifica daca entitatea gasita este un fisier
					if (toDel instanceof File)
						
						// se verifica drepturile de scriere asupra entiatii gasite
						if (c.hasRightToWrite(toDel))
							c.currentDirectory.sub.remove(c.currentDirectory.sub.indexOf(toDel));
					
						// daca nu exista drepturi, se afiseaza eroare
						else {
							printError(c, -5);
							c.restoreDir(bkp);
							return;
						}
					
					// se verifica daca entiatea gasita este un director
					else if (toDel instanceof Directory)
						
						// daca exista drepturi de scriere, se sterge referinta catre parinte
						// a entiatilor subordonate directorului de sters, iar apoi se
						// sterge directorul
						if (c.hasRightToWrite(c.currentDirectory)) {
							for (Entity e : ((Directory) toDel).sub)
								e.parent = null;
							c.currentDirectory.sub.remove(c.currentDirectory.sub.indexOf(toDel));
							
						// daca nu exista drepturi, se afiseaza eroare
						} else {
							printError(c, -5);
							c.restoreDir(bkp);
							return;
						}

				} catch (Exception e) {
					
					// daca entitatea ce se doreste a fi stearsa nu exista, se afiseaza eroare
					if (e.getMessage().equals("-1")) {
						printError(c, -11);
						c.restoreDir(bkp);
						return;
					}
				}

				c.restoreDir(bkp);
			}
		}
	}

	public static class Rmdir implements Command {

		/**
		 * metoda verifica daca directorului "d" este parintele entitatii "e"
		 * @param c CommandExecuter-ul ce retine ierarhia de fisiere
		 * @param e entitatea pentru care se verifica
		 * @param d folderul ce se verifica daca este parintele lui "e"
		 * @return true daca este "d" este parintele/stramosul lui "e" 
		 */
		public boolean checkIfParent(CommandExecuter c, Entity e, Directory d) {
			if (!e.name.equals("/")) {
				e = c.currentDirectory.parent;
				while (true) {

					if (e.name.equals(d.name))
						return true;

					if (e.name.equals("/"))
						break;

					e = e.parent;
				}
			}
			return false;
		}

		public void executeCommand(CommandExecuter c) {
			if (c.command.startsWith("rmdir")) {

				Directory bkp = c.currentDirectory;
				String originalCommand = c.command;

				// se pregateste path-ul catre directorul care vrem sa-l stergem
				String pathTo = c.command.substring(6);
				Directory toDel = new Directory(pathTo.split("/")[pathTo.split("/").length - 1]);

				// se incearca ajungerea catre directorul dorit
				c.command = "cd" + " " + pathTo;
				try {
					c.cd = false;
					new CommandExecuter.Cd().executeCommand(c);
					c.cd = true;
				} catch (Exception e) {

					// daca in calea catre directorul dorit exista un fisier, se afiseaza eroare
					if (c.command.equals("file")) {
						c.cd = true;
						c.restore(bkp, originalCommand);
						printError(c, -3);
						return;
					}

					// daca un director din calea dorita nu exista, se afiseaza eroare
					c.restore(bkp, originalCommand);
					if (e.getMessage().equals("-1")) {
						printError(c, -2);
						return;
					}
				}

				// daca folosing comanda "cd" am ajuns la calea dorita dar nu am gasit directorul dorit,
				// se afiseaza eroare
				if (c.command.equals("outOfBounds")) {
					c.restore(bkp, originalCommand);
					printError(c, -2);
					return;
				}

				c.restoreCom(originalCommand);

				// se verifica daca directorul ce se doreste a fi stears este un parinte/stramos
				// al directorului curent
				if (checkIfParent(c, bkp, c.currentDirectory) || c.currentDirectory.equals(bkp)) {
					printError(c, -13);
					c.restoreDir(bkp);
					return;
				}

				// se verifica daca directorul ce se doreste a fi sters este gol
				// daca nu este, se afiseaza eroare
				if (c.currentDirectory.sub.size() >= 1) {
					printError(c, -14);
					c.restoreDir(bkp);
					return;
				}

				// daca user-ul curent este root, se "merge" in folderul parinte celui curent
				// si se sterge folderul dorit
				if (c.currentUser.name.equals("root")) {
					c.command = "cd ..";
					new CommandExecuter.Cd().executeCommand(c);
					c.currentDirectory.sub.remove(c.currentDirectory.sub.indexOf(toDel));

				} else

				// daca user-ul nu este root, se verifica permisiunile de write,
				// si se afiseaza eroare in caz ca nu exista permisiuni
				if (!c.hasRightToWrite(c.currentDirectory)) {
					printError(c, -5);
					c.restoreDir(bkp);
					return;
				}

				// daca user-ul nu este root, dar are permisiuni de stergere a folderului
				// se incearca "mergerea" in folderul parinte celui dorit
				else {

					c.command = "cd ..";
					new CommandExecuter.Cd().executeCommand(c);

					// se verifica daca user-ul curent are permisiuni de write pe directorul
					// parinte, si daca nu are se afiseaza eroare
					if (!c.hasRightToWrite(c.currentDirectory)) {
						c.restore(bkp, originalCommand);
						printError(c, -5);
						return;
					}

					// daca nu exista erori, se sterge directorul
					c.currentDirectory.sub.remove(c.currentDirectory.sub.indexOf(toDel));
				}

				c.restoreDir(bkp);
			}
		}
	}

	public static class WriteToFile implements Command {
		public void executeCommand(CommandExecuter c) {
			if (c.command.startsWith("writetofile")) {

				Directory bkp = c.currentDirectory;
				String originalCommand = c.command;

				// se pregateste path-ul catre fisierul in care trebuie sa scrie
				// limit tine pe pozitia - 0 path-ul spre fisier
				// 						 - 1 content-ul de scris
				String[] limit = c.command.substring(12).split(" \"");
				String pathTo = limit[0];
				String[] file = pathTo.split("/");

				// se pregateste numele fisierului in care se scrie
				File write = new File(
						pathTo.substring(pathTo.length() - file[file.length - 1].length()));

				pathTo = pathTo.substring(0, pathTo.length() - file[file.length - 1].length());

				// se incearca ajungerea in folderul fisierului in care vom scrie
				c.command = "cd" + " " + pathTo;
				new CommandExecuter.Cd().executeCommand(c);

				// daca fisierul in care vrem sa scriem e defapt un director, se afiseaza eroare
				if (c.currentDirectory.sub.indexOf(new Directory(write.name)) != -1) {
					c.restore(bkp, originalCommand);
					printError(c, -1);
					return;
				}

				try {

					// se incearca accesarea fisierului in care se va scrie
					write = (File) c.currentDirectory.sub
							.get(c.currentDirectory.sub.indexOf(write));

					// daca nu exista drepturi de write in fisier, se afiseaza eroare
					if (!c.hasRightToWrite(write)) {
						c.restore(bkp, originalCommand);
						printError(c, -5);
						return;
					}

					// se adauga content - ul in fisier intre ghilimele
					write.content = "\"" + limit[1];

				} catch (Exception e) {
					
					// daca fisierul in care dorim sa scriem nu exista, se afiseaza eroare
					if (e.getMessage().equals("-1")) {
						c.restore(bkp, originalCommand);
						printError(c, -11);
						return;
					}
				}

				c.restoreDir(bkp);
			}
		}
	}

	public static class Cat implements Command {
		public void executeCommand(CommandExecuter c) {
			if (c.command.startsWith("cat")) {

				Directory bkp = c.currentDirectory;
				String originalCommand = c.command;

				// se pregateste path-ul catre fisierul din care vrem sa citim
				String[] limit = c.command.substring(4).split("/");
				String pathTo = c.command.substring(4);

				String file = pathTo.substring(pathTo.length() - limit[limit.length - 1].length());
				pathTo = pathTo.substring(0, pathTo.length() - limit[limit.length - 1].length());

				// se incearca ajungerea la fisierul din care se va citi
				c.command = "cd" + " " + pathTo;
				try {
					c.cd = false;
					new CommandExecuter.Cd().executeCommand(c);
					c.cd = true;

					// se verifica daca daca unul dintre fisierele din path nu are drept de execute
					if (c.command.equals("permission"))
						throw new Exception();

				} catch (Exception e) {
					
					// daca nu are drept de execute, se afiseaza eroare
					if (c.command.equals("permission")) {
						c.restore(bkp, originalCommand);
						printError(c, -6);
						return;
					}
					
					// daca in path exista un fisier, se afiseaza eroare
					if (c.command.equals("file")) {
						c.restore(bkp, originalCommand);
						printError(c, -3);
						return;
					}

				}
				c.restoreCom(originalCommand);

				// daca fisierul din care trebuie citi este defapt un direct, se afiseaza eroare
				if (c.currentDirectory.sub.indexOf(new Directory(file)) != -1) {
					c.restore(bkp, originalCommand);
					printError(c, -1);
					return;
				}

				try {

					// incercam sa accesam fisierul din care trebuie citit
					File toPrint = (File) c.currentDirectory.sub
							.get(c.currentDirectory.sub.indexOf(new File(file)));

					// daca fisierul din care se citeste nu are drept de read, se afiseaza eroare
					if (!c.hasRightToRead(toPrint)) {
						c.restore(bkp, originalCommand);
						printError(c, -4);
						return;
					}

					// daca nu exista erori se printeaza content-ul
					System.out.println(toPrint.content);
					
				} catch (Exception e) {
					
					// daca nu exista o entitate cu numele dorit, se afiseaza eroare
					if (e.getMessage().equals("-1"))
						printError(c, -11);
				}

				c.restoreDir(bkp);
			}
		}
	}

	public static class Tree {
		public void executeCommand(CommandExecuter c, int iter) {

			// caz special ce printeaza doar root -ul
			if (iter == 1)
				System.out.println(c.root.name + " " + "d" + c.root.ownerPermission
						+ c.root.otherPermission + " " + c.root.owner);
			
			// se creeaza un vectorul cu taburi pentru a putea indenta corect ierarhia de fisiere
			char[] tabs = new char[iter];
			Arrays.fill(tabs, '\t');

			// se parcurge recursiv tot tree-ul cu radacina in /
			// se printeaza tipul entitatii + permisiuni + owner
			for (Entity element : c.currentDirectory.sub) {
				Directory bkp = c.currentDirectory;
				if (element instanceof File) {

					// print pentru indentare corecta
					System.out.print(new String(tabs));

					System.out.println(element.name + " " + "f" + element.ownerPermission
							+ element.otherPermission + " " + element.owner);
				} else {

					// print pentru indentare corecta
					System.out.print(new String(tabs));

					System.out.println(element.name + " " + "d" + element.ownerPermission
							+ element.otherPermission + " " + element.owner);

					c.currentDirectory = (Directory) element;
					new CommandExecuter.Tree().executeCommand(c, iter + 1);
				}
				c.restoreDir(bkp);
			}
		}
	}
}
