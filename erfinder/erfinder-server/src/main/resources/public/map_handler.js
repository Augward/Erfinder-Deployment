let ER_MAP = null;
let facilitiesLoaded = false;
let ER_MARKERS = [];
let USER_LOCATION = null;
let USER_MARKER = null;
let SEARCH_MARKER = null;
let FACILITIES_CACHE = [];
let INPUT_POSITION = null;
let INPUT_ZOOM = 9;
let USE_INPUT = false;
let FADED_PK = -1;

function initLeafletSafely() {
    var m = document.getElementById('map');
    if (m) {
        m.style.width = '100%';
        m.style.height = '100%';
    } else {
        m = document.getElementById('landing-map');
    }

    if (!m) return;


    if (!ER_MAP) {
        ER_MAP = L.map(m).setView([41.8, -91.6], 9);

        L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
            attribution: '&copy; OpenStreetMap contributors'
        }).addTo(ER_MAP);

        console.log('Map initialized');

        resetSearchMarker(41.8, -91.6);
    }

    ER_MAP.invalidateSize(true);

    if (!facilitiesLoaded) {
        facilitiesLoaded = true;
        loadERFacilities();
        setInterval(loadERFacilities, 10000);
        showUserLocation();
    }
}

setTimeout(initLeafletSafely, 500);
setTimeout(initLeafletSafely, 1500);
setTimeout(initLeafletSafely, 3000);

function loadERFacilities() {
    if (!ER_MAP) return;

    fetch('/api/facilities')
        .then(res => res.json())
        .then(facilities => {
            console.log('Facilities loaded:', facilities);
            clearERMarkers();
            FACILITIES_CACHE = facilities;
            facilities.forEach(f => addERMarker(f));
        })
        .catch(err => console.error('Failed to load facilities', err));
}
function addERMarker(f) {
    const lat = Number(f.lat);
    const lon = Number(f.lon);

    if (!Number.isFinite(lat) || !Number.isFinite(lon)) {
        console.warn('Invalid coordinates:', f);
        return;
    }

    const marker = L.marker([lat, lon]).addTo(ER_MAP);
    marker.erpk = f.id;

    if (FADED_PK >= 0 && FADED_PK != marker.erpk) {
        marker.setOpacity(0.5);
    }

    marker.bindPopup(`
        <strong>${f.name}</strong><br/>
        Trauma Level: ${f.trauma}<br/>
        Specialties: ${f.specialties}<br/>
        Beds Available: ${f.beds}<br/>
        Wait Time: ${f.wait} minutes
    `);

    ER_MARKERS.push(marker);
}
function clearERMarkers() {
    ER_MARKERS.forEach(marker => ER_MAP.removeLayer(marker));
    ER_MARKERS = [];
}

function fadeERMarkers(pk_keep) {
    ER_MARKERS.forEach(marker => { if (marker.erpk != pk_keep) { marker.setOpacity(0.5); } });
}

function unfadeERMarkers() {
    ER_MARKERS.forEach(marker => marker.setOpacity(1.0));
    FADED_PK = -1;
}

