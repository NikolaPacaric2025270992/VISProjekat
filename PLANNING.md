# Plan za nastavak rada

Ovaj fajl je radna lista za sledeci prolaz kroz projekat. Aktivni kod je u:

- `Projekat/Backend/VBIS_Projekat`
- `Projekat/Frontend`

Folder `Staro/` tretirati kao arhivu, osim ako se posebno ne odluci da se i on sredjuje.

## Vec reseno

- `DatabaseSeederService` sada prvo cita i proverava Arango podatke, pa tek onda cisti Fuseki.
- Brisanje studenta/agencije je kaskadno za polaganja/oglase.
- Dodati su rollback pokusaji za Arango upis ako Fuseki upis padne.
- SPARQL string literali i RDF ID-jevi su osnovno validirani/escape-ovani.
- Import seme su uskladjene sa enum/model vrednostima za `Prioritet` i `Polaganje.ocena`.
- Login/update response vise ne vraca lozinku kroz `StudentResponseDTO` i `AgencijaResponseDTO`.
- Promena lozinke ide preko backend endpointa `/promeni-lozinku`.
- Frontend `catch (err/error)` lint greske su ociscene.
- Provere koje trenutno prolaze:
  - `mvn -q -DskipTests compile`
  - `npm run lint`
  - `npm run build`

## Otvoreno za sutra

### P2: `Predmet` se cuva u Arango pod pogresnim imenima polja

Problem:

- `ArangoService.sacuvajPredmet` upisuje `naziv` i `nivo`.
- `Predmet` model ocekuje `nazivPredmeta` i `nivoKojiNudi`.
- Seed fajlovi vec koriste ispravna imena, zato aplikacija radi u glavnom demo toku.
- Problem se javlja ako se predmet doda kroz API, pa se posle restarta procita iz Aranga i sinhronizuje u Fuseki.

Predlog:

- U `sacuvajPredmet` upisivati `nazivPredmeta` i `nivoKojiNudi`.
- Po zelji u `sviPredmeti` dodati backward compatibility za stare dokumente koji imaju `naziv`/`nivo`.

Fajlovi:

- `Projekat/Backend/VBIS_Projekat/src/main/java/com/mycompany/vbisapi/service/ArangoService.java`
- `Projekat/Backend/VBIS_Projekat/src/main/java/com/mycompany/vbisapi/model/Predmet.java`

### P2: Demo/import podaci nisu iz iste familije ID-jeva

Problem:

- `SeederCollections/vestine.json` koristi ID-jeve tipa `v_java`, `v_reactjs`.
- `oglasi-import.json` koristi `vestina_java`, `vestina_react`, `vestina_baze`.
- `SeederCollections/predmeti.json` koristi predmete tipa `pr_oop1`, `pr_baze_podataka`.
- `polaganja-import.json` koristi `predmet_it_uvod_java`, `predmet_it_mreze`.
- Import moze proci validaciju, ali preporuke nece raditi kako se ocekuje jer RDF veze ne pogadjaju iste vestine/predmete.

Predlog:

- Odabrati jednu konvenciju ID-jeva.
- Najjednostavnije: prilagoditi `oglasi-import.json` i `polaganja-import.json` da koriste `SeederCollections` ID-jeve.
- Ako se koriste stari root fajlovi `vestine.json` i `predmeti.json`, jasno odvojiti koji set je demo set.

Fajlovi:

- `Projekat/oglasi-import.json`
- `Projekat/polaganja-import.json`
- `Projekat/SeederCollections/vestine.json`
- `Projekat/SeederCollections/predmeti.json`

### P2: XML import koristi `ClassPathResource.getFile()`

Problem:

- `ImportService` za XSD seme koristi `new ClassPathResource(...).getFile()`.
- To radi iz IDE-a/exploded classes, ali moze pasti kada se backend pokrene kao Spring Boot JAR.

Predlog:

- Zameniti `getFile()` sa `getInputStream()` i `new StreamSource(schemaStream)`.
- Uraditi i za `oglas-schema.xsd` i za `polaganje-schema.xsd`.

Fajl:

- `Projekat/Backend/VBIS_Projekat/src/main/java/com/mycompany/vbisapi/service/ImportService.java`

### P2: Frontend preporuka trazi oglas preko substringa

Problem:

