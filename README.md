# GameSessions

<b><h3> Scopul </h3></b>

Scopul principal al aplicatiei este sa ofere o solutie de manageriere a jocurilor video. Utilizatorii isi pot contoriza timpul petrecut in jocurile video, isi pot pune un timp maxim pe care il pot juca zilnic. Isi pot vedea timpul total petrecut in jocuri si pot posta review-uri.

<b><h3>Reguli de business</h3></b>

<i> Reguli generale</i>
1. Accesul se poate face doar prin inregistrare, pentru a putea folosi aplicatia, utilizatorul este rugat sa se inregistreze sau sa se logheze.
2. Exista 3 tipuri de utilizatori: admin, manager si user. Admin-ul are acces la toate comenzile, manager-ul poate vedea anumite detalii din baza de date dar nu are acces la anumite comenzi de admin, iar user-ul poate folosi functionalitatile de baza ale aplicatiei.

<i> Comenzi specifice unui user/ manager/ admin </i> 

1. Se poate inregistra cu o adresa unica de e-mail (valida), un username unic (not blank), o parola (care va fi criptata in baza de date) si nume si prenume (ultimele doua campuri fiind optionale).
2. Isi poate schimba username-ul sau parola.
3. Poate incepe o sesiune de joc noua ( doar daca numele jocului pe care il introduce este unul valid, verificat printr-un API extern, rawg.io, care este o librarie externa ce contine toate jocurile video). User-ul poate sa isi incheie ulterior sesiunea.
4. Daca se depaseste limita de joc admisa zilnica, atunci sesiunea ii este incheiata si nu va mai putea incepe alta sesiune.
5. Poate vedea cat timp s-a jucat intr-o anumita zi / in ziua curenta.
6. Isi poate schimba timpul de joc maxim alocat pe zi.
7. Poate sa posteze un review al unui joc deja existent in baza de date de jocuri.

<i> Comenzi specifice unui manager/ admin </i>

1. Poate vedea sesiunile de joc dupa un ID, sortate alfabetic, dupa o un anumit timp minim de joc in sesiune, ordonate dupa timpul de incepere. Paginatia este inclusa.
2. Poate vedea utilizatorii dupa un minim de timp jucat, poate vedea cel mai mult sau cel mai putin timp jucat intr-o zi.
3. Poate adauga / vedea un joc in baza de date. Jocul este adaugat doar dupa ce este verificat de catre API-ul extern din rawg.io, de unde sa ia numele jocului, descrierea lui si rating-ul de pe Metacritic.
4. Poate vedea toate recenziile unui joc, daca jocul exista in baza de date.

<i> Comenzi specifice unui admin </i>

1. Poate vedea toti utilizatorii, toate sesiunile.
2. Poate sa schimbe username-ul si parola unui utilizator, poate sa ii schimbe si rolul.
3. Poate sterge un utilizator pe baza username-ului.
4. Poate sa vada toate sesiunile pe baza unui query SQL pe care acesta il introduce ca parametru. Daca parantezele din comanda SQL nu se inchid corespunzator, atunci nu se va executa comanda. Nu se poate face SQL Injection.