function resetSearchMarker(lat, lon) {
    if (SEARCH_MARKER) {
        ER_MAP.removeLayer(SEARCH_MARKER);
    }

    SEARCH_MARKER = L.circleMarker([lat, lon], {
         radius: 8,
         color: '#ba70ef',
         fillColor: '#ef70e5',
         fillOpacity: 0.9,
         weight: 2
    }).addTo(ER_MAP);
    // SEARCH_MARKER.draggable = true;
    // SEARCH_MARKER.autoPan = true;
    ER_MAP.on('move', (e) => {
        if (USE_INPUT) {
            SEARCH_MARKER.setLatLng(INPUT_POSITION);
        }
        else {
            SEARCH_MARKER.setLatLng(ER_MAP.getCenter());
        }
    });
}
function showUserLocation() {
    if (!navigator.geolocation) {
        console.warn('Geolocation not supported');
        return;
    }

    navigator.geolocation.getCurrentPosition(
        pos => {
            const lat = pos.coords.latitude;
            const lon = pos.coords.longitude;

            console.log('User location:', lat, lon);

            USER_LOCATION = { lat, lon };


            // Remove previous marker if any
            if (USER_MARKER) {
                ER_MAP.removeLayer(USER_MARKER);
            }

            USER_MARKER = L.circleMarker([lat, lon], {
                radius: 8,
                color: '#991b1b',
                fillColor: '#ef4444',
                fillOpacity: 0.9,
                weight: 2
            }).addTo(ER_MAP);

            resetSearchMarker(lat, lon);

            USER_MARKER.bindPopup('<strong>Your Location</strong>');

            ER_MAP.invalidateSize(true);
            ER_MAP.setView([lat,lon], 13);
        },
        err => {
            console.error('Location error:', err);
        },
        { enableHighAccuracy: true }
    );
}
function distanceKm(lat1, lon1, lat2, lon2) {
    const R = 6371;
    const dLat = (lat2 - lat1) * Math.PI / 180;
    const dLon = (lon2 - lon1) * Math.PI / 180;

    const a =
        Math.sin(dLat / 2) ** 2 +
        Math.cos(lat1 * Math.PI / 180) *
        Math.cos(lat2 * Math.PI / 180) *
        Math.sin(dLon / 2) ** 2;

    return 2 * R * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
}
function recommendERs() {
    if (!USER_LOCATION || FACILITIES_CACHE.length === 0) return;

    let bestFacility = null;
    let bestDistance = Infinity;

    FACILITIES_CACHE.forEach(f => {
        const lat = Number(f.lat);
        const lon = Number(f.lon);
        if (!Number.isFinite(lat) || !Number.isFinite(lon)) return;

        const d = distanceKm(
            USER_LOCATION.lat,
            USER_LOCATION.lon,
            lat,
            lon
        );

        if (d < bestDistance) {
            bestDistance = d;
            bestFacility = f;
        }
    });

    if (bestFacility) {
        L.popup().setLatLng([bestFacility.lat, bestFacility.lon])
            .setContent(`
                 <strong>Recommended ER</strong><br/>
                 ${bestFacility.name}<br/>
                 Distance: ${bestDistance.toFixed(2)} km<br/>
                 Wait Time: ${bestFacility.wait} min`).openOn(ER_MAP);
    }
}
function return_to_input() {
    if (ER_MAP) {
        ER_MAP.flyTo([INPUT_POSITION.lat, INPUT_POSITION.lon], INPUT_ZOOM);
        ER_MAP.dragging.enable();
        ER_MAP.boxZoom.enable();
        ER_MAP.doubleClickZoom.enable();
        ER_MAP.scrollWheelZoom.enable();
        ER_MAP.touchZoom.enable();
        USE_INPUT = false;
    }

    unfadeERMarkers();
}
function get_search_pk(esi, injury, priority, autozoom) {
    // remove services that can't serve based on trauma level
    // if none exist, then go to less capable service
    const levels = ['I','II','III','IV','V'];
    var possible = [];
    var tesi = esi;

    var lat =  41.8;
    var lon = -91.6;

    // get lat and lon from the search marker
    if (SEARCH_MARKER) {
        const ltln = SEARCH_MARKER.getLatLng();
        lat = ltln.lat;
        lon = ltln.lng;
        INPUT_POSITION = { lat, lon };
        INPUT_ZOOM = ER_MAP.getZoom();
    }

    // don't tell people there's nothing
    while (possible.length == 0) {
        possible = FACILITIES_CACHE.filter((f) => levels.findIndex((l) => l === f.trauma) + 1 <= tesi);
        tesi += 1;
    }

    // expected rate of travel. Assumes 50 km/h for now.
    const kmm = 50 / 60;
    var scorer;
    switch (priority) {
        case 0: // DIST + WAIT
            scorer = (f) => f.wait + distanceKm(f.lat, f.lon, lat, lon) / kmm;
            break;
        case 1: // DIST ONLY
            scorer = (f) => distanceKm(f.lat, f.lon, lat, lon) / kmm;
            break;
        case 2: // WAIT ONLY
            scorer = (f) => f.wait;
            break;
    }
    const compare = (a,b) => scorer(a) - scorer(b);
    possible.sort(compare);
    var result = possible.slice(0,5).reduce((acc, f) => acc+f.id+',', '');

    if (autozoom && ER_MAP) {
        ER_MAP.flyToBounds([[possible[0].lat, possible[0].lon],[lat, lon]], { padding : [30,30]});
        ER_MAP.dragging.disable();
        ER_MAP.boxZoom.disable();
        ER_MAP.doubleClickZoom.disable();
        ER_MAP.scrollWheelZoom.disable();
        ER_MAP.touchZoom.disable();
        USE_INPUT = true;
        fadeERMarkers(possible[0].id);
        FADED_PK = possible[0].id;
    }

    return result;
}