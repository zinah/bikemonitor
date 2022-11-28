# bikemonitor

En enkel web applikasjon med en API som gir en liste av sykkelstasjoner i Oslo med informasjon om navn, antall tilgjengelige låser og antall ledige sykkler. Den bruker [Bysykkel åpent API](https://oslobysykkel.no/apne-data/sanntid). Den er er skrevet i Kotlin og Spring og bruker Gradle.


## API spec

|  **Field name** | **Required**  |  **Beskrivelse** |
|---|---|---|
| stations  | ja | En liste med stasjoner |
| station_id  | ja | Stasjons ID  |
| name | ja | Statsjons fullt navn  |
| num_bikes_available | ja | Antall ledige sykkler på stasjonen|
| num_docks_available| ja | Antall tilgjengelige låser på stasjonen |

Et eksempel på svar fra APIen:

```
{
  "last_updated": 1669662578,
  "data": {
    "stations": [
      {
        "station_id": "2350",
        "name": "Blindern T-Bane",
        "num_bikes_available": 0,
        "num_docks_available": 25
      },
      {
        "station_id": "2349",
        "name": "Maritimt Museum",
        "num_bikes_available": 5,
        "num_docks_available": 16
      },
      (...)
    ]
  }
}
```

For å teste APIen kjør:
```
./gradlew bootRun
curl -v http://localhost:8080/bikemonitor/availability.json
```

## Unit tester
```
./gradlew test --info
```

## Docker

```
docker build -t example/bikemonitor .
docker run -p 8080:8080 example/bikemonitor
```

## TODO

- [ ] caching - data tilgjengelig fra den eksterne APIen oppdateres hvert 10. sekund og lista over stasjonenes detlajer endrer seg ikke så ofte - disse kunne lagres i Redis, MongoDB etc. Den kan oppstå en situasjon når data fra to eksterne endpoints are ikke synkroniserte og da må cache oppdateres unasett expiry time. Denne er merkert som TODO - ikke implementert i koden
- [ ] baseurlen til GBFS og urlen til Oslo Bysykkel sin API kunne lagres i en slags konfigurasjonsfil
- [ ] koden bruker `khttp` biblioteket får å kontakte den eksterne APIen. Dette biblioteket er utdatert, vedlikeholdes ikke lenger og støtter ikke nye versjoner av Java - må byttes til noe annet
- [ ] requests til den eksterne APIen bør bruke `Client-Identifier` header, grunnen at de ikke gjør det er at `khttp` biblioteket er utdatert og fungerer ikke lenger når man sender custom headers
- [ ] feil fra den eksterne APIen bør være håndtert
- [ ] validation av data som kommer fra den eksterne APIen
- [ ] flere tester, spesielt for funksjonen som setter data om tilgjengelighet og detaljer om stasjoner sammen og de funksjonene som laster ned JSON fra den eksterne APIen