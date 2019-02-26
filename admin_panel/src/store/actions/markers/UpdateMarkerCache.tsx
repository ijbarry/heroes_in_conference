import {Action} from "redux";
import {AppActionTypes} from "../../AppActions";
import {MapMarker} from "../../../maps/MapMarker";
import {MutableCache} from "../../Cache";


export interface UpdateMarkerCacheAction extends Action<AppActionTypes> {
    type: AppActionTypes.UPDATE_MARKER_CACHE,
    cache: MutableCache<MapMarker>,
}

export function updateMarkerCache(cache: MutableCache<MapMarker>): UpdateMarkerCacheAction {
    return {
        type: AppActionTypes.UPDATE_MARKER_CACHE,
        cache,
    };
}