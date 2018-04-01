import java.util.ArrayList;
import java.util.List;

public class Directory extends Entity {
	
	/**
	 * constructorul ce creeaza un directorul cu numele primit ca parametru
	 * @param name numele directorului de creeat
	 */
	public Directory(String name) {
		super(name);
	}

	// lista ce contine sub-entitatile directorului curent
	public List<Entity> sub = new ArrayList<>();

	/**
	 * metoda ce suprascrie metoda equals pentru a compara doua directoare
	 */
	public boolean equals(Object o){
		if(o instanceof Directory)
			if(this.name.equals(((Directory) o).name))
				return true;
		return false;
	}
}