- `StudentDashboard.jsx` koristi `idPreporuke.includes(idOglasa) || idOglasa.includes(idPreporuke)`.
- Ako postoje `oglas_1` i `oglas_10`, moze se povezati pogresan oglas.

Predlog:

- Normalizovati ID-jeve i koristiti exact equality.
- Ako backend vec vraca ID oglasa iz Fuseki kao isti ID koji Arango koristi, dovoljno je `idPreporuke === idOglasa`.

Fajl:

- `Projekat/Frontend/src/pages/StudentDashboard.jsx`

### P3: Import seme traze polja koja backend svakako prepisuje

Problem:

- `oglas-schema.json`/XSD zahtevaju `agencijaId`, a controller ga prepisuje iz request parametra.
- `polaganje-schema.json`/XSD zahtevaju `studentId`, a controller ga prepisuje iz request parametra.
- Zbog toga korisnik mora da ubaci placeholder vrednosti u import fajl, iako se one ne koriste.

Predlog:

- U import semama napraviti `agencijaId` i `studentId` opcionim.
- Ostaviti ih dozvoljenim zbog kompatibilnosti, ali ne kao `required`.

Fajlovi:

- `Projekat/Backend/VBIS_Projekat/src/main/resources/schemas/oglas-schema.json`
- `Projekat/Backend/VBIS_Projekat/src/main/resources/schemas/oglas-schema.xsd`
- `Projekat/Backend/VBIS_Projekat/src/main/resources/schemas/polaganje-schema.json`
- `Projekat/Backend/VBIS_Projekat/src/main/resources/schemas/polaganje-schema.xsd`

### P3: Duplirana vestina u jednom oglasu pravi isti RDF zahtev ID

Problem:

- `FusekiService.sacuvajOglasURDF` pravi zahtev ID kao `Zahtev_` + `oglasId` + `vestinaId`.
- UI dozvoljava da se ista vestina doda vise puta u isti oglas.
- Duplikati zavrse na istom RDF cvoru, sto moze dati cudno bodovanje.

Predlog:

- Frontend: spreciti izbor iste vestine vise puta u jednom oglasu.
- Backend: dodatno validirati `zahtevaneVestine` i odbiti duplikate.
- Alternativno: u RDF zahtev ID dodati indeks, ali to dozvoljava semanticki sumnjive duplikate.

Fajlovi:

- `Projekat/Frontend/src/pages/AgencijaDashboard.jsx`
- `Projekat/Backend/VBIS_Projekat/src/main/java/com/mycompany/vbisapi/service/FusekiService.java`
- `Projekat/Backend/VBIS_Projekat/src/main/java/com/mycompany/vbisapi/service/OglasService.java`

### Nizi prioritet: paginacija nema validaciju

Problem:

- `stranica` i `poStranici` se direktno koriste za `LIMIT`/`OFFSET`.
- Negativne ili nulte vrednosti mogu napraviti SPARQL gresku.

Predlog:

- Clamp-ovati `stranica >= 1`.
- Clamp-ovati `poStranici` na razuman opseg, npr. `1..100`.

Fajl:

- `Projekat/Backend/VBIS_Projekat/src/main/java/com/mycompany/vbisapi/service/FusekiService.java`

## Namerno ostavljeno kako jeste

Ovo ne treba dirati za sada, jer je tako dogovoreno za fakultetski/lokalni projekat:

- Hardkodovan `http://localhost:8080` u frontendu.
- UI-only dugmad koja prikazuju mogucnost prosirenja sistema.
- Plain-text lozinke u bazi, bez BCrypt-a.

## Preporuceni redosled rada

1. Popraviti `Predmet` mapiranje u Arango.
2. Uskladiti demo/import JSON fajlove sa seed ID-jevima.
3. Popraviti XML import da koristi `InputStream`.
4. Popraviti exact match oglasa u `StudentDashboard`.
5. Olaksati import seme tako da placeholder ID-jevi nisu obavezni.
6. Dodati zastitu od dupliranih vestina u oglasu.
7. Na kraju dodati validaciju paginacije.

## Komande za proveru

Backend:

```powershell
cd "Projekat/Backend/VBIS_Projekat"
mvn -q -DskipTests compile
```

Frontend:

```powershell
cd "Projekat/Frontend"
npm run lint
npm run build
```
