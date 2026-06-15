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
- `Predmet` se sada cuva u Arango pod poljima `nazivPredmeta` i `nivoKojiNudi`, uz backward compatibility za stare dokumente sa `naziv`/`nivo`.
- XML import sada ucitava XSD seme preko `ClassPathResource.getInputStream()` i `StreamSource`, pa radi i kada je backend spakovan kao Spring Boot JAR.
- Frontend preporuke sada povezuju preporuceni oglas sa punim oglasom preko normalizovanog exact ID match-a, bez substring poredjenja.
- Import seme vise ne zahtevaju `agencijaId` za oglase ni `studentId` za polaganja; polja su i dalje dozvoljena zbog kompatibilnosti, ali ih backend prepisuje iz request parametara.
- Duplirane vestine u jednom oglasu su blokirane na backendu kroz `OglasService`, a frontend forma u `AgencijaDashboard` onemogucava izbor iste vestine u vise redova.
- Paginacija u `FusekiService` sada clampuje `stranica >= 1` i `poStranici` na opseg `1..100` pre SPARQL `LIMIT/OFFSET`.
- Provere koje trenutno prolaze:
  - `mvn -q -DskipTests compile`
  - `npm run lint`
  - `npm run build`

## Odlozeno dok se ne sredjuju podaci

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

## Namerno ostavljeno kako jeste

Ovo ne treba dirati za sada, jer je tako dogovoreno za fakultetski/lokalni projekat:

- Hardkodovan `http://localhost:8080` u frontendu.
- UI-only dugmad koja prikazuju mogucnost prosirenja sistema.
- Plain-text lozinke u bazi, bez BCrypt-a.

## Preporuceni redosled rada

1. Kada sistemske stvari budu dobre, vratiti se na demo/import JSON ID-jeve.

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
