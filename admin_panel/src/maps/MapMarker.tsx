import {LatLng, LatLngTuple} from "leaflet";
import * as uuidv4 from 'uuid/v4';

export interface GridPos {
    x: number,
    y: number,
}

function toLatLng(g: GridPos): LatLngTuple {
    return [g.y, g.x];
}

function fromLatLng(l: LatLng): GridPos {
    return {
        x: l.lng,
        y: l.lat,
    };
}

export const GridPos = {
    toLatLng,
    fromLatLng,
};

export interface MapMarker {
    id: string,
    mapId: string,

    name: string,
    description: string,

    pos: GridPos,

}

/**
 * Gives a function that returns true iff the marker provided has the given map id
 */
function filterOnMapId(id: string): (mark: MapMarker) => boolean {
    return (mark) => {
        return mark.mapId === id;
    };
}

function sortByName(a: MapMarker, b: MapMarker): number {
    return a.name.localeCompare(b.name);
}

function create(mapId: string, pos: GridPos): MapMarker {
    const id = uuidv4();

    return {
        id,
        mapId,
        pos,
        name: "New Marker",
        description: "",
    }
}
export const MapMarker = {
    filterOnMapId,
    sortByName,
    create,
};
