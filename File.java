
public class File extends Entity{
	
	/**
	 * constructorul ce creeaza un fisier cu numele primit ca parametru
	 * @param name numele fisierului ce urmeaza sa fie creat
	 */
	public File(String name) {
		super(name);
	}

	public String content;
	
	/**
	 * metoda ce suprascrie metoda equals pentru a compara doua fisiere
	 */
	public boolean equals(Object o){
		if(o instanceof File)
			if(this.name.equals(((File) o).name))
				return true;
		return false;
	}

}
