# Unix-terminal-simulation

*Test - main
- pentru a obtine comanda dorita se foloseste design patern-ul Factory; acesta returneaza pe baza numelui comenzii o instanta a clasei ce implementeaza metoda;
- pentru a obtine numele comenzii citim din fisierul de input;
- la final dupa ce s-au executat toate comenzile, se va afisa intreaga ierarhie de fisiere;

-------------------------- Clase --------------------------------
- Entity: - reprezinta entitatile cu tot ce contin ele: nume, permisiuni, owner, directorul parinte;

- Directory - mosteneste clasa Entity si are in plus fata de aceasta o lista de subdirectoare; astfel este utilizat design patern-ul composite si se creeaza ierarhia de fisiere;

- File - mosteneste clasa Entity si are in plus fata de aceasta un string cu content-ul fisierului;

- User - reprezinta userii cu tot ce au ei: nume si directorul home;

- CommandFactory - reprezinta "fabrica" de instante de comenzi;

- Command - interfata cu metoda executeCommand ce va fi implementata de clasa CommandExecuter;

- CommandExecuter - clasa ce contine clasele interne ce implementeaza comenzile; tot aceasta clasa retine ierarhia de fisiere si ce este reprezentativ acesteia: directorul curent, directorul root, userul curent, lista de useri, dar si comanda ce se trebuie rulata;
* contine si un camp cd - spune comenzii cd daca atunci cand aceasta este executata ea este executata din bash sau din alta comanda;


------------------------- Comenzi ------------------------------


*Adduser
- are loc doar daca user-ul curent este root;
- creeaza un user, il adauga in lista de useri si ii creeaza si un director home cu acelasi nume si il pune in /.
- in cazul in care acesta exista sau daca user-ul curent nu este root -> se afiseaza eroarea corespunzatoare;

*Deluser
- are loc doar daca user-ul curent este root;
- daca acesta exista, el este sters si apoi se parcurg recursiv toate fisierele si in cazul in care user-ul sters este owner-ul fisierului, owner-ul este modificat la primul utilizator adaugat in lista de useri;
- in cazul in care user-ul nu exista sau daca user-ul ce curent nu este root -> se afiseaza eroarea corespunzatoare;

*Chuser
- se schimba user-ul curent, daca acesta exista;
- odata cu schimbarea user-ului, se schimba si directorul curent cu directorul home al user-ului nou schimbat;
- in cazul in care user-ul nu exista -> se afiseaza eroarea corespunzatoare;

*Cd
- se incearca schimbarea directorului curent;
- se verifica mai intai daca calea primita este una absoluta sau una relativa;
- se incearca intrarea pe rand prin fiecare folder pana la gasirea folderului;
- respecta un flag ce tine cont daca comanda a fost data din bash sau din alta comanda;
- daca comanda este rulata din alta comanda, in loc sa afiseze un mesaj, aceasta va seta un "flag" si se va intoarce in comanda originala, care va afisa mesajul de eroare;
- in cazul in care comanda intampina o problema -> se afiseaza eroarea corespunzatoare;

*Mkdir
- se incearca creerea unui nou folder;
- cazul "mkdir /" este considerat un caz special, adica odata ce se creeaza folderul /, se creeaza si user-ul "root";
- inainte de a creea directorul se incearca folosirea comenzii cd pentru a ajunge la calea in care se doreste a se creea folderul;
- in cazul in care comanda cd sau mkdir intampina o problema -> se afiseaza eroarea corespunzatoare;

*Ls
- se incearca listarea continutului unui folder;
- inainte de a lista continutul unui director se incearca folosirea comenzii cd pentru a ajunge la calea din care se doreste a se lista;
- se poate lista numai din fisiere;
- in cazul in care comanda cd sau ls intampina o problema -> se afiseaza eroarea corespunzatoare;

*Chmod
- se incearca schimbarea permisiunilor unei entitati;
- inainte de a schimba permisiunile se incearca folosirea comenzii cd pentru a ajunge la calea la care este entitatea careia dorim sa ii schimbam permisiunile;
- in cazul in care comanda cd sau chmod intampina o problema -> se afiseaza eroarea corespunzatoare;

*Touch
- se incearca creerea unui fisier;
- inainte de a creea fisierul se incearca folosirea comenzii cd pentru a ajunge la calea la care trebuie sa creem fisierul;
- se verifica sa nu existe deja un director cu acelasi nume cu fisierul pe care dorim sa il creem;
- in cazul in care comanda cd sau touch intampina o problema -> se afiseaza eroarea corespunzatoare;

*Rm
- se incearca stergerea unui fisier sau a unei ierarhii;
- exista in doua variante: rm <path> sau rm -r <path>;
- ambele acceseaza calea pana la entitatea/ieararhia care se doreste a fi stearsa;
- rm nu poate sterge directoare;
- comanda rm -r necesita drepturi de scriere doar pe directorul de la care pleaca ierarhia;
- in cazul in care comanda cd sau touch intampina o problema -> se afiseaza eroarea corespunzatoare;

*Rmdir
- se incearca stergerea unui director;
- inainte de a sterge directorul, se foloseste comanda cd pentru a ajunge la calea in care se afla directorul de sters;
- directorul trebuie sa fie gol pentru a putea fi sters;
- in cazul in care comanda cd sau touch intampina o problema -> se afiseaza eroarea corespunzatoare;

*WriteToFile
- se incearca scrierea intr-un fisier al unui content;
- se poate scrie doar in fisiere;
- inainte de a scrie in fisier, se foloseste comanda cd pentru a ajunge la calea in care se afla fisierul in care trebuie scris;
- in cazul in care comanda cd sau touch intampina o problema -> se afiseaza eroarea corespunzatoare;

*Cat
- se incearca listarea content-ului unui fisier;
- se poate citi doar dintr-un fisier;
- inainte de a lista continutul, se foloseste comanda cd pentru a ajunge la calea in care se afla fisierul care trebuie listat;
- in cazul in care comanda cd sau touch intampina o problema -> se afiseaza eroarea corespunzatoare;

******
Extra*
******

- exista in clasa CommandExecuter metode ce verifica drepturile de citire scriere executie a unei entitati primite ca parametru in raport cu userul curent;

- exista o metoda ce afiseaza mesajul de eroare pe baza unui numar de eroare "errNo", fiecare de la -1 la -14 corespunzand unui mesaj de eroare diferit;

- exista o metoda de restore a directorului curent sau al comenzii originale; deoarece se foloseste comanda cd in interiorul alor comenzi, atat directorul curent cat si comanda originala sunt alterate, asa ca la sfarsitul tuturor comenzilor, fie ca acestea se termina cu succes sau ca se termina cu eroare, se face restore la parametrii originali; uneori se folosesc aceste metode chiar si in interiorul codului metodelor in cazul in care este nevoie;
