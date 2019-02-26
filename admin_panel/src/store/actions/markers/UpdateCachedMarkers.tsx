import {AppActionTypes} from "../../AppActions";
import {Action} from "redux";
import {MapMarker} from "../../../maps/MapMarker";
import {LoadedContainer} from "../../Container";
import {IDMap} from "../../IDMap";


export interface UpdateCachedMarkersAction extends Action<AppActionTypes> {
    type: AppActionTypes.UPDATE_CACHED_MARKERS,
    markers: IDMap<LoadedContainer<MapMarker> | null>,
}

export function updateCachedMarkers(markers: IDMap<LoadedContainer<MapMarker> | null>): UpdateCachedMarkersAction {

    return {
        type: AppActionTypes.UPDATE_CACHED_MARKERS,
        markers,
    };
}