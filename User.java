
public class User {
	public String name;
	public Directory homeDir;

	/**
	 * constructorul ce creeaza un user cu numele primit ca parametru
	 * @param name numele userului de creeat
	 */
	public User(String name) {
		this.name = name;
	}

	/**
	 * metoda ce suprascrie metoda equals pentru a compara doi useri
	 */
	public boolean equals(Object o) {
		if (o instanceof User)
			if (this.name.equals(((User) o).name))
				return true;
		return false;		
	}
}
