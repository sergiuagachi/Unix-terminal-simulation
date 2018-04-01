
public class Entity {
	
	public String name;
	public String ownerPermission = "rwx";
	public String otherPermission = "---";
	public String owner;
	public Directory parent;

	/**
	 * constructorul ce creeaza o entitate cu numele primit ca parametru
	 * @param name numele entitatii de creeat
	 */
	public Entity(String name) {
		this.name = name;
	}

	/**
	 * metoda ce suprascrie metoda equals pentru a compara doua entitati
	 */
	public boolean equals(Object o) {
		if (o instanceof Entity)
			if (this.name.equals(((Entity) o).name))
				return true;
		return false;
	}

	/**
	 * metoda ce seteaza permisiunile pe baza string-ului primit la intrare 
	 * @param perm stringul ce contine - prima pozitie in string se afla cifra permisiunilor ownerului
	 *    							   - a doua pozitie se afla cifra asciata permisiunilor celorlalti useri
	 */
	public void setPermission(String perm) {
		char own = perm.charAt(0);
		char oth = perm.charAt(1);

		switch (own) {
		case '0':
			ownerPermission = "---";
			break;
		case '1':
			ownerPermission = "--x";
			break;
		case '2':
			ownerPermission = "-w-";
			break;
		case '3':
			ownerPermission = "-wx";
			break;
		case '4':
			ownerPermission = "r--";
			break;
		case '5':
			ownerPermission = "r-x";
			break;
		case '6':
			ownerPermission = "rw-";
			break;
		case '7':
			ownerPermission = "rwx";
			break;
		}
		switch (oth) {
		case '0':
			otherPermission = "---";
			break;
		case '1':
			otherPermission = "--x";
			break;
		case '2':
			otherPermission = "-w-";
			break;
		case '3':
			otherPermission = "-wx";
			break;
		case '4':
			otherPermission = "r--";
			break;
		case '5':
			otherPermission = "r-x";
			break;
		case '6':
			otherPermission = "rw-";
			break;
		case '7':
			otherPermission = "rwx";
			break;
		}
	}
}
