package com.rada.shared.data

import com.rada.shared.domain.ReportType
import com.rada.shared.domain.SpeedCamera

object FixedCameras {

    val approximate = listOf(
        SpeedCamera(
            id = "mombasa-sameer", latitude = -1.329581, longitude = 36.869511,
            speedLimit = 80.0, bearing = SpeedCamera.ANY_DIRECTION, type = ReportType.CAMERA,
            placeName = "Mombasa Road · Sameer Business Park / General Motors", approximate = true
        ),
        SpeedCamera(
            id = "mombasa-cabanas", latitude = -1.335589, longitude = 36.893227,
            speedLimit = 80.0, bearing = SpeedCamera.ANY_DIRECTION, type = ReportType.CAMERA,
            placeName = "Mombasa Road · Near Cabanas, approaching JKIA", approximate = true
        ),
        SpeedCamera(
            id = "mombasa-south-cemetery", latitude = -1.296276, longitude = 36.820381,
            speedLimit = 80.0, bearing = SpeedCamera.ANY_DIRECTION, type = ReportType.CAMERA,
            placeName = "Mombasa Road · Nairobi South Cemetery, near Railway Golf Club", approximate = true
        ),
        SpeedCamera(
            id = "mombasa-bunyala", latitude = -1.297790, longitude = 36.829097,
            speedLimit = 80.0, bearing = SpeedCamera.ANY_DIRECTION, type = ReportType.CAMERA,
            placeName = "Mombasa Road · Bunyala Road roundabout", approximate = true
        ),
        SpeedCamera(
            id = "mombasa-capital-centre", latitude = -1.316378, longitude = 36.834620,
            speedLimit = 80.0, bearing = SpeedCamera.ANY_DIRECTION, type = ReportType.CAMERA,
            placeName = "Mombasa Road · Capital Centre", approximate = true
        ),
        SpeedCamera(
            id = "mombasa-panari", latitude = -1.329029, longitude = 36.855930,
            speedLimit = 80.0, bearing = SpeedCamera.ANY_DIRECTION, type = ReportType.CAMERA,
            placeName = "Mombasa Road · Opposite Panari Hotel (towards town)", approximate = true
        ),
        SpeedCamera(
            id = "mombasa-mlolongo", latitude = -1.389446, longitude = 36.937909,
            speedLimit = 80.0, bearing = SpeedCamera.ANY_DIRECTION, type = ReportType.CAMERA,
            placeName = "Mombasa Road · Near Mlolongo Weighbridge", approximate = true
        ),
        SpeedCamera(
            id = "thika-safari-park", latitude = -1.225221, longitude = 36.883482,
            speedLimit = 110.0, bearing = SpeedCamera.ANY_DIRECTION, type = ReportType.CAMERA,
            placeName = "Thika Road · Near Safari Park", approximate = true
        ),
        SpeedCamera(
            id = "thika-gsu", latitude = -1.242747, longitude = 36.863116,
            speedLimit = 80.0, bearing = SpeedCamera.ANY_DIRECTION, type = ReportType.CAMERA,
            placeName = "Thika Road · GSU Headquarters", approximate = true
        ),
        SpeedCamera(
            id = "thika-muthaiga-primary", latitude = -1.258282, longitude = 36.845978,
            speedLimit = 80.0, bearing = SpeedCamera.ANY_DIRECTION, type = ReportType.CAMERA,
            placeName = "Thika Road · Before Muthaiga Primary School", approximate = true
        ),
        SpeedCamera(
            id = "thika-moi-sports", latitude = -1.227548, longitude = 36.890182,
            speedLimit = 80.0, bearing = SpeedCamera.ANY_DIRECTION, type = ReportType.CAMERA,
            placeName = "Thika Road · Moi International Sports Centre (towards town)", approximate = true
        ),
        SpeedCamera(
            id = "thika-trm", latitude = -1.219650, longitude = 36.888529,
            speedLimit = 80.0, bearing = SpeedCamera.ANY_DIRECTION, type = ReportType.CAMERA,
            placeName = "Thika Road · Approaching TRM Mall (towards Thika)", approximate = true
        ),
        SpeedCamera(
            id = "thika-kahawa-sukari", latitude = -1.180695, longitude = 36.930847,
            speedLimit = 80.0, bearing = SpeedCamera.ANY_DIRECTION, type = ReportType.CAMERA,
            placeName = "Thika Road · Kahawa Sukari, near Kenyatta University", approximate = true
        ),
        SpeedCamera(
            id = "thika-ruiru-sports", latitude = -1.130754, longitude = 36.990864,
            speedLimit = 80.0, bearing = SpeedCamera.ANY_DIRECTION, type = ReportType.CAMERA,
            placeName = "Thika Road · Near Ruiru Sports Club", approximate = true
        ),
        SpeedCamera(
            id = "thika-muthaiga-police", latitude = -1.258895, longitude = 36.843186,
            speedLimit = 80.0, bearing = SpeedCamera.ANY_DIRECTION, type = ReportType.CAMERA,
            placeName = "Thika Road · Past Muthaiga Police Station", approximate = true
        ),
        SpeedCamera(
            id = "waiyaki-abc", latitude = -1.259443, longitude = 36.777650,
            speedLimit = 60.0, bearing = SpeedCamera.ANY_DIRECTION, type = ReportType.CAMERA,
            placeName = "Waiyaki Way · Near Ola station / TotalEnergies, before ABC Place", approximate = true
        ),
        SpeedCamera(
            id = "waiyaki-consolata", latitude = -1.267425, longitude = 36.803015,
            speedLimit = 60.0, bearing = SpeedCamera.ANY_DIRECTION, type = ReportType.CAMERA,
            placeName = "Waiyaki Way · Opposite Consolata Shrine (towards CBD)", approximate = true
        ),
        SpeedCamera(
            id = "waiyaki-villa-rosa", latitude = -1.271483, longitude = 36.809092,
            speedLimit = 60.0, bearing = SpeedCamera.ANY_DIRECTION, type = ReportType.CAMERA,
            placeName = "Waiyaki Way · After Villa Rosa Kempinski", approximate = true
        ),
        SpeedCamera(
            id = "cbd-parliament", latitude = -1.290031, longitude = 36.819808,
            speedLimit = 50.0, bearing = SpeedCamera.ANY_DIRECTION, type = ReportType.CAMERA,
            placeName = "CBD · Parliament Buildings area", approximate = true
        ),
        SpeedCamera(
            id = "cbd-ngara-pangani-girls", latitude = -1.268955, longitude = 36.837370,
            speedLimit = 50.0, bearing = SpeedCamera.ANY_DIRECTION, type = ReportType.CAMERA,
            placeName = "CBD · Ring Road Ngara, near Pangani Girls High School", approximate = true
        ),
        SpeedCamera(
            id = "cbd-starehe-boys", latitude = -1.280527, longitude = 36.839962,
            speedLimit = 50.0, bearing = SpeedCamera.ANY_DIRECTION, type = ReportType.CAMERA,
            placeName = "CBD · General Waruinge Street, near Starehe Boys Centre", approximate = true
        ),
        SpeedCamera(
            id = "cbd-globe-roundabout", latitude = -1.278654, longitude = 36.821248,
            speedLimit = 50.0, bearing = SpeedCamera.ANY_DIRECTION, type = ReportType.CAMERA,
            placeName = "CBD · Globe Roundabout flyover", approximate = true
        ),
        SpeedCamera(
            id = "cbd-lusaka-city-stadium", latitude = -1.293674, longitude = 36.842332,
            speedLimit = 50.0, bearing = SpeedCamera.ANY_DIRECTION, type = ReportType.CAMERA,
            placeName = "CBD · Lusaka Road, near City Stadium", approximate = true
        ),
        SpeedCamera(
            id = "southern-uhuru-gardens", latitude = -1.325926, longitude = 36.799300,
            speedLimit = 80.0, bearing = SpeedCamera.ANY_DIRECTION, type = ReportType.CAMERA,
            placeName = "Southern Bypass · Opposite Uhuru Gardens", approximate = true
        ),
        SpeedCamera(
            id = "langata-barracks", latitude = -1.334939, longitude = 36.789923,
            speedLimit = 50.0, bearing = SpeedCamera.ANY_DIRECTION, type = ReportType.CAMERA,
            placeName = "Lang'ata Road · Approaching Lang'ata Barracks", approximate = true
        ),
        SpeedCamera(
            id = "langata-bomas", latitude = -1.337589, longitude = 36.768667,
            speedLimit = 50.0, bearing = SpeedCamera.ANY_DIRECTION, type = ReportType.CAMERA,
            placeName = "Lang'ata Road · Bomas of Kenya", approximate = true
        ),
        SpeedCamera(
            id = "langata-cemetery", latitude = -1.332638, longitude = 36.777118,
            speedLimit = 50.0, bearing = SpeedCamera.ANY_DIRECTION, type = ReportType.CAMERA,
            placeName = "Lang'ata Road · Kungu Karumba Road, near Lang'ata Cemetery", approximate = true
        ),
        SpeedCamera(
            id = "ngong-prestige", latitude = -1.300904, longitude = 36.787060,
            speedLimit = 50.0, bearing = SpeedCamera.ANY_DIRECTION, type = ReportType.CAMERA,
            placeName = "Ngong Road · Before Prestige Plaza", approximate = true
        ),
        SpeedCamera(
            id = "ngong-ndemi-lane", latitude = -1.301744, longitude = 36.773213,
            speedLimit = 50.0, bearing = SpeedCamera.ANY_DIRECTION, type = ReportType.CAMERA,
            placeName = "Ngong Road · Before Ndemi Lane, approaching Ligi Ndogo", approximate = true
        ),
        SpeedCamera(
            id = "raila-t-mall", latitude = -1.312248, longitude = 36.816942,
            speedLimit = 50.0, bearing = SpeedCamera.ANY_DIRECTION, type = ReportType.CAMERA,
            placeName = "Raila Odinga Way · Approaching T-Mall roundabout", approximate = true
        ),
        SpeedCamera(
            id = "james-gichuru-st-austins", latitude = -1.276658, longitude = 36.772124,
            speedLimit = 50.0, bearing = SpeedCamera.ANY_DIRECTION, type = ReportType.CAMERA,
            placeName = "James Gichuru Road · Near roundabout at St Austin's Academy", approximate = true
        )
    )
}
