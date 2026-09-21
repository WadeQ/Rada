# Cameras still needing coordinates

Source: the Money254 list of NTSA camera points (limits below are from that article; ranges use the lower figure). The Kenya Times list could not be read (bot-blocked).

Each entry needs a real latitude and longitude, and ideally the travel direction, taken on the road. Add it to `shared/src/commonMain/kotlin/com/rada/shared/data/FixedCameras.kt`.

| Road | Spot | Limit | Why it is not in the app |
|---|---|---|---|
| Mombasa Road | General Motors (from town) | 80 km/h | not found on the map |
| Mombasa Road | Before Luqman Station | 80 km/h | not found on the map |
| Thika Road | Jomoko | 80 km/h | not found on the map |
| Thika Road | Pangani | 80 km/h | not found on the map |
| Thika Road | Muthaiga Interchange | 80 km/h | not found on the map |
| Thika Road | Roysambu | 80 km/h | matched a road or area, not a specific spot |
| Thika Road | Thika town section | 80 km/h | matched a road or area, not a specific spot |
| Thika Road | Past Del Monte, near Shell station | 80 km/h | not found on the map |
| Waiyaki Way | Near Nairobi campus, approaching University Way roundabout | 60 km/h | not found on the map |
| University Way | After the roundabout, approaching University of Nairobi | 50 km/h | not found on the map |
| University Way | Harry Thuku Road / Muindi Mbingu Street junction | 50 km/h | not found on the map |
| CBD | Swaminarayan Road, towards UoN Law School | 50 km/h | matched a road or area, not a specific spot |
| CBD | Junction to Kolobot Road | 50 km/h | matched a road or area, not a specific spot |
| CBD | Racecourse Road | 50 km/h | matched a road or area, not a specific spot |
| CBD | Past Total Quarry Road | 50 km/h | map result was in Kware, nowhere near the CBD |
| Southern Bypass | Approaching Swaminarayan Street junction | 80 km/h | matched a road or area, not a specific spot |
| Southern Bypass | After Thogoto Interchange | 80 km/h | matched a road or area, not a specific spot |
| Southern Bypass | Before Dagoretti Interchange | 80 km/h | matched a road or area, not a specific spot |
| Southern Bypass | Nairobi River crossing, near Ondiri Swamp | 80 km/h | matched a road or area, not a specific spot |
| Southern Bypass | Kikuyu, near railway crossing | 80 km/h | matched a road or area, not a specific spot |
| Southern Bypass | Near Virtual Weighbridge, Kikuyu | 80 km/h | not found on the map |
| Southern Bypass | Ngong Road Interchange | 80 km/h | not found on the map |
| Lang'ata Road | Weston Hotel area, past Shell Wilson | 50 km/h | not found on the map |
| Limuru Road | Near Chenza Lane | 50 km/h | not found on the map |
| State House Road | Approaching St Andrews PCEA Church | 50 km/h | not found on the map |
| Jogoo Road | Past Bondo Street entry | 50 km/h | not found on the map |
| Jogoo Road | Past Rabai Road entry | 50 km/h | not found on the map |
| Eastleigh First Avenue | First Avenue junction | 50 km/h | matched a road or area, not a specific spot |
| Eastleigh First Avenue | After Mukuyu Avenue, before Fourth Street | 50 km/h | not found on the map |
| Kiambu Road | Past Runda Mall, approaching TotalEnergies Fourways | 50 km/h | not found on the map |
| Lower Kabete Road | Near junction before General Mathenge Drive | 50 km/h | matched a road or area, not a specific spot |
| Eastern Bypass | Near Waris Mall | 80 km/h | matched a road or area, not a specific spot |
| Airport North Road | Shell station past KQ Pride Centre junction | 50 km/h | not found on the map |
| Nairobi Expressway | Parliament Building area | 80 km/h | same spot as the CBD Parliament camera; can't tell the two roads apart without a direction |
| Nairobi Expressway | Before Tegla Loroupe Road | 80 km/h | not found on the map |
| Nairobi Expressway | Before Imaara Shopping Mall | 80 km/h | not found on the map |
| Nairobi Expressway | Approaching Syokimau, opposite Astrol station (towards town) | 80 km/h | matched a road or area, not a specific spot |
